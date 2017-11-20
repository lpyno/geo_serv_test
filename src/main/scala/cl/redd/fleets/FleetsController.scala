package cl.redd.fleets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects._
import cl.tastets.life.objects.ServicesEnum
import spray.json.pimpAny

import scala.concurrent.{ExecutionContext, Future}

class FleetsController( implicit val system : ActorSystem,
                        implicit val materializer : ActorMaterializer,
                        implicit val ec : ExecutionContext ){

  //def getFleetsByUserId( realm:String , userId:Int , withVehicles:Boolean , fps:FilterPaginateSort ) : Future[List[Fleet]] = ???
  def getFleetsByUserId( params: Option[GetFleetsByUserId] = None ) : Future[String] = {
  //def getFleetsByUserId( params: Option[GetFleetsByUserId] = None ) : Future[List[FleetOld]] = {

    println( "in getFleetsByUserId ..." )

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString )
    //val serviceHost = "http://localhost:42100"
    val url = s"$serviceHost/metadata/vehicle/fleet/getMetadataByUser?realm=${params.get.realm.get}"
    val filterOld = new FilterOld( params.get.fps.get.filterParams , params.get.userId , params.get.userProfile , params.get.companyId )
    val sortOld = new SortOld( params.get.fps.get.sortParam , params.get.fps.get.sortOrder )
    val paginatedOld = new PaginatedOld( params.get.fps.get.pagLimit , params.get.fps.get.pagOffset )
    val reqData = new RequestData( Some(filterOld) , Some(sortOld) , Some(paginatedOld) )
    val hds = List(RawHeader("Accept", "application/json"))
    val body = HttpEntity( ContentTypes.`application/json`, reqData.toJson.toString() )
    println( s"url ... $url" )
    println( s"headers ... $hds" )
    println( s"body ... $body" )

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => println( s"list fleets get OK!... $entity"); Unmarshal(entity).to[String]
      //case HttpResponse( StatusCodes.OK , _ , entity , _ ) => println( s"list fleets get OK!... $entity"); Unmarshal(entity).to[List[FleetOld]]
    }

  }

}
