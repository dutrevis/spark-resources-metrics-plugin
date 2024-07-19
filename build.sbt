name := "spark-resources-metrics-plugin"

scalaVersion := "2.12.19"
crossScalaVersions := Seq("2.12.19", "2.13.13")

libraryDependencies += "io.dropwizard.metrics" % "metrics-core" % "4.2.7"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.0"
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.17.31" % "test"
libraryDependencies += "org.scalamock" %% "scalamock" % "5.1.0" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.17" % "test"

// https://github.com/scoverage/sbt-scoverage/issues/84
Default / coverageEnabled := false
Test / test / coverageMinimumStmtTotal := 60
Test / test / coverageFailOnMinimum := false
Test / test / coverageHighlighting := true
// coverageMinimumBranchTotal := 90
// coverageMinimumStmtPerPackage := 90
// coverageMinimumBranchPerPackage := 85
// coverageMinimumStmtPerFile := 85
// coverageMinimumBranchPerFile := 80

inThisBuild(
  List(
    organization := "io.github.dutrevis",
    description := """
        Spark Resources Metrics plugin is an Apache Spark plugin that
        registers metrics onto the Apache Spark metrics system, that
        will sink values collected from operational system's resources,
        aiming to cover metrics that the Spark metrics system do not
        provide, like the Ganglia monitoring system metrics.
    """,
    homepage := Some(
      url("https://github.com/dutrevis/spark-resources-metrics-plugin")
    ),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        id = "dutrevis",
        name = "Eduardo Trevisani Gamba",
        email = "",
        url = url("https://github.com/dutrevis")
      )
    ),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
  )
)
