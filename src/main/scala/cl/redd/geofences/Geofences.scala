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
import cl.redd.objects.RequestResponses.GetByCompanyReq
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
    println( "save body:" + body )
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
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[GeofenceOld]
    }.map( gOld => newFromOld( gOld, realm, gOld.companyId ) )

  }


  /** 3.3 "/getByCompany", GET method */
  def getGeofencesByCompanyId( realm:String, companyId:Int, fps:FilterPaginateSort ) : Future[List[Geofence]] = {

    // filters supported: 1.by name, 2.by alarm, 3.by id/ignoreIds, 4.by typeId, 5.by userId, 6.by lastUpdateTs (updateDateInit), 7.by bbox, 8.by extraFields
    // sort params supported: name, maxSpeed, typeId, updateDateInit(lastUpdateTs) [only asc]

    val listFilters = fps.filterParams
      .map( tuple => s"""{"${tuple._1}":"${tuple._2}"}""" )
      .toString
      .replace("List(","")
      .replace(")","")
      .replace(" ","")

    // request format to /geofence/getAllPaginatedNew
    val strBody =
      s"""{"filter":{"realm":"$realm","companyId":$companyId,"filter":[$listFilters],"sort":"${fps.sortParam}"},"paginated":{"limit":${fps.pagLimit},"offset":${fps.pagOffset}}}"""
        .stripMargin
    println( s"strBody: $strBody" )

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url = s"$serviceHost/geofence/getAllPaginatedNew"
    val hds = List(RawHeader("Accept", "application/json"))
    val body = HttpEntity( ContentTypes.`application/json`, strBody )

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[List[GeofenceOld]]
    }.map( listGf => listGf.map( gf => newFromOld( gf , Some(realm) , Some(companyId) ) ) )

  }

  def parseRequestParams( request:String ):Option[GetByCompanyReq] = {

    val jsObject = request
      .parseJson
      .asJsObject( "Invalid Json")

    val realmJs = jsObject.fields.get("realm")
    val realm = if (realmJs.isDefined){
      realmJs.get.convertTo[String]
    } else { return None }
    //println( s"realm: $realm " )
    val companyIdJs = jsObject.fields.get("companyId")
    val companyId = if (companyIdJs.isDefined){
      companyIdJs.get.convertTo[Int]
    } else { return None }
    //println( s"companyId: $companyId" )
    val fpsJs = jsObject.fields.get("fps")
    val fps = if (fpsJs.isDefined){
      fpsJs.get.convertTo[Map[String,JsValue]]
    } else { return None }
    //println( s"fps: $fps" )
    val filterParamsJs = fps.get("filterParams")
    val filterParams = if (filterParamsJs.isDefined){
      filterParamsJs.get.convertTo[Map[String,String]]
    } else { return None }
    //println( s"filterFields: $filterParams" )

    Some(
      GetByCompanyReq( realm, companyId, FilterPaginateSort(
        filterParams,
        fps.get("pagLimit").get.convertTo[Int],
        fps.get("pagOffset").get.convertTo[Int],
        fps.get("sortOrder").get.convertTo[Int],
        fps.get("sortParam").get.convertTo[String]
        )
      )
    )
  }

  def queryOldGeofences( realm:Option[String], companyId:Option[Int], fps:Option[FilterPaginateSort] ) : Future[HttpResponse] = {

    val request = constructRequest( realm , companyId , fps )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url = s"$serviceHost/geofence/getAllPaginatedNew"
    val hds = List(RawHeader("Accept", "application/json"))
    val body = HttpEntity( ContentTypes.`application/json`, request.toJson.toString )

    Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

  }

  private def constructRequest( realm:Option[String], companyId:Option[Int], fps:Option[FilterPaginateSort] ):Option[GeofGetAllPagNewReq] = {

    if ( realm.isDefined && companyId.isDefined && fps.isDefined ){
      // filters OK: by name, by alarm
      val filter = Some( FilterMain( realm.get , companyId.get , fps.get.filterParams ) )
      val paginated = Some( Map( "limit" -> fps.get.pagLimit, "offset" -> fps.get.pagOffset ) )
      Some( GeofGetAllPagNewReq( filter , paginated ) )
    } else {
      println( "Request param undefined..." )
      None
    }

  }

  def newFromOld( geofence:GeofenceOld, realm:Option[String], companyId:Option[Int] ): Geofence = {

    Geofence(
      geofence.id, geofence.name, geofence.alarm, geofence.colour, geofence.buffer, geofence.lastUpdateTs,
      geofence.latitude, geofence.longitude, geofence.theGeom, geofence.bboxGeom,
      geofence.userId, geofence.total, realm , geofence.typeId, companyId, geofence.maxSpeed,
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

  def delete( geofenceId:Long ) : Future[Map[String,Long]] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url = s"$serviceHost/geofence/delete/$geofenceId"
    val hds = List(RawHeader("Accept", "application/json"))
    val futHttpResp = Http().singleRequest( HttpRequest( HttpMethods.DELETE , url, hds ) )

    val rv = futHttpResp.flatMap{
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
        Unmarshal(entity).to[Map[String,Long]]
      }
    }
    rv
  }

}
