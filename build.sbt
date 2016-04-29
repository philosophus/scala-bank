lazy val root = (project in file(".")).
  settings(
    name := "scala-bank",
    organization := "com.fdahms",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.11.8"
  )

import sbtprotobuf.{ProtobufPlugin=>PB}

PB.protobufSettings

libraryDependencies ++= Seq(
  "org.zeromq" % "jeromq" % "0.3.5"
)
