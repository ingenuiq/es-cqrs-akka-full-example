package com.ingenuiq.note.query.dao

import com.ingenuiq.note.base.BaseRepoSpec
import com.ingenuiq.note.command.note.NoteEvent._
import com.ingenuiq.note.common.{ NoteId, UserId }
import com.ingenuiq.note.query.common.PersistentEventMetadata
import com.ingenuiq.note.query.dao.repos.NoteRepo
import org.scalatest.concurrent.Eventually

import scala.concurrent.ExecutionContext.Implicits.global

class NoteRepoSpec extends BaseRepoSpec with Eventually {

  val noteRepo: NoteRepo = new NoteRepo

  val userId: UserId = UserId.generateNew

  "insert" should {
    "create new note" in {

      val noteCreated = NoteCreated(PersistentEventMetadata(userId), generateRandomNote())

      noteRepo.insertNote(noteCreated).futureValue

      val res = noteRepo.getNotes.futureValue
      res.map(_.id) should contain(noteCreated.note.id)
    }
  }

  "update" should {
    "update existing note title" in {
      val noteCreated = NoteCreated(PersistentEventMetadata(userId), generateRandomNote())

      noteRepo.insertNote(noteCreated).futureValue

      val noteUpdated = NoteUpdated(PersistentEventMetadata(userId), noteCreated.note.id, Some(Some("title")), None)

      noteRepo.updateNote(noteUpdated).futureValue

      val res = noteRepo.getNotes.futureValue.filter(_.id == noteCreated.note.id)
      res.head.title shouldBe Some("title")
      res.head.content shouldBe noteCreated.note.content
    }

    "update existing note content" in {
      val noteCreated = NoteCreated(PersistentEventMetadata(userId), generateRandomNote())

      noteRepo.insertNote(noteCreated).futureValue

      val noteUpdated = NoteUpdated(PersistentEventMetadata(userId), noteCreated.note.id, None, Some(Some("content")))

      noteRepo.updateNote(noteUpdated).futureValue

      val res = noteRepo.getNotes.futureValue.filter(_.id == noteCreated.note.id)
      res.head.title shouldBe noteCreated.note.title
      res.head.content shouldBe Some("content")
    }

    "update existing note title and content" in {
      val noteCreated = NoteCreated(PersistentEventMetadata(userId), generateRandomNote())

      noteRepo.insertNote(noteCreated).futureValue

      val noteUpdated = NoteUpdated(PersistentEventMetadata(userId), noteCreated.note.id, Some(Some("title")), Some(Some("content")))

      noteRepo.updateNote(noteUpdated).futureValue

      val res = noteRepo.getNotes.futureValue.filter(_.id == noteCreated.note.id)
      res.head.title shouldBe Some("title")
      res.head.content shouldBe Some("content")
    }

    "not update title if note doesn't exist" in {
      val noteUpdated = NoteUpdated(PersistentEventMetadata(userId), NoteId.generateNew, Some(Some("title")), None)

      noteRepo.updateNote(noteUpdated).futureValue

      val res = noteRepo.getNotes.futureValue
      res.map(_.id) should not contain noteUpdated.noteId
    }

  }

  "delete" should {
    "delete an existing note" in {
      val noteCreated = NoteCreated(PersistentEventMetadata(userId), generateRandomNote())
      noteRepo.insertNote(noteCreated).futureValue shouldBe 1

      val res1 = noteRepo.getNotes.futureValue
      res1.map(_.id) should contain(noteCreated.note.id)

      val noteDeleted = NoteDeleted(PersistentEventMetadata(userId), noteCreated.note.id)
      noteRepo.removeNote(noteDeleted).futureValue shouldBe 1

      val res2 = noteRepo.getNotes.futureValue
      res2.map(_.id) shouldNot contain(noteCreated.note.id)
    }

    "delete nothing if note do not exists" in {
      val noteDeleted = NoteDeleted(PersistentEventMetadata(userId), NoteId.generateNew)
      noteRepo.removeNote(noteDeleted).futureValue shouldBe 0
    }
  }

  "getNotes" should {
    "get all notes" in {
      val noteCreated1 = NoteCreated(PersistentEventMetadata(userId), generateRandomNote())
      val noteCreated2 = NoteCreated(PersistentEventMetadata(userId), generateRandomNote())

      noteRepo.insertNote(noteCreated1).futureValue
      noteRepo.insertNote(noteCreated2).futureValue

      val res = noteRepo.getNotes.futureValue

      res should have size 2

    }
  }

}
