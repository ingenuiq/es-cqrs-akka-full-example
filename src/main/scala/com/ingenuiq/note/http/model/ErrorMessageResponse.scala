package com.ingenuiq.note.http.model

import com.ingenuiq.note.utils
import play.api.libs.json.{ Json, Writes }

case class ErrorMessageResponse(errorMessage: String = "Error on our side, working on it", correlationId: String = utils.currentTraceId)

object ErrorMessageResponse {
  implicit val writes: Writes[ErrorMessageResponse] = Json.writes[ErrorMessageResponse]
}
