package com.ingenuiq.note.http.query

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import com.ingenuiq.note.common._
import com.ingenuiq.note.http.RouteHelpers
import com.ingenuiq.note.http.model._
import com.ingenuiq.note.query.events.NoteEventQuery.GetNoteEvents
import com.ingenuiq.note.query.events.NoteEventQueryResponse
import com.ingenuiq.note.query.model.{ FailureResult, FullResult }
import com.ingenuiq.note.query.note.NoteQuery.GetNotes
import com.ingenuiq.note.query.note.{ NoteQuery, NoteQueryResponse }
import com.ingenuiq.note.serialization.PlayJsonSupport
import com.ingenuiq.note.settings.Settings
import com.typesafe.scalalogging.LazyLogging

class QueryRoutes(queryActor: ActorRef, settings: Settings)
    extends PlayJsonSupport
    with PredefinedTimeout
    with RouteHelpers
    with LazyLogging {

  def routes(implicit userId: UserId): Route =
    pathPrefix(QueryPath) {
      pathTail(NotePath) {
        get(getNotes)
      } ~
      pathTail(NotePath / EventPath) {
        get(getNoteEvents)
      } ~
      pathTail(NotePath / ID) { id: NoteId => get(getNoteById(id)) }
    }

  def getNoteEvents(implicit userId: UserId): Route =
    decodeRequest {
      onSuccess(queryActor ? GetNoteEvents(userId)) {
        case FullResult(e: NoteEventQueryResponse.NoteEventsFetchedResponse) =>
          logger.info("Note events response")
          complete(StatusCodes.OK -> QueryResponse.NoteEventsResponse.toResponse(e))
        case FailureResult(t, m, e) =>
          logger.error(s"Type $t, message: $m, exception ${e.map(_.getLocalizedMessage)}")
          complete(StatusCodes.InternalServerError -> ErrorMessageResponse())
      }
    }

  def getNotes(implicit userId: UserId): Route =
    decodeRequest {
      onSuccess(queryActor ? GetNotes(userId)) {
        case FullResult(e: NoteQueryResponse.NotesFetchedResponse) =>
          logger.info("Notes response")
          complete(StatusCodes.OK -> QueryResponse.NotesResponse.toResponse(e))
        case FailureResult(t, m, e) =>
          logger.error(s"Type $t, message: $m, exception ${e.map(_.getLocalizedMessage)}")
          complete(StatusCodes.InternalServerError -> ErrorMessageResponse())
      }
    }

  def getNoteById(id: NoteId)(implicit userId: UserId): Route =
    decodeRequest {
      onSuccess(queryActor ? NoteQuery.GetNote(userId, id)) {
        case FullResult(NoteQueryResponse.NoteFetchedResponse(Some(note))) =>
          logger.info("Note response")
          complete(StatusCodes.OK -> QueryResponse.NoteResponse.toResponse(note))
        case FullResult(NoteQueryResponse.NoteFetchedResponse(None)) =>
          logger.info("Note not found response")
          complete(StatusCodes.NotFound -> CorrelationIdResponse())
        case FailureResult(t, m, e) =>
          logger.error(s"Type $t, message: $m, exception ${e.map(_.getLocalizedMessage)}")
          complete(StatusCodes.InternalServerError -> ErrorMessageResponse())
      }
    }

}
