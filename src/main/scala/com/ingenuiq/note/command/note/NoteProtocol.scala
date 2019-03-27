package com.ingenuiq.note.command.note

import com.ingenuiq.note.common.{ EventId, NoteId, UserId }
import com.ingenuiq.note.query.common.{ Command, Event, PersistentEvent, PersistentEventMetadata }

sealed trait NoteCommand extends Command {
  def noteId: NoteId
}

object NoteCommand {

  case class CreateNote(userId: UserId, note: Note) extends NoteCommand {
    override def noteId: NoteId = note.id
  }

  case class UpdateNote(userId: UserId, note: Note) extends NoteCommand {
    override def noteId: NoteId = note.id
  }

  case class DeleteNote(userId: UserId, noteId: NoteId) extends NoteCommand

}

sealed trait NoteEvent extends Event

sealed trait NoteQueryEvent extends NoteEvent
sealed trait PersistentNoteEvent extends NoteEvent with PersistentEvent {
  def noteId: NoteId
}

object NoteEvent {

  case class NoteCreated(persistentEventMetadata: PersistentEventMetadata, note: Note) extends PersistentNoteEvent {
    override def noteId: NoteId = note.id
  }

  case class NoteDeleted(persistentEventMetadata: PersistentEventMetadata, noteId: NoteId) extends PersistentNoteEvent

  case class NoteUpdated(persistentEventMetadata: PersistentEventMetadata,
                         noteId:                  NoteId,
                         title:                   Option[Option[String]],
                         content:                 Option[Option[String]])
      extends PersistentNoteEvent

  case object NoteNoChangesToUpdateFound extends NoteQueryEvent
  case object NoteAlreadyExists extends NoteQueryEvent
  case object NoteNotFound extends NoteQueryEvent

}
