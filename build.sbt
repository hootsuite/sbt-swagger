import sbt._
import Version._

sbtPlugin := true
publishMavenStyle := false
bintrayRepository := "sbt-plugins"
bintrayOrganization in bintray := None

name         := "sbt-swagger"
organization := "com.hootsuite"
organizationName := "Hootsuite Media Inc."
organizationHomepage := Some(url("http://hootsuite.com"))
version      := Version.project
// This needs to be 2.10.4 as SBT 0.13.x plugins need to be compiled against scala 2.10.
// SBT plugins are not cross-compilable to different scala versions at this time.
scalaVersion := Version.scala

scalacOptions ++= Vector(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "UTF-8"
)

resolvers += Resolver.jcenterRepo
libraryDependencies ++= Seq(
  "com.wordnik"   %% "swagger-core"  % "1.3.12",
  "com.wordnik"   %% "swagger-jaxrs" % "1.3.12",
  "org.clapper"   %% "classutil"     % "1.0.5",
  "org.scalatest" %% "scalatest"     % "2.2.1"   % Test
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
