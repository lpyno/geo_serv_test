package cl.redd.objects

import spray.json.JsValue

/*case class Vehicle (

                   one : Option[Int] = None,
                   two : Option[String] = None

                   )*/

case class Vehicle (

//                   vehicleFirstBlock: Option[VehicleFirstBlock] = None,
//                   vehicleLastBlock : Option[VehicleLastBlock] = None
                  id				      : Option[Int] = None,
                  name				    : Option[String] = None,
                  activityStatus : Option[String] = None, // ( "active","inactive","inactive24","moving" )
                  //companyName		: Option[String] = None,
                  companyId			: Option[Int] = None,
                  rutCompany		  : Option[String] = None,
                  vin				    : Option[String] = None,
                  plateNumber		: Option[String] = None,
                  engineTypeId		  : Option[Int] = None,
                  subVehicleTypeId	: Option[Int] = None,
                  createDate		  : Option[Long] = None,
                  validateDate		: Option[Long] = None,
                  dischargeDate	: Option[Long] = None,
                  //status			    : Option[Boolean] = None,
                  imei				    : Option[String] = None,
                  deviceTypeId		: Option[Int] = None,
                  simCard			  : Option[String] = None,
                  engineTypeName	: Option[String] = None,
                  deviceTypeName	: Option[String] = None,
                  subVehicleTypeName : Option[String] = None,
                  vehicleTypeName	  : Option[String] = None,
                  realm				      : Option[String] = None,
                  extraFields		: Option[String] = None,
                  lastState			: Option[LastState] = None
                 )
/*
case class VehicleFirstBlock (

                              id				    : Option[Int] = None,
                              name				  : Option[String] = None,
                              activityStatus: Option[String] = None, // ( "active","inactive","inactive24","moving" )
                              companyName		: Option[String] = None,
                              companyId			: Option[Int] = None,
                              rutCompany		: Option[String] = None,
                              vin				    : Option[String] = None,
                              plateNumber		: Option[String] = None,
                              engineTypeId		  : Option[Int] = None,
                              subVehicleTypeId	: Option[Int] = None,
                              createDate		  : Option[Long] = None,
                              validateDate		: Option[Long] = None,
                              dischargeDate		: Option[Long] = None,
                              status			    : Option[Boolean] = None,
                              imei				    : Option[String] = None,
                              deviceTypeId		: Option[Int] = None,
                              simCard			    : Option[String] = None,
                              engineTypeName	: Option[String] = None,
                              deviceTypeName	: Option[String] = None,
                              subVehicleTypeName: Option[String] = None,
                              vehicleTypeName	  : Option[String] = None,
                              realm				      : Option[String] = None

                             )


case class VehicleLastBlock (

                            extraFields		: Option[String] = None,
                            lastState			: Option[LastState] = None

                            )*/


