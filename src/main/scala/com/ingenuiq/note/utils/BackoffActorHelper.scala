package com.ingenuiq.note.utils

import akka.actor.Props
import akka.pattern.{ BackoffOpts, BackoffSupervisor }

import scala.concurrent.duration._

trait BackoffActorHelper {

  def backoffActor(childName: String, props: Props): Props =
    BackoffSupervisor.props(
      BackoffOpts
        .onStop(
          props,
          childName    = childName,
          minBackoff   = 3.seconds,
          maxBackoff   = 30.seconds,
          randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
        )
        .withDefaultStoppingStrategy // Stop at any Exception thrown
    )

}
