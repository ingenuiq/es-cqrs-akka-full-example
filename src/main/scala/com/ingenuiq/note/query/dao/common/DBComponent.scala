package com.ingenuiq.note.query.dao.common

import com.typesafe.config.ConfigFactory

object DBComponent {

  private val config = ConfigFactory.load()

  val driver = config.getString("rdbms.properties.driver") match {
    case "org.h2.Driver" => slick.jdbc.H2Profile
    case _               => slick.jdbc.PostgresProfile
  }

  import driver.api._

  val db: Database = Database.forConfig("rdbms.properties")

}
