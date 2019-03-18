package ca.usask.chdp.ExpServerCore.View

import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor._
import scalaz._
import Scalaz._
import akka.actor.{Props, Actor}
import com.vaadin.ui.{CustomComponent, Label, CssLayout}
import ca.usask.chdp.ExpServerCore.Game
import ca.usask.chdp.Enums._
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{KillSelfAndGame, SendMeRaceResults, PlayerInfo}
import scala.Some
import ca.usask.chdp.ExpServerCore.Models.MsgData
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.{ReturningUserInfo, StartTutorial, PlayerFinishedInstructions, WaitingPlayer}
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import ca.usask.chdp.ExpServerCore.Tutorial.{DamageReportTutorialView, RaceResultsTutorialView, WatchingRaceTutorialView, TutorialJSViewControl}
import ca.usask.chdp.models.ParticipantDAO
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor

/**
 * The Game sends us a list of states the player's view should be in.
 * if it's in those states, don't do anything. If not, build it.
 * Each playeLogic Actor has this private object to manage it's views.
 */
class VaadinViewManager extends CustomComponent with ViewManager {

  // This VaadinViewManager is now an active ViewManager, make sure to set it's receive function.

  var myPhysicalLocation: Option[String] = None
  var waitingPlayerInfo: Option[WaitingPlayer] = None
  var playerInfo: Option[PlayerInfo] = None
  var myGlobalId: String = ""
  var myRole: String = ""
  private val viewManagerLayout = new CssLayout
  setCompositionRoot(viewManagerLayout)
  setSizeUndefined()

  val log = LoggerFactory.getLogger("ViewManager")

  private var curBaseUIState: UIStateCmd = null
  private[this] var curUIRefreshableView: Option[UIRefreshable] = None
  private var workingView: Option[WorkingView] = None
  private var chatMsgReceiver: Option[(List[MsgData]) => Unit] = None
  private var waitingLobbyView: Option[WaitingInLobbyView] = None
  private var waitingForPartnerWnd: Option[WaitingForPartnerWnd] = None
  private var waitingForPartnerToFinishSurveyWnd: Option[WaitingForPartnerToFinishSurveyView] = None

