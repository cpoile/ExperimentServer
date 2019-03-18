package ca.usask.chdp.models

import akka.actor.ActorRef
import reflect.BeanProperty

object Msgs {
  sealed trait LobbyAdminMsg
  case class WaitingPlayerInfo(@BeanProperty globalId: String,
                               @BeanProperty var manipulation: Int,
                               @BeanProperty var role: String,
                               @BeanProperty location: String) extends LobbyAdminMsg
  case class AllWaitingPlayerInfo(allInfos: List[WaitingPlayerInfo]) extends LobbyAdminMsg
  case class ChangeWaitingPlayerInfo(newInfo: WaitingPlayerInfo) extends LobbyAdminMsg
  case class RemoveWaitingPlayerInfo(removedInfo: WaitingPlayerInfo) extends LobbyAdminMsg
  case class ReadyPlayerInfo(@BeanProperty globalId: String,
                             @BeanProperty var manipulation: Int,
                             @BeanProperty var role: String,
                             @BeanProperty location: String) extends LobbyAdminMsg
  case class AllReadyPlayerInfo(allInfos: List[ReadyPlayerInfo]) extends LobbyAdminMsg
  case class ChangeReadyPlayerInfo(newInfo: ReadyPlayerInfo) extends LobbyAdminMsg
  case class RemoveReadyPlayerInfo(removedInfo: ReadyPlayerInfo) extends LobbyAdminMsg
  case class RegisterListenerForGettingReadyUsers(listener: ActorRef) extends LobbyAdminMsg
  case class RegisterListenerForReadyUsers(listener: ActorRef) extends LobbyAdminMsg
  case class RegisterListenerForGames(listener: ActorRef) extends LobbyAdminMsg
  case object RequestUpdateOnSessionInfo extends LobbyAdminMsg
  case object RequestUpdateOnGettingStartedUsers extends LobbyAdminMsg
  case object RequestUpdateOnReadyUsers extends LobbyAdminMsg
  case class RemoveWaitingPlayers(wplrs: List[String]) extends LobbyAdminMsg
  case class RemoveReadyPlayers(wplrs: List[String]) extends LobbyAdminMsg
  case object StartGameMatchPlayers extends LobbyAdminMsg
  case object LobbyStopGame extends LobbyAdminMsg
  case object NewSessionId extends LobbyAdminMsg
  case class AllowLogins(allow: Boolean) extends LobbyAdminMsg
  case object ResetSession_DANGEROUS extends LobbyAdminMsg
  case class SessionData(sessionID: String) extends LobbyAdminMsg
  case class GameInfo(@BeanProperty gr_id: String, // Unique experiment-wide ID
                      @BeanProperty expSessionID: String,
                      @BeanProperty manipulation: Int,
                      @BeanProperty p1globalId: String,
                      @BeanProperty p1Role: String,
                      @BeanProperty p2globalId: String,
                      @BeanProperty p2Role: String,
                      @BeanProperty round: String,
                      @BeanProperty cmgPerRoundA: String,
                      @BeanProperty cmgPerRoundB: String,
                      @BeanProperty hbehPerRound: String) extends LobbyAdminMsg
  case class AllGameInfo(allGames: List[GameInfo]) extends LobbyAdminMsg
  case class FinishedGameInfo(info: GameInfo) extends LobbyAdminMsg
  case class AllFinishedGameInfo(allGames: List[GameInfo]) extends LobbyAdminMsg
  case class SessionInfo(sessionId: String, loginsAllowed: Boolean, gameStarted: Boolean, playersGettingReady: Int, playersReady: Int,
                         playersTotal: Int) extends LobbyAdminMsg {
    override def toString = "Id: <span class='sessEmph'>" + sessionId +
      "</span>, LoginsAllowed: <span class='sessEmph'>" + loginsAllowed +
      "</span>, Started: <span class='sessEmph'>" + gameStarted +
      "</span>, GettingReady: <span class='sessEmph'>" + playersGettingReady +
      "</span>, Ready: <span class='sessEmph'>" + playersReady + "</span>"
  }
}
