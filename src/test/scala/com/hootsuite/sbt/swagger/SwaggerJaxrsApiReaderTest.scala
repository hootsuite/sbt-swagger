package com.hootsuite.sbt.swagger

import com.hootsuite.service.dummy.endpoints.TweetEndpoints
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, FlatSpec}

class SwaggerJaxrsApiReaderTest extends FlatSpec with Matchers  with BeforeAndAfterEach with BeforeAndAfterAll {
  val config = new com.wordnik.swagger.config.SwaggerConfig(
    apiVersion = "beta",
    swaggerVersion = "2.0",
    basePath = "http://localhost",
    apiPath = "/")

  "apiDocs()" should "be awesome" in {
    val apiDocs = new SwaggerJaxrsApiReader(config).apiDocs(List(classOf[TweetEndpoints]))
    apiDocs should equal(apiDocsJson)
  }

  "endpoints()" should "be plenty" in {
    val endpoints: Map[String, String] = new SwaggerJaxrsApiReader(config).endpoints(Seq(classOf[TweetEndpoints]))
    endpoints should equal( Map("/tweet" -> tweetEndpointJson) )
  }

  private val apiDocsJson = """{
    |  "apiVersion" : "beta",
    |  "swaggerVersion" : "2.0",
    |  "apis" : [ {
    |    "path" : "/tweet.{format}",
    |    "description" : "View a tweet, list tweets, or post a tweet. Everything about Tweets!"
    |  } ]
    |}""".stripMargin

  private val tweetEndpointJson = """{
    |  "apiVersion" : "beta",
    |  "swaggerVersion" : "2.0",
    |  "basePath" : "http://localhost",
    |  "resourcePath" : "/tweet",
    |  "produces" : [ "text/plain" ],
    |  "apis" : [ {
    |    "path" : "/tweet/get/{userId}",
    |    "operations" : [ {
    |      "method" : "GET",
    |      "summary" : "Get a tweet of given tweetId",
    |      "notes" : "It actually returns a fixed value just for now.",
    |      "type" : "array",
    |      "items" : {
    |        "$ref" : "Tweet"
    |      },
    |      "nickname" : "get",
    |      "produces" : [ "application/json" ],
    |      "parameters" : [ {
    |        "name" : "userId",
    |        "required" : true,
    |        "type" : "string",
    |        "paramType" : "path",
    |        "allowMultiple" : false
    |      }, {
    |        "name" : "fromDate",
    |        "required" : false,
    |        "type" : "string",
    |        "paramType" : "header",
    |        "allowMultiple" : false
    |      }, {
    |        "name" : "toDate",
    |        "required" : false,
    |        "type" : "string",
    |        "paramType" : "query",
    |        "allowMultiple" : false
    |      }, {
    |        "name" : "what",
    |        "required" : false,
    |        "type" : "string",
    |        "paramType" : "form",
    |        "allowMultiple" : false
    |      }, {
    |        "name" : "who",
    |        "defaultValue" : "me",
    |        "required" : false,
    |        "type" : "string",
    |        "paramType" : "cookie",
    |        "allowMultiple" : false
    |      } ],
    |      "responseMessages" : [ {
    |        "code" : 403,
    |        "message" : "no tweets for you!"
    |      }, {
    |        "code" : 404,
    |        "message" : "invalid user id"
    |      }, {
    |        "code" : 405,
    |        "message" : "GETs only"
    |      } ]
    |    } ]
    |  } ],
    |  "models" : {
    |    "Tweet" : {
    |      "id" : "Tweet",
    |      "properties" : {
    |        "id" : {
    |          "type" : "integer",
    |          "format" : "int32"
    |        },
    |        "userId" : {
    |          "type" : "string"
    |        },
    |        "body" : {
    |          "type" : "string"
    |        }
    |      }
    |    }
    |  }
    |}""".stripMargin
}
