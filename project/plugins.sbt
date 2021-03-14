addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.10")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.15")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M7")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("com.julianpeeters" % "sbt-avrohugger" % "2.0.0-RC15")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.1")

resolvers += Resolver.bintrayRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon"          % "sbt-aspectj-runner" % "1.1.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent"      % "0.1.4")

libraryDependencies += "org.apache.avro" % "avro-tools" % "1.8.2"
