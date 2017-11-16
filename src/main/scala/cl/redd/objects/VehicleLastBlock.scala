package cl.redd.objects

case class VehicleLastBlock (

                              realm		      : Option[String] = None,
                              extraFields		: Option[String] = None,
                              lastState			: Option[LastState] = None

                            )