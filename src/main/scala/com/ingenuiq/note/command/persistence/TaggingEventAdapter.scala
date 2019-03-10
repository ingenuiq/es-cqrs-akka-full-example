package com.ingenuiq.note.command.persistence

import akka.persistence.journal.{ EventAdapter, EventSeq }
import com.ingenuiq.note.command.note.PersistentNoteEvent
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.specific.SpecificRecordBase

class TaggingEventAdapter extends EventAdapter with LazyLogging {

  override def toJournal(event: Any): Any = event match {
    case e: PersistentNoteEvent => AvroConverters.from(e)
    case e => logger.error(s"Received unexpected message to be written in journal, ${e.getClass.getSimpleName}, $e")
  }

  override def fromJournal(event: Any, manifest: String): EventSeq = EventSeq.single {
    event match {
      case e: SpecificRecordBase => AvroConverters.to(e)
      case e =>
        logger.error(s"Received unexpected message from journal, not avro, ${e.getClass.getSimpleName}, $e")
        e
    }
  }

  override def manifest(event: Any): String = ""
}
