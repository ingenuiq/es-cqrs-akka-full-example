package com.ingenuiq.note.command.note

import com.ingenuiq.note.common.NoteId

case class Note(id: NoteId, title: Option[String], content: Option[String])
