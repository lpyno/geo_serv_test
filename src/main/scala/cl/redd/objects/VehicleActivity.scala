package cl.redd.objects

case class VehicleActivity (
                            id     : Option[Int] = None,
                            status : Option[Map[String,Int]] = None
                             /* actives			 : Option[Int] = None,
                                inactives		 : Option[Int] = None,
                                inactives24h : Option[Int] = None,
                                moving			 : Option[Int] = None*/
                           )
