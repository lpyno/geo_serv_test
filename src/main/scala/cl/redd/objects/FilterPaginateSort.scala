package cl.redd.objects

case class FilterPaginateSort (
                                //filterParams:Option[List[Map[String,String]]] = None,
                                filterParams:Map[String,String],
                                pagLimit:Int=30,
                                pagOffset:Int,
                                sortOrder:Int,
                                sortParam:String
                              )