  // this is the partial function that must be defined by any ViewManager.
  // similar to an actor's receive.
  // We receive messages sent to our ViewActor.
  def receiveMsg = {
    /**
     * Pre-game messages:
     */
    // STEP 1:
    case LoginPage(location) => {
      viewManagerLayout.removeAllComponents()
      curBaseUIState = null
      viewManagerLayout.addComponent(new SimLoginView(this, location))
      myPhysicalLocation = location
    }
    // STEP 2:
    case LoggedIn(email, pwd, globalId, location) => {
      myGlobalId = globalId
      Lobby.idToWaitingPlayer.get(globalId) match {
        case Some(wp) => {
          log.debug("LoggedIn sent to ViewManager. This seems to be a returning WaitingPlayer: {}", wp)
          // the player was in the middle of the intro to the game. How early in the process?
          myRole = wp.role
          Lobby.giveMeViewActorForAReconnectingWaitingPlayer(wp, this)
        }
        case None => {
          // maybe user is resuming interrupted game?
          Lobby.isUserResuming(globalId) match {
            case Some(retUserInfo) =>
              log.debug("LoggedIn sent to ViewManager. This sends to ReconnectInGameParticipantToSim. " +
                "UserInfo is defined: {}", retUserInfo)
              this.send(ReconnectInGameParticipantToSim(retUserInfo))
            case None =>
              // Nope, User is new.
              log.debug("LoggedIn sent to ViewManager. UserInfo is not defined. Calling giveMeViewActor. " +
                "This sends to Step 3, then sends NewPlayer to the lobby.")
              Lobby.giveMeViewActor(globalId, email, location, this)
          }
        }
      }
      // Either way, we need to register this ViewManager with the Lobby for every alternative.
      Lobby.registerViewManager(globalId, this)
    }

    // STEP 3:
    case YouHaveAViewActor(globalID, email, loc) => {
      // now that I have a viewActor I can join the lobby.
      // The lobby has the viewActor in a globalId -> viewActor map
      // Lobby will tell me what manipulation I am and I can read instructions.
      Lobby.newPlayer(WaitingPlayer(globalID, email, location = loc))
    }
    // STEP 4:
    case YouAreRegisteredInLobby(waitingPlayer) => {
      // I have my manipulation and Role. Now show instructions, or in this case, the tutorial.
      myRole = waitingPlayer.role
      // Deprecated InstructionsPage:
      //this.send(InstructionsPage(regPlayer))
      this.send(TutorialPage(waitingPlayer))
    }
    // STEP 5:
    // This is what we used to use when we gave players a whole page of instructions. When they
    // answered the questions right it sent a "WaitingInLobbyPage" message.
    case InstructionsPage(waitingPlayer) => {
      viewManagerLayout.removeAllComponents()
      curBaseUIState = null
      viewManagerLayout.addComponent(new InstructionsView(waitingPlayer, this))
    }
    // STEP 5:
    // Now we give them a tutorialGame based on their role.
    case TutorialPage(wp) => {
      // save the waitingPlayer info; it is used when they reach the TutorialSummaryAndQsView

      waitingPlayerInfo = wp.copy(currentStageOfWaiting = "TutorialPage").some
      // save waitingPlayerInfo in case we have to reconnect
      Lobby.registerWaitingPlayerInfo(waitingPlayerInfo.get)

      // if we are in testing mode (integration tests) skip the tutorial and go straight to game.
      if (Lobby.settings.testing_skipTutorial) {
        this.send(FinishedTutorial(waitingPlayerInfo.get))
        // viewManagerLayout.removeAllComponents()
        // curBaseUIState = null
        // viewManagerLayout.addComponent(new TutorialSummaryAndQsView(waitingPlayer, this))
      } else {
        Lobby.lobby ! StartTutorial(waitingPlayerInfo.get)
      }
    }
    // STEP 6:
    case FinishedTutorial(wp) => {
      // We reached this part. Save the player's progress (to use a game metaphor)
      waitingPlayerInfo = wp.copy(currentStageOfWaiting = "FinishedTutorial").some
      // save waitingPlayerInfo in case we have to reconnect
      Lobby.registerWaitingPlayerInfo(waitingPlayerInfo.get)

      // If we are giving a before-game survey, give it now. If not, send to the waiting in Lobby Page.
      if (Lobby.settings.giveSurveyBeforeFirstRound) {
        val modalWindow = new TakingSurveyInternallyRnd0Wnd(waitingPlayerInfo.get, this)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      } else
        this.send(WaitingInLobbyPage(waitingPlayerInfo.get))
    }
    // STEP 7:
    case FinishedFirstSurvey(waitingPlayer, mapOfAnswers) => {
      //log.debug("finishedSurvey called. curUser: {}", curUser)
      val part = ParticipantDAO.findByGlobalId(waitingPlayer.globalId)
      //log.debug("part bean: {}", part)
      part foreach { p =>
      //log.debug("updating user: ", p)
        val newParticipantBean = p.copy(internalSurveyRnd0 = mapOfAnswers)
        ParticipantDAO.insertUpdate(newParticipantBean)
      }
      this.send(WaitingInLobbyPage(waitingPlayer))
    }
    // STEP 8:
    case WaitingInLobbyPage(wp) => {
      waitingPlayerInfo = wp.copy(currentStageOfWaiting = "WaitingInLobbyPage").some
      // save waitingPlayerInfo in case we have to reconnect
      Lobby.registerWaitingPlayerInfo(waitingPlayerInfo.get)
      viewManagerLayout.removeAllComponents()
      curBaseUIState = null
      waitingLobbyView = new WaitingInLobbyView(waitingPlayerInfo.get, this).some
      viewManagerLayout.addComponent(waitingLobbyView.get)
      // Notify lobby that we're finished reading instructions.
      Lobby.lobby ! PlayerFinishedInstructions(waitingPlayerInfo.get)
    }
    // STEP 9:  (also called from step 5 during the tutorial)
    case WeHaveAPlayerLogic(plr) => {
      log.debug("WeHaveAPlayerLogic called for: ", plr)
      playerInfo = Some(PlayerInfo(myGlobalId, myRole, plr))
    }
    // STEP 10:
    // Now we are in the game and receive our first SetState message.

    /**
     * In game messages:
     */
    case SetState(state) => sync {
      setView(state.bean.uiCmd, state.bean)
      curUIRefreshableView foreach (_.setState(state))
    }
    // Refresh everything, something might have gone out of sync.
    case RefreshUI(state) => sync {
      setView(state.bean.uiCmd, state.bean)
      curUIRefreshableView foreach (_.refreshUI(state))
    }
    case ChatMessageLst(chatMsgs) => sync {
      chatMsgReceiver foreach (_(chatMsgs))
    }

    // Not using, but keeping incase we need to send view changes directly to a Javascript component:
    //case ViewSettings(settings) => sync {
    //  log.debug("calling viewsettings")
    //  curUITutorialViewControllable foreach (_.setViewSettings(settings))
    //}

    case YouHaveAViewActorForReconnectingWaitingPlayer(wp) => {
      waitingPlayerInfo = wp.some
      // now return to the stage they were in before leaving
      this.send(ViewActor.convertStringToWaitingPlayerStage(wp.currentStageOfWaiting, wp))
    }

    /**
     * Reconnecting.
     * NOTE: ReconnectInGameParticipantToSim comes from the ExpServerUI -- was having trouble because the
     * message was also used to reconnect a dropped user, which is different (especially when they are only
     * in the tutorial stage).
     */
    case ReconnectInGameParticipantToSim(retUserInfo) => {
      myGlobalId = retUserInfo.playerInfo.globalId
      // remove previous view manager, if any
      val prevVM = Lobby.removePreviousViewManager(myGlobalId)
      // Don't worry about this, it throws a null pointer exception.
      //if (prevVM.isDefined)
      //  prevVM.get(CloseView)
      Lobby.registerViewManager(myGlobalId, this)
      log.debug("ReconnectInGameParticipantToSim -- Viewmanager reconnecting globalId: {}.  ReturningUserInfo: {}", myGlobalId, retUserInfo)
      Lobby.retrieveMyViewActor(myGlobalId, this, retUserInfo)
    }
    case ReconnectedViewActor(retUserInfo) => {
      // now I can rejoin the game.
      playerInfo = Some(retUserInfo.playerInfo)

      sync {
        switchToReturningUserView()
      }
      playerInfo.get.playerLogic ! retUserInfo.msg
    }
    case CloseView => {
      sync {
        viewManagerLayout.removeAllComponents()
        curUIRefreshableView = None
        viewManagerLayout.addComponent(new Label("You have logged into the simulation from another window. " +
          "You may close this window now."))
        become(viewClosed)
      }
    }

    /**
     * Misc
     */
    case stats: LobbyStats => {
      waitingLobbyView foreach (_.updateLobbyStats(stats))
    }

    /**
     * Testing
     */
    // for testing
    case SetViewToRaceView(trackNum) => sync {
      switchToRaceView(trackNum)
    }
  }

