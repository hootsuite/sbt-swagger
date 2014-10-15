package com.hootsuite.sbt.swagger

import java.io.{File, FileWriter}
import java.lang.annotation.Annotation

import org.clapper.classutil.{ClassFinder, ClassInfo}
import sbt.Logger
import sbt.PluginManagement.PluginClassLoader

import scala.util.control.NonFatal

object Sbt extends sbt.Plugin {

  object Keys {
    lazy val sbtSwagger = sbt.TaskKey[Unit]("swagger", "generates JSON files for swagger, under target/swagger directory")
  }

  sealed trait Packages
  case class WhitelistPrefixes(prefixes: Seq[String]) extends Packages
  case object All extends Packages

  /**
   * https://github.com/wordnik/swagger-spec/blob/master/versions/2.0.md
   */
  val apiVersion = sbt.SettingKey[String]("apiVersion",
    "Provides the version of the application API (not to be confused by the specification version).")
  // val swaggerVersion = sbt.SettingKey[String]("swaggerVersion", "...")

  val basePath = sbt.SettingKey[String]("basePath",
    "The base path on which the API is served, which is relative to the host. If it is not included, the API is served directly under the host. The value MUST start with a leading slash (/). The basePath does not support path templating.")

  val apiPath = sbt.SettingKey[String]("apiPath", "apparently not used by the extractors at all")

  val packages = sbt.SettingKey[Packages]("packages", """Package name prefix, like "com.hootsuite.service.aaa"""")

  val excludePropertyClassPrefixes = sbt.SettingKey[Set[String]]("excludePropertyClassPrefixes",
    """Set of package[.classname] prefixes. Model properties whose fully qualified classname starts with an entry from the list will be excluded from the model."""".stripMargin)

  lazy val swaggerSettings: Seq[sbt.Def.Setting[_]] = Seq(
    Keys.sbtSwagger <<= (
      apiVersion, basePath, apiPath, packages, excludePropertyClassPrefixes,
      sbt.Keys.appConfiguration in sbt.GlobalScope, sbt.Keys.fullClasspath in sbt.Runtime, sbt.Keys.streams, sbt.Keys.target, sbt.Keys.classDirectory in sbt.Compile
    ).map {
      case (apiVersion, basePath, apiPath, packages, excludePropertyClassPrefixes, appConfiguration, classpath, streams, targetDirectory, classDirectory) =>

        implicit val log = streams.log("sbt-swagger")

        val swaggerConfig = new com.wordnik.swagger.config.SwaggerConfig(
          apiVersion = apiVersion, swaggerVersion = "2.0",
          basePath = basePath, apiPath = apiPath
        )

        val predicate: ClassInfo => Boolean = packages match {
          case WhitelistPrefixes(wl) => c => wl.exists(c.name.startsWith)
          case All => _ => true
        }

        val pluginClassLoader = classOf[com.wordnik.swagger.annotations.Api].getClassLoader.asInstanceOf[PluginClassLoader]

        // inject project dependencies into pluginClassLoader
        pluginClassLoader.add(classpath.files.map(_.toURI.toURL))

        // find project's compiled classes
        log.info(s"== sbt-swagger ==> looking for compiled project classes in: ${classDirectory.toURI.toURL}")
        val projectClassInfos = ClassFinder(Seq(classDirectory)).getClasses().filter(predicate).toList

        // inject project's classes into pluginClassLoader
        pluginClassLoader.add(projectClassInfos.map(_.location.toURI.toURL))

        // use pluginClassLoader prepared above to load project classes
        val projectClasses = projectClassInfos.map { classInfo => Class.forName(classInfo.name, false, pluginClassLoader) }
        log.info(s"== sbt-swagger ==> loaded ${projectClasses.size} project classes")

        val swaggerReader = new SwaggerJaxrsApiReader(swaggerConfig, excludePropertyClassPrefixes)
        val apiDocs = swaggerReader.apiDocs(projectClasses.toList)
        val endpoints = swaggerReader.endpoints(projectClasses.toList)

        val outputDir = new File(targetDirectory, "swagger")
        val apiDocsOutput = new File(outputDir, "api-docs.json")
        apiDocsOutput.getParentFile.mkdirs()
        withCloseable(new FileWriter(apiDocsOutput))(_.write(apiDocs))

        endpoints.foreach { case (endpointName, endpointJson) =>
          withCloseable(new FileWriter(new File(outputDir, s"$endpointName.json")))(_.write(endpointJson))
        }

        log.info(s"== sbt-swagger ==> $apiDocsOutput:\n$apiDocs\n\n")
        log.info(s"== sbt-swagger ==> ${endpoints.mkString("\n")}\n\n")
    },
    Keys.sbtSwagger <<= Keys.sbtSwagger.dependsOn(sbt.Keys.compile in sbt.Compile)
  )

  private def withCloseable[C <: {def close()},T](c: C)(block: (C) => T)(implicit log: Logger): T = {
    try { block(c) }
    finally {
      try { c.close() }
      catch { case NonFatal(e) => log.warn(e.getMessage) }
    }
  }

  private def getLoadedClasses(classLoader: ClassLoader, log: Logger): Array[Class[_]] = {
    val classesField = classOf[ClassLoader].getDeclaredField("classes")
    classesField.setAccessible(true)
    classesField.get(classLoader).asInstanceOf[java.util.Vector[Class[_]]].toArray[Class[_]](Array[Class[_]]())
    //.foreach(loaded => log(s"loaded class: $loaded"))
  }

  private def oneOffTest(classLoader: ClassLoader)(implicit log: Logger): Unit = {
    val apiAnnotation: Class[_] = Class.forName(classOf[com.wordnik.swagger.annotations.Api].getName, true, classLoader)
    val noGoodApiAnnotation: Class[_] = classOf[com.wordnik.swagger.annotations.Api]

    log.info(s"api annotation classloader from classOf[]: ${noGoodApiAnnotation.getClassLoader}")
    log.info(s"api annotation classloader from constructed classloader: ${apiAnnotation.getClassLoader}")

    log.info("ok so far...")

    Class.forName("com.hootsuite.service.owlydata.endpoints.MemberEndpoints", true, classLoader)

    log.info( noGoodApiAnnotation.toString )
    log.info( (apiAnnotation eq noGoodApiAnnotation).toString )
    log.info( (apiAnnotation == noGoodApiAnnotation).toString )

    val annotations = Class.forName("com.hootsuite.service.owlydata.endpoints.MemberEndpoints", true, classLoader).getAnnotations
    annotations.filter(_.annotationType().getName.endsWith("com.wordnik.swagger.annotations.Api")) foreach { (ann: Annotation) =>
      println(ann.annotationType)
      println(ann.annotationType.isAssignableFrom(classOf[com.wordnik.swagger.annotations.Api])) // false
      println(ann.annotationType == classOf[com.wordnik.swagger.annotations.Api]) // false
      println(ann.isInstanceOf[com.wordnik.swagger.annotations.Api]) // false
      println(ann.annotationType == apiAnnotation) // true
      log.info("cast!! " + ann.asInstanceOf[com.wordnik.swagger.annotations.Api])
    }
  }

}
