package cl.redd.objects

case class GetFleetsByUserId (

                             realm        :Option[String] = None ,
                             userId       :Option[Int] = None ,
                             companyId    :Option[Int] = None ,
                             userProfile  :Option[String] = None,
                             withVehicles :Option[Boolean] = None,
                             withLastState:Option[Boolean] = None,
                             fps          :Option[FilterPaginateSort] = None

                             )
