package com.ingenuiq.note.http.query

import com.ingenuiq.note.common.{ CorrelationId, NoteId, UserId }
import com.ingenuiq.note.http.PredefinedRoutePaths
import com.ingenuiq.note.query.common.Traceable
import com.ingenuiq.note.query.dao.model.{ NoteEventSql, NoteSql }
import com.ingenuiq.note.query.events.NoteEventQueryResponse
import com.ingenuiq.note.query.note.NoteQueryResponse
import com.ingenuiq.note.utils
import play.api.libs.json._

sealed trait QueryResponse extends Traceable

object QueryResponse extends PredefinedRoutePaths {

  case class NotesResponse(notes: Iterable[NoteResponse], correlationId: CorrelationId = CorrelationId(utils.currentTraceId))
      extends QueryResponse

  object NotesResponse {
    implicit val writes: Writes[NotesResponse] = Json.writes[NotesResponse]

    def toResponse(ohf: NoteQueryResponse.NotesFetchedResponse): NotesResponse =
      NotesResponse(ohf.notes.map(NoteResponse.toResponse))
  }

  case class NoteResponse(id: NoteId, title: Option[String], content: Option[String])

  object NoteResponse {
    implicit val writes: Writes[NoteResponse] = Json.writes[NoteResponse]

    def toResponse(n: NoteSql): NoteResponse =
      NoteResponse(id = n.id, title = n.title, content = n.content)
  }

  case class NoteEventsResponse(noteEvents: Iterable[NoteEventResponse], correlationId: CorrelationId = CorrelationId(utils.currentTraceId))
      extends QueryResponse

  object NoteEventsResponse {
    implicit val writes: Writes[NoteEventsResponse] = Json.writes[NoteEventsResponse]

    def toResponse(ohf: NoteEventQueryResponse.NoteEventsFetchedResponse): NoteEventsResponse =
      NoteEventsResponse(ohf.notes.map(NoteEventResponse.toResponse))
  }

  case class NoteEventResponse(userId: UserId, noteId: NoteId, eventName: String)

  object NoteEventResponse {
    implicit val writes: Writes[NoteEventResponse] = Json.writes[NoteEventResponse]

    def toResponse(n: NoteEventSql): NoteEventResponse =
      NoteEventResponse(userId = n.userId, noteId = n.noteId, eventName = n.eventName)
  }
}
