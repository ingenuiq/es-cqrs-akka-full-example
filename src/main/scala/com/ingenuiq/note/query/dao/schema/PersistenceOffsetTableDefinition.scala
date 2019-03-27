package com.ingenuiq.note.query.dao.schema

import akka.persistence.query.Offset
import com.ingenuiq.note.query.dao.common.DbTypeMappers
import com.ingenuiq.note.query.dao.model.PersistenceOffset
import slick.lifted.ProvenShape

trait PersistenceOffsetTableDefinition extends DbTypeMappers {

  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  val persistenceOffsets = TableQuery[PersistenceOffsetTable](new PersistenceOffsetTable(_))

  final class PersistenceOffsetTable(tag: Tag) extends Table[PersistenceOffset](tag, "persistence_offset") {

    def tagId:  Rep[String] = column[String]("tag", O.PrimaryKey)
    def offset: Rep[Offset] = column[Offset]("offset")

    def * : ProvenShape[PersistenceOffset] =
      (tagId, offset).mapTo[PersistenceOffset]
  }
}
