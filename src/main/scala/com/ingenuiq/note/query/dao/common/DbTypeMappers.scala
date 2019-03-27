package com.ingenuiq.note.query.dao.common

import java.time._
import java.util.UUID

import akka.persistence.query.{ NoOffset, Offset, TimeBasedUUID }
import com.datastax.driver.core.utils.UUIDs
import com.ingenuiq.note.common.{ CorrelationId, EventId, NoteId, UserId }
import slick.jdbc.JdbcType

trait DbTypeMappers {
  import DBComponent.driver.api._

  private def localDateTimeToLong(date: LocalDateTime): Long          = date.toInstant(ZoneOffset.UTC).toEpochMilli
  private def localDateToLong(date:     LocalDate):     Long          = date.atStartOfDay.toInstant(ZoneOffset.UTC).toEpochMilli
  private def longToLocalDateTime(date: Long):          LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneOffset.UTC)
  private def longToLocalDate(date:     Long):          LocalDate     = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneOffset.UTC).toLocalDate

  implicit val dateTimeConverter: JdbcType[LocalDateTime] =
    MappedColumnType
      .base[LocalDateTime, Long](e => localDateTimeToLong(e), e => longToLocalDateTime(e))

  implicit val dateConverter: JdbcType[LocalDate] =
    MappedColumnType
      .base[LocalDate, Long](e => localDateToLong(e), e => longToLocalDate(e))

  implicit val noteIdConverter: JdbcType[NoteId] =
    MappedColumnType.base[NoteId, UUID](_.value, e => NoteId(e))

  implicit val userIdConverter: JdbcType[UserId] =
    MappedColumnType.base[UserId, String](_.value, e => UserId(e))

  implicit val eventIdConverter: JdbcType[EventId] =
    MappedColumnType.base[EventId, UUID](_.value, e => EventId(e))

  implicit val correlationIdConverter: JdbcType[CorrelationId] =
    MappedColumnType.base[CorrelationId, String](_.value, e => CorrelationId(e))

  private[this] def offsetToInternalOffset(offset: Offset): UUID =
    offset match {
      case TimeBasedUUID(uuid) => uuid
      case NoOffset            => UUIDs.startOf(Instant.MIN.toEpochMilli)
      case unsupported =>
        throw new IllegalArgumentException("Cassandra does not support " + unsupported.getClass.getName + " offsets")
    }

  implicit val offsetMapper: JdbcType[Offset] =
    MappedColumnType.base[Offset, UUID](e => offsetToInternalOffset(e), s => TimeBasedUUID(s))

}
