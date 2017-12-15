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