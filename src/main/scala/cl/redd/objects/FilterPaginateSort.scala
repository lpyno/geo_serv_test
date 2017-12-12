package cl.redd.objects

case class FilterPaginateSort (
                                filterParams:Map[String,String],
                                pagLimit:Int=30,
                                pagOffset:Int,
                                sortOrder:Int,
                                sortParam:String
                              )
