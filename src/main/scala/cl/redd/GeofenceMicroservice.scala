package cl.redd

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import cl.redd.auth.AuthenticationService
import cl.redd.geofences._
import cl.redd.swagger.SwaggerDocService
import cl.redd.discovery.ReddDiscoveryClient


object GeofenceMicroservice extends App with RouteConcatenation {

  implicit val system = ActorSystem("geofence-microservice")

  sys.addShutdownHook(system.terminate())

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  ReddDiscoveryClient.init()

  val routes =
    cors() (
      new GeofenceService().route ~
      new AuthenticationService().route ~
      SwaggerDocService.routes
    )

  Http().bindAndHandle(routes, "0.0.0.0", 12345)

}