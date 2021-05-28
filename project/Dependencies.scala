import sbt._

object Dependencies {

  private[this] object Versions {
    val akka:                    String = "2.6.14"
    val akkaHttp:                String = "10.2.4"
    val akkaHttpCors:            String = "1.1.1"
    val akkaPersistenceInmemory: String = "2.5.15.2"
//    val akkaPersistencePostgres: String = "0.5.0-M1"
    val akkaPersistenceCassandra: String = "1.0.5"
    val enumeratum:               String = "1.6.1"
    val logbackClassic:           String = "1.2.3"
    val playJson:                 String = "2.9.2"
    val pureConfig:               String = "0.15.0"
    val scalaLogging:             String = "3.9.3"
    val scalaTest:                String = "3.2.9"
    val avro4s:                   String = "4.0.9"
    val slick:                    String = "3.3.3"
    val postgres:                 String = "42.2.20"
    val h2:                       String = "1.4.200"
    val embeddedPostgres:         String = "1.2.6"
    val classutil:                String = "1.5.1"
    val mockitoCore:              String = "3.10.0"
    val commonsCodec:             String = "1.15"
    val slf4j:                    String = "1.7.30"
    val kamon:                    String = "2.1.18"
    val flyway:                   String = "7.9.1"
  }

  val all:       Seq[ModuleID] = ProductionDependencies.dependencies ++ TestDependencies.dependencies
  val allScala2: Seq[ModuleID] = ProductionDependencies.scala2Dependencies ++ TestDependencies.scala2Dependencies

  private[this] object ProductionDependencies {

    val dependencies: Seq[ModuleID] =
      postgres ++ commonCodec ++ flyway ++ loggingJava

    val scala2Dependencies: Seq[ModuleID] = logging ++ akka ++ query ++ playJson ++ kamon ++ avro4s ++ enumeratum ++ pureConfig

    private lazy val akka: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-actor"                 % Versions.akka,
      "com.typesafe.akka" %% "akka-persistence-cassandra" % Versions.akkaPersistenceCassandra,
      "com.typesafe.akka" %% "akka-stream"                % Versions.akka,
      "com.typesafe.akka" %% "akka-cluster"               % Versions.akka,
      "com.typesafe.akka" %% "akka-cluster-sharding"      % Versions.akka,
      "com.typesafe.akka" %% "akka-persistence-query"     % Versions.akka,
      "com.typesafe.akka" %% "akka-http-core"             % Versions.akkaHttp,
      //      "com.swissborg"          %% "akka-persistence-postgres" % Versions.akkaPersistencePostgres,
      "ch.megard"              %% "akka-http-cors" % Versions.akkaHttpCors,
      "org.scala-lang.modules" %% "scala-xml"      % "2.0.0"
    )

    private lazy val avro4s: Seq[ModuleID] = Seq("com.sksamuel.avro4s" %% "avro4s-core" % Versions.avro4s)

    private lazy val loggingJava: Seq[ModuleID] =
      Seq("ch.qos.logback" % "logback-classic" % Versions.logbackClassic, "org.slf4j" % "log4j-over-slf4j" % Versions.slf4j)

    private lazy val logging: Seq[ModuleID] =
      Seq("com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging, "com.typesafe.akka" %% "akka-slf4j" % Versions.akka)

    private lazy val pureConfig: Seq[ModuleID] = Seq("com.github.pureconfig" %% "pureconfig" % Versions.pureConfig)

    private lazy val flyway: Seq[ModuleID] = Seq("org.flywaydb" % "flyway-core" % Versions.flyway)

    private lazy val query: Seq[ModuleID] =
      Seq("com.typesafe.slick" %% "slick" % Versions.slick, "com.typesafe.slick" %% "slick-hikaricp" % Versions.slick)

    private lazy val enumeratum: Seq[ModuleID] = Seq("com.beachape" %% "enumeratum" % Versions.enumeratum)

    private lazy val postgres: Seq[ModuleID] = Seq("org.postgresql" % "postgresql" % Versions.postgres)

    private lazy val playJson: Seq[ModuleID] = Seq("com.typesafe.play" %% "play-json" % Versions.playJson)

    private lazy val kamon: Seq[ModuleID] = Seq("io.kamon" %% "kamon-bundle" % Versions.kamon)

    private lazy val commonCodec: Seq[ModuleID] = Seq("commons-codec" % "commons-codec" % Versions.commonsCodec)
  }

  private[this] object TestDependencies {

    private val TestAndITs = "test;it"

    lazy val dependencies: Seq[ModuleID] =
      (mockito ++ h2).map(_ % TestAndITs)

    lazy val scala2Dependencies: Seq[ModuleID] =
      (akkaTest ++ scalaTest ++ embeddedCassandra ++ slickTest ++ clapper).map(_ % TestAndITs)

    private lazy val akkaTest: Seq[ModuleID] = Seq(
      "com.typesafe.akka"   %% "akka-testkit"              % Versions.akka,
      "com.typesafe.akka"   %% "akka-stream-testkit"       % Versions.akka,
      "com.typesafe.akka"   %% "akka-http-testkit"         % Versions.akkaHttp,
      "com.github.dnvriend" %% "akka-persistence-inmemory" % Versions.akkaPersistenceInmemory
    )

    private lazy val scalaTest: Seq[ModuleID] = Seq("org.scalatest" %% "scalatest" % Versions.scalaTest)

    private lazy val embeddedCassandra: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Versions.akkaPersistenceCassandra
    )

    private lazy val h2: Seq[ModuleID] = Seq("com.h2database" % "h2" % Versions.h2)

//    private lazy val postgres: Seq[ModuleID] = Seq("io.zonky.test" % "embedded-postgres" % Versions.embeddedPostgres)

    private lazy val mockito: Seq[ModuleID] =
      Seq("org.mockito" % "mockito-core" % Versions.mockitoCore)

    private lazy val clapper: Seq[ModuleID] =
      Seq("org.clapper" %% "classutil" % Versions.classutil)

    private lazy val slickTest: Seq[ModuleID] = Seq("com.typesafe.slick" %% "slick-testkit" % Versions.slick)

  }
}
