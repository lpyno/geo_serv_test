package cl.redd.objects

case class StreamRequest (

                         operation:String,
                         realm:String,
                         companyId:Int,
                         ids:List[Int]

                         )
