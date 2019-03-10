package com.ingenuiq.note.utils

import com.ingenuiq.note.command.persistence.{ SchemaInfo, StatementSchemaMap }
import org.apache.avro.SchemaCompatibility
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType
import org.scalatest.{ FreeSpec, Matchers }

class JournalSchemaEvolutionTest extends FreeSpec with Matchers {

  val (currentSchemaPairs: List[SchemaInfo], allSchemaPairs: List[SchemaInfo]) = StatementSchemaMap()

  "Schema compatibility" - {
    val groupedSchemasByName: Map[String, List[SchemaInfo]] = allSchemaPairs.groupBy(_.schema.getName)
    val schemasWithMultipleVersions = currentSchemaPairs.filter(x => groupedSchemasByName(x.schema.getName).size > 1)

    "be forward compatibility" in {

      schemasWithMultipleVersions.foreach { currentSchemaInfo =>
        val allSchemasForSpecificEventName = groupedSchemasByName(currentSchemaInfo.schema.getName)

        allSchemasForSpecificEventName.foreach { oldSchema =>
          withClue(
            s"Event name: ${currentSchemaInfo.schema.getName}, incompatible manifestHash: ${oldSchema.manifestHash}, reader filename: ${oldSchema.filePath}, writer filename: ${currentSchemaInfo.filePath}"
          ) {
            SchemaCompatibility
              .checkReaderWriterCompatibility(oldSchema.schema, currentSchemaInfo.schema)
              .getType shouldBe SchemaCompatibilityType.COMPATIBLE
          }
        }
      }
    }

    "be backward compatibility" in {
      schemasWithMultipleVersions.foreach { currentSchemaInfo =>
        val allSchemasForSpecificEventName = groupedSchemasByName(currentSchemaInfo.schema.getName)

        allSchemasForSpecificEventName.foreach { newSchema =>
          withClue(
            s"Event name: ${currentSchemaInfo.schema.getName}, incompatible manifestHash: ${newSchema.manifestHash}, reader filename: ${currentSchemaInfo.filePath}, writer filename: ${newSchema.filePath}"
          ) {
            SchemaCompatibility
              .checkReaderWriterCompatibility(currentSchemaInfo.schema, newSchema.schema)
              .getType shouldBe SchemaCompatibilityType.COMPATIBLE
          }
        }
      }
    }
  }

  "Schemas should not be duplicated" in {
    allSchemaPairs.groupBy(_.manifestHash).exists(_._2.size > 1) shouldBe false
  }
}
