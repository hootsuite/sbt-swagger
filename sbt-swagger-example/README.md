## How to try

```
sbt swagger
```

This will generate the following 2 files, which can be rendered with [swagger-ui](http://swagger.io/swagger-ui/).


* ./target/swagger/api-docs.json
* ./target/swagger/users.json

Along with the output from the sbt process

    > swagger
    [info] == sbt-swagger ==> looking for compiled project classes in: file:/Users/uji/git/sbt-swagger/sbt-swagger-example/target/scala-2.10/classes/
    SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
    SLF4J: Defaulting to no-operation (NOP) logger implementation
    SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
    [info] == sbt-swagger ==> loaded 10 project classes
    [info] == sbt-swagger ==> /Users/uji/git/sbt-swagger/sbt-swagger-example/target/swagger/api-docs.json:
    [info] {
    [info]   "apiVersion" : "1.0",
    [info]   "swaggerVersion" : "2.0",
    [info]   "apis" : [ {
    [info]     "path" : "/users.{format}",
    [info]     "description" : "CRUD (so far just R) for our valuable awesome friendly users"
    [info]   }, {
    [info]     "path" : "/users.{format}",
    [info]     "description" : "CRUD (so far just R) for our valuable awesome friendly users"
    [info]   } ]
    [info] }
    [info] == sbt-swagger ==> /users -> {
    [info]   "apiVersion" : "1.0",
    [info]   "swaggerVersion" : "2.0",
    [info]   "basePath" : "http://localhost",
    [info]   "resourcePath" : "/users",
    [info]   "produces" : [ "application/json" ],
    [info]   "apis" : [ {
    [info]     "path" : "/users/",
    [info]     "operations" : [ {
    [info]       "method" : "GET",
    [info]       "summary" : "Get the key with the supplied key ID.",
    [info]       "notes" : "",
    [info]       "type" : "User",
    [info]       "nickname" : "getByEmail",
    [info]       "produces" : [ "application/json" ],
    [info]       "parameters" : [ {
    [info]         "name" : "body",
    [info]         "required" : false,
    [info]         "type" : "string",
    [info]         "paramType" : "body",
    [info]         "allowMultiple" : false
    [info]       } ],
    [info]       "responseMessages" : [ {
    [info]         "code" : 200,
    [info]         "message" : "Success. Body contains key and creator information.",
    [info]         "responseModel" : "User"
    [info]       }, {
    [info]         "code" : 400,
    [info]         "message" : "Bad Request. Errors specify: (snip)",
    [info]         "responseModel" : "BadRequest"
    [info]       }, {
    [info]         "code" : 404,
    [info]         "message" : "Not Found.",
    [info]         "responseModel" : "NotFound"
    [info]       } ]
    [info]     } ]
    [info]   } ],
    [info]   "models" : {
    [info]     "NotFound" : {
    [info]       "id" : "NotFound",
    [info]       "properties" : {
    [info]         "msg" : {
    [info]           "type" : "string"
    [info]         }
    [info]       }
    [info]     },
    [info]     "User" : {
    [info]       "id" : "User",
    [info]       "properties" : {
    [info]         "email" : {
    [info]           "type" : "string"
    [info]         }
    [info]       }
    [info]     },
    [info]     "BadRequest" : {
    [info]       "id" : "BadRequest",
    [info]       "properties" : {
    [info]         "msg" : {
    [info]           "type" : "string"
    [info]         }
    [info]       }
    [info]     }
    [info]   }
    [info] }
    [success] Total time: 1 s, completed 11-Dec-2015 10:22:34 AM
