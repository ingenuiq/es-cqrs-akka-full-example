package com.ingenuiq.note.query.dao.model

import java.time.LocalDateTime

import com.ingenuiq.note.command.note.NoteEvent.{ NoteCreated, NoteDeleted, NoteUpdated }
import com.ingenuiq.note.command.note.PersistentNoteEvent
import com.ingenuiq.note.common.{ CorrelationId, EventId, NoteId, UserId }

case class NoteEventSql(eventId:       EventId,
                        userId:        UserId,
                        noteId:        NoteId,
                        eventName:     String,
                        lastModified:  LocalDateTime,
                        correlationId: CorrelationId)

object NoteEventSql {

  def toSql(pne: PersistentNoteEvent): NoteEventSql = {
    val name = pne match {
      case _: NoteCreated => "Note created"
      case _: NoteUpdated => "Note updated"
      case _: NoteDeleted => "Note deleted"
    }

    NoteEventSql(
      pne.persistentEventMetadata.eventId,
      pne.persistentEventMetadata.userId,
      pne.noteId,
      name,
      pne.persistentEventMetadata.created,
      pne.persistentEventMetadata.correlationId
    )
  }

  val tupled: ((EventId, UserId, NoteId, String, LocalDateTime, CorrelationId)) => NoteEventSql = (this.apply _).tupled

}
