package cl.redd.objects

case class ProfileOld (

                  id            : Option[Int]    = None,
                  name          : Option[String] = None,
                  creationDate  : Option[Long]   = None,
                  functionality : Option[List[FunctionalityOld]] = None

                 )