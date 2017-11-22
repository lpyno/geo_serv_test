package cl.redd.objects

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import cl.redd.geofences.GeofenceActor._
import spray.json._

object ReddJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{

  // Geofences
  // FilterPaginateSort
  implicit val fpsFormat = jsonFormat5( FilterPaginateSort )
  // Geofence
  implicit val geofenceFormat = jsonFormat18( Geofence )
  // save
  implicit val saveReqFormat  = jsonFormat2 ( SaveRequest )
  implicit val saveRespFormat = jsonFormat1 ( SaveResponse )
  // getById
  implicit val getByIdReqFormat   = jsonFormat3 ( GetByIdReq )
  implicit val getByIdRespFormat  = jsonFormat1 ( GetByIdResp )
  // getByCompany
  implicit val getByCompanyReqFormat  = jsonFormat3( GetByCompanyReq )
  implicit val getByCompanyRespFormat = jsonFormat1( GetByCompanyResp )
  // getFromCompanyByParameter
  implicit val getFromCompanyByParamReqFormat  = jsonFormat5 ( GetFromCompanyByParamReq )
  implicit val getFromCompanyByParamRespFormat = jsonFormat1 ( GetFromCompanyByParamResp )
  // getFromUserByParameter
  implicit val getFromUserByParamReqFormat  = jsonFormat5 ( GetFromUserByParamReq )
  implicit val getFromUserByParamRespFormat = jsonFormat1 ( GetFromUserByParamResp )
  // update
  implicit val updateReqFormat  = jsonFormat2 ( UpdateReq )
  implicit val updateRespFormat = jsonFormat1 ( UpdateResp )
  // delete
  implicit val deleteReqFormat  = jsonFormat2 ( DeleteReq )
  implicit val deleteRespFormat = jsonFormat1 ( DeleteResp )
  // ChildOld
//  implicit val childOldFormat      = jsonFormat8( ChildOld )
  // FunctionalityOld
  implicit val functionalityOldFormat = jsonFormat5( FunctionalityOld )
  // ProfileOld
  implicit val profileOldFormat = jsonFormat4( ProfileOld )
  // User Old
  implicit val userOldFormat = jsonFormat5( UserOld )
  // User Info
  implicit val userInfoFormat = jsonFormat13( UserInfo )
  // login
  implicit val authLoginReqFormat = jsonFormat4( AuthLoginReq )
  // UserPrefOld
  implicit val userPrefOldFormat = jsonFormat5( UserPrefOld )
  // MetadataUserOld
  implicit val metadataUserOldFormat = jsonFormat16( MetadataUserOld )
  // ValidateOld
  implicit val validateOldFormat = jsonFormat6( ValidateOld )

  implicit val authProfContentFormat = jsonFormat1( AuthProfContent )

  implicit val authProfSubChildFormat = jsonFormat8( AuthProfSubChild )

  implicit val authProfChildFormat = jsonFormat8( AuthProfChild )

  implicit val authProfileOldFormat = jsonFormat10( AuthProfileOld )
  // SelectProfile
  implicit val selectProfileFormat = jsonFormat2( SelectProfiles )

  implicit val vehicleFirstBLockFormat = jsonFormat21( VehicleFirstBlock )
  implicit val lastStateFormat = jsonFormat12( LastState )
  implicit val vehicleLastBlockFormat  = jsonFormat3( VehicleLastBlock )
  implicit val vehicleFormat = jsonFormat22( Vehicle )
  implicit val lastStateOldFormat = jsonFormat15( LastStateOld )
  implicit val vehicleOldFormat = jsonFormat19( VehicleOld )
  implicit val getVehiclesByIdFormat = jsonFormat4( GetVehiclesById )
  implicit val midFormatOldFormat = jsonFormat1( MidFormatOld )
  implicit val getListByMidsFormat = jsonFormat3( GetListByMids )
  implicit val vehicleActivityFormat = jsonFormat4( VehicleActivity )
  implicit val getFleetsByUserIdFormat = jsonFormat7( GetFleetsByUserId )
  implicit val filterOldFormat = jsonFormat4( FilterOld )
  implicit val sortOldFormat = jsonFormat2( SortOld )
  implicit val paginatedOldFormat = jsonFormat2( PaginatedOld )
  implicit val requestDataFormat = jsonFormat3( RequestData )
  implicit val fleetOldFormat = jsonFormat16( FleetOld )
  implicit val vehicleFromGetByCompanyFormat = jsonFormat18( VehicleFromGetByCompany )
  implicit val vehicleFromGetMetadataByFleetFormat = jsonFormat18( VehicleFromGetMetadataByFleet )
  implicit val fleetFormat = jsonFormat18( Fleet )

}