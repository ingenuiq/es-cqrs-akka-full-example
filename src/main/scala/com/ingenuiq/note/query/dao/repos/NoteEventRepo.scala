package com.ingenuiq.note.query.dao.repos

import com.ingenuiq.note.query.dao.common.QueryFilterOptions
import com.ingenuiq.note.query.dao.model.NoteEventSql
import com.ingenuiq.note.query.dao.schema._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.higherKinds

class NoteEventRepo(implicit ec: ExecutionContext) extends NoteEventTableDefinition with LazyLogging with QueryFilterOptions {

  import com.ingenuiq.note.query.dao.common.DBComponent.db
  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  def upsertNoteEvent(ne: NoteEventSql): Future[Int] = {
    logger.trace("Upserting a note event")
    db.run(noteEvents.insertOrUpdate(ne))
  }

  def getNoteEvents: Future[Seq[NoteEventSql]] = {
    logger.trace("Querying note events")
    db.run(noteEvents.result)
  }

}
