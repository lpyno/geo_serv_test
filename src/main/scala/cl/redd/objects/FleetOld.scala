package cl.redd.objects

case class FleetOld (

                    id            : Option[Long] = None,
                    name          : Option[String] = None,
                    createDate    : Option[Long] = None,
                    companyId     : Option[Long] = None,
                    defaultFleet  : Option[Int] = None,
                    shared        : Option[Int] = None,
                    generateReport: Option[Int] = None,
                    companyName   : Option[String] = None,
                    maxSpeed      : Option[Int] = None,
                    startDay      : Option[Int] = None,
                    startHour     : Option[String] = None,
                    endDate       : Option[Int] = None,
                    endHour       : Option[String] = None,
                    inactiveDays  : Option[Int] = None,
                    total         : Option[Int] = None,
                    realm         : Option[String] = None

                   )
