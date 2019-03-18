package ca.usask.chdp.ExpServerAdmin

import akka.actor.{ActorRef, Actor, ActorLogging}
import View.Admin_ExpServerView
import ca.usask.chdp.models.Msgs.LobbyAdminMsg
import ca.usask.chdp.ExpServerAdmin.AdminActor.SendThis

class AdminActor extends Actor with ActorLogging {
  var admin: Option[Admin_ExpServerView] = None

  def receive = {
    case adminView: Admin_ExpServerView => admin = Some(adminView)
    case msg: LobbyAdminMsg => admin.map { _.receive(msg) }
    case SendThis(msg: LobbyAdminMsg, target: ActorRef) => target ! msg
    case msg => log.error("Received a msg that was not a LobbyAdminMsg -- {}", msg)
  }
}

object AdminActor {
  case class SendThis(msg: LobbyAdminMsg, target: ActorRef)
}


