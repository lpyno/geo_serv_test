package cl.redd.auth

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.objects._
import cl.redd.objects.ReddJsonProtocol._
import io.swagger.annotations._
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Api(value = "/authentication", produces = "application/json")
@Path("/")
class AuthApi(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)

  val auth:Auth = new Auth()

  val route = login

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
          val startTs = System.currentTimeMillis()
          println( s"running 'login()'... [$startTs]" )
          val futureUserInfo = auth.login( realm, user , password , device)
          onComplete( futureUserInfo ){
            case Success( userInfo ) => complete {
              println(s"Auth login() completed!...")
              println(s"isAdmin: [${userInfo.isAdmin.get}]")
              println(s"elapsed: [${System.currentTimeMillis() - startTs} ms]")
              ToResponseMarshallable( userInfo.toJson )
            }
            case Failure( error ) => complete {
              println( s"login failed!... ${error.printStackTrace()}" )
              ToResponseMarshallable( error.getMessage )
            }
          }
        }
      }
    }

}



