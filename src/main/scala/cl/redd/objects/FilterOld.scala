package cl.redd.objects

case class FilterOld(

                    //filter      : Option[List[Map[String,String]]] = None,
                    filter      : Map[String,String],
                    userId      : Int,
                    userProfile : String,
                    companyId   : Int

                    )