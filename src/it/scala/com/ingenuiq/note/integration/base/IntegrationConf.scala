package com.ingenuiq.note.integration.base

import com.typesafe.config.{ Config, ConfigFactory, ConfigValueFactory }

import scala.collection.JavaConverters._

object IntegrationConf {

  def config(className: Class[_]): Config = {
    val clusterName = className.getName
      .replace('.', '-')
      .replace('_', '-')
      .filter(_ != '$')

    ConfigFactory
      .load()
  }

}
