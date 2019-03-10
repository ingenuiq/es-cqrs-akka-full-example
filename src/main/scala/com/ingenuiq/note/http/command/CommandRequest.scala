package com.ingenuiq.note.http.command

import com.ingenuiq.note.command.note.Note
import com.ingenuiq.note.command.note.NoteCommand.{ CreateNote, UpdateNote }
import com.ingenuiq.note.common.{ NoteId, UserId }
import com.ingenuiq.note.serialization.PlayJsonSupport
import play.api.libs.json._

object CommandRequest extends PlayJsonSupport {

  case class NotePayload(title: Option[String], content: Option[String]) {

    def toCreateCommand(userId: UserId): CreateNote =
      CreateNote(userId = userId, note = NotePayload.toNote(NoteId.generateNew, this))

    def toUpdateCommand(userId: UserId, noteId: NoteId): UpdateNote =
      UpdateNote(userId = userId, note = NotePayload.toNote(noteId, this))
  }

  object NotePayload {

    private def toNote(noteId: NoteId, payload: NotePayload): Note =
      Note(id = noteId, title = payload.title, content = payload.content)

    implicit val reads: Reads[NotePayload] = Json.reads[NotePayload]
  }

}
