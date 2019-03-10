package com.ingenuiq.note.query.dao.common

import slick.lifted.CanBeQueryCondition

import scala.language.higherKinds

trait QueryFilterOptions {

  import com.ingenuiq.note.query.dao.common.DBComponent.driver.api._

  implicit class ConditionalQueryFilter[A, B, C[_]](q: Query[A, B, C]) {

    def filterOpt[D, T <: Rep[_]: CanBeQueryCondition](option: Option[D])(f: (A, D) => T): Query[A, B, C] =
      option.map(d => q.filter(a => f(a, d))).getOrElse(q)

    def filterIf(p: Boolean)(f: A => Rep[Boolean]): Query[A, B, C] =
      if (p) q.filter(f) else q

    def filterIfOptional(p: Boolean)(f: A => Rep[Option[Boolean]]): Query[A, B, C] =
      if (p) q.filter(f) else q

  }
}
