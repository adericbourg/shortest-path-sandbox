organization := "net.dericbourg"

name := "shortest-path-sandbox"

description := "Sandbox around shortest paths computations"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.0"

mainClass in(Compile, run) := Some("net.dericbourg.ratp.gtfs.Import")

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc41",
  "io.spray" %% "spray-json" % "1.3.2"
)

libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "scala-csv" % "1.3.4"
)

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "20.0"
)

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
