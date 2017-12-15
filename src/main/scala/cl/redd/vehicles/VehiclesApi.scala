package cl.redd.vehicles

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects.RequestResponses.GetVehiclesByUserId
import cl.redd.objects._
import io.swagger.annotations._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}


@Api(value = "/vehicles", produces = "application/json")
@Path("/")
class VehiclesApi(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)

  val vehicles:Vehicles = new Vehicles()

  val route = save ~
              getById ~
              getByImei ~
              getByUserId

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
              vehicles.save(Some(vehicle))
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
              vehicles.getVehiclesById(vehicle.realm, vehicle.vehicleIds, vehicle.withLastState, vehicle.fps)
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
              val futureList = vehicles.getVehiclesByImei( Some( params ) )
              onComplete( futureList ) {
                case Success( v )   => complete{ v }
                case Failure( err ) => complete{ err.getMessage }
              }
          }
        }
      }
    }

  /** 4.3 "/getByCompany", GET method */
  @Api(value = "/vehicles/getByCompanyId", produces = "application/json")
  @Path("/vehicles/getByCompanyId")
  @ApiOperation(value = "Obtiene vehículos de una compañía", nickname = "getVehiclesByCompanyId", httpMethod = "GET", response = classOf[List[VehicleOld]])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "realm",
        value = "dominio a consultar",
        dataTypeClass = classOf[String],
        paramType = "query" ),
      new ApiImplicitParam( name  = "companyId",
        value = "id de compañia",
        dataTypeClass = classOf[Int],
        paramType = "query" ),
      new ApiImplicitParam( name  = "withLastState",
        value = "con información de último estado",
        dataTypeClass = classOf[Boolean],
        paramType = "query" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getByCompanyId =
    pathPrefix("vehicles") {
      path("getByCompanyId") {
        complete ( ToResponseMarshallable( "getByCompany Unimplemented!" ) )
      }
    }

  /** 4.4 "/getByUser", POST method */
  @Api(value = "/vehicles/getByUserId", produces = "application/json")
  @Path("/vehicles/getByUserId")
  @ApiOperation(value = "Obtiene vehículos de una compañía", nickname = "getVehiclesByUserId", httpMethod = "POST", response = classOf[List[Vehicle]])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "objeto request",
        value = "información del request",
        dataTypeClass = classOf[GetVehiclesByUserId],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getByUserId =
    pathPrefix("vehicles"){
      path("getByUserId"){
        post{
          entity(as[String]){
            request => //println( s"entity as str: $request" )
              val params = vehicles.parseGetByUserParams( request )
              //println( s"after parse: $params" )
              if( params.isDefined ){
                val vehiclesList: Future[List[Vehicle]] = vehicles.getByUserId( params.get.realm, params.get.companyId, params.get.withLastState, params.get.fps )
                //val vehiclesList = Future( List( Vehicle() ) )
                onComplete(vehiclesList) {
                  case Success(vehiclesList) => complete {
                    println(s"Vehicles getByUserId OK!... userProfile:${params.get.fps.filterParams.get( "userProfile" ).get},listSize[${vehiclesList.size}]")
                    ToResponseMarshallable( vehiclesList )
                  }
                  case Failure(err) => complete {
                    println(s"getByUserId failed... ${err.getMessage}")
                    ToResponseMarshallable( err )
                  }
                }
              } else {
                complete( ToResponseMarshallable("Vehicles request param undefined...") )
              }
          }
        }
      }
    }
}



