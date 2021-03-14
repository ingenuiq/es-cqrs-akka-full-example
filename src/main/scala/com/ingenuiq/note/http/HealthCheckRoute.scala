package com.ingenuiq.note.http

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{ complete, path }
import akka.http.scaladsl.server.Route

trait HealthCheckRoute {

  private[http] def healthCheckRoute: Route =
    path("health-check") {
      complete((OK, "ok"))
    }
}
