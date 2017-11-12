package cl.redd.objects

case class SelectProfiles (

  profiles  : Option[List[AuthProfileOld]] = None,
  status    : Option[String] = None

)