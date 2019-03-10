package com.ingenuiq.note.integration.utils

import java.util.UUID

import com.ingenuiq.note.common.{ CorrelationId, NoteId, UserId }
import com.ingenuiq.note.http.command.CommandRequest.NotePayload
import com.ingenuiq.note.http.command.CommandResponse._
import com.ingenuiq.note.http.model.{ CorrelationIdResponse, ErrorMessageResponse }
import com.ingenuiq.note.http.query.QueryResponse._
import play.api.libs.json._

trait PlayJsonSupportReaders {

  implicit lazy val treads: Reads[NoteId] = Reads[NoteId] {
    case JsString(str) => JsSuccess(NoteId(UUID.fromString(str)))
    case _             => JsError("Unparsable UUID")
  }

  implicit lazy val creads: Reads[CorrelationId] = Reads[CorrelationId] {
    case JsString(str) => JsSuccess(CorrelationId(str))
    case _             => JsError("Not JsString for CorrelationId")
  }

  implicit lazy val ureads: Reads[UserId] = Reads[UserId] {
    case JsString(str) => JsSuccess(UserId(str))
    case _             => JsError("Unparsable UUID")
  }

  implicit lazy val readsNoteEventResponse:      Reads[NoteEventResponse]     = Json.reads[NoteEventResponse]
  implicit lazy val readsNoteEventsResponse:     Reads[NoteEventsResponse]    = Json.reads[NoteEventsResponse]
  implicit lazy val readsNoteDetailsResponse:    Reads[NoteResponse]          = Json.reads[NoteResponse]
  implicit lazy val readsNotesTableRowsResponse: Reads[NotesResponse]         = Json.reads[NotesResponse]
  implicit lazy val readsNoteCreationResponse:   Reads[NoteCreationResponse]  = Json.reads[NoteCreationResponse]
  implicit lazy val readsNoteUpdateResponse:     Reads[NoteUpdateResponse]    = Json.reads[NoteUpdateResponse]
  implicit lazy val readsNoteDeletionResponse:   Reads[NoteDeletionResponse]  = Json.reads[NoteDeletionResponse]
  implicit lazy val readsErrorMessageResponse:   Reads[ErrorMessageResponse]  = Json.reads[ErrorMessageResponse]
  implicit lazy val readsCorrelationIdResponse:  Reads[CorrelationIdResponse] = Json.reads[CorrelationIdResponse]
  implicit lazy val readsNotePayload:            Reads[NotePayload]           = Json.reads[NotePayload]
}
