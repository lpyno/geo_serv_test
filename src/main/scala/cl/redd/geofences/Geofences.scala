package cl.redd.geofences

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects._
import cl.tastets.life.objects.ServicesEnum
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class Geofences (implicit val actor:ActorSystem, implicit val materializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  /** 3.1 "/save", POST method */

  def save( geofence:Geofence ) : Future[Geofence] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url = s"$serviceHost/geofence/save"
    val hds = List(RawHeader("Accept", "application/json"))
    val body = HttpEntity( MediaTypes.`application/json`, newToSaveFormat( geofence ).toJson.toString )
    val futureHttpResp = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

    val rv = futureHttpResp.flatMap{
      case HttpResponse( StatusCodes.OK, _ , entity , _ ) => {
        Unmarshal( entity ).to[GeofenceOld].map( g => newFromOld( g, geofence.realm, geofence.companyId ) )
      }
    }
    rv
  }

  /** 3.2 "/getById", GET method */

  def getGeofenceByIds( realm:String , geofenceIds:List[Int] , fps:FilterPaginateSort ) : List[Geofence] = ???

  def getById ( realm:Option[String]=None , geofenceId:Option[Long]=None ) : Future[Geofence] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString() )
    val url = s"$serviceHost/geofence/findById?realm=${realm.get}&id=${geofenceId.get}"
    val hds = List(RawHeader("Accept", "application/json"))
    val futHttpResp = Http().singleRequest( HttpRequest( HttpMethods.GET , url, hds ) )

    futHttpResp.flatMap{
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
        println( "getById httpResponse OK!!" )
        Unmarshal(entity).to[GeofenceOld]
      }
    }.map( gOld => newFromOld( gOld, realm, gOld.companyId ) )

  }


  /** 3.3 "/getByCompany", GET method */
  def getGeofencesByCompanyId( realm:Option[String], companyId:Option[Int], fps:Option[FilterPaginateSort] ) : Future[List[Geofence]] = {

    // filters OK: by name, by alarm
    val filter = Some( FilterMain( realm , companyId , if (fps.isDefined) fps.get.filterParams else None) )
    val paginated = if( fps.get.pagLimit.isDefined && fps.get.pagOffset.isDefined )
                      Some( Map("limit"->fps.get.pagLimit.get,"offset"->fps.get.pagOffset.get) )
                    else
                      None
    val request = GeofGetAllPagNewReq( filter , paginated )
    println( s"request: $request" )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    println( s"serviceHost: $serviceHost" )
    val url         = s"$serviceHost/geofence/getAllPaginatedNew"
    println( s"url: $url" )
    val hds         = List(RawHeader("Accept", "application/json"))
    println( s"headers: $hds" )
    val body        = HttpEntity( ContentTypes.`application/json`, request.toJson.toString() )
    println( s"body: $body" )

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

    val oldList = future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
        println( "httpResponse OK!!" )
        Unmarshal(entity).to[List[GeofenceOld]]
      }
    }
    println( oldList )
    val rv = oldList.map( listGf => listGf.map( gf => newFromOld( gf , realm , companyId ) ) )
    rv

  }

  def queryOldGeofences( realm:Option[String], companyId:Option[Int], fps:Option[FilterPaginateSort] ) : Future[HttpResponse] = {

    val request     = constructRequest( realm , companyId , fps )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url         = s"$serviceHost/geofence/getAllPaginatedNew"
    val hds         = List(RawHeader("Accept", "application/json"))
    val body        = HttpEntity( ContentTypes.`application/json`, request.toJson.toString() )

    Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

  }

  private def constructRequest( realm:Option[String], companyId:Option[Int], fps:Option[FilterPaginateSort] ):Option[GeofGetAllPagNewReq] = {

    if ( realm.isDefined && companyId.isDefined && fps.isDefined && fps.get.pagLimit.isDefined && fps.get.pagOffset.isDefined ){
      // filters OK: by name, by alarm
      val filter = Some( FilterMain( realm , companyId , fps.get.filterParams ) )
      val paginated = Some( Map( "limit" -> fps.get.pagLimit.get, "offset" -> fps.get.pagOffset.get ) )
      Some( GeofGetAllPagNewReq( filter , paginated ) )
    } else {
      println( "Request param undefined..." )
      None
    }

  }

  def newFromOld( geofence:GeofenceOld, realm:Option[String], companyId:Option[Int] ): Geofence = {

    Geofence(
      geofence.id, geofence.name, geofence.alarm, geofence.colour, geofence.buffer, geofence.last_update_timestamp,
      geofence.latitude, geofence.longitude, geofence.theGeom, geofence.bboxGeom,
      geofence.userid, geofence.total, realm , geofence.typeId, companyId, geofence.maxSpeed,
      geofence.extraFields, strToVector( geofence.theGeom ), strToVector( geofence.bboxGeom )
    )

  }

  def oldFromNew( geofence:Geofence ): GeofenceOld = {

    GeofenceOld(
      geofence.id, geofence.name, geofence.buffer, geofence.lastUpdateTs, geofence.alarm, geofence.latitude,
      geofence.colour, geofence.userId, geofence.longitude, geofence.total, geofence.typeId,
      geofence.companyId, geofence.extraFields, geofence.maxSpeed, geofence.theGeom, geofence.bboxGeom
    )

  }

  def newToSaveFormat( geofence:Geofence ): GeofenceToSave = {

    GeofenceToSave(
      geofence.name, geofence.realm, geofence.companyId, geofence.userId, geofence.typeId,
      geofence.buffer, geofence.bboxGeom, geofence.theGeom, geofence.alarm, geofence.maxSpeed,
      geofence.colour
    )

  }

  private def strToVector( str:Option[String] ): Option[Vector[String]] = {

    if ( str.isDefined ) {
      val rv = str.get.replace("POLYGON((", "")
        .replace("))", "")
        .replace(' ', ';')
        .split(",")
        .toVector
      Some( rv )
    } else {
      None
    }

  }


  /**3.4 "/getFromCompanyByParameter" ( * "getByCompany" w/ Filter? ) */

  def getCompanyGeofencesByParameter(realm:String , companyId:Int , paramName:String, paramValue:String , fps:FilterPaginateSort ) : List[Geofence] = ???

  /** 3.5 "/getFromUserByParameter" ( * "getByUser" w/ Filter? ) */

  def getUserGeofencesByParameter( realm:String , userId:Int , paramName:String , paramValue:String , fps:FilterPaginateSort ) : List[Geofence] = ???

  /** 3.6 "/update", PUT method */

  def update( realm:String , updatedGeofence:Geofence ) : Geofence = ???

  /** 3.7 "/delete", DELETE method */

  def delete( realm:String , geofenceId:Int ) : Geofence = ???

}
