package cl.redd.vehicles

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects.{FilterPaginateSort, Vehicle}
import cl.tastets.life.objects.ServicesEnum
import spray.json.pimpAny

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class VehiclesController(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

  /** 3.1 "/save", POST method */

  def save( vehicle:Option[Vehicle] = None ) : Vehicle = {

    val rv:Vehicle =

      if( vehicle.nonEmpty ) {

          val rvVehicle = Try[Vehicle] {
          val futureSave = saveVehicle( vehicle.get )
          val rv = Await.result( futureSave , Duration( 10 , "sec" ) )
          rv // rvVehicle = rv
        }

        val opResult:Vehicle = rvVehicle match {
          case Success( v ) => println( "Vehicle saved OK!" ); v
          case Failure( e ) => println( "Vehicle save failed!: " , e.getMessage ); new Vehicle
        }

        opResult // rv = opResult

      } else {

        println( "Empty parameter!..." )
        new Vehicle

      }

    rv //

  }



  private def saveVehicle( vehicle:Vehicle ) : Future[Vehicle] = ???
  /*
  {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString )

    val url = s"$serviceHost/metadata/vehicle/update"

    val entity = HttpEntity(MediaTypes.`application/json`, vehicle.toJson.toString())

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "PUT" ) , uri = url , entity = entity ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[Vehicle]
    }

  }
  */
}
