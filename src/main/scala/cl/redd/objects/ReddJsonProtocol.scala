package cl.redd.objects

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import cl.redd.geofences.GeofenceActor._
import spray.json._

object ReddJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{

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

}