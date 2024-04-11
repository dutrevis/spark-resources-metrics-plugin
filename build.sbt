name := "spark-resources-metrics-plugin"

version := "0.1-SNAPSHOT"
isSnapshot := true

scalaVersion := "2.12.15"
crossScalaVersions := Seq("2.12.15", "2.13.8")

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

libraryDependencies += "io.dropwizard.metrics" % "metrics-core" % "4.2.7"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.0"
libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.17" % "test"
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.11.3" % "test"
libraryDependencies += "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % "test"

coverageEnabled := true
// coverageFailOnMinimum := true
// coverageMinimumStmtTotal := 90
// coverageMinimumBranchTotal := 90
// coverageMinimumStmtPerPackage := 90
// coverageMinimumBranchPerPackage := 85
// coverageMinimumStmtPerFile := 85
// coverageMinimumBranchPerFile := 80

// publishing to Sonatype Nexus repository and Maven
publishMavenStyle := true

organization := "io.github.dutrevis"

description := """
  Spark Resource Metrics plugin is an Apache Spark plugin that
  sinks values into the Apache Spark metrics system, obtained
  from operational system's sources, aiming to cover metrics
  that the Spark metrics system does not provide but other
  metric systems do, like the Ganglia monitoring system.
"""

developers := List(
  Developer(
    "dutrevis",
    "Eduardo Trevisani",
    "",
    url("https://github.com/dutrevis")
  )
)
homepage := Some(
  url("https://github.com/dutrevis/spark-resources-metrics-plugin")
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/dutrevis/spark-resources-metrics-plugin"),
    "scm:git@github.com:dutrevis/spark-resources-metrics-plugin.git"
  )
)
