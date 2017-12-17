package cl.redd.objects

case class VehicleFromGetByUser (

                                 company     : Option[String] = None,
                                 idCompany   : Option[Int] = None,
                                 rut_Company : Option[String] = None,
                                 vehicleId   : Option[Int] = None,
                                 vin         : Option[String] = None,
                                 plateNumber : Option[String] = None,
                                 //plate_number: Option[String] = None,
                                 name        : Option[String] = None,
                                 engineTypeId    : Option[Int] = None,
                                 subVehicleTypeId: Option[Int] = None,
                                 createDate      : Option[Long] = None,
                                 validateDate    : Option[Long] = None,
                                 dischargeDate   : Option[Long] = None,
                                 lastActivityDate: Option[Long] = None,
                                 status      : Option[Boolean] = None,
                                 extraFields : Option[String] = None,
                                 _m          : Option[String] = None,
                                 deviceTypeId: Option[Int] = None,
                                 simcard     : Option[String] = None,
                                 //engineTypeName: Option[String] = None,
                                 deviceTypeName: Option[String] = None,
                                 //subVehicleTypeName: Option[String] = None,
                                 //vehicleTypeName: Option[String] = None,
                                 total: Option[Long] = None,
                                 realm: Option[String] = None,
                                 lastState: Option[LastStateOld] = None

                               )