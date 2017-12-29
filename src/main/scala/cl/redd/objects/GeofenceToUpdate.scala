package cl.redd.objects

case class GeofenceToUpdate (

                            name:String,
                            userId:Int,
                            theGeom:String,
                            bboxGeom:String,
                            buffer:Int,
                            colour:String,
                            maxSpeed:Int,
                            alarm:Boolean,
                            extraFields:String,
                            id:Int

                            )
