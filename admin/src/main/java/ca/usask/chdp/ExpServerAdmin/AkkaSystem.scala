package ca.usask.chdp.ExpServerAdmin

import akka.actor.{Props, ActorRef, ActorSystem}
import scalaz._
import Scalaz._
import ca.usask.chdp.models.Msgs.{LobbyAdminMsg, RegisterListenerForReadyUsers, RegisterListenerForGames, RegisterListenerForGettingReadyUsers}
import ca.usask.chdp.ExpServerCore.Models.Model
import View.Admin_ExpServerView
import ca.usask.chdp.ExpSettings
import akka.event.Logging
import ca.usask.chdp.ExpServerAdmin.AdminActor.SendThis

object AkkaSystem {
  lazy val system = ActorSystem("Admin")
  val log = Logging.getLogger(system, this)
  private var m_lobby: Option[ActorRef] = None
  lazy val lobby = m_lobby | connectToLobby.get
  var adminActor: ActorRef = system.actorOf(Props[AdminActor], "Admin" + Model.counter("admin"))

  def setAdminActorsView (viewManager: Admin_ExpServerView) {
    adminActor ! viewManager
  }
  def connectToLobby = {
    m_lobby = system.actorFor("akka://Exp@127.0.0.1:" + ExpSettings.get.portSimulation + "/user/Sys/lobby").some
    m_lobby
  }
  def registerWithLobby() {
    AkkaSystem.lobby ! RegisterListenerForGettingReadyUsers(adminActor)
    AkkaSystem.lobby ! RegisterListenerForReadyUsers(adminActor)
    log.debug("sent registerListenerForWaitingUsers and ReadyUsers to lobby.")
    AkkaSystem.lobby ! RegisterListenerForGames(adminActor)
    log.debug("sent registerListenerForGames to lobby.")
  }
  def resetConnection() {
    log.debug("Admin: Resetting connection.")
    m_lobby = system.actorFor("akka://Exp@127.0.0.1:" + ExpSettings.get.portSimulation + "/user/Sys/lobby").some
    registerWithLobby()
    log.debug("Admin: Akka loglevel -- is debug enabled (should be true): {}, is warning enabled (should be true): {}", system.log.isDebugEnabled, system.log.isWarningEnabled)
  }
  def sendMsgToLobby(msg: LobbyAdminMsg) {
    adminActor ! SendThis(msg, lobby)
  }
}
