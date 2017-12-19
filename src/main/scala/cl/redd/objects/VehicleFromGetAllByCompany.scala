package cl.redd.objects

case class VehicleFromGetAllByCompany (

                                      id            : Option[Int] = None,
                                      name          : Option[String] = None,
                                      plateNumber   : Option[String] = None,
                                      createDate    : Option[Long] = None,
                                      validateDate  : Option[Long] = None,
                                      dischargeDate : Option[Long] = None,
                                      validate      : Option[Boolean] = None,
                                      vin           : Option[String] = None,
                                      companyId     : Option[Int] = None,
                                      subVehicleTypeId : Option[Int] = None,
                                      engineTypeId  : Option[Int] = None,
                                      extraFields   : Option[String] = None,
                                      engineTypeName      : Option[String] = None,
                                      subVehicleTypeName  : Option[String] = None,
                                      vehicleTypeName     : Option[String] = None,
                                      _m              : Option[String] = None,
                                      simCardPhone    : Option[String] = None,
                                      deviceTypeName  : Option[String] = None,
                                      deviceTypeId    : Option[Int] = None,
                                      total : Option[Long] = None,
                                      realm : Option[String] = None,
                                      lastState: Option[LastStateOld] = None

                                      )
