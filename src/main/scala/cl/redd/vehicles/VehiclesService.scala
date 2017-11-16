package cl.redd.vehicles

import javax.ws.rs.Path

import akka.actor.ActorSystem
import scala.util.{Success,Failure}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects._
import io.swagger.annotations._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Try}


@Api(value = "/vehicles", produces = "application/json")
@Path("/")
class VehiclesService(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)

  val vehController:VehiclesController = new VehiclesController()

  val route = save ~
              getById ~
              getByImei

  /** 4.1 "/save", POST method */
  @Api(value = "/vehicles/save", produces = "application/json")
  @Path("/vehicles/save")
  @ApiOperation(value = "Actualiza vehiculo", nickname = "updateVehicle", httpMethod = "PUT", response = classOf[Vehicle])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "vehicle",
        value = "Objeto Vehiculo",
        required = true,
        dataTypeClass = classOf[Vehicle],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def save =
    pathPrefix("vehicles") {
      path("save") {
        put {
          entity(as[Vehicle]) {
            vehicle => complete {
              vehController.save(Some(vehicle))
            }
          }
        }
      }
    }

  /** 4.2 "/getById", GET method */
  @Api(value = "/vehicles/getById", produces = "application/json")
  @Path("/vehicles/getById")
  @ApiOperation(value = "Obtiene vehículos (uno o mas) por Id", nickname = "getVehiclesById", httpMethod = "POST", response = classOf[List[Vehicle]])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "request",
        value = "Información del request",
        dataTypeClass = classOf[GetVehiclesById],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getById =
    pathPrefix("vehicles") {
      path("getById") {
        post {
          println( "route vehicles/getById matched..." )
          entity(as[GetVehiclesById]) {
            vehicle => complete {
              vehController.getVehiclesById(vehicle.realm, vehicle.vehicleIds, vehicle.withLastState, vehicle.fps)
            }
          }
        }
      }
    }

  /** 4.2 "/getById", GET method */
  @Api(value = "/vehicles/getByImei", produces = "application/json")
  @Path("/vehicles/getByImei")
  @ApiOperation(value = "Obtiene vehículos (uno o mas) por Imei", nickname = "getVehiclesByImei", httpMethod = "POST", response = classOf[List[VehicleOld]])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "lista con números de Imei",
        value = "Información del request",
        dataTypeClass = classOf[GetListByMids],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getByImei =
    pathPrefix("vehicles") {
      path("getByImei") {
        post {
          entity(as[GetListByMids]) {
            params => println( s"route vehicles/getByIMEI matched...$params" )
              val futureList = vehController.getVehiclesByImei( Some( params ) )
              onComplete( futureList ) {
                case Success( v )   => complete{ v }
                case Failure( err ) => complete{ err.getMessage }
              }
          }
        }
      }
    }
}



