import sbt._

object Dependencies {

  object Versions {
    val catsEffect = "1.0.0"
    val catsPar    = "0.2.0"
    val fs2        = "1.0.0-RC2"

    val slf4j      = "1.7.25"
    val logback    = "1.1.3"

    val kindProjector    = "0.9.8"
    val betterMonadicFor = "0.3.0-M2"

    val scalaTest  = "3.0.5"
    val scalaCheck = "1.14.0"
  }

  object Libraries {
    lazy val catsEffect = "org.typelevel"         %% "cats-effect"      % Versions.catsEffect
    lazy val catsPar    = "io.chrisdavenport"     %% "cats-par"         % Versions.catsPar
    lazy val fs2Core    = "co.fs2"                %% "fs2-core"         % Versions.fs2

    lazy val kindProjector    = "org.spire-math" % "kind-projector" % Versions.kindProjector cross CrossVersion.binary
    lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor

    lazy val slf4j   = "org.slf4j" % "slf4j-api" % Versions.slf4j
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    lazy val scalaTest  = "org.scalatest"  %% "scalatest"  % Versions.scalaTest  % "test"
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck % "test"
  }

}
