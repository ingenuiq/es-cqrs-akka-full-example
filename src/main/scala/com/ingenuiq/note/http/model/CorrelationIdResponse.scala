package com.ingenuiq.note.http.model

import com.ingenuiq.note.common.CorrelationId
import com.ingenuiq.note.utils
import play.api.libs.json.{ Json, Writes }

case class CorrelationIdResponse(correlationId: CorrelationId = CorrelationId(utils.currentTraceId))

object CorrelationIdResponse {
  implicit val writes: Writes[CorrelationIdResponse] = Json.writes[CorrelationIdResponse]
}
