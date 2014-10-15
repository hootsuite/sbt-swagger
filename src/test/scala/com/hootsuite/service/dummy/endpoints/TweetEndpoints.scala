package com.hootsuite.service.dummy.endpoints

import javax.ws.rs._

import com.wordnik.swagger.annotations._

import scala.concurrent.Future

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
