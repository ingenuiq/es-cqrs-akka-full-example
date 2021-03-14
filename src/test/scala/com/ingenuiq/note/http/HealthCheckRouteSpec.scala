package com.ingenuiq.note.http

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ RouteTest, ScalatestRouteTest }
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HealthCheckRouteSpec
    extends AnyWordSpec
    with HealthCheckRoute
    with Matchers
    with Eventually
    with Directives
    with RouteTest
    with ScalatestRouteTest {

  "Health Check " should {
    "run health check is ok" in {
      Get(s"""/health-check""") ~> healthCheckRoute ~> check {
        handled shouldBe true
        responseAs[String] shouldBe "ok"
      }
    }
  }
}
