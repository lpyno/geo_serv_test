package cl.redd.fleets

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects._
import io.swagger.annotations._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Api(value = "/fleets", produces = "application/json")
@Path("/")
class FleetsApi( implicit val system:ActorSystem,
                     implicit val materializer: ActorMaterializer,
                     implicit val ec:ExecutionContext ) extends Directives {

  val fleets:Fleets = new Fleets()

  val route =
    getByUser ~
    //save ~
    //getById ~
    //getByCompany ~
    update
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
          entity(as[String]) {
            request =>
              val startTs = System.currentTimeMillis()
              val reqObj:Option[GetFleetsByUserId] = fleets.getReqParamsFleetsByUser( request )
              if ( reqObj.isDefined ) {
                val fleetList:Future[List[Fleet]] =
                  fleets.getFleetsByUserId( reqObj.get.realm, reqObj.get.userId, reqObj.get.userProfile,
                    reqObj.get.companyId, reqObj.get.withVehicles, reqObj.get.withLastState, reqObj.get.fps)
                onComplete( fleetList ) {
                  case Success( fleets ) => complete {
                    println(s"Fleets getByUserId() completed!...")
                    println(s"userProfile: [${reqObj.get.userProfile}]")
                    println(s"w/Vehicles : [${reqObj.get.withVehicles}]")
                    println(s"w/LastState: [${reqObj.get.withLastState}]")
                    println(s"listSize   : [${fleets.size}]")
                    println(s"elapsed    : [${System.currentTimeMillis() - startTs} ms]")
                    ToResponseMarshallable( fleets )
                  }
                  case Failure( err ) => complete {
                    print(s"getFleetsByUserId Failed!...${err.getMessage}")
                    ToResponseMarshallable( err )
                  }
                }
              } else {
                complete( ToResponseMarshallable("RequestParam Undefined...") )
              }
            }
          }
        }
      }

  /** 2.5 "/update", PUT method */
  @Api(value = "/fleets/update", produces = "application/json")
  @Path("/fleets/update")
  @ApiOperation(value = "Actualiza un objeto flota", nickname = "updateFleet", httpMethod = "PUT", response = classOf[FleetToUpdate])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "fleet",
        value = "flota actualizada",
        required = true,
        dataTypeClass = classOf[FleetToUpdate],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def update =
    pathPrefix( "fleets" ){
      path("update") {
        put {
          entity(as[String]) {
            request =>
              val startTs = System.currentTimeMillis()
              val fleetToUpdate = fleets.parseFleetToUpdate( request )
              //println( fleetToUpdate.toJson.toString )
              if ( fleetToUpdate.isDefined ){
                val eval = fleets.update( fleetToUpdate.get )
                onComplete( eval ){
                  case Success( fleet ) => complete{
                    println(s"Fleets update '${fleet.id}' completed!...")
                    println(s"elapsed: [${System.currentTimeMillis() - startTs} ms]")
                    ToResponseMarshallable( fleet )
                  }
                  case Failure( error ) => complete{
                    println(s"Fleets update '${fleetToUpdate.get.id}' failed... ${error.getMessage}")
                    ToResponseMarshallable( error )
                  }
                }
              } else{
                complete( ToResponseMarshallable("Fleets request param undefined...") )
              }
          }
        }
      }
    }

}

