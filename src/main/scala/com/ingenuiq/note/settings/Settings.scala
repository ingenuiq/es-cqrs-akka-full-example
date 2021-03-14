package com.ingenuiq.note.settings

import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource
import pureconfig.generic.auto._

case class Settings(rebuildReadside:           Boolean,
                    tracingMonitoringSettings: TracingMonitoringSettings,
                    httpListenerSettings:      HttpListenerSettings)

object Settings {

  val conf: Settings = ConfigSource.fromConfig(ConfigFactory.load).loadOrThrow[Settings]
}
