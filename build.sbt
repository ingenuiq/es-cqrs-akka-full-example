import sbt.Keys.fork

val projectName         = "full-cqrs-example"
val projectOrganization = "com.ingenuiq"
val mainClassName       = "com.ingenuiq.note.Main"

lazy val generateAvsc = taskKey[Unit]("Generate .avsc from .avdl for avro")

import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings
inConfig(IntegrationTest)(scalafmtConfigSettings)

lazy val root = project
  .in(file("."))
  .configs(IntegrationTest extend Test)
  .enablePlugins(AshScriptPlugin, JavaServerAppPackaging, JavaAgent)
  .settings(
    Settings.settings,
    Defaults.itSettings,
    DockerSettings.settings,
    Compile / packageDoc / publishArtifact := false,
    libraryDependencies ++= Dependencies.all,
    libraryDependencies ++= Dependencies.allScala2.map(_.cross(CrossVersion.for3Use2_13)),
    Compile / run / mainClass := Some(mainClassName),
    Compile / packageBin / mainClass := Some(mainClassName),
    executableScriptName := Project.normalizeModuleID(mainClassName.split('.').last),
    name := projectName,
    organization := projectOrganization,
    version := version.value,
    Test / parallelExecution := false,
    IntegrationTest / parallelExecution := false,
    run / fork := true,
    Test / packageBin / publishArtifact := true,
    Global / concurrentRestrictions += Tags.limit(Tags.Test, 1),
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.9.2",
    Universal / javaOptions += "-Dorg.aspectj.tracing.factory=default",
    AvroSupport.avroSettings
//    resolvers ++= repos
  )

//val repos = Seq(
//  "confluent" at "https://packages.confluent.io/maven/",
//  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
//  DefaultMavenRepository,
//  Resolver.sonatypeRepo("public")
//)
