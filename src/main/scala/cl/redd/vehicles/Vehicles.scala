package cl.redd.vehicles

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.objects.RequestResponses.GetVehiclesByUserId
import cl.redd.objects._
import cl.tastets.life.objects.ServicesEnum
import spray.json._
import cl.redd.objects.ReddJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

class Vehicles(implicit val actor:ActorSystem, implicit val materializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

  /** 4.1 "/save", POST method */

  def save( vehicle:Option[Vehicle] = None ) : Vehicle = ???

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

  def getVehiclesById( realm:Option[String] = None, ids:Option[List[Int]] = None, withLastState:Option[Boolean] = None , fps:Option[FilterPaginateSort] = None ) : List[VehicleOld] = ???

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
    val isAdmin = fps.filterParams.get("userProfile").get.equalsIgnoreCase( "ADMIN" )
    val strBody =
      s"""{"filter":{"userProfile":"${fps.filterParams.get("userProfile").get}","userId":${fps.filterParams.get("userId").get}},"sort":{"field":"${fps.sortParam}", "order":${fps.sortOrder}},"paginated":{"limit":${fps.pagLimit},"offset":${fps.pagOffset}}}"""
        .stripMargin
    val body = HttpEntity( MediaTypes.`application/json`, strBody )
    val futHttpResp = Http().singleRequest( HttpRequest( HttpMethods.POST, url, hds, body ) )

    val rv = futHttpResp.flatMap{
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => {
        if ( isAdmin ){
          Unmarshal(entity).to[List[VehicleFromGetByUserAdmin]]
        } else{
          // change status field from boolean to int to match the admin vehicle format
          Unmarshal(entity).to[List[VehicleFromGetByUser]].map( list => list.map( v => statusToAdminFormat( v )))
        }
      }.map( list => list.map( v => newFromGetByUser( v ) ) )
        .map( list => list.map( v => getActivityStatus( v ) ) )
    }
    rv

  }

  def getByCompanyId( realm:String, companyId:Int, withLastState:Boolean, fps:FilterPaginateSort ): Future[List[Vehicle]] = {
    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString() )
    val url = s"$serviceHost/metadata/vehicle/getAllByCompany?realm=$realm&companyId=$companyId&lastState=$withLastState&revertUnsuscribe=false"
    val hds = List(RawHeader("Accept", "application/json"))
    // filter params supported: "id" & "ignoredIds" (as list []), "companyId", "name", "plateNumber", "vin", "validate", "fleetId", "imei"
    val listFilters = fps.filterParams
      .map( tuple => s"""{"${tuple._1}":"${tuple._2}"}""" )
      .toString
      .replace("List(","")
      .replace(")","")
      .replace(" ","")
    // sort params supported: name, lastActivityDate
    val strBody = s"""{"filter":{"filter":[$listFilters],"sort":"${fps.sortParam}"},"paginated":{"limit":${fps.pagLimit},"offset":${fps.pagOffset}}}"""
      .stripMargin
    val body = HttpEntity( MediaTypes.`application/json`, strBody )

    val futHttpResp = Http().singleRequest( HttpRequest( HttpMethods.POST, url, hds, body ) )
    val rv = futHttpResp.flatMap{
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[List[VehicleFromGetAllByCompany]]
    }.map( list => list.map( v => newFromGetAllByCompany( v )))
      .map( list => list.map( v => getActivityStatus( v )))
    rv
  }

  def update( vehicleToUpdate: VehicleToUpdate ):Future[VehicleToUpdate] = {

    implicit val fmt = jsonFormat13(VehicleToUpdate)
    val serviceHost = ReddDiscoveryClient.getNextIpByName(ServicesEnum.METADATAVEHICLE.toString())

    val url = s"$serviceHost/metadata/vehicle/update"
    val hds = List(RawHeader("Accept", "application/json"))
    val body = HttpEntity(MediaTypes.`application/json`, vehicleToUpdate.toJson.toString)
    println( s"update body: $body" )
    val futHttpResp = Http().singleRequest(HttpRequest(HttpMethods.PUT, url, hds, body))

    futHttpResp.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => Unmarshal(entity).to[VehicleToUpdate]
    }
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
      lastState       = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( LastState() )
    )
  }

  def statusToAdminFormat( v:VehicleFromGetByUser ):VehicleFromGetByUserAdmin = {

    VehicleFromGetByUserAdmin(
      company     = v.company,
      idCompany   = v.idCompany,
      rut_Company = v.rut_Company,
      vehicleId   = v.vehicleId,
      vin         = v.vin,
      plateNumber = v.plateNumber,
      //plate_number: Option[String] = None,
      name        = v.name,
      engineTypeId    = v.engineTypeId,
      subVehicleTypeId= v.subVehicleTypeId,
      createDate      = v.createDate,
      validateDate    = v.validateDate,
      dischargeDate   = v.dischargeDate,
      lastActivityDate = v.lastActivityDate,
      status      = if ( v.status.get ) Some(1) else Some(0),
      extraFields = v.extraFields,
      _m          = v._m,
      deviceTypeId = v.deviceTypeId,
      simcard     = v.simcard,
      //engineTypeName: Option[String] = None,
      deviceTypeName = v.deviceTypeName,
      //subVehicleTypeName: Option[String] = None,
      //vehicleTypeName: Option[String] = None,
      total = v.total,
      realm = v.realm,
      lastState = v.lastState
    )

  }

  def newFromGetByUser( vo:VehicleFromGetByUserAdmin ): Vehicle = {

    Vehicle (
      id                = if( vo.vehicleId.isDefined ) vo.vehicleId else None,
      name              = if( vo.name.isDefined ) vo.name else None,
      activityStatus    = None, // filled later
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
      lastState       = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( LastState() )
    )

  }

  def newFromGetAllByCompany( vo:VehicleFromGetAllByCompany ): Vehicle = {

    Vehicle (
      id                = if( vo.id.isDefined ) vo.id else None,
      name              = if( vo.name.isDefined ) vo.name else None,
      activityStatus    = None, // filled later
      vin               = if( vo.vin.isDefined ) vo.vin else None,
      plateNumber       = if( vo.plateNumber.isDefined ) vo.plateNumber else None,
      engineTypeId      = if( vo.engineTypeId.isDefined ) vo.engineTypeId else None,
      subVehicleTypeId  = if( vo.subVehicleTypeId.isDefined ) vo.subVehicleTypeId else None,
      createDate        = if( vo.createDate.isDefined ) vo.createDate else None,
      validateDate      = if( vo.validateDate.isDefined ) vo.validateDate else None,
      dischargeDate     = if( vo.dischargeDate.isDefined ) vo.dischargeDate else None,
      imei              = if( vo._m.isDefined ) vo._m else None,
      deviceTypeId      = if( vo.deviceTypeId.isDefined ) vo.deviceTypeId else None,
      simCard           = if( vo.simCardPhone.isDefined ) vo.simCardPhone else None,
      engineTypeName    = if( vo.engineTypeName.isDefined ) vo.engineTypeName else None,
      deviceTypeName    = if( vo.deviceTypeName.isDefined ) vo.deviceTypeName else None,
      subVehicleTypeName = if( vo.subVehicleTypeName.isDefined ) vo.subVehicleTypeName else None,
      vehicleTypeName    = if( vo.vehicleTypeName.isDefined ) vo.vehicleTypeName else None,
      realm           = if( vo.realm.isDefined ) vo.realm else None,
      total           = if ( vo.total.isDefined ) vo.total else None,
      extraFields     = if( vo.extraFields.isDefined ) vo.extraFields else None,
      lastState       = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( LastState() )
    )

  }

