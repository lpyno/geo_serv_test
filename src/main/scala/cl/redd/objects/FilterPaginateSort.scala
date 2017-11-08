package cl.redd.objects

//case class FilterPaginateSort ( filterParams:ImmutableMap[String,String] , pagLimit:Int , pagOffset:Int , sortOrder:Int , sortParam:String )
case class FilterPaginateSort ( filterParams:Option[List[Tuple2[String,String]]] , pagLimit:Option[Int] , pagOffset:Option[Int] , sortOrder:Option[Int] , sortParam:Option[String] )
