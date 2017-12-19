package cl.redd.geofences

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects.RequestResponses._
import cl.redd.objects._
import io.swagger.annotations._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Api(value = "/geofences", produces = "application/json")
@Path("/")
class GeofenceApi( implicit val system:ActorSystem, implicit val materializer:ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  val geofences = new Geofences()

  val route = save ~
              getById ~
              getByCompany ~
              getFromCompanyByParameter ~
              getFromUserByParameter ~
              update ~
              deleteGeofence

  /** 3.1 "/save", POST method */
  @Api( value = "/save", produces = "application/json")
  @Path("geofences/save")
  @ApiOperation(value = "Crea Geocerca", nickname = "saveGeofence", httpMethod = "POST", response = classOf[Geofence])
  @ApiImplicitParams(
    Array(
       new ApiImplicitParam( name  = "geofence",
                            value = "nueva geocerca",
                            required = true,
                            dataTypeClass = classOf[GeofenceToSave],
                            paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def save =
    pathPrefix("geofences") {
      path("save") {
        post {
          entity(as[Geofence]) {
            val startTs = System.currentTimeMillis()
            request => val resp = geofences.save( request )
            onComplete( resp ) {
              case Success( resp ) => complete {
                println(s"Geofences save() completed!...")
                println(s"id     :     [${resp.id}]")
                println(s"elapsed:     [${System.currentTimeMillis() - startTs} ms]")
                ToResponseMarshallable( resp )
              }
              case Failure( err ) => complete {
                println( s"Save Failed... $err" )
                ToResponseMarshallable( err )
              }
            }
          }
        }
      }
    }

  /** 3.2 "/getById", GET method */
  @Api(value = "/getById", produces = "application/json")
  @Path("geofences/getById")
  @ApiOperation(value = "Solicita geocercas por ID", nickname = "getById", httpMethod = "GET", response = classOf[Geofence])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name = "realm",
                            value = "dominio a consultar",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "query" ),
      new ApiImplicitParam( name = "id",
                            value = "id geocerca a buscar",
                            required = true,
                            dataTypeClass = classOf[Int],
                            paramType = "query" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getById =
    pathPrefix("geofences") {
      path("getById") {
        get {
          parameters( 'realm.as[Option[String]] , 'id.as[Option[Long]] ) {
            ( realm , id ) => val geofence: Future[Geofence] = geofences.getById( realm, id )
            onComplete(geofence) {
              case Success(geofence) => complete {
                println("getById OK!")
                ToResponseMarshallable( geofence )
              }
              case Failure(err) => complete {
                println(s"getById failed... ${err.getMessage}")
                ToResponseMarshallable( err )
              }
            }
          }
        }
      }
    }
  /** 3.3 "/getByCompany", POST method */
  @Api(value = "/getByCompany", produces = "application/json")
  @Path("/geofences/getByCompany")
  @ApiOperation(value = "Solicita geocercas por ID de compañia", nickname = "getByCompanyId", httpMethod = "POST", response = classOf[List[GeofenceOld]])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name = "Request",
                            value = "Formato del request",
                            required = true,
                            dataTypeClass = classOf[GetByCompanyReq],
                            paramType = "body" )
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def getByCompany =
    pathPrefix("geofences"){
      path("getByCompany") {
        post {
          entity(as[String]) {
            val startTs = System.currentTimeMillis()
            request =>
            val constructedReq:Option[GetByCompanyReq] = geofences.parseRequestParams( request )
            if( constructedReq.isDefined ){
              //println( s"constructed request: $constructedReq" )
              val rv: Future[List[Geofence]] = geofences.getGeofencesByCompanyId( constructedReq.get.realm, constructedReq.get.companyId, constructedReq.get.fps )
              onComplete( rv ) {
               case Success(geofences) => complete {
                 println(s"Geofences getByCompanyId() completed!...")
                 println(s"listSize:    [${geofences.size}]")
                 println(s"elapsed:     [${System.currentTimeMillis() - startTs} ms]")
                  ToResponseMarshallable( geofences )
                }
                case Failure(err) => complete {
                  println( s"Geofences by company failed!...${err.getMessage}" )
                  ToResponseMarshallable( err.getMessage )
                }
              }
            } else {
              complete( ToResponseMarshallable("RequestParam Undefined...") )
            }
          }
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

  /** 3.7 "/delete", DELETE method */
  //delete( realm:String , geofenceId:Int ) : Geofence = ???
  @Api(value = "/delete", produces = "application/json")
  @Path("geofences/delete")
  @ApiOperation(value = "Borra geocerca", nickname = "deleteGeofence", httpMethod = "DELETE", response = classOf[(String,Long)])
  @ApiImplicitParams(Array(
    new ApiImplicitParam( name = "id",
      value = "id", required = true,
      dataTypeClass = classOf[Long], paramType = "query" )
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def deleteGeofence =
    pathPrefix( "geofences" ) {
      path("delete") {
        akka.http.scaladsl.server.Directives.delete {
          println( s"route found..." )
          parameters ( "id".as[Option[Long]] ) {
            println( s"in delete..." )
            id => val rv = geofences.delete( id.get )
            onComplete( rv ) {
              case Success( rv  ) => complete{
                println( s"Delete OK! id: ${rv}" )
                ToResponseMarshallable( rv )
              }
              case Failure( err ) => complete{
                println( s"Delete Failed... ${err.getMessage}" )
                ToResponseMarshallable( err )
              }
            }
          }
        }
      }
    }

}



