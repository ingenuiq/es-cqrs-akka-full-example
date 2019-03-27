package com.ingenuiq.note.query

import akka.actor._
import akka.cluster.singleton.{ ClusterSingletonManager, ClusterSingletonManagerSettings }

trait SingletonBootstrap {

  def startSingleton(system: ActorSystem, props: Props, managerName: String, terminationMessage: Any = PoisonPill): ActorRef =
    system.actorOf(
      ClusterSingletonManager
        .props(singletonProps = props, terminationMessage = terminationMessage, settings = ClusterSingletonManagerSettings(system)),
      managerName
    )
}
