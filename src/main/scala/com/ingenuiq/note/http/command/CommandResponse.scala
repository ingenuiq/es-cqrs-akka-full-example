package com.ingenuiq.note.http.command

import com.ingenuiq.note.common.{ CorrelationId, NoteId }
import com.ingenuiq.note.utils
import play.api.libs.json._

sealed trait CommandResponse

object CommandResponse {

  case class NoteCreationResponse(noteId: NoteId, correlationId: CorrelationId = CorrelationId(utils.currentTraceId))
      extends CommandResponse

  object NoteCreationResponse {
    implicit val writes: Writes[NoteCreationResponse] = Json.writes[NoteCreationResponse]
  }

  case class NoteUpdateResponse(noteId: NoteId, correlationId: CorrelationId = CorrelationId(utils.currentTraceId)) extends CommandResponse

  object NoteUpdateResponse {
    implicit val writes: Writes[NoteUpdateResponse] = Json.writes[NoteUpdateResponse]
  }

  case class NoteDeletionResponse(noteId: NoteId, correlationId: CorrelationId = CorrelationId(utils.currentTraceId))
      extends CommandResponse

  object NoteDeletionResponse {
    implicit val writes: Writes[NoteDeletionResponse] = Json.writes[NoteDeletionResponse]
  }

}
