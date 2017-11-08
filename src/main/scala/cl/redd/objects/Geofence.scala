package cl.redd.objects

case class Geofence (

  id		      : Option[Int]     = None,
  name		    : Option[String]  = None,
  alarm		    : Option[Boolean] = None,
  colour	    : Option[String]  = None,
  buffer	    : Option[Int]     = None,
  latitude	  : Option[Double]  = None,
  longitude	  : Option[Double]  = None,
  the_geom	  : Option[String]  = None,
  bbox_geom	  : Option[String]  = None,
  userId	    : Option[Int]     = None,
  total		    : Option[Int]     = None,
  realm		    : Option[String]  = None,
  typeId	    : Option[Int]     = None,
  companyId	  : Option[Int]     = None,
  maxSpeed	  : Option[Int]     = None,
  extraFields	: Option[String]  = None,
  geoCoordinatesGeom	  : Option[Vector[String]] = None,
  geoCoordinatesBboxGeom: Option[Vector[String]] = None

                    )