  def viewClosed: ReceiveMsg = {
    case msg => {
      println("Viewmanager for " + playerInfo.get + " received message: " + msg)
    }


  }

  /**
   * Check if we are on this base View currently, if not switch to it.
   */
  def checkAndSetBaseUIState(baseState: UIStateCmd, stateBean: UIState) {
    if (curBaseUIState != baseState) {
      log.debug("{} -- {} -- curBaseUIState was -- {} -- switching to --  {}",
        Array(playerInfo.get.globalId, playerInfo.get.role, curBaseUIState, baseState).asInstanceOf[Array[AnyRef]])
      setView(baseState, stateBean)
      curBaseUIState = baseState
    }
  }
  def clearAllViewState() {
    viewManagerLayout.removeAllComponents()
    workingView = None
    curUIRefreshableView = None
    chatMsgReceiver = None
  }
  /**
   * Modification: we are now creating new views everytime we shift view, to simplify things.
   * I'm not sure if this creates a memory leak yet.
   */
  private def setView(uiCmd: UIStateCmd, stateBean: UIState) {
    uiCmd match {
      /**
       * Specific to A:
       */
      case cmd: A_WorkingOnProj1 => {
        if (curBaseUIState != cmd) {
          waitingForPartnerWnd.foreach(_.close())
          log.debug("{} -- {} -- CurBaseUIState set to A_WorkingOnProj1. Is Tutorial? {}",
            Array[AnyRef](playerInfo.get.globalId, playerInfo.get.role, cmd.isTutorial.toString))
          viewManagerLayout.removeAllComponents()
          workingView = if (cmd.isTutorial)
            (new WorkingView(stateBean, playerInfo.get) with TutorialJSViewControl).some
          else
            new WorkingView(stateBean, playerInfo.get).some
          viewManagerLayout.addComponent(workingView.get)
          curUIRefreshableView = workingView
          chatMsgReceiver = (workingView.get.messageReceived(_)).some
          workingView.get.startWorkPhase()
          curBaseUIState = cmd
        }
      }
      case A_FinishedGoals => {
        checkAndSetBaseUIState(A_WorkingOnProj1(), stateBean)
        val modalWindow = new A_FinishedGoalsWnd(playerInfo.get)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }
      case cmd: A_WorkingOnProj1And2 => {
        checkAndSetBaseUIState(A_WorkingOnProj1(isTutorial = cmd.isTutorial), stateBean)
        workingView.get.allowPersProject()
      }
      case cmd: A_FinishedWorkDays => {
        checkAndSetBaseUIState(A_WorkingOnProj1(isTutorial = cmd.isTutorial), stateBean)
        workingView.get.allowPersProject()
        val modalWindow = new A_FinishedWorkDaysWnd(playerInfo.get)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }
      case cmd: A_WaitingForB => {
        checkAndSetBaseUIState(A_WorkingOnProj1(isTutorial = cmd.isTutorial), stateBean)
        workingView.get.allowPersProject()
        workingView.get.aWaitingForB(waiting = true, isTutorial = cmd.isTutorial)
      }
      case A_BFinishedRound => {
        checkAndSetBaseUIState(A_WorkingOnProj1(), stateBean)
        workingView.get.allowPersProject()
        workingView.get.aWaitingForB(waiting = true)
        workingView foreach (_.finishedWorkPhase())
        val modalWindow = new A_BFinishedRoundWnd(playerInfo.get)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }

      /**
       * Specific to B:
       */
      case B_WatchingA(halfwayThroughGameNotice, manipulation, isTut) => {
        if (curBaseUIState != B_WatchingA()) {
          waitingForPartnerWnd.foreach(_.close())
          log.debug("{} -- {} -- CurBaseUIState set to B_WatchingA. -- Is Tutorial?? --- {}",
            Array(playerInfo.get.globalId, playerInfo.get.role, isTut).asInstanceOf[Array[AnyRef]])
          viewManagerLayout.removeAllComponents()
          val watchingAView = if (isTut)
            new WatchingAView(stateBean, playerInfo.get) with TutorialJSViewControl
          else
            new WatchingAView(stateBean, playerInfo.get)
          viewManagerLayout.addComponent(watchingAView)
          curUIRefreshableView = watchingAView.some
          chatMsgReceiver = (watchingAView.messageReceived(_)).some
          curBaseUIState = B_WatchingA()

          // Now send a message to retrieve last rounds race results so the view can display them.
          // Let the view handle it.
          playerInfo.get.playerLogic ! SendMeRaceResults(watchingAView.setupRaceResults)

          // Now display the halfway info, if we are halfway through.
          if (halfwayThroughGameNotice) {
            val modalWindow = new B_HalfwayThroughGameWnd(playerInfo.get, manipulation)
            getUI.addWindow(modalWindow)
            modalWindow.center()
          }
        }
      }
      case B_ASentProj(aSentThisProjectData) => {
        checkAndSetBaseUIState(B_WatchingA(), stateBean)
        val modalWindow = new B_ASentProjWnd(playerInfo.get, aSentThisProjectData)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }
      case cmd: B_WorkingOnProj => {
        if (curBaseUIState != cmd) {
          log.warn("CurBaseUIState set to B_WorkingOnProj -- {} -- {}. Is Tutorial? {}",
            Array[AnyRef](playerInfo.get.globalId, playerInfo.get.role, cmd.isTutorial.toString))
          viewManagerLayout.removeAllComponents()
          workingView = if (cmd.isTutorial)
            (new WorkingView(stateBean, playerInfo.get) with TutorialJSViewControl).some
          else
            new WorkingView(stateBean, playerInfo.get).some
          viewManagerLayout.addComponent(workingView.get)
          curUIRefreshableView = workingView
          chatMsgReceiver = (workingView.get.messageReceived(_)).some
          workingView.get.startWorkPhase()
          curBaseUIState = cmd
        }
      }
      case B_FinishedTeamProj(daysLeftOver, isTut) => {
        checkAndSetBaseUIState(B_WorkingOnProj(isTutorial = isTut), stateBean)
        val modalWindow = new B_FinishedTeamProjWnd(daysLeftOver, playerInfo.get)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }
      case B_RanOutOfWorkDays(catPicNum, shortfall) => {
        checkAndSetBaseUIState(B_WorkingOnProj(), stateBean)
        val modalWindow = new B_RanOutOfWorkDaysWnd(catPicNum, shortfall, playerInfo.get)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }

      /**
       * For both players:
       */
      case cmd: WatchingRace => {
        clearAllViewState()
        val watchingRaceView = if (cmd.isTutorial) {
          new WatchingRaceView(playerInfo.get, cmd.raceHistory, cmd.historyOfPosChange, stateBean) with WatchingRaceTutorialView
        } else {
          new WatchingRaceView(playerInfo.get, cmd.raceHistory, cmd.historyOfPosChange, stateBean)
        }
        viewManagerLayout.addComponent(watchingRaceView)
        curBaseUIState = cmd
      }
      case cmd: RaceResults => {
        clearAllViewState()
        val raceResView = if (cmd.isTutorial) {
          new RaceResultsView(playerInfo.get, stateBean, cmd) with RaceResultsTutorialView
        } else {
          new RaceResultsView(playerInfo.get, stateBean, cmd)
        }
        viewManagerLayout.addComponent(raceResView)
        curBaseUIState = cmd
      }
      case cmd: DamageReport => {
        clearAllViewState()
        val dmgReportView = if (cmd.isTutorial) {
          new DamageReportView(playerInfo.get, stateBean) with DamageReportTutorialView
        } else {
          new DamageReportView(playerInfo.get, stateBean)
        }
        viewManagerLayout.addComponent(dmgReportView)
        curBaseUIState = cmd
      }
      case TutorialSummaryAndQs => {
        clearAllViewState()
        // TODO: kill the fake tutorial game.
        // TODO: check if all viewactor/game connections are okay.
        playerInfo.get.playerLogic ! KillSelfAndGame
        viewManagerLayout.addComponent(new TutorialSummaryAndQsView(waitingPlayerInfo.get, this))
      }
      case TakingSurvey(surveyName, isHalfway) => {
        checkAndSetBaseUIState(DamageReport(), stateBean)
        val modalWindow = new TakingSurveyInternallyWnd(playerInfo.get, surveyName, isHalfway)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }
      case WaitingForPartnerToFinishSurvey => {
        viewManagerLayout.addComponent(new WaitingForPartnerToFinishSurveyView)
      }
      case WaitingForPartner => {
        checkAndSetBaseUIState(DamageReport(), stateBean)
        waitingForPartnerWnd = new WaitingForPartnerWnd().some
        getUI.addWindow(waitingForPartnerWnd.get)
        waitingForPartnerWnd.get.center()
      }
      case ShowFinishedGame(role, raceResults, retUUID, surveyLoc) => {
        waitingForPartnerWnd.foreach(_.close())
        clearAllViewState()
        val modalWindow = if (role == "RoleA")
          new FinishedGameWnd(raceResults, retUUID, surveyLoc, role,
            stateBean.proj2DaysWorked.foldLeft(0)((prev, kv) => prev + kv._2))
        else
          new FinishedGameWnd(raceResults, retUUID, surveyLoc, role,
            stateBean.proj2DaysWorked.foldLeft(0)((prev, kv) => prev + kv._2))
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }
      case AllDone => {
        log.debug("viewManager received all done for {}.", playerInfo)
        clearAllViewState()
        val modalWindow = new AllDoneWnd(playerInfo.get)
        getUI.addWindow(modalWindow)
        modalWindow.center()
      }
    }
  }


