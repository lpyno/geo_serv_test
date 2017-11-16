package cl.redd.objects

case class GetVehiclesById( realm         : Option[String]    = None,
                            vehicleIds    : Option[List[Int]] = None,
                            withLastState : Option[Boolean] = None,
                            fps           : Option[FilterPaginateSort] = None )
