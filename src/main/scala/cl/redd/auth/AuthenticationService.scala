package cl.redd.auth

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.geofences.GeofenceActor._
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects._
import io.swagger.annotations._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


@Api(value = "/authentication", produces = "application/json")
@Path("/")
class AuthenticationService(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)

  val authController:AuthenticationController = new AuthenticationController()

  val route = login ~
              validateOp

  /** 1.1 "/login", GET method */
  @Api(value = "/login", produces = "application/json")
  @Path("/login")
  @ApiOperation(value = "Valida credenciales de usuario", nickname = "loginAuth", httpMethod = "GET", response = classOf[UserInfo])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam( name  = "realm",
                            value = "Dominio a consultar",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "query" ),
      new ApiImplicitParam( name  = "user",
                            value = "Nombre de usuario",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "query" ),
      new ApiImplicitParam( name  = "password",
                            value = "Contraseña de usuario",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "query" ),
      new ApiImplicitParam( name  = "device",
                            value = "Plataforma",
                            required = true,
                            dataTypeClass = classOf[String],
                            paramType = "query" )
          )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def login =
    path("login") {
      get {
        parameters( 'realm.as[Option[String]] , 'user.as[Option[String]] , 'password.as[Option[String]] , 'device.as[Option[String]] ) { ( realm , user , password , device ) =>
          complete {
            authController.login( realm, user , password , device )
          }
        }
      }
    }

  /** 1.2 "/validate", GET method */
  @Api(value = "/validate", produces = "application/json")
  @Path("/validate")
  @ApiOperation(value = "Valida acciones de usuario", nickname = "authValidate", httpMethod = "GET", response = classOf[UserInfo])
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
  def validateOp =
    path("validate") {
      post {
        entity(as[GetByIdReq]) { request =>
          complete { "getById method" }
        }
      }
    }
}



