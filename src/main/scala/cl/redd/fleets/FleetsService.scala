package cl.redd.fleets

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.objects._
import cl.redd.objects.ReddJsonProtocol._
import io.swagger.annotations._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import spray.json._

@Api(value = "/fleets", produces = "application/json")
@Path("/")
class FleetsService( implicit val system:ActorSystem,
                     implicit val materializer: ActorMaterializer,
                     implicit val ec:ExecutionContext ) extends Directives {


  implicit val timeout = Timeout(5.seconds)

  val fleetsController:FleetsController = new FleetsController()

  val route = getByUser
              //save ~
              //getById ~
              //getByCompany ~
              //update ~
              //delete

  /** 2.1 "/save", POST method */
  @Api(value = "/fleets/getByUser", produces = "application/json")
  @Path("/fleets/getByUser")
  @ApiOperation(value = "getByUser", nickname = "getFleetsByUser", httpMethod = "POST", response = classOf[List[Fleet]])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "fleet",
        value = "Objeto Request",
        required = true,
        dataTypeClass = classOf[GetFleetsByUserId],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(

    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getByUser =
    pathPrefix("fleets") {
      path("getByUser") {
        post {
          entity(as[GetFleetsByUserId]) {
            request => val fleets:Future[List[Fleet]] = fleetsController.getFleetsByUserId( Some(request) )
              onComplete( fleets ) {
                case Success( v )   => complete{ println(s"getFleetsByUserId OK!...$v"); v.toJson }
                case Failure( err ) => complete{ print(s"getFleetsByUserId Failed!..."); println(err.getMessage); println(err.getLocalizedMessage );err.getMessage }
              }
            }
          }
        }
      }



  /*
  /fleets/getByUser'


[10:36]
withVehicles : true/false


[10:36]
withusers: true/false


[10:37]
'/fleets/getById'
   */

}

