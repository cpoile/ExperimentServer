package ca.usask.chdp.ExpServerCore.Tutorial

import akka.actor.{ActorLogging, Actor}

class FakeViewActor extends Actor with ActorLogging {
  def receive = {
    case msg => log.debug("")
  }
}
