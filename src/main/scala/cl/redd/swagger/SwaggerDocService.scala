package cl.redd.swagger

import cl.redd.auth.AuthApi
import cl.redd.fleets.FleetsApi
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import cl.redd.geofences.GeofenceApi
import cl.redd.vehicles.VehiclesApi
import io.swagger.models.ExternalDocs
import io.swagger.models.auth.BasicAuthDefinition

object SwaggerDocService extends SwaggerHttpService {

  override val apiClasses = Set( classOf[GeofenceApi] , classOf[AuthApi] , classOf[VehiclesApi] , classOf[FleetsApi] )
  override val info = Info( version = "0.0.6",
                            title = "[Auth - Vehicles - Fleets - Geofence] Service" )
  override val host = "localhost:12345"
  override val basePath = "/"
  override val externalDocs = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
  override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")

}