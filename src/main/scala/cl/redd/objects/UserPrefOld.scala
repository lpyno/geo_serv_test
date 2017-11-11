package cl.redd.objects

case class UserPrefOld (

  id           : Option[Int],
  name         : Option[String],
  value        : Option[String],
  defaultValue : Option[String],
  description  : Option[String]

)
