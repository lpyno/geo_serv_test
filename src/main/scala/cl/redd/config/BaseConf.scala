package cl.redd.config

import com.typesafe.config.{Config, ConfigFactory}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{MongoClient, MongoCollection}
import scala.concurrent.duration.{FiniteDuration, _}

/**
  * Created by cmontecinos on 28-06-17.
  */
object BaseConf {

  val config: Config = ConfigFactory.load("application.conf")
  val env: Config = config.getConfig(config.getString("env")).withFallback(config)
  val zone : Int = env.getInt("zoneOffset")
  val passivationTimeoutCheck : FiniteDuration = env.getInt("passivationTimeout").minute
  val passivationCache : FiniteDuration = 10.minute
  val threshold = 5

  lazy val mongoClient = MongoClient(env.getString("mongo.url"))

  lazy val alarmsCollection : MongoCollection[Document] = mongoClient
    .getDatabase(env.getString("mongo.dbname"))
    .getCollection(env.getString("mongo.programmedAlarms"))

  lazy val webNotificationsCollection : MongoCollection[Document] = mongoClient
    .getDatabase(env.getString("mongo.dbWebNotifications"))
    .getCollection(env.getString("mongo.webNotificationsCollection"))

}
