package com.ingenuiq.note.base

import akka.actor.Actor.Receive

object GetInternalStateActor {
  case object GetInternalState
  case object GetPartialFunctions
  case class ActorPartialFunctions(command: Receive, recover: Receive)
}
