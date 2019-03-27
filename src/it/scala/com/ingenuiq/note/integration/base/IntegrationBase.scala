package com.ingenuiq.note.integration.base

import akka.actor.{ ActorRef, ActorSystem }
import akka.cluster.Cluster
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
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }

import scala.concurrent.duration.DurationInt

abstract class IntegrationBase
    extends WordSpec
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

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(10, Seconds), Span(500, Millis))

  lazy val commandActor: ActorRef = system.actorOf(CommandSupervisorActor(), "commandActor")
  lazy val queryActor:   ActorRef = system.actorOf(QuerySupervisorActor(), "queryActor")

  val settings: Settings = Settings.conf

  setupCluster(system)

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

  def setupCluster(system: ActorSystem): Unit = {
    val cluster = Cluster(system)
    cluster.join(cluster.selfAddress)
  }

}
