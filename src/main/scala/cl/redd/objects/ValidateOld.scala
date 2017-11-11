package cl.redd.objects

case class ValidateOld (

  companyId     : Option[String] = None,
  metadataUser  : Option[MetadataUserOld] = None,
  userId        : Option[String] = None,
  status        : Option[String] = None,
  username      : Option[String] = None,
  token         : Option[String] = None

)

/**
  * auth/validate GET return

    {
    "companyId": "144",
    "metadataUser": {
    "lastName": "Ibañez",
    "preferences": [
    {
    "id": 1,
    "name": "ZONA_HORARIA",
    "value": "-04:00",
    "defaultValue": "-03:00",
    "description": "Zona Horaria"
    }
    ],
    "logoPath": "http://rslite.gps.cl/TastetsWeb/RS/img/logos/logo-GL.jpg",
    "tokenTime": 1479398416000,
    "userName": "jibanez-gl",
    "token": "kku7UcJqij9KeDmpXGRZicocD",
    "rut": "12345678",
    "password": "34d734a4ee19148aa2cc6cac2ee10ce5651156b2",
    "companyId": 144,
    "phone": null,
    "name": "Jose",
    "realm": "rslite",
    "id": 397,
    "email": "sparedes@reddsystem.com",
    "createDate": 1393094719000
    },
    "userId": "397",
    "status": "ok",
    "token": "654E59F8460F27FE1F4FFC003EA344D5",
    "username": "jibanez-gl"
    } */
