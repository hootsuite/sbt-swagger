package com.hootsuite.sbt.swagger

import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.config.SwaggerConfig
import com.wordnik.swagger.converter.{ModelConverters, SwaggerSchemaConverter}
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader
import com.wordnik.swagger.model.{ApiListingReference, ResourceListing, SwaggerSerializers}
import org.json4s.jackson.Serialization

/**
 * Extractor of Swagger & JAX-RS (jsr311) annotations from compiled classes
 * into Swagger API models to be serialized into JSON and fed into swagger-ui for further consumption.
 *
 * Swagger's @Api, @ApiOperations and @ApiResponse(s) are the main markers
 * and can be complemented with additional semantics with javax.ws.rs.* annotations.
 *
 * Neither Swagger nor JAX-RS alone offer a complete set of functionality.
 *
 * This extractor favors Swagger's @Api* annotations over javax.ws.rs.* ones where there is an overlap:
 * if a piece of information is specified using both Swagger @Api* and javax.ws.rs.* annotation,
 * the generated models will use the information from Swagger's annotation.
 *
 * Notes:
 *
 * '@ApiOperation'
 * - does not allow to specify a (sub)path - would require one class per endpoint-path
 * -- javax.ws.rs.@Path does, can annotate methods
 *
 * '@ApiParam'
 * - does not allow to specify the source of a parameter (header/path/query/form/cookie)
 * -- javax.ws.rs.@PathParam et al. do
 * - allows a few extra attributes to be specified:
 *     required         (defaults to false)
 *     allowMultiple    (defaults to false - except for path parameters)
 *     allowableValues  (defaults to false)
 *     description
 *
 * '@ApiModel'
 * - marks explicitly a class as a model (though all models should be recursively discovered from API inputs/outputs)
 *
 * '@ApiModelProperty'
 * - marks explicitly a method or field as a model property
 * - 'hidden = true' useful for hiding implicits (e.g. JSON formatters) that are not part of the API and should be filtered out by the extractor
 *
 *  JAX-RS javax.ws.rs.* suggested annotations:
 *  - @Path
 *  - @Consumes
 *  - @Produces
 *  - @PathParam
 *  - @HeaderParam
 *  - @QueryParam
 *  - @FormParam
 *  - @CookieParam
 *  - @DefaultValue
 */
class SwaggerJaxrsApiReader(val config: SwaggerConfig, val excludePropertyClassPrefixes: Set[String] = Set()) extends DefaultJaxrsApiReader {

  implicit val formats = SwaggerSerializers.formats

  private val customSwaggerSchemaConverter = new SwaggerSchemaConverter {
    override def read(cls: Class[_], typeMap: Map[String, String]) = super.read(cls, typeMap).map {
      case model => model.copy(properties = model.properties.filterNot {
        case (propName, prop) => excludePropertyClassPrefixes.exists(prop.qualifiedType.startsWith)
      })
    }
  }

  ModelConverters.addConverter(customSwaggerSchemaConverter, first = true)

  def apiDocs(klasses: List[Class[_]]): String = {
    val apis: List[ApiListingReference] = klasses.flatMap(_.getAnnotations.collectFirst {
      case classAnn: Api => ApiListingReference(path = s"${classAnn.value}.{format}", description = Some(classAnn.description))
    })

    Serialization.writePretty(
      ResourceListing(config.apiVersion, config.swaggerVersion, apis, authorizations = Nil, info = None)
    )
  }

  def endpoints(klasses: Seq[Class[_]]): Map[String,String] = {
    (for {
      endpointHolder <- klasses
      endpointPathName <- endpointHolder.getAnnotations.collectFirst { case classAnn: Api => classAnn.value }
      apiListing = read("/", endpointHolder, config).getOrElse("extract error") // todo: better error reporting?
    } yield {
      endpointPathName -> Serialization.writePretty(apiListing)
    }).toMap
  }
}
