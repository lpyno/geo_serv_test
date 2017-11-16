package cl.redd.objects

case class LastStateOld (

                    date     : Option[Long]   = None,
                    eventId  : Option[Int]    = None,
                    lng      : Option[Float]  = None,
                    odometer : Option[Double] = None,
                    hourmeter: Option[Long]   = None,
                    latitude : Option[Float]  = None,
                    alt      : Option[Float]  = None,
                    azimuth  : Option[Float]  = None,
                    speed    : Option[Float]  = None,
                    _m       : Option[String] = None,
                    geotext  : Option[String] = None,
                    _t       : Option[Long]   = None,
                    odo      : Option[Double] = None,
                    lat      : Option[Float]  = None,
                    longitude: Option[Float]  = None

                   )

/*

 "lastState": {
    "date": 1471896270000,
    "eventId": 141,
    "lng": -68.331978,
    "odometer": 148964.6,
    "hourmeter": 12176986,
    "latitude": -23.490961,
    "alt": 2306.5,
    "azimuth": 259,
    "speed": 18.7,
    "_m": "861074022157294",
    "geotext": "Camino interior Mina Sociedad chilena del Litio,San Pedro de Atacama,De Antofagasta ,Chile",
    "_t": 1471896270000,
    "odo": 148964.6,
    "lat": -23.490961,
    "longitude": -68.331978
  }

 */