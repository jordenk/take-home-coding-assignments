import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.github.jordenk"
ThisBuild / organizationName := "github.jordenk"

lazy val root = (project in file("."))
  .settings(
    name := "attire-advisor",
    libraryDependencies += scalaTest % Test
  )

