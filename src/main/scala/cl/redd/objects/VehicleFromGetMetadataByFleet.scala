package cl.redd.objects

case class VehicleFromGetMetadataByFleet (

                                         companyId   : Option[Int]=None,
                                         fleetId     : Option[Int]=None,
                                         fleetName   : Option[String]=None,
                                         vehicleId   : Option[Int]=None,
                                         plateNumber : Option[String]=None,
                                         name        : Option[String]=None,
                                         vin         : Option[String]=None,
                                         _m          : Option[String]=None,
                                         imei           : Option[String]=None,
                                         simcard        : Option[String]=None,
                                         createDate     : Option[Long]=None,
                                         validateDate   : Option[Long]=None,
                                         dischargeDate  : Option[Long]=None,
                                         extraFields    : Option[String]=None,
                                         realm          : Option[String]=None,
                                         deviceTypeId      : Option[Int]=None,
                                         deviceTypeName    : Option[String]=None,
                                         lastState         : Option[LastStateOld]=None

                                         )
/*
{
    "companyId": 144,
    "fleetId": 58,
    "fleetName": "GL Default",
    "vehicleId": 1,
    "plateNumber": "ghyp25",
    "name": "ghyp25",
    "vin": "ghyp25",
    "_m": "861074022098480",
    "imei": "861074022098480",
    "simcard": "+56952073363",
    "createDate": 1393015046000,
    "validateDate": 1506371210000,
    "dischargeDate": null,
    "extraFields": null,
    "realm": "rslite",
    "deviceTypeId": 3,
    "deviceTypeName": "Queclink GV200",
    "lastState": {
      "date": 1471896187000,
      "eventId": 141,
      "lng": -69.528339,
      "odometer": 138022.7,
      "hourmeter": 35003352,
      "latitude": -20.945564,
      "alt": 933.6,
      "azimuth": 60,
      "speed": 10.9,
      "_m": "861074022098480",
      "geotext": "5 NORTE 1168,Pozo Almonte,De Tarapaca ,Chile",
      "_t": 1471896187000,
      "odo": 138022.7,
      "lat": -20.945564,
      "longitude": -69.528339
    }
  }
 */