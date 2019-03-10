package com.ingenuiq.note.query.dao.schema

import com.ingenuiq.note.query.dao.common.DbTypeMappers
import com.ingenuiq.note.query.dao.model.PersistenceOffset
import slick.lifted.ProvenShape

trait PersistenceOffsetTableDefinition extends DbTypeMappers {

  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  val persistenceOffsets = TableQuery[PersistenceOffsetTable](new PersistenceOffsetTable(_))

  final class PersistenceOffsetTable(tag: Tag) extends Table[PersistenceOffset](tag, "persistence_offset") {

    def persistenceId = column[String]("persistence_id", O.PrimaryKey)
    def offset        = column[Long]("offset")

    def * : ProvenShape[PersistenceOffset] =
      (persistenceId, offset).mapTo[PersistenceOffset]
  }
}
