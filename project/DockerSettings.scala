import com.typesafe.sbt.GitPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.Docker
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport._
import java.time.Clock
import sbt.Def._
import sbt.Keys._

object DockerSettings {

  lazy val settings: Seq[Setting[_]] = Seq(
    Docker / daemonUser := "65534",
    dockerAlias := DockerAlias(
          dockerRepository.value,
          dockerUsername.value,
          packageName.value,
          Option(sys.env.getOrElse("DOCKER_IMAGE_TAG", git.gitDescribedVersion.value.getOrElse(version.value)))
        ),
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerBuildOptions ++= Seq("--pull"),
    dockerCommands := {
      dockerCommands.value.flatMap {
        case eq @ Cmd("EXPOSE", _) =>
          Seq(eq, Cmd("RUN", "apk add --no-cache tini"))
        case other => Seq(other)
      }
    },
    dockerEntrypoint := Seq("tini") ++ dockerEntrypoint.value,
    dockerExposedPorts := Seq(8080),
    dockerLabels := Map(
          "BUILD_BRANCH" -> git.gitCurrentBranch.value,
          "BUILD_COMMIT" -> git.gitHeadCommit.value.getOrElse(""),
          "BUILD_TIME" -> Clock.systemUTC().instant().toString,
          "SERVICE_NAME" -> sys.env.getOrElse("SERVICE_NAME", name.value)
        )
  )
}
