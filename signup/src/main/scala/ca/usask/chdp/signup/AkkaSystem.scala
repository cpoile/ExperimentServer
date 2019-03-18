package ca.usask.chdp.signup

import akka.actor.{Cancellable, ActorSystem}
import collection._
import collection.JavaConversions._


object AkkaSystem {
  lazy val system = ActorSystem("Admin")

  //lazy val scheduledActors: mutable.ConcurrentMap[String, Cancellable] = new java.util.concurrent.ConcurrentHashMap[String, Cancellable]
}
