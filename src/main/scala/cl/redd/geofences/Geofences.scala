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

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class Geofences (implicit val actor:ActorSystem, implicit val materializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  /** 3.1 "/save", POST method */

  def save( geofence:Option[Geofence] = None ) : Geofence = {

    val rv:Geofence =
      if( geofence.nonEmpty ) {
        val resp = Try[Geofence] {
          val futureSave = saveGeofence( geofence.get )
          val saveRv = Await.result( futureSave , Duration( 10 , "sec" ) )
          saveRv // resp = saveRv
        }
        val opResult:Geofence = resp match {
          case Success( v ) => println( "Save OK!" ); v
          case Failure( e ) => println( "Save failed!: " , e.getMessage ); new Geofence
        }
        opResult // rv = opResult
      } else {
        println( "Empty parameter!..." )
        new Geofence
      }
    rv //
  }

  private def saveGeofence( geofence:Geofence ) : Future[Geofence] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url = s"$serviceHost/geofence/save"
    val entity = HttpEntity(MediaTypes.`application/json`, geofence.toJson.toString())
    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "POST" ) , uri = url , entity = entity ) )
    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[Geofence]
    }
  }

  /** 3.2 "/getById", GET method */

  def getGeofenceByIds( realm:String , geofenceIds:List[Int] , fps:FilterPaginateSort ) : List[Geofence] = ???


  /** 3.3 "/getByCompany", GET method */
  def getGeofencesByCompanyId( realm:Option[String], companyId:Option[Int], fps:Option[FilterPaginateSort] ) : Future[List[Geofence]] = {

    // filters OK: by name, by alarm
    val filter = Some( new FilterMain( realm , companyId , if (fps.isDefined) fps.get.filterParams else None) )
    val paginated = if( fps.get.pagLimit.isDefined && fps.get.pagOffset.isDefined )
                      Some( Map("limit"->fps.get.pagLimit.get,"offset"->fps.get.pagOffset.get) )
                    else
                      None
    val request = new GeofGetAllPagNewReq( filter , paginated )
    val futGeofList = {

      val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
      val url         = s"$serviceHost/geofence/getAllPaginatedNew"
      val hds         = List(RawHeader("Accept", "application/json"))
      val body        = HttpEntity( ContentTypes.`application/json`, request.toJson.toString() )

      val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

      future.flatMap {
        case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
          Unmarshal(entity).to[List[GeofenceOld]]
        }
      }
    }.map( listGf => listGf.map( gf => newGeofenceFromOld( gf , realm , companyId ) ) )

    futGeofList

  }

  def getGeofencesByCompanyIdTest( realm:Option[String], companyId:Option[Int], fps:Option[FilterPaginateSort] ) : Future[HttpResponse] = {

    // filters OK: by name, by alarm
    val filter = Some( new FilterMain( realm , companyId , if (fps.isDefined) fps.get.filterParams else None) )

    val paginated = if( fps.get.pagLimit.isDefined && fps.get.pagOffset.isDefined )
                      Some( Map("limit"->fps.get.pagLimit.get,"offset"->fps.get.pagOffset.get) )
                    else
                      None
    val request     = new GeofGetAllPagNewReq( filter , paginated )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url         = s"$serviceHost/geofence/getAllPaginatedNew"
    val hds         = List(RawHeader("Accept", "application/json"))
    val body        = HttpEntity( ContentTypes.`application/json`, request.toJson.toString() )

    Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

  }



  def newGeofenceFromOld( geofence:GeofenceOld, realm:Option[String], companyId:Option[Int] ): Geofence = {

    return new Geofence( geofence.id, geofence.name, geofence.alarm, geofence.colour, geofence.buffer,
                         geofence.latitude, geofence.longitude, geofence.theGeom, geofence.bboxGeom,
                         geofence.userid, geofence.total, realm , geofence.typeId, companyId, geofence.maxSpeed,
                         geofence.extraFields, strToVector( geofence.theGeom ), strToVector( geofence.bboxGeom ) )

  }

  def strToVector( str:Option[String] ): Option[Vector[String]] = {

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
