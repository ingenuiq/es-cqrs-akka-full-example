package com.ingenuiq.note.http

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.DebuggingDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.ingenuiq.note.common.{ PredefinedTimeout, UserId }
import com.ingenuiq.note.http.command.CommandRoutes
import com.ingenuiq.note.http.model.ErrorMessageResponse
import com.ingenuiq.note.http.query.QueryRoutes
import com.ingenuiq.note.serialization.PlayJsonSupport
import com.ingenuiq.note.settings.Settings
import com.typesafe.scalalogging.LazyLogging

trait BaseRoutes extends LazyLogging with PlayJsonSupport with HealthCheckRoute with PredefinedTimeout {

  val commandActor: ActorRef
  val queryActor:   ActorRef
  val settings:     Settings

  lazy val commandRoutes: CommandRoutes = new CommandRoutes(commandActor, settings)
  lazy val queryRoutes:   QueryRoutes   = new QueryRoutes(queryActor, settings)

  private val rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .result()
      .withFallback(RejectionHandler.default)
      .mapRejectionResponse {
        case res @ HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
          // since all Akka default rejection responses are Strict this will handle all rejections
          val message = ent.data.utf8String.replaceAll("\"", """\"""")
          res.copy(entity = HttpEntity(ContentTypes.`application/json`, s"""{"rejection": "$message"}"""))

        case x => x // pass through all other types of responses
      }

  private val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e =>
      extractUri { uri =>
        logger.error(s"Exception while handling request $uri", e)
        complete(StatusCodes.InternalServerError -> ErrorMessageResponse())
      }

  }

  private val handleErrors: Directive[Unit] = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

  private val corsSettings: CorsSettings = CorsSettings.defaultSettings
    .withAllowedOrigins(HttpOriginMatcher.*)
    .withAllowedMethods(List(HttpMethods.PUT, HttpMethods.GET, HttpMethods.POST, HttpMethods.OPTIONS, HttpMethods.DELETE))

  private def publicRoutes: Route = healthCheckRoute

  private def securedRoutes(commandActor: ActorRef, queryActor: ActorRef): Route =
    pathPrefix(PredefinedRoutePaths.BasePath) {
      DebuggingDirectives.logRequest(("", Logging.DebugLevel)) {
        implicit val userId: UserId = UserId.generateNew // login missing
        commandRoutes.routes ~ queryRoutes.routes
      }
    }

  def routes(commandActor: ActorRef, queryActor: ActorRef): Route =
    cors(corsSettings) {
      handleErrors {
        publicRoutes ~ securedRoutes(commandActor, queryActor)
      }
    }

}
