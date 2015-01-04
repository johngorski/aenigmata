val junit = "junit" % "junit" % "4.10" % "test"

val scalatest = "org.scalatest" %% "scalatest" % "1.9.1" % "test"

val scalacheck = "org.scalacheck" %% "scalacheck" % "1.10.1"

lazy val commonSettings = Seq(
  organization := "github.johngorskijr",
  version := "1.0.0",
  scalaVersion := "2.10.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "aenigmata",
    libraryDependencies += junit,
    libraryDependencies += scalatest,
    libraryDependencies += scalacheck
  )
