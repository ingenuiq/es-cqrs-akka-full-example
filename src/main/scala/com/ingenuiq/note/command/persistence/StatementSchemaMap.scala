package com.ingenuiq.note.command.persistence

import java.io.File
import java.util.jar.JarFile

import com.ingenuiq.note.command.persistence.StatementSchemaMap.HashFingerprint
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.Schema
import org.apache.commons.codec.digest.DigestUtils

import scala.jdk.CollectionConverters._
import scala.io.Source

case class SchemaInfo(manifestHash: HashFingerprint, schema: Schema, filePath: String)

object StatementSchemaMap extends LazyLogging {

  private val CUT_LINE: String = ".avsc"

  type HashFingerprint = String
  type FullPath        = String

  private val activeSchemasPath:         String = "avro/avsc"
  private val historyVersionSchemasPath: String = "avro/avsc-history"

  private lazy val activeSchemaFilenames:         List[String] = getFolderFiles(activeSchemasPath)
  private lazy val historyVersionSchemaFilenames: List[String] = getFolderFiles(historyVersionSchemasPath)

  private def getFolderFiles(path: String): List[String] = {
    val jarFile = new File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    if (jarFile.isFile) readJarFile(jarFile, path) // Run with JAR file
    else readPath(path) // Run with IDE
  }

  private[persistence] def readJarFile(jarFile: File, path: String): List[String] = {
    val jar     = new JarFile(jarFile)
    val entries = jar.entries.asScala.toList //gives ALL entries in jar
    jar.close()
    entries.flatMap { entry =>
      val name = entry.getName
      if (name.startsWith(path + "/") && !name.endsWith("/"))
        Option(name.replace(s"$path/", ""))
      else None
    }.sorted
  }

  private[persistence] def readPath(path: String): List[String] = {
    val basePath = getClass.getResource("/").toURI.getPath
    val files    = new File(getClass.getResource("/" + path).toURI).listFiles.toList
    files.flatMap { entry =>
      if (entry.isFile) List(entry.getName)
      else if (entry.isDirectory)
        getFolderFiles(entry.getAbsolutePath.replace(basePath, "")).map(f => s"${entry.getName}/$f")
      else List.empty
    }.sorted
  }

  private lazy val currentSchemaPairs: List[SchemaInfo] =
    activeSchemaFilenames.collect {
      case filename if filename.endsWith(CUT_LINE) =>
        val inputStream = Source.fromResource(activeSchemasPath + "/" + filename).getLines().mkString(" ")
        val fingerprint: HashFingerprint = getMD5FromUrl(inputStream)
        val schema:      Schema          = getSchemaFromUrl(inputStream)
        SchemaInfo(fingerprint, schema, activeSchemasPath + "/" + filename)
    }

  private lazy val historySchemaPairs: List[SchemaInfo] =
    historyVersionSchemaFilenames.collect {
      case filename if filename.endsWith(CUT_LINE) =>
        val inputStream = Source.fromResource(historyVersionSchemasPath + "/" + filename).getLines().mkString(" ")
        val fingerprint: HashFingerprint = getMD5FromUrl(inputStream)
        val schema:      Schema          = getSchemaFromUrl(inputStream)
        SchemaInfo(fingerprint, schema, historyVersionSchemasPath + "/" + filename)
    }

  final lazy val allSchemaPairs: List[SchemaInfo] = currentSchemaPairs ++ historySchemaPairs

  def getMD5FromUrl(fileStream: String): HashFingerprint = DigestUtils.md5Hex(fileStream)

  def getSchemaFromUrl(fileStream: String): Schema =
    new Schema.Parser().parse(Source.fromString(fileStream).getLines().mkString)

  def apply(): (List[SchemaInfo], List[SchemaInfo]) = (currentSchemaPairs, allSchemaPairs)
}
