inThisBuild(List(
  organization := "co.blocke",
  homepage := Some(url("https://github.com/gzoller/scala-reflection")),
  licenses := List("MIT" -> url("https://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      "gzoller",
      "Greg Zoller",
      "gzoller@blocke.co",
      url("http://www.blocke.co")
    ),
    Developer(
      "pjfanning",
      "PJ Fanning",
      "",
      url("https://github.com/pjfanning")
    )
  )
))

name := "rnd"
ThisBuild / organization := "co.blocke"
ThisBuild / scalaVersion := "3.3.0"

lazy val root = project
  .in(file("."))
  .settings(settings)
  .settings(
    name := "reflection_library",
    // Compile / packageBin / mappings += {
    //   (baseDirectory.value / "plugin.properties") -> "plugin.properties"
    // },
    doc := null,  // disable dottydoc for now
    Compile / doc / sources := Seq(),
    //sources in (Compile, doc) := Seq(),
    Test / parallelExecution := false,
    libraryDependencies ++= Seq(
      "co.blocke" %% "scala-reflection" % "new_valueOf_bd41b4",//"1.2.0",
      // "co.blocke" %% "scalajack" % "7.0.3",
      "org.scalameta"  %% "munit"  % "0.7.29" % Test
    )
  )

//==========================
// Settings
//==========================
lazy val settings = Seq(
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= compilerOptions,
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-deprecation",
  "-explain",
  "-encoding",
  "utf8"
)
