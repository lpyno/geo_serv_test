package cl.redd.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects.UserInfo
import spray.json.pimpAny

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AuthenticationController(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

  def login( realm:Option[String] , user:Option[String] , pass:Option[String] , device:Option[String] ) : UserInfo = {

    val rv:UserInfo =

      if( realm.nonEmpty && user.nonEmpty && pass.nonEmpty && device.nonEmpty ) {

        val resp = Try[UserInfo] {
          val future = authLogin( realm.get , user.get , pass.get , device.get )
          val saveRv = Await.result( future , Duration( 10 , "sec" ) )
          saveRv // resp = saveRv
        }

        val opResult:UserInfo = resp match {
          case Success( v ) => println( "Login OK!" ); v
          case Failure( e ) => println( "Login failed!... " , e.getMessage ); new UserInfo
        }

        opResult // rv = opResult

      } else {

        println( "Empty parameter!..." )
        new UserInfo //

      }

    rv

  }

  private def authLogin(  realm:String , user:String ,  pass:String ,  device:String ): Future[UserInfo] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.AUTH.toString() )

    val url = s"$serviceHost/auth/login"

    val entity = HttpEntity(MediaTypes.`application/json`, AuthLoginReq.toJson.toString())

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url , entity = entity ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[UserInfo]
    }

  }

}

/*
    // calls tastets-web
    public User loginUser(String realm, String userName, String pass, String device) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ReddDiscoveryClient.getNextIpByName(ServicesEnum.AUTH.toString()) +"/auth/login");
        builder.queryParam("realm", realm);
        builder.queryParam("user", userName);
        builder.queryParam("pass", pass);
        builder.queryParam("device", device);
        return restTemplate.getForObject(builder.build().encode().toUri(), User.class);
    }

    public Map login(String realm, String usernameDecode, String passDecode, String device) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ReddDiscoveryClient.getNextIpByName(ServicesEnum.AUTH.toString()) +"/auth/login");
        builder.queryParam("realm", realm);
        builder.queryParam("user", usernameDecode);
        builder.queryParam("pass", passDecode);
        builder.queryParam("device", device);
        return restTemplate.getForObject(builder.build().encode().toUri(), Map.class);
    }


 */