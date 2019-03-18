package ca.usask.chdp.ExpServerCore.ExpActors

import akka.actor.{ActorRef, Actor}
import ca.usask.chdp.ExpServerCore.Models.MsgData
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.{RegisterNewVM, ViewMsg}
import ca.usask.chdp.ExpServerCore.View.{UIStateBeanItem, ViewManager}
import scalaz._
import Scalaz._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.{WaitingPlayer, ReturningUserInfo}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.ASentThisProjectData
import ca.usask.chdp.ExpServerCore.View.UIState

/**
 * This actor acts as a bridge or Interface between the Expcore and the particular view implementation.
 * We can use whatever view we want, and it will register it's function with this view Actor.
 * It is up to the implementing view to handle each of the ViewMsg messages.
 */
class ViewActor(globalID: String) extends Actor {
  var viewManager: Option[ViewManager] = None

  def receive = {
    case RegisterNewVM(newVM) => viewManager = newVM.some
    case msg: ViewMsg => viewManager foreach (_.send(msg))
  }

}

object ViewActor {

  case class RegisterNewVM(viewManager: ViewManager)

  sealed trait ViewMsg
  case class SetState(state: UIStateBeanItem) extends ViewMsg
  case class RefreshUI(bean: UIStateBeanItem) extends ViewMsg
  // We send role because it is possible it will change during the lobby period. When we get a
  // player actor that means the game has started, and role is now fixed.
  case class WeHaveAPlayerLogic(plr: ActorRef) extends ViewMsg
  case class ChatMessageLst(msgs: List[MsgData]) extends ViewMsg
  case class YouHaveAViewActor(globalID: String, email: String, location: String) extends ViewMsg
  case class ReconnectInGameParticipantToSim(retUserInfo: ReturningUserInfo) extends ViewMsg
  case class ReconnectedViewActor(userClosure: ReturningUserInfo) extends ViewMsg
  case class YouHaveAViewActorForReconnectingWaitingPlayer(wp: WaitingPlayer) extends ViewMsg
  case class LoggedIn(email: String, pwd: String, globalId: String,
                      location: String = "") extends ViewMsg
  case object CloseView extends ViewMsg
  case class SetViewToRaceView(trackNum: Int) extends ViewMsg
  case class YouAreRegisteredInLobby(registeredPlayer: WaitingPlayer) extends ViewMsg
  case class LobbyStats(numParticipants: Int, numWaitingInLobby: Int) extends ViewMsg
  case class ViewSettings(settings: Map[String, Object]) extends ViewMsg
  case object SetupViewSettingsForTutorial extends ViewMsg

  def convertStringToWaitingPlayerStage(stage: String, wp: WaitingPlayer): ViewMsg = {
    stage match {
      case "TutorialPage" => TutorialPage(wp)
      case "FinishedTutorial" => FinishedTutorial(wp)
      case "WaitingInLobbyPage" => WaitingInLobbyPage(wp)
    }
  }

  /**
   * HTML Pages.
   */
  case class LoginPage(room: Option[String] = None) extends ViewMsg
  case class InstructionsPage(waitingPlayer: WaitingPlayer) extends ViewMsg
  case class TutorialPage(waitingPlayer: WaitingPlayer) extends ViewMsg
  case class FinishedTutorial(waitingPlayer: WaitingPlayer) extends ViewMsg
  case class FinishedFirstSurvey(waitingPlayer: WaitingPlayer,
                                 results: Map[String, Int]) extends ViewMsg
  case class WaitingInLobbyPage(waitingPlayer: WaitingPlayer) extends ViewMsg


  /**
   * Not sure where to put this. ViewManagers (and other ViewActors) use them..
   */
  sealed trait UIStateCmd

  // Part1:
  // Login
  // Reading consent.
  // answering survey questions.

  // Part2:
  // Login
  // Reading consent.
  // Reading/watching intro.
  // Answering control questions.
  // Waiting in Lobby for a match.
  // Found a match/starting game.

  // Specific to A:
  case class A_WorkingOnProj1(isTutorial: Boolean = false) extends UIStateCmd
  case object A_FinishedGoals extends UIStateCmd
  case class A_WorkingOnProj1And2(isTutorial: Boolean = false) extends UIStateCmd
  case class A_FinishedWorkDays(isTutorial: Boolean = false) extends UIStateCmd
  case class A_WaitingForB(isTutorial: Boolean = false) extends UIStateCmd
  case object A_BFinishedRound extends UIStateCmd

  // Specific to B:
  case class B_WatchingA(halfwayThroughGameNotice: Boolean = false,
                         manipulation: Int = 0,
                         isTutorial: Boolean = false) extends UIStateCmd
  case class B_ASentProj(data: ASentThisProjectData) extends UIStateCmd
  case class B_WorkingOnProj(isTutorial: Boolean = false) extends UIStateCmd
  case class B_FinishedTeamProj(daysLeftOver: Int, isTutorial: Boolean = false) extends UIStateCmd
  case class B_RanOutOfWorkDays(catPicNum: Int, shortfall: Int) extends UIStateCmd

  // For both:
  case object TutorialSummaryAndQs extends UIStateCmd
  case class WatchingRace(raceHistory: List[List[Int]],
                          historyOfPosChange: List[List[Map[String, Int]]],
                          isTutorial: Boolean = false) extends UIStateCmd
  case class RaceResults(overallPoints: Map[String, Int], finishingPositions: List[Int],
                         changeInPoints: Map[String, Int], thisRoundRanking: Map[String, Int],
                         changeInRanking: Map[String, Int], trackNum: Int,
                         historyOfWins: Map[String, Int],
                         isTutorial: Boolean = false) extends UIStateCmd
  case class DamageReport(isTutorial: Boolean = false) extends UIStateCmd
  case class TakingSurvey(surveyName: String, isHalfway: Boolean) extends UIStateCmd
  case object WaitingForPartnerToFinishSurvey extends UIStateCmd
  case object WaitingForPartner extends UIStateCmd
  case class ShowFinishedGame(role: String, raceResults: RaceResults, retUUID: String,
                              surveyLoc: String) extends UIStateCmd
  case object AllDone extends UIStateCmd
}