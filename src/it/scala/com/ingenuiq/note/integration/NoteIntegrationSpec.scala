package com.ingenuiq.note.integration

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import com.ingenuiq.note.http.command.CommandResponse._
import com.ingenuiq.note.http.query.QueryResponse._
import com.ingenuiq.note.integration.base.IntegrationBase
import com.ingenuiq.note.integration.utils.{ PlayJsonSupportReaders, PlayJsonSupportWriters }
import com.ingenuiq.note.utils.NoteModelsHelper

class NoteIntegrationSpec extends IntegrationBase with NoteModelsHelper with PlayJsonSupportWriters with PlayJsonSupportReaders {

  "note get all" should {
    "Submit new note and it should appear in the view" in {
      val newNote = generateRandomNotePayload()

      Post(s"/$NotePath", newNote) ~> baseTestRoute ~> check {
        status should be(Created)

        eventually {
          Get(s"/$QueryPath/$NotePath") ~> baseTestRoute ~> check {
            status should be(OK)
            val notes = responseAs[NotesResponse].notes
            notes should have size 1
            notes.head.content shouldBe newNote.content
            notes.head.title shouldBe newNote.title
          }
        }
      }
    }
  }

  "GET /note/{uuid}" should {

    "get existing note" in {
      val newNote = generateRandomNotePayload()

      Post(s"/$NotePath", newNote) ~> baseTestRoute ~> check {
        status should be(Created)
        val noteId = responseAs[NoteCreationResponse].noteId

        eventually {
          Get(s"/$QueryPath/$NotePath/$noteId") ~> baseTestRoute ~> check {
            val result = responseAs[NoteResponse]
            result.content shouldBe newNote.content
            result.title shouldBe newNote.title
            result.id shouldBe noteId
          }
        }
      }
    }

    "get 404 for non-exiting note id" in {
      Get(s"/$QueryPath/$NotePath/${UUID.randomUUID().toString}") ~> baseTestRoute ~> check {
        status should be(NotFound)
      }
    }

    "get 400 for invalid note id" in {
      Get(s"/$QueryPath/$NotePath/blabla") ~> baseTestRoute ~> check {
        status should be(BadRequest)
      }
    }
  }

  "PUT /note/{uuid}" should {

    "update an existing note" in {
      val newNote = generateRandomNotePayload()

      Post(s"/$NotePath", newNote) ~> baseTestRoute ~> check {
        status should be(Created)
        val noteId = responseAs[NoteCreationResponse].noteId

        Put(s"/$NotePath/$noteId", newNote.copy(Some("new title"))) ~> baseTestRoute ~> check {
          status shouldBe OK
          responseAs[NoteUpdateResponse].noteId shouldBe noteId
        }

        eventually {
          Get(s"/$QueryPath/$NotePath/$noteId") ~> baseTestRoute ~> check {
            val result = responseAs[NoteResponse]
            result.content shouldBe newNote.content
            result.title shouldBe Some("new title")
            result.id shouldBe noteId
          }
        }

      }
    }

    "get 400 for malformed note id" in {
      val newNote = generateRandomNotePayload()
      Put(s"/$NotePath/wrongUuid", newNote) ~> baseTestRoute ~> check {
        status shouldBe BadRequest
      }
    }

    "get 404 for non-existing note id" in {
      val newNote = generateRandomNotePayload()
      Put(s"/$NotePath/${UUID.randomUUID().toString}", newNote) ~> baseTestRoute ~> check {
        status shouldBe NotFound
      }
    }
  }

  "DELETE /note/{uuid}" should {

    "delete an existing note" in {
      val newNote = generateRandomNotePayload()

      Post(s"/$NotePath", newNote) ~> baseTestRoute ~> check {
        status should be(Created)
        val noteId = responseAs[NoteCreationResponse].noteId

        Delete(s"/$NotePath/$noteId") ~> baseTestRoute ~> check {
          status shouldBe OK
          responseAs[NoteDeletionResponse].noteId shouldBe noteId
        }
      }
    }

    "get 400 for malformed note id" in {
      Delete(s"/$NotePath/wrongUuid") ~> baseTestRoute ~> check {
        status shouldBe BadRequest
      }
    }

    "get 404 for non-existing note id" in {
      Delete(s"/$NotePath/${UUID.randomUUID().toString}") ~> baseTestRoute ~> check {
        status shouldBe NotFound
      }
    }
  }

  "GET /note/event" should {

    "return created events" in {
      val newNote = generateRandomNotePayload()

      Post(s"/$NotePath", newNote) ~> baseTestRoute ~> check {
        status should be(Created)
        val noteId = responseAs[NoteCreationResponse].noteId

        Put(s"/$NotePath/$noteId", newNote.copy(Some("new title"))) ~> baseTestRoute ~> check {
          status shouldBe OK
          responseAs[NoteUpdateResponse].noteId shouldBe noteId
        }

        eventually {
          Get(s"/$QueryPath/$NotePath/$EventPath") ~> baseTestRoute ~> check {
            val result = responseAs[NoteEventsResponse]
            result.noteEvents.filter(_.noteId == noteId) should have size 2
          }
        }
      }
    }
  }

}
