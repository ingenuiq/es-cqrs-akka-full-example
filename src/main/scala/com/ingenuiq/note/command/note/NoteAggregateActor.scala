package com.ingenuiq.note.command.note

import akka.actor.{ ActorLogging, Props }
import akka.cluster.sharding.ShardRegion
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import com.ingenuiq.note.command.note.NoteCommand._
import com.ingenuiq.note.command.note.NoteEvent._
import com.ingenuiq.note.common.UserId
import com.ingenuiq.note.query.common.PersistentEventMetadata

object NoteAggregateActor {

  def apply(): Props = Props(classOf[NoteAggregateActor], "")

  val persistenceId: String = "NoteAggregateActor"

  private val numberOfShards: Int = 30

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case e: NoteCommand => (e.noteId.value.toString, e)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case e: NoteCommand => (e.noteId.value.hashCode.abs % numberOfShards).toString
    case ShardRegion.StartEntity(id) =>
      // StartEntity is used by remembering entities feature
      (id.toLong % numberOfShards).toString
  }

}

class NoteAggregateActor(val persistenceIdAddon: String = "") extends PersistentActor with ActorLogging {

  var noteState: Option[Note] = None

  override def receiveRecover: Receive = {
    case e: PersistentNoteEvent => changeState(e)
    case RecoveryCompleted =>
  }

  override def receiveCommand: Receive = {
    case CreateNote(userId, note) =>
      noteState match {
        case Some(_) =>
          log.info(s"Received create note with note id that already exists, ${note.id}")
          sender() ! NoteAlreadyExists
        case None =>
          persist(NoteCreated(PersistentEventMetadata(userId), note)) { e =>
            log.debug("Persisted note creation")
            changeState(e)
            sender() ! e

          }
      }

    case UpdateNote(userId, updatedNote) =>
      noteState match {
        case None =>
          log.info("Received update for note that doesn't exit")
          sender() ! NoteNotFound
        case Some(existingNote) =>
          calculateUpdate(userId, existingNote, updatedNote) match {
            case None => sender() ! NoteNoChangesToUpdateFound
            case Some(noteUpdated) =>
              persist(noteUpdated) { e =>
                log.debug("Persisted note updated")
                changeState(e)
                sender() ! e
              }
          }
      }

    case DeleteNote(userId, noteId) =>
      noteState match {
        case Some(_) =>
          persist(NoteDeleted(PersistentEventMetadata(userId), noteId)) { e =>
            log.debug("Persisted note deletion")
            changeState(e)
            sender() ! e
          }
        case None =>
          log.info("Received deletion of a note that doesn't exit")
          sender() ! NoteNotFound
      }
  }

  def calculateUpdate(userId: UserId, currentNote: Note, updatedNote: Note): Option[NoteUpdated] = {
    val titleUpdate =
      if (currentNote.title != updatedNote.title)
        Some(updatedNote.title)
      else None
    val contentUpdate =
      if (currentNote.content != updatedNote.content)
        Some(updatedNote.content)
      else None

    (titleUpdate, contentUpdate) match {
      case (None, None) => None
      case _            => Some(NoteUpdated(PersistentEventMetadata(userId), currentNote.id, titleUpdate, contentUpdate))
    }
  }

  def changeState: PartialFunction[PersistentNoteEvent, Unit] = {
    case e: NoteCreated =>
      noteState = Some(e.note)
    case e: NoteUpdated =>
      noteState match {
        case Some(note) =>
          val updatedNote = (e.title, e.content) match {
            case (Some(updateTitle), Some(updateContent)) => note.copy(title   = updateTitle, content = updateContent)
            case (Some(updateTitle), None)                => note.copy(title   = updateTitle)
            case (None, Some(updateContent))              => note.copy(content = updateContent)
            case _ =>
              log.warning(s"Received event $e but there is no updates")
              note
          }

          noteState = Some(updatedNote)
        case None =>
          log.warning(s"Received event $e but there is no note")
      }
    case e: NoteDeleted =>
      noteState = None

  }

  override def persistenceId: String = NoteAggregateActor.persistenceId + "-" + self.path.name + persistenceIdAddon

}
