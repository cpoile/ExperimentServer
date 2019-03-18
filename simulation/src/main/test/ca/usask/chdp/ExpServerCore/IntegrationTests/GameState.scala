package ca.usask.chdp.ExpServerCore.IntegrationTests

import org.slf4j.LoggerFactory
import akka.actor.ActorRef
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.WaitingPlayer
import ca.usask.chdp.ExpServerCore.Models.Model
import scalaz._
class GameState(tp1: GameState.MinimalTestProbe, tp2: GameState.MinimalTestProbe) {
  import GameState._

  val id = Model.counter("GameState")
  var curRnd: Int = 0
  val log = LoggerFactory.getLogger(id)
  log.debug("Integration test GameState starting.")
  addSysState(id)
  val otherRole = Map("RoleA" -> "RoleB", "RoleB" -> "RoleA")
  var wp1: WaitingPlayer = _
  var wp2: WaitingPlayer = _
  val vaProbe1: MinimalTestProbe = tp1
  val vaProbe2: MinimalTestProbe = tp2

  /**
   * String is RoleA or RoleB
   */
  var probe = Map.empty[String, MinimalTestProbe]
  /**
   * String is RoleA or RoleB
   */
  var plr: Map[String, ActorRef] = Map.empty[String, ActorRef]
  var idToPlr: Map[String, ActorRef] = Map.empty[String, ActorRef]
  var daysWorked = Map("RoleA" -> 0, "RoleB" -> 0)
  val em1 = "emailNum_" + Model.counter("TESTING_email")
  val em2 = "emailNum_" + Model.counter("TESTING_email")
  val id1 = Model.counter("player")
  val id2 = Model.counter("player")

  def close() {
    //   log.error("should be stopping, but I'm not...")
    log.warn("Stopping.")
    remSysState(this.id)
    wp1 = null
    wp2 = null
    plr = null
    idToPlr = null
    daysWorked = null
  }
}

import Scalaz._

object GameState {
  type MinimalTestProbe = { def expectMsgClass[C](c: Class[C]): C; def ref: ActorRef }

}
