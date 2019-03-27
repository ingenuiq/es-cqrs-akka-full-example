import sbt._

object Dependencies {

  private[this] object Versions {
    val akka:                     String = "2.5.21"
    val akkaHttp:                 String = "10.1.7"
    val akkaHttpCors:             String = "0.3.4"
    val akkaPersistenceInmemory:  String = "2.5.1.1"
    val akkaPersistenceCassandra: String = "0.92"
    val enumeratum:               String = "1.5.13"
    val logbackClassic:           String = "1.2.3"
    val playJson:                 String = "2.7.1"
    val pureConfig:               String = "0.10.2"
    val scalaLogging:             String = "3.9.2"
    val scalaTest:                String = "3.0.5"
    val akkaInMemory:             String = "2.5.1.1"
    val avro4s:                   String = "2.0.4"
    val slick:                    String = "3.3.0"
    val postgres:                 String = "42.2.5"
    val h2:                       String = "1.4.197"
    val classutil:                String = "1.4.0"
    val mockitoCore:              String = "2.24.0"
    val commonsCodec:             String = "1.12"
    val slf4jVersion:             String = "1.7.25"
    val akkaManagementVersion:    String = "1.0.0-RC4"
    val akkaDiscoveryDnsVersion:  String = "0.20.0"
    val reactiveLibVersion:       String = "1.7.0"
  }

  val all: Seq[ModuleID] = ProductionDependencies.values ++ TestDependencies.values

  private[this] object ProductionDependencies {

    val values
      : Seq[ModuleID] = akka ++ avro4s ++ playJson ++ pureConfig ++ logging ++ query ++ postgres ++ enumeratum ++ kamon ++ commonCodec

    private lazy val akka: Seq[ModuleID] = Seq(
      "com.typesafe.akka"             %% "akka-actor"                          % Versions.akka,
      "com.typesafe.akka"             %% "akka-stream"                         % Versions.akka,
      "com.typesafe.akka"             %% "akka-cluster"                        % Versions.akka,
      "com.typesafe.akka"             %% "akka-cluster-sharding"               % Versions.akka,
      "com.typesafe.akka"             %% "akka-persistence-query"              % Versions.akka,
      "com.typesafe.akka"             %% "akka-http-core"                      % Versions.akkaHttp,
      "com.typesafe.akka"             %% "akka-persistence-cassandra"          % Versions.akkaPersistenceCassandra,
      "ch.megard"                     %% "akka-http-cors"                      % Versions.akkaHttpCors,
      "com.lightbend.akka.management" %% "akka-management"                     % Versions.akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap"   % Versions.akkaManagementVersion,
      "com.lightbend.akka.discovery"  %% "akka-discovery-kubernetes-api"       % Versions.akkaManagementVersion,
      "com.lightbend.akka.discovery"  %% "akka-discovery-dns"                  % Versions.akkaDiscoveryDnsVersion,
      "com.github.TanUkkii007"        %% "akka-cluster-custom-downing"         % "0.0.12",
      "com.lightbend.rp"              %% "reactive-lib-akka-cluster-bootstrap" % Versions.reactiveLibVersion,
      "com.lightbend.rp"              %% "reactive-lib-status"                 % Versions.reactiveLibVersion,
    )

    private lazy val avro4s: Seq[ModuleID] = Seq("com.sksamuel.avro4s" %% "avro4s-core" % Versions.avro4s)

    private lazy val logging: Seq[ModuleID] = Seq(
      "ch.qos.logback"             % "logback-classic"  % Versions.logbackClassic,
      "com.typesafe.scala-logging" %% "scala-logging"   % Versions.scalaLogging,
      "org.slf4j"                  % "log4j-over-slf4j" % Versions.slf4jVersion,
      "com.typesafe.akka"          %% "akka-slf4j"      % Versions.akka
    )

    private lazy val pureConfig: Seq[ModuleID] = Seq("com.github.pureconfig" %% "pureconfig" % Versions.pureConfig)
    private lazy val query: Seq[ModuleID] =
      Seq("com.typesafe.slick" %% "slick" % Versions.slick, "com.typesafe.slick" %% "slick-hikaricp" % Versions.slick)

    private lazy val enumeratum: Seq[ModuleID] = Seq("com.beachape" %% "enumeratum" % Versions.enumeratum)

    private lazy val postgres: Seq[ModuleID] = Seq("org.postgresql" % "postgresql" % Versions.postgres)

    private lazy val playJson: Seq[ModuleID] = Seq("com.typesafe.play" %% "play-json" % Versions.playJson)

    private lazy val kamon: Seq[ModuleID] = Seq(
      "io.kamon" %% "kamon-akka-2.5"        % "1.1.3",
      "io.kamon" %% "kamon-scala-future"    % "1.0.0",
      "io.kamon" %% "kamon-core"            % "1.1.4",
      "io.kamon" %% "kamon-logback"         % "1.0.5",
      "io.kamon" %% "kamon-zipkin"          % "1.0.0",
      "io.kamon" %% "kamon-akka-http-2.5"   % "1.1.1",
      "io.kamon" %% "kamon-prometheus"      % "1.1.1",
      "io.kamon" %% "kamon-system-metrics"  % "1.0.0",
      "io.kamon" %% "kamon-jdbc"            % "1.0.2",
      "io.kamon" %% "kamon-akka-remote-2.5" % "1.1.0"
    )

    private lazy val commonCodec: Seq[ModuleID] = Seq("commons-codec" % "commons-codec" % Versions.commonsCodec)
  }

  private[this] object TestDependencies {

    private val TestAndITs = "test;it"

    lazy val values: Seq[ModuleID] =
      (akkaTest ++ scalaTest ++ otherDepsTest ++ embeddedCassandra ++ h2 ++ slickTest).map(_ % TestAndITs)

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

    private lazy val otherDepsTest: Seq[ModuleID] =
      Seq("org.mockito" % "mockito-core" % Versions.mockitoCore, "org.clapper" %% "classutil" % Versions.classutil)

    private lazy val slickTest: Seq[ModuleID] = Seq("com.typesafe.slick" %% "slick-testkit" % Versions.slick)

  }
}
