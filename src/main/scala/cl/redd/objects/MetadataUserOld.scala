package cl.redd.objects

case class MetadataUserOld (

  lastName    : Option[String] = None,
  preferences : Option[List[UserPrefOld]] = None,
  logoPath    : Option[String] = None,
  tokenTime   : Option[Long]   = None,
  userName    : Option[String] = None,
  token       : Option[String] = None,
  rut         : Option[String] = None,
  password    : Option[String] = None,
  companyId   : Option[Int]    = None,
  phone       : Option[String] = None,
  name        : Option[String] = None,
  realm       : Option[String] = None,
  id          : Option[Int]    = None,
  email       : Option[String] = None,
  createDate  : Option[Long]   = None

)
