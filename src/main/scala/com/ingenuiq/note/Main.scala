package com.ingenuiq.note

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.ingenuiq.note.command.CommandSupervisorActor
import com.ingenuiq.note.http.BaseRoutes
import com.ingenuiq.note.query.QuerySupervisorActor
import com.ingenuiq.note.query.dao.TableDefinitionCreator
import com.ingenuiq.note.settings.Settings
import com.typesafe.scalalogging.LazyLogging
import kamon.Kamon
import kamon.zipkin.ZipkinReporter

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object Main extends App with KamonInit with LazyLogging with BaseRoutes {

  implicit val system:           ActorSystem       = ActorSystem("note-actor-system")
  implicit val materializer:     ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext  = system.dispatcher

  override val settings: Settings = Settings.conf

  new TableDefinitionCreator().rebuildSchema(settings.rebuildReadside)

  override val commandActor = system.actorOf(CommandSupervisorActor(), name = "commandActor")
  override val queryActor   = system.actorOf(QuerySupervisorActor(), name   = "queryActor")

  if (settings.tracingMonitoringSettings.zipkinEnabled) {
    logger.info("Zipkin tracing enabled")
//    Kamon.addReporter(new ZipkinReporter)
  }
  else
    logger.info("Zipkin tracing disabled")

  private val bindingFutureHttp: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes(commandActor, queryActor), settings.httpListenerSettings.interface, settings.httpListenerSettings.port)

  bindingFutureHttp.onComplete {
    case Success(_) =>
      logger.info(s"Server started on [${settings.httpListenerSettings.interface}:${settings.httpListenerSettings.port}]")
    case Failure(error) => logger.error(s"Error binding HTTP listener: $error")
  }

  sys.addShutdownHook {
    bindingFutureHttp.flatMap(_.unbind()).onComplete { _ =>
      materializer.shutdown()
      system.terminate()
    }
  }
}

trait KamonInit {
//  Kamon.init()
}
