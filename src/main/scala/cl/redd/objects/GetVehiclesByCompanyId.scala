package cl.redd.objects

case class GetVehiclesByCompanyId (

                                  realm:String,
                                  companyId:Int,
                                  withLastState:Boolean,
                                  fps:FilterPaginateSort

                                  )
