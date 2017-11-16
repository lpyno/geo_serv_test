package cl.redd.vehicles

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

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class VehiclesController(implicit val actor:ActorSystem, implicit val materializer: ActorMaterializer, implicit val ec:ExecutionContext ) {

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

  private def vehToOld( vehicle : Vehicle ): VehicleOld = {

    val vehicleOld =

      new VehicleOld(
        simcard         = vehicle.vehicleFirstBlock.get.simCard,
        plateNumber     = vehicle.vehicleFirstBlock.get.plateNumber,
        engineTypeName  = vehicle.vehicleFirstBlock.get.engineTypeName,
        _m              = vehicle.vehicleFirstBlock.get.imei,
        validateDate    = vehicle.vehicleFirstBlock.get.validateDate,
        companyId       = vehicle.vehicleFirstBlock.get.companyId,
        engineTypeId    = vehicle.vehicleFirstBlock.get.engineTypeId,
        subVehicleTypeName = vehicle.vehicleFirstBlock.get.subVehicleTypeName,
        dischargeDate      = vehicle.vehicleFirstBlock.get.dischargeDate,
        subVehicleTypeId   = vehicle.vehicleFirstBlock.get.subVehicleTypeId,
        name               = vehicle.vehicleFirstBlock.get.name,
        vin                = vehicle.vehicleFirstBlock.get.vin,
        id                 = vehicle.vehicleFirstBlock.get.id,
        plate_number    = vehicle.vehicleFirstBlock.get.plateNumber,
        vehicleTypeName = vehicle.vehicleFirstBlock.get.vehicleTypeName,
        createDate      = vehicle.vehicleFirstBlock.get.createDate,
        status          = vehicle.vehicleFirstBlock.get.status,
        extraFields     = vehicle.vehicleLastBlock.get.extraFields,

        lastState = Some( new LastStateOld (
          date      = vehicle.vehicleLastBlock.get.lastState.get.date,
          eventId   = vehicle.vehicleLastBlock.get.lastState.get.eventId,
          lng       = vehicle.vehicleLastBlock.get.lastState.get.longitude,
          odometer  = vehicle.vehicleLastBlock.get.lastState.get.odometer,
          hourmeter = vehicle.vehicleLastBlock.get.lastState.get.hourmeter,
          latitude  = vehicle.vehicleLastBlock.get.lastState.get.latitude,
          alt       = vehicle.vehicleLastBlock.get.lastState.get.altitude,
          azimuth   = vehicle.vehicleLastBlock.get.lastState.get.azimuth,
          speed     = vehicle.vehicleLastBlock.get.lastState.get.speed,
          _m        = vehicle.vehicleFirstBlock.get.imei,
          geotext   = vehicle.vehicleLastBlock.get.lastState.get.geotext,
          _t        = vehicle.vehicleLastBlock.get.lastState.get.date,
          odo       = vehicle.vehicleLastBlock.get.lastState.get.odometer,
          lat       = vehicle.vehicleLastBlock.get.lastState.get.latitude,
          longitude = vehicle.vehicleLastBlock.get.lastState.get.longitude ) )
      )

    vehicleOld

  }

  /*private def vehOldToNew ( vehicleOld: VehicleOld ): Vehicle = {

    val tmpFirstBlock =
      new VehicleFirstBlock( id             = vehicleOld.id,
                             name           = vehicleOld.name,
                             activityStatus = None, // TODO
                             companyName    = None,    // TODO
                             companyId      = vehicleOld.companyId,
                             rutCompany     = None,     // TODO
                             vin            = vehicleOld.vin,
                             plateNumber    = if ( vehicleOld.plate_number.nonEmpty ) vehicleOld.plate_number
                                              else if ( vehicleOld.plateNumber.nonEmpty ) vehicleOld.plateNumber
                                              else
      )



    val vehicle =
      new Vehicle( vehicleFirstBlock = Some( tmpFirstBlock ) )

  }*/

  private def saveVehicle( vehicle:Vehicle ) : Future[Vehicle] = {

    val serviceHost = ReddDiscoveryClient.getNextIpByName( ServicesEnum.METADATAVEHICLE.toString )

    val url = s"$serviceHost/metadata/vehicle/update"

    val vehicleOld:VehicleOld = new VehicleOld( simcard         = vehicle.vehicleFirstBlock.get.simCard,
                                                plateNumber     = vehicle.vehicleFirstBlock.get.plateNumber,
                                                engineTypeName  = vehicle.vehicleFirstBlock.get.engineTypeName,
                                                _m              = vehicle.vehicleFirstBlock.get.imei,
                                                validateDate    = vehicle.vehicleFirstBlock.get.validateDate,
                                                companyId       = vehicle.vehicleFirstBlock.get.companyId,
                                                engineTypeId    = vehicle.vehicleFirstBlock.get.engineTypeId,
                                                subVehicleTypeName = vehicle.vehicleFirstBlock.get.subVehicleTypeName,
                                                dischargeDate      = vehicle.vehicleFirstBlock.get.dischargeDate,
                                                subVehicleTypeId   = vehicle.vehicleFirstBlock.get.subVehicleTypeId,
                                                name               = vehicle.vehicleFirstBlock.get.name,
                                                vin                = vehicle.vehicleFirstBlock.get.vin,
                                                id                 = vehicle.vehicleFirstBlock.get.id,
                                                plate_number    = vehicle.vehicleFirstBlock.get.plateNumber,
                                                vehicleTypeName = vehicle.vehicleFirstBlock.get.vehicleTypeName,
                                                createDate      = vehicle.vehicleFirstBlock.get.createDate,
                                                status          = vehicle.vehicleFirstBlock.get.status,
                                                extraFields     = vehicle.vehicleLastBlock.get.extraFields,

                                                lastState = Some( new LastStateOld (
                                                                              date      = vehicle.vehicleLastBlock.get.lastState.get.date,
                                                                              eventId   = vehicle.vehicleLastBlock.get.lastState.get.eventId,
                                                                              lng       = vehicle.vehicleLastBlock.get.lastState.get.longitude,
                                                                              odometer  = vehicle.vehicleLastBlock.get.lastState.get.odometer,
                                                                              hourmeter = vehicle.vehicleLastBlock.get.lastState.get.hourmeter,
                                                                              latitude  = vehicle.vehicleLastBlock.get.lastState.get.latitude,
                                                                              alt       = vehicle.vehicleLastBlock.get.lastState.get.altitude,
                                                                              azimuth   = vehicle.vehicleLastBlock.get.lastState.get.azimuth,
                                                                              speed     = vehicle.vehicleLastBlock.get.lastState.get.speed,
                                                                              _m        = vehicle.vehicleFirstBlock.get.imei,
                                                                              geotext   = vehicle.vehicleLastBlock.get.lastState.get.geotext,
                                                                              _t        = vehicle.vehicleLastBlock.get.lastState.get.date,
                                                                              odo       = vehicle.vehicleLastBlock.get.lastState.get.odometer,
                                                                              lat       = vehicle.vehicleLastBlock.get.lastState.get.latitude,
                                                                              longitude = vehicle.vehicleLastBlock.get.lastState.get.longitude ) )
                                                )

    //  val entity = HttpEntity(MediaTypes.`application/json`, vehicle.toJson.toString())

    val entity = HttpEntity(MediaTypes.`application/json`, vehicleOld.toJson.toString())

    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( method = HttpMethod.custom( "PUT" ) , uri = url , entity = entity ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[Vehicle]
    }

  }

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
    val future:Future[HttpResponse] = Http().singleRequest( HttpRequest( HttpMethods.POST , url , hds , body ) )

    future.flatMap {
      case HttpResponse( StatusCodes.OK , _ , entity , _ ) => Unmarshal(entity).to[List[VehicleOld]]
    }

  }

}
