name := "PPS25-Scalamon"
version := "0.1.0"

scalaVersion := "3.4.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"

Compile / mainClass := Some("scalamon.gui.BattleApp")
Compile / run / fork := true

inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq("-Wunused:all")
  )
)