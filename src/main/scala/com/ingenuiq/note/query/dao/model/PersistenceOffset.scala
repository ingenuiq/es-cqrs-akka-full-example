package com.ingenuiq.note.query.dao.model

import akka.persistence.query.Offset

case class PersistenceOffset(tag: String, offset: Offset)
