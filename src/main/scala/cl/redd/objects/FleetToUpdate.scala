package cl.redd.objects

case class FleetToUpdate (

                          name: String,
                          vehicles:Option[Vector[VehicleFleetUpdate]],
                          companies:Option[Vector[CompanyFleetUpdate]],
                          users:Option[Vector[UserFleetUpdate]],
                          generateReport: Boolean,
                          maxSpeed: Int,
                          startDay: Int,
                          startHour: String,
                          endDay: Int,
                          endHour: String,
                          inactiveDays: Int,
                          id: Int,
                          realm:String

                         )
