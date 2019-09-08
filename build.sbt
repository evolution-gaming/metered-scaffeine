name := "metered-scaffeine"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/metered-scaffeine"))

startYear := Some(2017)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.0", "2.12.9")

releaseCrossBuild := true

scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits", "-no-link-warnings")

resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

libraryDependencies ++= Seq(
  "com.github.blemale"  %% "scaffeine"           % "3.1.0",
  "com.evolutiongaming" %% "executor-tools"      % "1.0.2",
  "io.prometheus"        % "simpleclient_common" % "0.6.0",
  "org.scalatest"       %% "scalatest"           % "3.0.8" % Test
)

licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))
