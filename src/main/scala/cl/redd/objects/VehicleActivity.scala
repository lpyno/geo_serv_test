package cl.redd.objects

case class VehicleActivity ( 		actives			 : Option[Int] = None,
                                inactives		 : Option[Int] = None,
                                inactives24h : Option[Int] = None,
                                moving			 : Option[Int] = None
                           )
