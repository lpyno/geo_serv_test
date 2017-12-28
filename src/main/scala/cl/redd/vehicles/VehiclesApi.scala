package cl.redd.vehicles

import javax.ws.rs.Path

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.Timeout
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects.RequestResponses.GetVehiclesByUserId
import cl.redd.objects._
import io.swagger.annotations._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Api(value = "/vehicles", produces = "application/json")
@Path("/")
class VehiclesApi(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)

  val vehicles:Vehicles = new Vehicles()

  val route = getById ~
              getByImei ~
              getByUserId ~
              getByCompanyId ~
              update ~
              websocketRoute

/*
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
              "asdf"//vehicles.save(Some(vehicle))
            }
          }
        }
      }
    }
*/

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

  /** 4.3 "/getByCompany", POST method */
  @Api(value = "/vehicles/getByCompanyId", produces = "application/json")
  @Path("/vehicles/getByCompanyId")
  @ApiOperation(value = "Obtiene vehículos de una compañía", nickname = "getVehiclesByCompanyId", httpMethod = "POST", response = classOf[List[VehicleFromGetAllByCompany]])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "objeto request",
        value = "información del request",
        dataTypeClass = classOf[GetVehiclesByCompanyId],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getByCompanyId =
    pathPrefix("vehicles") {
      path("getByCompanyId") {
        post{
          entity(as[String]){
            val startTs = System.currentTimeMillis()
            request => val params = vehicles.parseGetByCompanyParams( request )
            if ( params.isDefined ){
              val vehicleList = vehicles.getByCompanyId( params.get.realm, params.get.companyId, params.get.withLastState, params.get.fps )
              onComplete( vehicleList ) {
                case Success( vehicleList ) => complete {
                  println(s"Vehicles getByCompanyId() '${params.get.companyId}' completed!...")
                  println(s"listSize:    [${vehicleList.size}]")
                  println(s"elapsed:     [${System.currentTimeMillis() - startTs} ms]")
                  ToResponseMarshallable( vehicleList )
                }
                case Failure( error ) => complete{
                  println(s"Vehicles getByCompanyId failed... ${error.getMessage}")
                  ToResponseMarshallable( error )
                }
              }
            } else {
              complete( ToResponseMarshallable("Vehicles request param undefined...") )
            }
          }
        }
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
            val startTs = System.currentTimeMillis()
            request => val params = vehicles.parseGetByUserParams( request )
              if( params.isDefined ){
                val vehiclesList: Future[List[Vehicle]] = vehicles.getByUserId( params.get.realm, params.get.companyId, params.get.withLastState, params.get.fps )
                onComplete(vehiclesList) {
                  case Success(vehiclesList) => complete {
                    println(s"Vehicles getByUserId() '${params.get.fps.filterParams.get( "userId" ).get}' completed!...")
                    println(s"company ID...[${params.get.companyId}]")
                    println(s"userProfile..[${params.get.fps.filterParams.get( "userProfile" ).get}]")
                    println(s"listSize.....[${vehiclesList.size}]")
                    println(s"elapsed......[${System.currentTimeMillis() - startTs} ms]")
                    ToResponseMarshallable( vehiclesList )
                  }
                  case Failure(err) => complete {
                    println(s"Vehicles getByUserId failed... ${err.getMessage}")
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
  /** 4.7 "/update", PUT method */
  @Api(value = "/vehicles/update", produces = "application/json")
  @Path("/vehicles/update")
  @ApiOperation(value = "Actualiza vehículo", nickname = "updateVehicle", httpMethod = "PUT", response = classOf[VehicleToUpdate])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "objeto para actualización",
        value = "información parámetros a actualizar",
        dataTypeClass = classOf[VehicleToUpdate],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def update = pathPrefix( "vehicles" ){
    path( "update" ){
      put{
        entity(as[String]) {
          implicit val fmt = jsonFormat13(VehicleToUpdate)
          val startTs = System.currentTimeMillis()
          request =>
          val vehicleToUpdate = vehicles.validateUpdateParams( request )
          if ( vehicleToUpdate.isDefined ){
            val rv = vehicles.update( vehicleToUpdate.get )
            onComplete( rv ) {
              case Success( v ) => complete {
                println(s"Vehicles update completed!...")
                println(s"elapsed: [${System.currentTimeMillis() - startTs} ms]")
                ToResponseMarshallable( v )
              }
              case Failure( e ) => complete {
                println( s"Vehicles update failed!...${e.getMessage}" )
                ToResponseMarshallable( e.getMessage )
              }
            }
          } else {
            complete( ToResponseMarshallable("RequestParam Undefined...") )
          }
        }
      }
    }
  }



  def websocketRoute =
    pathPrefix("vehicles"){
      path("ws") {
        handleWebSocketMessages(wsHandler)
      }
    }
  // websocket test

  def wsHandler: Flow[Message, Message, Any] =
    Flow[Message].map {
      case tm : TextMessage => {
        TextMessage( Source.single("rs echo input:") ++ tm.textStream )
      }
      case bm : BinaryMessage =>
        TextMessage( "Message unsupported" )
    }

  def getVehicles( realm:String, companyId:Int, ids:List[Int] ): Future[List[Vehicle]] = {

    // {"filter":{"userProfile":"ADMIN","userId":925,"filter":[{"id":[1,2,3]}]},"sort":{"field":"name", "order":1},"paginated":{"limit":10,"offset":0}}
    val filterParams = Map( "id" -> ids.toString.replace("List(","").replace(")","").replace(" ","") )
    println( s"filterParams: $filterParams" )
    val fps:FilterPaginateSort = FilterPaginateSort( filterParams, 0, 0, 0, "name" )
    //val vehicleList = vehicles.getByCompanyId( realm, companyId, true, fps )
    val vehicleList = vehicles.getByCompanyId( realm, companyId, true, fps )
    vehicleList
    //Future( s"Ids to stream: $ids" )
  }

  def parseMessage: Flow[Message,StreamRequest,NotUsed] = ???



}



