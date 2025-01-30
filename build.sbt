import scala.sys.process.*
ThisBuild / scalaVersion := "3.3.4"

val hooks = taskKey[Unit]("change git hooks path to a tracked folder")
hooks := {
  val command = "git config core.hooksPath git-hooks"
  println(s"changeHooksPath command exited with code: ${command.!}")
}

scalacOptions += "-deprecation"

scalafmtOnCompile := true
coverageEnabled   := true

wartremoverWarnings ++= Warts.all
wartremoverWarnings --= Seq(
  Wart.ImplicitParameter,
  Wart.Nothing,
  Wart.Equals
)

wartremoverErrors ++= Warts.unsafe
wartremoverErrors --= Seq(
)

Test / fork := true
Test / javaOptions ++= Seq(
  "-Xmx2G",
)

lazy val root = (project in file("."))
  .settings(
    name := "pps-23-ulisse",
    libraryDependencies ++= Seq(
      "com.formdev" % "flatlaf" % "3.5.4",
      "com.formdev" % "flatlaf-extras" % "3.5.4",
      "com.miglayout" % "miglayout" % "3.7.4",
      "org.typelevel"          %% "cats-core"         % "2.12.0",
      "org.scala-lang.modules" %% "scala-swing"       % "3.0.0",
      "org.scala-lang.modules" %% "scala-swing"       % "3.0.0",
      "org.slf4j"               % "slf4j-simple"      % "2.0.13",
      "org.scalatestplus"      %% "mockito-5-10"      % "3.2.18.0" % Test,
      "org.scalatest"          %% "scalatest"         % "3.2.19"   % Test,
      "io.cucumber"            %% "cucumber-scala"    % "8.25.1"   % Test,
      "com.tngtech.archunit"    % "archunit"          % "1.3.0"    % Test,
      "org.junit.jupiter"       % "junit-jupiter-api" % "5.10.3"   % Test
      ),
    Global / onLoad ~= (_ andThen ("hooks" :: _))
  )
