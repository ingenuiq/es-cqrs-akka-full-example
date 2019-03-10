package com.ingenuiq.note.query.dao.schema

import java.time.LocalDateTime

import com.ingenuiq.note.common.{ CorrelationId, NoteId }
import com.ingenuiq.note.query.dao.common.DbTypeMappers
import com.ingenuiq.note.query.dao.model.NoteSql
import slick.lifted.ProvenShape

import scala.language.higherKinds

trait NoteTableDefinition extends DbTypeMappers {

  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  val notes = TableQuery[NoteTable]

  final class NoteTable(slickTag: Tag) extends Table[NoteSql](slickTag, _tableName = "notes") {

    val id:            Rep[NoteId]         = column[NoteId]("id", O.PrimaryKey)
    val title:         Rep[Option[String]] = column[Option[String]]("title")
    val content:       Rep[Option[String]] = column[Option[String]]("content")
    val lastModified:  Rep[LocalDateTime]  = column[LocalDateTime]("last_modified")
    val correlationId: Rep[CorrelationId]  = column[CorrelationId]("correlation_id")

    def * = (id, title, content, lastModified, correlationId).mapTo[NoteSql]

  }

}
