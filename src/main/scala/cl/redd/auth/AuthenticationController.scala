package cl.redd.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects.{UserInfo, UserOld, UserPrefOld, ValidateOld}
import cl.tastets.life.objects.ServicesEnum

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AuthenticationController( implicit val actor:ActorSystem, implicit val materializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

  def login(realm:Option[String] = None, user:Option[String] = None, pass:Option[String] = None, device:Option[String] = None ) : UserInfo = {

    println( "in 'login' ..." )

    val rv:UserInfo =

      if( realm.nonEmpty && user.nonEmpty && pass.nonEmpty && device.nonEmpty ) {

        val rvLogin = Try[UserOld] {
          val future = authLogin( realm.get , user.get , pass.get , device.get )
          val rv = Await.result( future , Duration( 10 , "sec" ) )
          rv // rvLogin = saveRv
        }

        val userOld:UserOld = rvLogin match {
          case Success( userOld ) => println( "Login OK!" ); userOld
          case Failure( err ) => println( "Login failed!... " , err.getMessage ); new UserOld
        }

        println( userOld )

        val rvValidate = Try[ValidateOld] {
          val future = validate( realm , userOld.token , device )
          val rv = Await.result( future , Duration( 10 , "sec" ) )
          rv // validateOld = validateRv
        }

        val validateOld:ValidateOld = rvValidate match {
          case Success( v ) => println( "Validate OK!" ); v
          case Failure( e ) => println( "Validation failed!... " , e.getMessage ); new ValidateOld
        }

        println( validateOld )
        constructUserInfo( userOld , validateOld )

      } else {

        println( "Empty parameter!..." )
        new UserInfo //

      }

    rv

}

  private def authLogin( realm:String , user:String ,  pass:String ,  device:String ): Future[UserOld] = {

    println( "in 'authLogin' ..." )

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.AUTH.toString() )

    val url = s"$serviceHost/auth/login?realm=$realm&user=$user&pass=$pass&device=$device"

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[UserOld]
    }

  }

  def validate( realm:Option[String] = None, token:Option[String] = None , device:Option[String] = None):Future[ValidateOld] = {

    println( "in 'validate' ..." )

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.AUTH.toString() )

    val url = s"$serviceHost/auth/validate?realm=${realm.get}&token=${token.get}&device=${device.get}"

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[ValidateOld]
    }

  }

  def constructUserInfo( userOld:UserOld , validateOld:ValidateOld ): UserInfo = {
  //def constructUserInfo( userOld:UserOld , validateOld:ValidateOld ): Unit = {

    val listPreferences:List[UserPrefOld] = validateOld.metadataUser.get.preferences.get

    val userInfo:UserInfo = new UserInfo (

      id          = userOld.userId,
      name        = validateOld.metadataUser.get.name.orElse( None ),
      companyId   = userOld.companyId,
      isAdmin     = None, // isAdmin TODO
      profiles    = None, // profiles TODO
      realm       = validateOld.metadataUser.get.realm,
      status      = userOld.status,
      token       = validateOld.token,
      email       = validateOld.metadataUser.get.email,
      timeZone    = listPreferences.head.value,
      savedViews  = None,
      dashboard   = None,
      extraFields = None

    )

    println( userInfo )
    userInfo

  }

}



/*
  val jsonString = news.toJson.toString // news is an existing News object
  val newsObject = jsonString.parseJson.convertTo[News]
  */

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