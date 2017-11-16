package cl.redd.objects

case class GetListByMids (
                          lastState : Option[Boolean] = None,
                          //mids      : Option[List[Map[String,String]]] = None,
                          mids      : Option[List[MidFormatOld]] = None,
                          realm     : Option[String] = None
                         )

