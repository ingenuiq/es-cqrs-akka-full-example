package com.ingenuiq.note.query.events

import akka.actor.Props
import com.ingenuiq.note.command.note.{ NoteAggregateActor, PersistentNoteEvent }
import com.ingenuiq.note.command.persistence.TaggingEventAdapter
import com.ingenuiq.note.query.common.ViewBuilderActor.Action
import com.ingenuiq.note.query.common.{ BaseViewActor, PersistedEventEnvelope, ViewBuilderActor }
import com.ingenuiq.note.query.dao.model.NoteEventSql
import com.ingenuiq.note.query.dao.repos.NoteEventRepo
import com.ingenuiq.note.query.events.NoteEventQuery.GetNoteEvents
import com.ingenuiq.note.query.events.NoteEventQueryResponse.NoteEventsFetchedResponse

object NoteEventViewBuilder {
  val name    = "noteEventViewBuilder"
  def apply() = Props(classOf[NoteEventViewBuilder])
}

class NoteEventViewBuilder extends ViewBuilderActor {
  override def tag: String = TaggingEventAdapter.noteTag

  override def identifier: String = "NoteEventViewBuilder"

  import context.dispatcher
  val noteEventRepo: NoteEventRepo = new NoteEventRepo

  override def actionFor(env: PersistedEventEnvelope): Action[_] =
    env.event match {
      case pne: PersistentNoteEvent =>
        () =>
          noteEventRepo.upsertNoteEvent(NoteEventSql.toSql(pne))
    }
}

object NoteEventView {

  val name = "noteEventView"

  def apply() = Props(classOf[NoteEventView])
}

class NoteEventView extends BaseViewActor {

  import context.dispatcher

  val noteEventRepo: NoteEventRepo = new NoteEventRepo

  override def receive: Receive = {
    case e: GetNoteEvents =>
      logger.trace("Received request to fetch note events")
      pipeResponse(
        noteEventRepo.getNoteEvents
          .map { res =>
            logger.trace("Received response from repo to fetch note events")
            NoteEventsFetchedResponse(res)
          }
      )

  }
}
