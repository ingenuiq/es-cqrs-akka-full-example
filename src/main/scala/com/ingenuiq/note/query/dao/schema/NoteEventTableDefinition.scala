package com.ingenuiq.note.query.dao.schema

import java.time.LocalDateTime

import com.ingenuiq.note.common.{ CorrelationId, EventId, NoteId, UserId }
import com.ingenuiq.note.query.dao.common.DbTypeMappers
import com.ingenuiq.note.query.dao.model.NoteEventSql

import scala.language.higherKinds

trait NoteEventTableDefinition extends DbTypeMappers {

  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  val noteEvents = TableQuery[NoteEventTable]

  final class NoteEventTable(slickTag: Tag) extends Table[NoteEventSql](slickTag, _tableName = "note_events") {

    val eventId:       Rep[EventId]       = column[EventId]("event_id", O.PrimaryKey)
    val userId:        Rep[UserId]        = column[UserId]("user_id")
    val noteId:        Rep[NoteId]        = column[NoteId]("note_id")
    val eventName:     Rep[String]        = column[String]("event_name")
    val lastModified:  Rep[LocalDateTime] = column[LocalDateTime]("last_modified")
    val correlationId: Rep[CorrelationId] = column[CorrelationId]("correlation_id")

    def * = (eventId, userId, noteId, eventName, lastModified, correlationId).mapTo[NoteEventSql]

  }

}
