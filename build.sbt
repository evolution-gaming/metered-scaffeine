name := "metered-scaffeine"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/metered-scaffeine"))

startYear := Some(2017)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
//  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"
)

scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits", "-no-link-warnings")

resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

libraryDependencies ++= Seq(
  "com.github.blemale" %% "scaffeine" % "2.0.0",
  "io.dropwizard.metrics" % "metrics-core" % "3.1.2",
  "nl.grons" %% "metrics-scala" % "3.5.2_a2.3"
)

licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))
