package com.ingenuiq.note.query.note

import akka.actor.Props
import com.ingenuiq.note.command.note.NoteEvent._
import com.ingenuiq.note.command.persistence.TaggingEventAdapter
import com.ingenuiq.note.query.common.ViewBuilderActor.Action
import com.ingenuiq.note.query.common.{ BaseViewActor, PersistedEventEnvelope, ViewBuilderActor }
import com.ingenuiq.note.query.dao.repos.NoteRepo
import com.ingenuiq.note.query.note.NoteQuery.{ GetNote, GetNotes }
import com.ingenuiq.note.query.note.NoteQueryResponse.{ NoteFetchedResponse, NotesFetchedResponse }

object NoteViewBuilder {
  val name    = "noteViewBuilder"
  def apply() = Props(classOf[NoteViewBuilder])
}

class NoteViewBuilder extends ViewBuilderActor {
  override def tag: String = TaggingEventAdapter.noteTag

  override def identifier: String = "NoteViewBuilder"

  import context.dispatcher
  val noteRepo: NoteRepo = new NoteRepo

  override def actionFor(env: PersistedEventEnvelope): Action[_] =
    env.event match {
      case ur: NoteCreated =>
        () =>
          noteRepo.insertNote(ur)
      case ur: NoteUpdated =>
        () =>
          noteRepo.updateNote(ur)
      case ur: NoteDeleted =>
        () =>
          noteRepo.removeNote(ur)
    }
}

object NoteView {

  val name = "noteHistoryView"

  def apply() = Props(classOf[NoteView])
}

class NoteView extends BaseViewActor {

  import context.dispatcher

  val noteRepo: NoteRepo = new NoteRepo

  override def receive: Receive = {
    case e: GetNotes =>
      logger.trace("Received request to fetch notes")
      pipeResponse(
        noteRepo.getNotes
          .map { res =>
            logger.trace("Received response from repo to fetch notes")
            NotesFetchedResponse(res)
          }
      )

    case e: GetNote =>
      logger.trace("Received request to fetch note")
      pipeResponse(noteRepo.getNote(e.noteId).map { res =>
        logger.trace("Received response from repo to fetch note")
        NoteFetchedResponse(res)
      })
  }
}
