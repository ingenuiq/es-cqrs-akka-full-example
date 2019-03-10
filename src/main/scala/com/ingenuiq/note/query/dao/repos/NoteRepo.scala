package com.ingenuiq.note.query.dao.repos

import com.ingenuiq.note.command.note.NoteEvent._
import com.ingenuiq.note.common.NoteId
import com.ingenuiq.note.query.dao.common.QueryFilterOptions
import com.ingenuiq.note.query.dao.model.NoteSql
import com.ingenuiq.note.query.dao.schema._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.higherKinds

class NoteRepo(implicit ec: ExecutionContext) extends NoteTableDefinition with LazyLogging with QueryFilterOptions {

  import com.ingenuiq.note.query.dao.common.DBComponent.db
  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  def insertNote(po: NoteCreated): Future[Int] = {
    logger.trace("Inserting a note")
    db.run(notes += NoteSql.fromCreatedToSql(po))
  }

  def updateNote(po: NoteUpdated): Future[Int] = {
    logger.trace("Updating note")

    (po.title, po.content) match {
      case (Some(title), Some(content)) =>
        db.run(
          notes
            .filter(_.id === po.noteId)
            .map(x => (x.title, x.content, x.lastModified, x.correlationId))
            .update((title, content, po.persistentEventMetadata.created, po.persistentEventMetadata.correlationId))
        )
      case (Some(title), None) =>
        db.run(
          notes
            .filter(_.id === po.noteId)
            .map(x => (x.title, x.lastModified, x.correlationId))
            .update((title, po.persistentEventMetadata.created, po.persistentEventMetadata.correlationId))
        )
      case (None, Some(content)) =>
        db.run(
          notes
            .filter(_.id === po.noteId)
            .map(x => (x.content, x.lastModified, x.correlationId))
            .update((content, po.persistentEventMetadata.created, po.persistentEventMetadata.correlationId))
        )
      case _ =>
        Future.successful(0)
    }

  }

  def removeNote(po: NoteDeleted): Future[Int] = {
    logger.trace("Inserting a note")
    db.run(notes.filter(note => note.id === po.noteId).delete)
  }

  def getNotes: Future[Seq[NoteSql]] = {
    logger.trace("Get a notes from repo")

    db.run(notes.result)
  }

  def getNote(noteId: NoteId): Future[Option[NoteSql]] = {
    logger.trace("Get a note")

    db.run(notes.filter(x => x.id === noteId).result).map(_.headOption)
  }

}
