name := """taxi-fares"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.0.0-RC2",
  "com.google.inject" % "guice" % "3.0",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.2.0" % "test",
  "org.xerial" % "sqlite-jdbc" % "3.7.2"
)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"