package com.ingenuiq.note.integration.base

import akka.actor.ActorRef
import akka.http.scaladsl.server.{ Directives, Route }
import akka.http.scaladsl.testkit.{ RouteTest, RouteTestTimeout, ScalatestRouteTest }
import com.ingenuiq.note.command.CommandSupervisorActor
import com.ingenuiq.note.common.{ PredefinedTimeout, UserId }
import com.ingenuiq.note.http.PredefinedRoutePaths
import com.ingenuiq.note.http.command.CommandRoutes
import com.ingenuiq.note.http.query.QueryRoutes
import com.ingenuiq.note.query.QuerySupervisorActor
import com.ingenuiq.note.query.dao.TableDefinitionCreator
import com.ingenuiq.note.serialization.PlayJsonSupport
import com.ingenuiq.note.settings.Settings
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.DurationInt

abstract class IntegrationBase
    extends AnyWordSpec
    with Matchers
    with Eventually
    with BeforeAndAfterAll
    with PredefinedTimeout
    with Directives
    with RouteTest
    with ScalatestRouteTest
    with PlayJsonSupport
    with PredefinedRoutePaths {

  implicit def default: RouteTestTimeout = RouteTestTimeout(new DurationInt(10).second)

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(10, Seconds), Span(200, Millis))

  lazy val commandActor: ActorRef = system.actorOf(CommandSupervisorActor(), "commandActor")
  lazy val queryActor:   ActorRef = system.actorOf(QuerySupervisorActor(), "queryActor")

  val settings: Settings = Settings.conf

  lazy val commandRoutes: CommandRoutes = new CommandRoutes(commandActor, settings)
  lazy val queryRoutes:   QueryRoutes   = new QueryRoutes(queryActor, settings)

  val userId: UserId = UserId.generateNew

  def baseTestRoute(implicit userId: UserId = userId): Route =
    Route.seal(commandRoutes.routes ~ queryRoutes.routes)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedCassandra.startCassandra()
    new TableDefinitionCreator().createQuerySchemaWithRetry(1)
  }

}
