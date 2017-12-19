package cl.redd.objects

case class VehicleToUpdate (

                         realm          : Option[String]=None,
                         name           : Option[String]=None,
                         plateNumber    : Option[String]=None,
                         vin            : Option[String]=None,
                         subVehicleType : Option[Long]=None,
                         engineType     : Option[Long]=None,
                         vehicleType    : Option[Long]=None,
                         companyId      : Option[Long]=None,
                         validateDate   : Option[Long]=None,
                         _m             : Option[String]=None,
                         id             : Option[Long]=None,
                         freeText       : Option[String]=None

                         )
