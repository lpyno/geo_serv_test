package cl.redd.objects

case class UserOld (

  companyId: Option[Int] = None,
  userId   : Option[Int] = None,
  status   : Option[String] = None,
  username : Option[String] = None,
  token    : Option[String] = None

)

/*
{
  "companyId": 144,
  "userId": 397,
  "status": "ok",
  "username": "jibanez-gl",
  "token": "56B65992B4FE85293236AC2D1FB429C4"
}*/