/*  def newFromGetAllByCompany( vo:VehicleFromGetAllByCompany ): Vehicle = {

    Vehicle (
      id                = if( vo.id.isDefined ){ println(s"id: ${vo.id.get}"); vo.id } else None,
      name              = if( vo.name.isDefined ){ println(s"name: ${vo.name.get}");vo.name } else None,
      activityStatus    = None, // filled later
      vin               = if( vo.vin.isDefined ){ println(s"vin: ${vo.vin.get}");vo.vin } else None,
      plateNumber       = if( vo.plateNumber.isDefined ){ println(s"plateNumber: ${vo.plateNumber.get}");vo.plateNumber } else None,
      engineTypeId      = if( vo.engineTypeId.isDefined ){ println(s"engineTypeId: ${vo.engineTypeId.get}");vo.engineTypeId } else None,
      subVehicleTypeId  = if( vo.subVehicleTypeId.isDefined ){ println(s"subVehicleTypeId: ${vo.subVehicleTypeId.get}");vo.subVehicleTypeId } else None,
      createDate        = if( vo.createDate.isDefined ) { println(s"createDate: ${vo.createDate.get}");vo.createDate } else None,
      validateDate      = if( vo.validateDate.isDefined ) { println(s"validateDate: ${vo.validateDate.get}");vo.validateDate } else None,
      dischargeDate     = if( vo.dischargeDate.isDefined ) { println(s"dischargeDate: ${vo.dischargeDate.get}");vo.dischargeDate } else None,
      imei              = if( vo._m.isDefined ) { println(s"_m : ${vo._m.get}");vo._m } else None,
      deviceTypeId      = if( vo.deviceTypeId.isDefined ) { println(s"deviceTypeId : ${vo.deviceTypeId.get}");vo.deviceTypeId } else None,
      simCard           = if( vo.simCardPhone.isDefined ) { println(s"simCardPhone : ${vo.simCardPhone.get}");vo.simCardPhone } else None,
      engineTypeName    = if( vo.engineTypeName.isDefined ) { println(s"engineTypeName : ${vo.engineTypeName.get}");vo.engineTypeName } else None,
      deviceTypeName    = if( vo.deviceTypeName.isDefined ) { println(s"deviceTypeName : ${vo.deviceTypeName.get}");vo.deviceTypeName } else None,
      subVehicleTypeName = if( vo.subVehicleTypeName.isDefined ) { println(s"subVehicleTypeName : ${vo.subVehicleTypeName.get}");vo.subVehicleTypeName } else None,
      vehicleTypeName    = if( vo.vehicleTypeName.isDefined ) { println(s"vehicleTypeName : ${vo.vehicleTypeName.get}");vo.vehicleTypeName } else None,
      realm           = if( vo.realm.isDefined ) { println(s"realm : ${vo.realm.get}");vo.realm } else None,
      total           = if ( vo.total.isDefined ) { println(s"total : ${vo.total.get}");vo.total } else None,
      extraFields     = if( vo.extraFields.isDefined ) { println(s"extraFields : ${vo.extraFields.get}");vo.extraFields } else None,
      lastState       = if( vo.lastState.isDefined ) lastStateNew( vo.lastState.get ) else Some( LastState() )
    )

  }*/

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

  def parseGetByCompanyParams( request: String ): Option[GetVehiclesByCompanyId] = {

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
    Some(
      GetVehiclesByCompanyId(realm, companyId, withLastState, FilterPaginateSort(
        filterParams,
        fps.get("pagLimit").get.convertTo[Int],
        fps.get("pagOffset").get.convertTo[Int],
        fps.get("sortOrder").get.convertTo[Int],
        fps.get("sortParam").get.convertTo[String]
      )
      )
    )
  }

  def validateUpdateParams( request : String ):Option[VehicleToUpdate] = {

    val jsObject = request
      .parseJson
      .asJsObject("Invalid Json")

    val id = if (jsObject.fields.get("id").isDefined) {
      jsObject.fields.get("id").get.convertTo[Int]
    } else {
      return None
    }
    val name = if (jsObject.fields.get("name").isDefined) {
      jsObject.fields.get("name").get.convertTo[String]
    } else {
      return None
    }
    val plateNumber= if (jsObject.fields.get("plateNumber").isDefined) {
      jsObject.fields.get("plateNumber").get.convertTo[String]
    } else {
      return None
    }
    val vin = if (jsObject.fields.get("vin").isDefined) {
      jsObject.fields.get("vin").get.convertTo[String]
    } else {
      return None
    }
    val subVehicleType = if (jsObject.fields.get("subVehicleType").isDefined) {
      jsObject.fields.get("subVehicleType").get.convertTo[Int]
    } else {
      return None
    }
    val engineType = if (jsObject.fields.get("engineType").isDefined) {
      jsObject.fields.get("engineType").get.convertTo[Int]
    } else {
      return None
    }
    val companyId = if (jsObject.fields.get("companyId").isDefined) {
      jsObject.fields.get("companyId").get.convertTo[Int]
    } else {
      return None
    }
    val extraFields = if (jsObject.fields.get("extraFields").isDefined) {
      jsObject.fields.get("extraFields").get.convertTo[String]
    } else {
      return None
    }
    val validate = if (jsObject.fields.get("validate").isDefined) {
      jsObject.fields.get("validate").get.convertTo[Int]
    } else {
      return None
    }
    val validateDate = if (jsObject.fields.get("validateDate").isDefined) {
      jsObject.fields.get("validateDate").get.convertTo[Long]
    } else {
      return None
    }
    val downDate:Option[Long] = if (jsObject.fields.get("downDate").isDefined) {
      jsObject.fields.get("downDate").get.convertTo[Option[Long]]
    } else {
      None
    }
    val requestBy = if (jsObject.fields.get("requestBy").isDefined) {
      jsObject.fields.get("requestBy").get.convertTo[Option[String]]
    } else {
      None
    }
    val realm = if (jsObject.fields.get("realm").isDefined) {
      jsObject.fields.get("realm").get.convertTo[String]
    } else {
      return None
    }
    Some( VehicleToUpdate(
      id, name, plateNumber, vin, subVehicleType, engineType, companyId, extraFields, validate, validateDate,
      downDate, requestBy, realm
    ) )
  }

}
