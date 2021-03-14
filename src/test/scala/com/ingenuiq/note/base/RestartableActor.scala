package com.ingenuiq.note.base

import akka.persistence.PersistentActor
import com.ingenuiq.note.base.RestartableActor.{ RestartActor, RestartActorException }

trait RestartableActor extends PersistentActor {

  abstract override def receiveCommand: Receive = super.receiveCommand orElse {
      case RestartActor => throw new RestartActorException("Test - Restarting with exception")
    }
}

object RestartableActor {
  case object RestartActor

  private class RestartActorException(message: String) extends Exception(message)
}
