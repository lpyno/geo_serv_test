package cl.redd.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects._
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

        val rvMetadataUser = Try[MetadataUserOld] {
          val future = getUserMetadata( realm , userOld.userId )
          val rv = Await.result( future , Duration( 10 , "sec" ) )
          rv // validateOld = validateRv
        }

        val metadataOld:MetadataUserOld = rvMetadataUser match {
          case Success( metadata ) => println( "Metadata get OK!" ); metadata
          case Failure( err ) => println( "Metadata get failed!... " , err.getMessage ); new MetadataUserOld
        }

        println( metadataOld )

        val rvSelectProfiles = Try[SelectProfiles] {
          val future = selectProfiles( realm , device , userOld.username )
          val rv = Await.result( future , Duration( 10 , "sec" ) )
          rv // validateOld = validateRv
        }

        val authProfiles:SelectProfiles = rvSelectProfiles match {
          case Success( profiles ) => println( "Profiles get OK!" ); profiles
          case Failure( err ) => println( "Profiles get failed!... " , err.getMessage ); new SelectProfiles
        }

        println( authProfiles )

        constructUserInfo( userOld , metadataOld , authProfiles )

      } else {

        println( "Empty parameter!..." )
        UserInfo() //

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

  def constructUserInfo( userOld:UserOld , metadataOld:MetadataUserOld , authProfiles:SelectProfiles ): UserInfo = {

    val isAdmin:Option[Boolean] = if ( authProfiles.
                                        profiles.
                                          get.
                                            head.
                                              childs.
                                                get.
                                                  head.
                                                    state.
                                                      get.
                                                        equalsIgnoreCase( "configuration.isAdmin" ) )
                                    Some(true)
                                  else
                                    Some(false)

    val userInfo:UserInfo = new UserInfo (

      id          = userOld.userId,
      name        = metadataOld.userName,
      companyId   = metadataOld.companyId,
      isAdmin     = isAdmin,
      profiles    = metadataOld.profiles,
      realm       = metadataOld.realm,
      status      = userOld.status,
      token       = userOld.token,
      email       = metadataOld.email,
      timeZone    = metadataOld.preferences.get.head.value,
      savedViews  = None,
      dashboard   = None,
      extraFields = None

    )

    println( userInfo )
    userInfo

  }

  def selectProfiles(realm: Option[String], device: Option[String] , user:Option[String]):Future[SelectProfiles] = {

    println( "in 'select profile' ..." )

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.AUTH.toString() )

    val url = s"$serviceHost/auth/authorization/selectProfiles?realm=${realm.get}&device=${device.get}&user=${user.get}"

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[SelectProfiles]
    }

  }

  // http://192.168.173.166:42200/metadata/user/getById?realm=rslite&id=397

  def getUserMetadata( realm : Option[String] = None , userId : Option[Int] = None ) = {

    println( "in 'getUserMetadata' ..." )

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAUSER.toString() )

    val url = s"$serviceHost/metadata/user/getById?realm=${realm.get}&id=${userId.get}"

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[MetadataUserOld]
    }

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