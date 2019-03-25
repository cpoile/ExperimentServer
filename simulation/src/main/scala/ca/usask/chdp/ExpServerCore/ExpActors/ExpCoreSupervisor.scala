package ca.usask.chdp.ExpServerCore.ExpActors

import akka.actor._
import akka.event.LoggingReceive
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.{ReturningUserInfo, WaitingPlayer}
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor._
import ca.usask.chdp.ExpServerCore.View.ViewManager
import ca.usask.chdp.ExpServerCore.ExpActors.ExpCoreSupervisor.{CreateViewActorForAReconnectingWaitingPlayer, ReconnectViewActor, CreateViewActor}
import ca.usask.chdp.ExpServerCore.Models.Model

/**
  * ExpCoreSupervisor starts the lobby, connects ViewActors to players, and reconnects ViewActors
  * to disconnected players.
  * An exception at the Lobby level will be handled here and not kill the whole actor system.
  */
class ExpCoreSupervisor extends Actor with ActorLogging {

  import context._
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val lobby = actorOf(Props[Lobby], "lobby")
  log.debug("ExpCoreSupervisor started.")
  Lobby.registerLobby(lobby)

  override val supervisorStrategy = OneForOneStrategy() {
    case _: Exception ⇒ Resume
  }

  def receive = LoggingReceive {
    case CreateViewActor(globalID, email, location, vm) => {
      // create a view actor to be the bridge between the PlayerLogic (the simulation)
      // and the View (in this case, Vaadin)
      val viewActor = actorOf(Props(new ViewActor(globalID)), globalID + Model.counter("_vaNum_"))
      Lobby.registerViewActor(globalID, viewActor)
      viewActor ! RegisterNewVM(vm)
      // tell the viewManager
      //println("ExpCoreSupervisor: telling the viewmanager he has a view actor.")
      vm.send(YouHaveAViewActor(globalID, email, location))
    }
    case CreateViewActorForAReconnectingWaitingPlayer(wp, viewMgr) => {
      // create a view actor to be the bridge between the PlayerLogic (the simulation)
      // and the View (in this case, Vaadin)
      val viewActor = actorOf(Props(new ViewActor(wp.globalId)), wp.globalId + Model.counter("_vaNum_"))
      Lobby.registerViewActor(wp.globalId, viewActor)
      viewActor ! RegisterNewVM(viewMgr)
      // tell the viewManager
      //println("ExpCoreSupervisor: telling the viewmanager he has a view actor.")
      viewMgr.send(YouHaveAViewActorForReconnectingWaitingPlayer(wp))
    }
    case ReconnectViewActor(globalID, vm, closure) => {
      val originalVA = Lobby.idToViewActor(globalID)
      Lobby.registerViewActor(globalID, originalVA)
      originalVA ! RegisterNewVM(vm)
      vm.send(ReconnectedViewActor(closure))
    }
    case plr: WaitingPlayer => lobby ! plr
    case _ => log.error("Strange, ExpCoreSupervisor should not be receiving any more messages.")
  }

}

object ExpCoreSupervisor {

  trait ExpCoreMsg

  case class CreateViewActor(globalID: String, email: String, location: String,
                             viewManager: ViewManager) extends ExpCoreMsg

  case class CreateViewActorForAReconnectingWaitingPlayer(wp: WaitingPlayer,
                                                          vm: ViewManager) extends ExpCoreMsg

  case class ReconnectViewActor(globalID: String,
                                viewManager: ViewManager, retUserInfo: ReturningUserInfo) extends ExpCoreMsg

}
