package cl.redd.geofences

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.geofences.GeofenceActor._
import cl.redd.objects._
import io.swagger.annotations._
import cl.redd.objects.ReddJsonProtocol._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


@Api(value = "/geofences", produces = "application/json")
@Path("/")
class GeofenceService( implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)

  val geofenceController:GeofenceController = new GeofenceController()

  val route = save ~
              getById ~
              getByCompany ~
              getFromCompanyByParameter ~
              getFromUserByParameter ~
              update //~
              //delete

  /** 3.1 "/save", POST method */
  @Api( value = "/save", produces = "application/json")
  @Path("/save")
  @ApiOperation(value = "Crea Geocerca", nickname = "saveGeofence", httpMethod = "POST", response = classOf[Geofence])
  @ApiImplicitParams(
    Array(
      /*new ApiImplicitParam( name  = "realm",
                            value = "dominio donde se creará la nueva geocerca",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "body" ),*/

      new ApiImplicitParam( name  = "geofence",
                            value = "nueva geocerca",
                            required = true,
                            dataTypeClass = classOf[Geofence],
                            paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def save =
    path("save") {
      post {
        entity(as[Geofence]) {
          println( "testing debug messages..." )
          request => complete { geofenceController.save( Some( request ) ) }

        }
      }
    }

  /** 3.2 "/getById", GET method */
  @Api(value = "/getById", produces = "application/json")
  @Path("/getById")
  @ApiOperation(value = "Solicita geocercas por ID", nickname = "getById", httpMethod = "GET", response = classOf[GetByIdResp])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name = "realm",
                            value = "dominio a consultar",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "query" ),
      new ApiImplicitParam( name = "geofenceIds",
                            value = "lista de identificadores requeridos ( >= 1 ) ",
                            required = true,
                            dataTypeClass = classOf[List[Int]],
                            paramType = "query" ),
      new ApiImplicitParam( name = "fps",
                            value = "información para filtro, paginado y ordenamiento",
                            required = true,
                            dataTypeClass = classOf[FilterPaginateSort],
                            paramType = "query" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getById =
    path("getById") {
      post {
        entity(as[GetByIdReq]) { request =>
          complete { "getById method" }
        }
      }
    }

  /** 3.3 "/getByCompany", GET method */
  @Api(value = "/getByCompany", produces = "application/json")
  @Path("/getByCompany")
  @ApiOperation(value = "Solicita geocercas por ID de compañia", nickname = "getByCompanyId", httpMethod = "POST", response = classOf[GetByCompanyResp])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name = "realm",
                            value = "dominio a consultar",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "body" ),
      new ApiImplicitParam( name = "companyId",
                            value = "identificador compañia",
                            required = true,
                            dataTypeClass = classOf[Int],
                            paramType = "body" ),
      new ApiImplicitParam( name = "fps",
                            value = "información para filtro, paginado y ordenamiento",
                            required = true,
                            dataTypeClass = classOf[FilterPaginateSort],
                            paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getByCompany =
  path("getByCompany") {
    post {
      entity(as[GetByCompanyReq]) { request =>
        complete { "getByCompany method" }
      }
    }
  }

  /** 3.4 "/getFromCompanyByParameter" ( * "getByCompany" w/ Filter? ) */
  // getCompanyGeofencesByParameter(realm:String , companyId:Int , paramName:ParamEnum , paramValue:Object , fps:FilterPaginateSort ) : List[Geofence] = ???
  @Api(value = "/getFromCompanyByParameter", produces = "application/json")
  @Path("/getFromCompanyByParameter")
  @ApiOperation(value = "Solicita las geocercas de una compañia filtradas por parámetro -> valor", nickname = "getCompanyGeofencesByParameter", httpMethod = "POST", response = classOf[GetFromCompanyByParamResp])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name = "realm",
                            value = "dominio a consultar",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "body" ),
      new ApiImplicitParam( name = "companyId",
                            value = "identificador compañia",
                            required = true,
                            dataTypeClass = classOf[Int],
                            paramType = "body" ),
      new ApiImplicitParam( name = "paramName",
                            value = "nombre del parámetro a filtrar",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "body" ),
      new ApiImplicitParam( name = "paramValue",
                            value = "valor del parámetro a filtrar",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "body" ),
      new ApiImplicitParam( name = "fps",
                            value = "información para filtro, paginado y ordenamiento",
                            required = true,
                            dataTypeClass = classOf[FilterPaginateSort],
                            paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getFromCompanyByParameter =
  path("getFromCompanyByParameter") {
    post {
      entity(as[GetFromCompanyByParamReq]) { request =>
        complete { "getFromCompanyByParameter method" }
      }
    }
  }

  /** 3.5 "/getFromUserByParameter" ( * "getByUser" w/ Filter? ) */
  // getUserGeofencesByParameter( realm:String , userId:Int , paramName:ParamEnum , paramValue:Object , fps:FilterPaginateSort ) : List[Geofence] = ???
  @Api(value = "/getFromUserByParameter", produces = "application/json")
  @Path("/getFromUserByParameter")
  @ApiOperation(value = "Solicita las geocercas de un usuario filtradas por parámetro -> valor", nickname = "getUserGeofencesByParameter", httpMethod = "POST", response = classOf[GetFromUserByParamResp])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name = "realm",
        value = "dominio a consultar",
        required = true,
        dataTypeClass = classOf[String],
        paramType = "body" ),
      new ApiImplicitParam( name = "userId",
        value = "identificador usuario",
        required = true,
        dataTypeClass = classOf[Int],
        paramType = "body" ),
      new ApiImplicitParam( name = "paramName",
        value = "nombre del parámetro a filtrar",
        required = true,
        dataTypeClass = classOf[String],
        paramType = "body" ),
      new ApiImplicitParam( name = "paramValue",
        value = "valor del parámetro a filtrar",
        required = true,
        dataTypeClass = classOf[String],
        paramType = "body" ),
      new ApiImplicitParam( name = "fps",
        value = "información para filtro, paginado y ordenamiento",
        required = true,
        dataTypeClass = classOf[FilterPaginateSort],
        paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getFromUserByParameter =
  path("getFromUserByParameter") {
    post {
      entity(as[GetFromUserByParamReq]) { request =>
        complete { "getFromUserByParameter method" }
      }
    }
  }

  /** 3.6 "/update", PUT method */
  //update( realm:String , updatedGeofence:Geofence ) = ???
  @Api(value = "/update", produces = "application/json")
  @Path("/update")
  @ApiOperation(value = "Actualiza un objeto geocerca", nickname = "updateGeofence", httpMethod = "PUT", response = classOf[UpdateResp])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "realm",
                            value = "dominio donde se creará la nueva geocerca",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "body" ),

      new ApiImplicitParam( name  = "geofence",
                            value = "nueva geocerca",
                            required = true,
                            dataTypeClass = classOf[Geofence],
                            paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def update =
  path("update") {
    put {
      entity(as[UpdateReq]) { request =>
        complete { "update method" }
      }
    }
  }

/*  /** 3.7 "/delete", DELETE method */
  //delete( realm:String , geofenceId:Int ) : Geofence = ???
  @Api(value = "/delete", produces = "application/json")
  @Path("/delete")
  @ApiOperation(value = "Borra objeto geocerca", nickname = "deleteGeofence", httpMethod = "DELETE", response = classOf[DeleteResp])
  @ApiImplicitParams(Array(
    new ApiImplicitParam( name = "body",
      value = "realm", required = true,
      dataTypeClass = classOf[DeleteReq], paramType = "body" )
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def delete =
  path("delete") {
 /*        entity(as[DeleteReq]) { request =>
        complete { (geofenceActor ? request).mapTo[DeleteResp] }
      }*/

  }*/



}



