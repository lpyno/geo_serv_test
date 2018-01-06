package cl.redd

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import cl.redd.auth.AuthApi
import cl.redd.geofences.GeofenceApi
import cl.redd.swagger.SwaggerDocService
import cl.redd.discovery.ReddDiscoveryClient
import cl.redd.fleets.FleetsApi
import cl.redd.vehicles.VehiclesApi
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpMethods
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.collection.immutable

object RestApi extends App with RouteConcatenation with SprayJsonSupport {

  implicit val system = ActorSystem("rastreosat-back")

  sys.addShutdownHook(system.terminate())

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val settings = CorsSettings.defaultSettings.copy( allowedMethods = immutable.Seq( HttpMethods.GET,
                                                                                    HttpMethods.PUT,
                                                                                    HttpMethods.POST,
                                                                                    HttpMethods.DELETE,
                                                                                    HttpMethods.OPTIONS ) )

  ReddDiscoveryClient.init()

  val routes =
    cors( settings ) (
      new GeofenceApi().route ~
      new AuthApi().route ~
      new VehiclesApi().route ~
      new FleetsApi().route ~
      SwaggerDocService.routes
    )

  Http().bindAndHandle(routes, "0.0.0.0", 12345)

}