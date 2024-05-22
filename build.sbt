name := "spark-resources-metrics-plugin"

version := "0.1-SNAPSHOT"
isSnapshot := true

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
