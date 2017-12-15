package cl.redd.vehicles

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects.RequestResponses.GetVehiclesByUserId
import cl.redd.objects._
import cl.tastets.life.objects.ServicesEnum
import spray.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class Vehicles(implicit val actor:ActorSystem, implicit val materializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

  /** 4.1 "/save", POST method */

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

  private def getById ( realm:String , id:Int , withLastState:Boolean ) : Future[VehicleOld] = {

    println( "in getVehiclesById..." )

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString )

    val url = s"$serviceHost/metadata/vehicle/getVehicleById?idVehicle=$id&realm=$realm&lastState=$withLastState"

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "GET" ) , uri = url ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[VehicleOld]
    }

  }

  def getVehiclesById( realm:Option[String] = None, ids:Option[List[Int]] = None, withLastState:Option[Boolean] = None , fps:Option[FilterPaginateSort] = None ) : List[VehicleOld] = {

    println( "in getVehiclesById..." )

    val vehiclesList:List[VehicleOld] =

      if( realm.nonEmpty && ids.nonEmpty && withLastState.nonEmpty && fps.nonEmpty ) {

        val rvList = Try[List[VehicleOld]] {

          val listOfFutures = for ( id <- ids.get ) yield {

            val veh = getById( realm.get , id , withLastState.get )
            veh

          }

          val futureVehList = Future.sequence( listOfFutures )

          val rv = Await.result( futureVehList , Duration( 10 , "sec" ) )

          rv

        }

        val opResult:List[VehicleOld] = rvList match {

          case Success( vehs ) => println("Vehicles get OK!") ; vehs
          case Failure( err ) => println("Vehicles get failed!: ", err.getMessage); List( new VehicleOld )

        }

        println( opResult )
        opResult // rv = opResult

      } else {

        println( "Empty parameter in request ..." )
        List( new VehicleOld )

      }

    vehiclesList

  }

  def getVehiclesByImei( params:Option[GetListByMids]=None ):Future[List[VehicleOld]] = {

    println( "in getVehiclesByImei ..." )
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString )
    val url = s"$serviceHost/metadata/vehicle/getByListMidsAsync"
    val hds= List(RawHeader("Accept", "application/json"))
    val body = HttpEntity( ContentTypes.`application/json`, params.get.toJson.toString() )
    println( s"url ... $url" )
    println( s"headers ... $hds" )
    println( s"body ... $body" )

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[List[VehicleOld]]
    }

  }

  def getByUserId( realm:String , companyId:Int , withLastState:Boolean , fps:FilterPaginateSort ) : Future[List[Vehicle]] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString() )
    val url = s"$serviceHost/metadata/vehicle/getVehiclesByUser?realm=$realm&companyId=$companyId&lastState=$withLastState"
    val hds = List(RawHeader("Accept", "application/json"))
    val strBody =
      s"""{"filter":{"userProfile":"${fps.filterParams.get("userProfile").get}","userId":"${fps.filterParams.get("userId").get}"},"sort":{"field":"${fps.sortParam}", "order":${fps.sortOrder}},"paginated":{"limit":${fps.pagLimit},"offset":${fps.pagOffset}}}"""
        .stripMargin
    val body = HttpEntity( MediaTypes.`application/json`, strBody )
    val futHttpResp = Http().singleRequest( HttpRequest( HttpMethods.POST, url, hds, body ) )

    val rv = futHttpResp.flatMap{
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[List[VehicleFromGetByUser]]
    }.map( list => list.map( v => newFromGetByUser( v ) ) )
      .map( list => list.map( v => getActivityStatus( v ) ) )
    rv
  }

  def vehOldToNew(vo:VehicleFromGetByCompany):Vehicle = {

    Vehicle (
      id                = if( vo.vehicleId.isDefined ) vo.vehicleId else None,
      name              = if( vo.name.isDefined ) vo.name else None,
      activityStatus    = None,
  //    companyId         = if( vo.idCompany.isDefined ) vo.idCompany else None,
  //    rutCompany        = None,
      vin               = if( vo.vin.isDefined ) vo.vin else None,
      plateNumber       = if( vo.plateNumber.isDefined ) vo.plateNumber else None,
      engineTypeId      = None, // movil.tipo_motor_id
      subVehicleTypeId  = None, // movil.sub_tipo_movil_id
      createDate        = None, // movil.fecha_creacion
      validateDate      = None, // movil.fecha_validado
      dischargeDate     = None, // movil.fecha_baja
      imei              = if( vo._m.isDefined ) vo._m else None,
      deviceTypeId      = if( vo.deviceTypeId.isDefined ) vo.deviceTypeId else None,
      simCard           = if( vo.simcard.isDefined ) vo.simcard else None,
      engineTypeName    = None,
      deviceTypeName    = if( vo.deviceTypeName.isDefined ) vo.deviceTypeName else None,
      subVehicleTypeName = None, //
      vehicleTypeName    = None,
      realm           = if( vo.realm.isDefined ) vo.realm else None,
      extraFields     = if( vo.extraFields.isDefined ) vo.extraFields else None,
      lastState       = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( new LastState )
    )
  }

  def newFromGetByUser( vo:VehicleFromGetByUser ): Vehicle = {

    Vehicle (
      id                = if( vo.vehicleId.isDefined ) vo.vehicleId else None,
      name              = if( vo.name.isDefined ) vo.name else None,
      activityStatus    = None, // filled later
      //companyId         = if( vo.idCompany.isDefined ) vo.idCompany else None,
      //rutCompany        = if( vo.rut_Company.isDefined ) vo.rut_Company else None,
      vin               = if( vo.vin.isDefined ) vo.vin else None,
      plateNumber       = if( vo.plateNumber.isDefined ) vo.plateNumber else None,
      engineTypeId      = if( vo.engineTypeId.isDefined ) vo.engineTypeId else None,
      subVehicleTypeId  = if( vo.subVehicleTypeId.isDefined ) vo.subVehicleTypeId else None,
      createDate        = if( vo.createDate.isDefined ) vo.createDate else None,
      validateDate      = if( vo.validateDate.isDefined ) vo.validateDate else None,
      dischargeDate     = if( vo.dischargeDate.isDefined ) vo.dischargeDate else None,
      imei              = if( vo._m.isDefined ) vo._m else None,
      deviceTypeId      = if( vo.deviceTypeId.isDefined ) vo.deviceTypeId else None,
      simCard           = if( vo.simcard.isDefined ) vo.simcard else None,
      //engineTypeName    = if( vo.engineTypeName.isDefined ) vo.engineTypeName else None,
      deviceTypeName    = if( vo.deviceTypeName.isDefined ) vo.deviceTypeName else None,
      //subVehicleTypeName = if( vo.subVehicleTypeName.isDefined ) vo.subVehicleTypeName else None,
      //vehicleTypeName    = if( vo.vehicleTypeName.isDefined ) vo.vehicleTypeName else None,
      realm           = if( vo.realm.isDefined ) vo.realm else None,
      total           = if ( vo.total.isDefined ) vo.total else None,
      extraFields     = if( vo.extraFields.isDefined ) vo.extraFields else None,
      lastState       = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( new LastState )
    )

  }


  def lastStateNew( lastState:LastStateOld ):Option[LastState] = {

    Some( LastState(
      date      = if(lastState.date.isDefined) lastState.date else None,
      eventId   = if(lastState.eventId.isDefined) lastState.eventId else None,
      odometer  = if(lastState.odometer.isDefined) lastState.odometer else None,
      hourmeter = if(lastState.hourmeter.isDefined) lastState.hourmeter else None,
      latitude  = if(lastState.latitude.isDefined) lastState.latitude else None,
      longitude = if(lastState.longitude.isDefined) lastState.longitude else None,
      altitude  = if(lastState.alt.isDefined) lastState.alt else None,
      azimuth   = if(lastState.azimuth.isDefined) lastState.azimuth else None,
      speed     = if(lastState.speed.isDefined) lastState.speed else None,
      geotext   = if(lastState.geotext.isDefined) lastState.geotext else None
    )
    )

  }

  def getActivityStatus( vehicle: Vehicle ):Vehicle = {

    if ( vehicle.lastState.isDefined && vehicle.lastState.get.date.isDefined && vehicle.lastState.get.speed.isDefined ){
      val secsElapsed = ( System.currentTimeMillis() - vehicle.lastState.get.date.get ) / 1000 // as seconds
      if ( secsElapsed > 86400 ) {
        return vehicle.copy( activityStatus = Some("INACTIVE24") )
      } else if ( secsElapsed > 28800 && secsElapsed <= 86400) {
         return vehicle.copy( activityStatus = Some("INACTIVE") )
      } else if ( secsElapsed < 28800 ) {
        return vehicle.copy( activityStatus = Some("ACTIVE") )
      } else if ( secsElapsed <= 900 && vehicle.lastState.get.speed.get >= 5 ) {
        return vehicle.copy( activityStatus = Some("ACTIVE5KM") )
      } else vehicle.copy( activityStatus = Some(s"INVALID_STATUS... elapsed: $secsElapsed") )
    } else {
      return vehicle
    }

  }

  def parseGetByUserParams( request: String ): Option[GetVehiclesByUserId] = {

    val jsObject = request
      .parseJson
      .asJsObject("Invalid Json")

    val realm = if (jsObject.fields.get("realm").isDefined) {
      jsObject.fields.get("realm").get.convertTo[String]
    } else {
      return None
    }
    val companyId = if (jsObject.fields.get("companyId").isDefined) {
      jsObject.fields.get("companyId").get.convertTo[Int]
    } else {
      return None
    }
    val withLastState = if (jsObject.fields.get("withLastState").isDefined) {
      jsObject.fields.get("withLastState").get.convertTo[Boolean]
    } else {
      return None
    }
    val fps = if (jsObject.fields.get("fps").isDefined) {
      jsObject.fields.get("fps").get.convertTo[Map[String, JsValue]]
    } else {
      return None
    }
    val filterParams = if (fps.get("filterParams").isDefined) {
      fps.get("filterParams").get.convertTo[Map[String, String]]
    } else {
      return None
    }
    if (filterParams.get("userProfile").isDefined && filterParams.get("userId").isDefined) {
      Some(
        GetVehiclesByUserId(realm, companyId, withLastState, FilterPaginateSort(
          filterParams,
          fps.get("pagLimit").get.convertTo[Int],
          fps.get("pagOffset").get.convertTo[Int],
          fps.get("sortOrder").get.convertTo[Int],
          fps.get("sortParam").get.convertTo[String]
        )
        )
      )
    } else { return None }
  }

}
