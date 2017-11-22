package cl.redd.objects

case class VehicleFromGetByCompany (

                                    company     : Option[String]=None,
                                    idCompany   : Option[Int]=None,
                                    vehicleId   : Option[Int]=None,
                                    plate_number: Option[String]=None,
                                    plateNumber : Option[String]=None,
                                    status      : Option[Boolean]=None,
                                    name        : Option[String]=None,
                                    vin         : Option[String]=None,
                                    extraFields : Option[String]=None,
                                    _m          : Option[String]=None,
                                    simcard     : Option[String]=None,
                                    engineTypeName    : Option[String]=None,
                                    subVehicleTypeName: Option[String]=None,
                                    vehicleTypeName   : Option[String]=None,
                                    deviceTypeId      : Option[Int]=None,
                                    deviceTypeName    : Option[String]=None,
                                    realm             : Option[String]=None,
                                    lastState         : Option[LastStateOld]=None

                                   )
