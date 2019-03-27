package com.ingenuiq.note.query.common

import akka.NotUsed
import akka.actor.{ Actor, PoisonPill }
import akka.pattern.pipe
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{ EventEnvelope, Offset, PersistenceQuery }
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import com.typesafe.scalalogging.LazyLogging
import kamon.Kamon
import kamon.trace.{ IdentityProvider, Span, SpanContext }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.util.{ Failure, Success }

object ViewBuilderActor {

  type Action[O] = () => Future[O]

  case class EnvelopeAndFunction(env:   PersistedEventEnvelope, action: Action[_])
  case class LatestOffsetResult(offset: Offset)
}

case class PersistedEventEnvelope(offset: Offset, tag: String, event: PersistentEvent, span: Span)

abstract class ViewBuilderActor extends Actor with LazyLogging {

  import ViewBuilderActor._
  import context.dispatcher

  val identityProvider: IdentityProvider = new IdentityProvider.Default

  val decider: Supervision.Decider = {
    case NonFatal(ex) =>
      logger.error(s"Got non fatal exception in ViewBuilder $identifier flow", ex)
      Supervision.Resume
    case ex =>
      logger.error(s"Got fatal exception in ViewBuilder $identifier flow, stream will be stopped", ex)
      Supervision.Stop
  }

  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(context.system).withSupervisionStrategy(decider)
  )

  val resumableProjection = ResumableProjection(identifier, context.system)

  val eventsFlow: Flow[EventEnvelope, Unit, NotUsed] =
    Flow[EventEnvelope]
      .collect {
        case EventEnvelope(offset, persistenceId, _, event: PersistentEvent) =>
          val parentSpan = Span.Remote(
            SpanContext(
              traceID          = identityProvider.traceIdGenerator().from(event.persistentEventMetadata.correlationId.value),
              spanID           = identityProvider.spanIdGenerator().generate(),
              parentID         = identityProvider.spanIdGenerator().from(event.persistentEventMetadata.spanId),
              samplingDecision = SpanContext.SamplingDecision.Sample
            )
          )
          val span = Kamon.buildSpan("Reply from ES").asChildOf(parentSpan).start()
          PersistedEventEnvelope(offset, persistenceId, event, span)
        case x =>
          throw new RuntimeException(s"Invalid event in the journal! $x")
      }
      .map(env => EnvelopeAndFunction(env, actionFor(env)))
      .mapAsync(parallelism = 1) { case EnvelopeAndFunction(env, f) => f.apply().map(_ => env) }
      .mapAsync(parallelism = 1) { env =>
        val span = resumableProjection.updateOffset(identifier, env.offset).map(_ => ())
        env.span.finish()
        span
      }

  val journal: CassandraReadJournal = PersistenceQuery(context.system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  self ! "start"

  def tag: String

  def identifier: String

  def actionFor(env: PersistedEventEnvelope): Action[_]

  def receive: PartialFunction[Any, Unit] = {
    case "start" =>
      resumableProjection.fetchLatestOffset(identifier).map(LatestOffsetResult).pipeTo(self)
    case LatestOffsetResult(offset) =>
      logger.info(s"Starting up view builder for entity $identifier, with tag $tag with offset of $offset")
      val eventsSource: Source[EventEnvelope, NotUsed] = journal.eventsByTag(tag, offset)

      eventsSource
        .via(eventsFlow)
        .runWith(Sink.ignore)
        .onComplete {
          case Failure(err) =>
            logger.error(s"Persistence query $identifier ended with failure: ${err.getMessage}")
            self ! PoisonPill
          case Success(_) =>
            logger.error(s"Persistence query $identifier ended successfully")
            context.system.scheduler.scheduleOnce(1.second, self, "start")
        }
    case x =>
      logger.error(s"Failed to obtain offset for $identifier, got this message ${x.toString}")
      self ! PoisonPill
  }

}
