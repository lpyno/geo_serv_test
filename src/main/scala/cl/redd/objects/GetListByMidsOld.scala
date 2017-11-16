package cl.redd.objects

case class GetListByMidsOld (

                            lastState : Option[Boolean] = None,
                            mids      : Option[List[MidFormatOld]] = None,
                            realm     : Option[String] = None

                            )
