package cl.redd.objects

case class Fleet (

  id			      : Option[Int] = None,
  name			    : Option[String] = None,
  companyId		  : Option[Int] = None,
  companyName	  : Option[String] = None,
  defaultFleet  : Option[Int] = None,
  shared		    : Option[Int] = None,
  maxSpeed		  : Option[Int] = None,
  createDate	  : Option[Long] = None,
  startDay		  : Option[Long] = None,
  startHour		  : Option[String] = None,
  endDate		    : Option[Long] = None,
  endHour		    : Option[String] = None,
  inactiveDays	: Option[Int] = None,
  generateReport: Option[Boolean] = None,
  total			    : Option[Int] = None,
  realm			    : Option[String] = None,
  activity		  : Option[VehicleActivity] = None,
  fleetVehicles	: Option[List[Vehicle]] = None

)
