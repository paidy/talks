import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.paidy.talks.http4s",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-Xfuture",
      "-Xlog-reflective-calls",
      "-Ywarn-inaccessible",
      "-Ypatmat-exhaust-depth", "20",
      "-Ydelambdafy:method",
      "-Xmax-classfile-name", "100"
    ),
    name := "http4s-talk",
    libraryDependencies ++= Seq(
      compilerPlugin(Libraries.kindProjector),
      Libraries.catsEffect,
      Libraries.fs2Core,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sDsl,
      Libraries.http4sCirce,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeGenericX,
      Libraries.logback    % Runtime,
      Libraries.scalaTest  % Test,
      Libraries.scalaCheck % Test
    )
  )
