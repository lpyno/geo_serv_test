package cl.redd.objects

case class FilterOld(

                    filter      : Option[List[Tuple2[String,String]]] = None,
                    userId      : Option[Int] = None,
                    userProfile : Option[String] = None,
                    companyId   : Option[Int] = None

                    )