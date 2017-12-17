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
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class Fleets( implicit val system : ActorSystem,
                        implicit val materializer : ActorMaterializer,
                        implicit val ec : ExecutionContext ){


  private def fleetOldToNew( fleetOld: FleetOld ) : Fleet = {

    val fleet =
      new Fleet( id = fleetOld.id, name = fleetOld.name, companyId = fleetOld.companyId,
        companyName = fleetOld.companyName, defaultFleet = fleetOld.defaultFleet, shared = fleetOld.shared,
        maxSpeed = fleetOld.maxSpeed, createDate = fleetOld.createDate, startDay = fleetOld.startDay,
        startHour = fleetOld.startHour, endDate = fleetOld.endDate, endHour = fleetOld.endHour,
        inactiveDays = fleetOld.inactiveDays, generateReport = fleetOld.generateReport, total = fleetOld.total,
        realm = fleetOld.realm
      )
    fleet
  }

  def getFleetStatus( realm:String , id:Long ):Future[VehicleActivity] = {

    val va = {
      val serviceHost = ReddDiscoveryClient.getNextIpByName(ServicesEnum.METADATAVEHICLE.toString)
      val url = s"$serviceHost/metadata/vehicle/fleet/getMetadataFleetStatus?realm=$realm&id=$id"
      val hds = List(RawHeader("Accept", "application/json"))
      val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, url, hds))
      future.flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) => {
          Unmarshal(entity).to[VehicleActivity]
        }
      }
    }
    return va
  }

  def getReqParamsFleetsByUser( request:String ):Option[GetFleetsByUserId] = {

    val jsObject = request
      .parseJson
      .asJsObject( "Invalid Json" )

    val realmJs = jsObject.fields.get("realm")
    val realm = if (realmJs.isDefined){
      realmJs.get.convertTo[String]
    } else { return None }

    val userIdJs = jsObject.fields.get("userId")
    val userId = if (userIdJs.isDefined){
      userIdJs.get.convertTo[Int]
    } else { return None }

    val companyIdJs = jsObject.fields.get("companyId")
    val companyId = if (companyIdJs.isDefined){
      companyIdJs.get.convertTo[Int]
    } else { return None }

    val userProfileJs = jsObject.fields.get("userProfile")
    val userProfile = if (userProfileJs.isDefined){
      userProfileJs.get.convertTo[String]
    } else { return None }

    val wVehiclesJs = jsObject.fields.get("withVehicles")
    val wVehicles = if (wVehiclesJs.isDefined){
      wVehiclesJs.get.convertTo[Boolean]
    } else { return None }

    val wLastStateJs = jsObject.fields.get("withLastState")
    val wLastState = if (wLastStateJs.isDefined){
      wLastStateJs.get.convertTo[Boolean]
    } else { return None }

    val fpsJs = jsObject.fields.get("fps")
    val fps = if (fpsJs.isDefined){
      fpsJs.get.convertTo[Map[String,JsValue]]
    } else { return None }

    val filterParamsJs = fps.get("filterParams")
    val filterParams = if (filterParamsJs.isDefined){
      filterParamsJs.get.convertTo[Map[String,String]]
    } else { return None }

    Some(
      GetFleetsByUserId( realm, userId, companyId, userProfile, wVehicles, wLastState, FilterPaginateSort(
        filterParams,
        fps.get("pagLimit").get.convertTo[Int],
        fps.get("pagOffset").get.convertTo[Int],
        fps.get("sortOrder").get.convertTo[Int],
        fps.get("sortParam").get.convertTo[String]
      )
      )
    )
  }


  private def getMetadataByFleet( fleet:Fleet , withLastState:Boolean ):Future[Fleet] = {

    val newFleet = {
      val serviceHost = ReddDiscoveryClient.getNextIpByName(ServicesEnum.METADATAVEHICLE.toString)
      val url = s"$serviceHost/metadata/vehicle/getMetadataByFleet?" +
                s"realm=${fleet.realm.get}&companyId=${fleet.companyId.get}&fleetId=${fleet.id.get}&lastState=$withLastState"
      val hds = List(RawHeader("Accept", "application/json"))
      val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, url, hds))
      future.flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) => Unmarshal(entity).to[List[VehicleFromGetMetadataByFleet]]
      }
    }
    val rv = newFleet
      .map( list => list.map( v => vehOldToNew( v ) ) )
      .map( vList => fleet.copy( fleetVehicles = Some(vList) ) )
    rv
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

  def vehOldToNew( vo:VehicleFromGetMetadataByFleet ):Vehicle = {

    Vehicle (
      id                = if( vo.vehicleId.isDefined ) vo.vehicleId else None,
      name              = if( vo.name.isDefined ) vo.name else None,
      /*activityStatus,*/
      //companyId         = if( vo.companyId.isDefined ) vo.companyId else None,
      /*rutCompany*/
      vin               = if( vo.vin.isDefined ) vo.vin else None,
      plateNumber       = if( vo.plateNumber.isDefined ) vo.plateNumber else None,
      /*engineTypeId      = vOld.engineTypeId,*/
      /*subVehicleTypeId  = vOld.subVehicleTypeId,*/
      createDate        = if( vo.createDate.isDefined ) vo.createDate else None,
      validateDate      = if( vo.validateDate.isDefined ) vo.validateDate else None,
      dischargeDate     = if( vo.dischargeDate.isDefined ) vo.dischargeDate else None,
      imei              = if( vo._m.isDefined ) vo._m else None,
      deviceTypeId      = if( vo.deviceTypeId.isDefined ) vo.deviceTypeId else None,
      simCard           = if( vo.simcard.isDefined ) vo.simcard else None,
      /*engineTypeName  = vOld.engineTypeName,*/
      deviceTypeName    = if( vo.deviceTypeName.isDefined ) vo.deviceTypeName else None,
      /*subVehicleTypeName = vOld.subVehicleTypeName,*/
      /*vehicleTypeName  = vOld.vehicleTypeName,*/
      realm              = if( vo.realm.isDefined ) vo.realm else None,
      extraFields        = if( vo.extraFields.isDefined ) vo.extraFields else None,
      lastState          = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( new LastState )
    )
  }


  def getFleetsByUserId( realm:String, userId:Int, userProfile:String, companyId:Int, withVehicles:Boolean, withLastState:Boolean, fps:FilterPaginateSort ) : Future[List[Fleet]] = {

    // supported filter fields: id, name, companyId, generateReport
    val filtersList = fps
      .filterParams
      .map( tuple => s"""{"${tuple._1}":"${tuple._2}"}""" )
      .toString().replace("List(","").replace(")","").replace(" ","")
    val strBody = s"""{"filter":{"filter":[$filtersList],"userId":$userId,"userProfile":"$userProfile","companyId":$companyId,"sort":"${fps.sortParam}"},"paginated":{"limit":${fps.pagLimit},"offset":${fps.pagOffset}}}"""
      .stripMargin

    val futListFleets = {

      val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString )
      val url = s"$serviceHost/metadata/vehicle/fleet/getMetadataByUser?realm=$realm"
      val hds = List(RawHeader("Accept", "application/json"))
      val body = HttpEntity( ContentTypes.`application/json`, strBody )

      val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )
      future.flatMap {
        case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[List[FleetOld]]
      }
    }.flatMap( list => Future { list.map( fleet => fleetOldToNew( fleet ) ) } )
      .map( l => l.filter( f => f.realm.isDefined
        && f.id.isDefined
        && f.companyId.isDefined
        && ( f.defaultFleet.isEmpty || f.defaultFleet.get != 1 ) ) )

    val fleetsWithActivity = {
      futListFleets.map(list => list.map(fleet => getFleetStatus(fleet.realm.get, fleet.id.get)
        .map(va => fleet.copy(activity = Some(va)))))
        .flatMap(l => Future.sequence(l))
    }

    if( withVehicles ) {
      return fleetsWithActivity.flatMap( list => Future.sequence{ list.map( fleet => getMetadataByFleet( fleet , withLastState ) ) } )
      //.flatMap( list => Future { list.sortWith( ( _.name.get < _.name.get ) ) } ) )
    } else {
      return fleetsWithActivity//.flatMap( list => Future { list.sortWith( ( _.name.get < _.name.get ) ) } )
    }
  }
}
