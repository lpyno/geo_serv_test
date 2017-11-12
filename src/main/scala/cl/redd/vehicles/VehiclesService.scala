package cl.redd.vehicles

import javax.swing.text.html.parser.Entity
import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cl.redd.objects.ReddJsonProtocol._
import cl.redd.objects._
import io.swagger.annotations._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


@Api(value = "/vehicles", produces = "application/json")
@Path("/")
class VehiclesService(implicit val actor:ActorSystem, implicit val actorMaterializer: ActorMaterializer, implicit val ec:ExecutionContext )
  extends Directives {

  implicit val timeout = Timeout(5.seconds)

  val vehController:VehiclesController = new VehiclesController

  val routes = save

  /**
    * 4.1 "/save", POST method

			save( realm:String , vehicle:Vehicle ) : Vehicle = ???
			save( vehicle:Vehicle ) : Vehicle = ???
  *
    **/

  def save = ???
/*
    path("save") {
      post {
        entity(as[Vehicle]) {
          vehicle => complete { vehController.save( Some( vehicle ) ) }
        }
      }
    }*/
}



