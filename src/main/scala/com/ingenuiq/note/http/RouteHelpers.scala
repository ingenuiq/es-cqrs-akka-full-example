package com.ingenuiq.note.http

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher.{ Matched, Matching, Unmatched }
import akka.http.scaladsl.server._
import com.ingenuiq.note.http.model.ErrorMessageResponse
import com.ingenuiq.note.serialization.PlayJsonSupport
import com.typesafe.scalalogging.LazyLogging

import scala.util.{ Failure, Success, Try }

trait RouteHelpers extends PlayJsonSupport with LazyLogging with PredefinedRoutePaths {

  private type Match0 = PathMatcher0
  private type Match1 = PathMatcher1[Try[UUID]]
  private type Match2 = PathMatcher[(Try[UUID], Try[UUID])]
  private type Match3 = PathMatcher[(Try[UUID], Try[UUID], Try[UUID])]

  object ID extends PathMatcher1[Try[UUID]] {

    def apply(path: Path): Matching[Tuple1[Try[UUID]]] = path match {
      case Path.Segment(segment, tail) => Matched(tail, Tuple1(parse(segment)))
      case _                           => Unmatched
    }
  }

  def end[L](pm: PathMatcher[L]): PathMatcher[L] = pm ~ Slash.? ~ PathEnd

  def pathTail(pm: Match0): Directive0 = pathPrefix(end(pm))

  def pathTail[T](pm: Match1)(route: T => Route)(implicit t: UUID => T): Route =
    pathPrefix(end(pm)) { id: Try[UUID] =>
      extractTry(for (u <- id) yield route(t(u)))
    }

  def pathTail[S, T](pm: Match2)(route: (S, T) => Route)(implicit s: UUID => S, t: UUID => T): Route =
    pathPrefix(end(pm)) { (id1: Try[UUID], id2: Try[UUID]) =>
      extractTry(for { u1 <- id1; u2 <- id2 } yield route(s(u1), t(u2)))
    }

  def pathTail[S, T, U](pm: Match3)(route: (S, T, U) => Route)(implicit s: UUID => S, t: UUID => T, u: UUID => U): Route =
    pathPrefix(end(pm)) { (id1: Try[UUID], id2: Try[UUID], id3: Try[UUID]) =>
      extractTry(for { u1 <- id1; u2 <- id2; u3 <- id3 } yield route(s(u1), t(u2), u(u3)))
    }

  private def extractTry(t: Try[Route]): Route = t match {
    case Success(x) => x
    case Failure(e) =>
      logger.error("extract try error", e)
      complete(BadRequest -> ErrorMessageResponse(errorMessage = e.getLocalizedMessage))
  }

  private def parse(id: String): Try[UUID] = Try(UUID.fromString(id))
}

trait PredefinedRoutePaths {
  val BasePath:  String = "api"
  val QueryPath: String = "query"
  val NotePath:  String = "note"
  val EventPath: String = "event"
}

object PredefinedRoutePaths extends PredefinedRoutePaths
