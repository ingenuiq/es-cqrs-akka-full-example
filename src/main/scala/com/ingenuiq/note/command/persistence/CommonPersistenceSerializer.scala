package com.ingenuiq.note.command.persistence

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import akka.serialization.SerializerWithStringManifest
import com.ingenuiq.note.command.persistence.StatementSchemaMap.HashFingerprint
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.Schema
import org.apache.avro.io.{ DecoderFactory, EncoderFactory }
import org.apache.avro.specific.{ SpecificDatumReader, SpecificDatumWriter, SpecificRecordBase }

class CommonPersistenceSerializer extends SerializerWithStringManifest with LazyLogging {

  private val DISCRIMINATOR: String = "-|-"

  val (currentSchemaPairs: List[SchemaInfo], allSchemaPairs: List[SchemaInfo]) =
    StatementSchemaMap()

  def identifier = 885242445

  override def manifest(obj: AnyRef): HashFingerprint = {
    val avroEventName = obj.getClass.getSimpleName
    currentSchemaPairs.find(_.schema.getName == avroEventName) match {
      case Some(schemaPair) => avroEventName + DISCRIMINATOR + schemaPair.manifestHash
      case None =>
        logger.error(s"Could not find a schema pair for $avroEventName")
        throw new NoSuchElementException(s"Could not find a schema pair for $avroEventName")

    }
  }

  override def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case x: SpecificRecordBase =>
      val writer  = new SpecificDatumWriter[SpecificRecordBase](x.getSchema)
      val out     = new ByteArrayOutputStream()
      val encoder = EncoderFactory.get().binaryEncoder(out, null)
      writer.write(x, encoder)
      encoder.flush()
      out.toByteArray
  }

  override def fromBinary(bytes: Array[Byte], manifest: HashFingerprint): AnyRef = {

    val decodedEvent = for {
      avroEventName <- manifest.split(DISCRIMINATOR).headOption
      manifestHash  <- manifest.split(DISCRIMINATOR).lastOption
    } yield {
      val writerSchema: SchemaInfo =
        allSchemaPairs
          .find(_.manifestHash == manifestHash) match {
          case Some(schemaPair) => schemaPair
          case None =>
            currentSchemaPairs.find(_.schema.getName == avroEventName) match {
              case Some(schemaPair) =>
                logger.trace(
                  s"Found older version of writer for schema manifestHash: ${schemaPair.manifestHash}, className: $avroEventName"
                )
                schemaPair
              case None =>
                throw new NoSuchElementException(s"No history schema found for manifest hash $manifest.")
            }
        }

      val readerSchema: Schema = currentSchemaPairs.find(_.schema.getName == avroEventName) match {
        case Some(schemaPair) => schemaPair.schema
        case None =>
          logger.error(s"Cannot find a reader schema className: $avroEventName")
          throw new NoSuchElementException(s"No active schema pair found for event $avroEventName.")
      }

      val reader       = new SpecificDatumReader[SpecificRecordBase](writerSchema.schema, readerSchema)
      val in           = new ByteArrayInputStream(bytes)
      val decoder      = DecoderFactory.get().binaryDecoder(in, null)
      val decodedEvent = reader.read(null, decoder)

      if (writerSchema.manifestHash != manifestHash) {
        logger.trace(
          s"Event ${decodedEvent.getClass} was decoded with older reader schema. ${writerSchema.manifestHash} instead of $manifestHash"
        )
      }
      decodedEvent
    }

    decodedEvent.getOrElse {
      logger.error(s"Manifest $manifest couldn't be split into event name and manifest hash with discriminator $DISCRIMINATOR")
      throw new IllegalArgumentException(
        s"Manifest $manifest couldn't be split into event name and manifest hash with discriminator $DISCRIMINATOR"
      )
    }
  }

}
