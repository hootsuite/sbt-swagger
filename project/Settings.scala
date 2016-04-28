import bintray.BintrayPlugin.autoImport._
import sbt.Keys._
import sbt._
import Version._

object Settings {
  lazy val publishSettings =
    if (Version.project.endsWith("-SNAPSHOT"))
      Seq(
        publishTo := Some("Artifactory Realm" at "http://oss.jfrog.org/artifactory/oss-snapshot-local"),
        bintrayReleaseOnPublish := false,
        // Only setting the credentials file if it exists (#52)
        credentials := List(Path.userHome / ".bintray" / ".artifactory").filter(_.exists).map(Credentials(_)),
        licenses := ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil // this is required! otherwise Bintray will reject the code
      )
    else
      Seq(
        organization := "com.hootsuite",
        pomExtra := <scm>
          <url>https://github.com/hootsuite/sbt-swagger</url>
          <connection>https://github.com/hootsuite/sbt-swagger</connection>
        </scm>
          <developers>
            <developer>
              <id>johnny.bufu</id>
              <name>Johnny Bufu</name>
              <url>http://www.hootsuite.com/</url>
            </developer>
            <developer>
              <id>tatsuhiro.ujihisa</id>
              <name>Tatsuhiro Ujihisa</name>
              <url>http://www.hootsuite.com/</url>
            </developer>
          </developers>,
        publishArtifact in Test := false,
        homepage := Some(url("https://github.com/hootsuite/sbt-swagger")),
        publishMavenStyle := true,
        pomIncludeRepository := { _ => false },
        resolvers += Resolver.url("sbt-swagger", url("http://dl.bintray.com/hootsuite/maven"))(Resolver.ivyStylePatterns),
        licenses := ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil // this is required! otherwise Bintray will reject the code
      )
}
