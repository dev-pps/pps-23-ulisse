import scala.sys.process.*
ThisBuild / scalaVersion := "3.3.4"

val hooks = taskKey[Unit]("change git hooks path to a tracked folder")
hooks := {
  val command = "git config core.hooksPath git-hooks"
  println(s"changeHooksPath command exited with code: ${command.!}")
}

scalafmtOnCompile := true
coverageEnabled := true

wartremoverErrors ++= Warts.unsafe
wartremoverErrors --= Seq(
  Wart.DefaultArguments,
)
wartremoverWarnings ++= Warts.all

lazy val root = (project in file("."))
  .settings(
    name := "pps-23-ulisse",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    ),
    Global / onLoad ~= (_ andThen ("hooks" :: _)),
  )