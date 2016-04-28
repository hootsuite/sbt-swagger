import sbt._

sbtPlugin := true

name := "sbt-swagger"

version := "1.0.0-SNAPSHOT" // increment this every time when you make change!

// This needs to be 2.10.4 as SBT 0.13.x plugins need to be compiled against scala 2.10.
// SBT plugins are not cross-compilable to different scala versions at this time.
scalaVersion := "2.10.4"

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "com.wordnik"   %% "swagger-core"  % "1.3.12",
  "com.wordnik"   %% "swagger-jaxrs" % "1.3.12",
  "org.clapper"   %% "classutil"     % "1.0.5",
  "org.scalatest" %% "scalatest"     % "2.2.1"   % "test"
)

organization := "com.hootsuite"

Settings.publishSettings
