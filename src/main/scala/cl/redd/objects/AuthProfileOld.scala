package cl.redd.objects

case class AuthProfileOld (

                     description : Option[String] = None,
                     index       : Option[Int]    = None,
                     realm       : Option[String] = None,
                     state       : Option[String] = None,
                     title       : Option[String] = None,
                     device      : Option[String] = None,
                     user        : Option[String] = None,
                     childs      : Option[List[AuthProfChild]] = None,
                     url         : Option[String] = None,
                     content     : Option[AuthProfContent] = None

                     )
