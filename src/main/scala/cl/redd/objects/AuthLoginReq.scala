package cl.redd.objects

case class AuthLoginReq(

  realm   : Option[String] = None,
  user    : Option[String] = None,
  pass    : Option[String] = None,
  device  : Option[String] = None

)