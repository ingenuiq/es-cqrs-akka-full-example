package com.ingenuiq.note.common

import java.util.UUID

import play.api.libs.json._

case class UserId(value: String) extends AnyVal {
  override def toString: String = value.toString
}

case class CorrelationId(value: String) extends AnyVal {
  override def toString: String = value.toString
}

case class NoteId(value: UUID) extends AnyVal {
  override def toString: String = value.toString
}

case class EventId(value: UUID) extends AnyVal {
  override def toString: String = value.toString
}

object EventId {
  def generateNew = EventId(UUID.randomUUID)

  implicit val eventIdWrites: Writes[EventId] = (t: EventId) => JsString(t.value.toString)

  implicit val eventIdReads: Reads[EventId] = {
    case e: JsString => JsSuccess(EventId(UUID.fromString(e.value)))
    case e => JsError(s"Expecting JsString in EventId reads but got $e")
  }
}

object UserId {
  val system        = UserId("99999999-9999-9999-9999-999999999999")
  val noUserId      = UserId("00000000-0000-0000-0000-000000000000")
  val unknownUserId = UserId("00000000-6666-0000-0000-000000000000")
  def generateNew   = UserId(UUID.randomUUID.toString)

  implicit val userIdWrites: Writes[UserId] = (t: UserId) => JsString(t.value.toString)

  implicit val userIdReads: Reads[UserId] = {
    case e: JsString => JsSuccess(UserId(e.value))
    case e => JsError(s"Expecting JsString in UserId reads but got $e")
  }
}

object CorrelationId {
  val noCorrelationId: CorrelationId = CorrelationId("00000000-0000-0000-0000-000000000000")

  implicit val correlationIdWrites: Writes[CorrelationId] = (t: CorrelationId) => JsString(t.value.toString)

  implicit val correlationIdReads: Reads[CorrelationId] = {
    case e: JsString => JsSuccess(CorrelationId(e.value))
    case e => JsError(s"Expecting JsString in correlationId reads but got $e")
  }
}

object NoteId {
  implicit val fromUUID:     UUID => NoteId = uuid => NoteId(uuid)
  def generateNew:           NoteId = NoteId(UUID.randomUUID)
  implicit val noteIdWrites: Writes[NoteId] = (t: NoteId) => JsString(t.value.toString)
}
