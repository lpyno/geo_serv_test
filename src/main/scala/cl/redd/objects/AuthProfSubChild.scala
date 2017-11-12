package cl.redd.objects

case class AuthProfSubChild (

                            title       : Option[String] = None,
                            description : Option[String] = None,
                            state       : Option[String] = None,
                            index       : Option[Int]    = None,
                            url         : Option[String] = None,
                            active      : Option[Boolean]= None,
                            childs      : Option[List[String]] = None,
                            content     : Option[AuthProfContent] = None

                            )
