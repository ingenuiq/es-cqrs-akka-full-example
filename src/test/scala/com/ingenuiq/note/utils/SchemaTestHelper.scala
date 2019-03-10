package com.ingenuiq.note.utils

import com.ingenuiq.note.query.dao.TableDefinitionCreator

import scala.concurrent.ExecutionContext.Implicits.global

object SchemaTestHelper extends TableDefinitionCreator {

  import com.ingenuiq.note.query.dao.common.DBComponent._
  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  def createQuerySchema(): Unit = createQuerySchemaWithRetry(1)

  def deleteQueryContent(): Unit = tables.foreach(x => db.run(x.delete))

}
