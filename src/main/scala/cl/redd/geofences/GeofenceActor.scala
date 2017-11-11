package cl.redd.geofences

import akka.actor.{Actor, ActorLogging}
import cl.redd.objects.{Geofence, FilterPaginateSort}

object GeofenceActor {

  /** save */
  case class SaveRequest ( realm:String , geofence:Geofence )
  case class SaveResponse( geofence:Geofence )
  /** getById */
  case class GetByIdReq ( realm:String , geofenceIds:List[Int] , fps:FilterPaginateSort )
  case class GetByIdResp( geofences:List[Geofence] )
  /** getByCompany */
  case class GetByCompanyReq ( realm:String , companyId:Int , fps:FilterPaginateSort )
  case class GetByCompanyResp( geofences:List[Geofence] )
  /** getFromCompanyByParameter */
  case class GetFromCompanyByParamReq (realm:String , companyId:Int , paramName:String/*Geofence.{param}*/ , paramValue:String , fps:FilterPaginateSort )
  case class GetFromCompanyByParamResp( geofences:List[Geofence] )
  /** getFromUserByParameter */
  case class GetFromUserByParamReq ( realm:String , userId:Int , paramName:String/*Geofence.{param}*/ , paramValue:String , fps:FilterPaginateSort )
  case class GetFromUserByParamResp( geofences:List[Geofence] )
  /** update */
  case class UpdateReq ( realm:String , updatedGeofence:Geofence )
  case class UpdateResp( updGeofence:Geofence )
  /** delete */
  case class DeleteReq ( realm:String , geofenceId:Int )
  case class DeleteResp( geofence:Geofence )
  /** login */

}

class GeofenceActor extends Actor with ActorLogging {
  import GeofenceActor._

  def receive: Receive = {
    case request: SaveRequest => { sender ! SaveResponse( request.geofence ) }

  }
}


