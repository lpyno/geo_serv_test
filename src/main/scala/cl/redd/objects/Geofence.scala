package cl.redd.objects

case class Geofence (

  id		                 : Option[Long]     = None, //*
  name		               : Option[String]  = None, //*
  alarm		               : Option[Boolean] = None, //*
  colour	               : Option[String]  = None, // ? in table geo.geofence
  buffer	               : Option[Int]     = None, //*
  lastUpdateTs           : Option[Long]    = None,
  latitude	             : Option[Double]  = None, //*
  longitude	             : Option[Double]  = None, //*
  theGeom 	             : Option[String]  = None, //*
  bboxGeom	             : Option[String]  = None, //*
  userId	               : Option[Long]     = None, // ? in table geo.geofence
  total		               : Option[Int]     = None, //*
  realm		               : Option[String]  = None, // ? in req query
  typeId	               : Option[Int]     = None, //*
  companyId	             : Option[Int]     = None, // ? in req query
  maxSpeed	             : Option[Int]     = None, //*
  extraFields	           : Option[Map[String,String]] = None, //*
  geoCoordinatesGeom	   : Option[Vector[String]] = None,
  geoCoordinatesBboxGeom : Option[Vector[String]] = None

)