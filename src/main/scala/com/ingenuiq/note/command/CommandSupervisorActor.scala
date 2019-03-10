package com.ingenuiq.note.command

import akka.actor.{ Actor, ActorRef, Props }
import com.ingenuiq.note.command.note._
import com.ingenuiq.note.common.PredefinedTimeout
import com.ingenuiq.note.utils.BackoffActorHelper

object CommandSupervisorActor {
  def apply() = Props(classOf[CommandSupervisorActor])
}

class CommandSupervisorActor extends Actor with PredefinedTimeout with BackoffActorHelper {

  val noteAggregateActor: ActorRef = context.system.actorOf(backoffActor("noteAggregateActor", NoteAggregateActor()))

  override def receive: Receive = {
    case command: NoteCommand => noteAggregateActor.forward(command)
  }

}
