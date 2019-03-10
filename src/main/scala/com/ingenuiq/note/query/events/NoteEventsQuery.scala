package com.ingenuiq.note.query.events

import com.ingenuiq.note.common.UserId
import com.ingenuiq.note.query.common.{ Event, Query }
import com.ingenuiq.note.query.dao.model.NoteEventSql

sealed trait NoteEventQuery extends Query

object NoteEventQuery {
  case class GetNoteEvents(userId: UserId) extends NoteEventQuery

}

sealed trait NoteEventQueryResponse extends Event

object NoteEventQueryResponse {

  case class NoteEventsFetchedResponse(notes: Iterable[NoteEventSql]) extends NoteEventQueryResponse

}
