import sbt.Keys.fork

val projectName         = "full-cqrs-example"
val projectOrganization = "com.ingenuiq"
val mainClassName       = "com.ingenuiq.note.Main"

lazy val generateAvsc  = taskKey[Unit]("Generate .avsc from .avdl for avro")

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
    publishArtifact in (Compile, packageDoc) := false,
    libraryDependencies ++= Dependencies.all,
    mainClass in (Compile, run) := Some(mainClassName),
    mainClass in (Compile, packageBin) := Some(mainClassName),
    executableScriptName := Project.normalizeModuleID(mainClassName.split('.').last),
    name := projectName,
    organization := projectOrganization,
    version := version.value,
    parallelExecution in Test := false,
    parallelExecution in IntegrationTest := false,
    fork in run := true,
    publishArtifact in (Test, packageBin) := true,
    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.9.2",
    javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default",
    AvroSupport.avroSettings,
    resolvers +=   Resolver.bintrayRepo("tanukkii007", "maven")
  )
