package cl.redd.objects

case class GeofenceOld (

                         id		                   : Option[Int]     = None, //*
                         name		                 : Option[String]  = None, //*
                         buffer	                 : Option[Int]     = None, //*
                         last_update_timestamp   : Option[Long]    = None,
                         alarm		               : Option[Boolean] = None, //*
                         latitude	               : Option[Double]  = None, //*
                         colour                  : Option[String]  = None,
                         userid                  : Option[Int]     = None,
                         longitude	             : Option[Double]  = None, //*
                         total		               : Option[Int]     = None, //*
                         typeId	                 : Option[Int]     = None, //*
                         companyId	             : Option[Int]     = None,
                         extraFields	           : Option[Map[String,String]]  = None, //*
                         maxSpeed	               : Option[Int]     = None, //*
                         theGeom	               : Option[String]  = None, //*
                         bboxGeom 	             : Option[String]  = None, //*

                       )
