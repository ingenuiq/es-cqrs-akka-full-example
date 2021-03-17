package com.ingenuiq.note.query.dao

import com.ingenuiq.note.query.dao.schema._
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.meta.MTable

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

//TODO move to tests or even better remove completely when schema gets stable. Flyway
class TableDefinitionCreator(implicit ec: ExecutionContext)
    extends PersistenceOffsetTableDefinition
    with NoteTableDefinition
    with NoteEventTableDefinition
    with LazyLogging {

  import com.ingenuiq.note.query.dao.common.DBComponent._
  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  protected val tables = List(notes, noteEvents, persistenceOffsets)

  private val fibonacci: LazyList[Int] = 0 #:: 1 #:: (fibonacci zip fibonacci.tail).map(t => t._1 + t._2)

  def rebuildSchema(rebuildReadside: Boolean): Unit = {
    if (rebuildReadside) {
      dropQuerySchemaWithRetry()
    }
    createQuerySchemaWithRetry()
  }

  def createQuerySchemaWithRetry(retries: Int = 10): Unit = {
    logger.info(s"Generating SQL schema. Max retries is set to $retries")
    createSchema(0, retries)
  }

  @tailrec private def createSchema(last: Int, retries: Int): Unit =
    if (last < retries) {
      val futureCreation: Future[List[Unit]] = db
        .run(MTable.getTables)
        .flatMap { v =>
          val names            = v.map(mt => mt.name.name)
          val createIfNotExist = tables.filter(table => !names.contains(table.baseTableRow.tableName)).map(_.schema.create)
          db.run(DBIO.sequence(createIfNotExist))
        }

      Try(Await.result(futureCreation, 5.seconds)) match {
        case Success(_) => logger.info("Schema successfully generated")
        case Failure(e) =>
          logger.error(s"Failed to generate query side schema", e)
          logger.info(s"Attempt n ${last + 1} for creation of tables has failed. Max retries: $retries")
          Thread.sleep(nextSleep(last))
          createSchema(last + 1, retries)
      }
    }
    else {
      logger.error(s"Failed to create the schema after $retries retries. Shutting down system..")
      System.exit(-1)
    }

  protected def nextSleep(index: Int): Int = {
    require(index <= 100, "Too high index for the fibonacci sequence")
    require(index >= 0, "Invalid negative index for the fibonacci sequence")
    fibonacci(index) * 1500
  }

  def dropQuerySchemaWithRetry(retries: Int = 10): Unit = {
    logger.info(s"Dropping SQL schema. Max retries is set to $retries")
    dropSchema(0, retries)
  }

  @tailrec private def dropSchema(last: Int, retries: Int): Unit =
    if (last < retries) {
      val futureGetTables = db.run(driver.defaultTables)

      //fix drop
      Try(Await.result(futureGetTables, 5.seconds)) match {
        case Success(tableList) =>
          val existingTableNames = tables.map(_.baseTableRow.tableName)
          val tableNames = tableList
            .withFilter(table => table.name.schema.contains("public") && existingTableNames.contains(table.name.name))
            .map(_.name.name)
          logger.info(s"Found ${tableNames.size} tables to drop, tables: ${tableNames.mkString("[", ", ", "]")}")
          val futureDropTables: Future[Seq[Int]] = Future.sequence(tableNames.map(table => db.run(sqlu"""DROP TABLE "#$table"""")))

          Try(Await.result(futureDropTables, 5.seconds)) match {
            case Success(_) =>
              logger.info("All tables successfully dropped")
            case Failure(e) =>
              logger.error(s"Failed to drop all tables on query side", e)
              logger.info(s"Attempt n ${last + 1} for dropping query schema has failed. Max retries: $retries")
              Thread.sleep(nextSleep(last))
              dropSchema(last + 1, retries)
          }
        case Failure(e) =>
          logger.error(s"Failed to drop query side schema", e)
          logger.info(s"Attempt n ${last + 1} for dropping query schema has failed. Max retries: $retries")
          Thread.sleep(nextSleep(last))
          dropSchema(last + 1, retries)
      }
    }
    else {
      logger.error(s"Failed to drop the schema after $retries retries. Shutting down system..")
      System.exit(-1)
    }
}
