name := "PPS25-Scalamon"
version := "0.1.0"

scalaVersion := "3.4.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq("-Wunused:all")
  )
)