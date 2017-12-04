package cl.redd.objects

case class GeofenceToSave (

  name      :Option[String] =None,
  realm     :Option[String] =None,
  companyId :Option[Int]    =None,
  userId    :Option[Long]   =None,
  typeId    :Option[Int]    =None,
  buffer    :Option[Int]    =None,
  bboxGeom  :Option[String] =None,
  theGeom   :Option[String] =None,
  alarm     :Option[Boolean]=None,
  maxSpeed  :Option[Int] =None,
  colour    :Option[String] =None

)
