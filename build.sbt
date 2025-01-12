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

wartremoverWarnings ++= Warts.all
wartremoverWarnings --= Seq(
  Wart.Nothing
)

lazy val root = (project in file("."))
  .settings(
    name := "pps-23-ulisse",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "io.cucumber" %% "cucumber-scala" % "8.25.1" % Test,
      "com.tngtech.archunit" % "archunit" % "1.3.0" % Test,
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % Test,
    ),
    Global / onLoad ~= (_ andThen ("hooks" :: _)),
  )