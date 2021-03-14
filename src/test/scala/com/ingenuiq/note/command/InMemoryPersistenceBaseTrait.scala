package com.ingenuiq.note.command

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.typesafe.config.{ ConfigFactory, ConfigValueFactory }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.{ FiniteDuration, SECONDS }

abstract class InMemoryPersistenceBaseTrait
    extends TestKit(
      ActorSystem(
        "e-portal-test",
        ConfigFactory
          .load()
          .withValue("akka.persistence.journal.plugin", ConfigValueFactory.fromAnyRef("inmemory-journal"))
          .withValue("akka.persistence.snapshot-store.plugin", ConfigValueFactory.fromAnyRef("inmemory-snapshot-store"))
      )
    )
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ImplicitSender {

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  val waitToMessageTimeout = FiniteDuration(1, SECONDS)

}
