name := "metered-scaffeine"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/metered-scaffeine"))

startYear := Some(2017)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.12.2", "2.11.11")

releaseCrossBuild := true

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"
)

scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits", "-no-link-warnings")

resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

libraryDependencies ++= Seq(
  "com.github.blemale" %% "scaffeine" % "2.1.0",
  "io.dropwizard.metrics" % "metrics-core" % "3.2.1",
  "nl.grons" %% "metrics-scala" % "3.5.6",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)

licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))
