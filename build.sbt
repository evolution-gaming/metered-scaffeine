name := "metered-scaffeine"

organization := "com.evolutiongaming"

homepage := Some(url("https://github.com/evolution-gaming/metered-scaffeine"))

startYear := Some(2017)

organizationName := "Evolution"

organizationHomepage := Some(url("https://evolution.com"))

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("3.8.4", "2.13.18")

releaseCrossBuild := true

Compile / doc / scalacOptions ++= Seq("-groups", "-implicits", "-no-link-warnings")

scalacOptions -= "-Xfatal-warnings"
scalacOptions += "-Werror"

publishTo := Some(Resolver.evolutionReleases)

libraryDependencies ++= Seq(
  "com.github.blemale" %% "scaffeine" % "5.3.0",
  "com.evolutiongaming" %% "executor-tools" % "1.0.5",
  "io.prometheus" % "simpleclient_common" % "0.6.0",
  "org.scalatest" %% "scalatest" % "3.2.20" % Test,
)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

//addCommandAlias("check", "all versionPolicyCheck Compile/doc")
addCommandAlias("check", "show version")
addCommandAlias("build", "+all compile test")
