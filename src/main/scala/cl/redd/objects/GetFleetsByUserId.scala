package cl.redd.objects

case class GetFleetsByUserId (

                             realm        :String,
                             userId       :Int,
                             companyId    :Int,
                             userProfile  :String,
                             withVehicles :Boolean,
                             withLastState:Boolean,
                             fps          :FilterPaginateSort

                             )
