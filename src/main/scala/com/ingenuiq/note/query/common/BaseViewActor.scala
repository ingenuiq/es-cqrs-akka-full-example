package com.ingenuiq.note.query.common

import akka.actor._
import com.ingenuiq.note.common.PredefinedTimeout
import com.ingenuiq.note.query.model.{ FailureResult, FailureType, FullResult, ServiceResult }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

/**
  * Base actor definition for other actors in the note app to extend from
  */
trait BaseViewActor extends Actor with LazyLogging with PredefinedTimeout {
  import akka.pattern.pipe
  import context.dispatcher

  //PF to be used with the .recover combinator to convert an exception on a failed Future into a
  //Failure ServiceResult
  private val toFailure: PartialFunction[Throwable, ServiceResult[Nothing]] = {
    case ex => FailureResult(FailureType.Service, ServiceResult.UnexpectedFailure, Some(ex))
  }

  /**
    * Pipes the response from a request to a service actor back to the sender, first
    * converting to a ServiceResult per the contract of communicating with a note service
    * @param f The Future to map the result from into a ServiceResult
    */
  def pipeResponse[T](f: Future[T], msgSender: ActorRef = sender()): Unit =
    f.map {
        case o: Option[_]     => ServiceResult.fromOption(o)
        case f: FailureResult => f
        case other => FullResult(other)
      }
      .recover(toFailure)
      .pipeTo(msgSender)
}
