package cl.redd.objects

case class FilterMain ( realm:Option[String]=None, companyId:Option[Int]=None, filter:Option[List[(String,String)]]=None )
