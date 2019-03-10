package com.ingenuiq.note.query.dao.model

import java.time.LocalDateTime

import com.ingenuiq.note.command.note.Note
import com.ingenuiq.note.command.note.NoteEvent.NoteCreated
import com.ingenuiq.note.common.{ CorrelationId, NoteId }

case class NoteSql(id: NoteId, title: Option[String], content: Option[String], lastModified: LocalDateTime, correlationId: CorrelationId)

object NoteSql {

  def fromCreatedToSql(noteCreated: NoteCreated): NoteSql =
    toSql(
      note          = noteCreated.note,
      lastModified  = noteCreated.persistentEventMetadata.created,
      correlationId = noteCreated.persistentEventMetadata.correlationId
    )

  private def toSql(note: Note, lastModified: LocalDateTime, correlationId: CorrelationId): NoteSql =
    NoteSql(id = note.id, title = note.title, content = note.content, lastModified = lastModified, correlationId = correlationId)

  val tupled: ((NoteId, Option[String], Option[String], LocalDateTime, CorrelationId)) => NoteSql = (this.apply _).tupled

}
