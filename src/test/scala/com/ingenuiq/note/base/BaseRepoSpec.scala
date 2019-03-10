package com.ingenuiq.note.base

import com.ingenuiq.note.settings.Settings
import com.ingenuiq.note.utils.{ NoteModelsHelper, SchemaTestHelper }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Span }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpec }

abstract class BaseRepoSpec
    extends WordSpec
    with NoteModelsHelper
    with ScalaFutures
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(1000, Millis), Span(10, Millis))

  val settings: Settings = Settings.conf

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    SchemaTestHelper.createQuerySchema()
  }

  override protected def afterEach(): Unit =
    SchemaTestHelper.deleteQueryContent()
}
