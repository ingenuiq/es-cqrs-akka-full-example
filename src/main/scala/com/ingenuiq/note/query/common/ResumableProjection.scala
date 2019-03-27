package com.ingenuiq.note.query.common

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.persistence.query.Offset
import com.ingenuiq.note.query.dao.model.PersistenceOffset
import com.ingenuiq.note.query.dao.repos.PersistenceOffsetRepo

import scala.concurrent.Future

abstract class ResumableProjection(identifier: String) {
  def storeLatestOffset(offset: Offset): Future[Boolean]
  def fetchLatestOffset: Future[Offset]
}

object ResumableProjection {

  def apply(identifier: String, system: ActorSystem) =
    new DBProjectionStorageExt(system)
}

class DBProjectionStorageExt(system: ActorSystem) extends Extension {

  import system.dispatcher

  val persistenceSequenceNrRepo: PersistenceOffsetRepo = PersistenceOffsetRepo()

  def updateOffset(identifier: String, offset: Offset): Future[Boolean] =
    persistenceSequenceNrRepo.upsert(PersistenceOffset(identifier, offset)).map(_ > 0)

  def fetchLatestOffset(identifier: String): Future[Offset] =
    persistenceSequenceNrRepo.getByPersistenceId(identifier).map(_.offset)
}

object DBProjectionStorage extends ExtensionId[DBProjectionStorageExt] with ExtensionIdProvider {
  override def lookup: DBProjectionStorage.type = DBProjectionStorage
  override def createExtension(system: ExtendedActorSystem) =
    new DBProjectionStorageExt(system)
}
