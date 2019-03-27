package com.ingenuiq.note.http.command

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import com.ingenuiq.note.command.note.NoteCommand._
import com.ingenuiq.note.command.note.NoteEvent._
import com.ingenuiq.note.common.{ NoteId, PredefinedTimeout, UserId }
import com.ingenuiq.note.http.RouteHelpers
import com.ingenuiq.note.http.model.ErrorMessageResponse
import com.ingenuiq.note.serialization.PlayJsonSupport
import com.ingenuiq.note.settings.Settings
import com.typesafe.scalalogging.LazyLogging

class CommandRoutes(commandActor: ActorRef, val settings: Settings)
    extends PlayJsonSupport
    with LazyLogging
    with PredefinedTimeout
    with RouteHelpers {

  def routes(implicit userId: UserId): Route =
    pathTail(NotePath) {
      post(createNote)
    } ~
      pathTail(NotePath / ID) { id: NoteId =>
        put(updateNote(id)) ~ delete(deleteNote(id))
      }

  def createNote(implicit userId: UserId): Route =
    decodeRequest {
      entity(as[CommandRequest.NotePayload]) { payload =>
        onSuccess(commandActor ? payload.toCreateCommand(userId)) {
          case e: NoteCreated =>
            logger.trace(s"Note created response")
            complete(StatusCodes.Created -> CommandResponse.NoteCreationResponse(e.note.id))
          case NoteAlreadyExists =>
            complete(StatusCodes.BadRequest -> ErrorMessageResponse("Note already exists"))
        }
      }
    }

  def updateNote(noteId: NoteId)(implicit userId: UserId): Route =
    pathEndOrSingleSlash {
      put {
        decodeRequest {
          entity(as[CommandRequest.NotePayload]) { payload =>
            onSuccess(commandActor ? payload.toUpdateCommand(userId, noteId)) {
              case _: NoteUpdated =>
                logger.trace(s"Note updated response")
                complete(StatusCodes.OK -> CommandResponse.NoteUpdateResponse(noteId))
              case NoteNotFound =>
                logger.trace("Note not found")
                complete(StatusCodes.NotFound -> ErrorMessageResponse("Note not found"))
            }
          }
        }
      }
    }

  def deleteNote(noteId: NoteId)(implicit userId: UserId): Route =
    decodeRequest {
      onSuccess(commandActor ? DeleteNote(userId, noteId)) {
        case e: NoteDeleted =>
          logger.trace(s"Note deleted response")
          complete(StatusCodes.OK -> CommandResponse.NoteDeletionResponse(e.noteId))
        case NoteNotFound =>
          logger.trace("Note not found")
          complete(StatusCodes.NotFound -> ErrorMessageResponse("Note not found"))
      }
    }

}
