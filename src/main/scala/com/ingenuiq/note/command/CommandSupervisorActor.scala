package com.ingenuiq.note.command

import akka.actor.{ Actor, ActorRef, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import com.ingenuiq.note.command.note._
import com.ingenuiq.note.common.PredefinedTimeout
import com.ingenuiq.note.utils.BackoffActorHelper

object CommandSupervisorActor {
  def apply() = Props(classOf[CommandSupervisorActor])
}

class CommandSupervisorActor extends Actor with PredefinedTimeout with BackoffActorHelper {

  val noteAggregateActor: ActorRef = ClusterSharding(context.system).start(
    typeName        = "userAggregateActor",
    entityProps     = NoteAggregateActor(),
    settings        = ClusterShardingSettings(context.system),
    extractEntityId = NoteAggregateActor.extractEntityId,
    extractShardId  = NoteAggregateActor.extractShardId
  )

  override def receive: Receive = {
    case command: NoteCommand => noteAggregateActor.forward(command)
  }

}
