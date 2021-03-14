package com.ingenuiq.note.command.persistence

import java.time._
import java.util.UUID

import com.ingenuiq.common.PersistentEventMetadataAvro
import com.ingenuiq.note.events._
import com.ingenuiq.note.command.note.NoteEvent._
import com.ingenuiq.note.command.note.{ Note, PersistentNoteEvent }
import com.ingenuiq.note.common._
import com.ingenuiq.note.query.common.{ PersistentEvent, PersistentEventMetadata }
import org.apache.avro.specific.SpecificRecordBase

object AvroConverters {

  def from(e: Note): NoteAvro =
    new NoteAvro(id = e.id.value.toString, title = e.title, content = e.content)

  def to(e: NoteAvro): Note = Note(id = NoteId(UUID.fromString(e.id)), title = e.title, content = e.content)

  def from(e: PersistentNoteEvent): SpecificRecordBase = e match {
    case e: NoteCreated => from(e)
    case e: NoteUpdated => from(e)
    case e: NoteDeleted => from(e)
  }

  def to(e: SpecificRecordBase): PersistentEvent = e match {
    case e: NoteCreatedAvro => to(e)
    case e: NoteUpdatedAvro => to(e)
    case e: NoteDeletedAvro => to(e)
  }

  def from(metadata: PersistentEventMetadata): PersistentEventMetadataAvro =
    PersistentEventMetadataAvro(
      correlationId = metadata.correlationId.value,
      eventId       = metadata.eventId.value.toString,
      userId        = metadata.userId.value,
      created       = localDateTimeToLong(metadata.created),
      spanId        = metadata.spanId
    )

  def to(metadata: PersistentEventMetadataAvro): PersistentEventMetadata =
    PersistentEventMetadata(
      correlationId = CorrelationId(metadata.correlationId),
      eventId       = EventId(UUID.fromString(metadata.eventId)),
      userId        = UserId(metadata.userId),
      created       = longToLocalDateTime(metadata.created),
      spanId        = metadata.spanId
    )

  def from(e: NoteCreated): NoteCreatedAvro =
    new NoteCreatedAvro(metadata = from(e.persistentEventMetadata), note = from(e.note))

  def to(e: NoteCreatedAvro): NoteCreated =
    NoteCreated(persistentEventMetadata = to(e.metadata), note = to(e.note))

  def from(e: NoteUpdated): NoteUpdatedAvro =
    new NoteUpdatedAvro(
      metadata = from(e.persistentEventMetadata),
      id       = e.noteId.value.toString,
      title    = e.title.map(UpdatebleAvro.apply),
      content  = e.content.map(UpdatebleAvro.apply)
    )

  def to(e: NoteUpdatedAvro): NoteUpdated =
    NoteUpdated(
      persistentEventMetadata = to(e.metadata),
      noteId                  = NoteId(UUID.fromString(e.id)),
      title                   = e.title.map(_.value),
      content                 = e.content.map(_.value)
    )

  def from(e: NoteDeleted): NoteDeletedAvro =
    new NoteDeletedAvro(metadata = from(e.persistentEventMetadata), noteId = e.noteId.value.toString)

  def to(e: NoteDeletedAvro): NoteDeleted =
    NoteDeleted(persistentEventMetadata = to(e.metadata), noteId = NoteId(UUID.fromString(e.noteId)))

  private def localDateTimeToLong(date: LocalDateTime): Long          = date.toInstant(ZoneOffset.UTC).toEpochMilli
  private def longToLocalDateTime(date: Long):          LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneOffset.UTC)
}
