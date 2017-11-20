package cl.redd.objects

case class RequestData( filter:Option[FilterOld] = None , sort:Option[SortOld] = None , paginated:Option[PaginatedOld] = None )