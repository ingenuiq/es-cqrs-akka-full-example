package com.ingenuiq.note.common

import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

trait PredefinedTimeout {

  implicit val timeout: Timeout = Timeout(10 seconds)
}
