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
import scala.util.{Failure, Success, Try}

class FleetsController( implicit val system : ActorSystem,
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
      //http://192.168.173.166:42100/metadata/vehicle/fleet/getMetadataFleetStatus?realm=rslite&id=58
      val url = s"$serviceHost/metadata/vehicle/fleet/getMetadataFleetStatus?realm=$realm&id=$id"
      println(s"url... $url")
      val hds = List(RawHeader("Accept", "application/json"))
      val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, url, hds))
      future.flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) => {
          println(s"fleet status [$id] OK!... $entity")
          Unmarshal(entity).to[VehicleActivity]
        }
      }
    }
    return va
  }

  def getFleetsByUserId(params: Option[GetFleetsByUserId] = None ) : Future[List[Fleet]] = {

    val filterOld = new FilterOld( params.get.fps.get.filterParams , params.get.userId , params.get.userProfile , params.get.companyId )
    val sortOld = new SortOld( params.get.fps.get.sortParam , params.get.fps.get.sortOrder )
    val paginatedOld = new PaginatedOld( params.get.fps.get.pagLimit , params.get.fps.get.pagOffset )
    val reqData = new RequestData( Some(filterOld) , Some(sortOld) , Some(paginatedOld) )

    val futListFleets = {

      val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString )
      val url         = s"$serviceHost/metadata/vehicle/fleet/getMetadataByUser?realm=${params.get.realm.get}"
      val hds         = List(RawHeader("Accept", "application/json"))
      val body        = HttpEntity( ContentTypes.`application/json`, reqData.toJson.toString() )

      val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

      future.flatMap { case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[List[FleetOld]] }

    }.flatMap( list => Future { list.map( fleet => fleetOldToNew( fleet ) ) } )
      .map( l => l.filter( f => f.realm.isDefined     &&
                                f.id.isDefined        &&
                                f.companyId.isDefined &&
                                ( f.defaultFleet.isEmpty || f.defaultFleet.get != 1 ) ) )
    // add activity status
    val fleetsWithActivity = {
      futListFleets.map(list => list.map(fleet => getFleetStatus(fleet.realm.get, fleet.id.get)
        .map(va => fleet.copy(activity = Some(va)))))
        .flatMap(l => Future.sequence(l))
    }
    // add vehicles/laststate
    if( params.get.withVehicles.get ) {
      return fleetsWithActivity.flatMap( list => Future.sequence{ list.map( fleet => getMetadataByFleet( fleet , params.get.withLastState.get ) ) } )
              //.flatMap( list => Future { list.sortWith( ( _.name.get < _.name.get ) ) } ) )
    } else {
      return fleetsWithActivity//.flatMap( list => Future { list.sortWith( ( _.name.get < _.name.get ) ) } )
    }

  }

  private def getMetadataByFleet( fleet:Fleet , withLastState:Boolean ):Future[Fleet] = {

    val newFleet = {
      val serviceHost = ReddDiscoveryClient.getNextIpByName(ServicesEnum.METADATAVEHICLE.toString)
      val url = s"$serviceHost/metadata/vehicle/getMetadataByFleet?" +
                s"realm=${fleet.realm.get}&companyId=${fleet.companyId.get}&fleetId=${fleet.id.get}&lastState=$withLastState"
      println( s"url... $url" )
      val hds = List(RawHeader("Accept", "application/json"))
      val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, url, hds))
      future.flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) => println(s"getMetadataByFleet [${fleet.id.get}] OK!... $entity");
          Unmarshal(entity).to[List[VehicleFromGetMetadataByFleet]]
      }
    }

    // print vehicles xceived
    //newFleet.map( list => list.foreach( println( _ ) ) )

    val newFleetWithNewVehicles = newFleet.map( list => list.map( v => vehOldToNew( v ) ) )


    return newFleetWithNewVehicles.map( vList => fleet.copy( fleetVehicles = Some(vList) ) )

  }

  def lastStateNew( lastState:LastStateOld ):Option[LastState] = {

      return Some( new LastState(
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
      ) )

  }

  def vehOldToNew( vo:VehicleFromGetMetadataByFleet ):Vehicle = {

      return new Vehicle (
        id                = if( vo.vehicleId.isDefined ) vo.vehicleId else None,
        name              = if( vo.name.isDefined ) vo.name else None,
        /*activityStatus,*/
        companyId         = if( vo.companyId.isDefined ) vo.companyId else None,
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
        lastState          = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( new LastState ) )

  }

}
