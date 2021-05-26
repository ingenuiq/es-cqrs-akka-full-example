import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import sbt.{ Compile, Resolver, Setting, URL }
import sbt.Keys._

object Settings {

  val settings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.13.6",
    scalacOptions := Seq(
          "-unchecked",
          "-feature",
          "-deprecation",
          "-encoding",
          "utf8",
          //        "-Xfatal-warnings", // Fail the compilation if there are any warnings.
          "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
          "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
          "-Xlint:package-object-classes", // Class or object defined in package object.
          "-Xlint:adapted-args" // Warn if an argument list is modified to match the receiver.
          // "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
        ),
    scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    scalafmtOnCompile in Compile := true,
    publishMavenStyle := false
  )
}
