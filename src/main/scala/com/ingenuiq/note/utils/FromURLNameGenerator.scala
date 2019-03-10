package com.ingenuiq.note.utils

import akka.http.scaladsl.model.HttpRequest
import kamon.akka.http.AkkaHttp.OperationNameGenerator

import scala.util.matching.Regex

class FromURLNameGenerator extends OperationNameGenerator {

  private val numericPattern         = "\\/\\d+"
  private val uuidPattern            = "\\/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
  private val stringAndNumberPattern = "\\/[A-Z]+-[0-9]+"

  private val patterns =
    Seq(uuidPattern, numericPattern, stringAndNumberPattern)

  private val matchingRegex: Regex = s"${patterns.mkString("|")}".r

  override def serverOperationName(request: HttpRequest): String = {
    val genericUri = matchingRegex.replaceAllIn(request.getUri().getPathString, "/:id")
    s"${request.method.value.toLowerCase}_$genericUri"
  }

  override def clientOperationName(request: HttpRequest): String =
    s"${request.method.value.toLowerCase}_${request.getUri().getHost.address().toLowerCase}"
}
