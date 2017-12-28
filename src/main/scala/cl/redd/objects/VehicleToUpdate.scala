package cl.redd.objects

case class VehicleToUpdate (

                         id             : Int,
                         name           : String,
                         plateNumber    : String,
                         vin            : String,
                         subVehicleType : Long,
                         engineType     : Long,
                         companyId      : Long,
                         extraFields    : String,
                         validate       : Int,
                         validateDate   : Long,
                         downDate       : Option[Long]=None,
                         requestBy      : Option[String]=None,
                         realm          : String

                         )
/*
"id": 8445,
"name": "lp test",
"plateNumber":"REDD18",
"vin": "REDD18 VIN",
"subVehicleType":1,
"engineType": 1,
"companyId": 144,
"extraFields": "{ extraFields as json }",
"validate":1,
"validateDate":1514408021000,
"downDate":null,
"requestBy":null,
"realm":"rslite"
*/