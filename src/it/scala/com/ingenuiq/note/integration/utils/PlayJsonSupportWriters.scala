package com.ingenuiq.note.integration.utils

import com.ingenuiq.note.command.note.Note
import com.ingenuiq.note.command.note.NoteEvent.NoteCreated
import com.ingenuiq.note.http.command.CommandRequest.NotePayload
import com.ingenuiq.note.query.common.PersistentEventMetadata
import play.api.libs.json._

trait PlayJsonSupportWriters {

  implicit lazy val writes1: Writes[NotePayload] = Json.writes[NotePayload]

  implicit lazy val noteWrites:                    Writes[Note]                    = Json.writes[Note]
  implicit lazy val persistentEventMetadataWrites: Writes[PersistentEventMetadata] = Json.writes[PersistentEventMetadata]
  implicit lazy val noteCreatedWrites:             Writes[NoteCreated]             = Json.writes[NoteCreated]
}
