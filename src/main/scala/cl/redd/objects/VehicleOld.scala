package cl.redd.objects

case class VehicleOld (

                      simcard        : Option[String] = None,
                      plateNumber    : Option[String] = None,
                      engineTypeName : Option[String] = None,
                      _m             : Option[String] = None,
                      validateDate   : Option[Long]   = None,
                      companyId      : Option[Int]    = None,
                      engineTypeId   : Option[Int]    = None,
                      subVehicleTypeName : Option[String] = None,
                      dischargeDate      : Option[Long]   = None,
                      subVehicleTypeId   : Option[Int]    = None,
                      name           : Option[String] = None,
                      vin            : Option[String] = None,
                      id             : Option[Int] = None,
                      plate_number   : Option[String] = None,
                      vehicleTypeName: Option[String] = None,
                      createDate     : Option[Long] = None,
                      status         : Option[Boolean] = None,
                      extraFields    : Option[String] = None,
                      lastState      : Option[LastStateOld] = None

                      )

/*

  "simcard": "+56957785261",
  "plateNumber": "GHJF-34",
  "lastState": {
    "date": 1471896270000,
    "eventId": 141,
    "lng": -68.331978,
    "odometer": 148964.6,
    "hourmeter": 12176986,
    "latitude": -23.490961,
    "alt": 2306.5,
    "azimuth": 259,
    "speed": 18.7,
    "_m": "861074022157294",
    "geotext": "Camino interior Mina Sociedad chilena del Litio,San Pedro de Atacama,De Antofagasta ,Chile",
    "_t": 1471896270000,
    "odo": 148964.6,
    "lat": -23.490961,
    "longitude": -68.331978
  },
  "engineTypeName": "Default",
  "_m": "861074022157294",
  "validateDate": 1393015271000,
  "companyId": 144,
  "engineTypeId": 1,
  "subVehicleTypeName": "Default",
  "dischargeDate": null,
  "subVehicleTypeId": 1,
  "name": "GHJF-34",
  "vin": "MR0FR22G5E0765882",
  "id": 254,
  "plate_number": "GHJF-34",
  "vehicleTypeName": "Default",
  "createDate": 1393015271000,
  "status": true,
  "extraFields": null

 */