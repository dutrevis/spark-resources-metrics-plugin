logLevel := Level.Warn

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.10")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.4")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")

// This instructs sbt to always include the dependency without creating an error for different available versions.
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
