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

import scala.concurrent.{ExecutionContext, Future}


class Auth( implicit val actor:ActorSystem, implicit val materializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

  def login(realm:Option[String] = None, user:Option[String] = None, pass:Option[String] = None, device:Option[String] = None ) : Future[UserInfo] = {

    if( realm.isDefined && user.isDefined && pass.isDefined && device.isDefined ) {

      val futureRvLogin = authLogin( realm.get, user.get, pass.get, device.get )

      futureRvLogin.flatMap{
        //println( "on flattened future..." )
        user => {
          //println( s"user: ${user}" )
          val futureMetadataUser = getUserMetadata( realm, user.userId )
          val futureProfiles = selectProfiles( realm, device, user.username )
          val results = for {
            metadata <- futureMetadataUser
            profiles <- futureProfiles
          } yield ( /*rvLog,*/ metadata, profiles )
          val userInfo = results.map{ params => constructUserInfo( user, params._1, params._2 ) }
          userInfo
        }
      }

    } else{

      println( "login failed, empty parameter!..." )
      return Future( UserInfo() ) //

    }
}

  private def authLogin( realm:String , user:String ,  pass:String ,  device:String ): Future[UserOld] = {

    val startTs = System.currentTimeMillis()
    //println( s"running 'authLogin()'... [$startTs]" )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.AUTH.toString() )
    val url = s"$serviceHost/auth/login?realm=$realm&user=$user&pass=$pass&device=$device"
    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )
    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
        //println( s"'authLogin()' completed![${System.currentTimeMillis()}], elapsed [${System.currentTimeMillis() - startTs} ms]" )
        Unmarshal(entity).to[UserOld]
      }
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

    val startTs = System.currentTimeMillis()
    //println( s"running 'constructUserInfo()'... [$startTs]" )
    val child = authProfiles.
      profiles.
      get.
      head.
      childs.
      get.
      head

    val isAdmin:Option[Boolean] =
      if ( child.state.get.equalsIgnoreCase( "configuration.isAdmin" ) ){
        child.active
      } else {
        println( "not configuration child, setting 'isAdmin' to false! ..." )
        Some(false)
      }

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
      timeZone    = if( metadataOld.preferences.get != Nil ) metadataOld.preferences.get.head.value else Some( "undefined preferences" ),
      savedViews  = None,
      dashboard   = None,
      extraFields = None

    )
    //println( s"'constructUserInfo()' completed... [${System.currentTimeMillis()}], elapsed[${System.currentTimeMillis()-startTs}]" )
    userInfo

  }

  def selectProfiles(realm: Option[String], device: Option[String] , user:Option[String]):Future[SelectProfiles] = {

    val startsTs = System.currentTimeMillis()
    //println( s"running selectProfiles()... [$startsTs]" )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.AUTH.toString() )
    val url = s"$serviceHost/auth/authorization/selectProfiles?realm=${realm.get}&device=${device.get}&user=${user.get}"
    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )
    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
        //println( s"response profiles: ${Unmarshal(entity).to[SelectProfiles].toString}" )
        //println( s"'selectProfiles()' completed... [${System.currentTimeMillis()}], elapsed [${System.currentTimeMillis() - startsTs}]" )
        Unmarshal(entity).to[SelectProfiles]
      }
    }
  }

  def getUserMetadata( realm : Option[String] = None , userId : Option[Int] = None ):Future[MetadataUserOld] = {

    val startTs = System.currentTimeMillis()
    //println( s"running 'getUserMetadata()'... [$startTs]" )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAUSER.toString() )
    val url = s"$serviceHost/metadata/user/getById?realm=${realm.get}&id=${userId.get}"
    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )
    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
        //println( s"response metadata: ${Unmarshal(entity).to[MetadataUserOld].toString}" )
        //println( s"'getUserMetadata()' completed'... [${System.currentTimeMillis()}], elapsed [${System.currentTimeMillis() - startTs}]" )
        Unmarshal(entity).to[MetadataUserOld]
      }
    }
  }
}
