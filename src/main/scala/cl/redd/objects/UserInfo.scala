package cl.redd.objects

case class UserInfo (

  id	       : Option[Int] = None,
  name	     : Option[String] = None,
  companyId	 : Option[Int] = None,
  isAdmin	   : Option[Boolean] = None,
  profiles	 : Option[List[ProfileOld]] = None,			// definir si es necesario obtener todo el perfilado o solo para rastreosar (los true)
  realm		   : Option[String] = None,
  status	   : Option[String] = None,
  token		   : Option[String] = None, 					    // se va a seguir ocupandp token?
  email		   : Option[String] = None,
  timeZone	 : Option[String] = None,
  savedViews : Option[List[String]] = None,   	            // vista guardada de seguimiento movil [definir tipo final]
  dashboard  : Option[List[String]] = None,                // data de dashboard
  extraFields: Option[String] = None                       // otros
                    
) //


