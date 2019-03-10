package com.ingenuiq.note.query.common

import java.time.LocalDateTime

import com.ingenuiq.note.common.{ CorrelationId, EventId, UserId }
import com.ingenuiq.note.utils

trait Command extends WithMetadata

trait Event

trait Query extends WithMetadata

trait PersistentEvent extends Event {
  def persistentEventMetadata: PersistentEventMetadata
}

trait Traceable {
  def correlationId: CorrelationId
}

trait WithMetadata {
  def userId: UserId
}

case class PersistentEventMetadata(userId:        UserId,
                                   eventId:       EventId = EventId.generateNew,
                                   created:       LocalDateTime = utils.now(),
                                   correlationId: CorrelationId = CorrelationId(utils.currentTraceId),
                                   spanId:        String = utils.currentSpanId)
    extends WithMetadata
