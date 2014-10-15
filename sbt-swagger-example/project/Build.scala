import com.hootsuite.sbt.swagger
import sbt._
import sbt.Keys._

object SbtSwaggerExampleBuild extends Build {
  name := "sbt-swagger-example"

  val swaggerSettings = Seq(
    swagger.Sbt.apiVersion := "1.0",
    swagger.Sbt.basePath := "http://localhost",
    swagger.Sbt.apiPath := "/",
    //swagger.Sbt.packages := swagger.Sbt.All
    swagger.Sbt.packages := swagger.Sbt.WhitelistPrefixes(Seq("com.hootsuite")),
    swagger.Sbt.excludePropertyClassPrefixes := Set("play.api.libs.json")) ++
    com.hootsuite.sbt.swagger.Sbt.swaggerSettings

  lazy val dependencySettings = Seq(
    libraryDependencies ++= Seq(
      "com.wordnik"          %  "swagger-annotations" % "1.3.10",
      "javax.ws.rs"          % "jsr311-api"           % "1.1.1"))

  lazy val myProject = Project(
    id = "sbt-swagger-example",
    base = file("."),
    settings = dependencySettings ++ swaggerSettings)
}
