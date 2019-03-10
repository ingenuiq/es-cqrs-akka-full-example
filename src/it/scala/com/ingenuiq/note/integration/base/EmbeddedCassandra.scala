package com.ingenuiq.note.integration.base

import java.io.File

import akka.persistence.cassandra.testkit.CassandraLauncher

object EmbeddedCassandra {
  private val directory      = new File("cassandra")
  private val configResource = "test-embedded-cassandra.yaml"
  private val clean          = true
  private val port           = 9042

  def startCassandra(): Unit = CassandraLauncher.start(directory, configResource, clean, port)

  def stopCassandra(): Unit = CassandraLauncher.stop()
}
