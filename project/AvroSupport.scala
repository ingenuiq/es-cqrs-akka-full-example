import java.util

import org.apache.avro.tool.IdlToSchemataTool
import sbt.Keys._
import sbt._
import sbtavrohugger.SbtAvrohugger.autoImport.{ avroScalaGenerateSpecific, avroScalaSpecificCustomTypes, avroSpecificSourceDirectories }

object AvroSupport {

  val generateInternalAvsc = Def.task {
    val s: TaskStreams = streams.value
    s.log.info("Generating .avsc schema files for Avro, used as journal model for Akka persistence...")

    val basePath = new java.io.File("").getAbsolutePath
    val avdlPath = new java.io.File(basePath + "/src/main/resources/avro")
    val avscPath = new java.io.File(basePath + "/src/main/resources/avro/avsc")

    avdlPath.listFiles().toList.withFilter(_.getName.endsWith(".avdl")).foreach { file =>
      val sourceFilePath = avdlPath.getPath + "/" + file.getName
      val arglist        = util.Arrays.asList(sourceFilePath, avscPath.getPath)
      try {
        s.log.info(s"Generating .avsc files in ${avscPath.getPath} from $sourceFilePath")
        new IdlToSchemataTool().run(null, null, null, arglist)
      } catch {
        case e: Exception =>
          s.log.error("Exception during avsc generation " + e.getLocalizedMessage)
          throw e
      }
    }

    val gitAddCmd = sys.env.getOrElse("GIT_ADD_CMD", "git add *.avsc")
    s.log.info(s"Adding .avsc files to git")
    import scala.sys.process._
    s"$gitAddCmd $avscPath".!!
  }

  val avroSettings = Seq(
    Compile / compile := (Compile / compile).dependsOn(generateInternalAvsc).value,
    sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue,
    avroSpecificSourceDirectories in Compile += (sourceDirectory in Compile).value / "resources" / "avro" / "avsc",
    avroSpecificSourceDirectories in Compile := (avroSpecificSourceDirectories in Compile).value,
    avroScalaSpecificCustomTypes in Compile := {
      avrohugger.format.SpecificRecord.defaultTypes.copy(protocol = avrohugger.types.ScalaADT, array = avrohugger.types.ScalaList)
    }
  )
}
