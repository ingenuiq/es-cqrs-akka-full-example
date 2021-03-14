package com.ingenuiq.note.serialization

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.server.{ RejectionError, ValidationRejection }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import play.api.libs.json.{ JsError, JsValue, Json, Reads, Writes }
import scala.collection.immutable.Seq

object PlayJsonSupport extends PlayJsonSupport {

  final case class PlayJsonError(error: JsError) extends RuntimeException {

    override def getMessage: String =
      JsError.toJson(error).toString()
  }
}

trait PlayJsonSupport {
  import PlayJsonSupport._

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    mediaTypes.map(ContentTypeRange.apply)

  def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(Marshaller.stringMarshaller)

  implicit def unmarshaller[A: Reads]: FromEntityUnmarshaller[A] = {
    def read(json: JsValue) =
      implicitly[Reads[A]]
        .reads(json)
        .recoverTotal(e => throw RejectionError(ValidationRejection(JsError.toJson(e).toString, Some(PlayJsonError(e)))))
    jsonStringUnmarshaller.map(data => read(Json.parse(data)))
  }

  implicit def marshaller[A](implicit writes: Writes[A], printer: JsValue => String = Json.prettyPrint): ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(printer).compose(writes.writes)
}
