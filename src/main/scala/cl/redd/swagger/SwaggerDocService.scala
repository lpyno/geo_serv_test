package cl.redd.swagger

import cl.redd.auth.AuthenticationService
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import cl.redd.geofences.GeofenceService
import io.swagger.models.ExternalDocs
import io.swagger.models.auth.BasicAuthDefinition

object SwaggerDocService extends SwaggerHttpService {

  override val apiClasses = Set( classOf[GeofenceService] , classOf[AuthenticationService] )
  override val info = Info( version = "0.0.1",
                            title = "Auth - Geofences Service" )
  override val host = "localhost:12345"
  override val basePath = "/"
  override val externalDocs = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
  override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")

}