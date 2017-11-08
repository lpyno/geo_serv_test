package cl.redd.geofences

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.objects.{FilterPaginateSort, Geofence}
import cl.redd.discovery.ReddDiscoveryClient
import cl.tastets.life.objects.ServicesEnum

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import cl.redd.objects.ReddJsonProtocol._

class GeofenceController ( implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

  /** 3.1 "/save", POST method */

  def save( realm:Option[String] = None , geofence:Option[Geofence] = None ) : Geofence = {

    val rv:Geofence =
      if( realm.isEmpty || geofence.isEmpty ) {

        val resp = Try[Geofence] {
          val futureSave = saveGeofence( realm.get , geofence.get )
          val saveRv = Await.result( futureSave , Duration( 10 , "sec" ) )
          saveRv // resp = saveRv
        }

        val opResult:Geofence = resp match {
          case Success( v ) => v
          case Failure( e ) => println( "Save failed!: " , e.getMessage ); new Geofence
        }

        opResult // rv = opResult

      } else {

        println( "Empty parameter!..." )
        new Geofence

      }
    rv //
  }

  def saveGeofence( realm:String , geofence:Geofence ) : Future[Geofence] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.GEOFENCE.toString )
    val url = s"$serviceHost/geofence/save?geofence=$geofence"
    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( uri = url ) )
    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[Geofence]
    }

  }

  /** 3.2 "/getById", GET method */

  def getGeofenceByIds( realm:String , geofenceIds:List[Int] , fps:FilterPaginateSort ) : List[Geofence] = ???

  /** 3.3 "/getByCompany", GET method */

  def getGeofencesByCompanyId( realm:String , companyId:Int , fps:FilterPaginateSort ) : List[Geofence] = ???

  /**3.4 "/getFromCompanyByParameter" ( * "getByCompany" w/ Filter? ) */

  def getCompanyGeofencesByParameter(realm:String , companyId:Int , paramName:String, paramValue:String , fps:FilterPaginateSort ) : List[Geofence] = ???

  /** 3.5 "/getFromUserByParameter" ( * "getByUser" w/ Filter? ) */

  def getUserGeofencesByParameter( realm:String , userId:Int , paramName:String , paramValue:String , fps:FilterPaginateSort ) : List[Geofence] = ???

  /** 3.6 "/update", PUT method */

  def update( realm:String , updatedGeofence:Geofence ) : Geofence = ???

  /** 3.7 "/delete", DELETE method */

  def delete( realm:String , geofenceId:Int ) : Geofence = ???

}
