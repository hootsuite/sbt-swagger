## Swagger SBT Plugin

SBT plugin for extracting Swagger & JAX-RS (jsr311) annotations from compiled classes
into Swagger API models, which are then serialized to JSON for consumption with
[swagger-ui](http://swagger.io/swagger-ui/).

### Installation

Modify the following 2 files in your project as described below:

* `project/plugins.sbt`
* `project/Build.scala`

#### project/plugins.sbt

Add `sbt-swagger` to your project's plugins:

```scala
addSbtPlugin("com.hootsuite" %% "sbt-swagger" % "1.0.0")
```

#### project/Build.scala

Configure your project's SBT build to use it.
Make sure to replace `com.example.your.project.package` in the example below with your project's package prefix.

```scala
import com.hootsuite.sbt.swagger
val swaggerSettings = Seq(
  swagger.Sbt.apiVersion := "1.0",
  swagger.Sbt.basePath := "http://localhost",
  swagger.Sbt.apiPath := "/",
  //swagger.Sbt.packages := swagger.Sbt.All
  swagger.Sbt.packages := swagger.Sbt.WhitelistPrefixes(Seq("com.example.your.project.package")),
  swagger.Sbt.excludePropertyClassPrefixes := Set("play.api.libs.json")
) ++ com.hootsuite.sbt.swagger.Sbt.swaggerSettings
libraryDependencies ++= Seq(
  // API annotations
  "com.wordnik"          %  "swagger-annotations" % "1.3.10",
  // The following is only necessary if you're having trouble resolving dependencies
  "javax.ws.rs"          % "jsr311-api"           % "1.1.1")

// merge swaggerSettings into the project's settings
lazy val myProject = Project(
  id = "something-meaingful",
  base = file("."),
  settings = platformSettings ++
             dependencySettings ++
             swaggerSettings ++
             ...
)
```

### Example

Check https://github.com/hootsuite/sbt-swagger/tree/master/sbt-swagger-example directory.
This is a small complete sample application that uses sbt-swagger.

### Usage

`sbt swagger`

Generates swagger JSON output in `target/swagger/*.json` files.

### API Annotations

Swagger's `@Api*` annotations are the main markers and can be complemented with additional semantics
with `javax.ws.rs.*` annotations. Neither Swagger nor JAX-RS alone offer a complete set of functionality.

This extractor favors Swagger's `@Api*` annotations over `javax.ws.rs.*` ones where there is an overlap:
if a piece of information is specified using both Swagger `@Api*` and `javax.ws.rs.*` annotation,
the generated models will use the information from Swagger's annotation.

```scala
// com.wordnik.swagger.annotations.*
// class level
@Api
@ApiModel
// method level
@ApiOperation
@ApiResponses
@ApiResponse
// parameter level
@ApiParam
@ApiModelProperty
// field level
@ApiModelProperty
```

```scala
// javax.ws.rs.*
// class level:
@Path
@Consumes
@Produces
// method level:
@GET
@POST
@HEAD
@PUT
@DELETE
@Path
@Consumes
@Produces
// parameter level:
@PathParam
@HeaderParam
@QueryParam
@FormParam
@CookieParam
@DefaultValue
```

### Examples

See [TweetEndpoints](src/test/scala/com/hootsuite/service/dummy/endpoints/TweetEndpoints.scala) :

```scala
@Path("/tweet")
@Api(value = "/tweet", position = 2, description = "View a tweet, list tweets, or post a tweet. Everything about Tweets!")
@Produces(Array("text/plain"))
trait TweetEndpoints  {

  @GET // one operation to rule them all #annotations
  @Path("/get/{userId}")
  @ApiOperation(
    value = "Get a tweet of given tweetId",
    notes = "It actually returns a fixed value just for now.",
    position = 8, // relative to others in the generated output
    response = classOf[Tweet],
    responseContainer = "List")
  @Produces(Array("application/json"))
  @ApiResponses(value = Array(
    new ApiResponse(code = 403, message = "no tweets for you!"),
    new ApiResponse(code = 404, message = "invalid user id"),
    new ApiResponse(code = 405, message = "GETs only")
  ))
  def get( @PathParam("userId") userId: String,
           @HeaderParam("fromDate") fromDate: String,
           @QueryParam("toDate") toDate: String,
           @FormParam("what") what: String,
           @CookieParam("who") @DefaultValue("me") who: String
         ): Future[List[Tweet]] = {
    Future.successful(List(Tweet(123123, s"$userId", "an awesome message"), Tweet(79878999, s"$userId", "another one")))
  }
}

case class Tweet(id: Int, userId: String, body: String)
```

### Known issues and workarounds

- Repeated invocations of the `swagger` command from the sbt console won't see updated/recompiled
classes. Need to quit/reload sbt, or run `sbt swagger` from the shell each time.
TODO: see issue #6 https://github.com/hootsuite/sbt-swagger/issues/6.

- All method parameters are automatically considered API parameters -
no option to hide some that are not part of the API.

Workaround: extract a method that represents a 1:1 mapping with the actual exposed API
in terms of parameters and input-output models, and wrap it as necessary for pre- (validation, etc)
and post-processing.

- Map data types are not supported by the extractor.

Workaround: use List/Seq (and case classes).

- Options are supported in models (in most cases), but not as API parameters.

Workaround: perform validation outside of the designated API method, pass non-optional values to the API
(and annotate with @ApiParam(required = true|false) when necessary).

- By default API parameters (all except path params) and model fields are considered optional.
This should be made configurable by the plugin to allow projects to minimize the amount of
`required = true|false` explicit annotations.

Workaround: use explicit annotations:
```scala
// for API parameters
@ApiParam(required = true)
// for model fields
@ApiModelProperty(required = true)
// for case class fields
@(ApiModelProperty @scala.annotation.meta.field)(required = true)
```

- Data type of API parameters and model fields is not extracted correctly for some combinations.

Workaround: use @ApiModelProperty(dataType = "...") to explicitly set it for the extractor.
@ApiModelProperty works and is picked up from annotated API params as well, not just model properties.

```scala
// "list", "set" and "array" all map to the same "array[T]" in the generated apidocs JSON
// List container ignored for API parameters (but not for model properties)
@ApiModelProperty(dataType = "list[CreatePostRequest]")
postRequests: List[CreatePostRequest]
```

```scala
// extracted as Object otherwise
@(ApiModelProperty @field)(dataType = "set[integer]")
errors: Option[Set[Int]] = None
@(ApiModelProperty @field)(dataType = "integer")
minAge: Option[Int] = None,
```

- Enum values need to be explicitly set (compiler enforces that annotation values are constants).

```scala
@(ApiModelProperty @field)(allowableValues = "Public,DirectConnections,DirectAndSecondaryConnections", dataType = "enum")
globalRestriction: Option[GlobalRestriction.Value] = None,
```

### Swagger API annotations quirks

(why we need to compensate with javax.ws.rs)

```
@ApiOperation :
  - does not allow to specify a (sub)path - would require one class per endpoint-path
  - javax.ws.rs.@Path does, can annotate methods

@ApiParam :
  - does not allow to specify the source of a parameter (header/path/query/form/cookie)
  - allows a few extra attributes to be specified:
    - required         (defaults to false)
    - dataType         useful when extractor didn't figure it out
    - allowMultiple    (defaults to false - except for path parameters)
    - allowableValues  useful for enum types (defaults to "")
    - description

@ApiModel :
 - marks explicitly a class as a model
   (though all models should be recursively discovered from API inputs/outputs)

@ApiModelProperty :
 - marks explicitly a method or field as a model property
 - 'hidden = true' useful for hiding implicits (e.g. JSON formatters)
   that are not part of the API and should be filtered out by the extractor
```

## Contributing

Submit a bug report and pull request in GitHub.

## Maintainers

* [Tatsuhiro Ujihisa](https://github.com/ujihisa) [@ujm](https://twitter.com/ujm)
* [Johnny Bufu](https://github.com/jbufu)
* [Diego Alvarez](https://github.com/d1egoaz) [@d1egoaz](https://twitter.com/d1egoaz)
* [Andres Rama](https://github.com/andresrama) [@andres_rama_hs](https://twitter.com/andres_rama_hs)
* [Steve Song](https://github.com/ssong-van) [@ssongvan](https://twitter.com/ssongvan)
