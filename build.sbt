name := "akka-mem-leak-demo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.7"

libraryDependencies += "com.typesafe.akka" %% "akka-cluster-typed" % "2.5.18"

scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfatal-warnings",
)