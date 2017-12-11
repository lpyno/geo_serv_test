package cl.redd.objects

//case class FilterMain ( realm:Option[String]=None, companyId:Option[Int]=None, filter:Option[List[Map[String,String]]]=None )
case class FilterMain ( realm:String, companyId:Int, filter:Map[String,String] )