  private def switchToWaitingForMatchView(manip: Int, role: String) {
    clearAllViewState()
    viewManagerLayout.addComponent(new WaitingForMatchView(manip, role))
  }
  private def switchToReturningUserView() {
    clearAllViewState()
    viewManagerLayout.addComponent(new MPR_ReturningUser)
  }
  /**
   * For testing the raceView without having to go through a whole round of clicking.
   */
  def switchToRaceView(trackNum: Int) {
    implicit val curManRound = CurGameManipRnd(1, 0)
    assert(trackNum >= 0 && trackNum <= 11)
    // just use dummy info; this is just for testing.
    playerInfo = PlayerInfo("PLayerTest", "RoleA", Lobby.system.actorOf(Props(new Actor {
      def receive = {
        case _ =>
      }
    }))).some
    val raceHistory = Game.createRace(Lobby.settings.trackSeq(trackNum),
      Lobby.settings.eqnVal(GameEqn.A_minHelp), Lobby.settings.numTeams, curManRound.manipulation, curManRound.round)
    val histOfPosChange = Game.genHistoryOfPosChange(raceHistory)
    val lastRndStandings = (for (i <- 0 until Lobby.settings.numTeams) yield {
      (i.toString, 0)
    }).toMap
    val firstRoundStandings = Map(Round(0) -> lastRndStandings)
    val overallStandings = (Round(0) -> Game.updateOverallPoints(raceHistory.last,
      firstRoundStandings, 0, Lobby.settings.numTeams))
    setView(WatchingRace(raceHistory, histOfPosChange), new UIState)
  }


  /**
   * Always synchronize on the VaadinSession. Call block by name.
   */
  private def sync[R](block: => R): Option[R] = {
    var result: Option[R] = None
    val lock = viewManagerLayout.getUI.getSession.getLockInstance
    lock.lock()
    try {
      result = Option(block)
    } finally {
      lock.unlock()
    }
    result
  }
}