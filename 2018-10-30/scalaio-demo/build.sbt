import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "com.paidy.demo",
        scalaVersion := "2.12.7",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    name := "scalaio-demo",
    organizationName := "Paidy Inc",
    startYear := Some(2018),
    licenses += ("MIT", new URL("https://opensource.org/licenses/MIT")),
    scalacOptions ++= commonScalacOptions,
    libraryDependencies ++= Seq(
      compilerPlugin(Libraries.kindProjector),
      compilerPlugin(Libraries.betterMonadicFor),
      Libraries.catsEffect,
      Libraries.catsPar,
      Libraries.fs2Core,
      Libraries.slf4j,
      Libraries.logback % "runtime",
      Libraries.scalaTest,
      Libraries.scalaCheck
    )
  )
  .settings(warnUnusedImport: _*)
  .settings(partialUnification: _*)
  .settings(scalafmtOnCompile := true)
  .enablePlugins(AutomateHeaderPlugin)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xlog-reflective-calls",
  "-Ywarn-inaccessible",
  "-Ypatmat-exhaust-depth",
  "20",
  "-Ydelambdafy:method",
  "-Xmax-classfile-name",
  "100"
)

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= { _.filterNot(Seq("-Xlint", "-Ywarn-unused-import").contains) },
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val partialUnification = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 12 =>
        Seq("-Ypartial-unification")
      case _ =>
        Seq()
    }
  }
)
