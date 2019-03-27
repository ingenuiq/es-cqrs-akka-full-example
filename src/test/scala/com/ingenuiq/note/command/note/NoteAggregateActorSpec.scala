package com.ingenuiq.note.command.note

import java.util.UUID

import akka.actor.{ ActorRef, Props }
import akka.persistence.PersistentActor
import com.ingenuiq.note.base.GetInternalStateActor.{ ActorPartialFunctions, GetInternalState, GetPartialFunctions }
import com.ingenuiq.note.base.RestartableActor
import com.ingenuiq.note.base.RestartableActor.RestartActor
import com.ingenuiq.note.command.InMemoryPersistenceBaseTrait
import com.ingenuiq.note.command.note.NoteCommand._
import com.ingenuiq.note.command.note.NoteEvent._
import com.ingenuiq.note.common._
import com.ingenuiq.note.utils.{ ClassUtils, NoteModelsHelper }
import org.mockito.Mockito.mock

class NoteAggregateActorSpec extends InMemoryPersistenceBaseTrait with NoteModelsHelper with ClassUtils {

  def productAggregateActorProps(persistenceId: String = UUID.randomUUID().toString) =
    Props(new NoteAggregateActor(persistenceId) with RestartableActor with GetNoteInternalStateActor)

  val userId: UserId = UserId.generateNew

  val randomNote: Note = generateRandomNote()

  "CreateNote" should {
    "create new note" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! CreateNote(userId, randomNote)
      expectMsgClass(classOf[NoteCreated])

      aggregate ! GetInternalState
      expectMsg(Some(randomNote))

      aggregate ! RestartActor

      aggregate ! GetInternalState
      expectMsg(Some(randomNote))
    }

    "get error if not already exists" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! CreateNote(userId, randomNote)
      expectMsgClass(classOf[NoteCreated])

      aggregate ! CreateNote(userId, randomNote)
      expectMsg(NoteAlreadyExists)

      aggregate ! GetInternalState
      expectMsg(Some(randomNote))

      aggregate ! RestartActor

      aggregate ! GetInternalState
      expectMsg(Some(randomNote))
    }
  }

  "UpdateNote" should {
    "get error if note doesn't exit" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! UpdateNote(userId, randomNote)
      expectMsg(NoteNotFound)
    }

    "update note title" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! CreateNote(userId, randomNote)
      expectMsgClass(classOf[NoteCreated])

      aggregate ! UpdateNote(userId, randomNote.copy(title = Some("new title")))
      expectMsgClass(classOf[NoteUpdated])

      aggregate ! GetInternalState
      expectMsg(Some(randomNote.copy(title = Some("new title"))))

      aggregate ! RestartActor

      aggregate ! GetInternalState
      expectMsg(Some(randomNote.copy(title = Some("new title"))))
    }

    "update note content" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! CreateNote(userId, randomNote)
      expectMsgClass(classOf[NoteCreated])

      aggregate ! UpdateNote(userId, randomNote.copy(content = Some("new content")))
      expectMsgClass(classOf[NoteUpdated])

      aggregate ! GetInternalState
      expectMsg(Some(randomNote.copy(content = Some("new content"))))

      aggregate ! RestartActor

      aggregate ! GetInternalState
      expectMsg(Some(randomNote.copy(content = Some("new content"))))
    }

    "update note title and content" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! CreateNote(userId, randomNote)
      expectMsgClass(classOf[NoteCreated])

      aggregate ! UpdateNote(userId, randomNote.copy(title = Some("new title"), content = Some("new content")))
      expectMsgClass(classOf[NoteUpdated])

      aggregate ! GetInternalState
      expectMsg(Some(Note(randomNote.id, Some("new title"), Some("new content"))))

      aggregate ! RestartActor

      aggregate ! GetInternalState
      expectMsg(Some(Note(randomNote.id, Some("new title"), Some("new content"))))
    }

  }

  "DeleteNote" should {
    "get error if note doesn't exit" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! DeleteNote(userId, randomNote.id)
      expectMsg(NoteNotFound)
    }

    "delete existing note" in {
      val aggregate: ActorRef = system.actorOf(productAggregateActorProps())

      aggregate ! CreateNote(userId, randomNote)
      expectMsgClass(classOf[NoteCreated])

      aggregate ! DeleteNote(userId, randomNote.id)
      expectMsgClass(classOf[NoteDeleted])

      aggregate ! GetInternalState
      expectMsg(None)

      aggregate ! RestartActor

      aggregate ! GetInternalState
      expectMsg(None)
    }
  }

  "check handling of all incoming commands and recovery events" should {
    import org.scalatest.prop.TableDrivenPropertyChecks._
    import org.scalatest.prop.TableFor1

    val commands: TableFor1[Class[_]] =
      Table("Command classes", implementationsOf(classOf[NoteCommand], Option("target")).map(toClass): _*)
    val events: TableFor1[Class[_]] =
      Table("Persistent classes", implementationsOf(classOf[PersistentNoteEvent], Option("target")).map(toClass): _*)

    system.actorOf(productAggregateActorProps()) ! GetPartialFunctions
    val pfs: ActorPartialFunctions = expectMsgType[ActorPartialFunctions]

    forAll(commands) { clazz: Class[_] =>
      s"""be able to match $clazz in "receive" function""" in {
        pfs.command.isDefinedAt(mock(clazz)) should be(true)
      }
    }

    forAll(events) { clazz: Class[_] =>
      // Could be useful to enrich the TableFor1 with the other possible events that an Actor can handle.
      //  E.g. SnapshotOffer, RecoveryCompleted etc.etc.
      s"""be able to match $clazz in "receive" function""" in {
        pfs.recover.isDefinedAt(mock(clazz)) should be(true)
      }
    }
  }
}

trait GetNoteInternalStateActor extends PersistentActor {

  var noteState: Option[Note]

  private def receivedInternalStateCommand: Receive = {
    case GetInternalState    => sender() ! noteState
    case GetPartialFunctions => sender() ! ActorPartialFunctions(receiveCommand, receiveRecover)
  }

  abstract override def receiveCommand: Receive =
    receivedInternalStateCommand orElse super.receiveCommand
}
