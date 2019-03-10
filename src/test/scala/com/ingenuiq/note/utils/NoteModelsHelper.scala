package com.ingenuiq.note.utils

import com.ingenuiq.note.command.note.Note
import com.ingenuiq.note.common.NoteId
import com.ingenuiq.note.http.command.CommandRequest.NotePayload
import com.ingenuiq.note.http.query.QueryResponse.NoteResponse

import scala.util.Random

trait NoteModelsHelper {

  private def randomString: String = s"Note-${Random.alphanumeric.take(10).mkString}"

  def generateRandomNotePayload(title: Option[String] = Option(randomString), content: Option[String] = Option(randomString)): NotePayload =
    NotePayload(title = title, content = content)

  def generateRandomNote(id:      NoteId         = NoteId.generateNew,
                         title:   Option[String] = Option(randomString),
                         content: Option[String] = Option(randomString)): Note =
    Note(id = id, title = title, content = content)

}
