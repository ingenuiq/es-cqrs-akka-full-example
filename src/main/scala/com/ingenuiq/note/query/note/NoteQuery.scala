package com.ingenuiq.note.query.note

import com.ingenuiq.note.common.{ NoteId, UserId }
import com.ingenuiq.note.query.common.{ Event, Query }
import com.ingenuiq.note.query.dao.model.NoteSql

sealed trait NoteQuery extends Query

object NoteQuery {
  case class GetNotes(userId: UserId) extends NoteQuery

  case class GetNote(userId: UserId, noteId: NoteId) extends NoteQuery
}

sealed trait NoteQueryResponse extends Event

object NoteQueryResponse {

  case class NotesFetchedResponse(notes: Iterable[NoteSql]) extends NoteQueryResponse
  case class NoteFetchedResponse(note:   Option[NoteSql]) extends NoteQueryResponse

}
