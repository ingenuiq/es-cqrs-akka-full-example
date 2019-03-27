package com.ingenuiq.note.query.dao.repos

import akka.persistence.query.Offset
import com.ingenuiq.note.query.dao.model.PersistenceOffset
import com.ingenuiq.note.query.dao.schema.PersistenceOffsetTableDefinition
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ ExecutionContext, Future }

class PersistenceOffsetRepo(implicit ec: ExecutionContext) extends PersistenceOffsetTableDefinition with LazyLogging {

  import com.ingenuiq.note.query.dao.common.DBComponent.db
  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  def upsert(po: PersistenceOffset): Future[Int] = {
    logger.trace(s"Updating persistence offset ${po.tag} to ${po.offset}")
    db.run(persistenceOffsets.insertOrUpdate(po))
  }

  def getByPersistenceId(tag: String): Future[PersistenceOffset] = {
    logger.trace(s"Get offset for $tag")
    val query = persistenceOffsets.filter(_.tagId === tag)
    db.run(query.result).map(_.headOption.getOrElse(PersistenceOffset(tag, Offset.noOffset)))
  }

}

object PersistenceOffsetRepo {
  def apply()(implicit ec: ExecutionContext): PersistenceOffsetRepo = new PersistenceOffsetRepo
}
