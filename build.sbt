name := "geofence-microservice"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += "spray" at "http://repo.spray.io"
//resolvers += "spray repo" at "http://repo.spray.io"
resolvers += "spray nightlies" at "http://nightlies.spray.io/"
resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
resolvers += "Tastets Maven Repository" at "http://maven.redd.cl:8080/repository/tastets/"
credentials += Credentials( "Repository Archiva Managed tastets Repository", "maven.redd.cl", "desarrollo", "d3s4r0ll0.2017" )

val akkaVersion = "2.5.6"
val akkaHttpVersion = "10.0.10"
val sprayVersion = "1.3.2"

libraryDependencies ++= Seq(
  "io.swagger"        % "swagger-jaxrs" % "1.5.16",
  "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.11.0",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.megard"         %% "akka-http-cors" % "0.2.2",
  "org.slf4j"         % "slf4j-simple" % "1.7.25",
//  "io.spray"          %% "spray-can"        % sprayVersion,
//  "io.spray"          %% "spray-routing"    % sprayVersion,
//  "io.spray"          %% "spray-client"     % sprayVersion,
  "io.spray"          %% "spray-json" % "1.3.3",
  "cl.redd"           % "redd-discovery-client" % "1.0.1",
  "cl.tastets.life"   % "tastets-objects" % "1.3.5",
  "org.mongodb.scala" % "mongo-scala-driver_2.12" % "2.1.0",
  "com.netflix.eureka" % "eureka-client" % "1.1.147",
  "org.reactivemongo" %% "reactivemongo" % "0.12.3",
  "org.reactivemongo" % "reactivemongo-akkastream_2.12" % "0.12.3"
)
