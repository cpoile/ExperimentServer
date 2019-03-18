package ca.usask.chdp.ExpServerCore.Tutorial

//import scala.collection._

import scala.collection.mutable
import akka.event.LoggingReceive
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor._
import ca.usask.chdp.ExpServerCore.Models._
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor._
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic._
import ca.usask.chdp.Enums._
import scalaz._
import Scalaz._
import java.util.concurrent.atomic.AtomicBoolean
import ca.usask.chdp.ExpServerCore.Game._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.ReturningUserInfo
import ca.usask.chdp.ExpServerCore.View._
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import ca.usask.chdp.models.ParticipantDAO
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby

class TutorialPlayerLogic(globalId: String, gameId: String, myRole: String, manipulation: Int,
                          myGame: ActorRef) extends Actor with ActorLogging {

  import context._
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy = OneForOneStrategy() {
    case _: Exception ⇒ Resume
  }

  implicit val timeout: Timeout = Timeout(20 seconds)

  // We lookup the viewActor because it might change. This might be a bottleneck, we'll see. 
  private def viewActor = Lobby.idToViewActor(globalId)

  private var curUIStateCmd: UIStateCmd = null
  private var curRnd: Int = 0
  private val buttonState = mutable.Map[String, Boolean](
    "Part1" -> false, "Part2" -> false, "Part3" -> false
  )
  private var buttonStateProj2 = false
  private var daysLeft: Int = 0
  /**
   * String is Partnum (Part1, Part2, Part3)
   */
  private val pStepCur = mutable.Map.empty[String, PartStepData]
  /**
   * String is Partnum (Part1, Part2, Part3)
   */
  private val curRndGoals = mutable.Map.empty[String, Int]
  private var myPartner: ActorRef = null
  private var watchStatusBar = ""
  /**
   * String is Partnum (Part1, Part2, Part3)
   */
  private var damageInSteps = Map.empty[String, Int]
  /**
   * String is Partnum (Part1, Part2, Part3)
   */
  var curRndFinalPartData = Map.empty[String, Int]

  /**
   * String is Partnum (Part1, Part2, Part3)
   */
  private var partsStartEnd = Map.empty[String, (Int, Int)]

  /**
   * We need to guard against doing more than one work at a time. So effectively LOCK this actor
   * and block it from doing more work until this one is finished.
   */
  private val workLock = new AtomicBoolean(false)

  private var currentCatPic = 1
  // Just to keep students interested, walk through sad cat pics.
  private val numCatPics = 8
  private var curRndAHelpBehs = 0
  // collect personal stats, if I'm Role.A
  /**
   * String is Round (Rnd1, Rnd2, etc.)
   */
  val recordOfProj2Work = mutable.Map[Int, Int]()
  for (i <- 0 until Lobby.settings.numRounds) {
    val rnd = Round(i)
    recordOfProj2Work += (RoundID(rnd) -> 0)
  }
  var raceResults: Option[RaceResults] = None
  def generateBlankRaceResults(): RaceResults = {
    val overallPoints = (for (i <- 0 until Lobby.settings.numTeams) yield {(i.toString -> 0) }).toMap
    val finishingPositions = (0 until Lobby.settings.numTeams).toList
    val changeInPoints = (for (i <- 0 until Lobby.settings.numTeams) yield {(i.toString -> 0) }).toMap
    val thisRoundRanking = (for (i <- 0 until Lobby.settings.numTeams) yield {(i.toString -> (i + 1)) }).toMap
    val changeInRanking = (for (i <- 0 until Lobby.settings.numTeams) yield {(i.toString -> 0) }).toMap
    val trackNum = 0
    val historyOfWins = (for (i <- 0 until Lobby.settings.numTeams) yield {(i.toString -> 0) }).toMap
    RaceResults(overallPoints, finishingPositions, changeInPoints, thisRoundRanking,
      changeInRanking, trackNum, historyOfWins)
  }


  /**
   * TESTING
   */
  var testHarnessCallbacks = List.empty[(TestingPhase.Value, () => Unit)]
  private def testHarnessRegister(reg: RegisterTestingCallback) {
    testHarnessCallbacks = (reg.phase, reg.callback) :: testHarnessCallbacks
  }
  // called when we receive start new round data
  def testHarnessClear() {
    testHarnessCallbacks = Nil
  }
  /**
   * Call this function with the phase you are in. If we are testing, it will prompt the
   * registered functions to be called.
   * @param phase
   */
  def testHarnessCall(phase: TestingPhase.Value) {
    // instead of calling:
    // testHarnessCallbacks.filter{ case(p, _) => p == phase }.foreach { case (_, func) => func() }
    // we can call:
    testHarnessCallbacks.foreach { case (p, func) => if (p == phase) func() }
  }
  /**
   * /TESTING
   */

  /**
   * State transitions and curState is a little tricky:
   * When a UIStateChangeMsg comes:
   * _FIRST_ we change their ui's curState to what they will see during their next state.
   * _THEN_ we become that next state and wait for messages.
   */

  // These are composable receive partial functions that we can add to receive blocks as needed.
  def handleAlways: Receive = {
    case GetState => sender ! makeUIStateBeanItem
    case ReturningUser => viewActor ! RefreshUI(makeUIStateBeanItem)
    case GetGameActor => sender ! myGame
    case rtc: RegisterTestingCallback => {
      testHarnessRegister(rtc)
    }
  }
  def handleChat: Receive = {
    case IEnteredChatMsg(msg) => myGame ! MsgEntered(msg)
    case ChatMsgReceived(sent) => viewActor ! ChatMessageLst(sent)
    case SendMeAllMsgs => myGame ! SendMeAllMsgs
  }


  /**
   * This was from back when we didn't know our role until now. But I'm keeping it for now because I don't want
   * to break anything so close to experiment date.
   */
  def receive /*Waiting for a match*/ = {
    case FoundMatch(partner) => {
      log.debug("Received FoundMatch: partner -- {} -- I am Role -- {}", partner, myRole)
      // notify my view manager of my stats.
      viewActor ! WeHaveAPlayerLogic(self)
      myPartner = partner
      myRole match {
        case "RoleA" => become(handleAlways orElse IAmAnA, discardOld = true)
        case "RoleB" => become(handleAlways orElse IAmAB, discardOld = true)
      }
    }
  }
  def IAmAnA: Receive = {
    case d: StartNewRoundData => {
      setupInitialRoundState(d)
      curUIStateCmd = ViewActor.A_WorkingOnProj1(isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
      val ObjMap = Map("tab6" -> "hi!!!", "tab7" -> "ho!!!")
      import scala.collection.JavaConversions._
      // not using, but keeping in case we need to send instructions directly
      //viewActor ! ViewSettings(Map("tab1" -> java.lang.Boolean.TRUE, "tab2" -> java.lang.Boolean.FALSE,
      //  "tab3" -> java.lang.Boolean.TRUE, "tab4" -> Array("hi there", "this is 425", "data").asInstanceOf[Array[AnyRef]]))
      become(handleAlways orElse handleChat orElse A_workingOnGoals orElse A_tutorialMessages, discardOld = true)
    }
  }
  def IAmAB: Receive = {
    case d: StartNewRoundData => {
      //  Start with A's initial state, but we just watch it.
      setupInitialRoundState(d)
      curUIStateCmd = ViewActor.B_WatchingA(isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
      become(handleAlways orElse handleChat orElse B_watchingA orElse B_tutorialMessages, discardOld = true)
    }
  }
  def A_workingOnGoals: Receive = LoggingReceive {
    case WorkOn(part) if (buttonState(part) == false) => {
      log.error("Received a request to work on part: {} when daysLeft = {} or buttonState(part) = {}. Attempting to refreshUI.",
        part, daysLeft, buttonState(part))
      viewActor ! RefreshUI(makeUIStateBeanItem)
      // but just in case it was the test harness.
      testHarnessCall(TestingPhase.WorkingPhase)
    }
    case WorkOn(part) if (daysLeft > 0 && !metGoal(part)) => {
      // The above guard checked if the WorkOn command was valid, given current state.
      // doWorkAndSetResults(part)

      // We need to guard against doing more than one work at a time. So effectively LOCK this actor
      // and block it from doing more work until this one is finished.
      if (workLock.compareAndSet(false, true)) {
        myGame ! GetNextPartStep(part)
      }
      else {}
      // do nothing in response.
    }
    /* DidWork is the response from above. Not using an ask because that was blocking. */
    case DidWork(part, pStep, partHasNext) => {
      // Now release the workLock to allow another work event.
      // We can release it now because actors are single-threaded.
      workLock.set(false)

      pStepCur(part) = pStep
      buttonState(part) = if (partHasNext) true else false
      daysLeft -= 1
      //    if (daysLeft < 0)
      //      log.error("--A WorkOnProj2-- daysLeft was {}. Trace1: --doWorkAndSetResults -- " +
      //        "Worked on part: {}, daysLeft: {}, metGoal: {}", daysLeft, part, daysLeft, metGoal(part))
      //    log.debug("Trace1: --doWorkAndSetResults -- Worked on part: {}, daysLeft: {}, metGoal: {}",
      //      part, daysLeft, metGoal(part))
      testHarnessCall(TestingPhase.WorkingPhase)

      log.debug("Role: {}, dayleft: {}, part workedOn: {}", Array("RoleA", daysLeft, part))

      if (metGoal(part))
        buttonState(part) = false
      if (metAllGoals) {
        myGame ! AFinishedPersGoals
        //curUIStateCmd = ViewActor.A_FinishedGoals
        viewActor ! SetState(makeUIStateBeanItem)
        myPartner ! PartnerFinishedPersGoals(makeUIStateBeanItem)
        // tutorial -- skip reading inpo
        self ! FinishedReadingInfo
        become(handleAlways orElse handleChat orElse A_readingFinishedGoalsInfo, discardOld = true)
      } else {
        // update as normal.
        viewActor ! SetState(makeUIStateBeanItem)
        myPartner ! PartnerState(makeUIStateBeanItem)
      }
    }
  }
  def A_tutorialMessages: Receive = LoggingReceive {
    case Tut_RunAForXDays(partName, numDays, delayInMillis) => {
      var countDaysToWork = numDays
      val callback = () => {
        // only continue if we still have days to work
        if (countDaysToWork > 0) {
          countDaysToWork -= 1
          // Is this a part or the personal Project?
          val workMsg = if (partName == "PersProj") WorkOnProj2(1) else WorkOn(partName)
          // only work on one part per callback. If we work on a part that is locked, that work request it lost.
          if (delayInMillis > 0)
            system.scheduler.scheduleOnce(delayInMillis milliseconds, self, workMsg)
          else
            self ! workMsg
        } else {
          // remove callback.
          this.testHarnessClear()
        }
      }: Unit
      this.testHarnessRegister(RegisterTestingCallback(TestingPhase.WorkingPhase, callback))
      // Now, we have to kick start the process with the first work call.
      callback()
    }
    case Tut_SendBProject => {
      self ! FinishedReadingInfo
    }
  }
  def B_tutorialMessages: Receive = LoggingReceive {
    // For A's part of Tutorial:
    case ReadyForBToDoTheirWork => {
      // Go through each part and do all the work we can, then move to the next, until done.
      // Use the testing harness callbacks to attach this.
      // will run until new round is sent to the playerLogic
      val callback = () => {
        // only work on one part per callback. If we work on a part that is finished, the test harness won't be called again.
        if (!metGoal("Part1"))
          self ! WorkOn("Part1")
        else if (!metGoal("Part2"))
          self ! WorkOn("Part2")
        else if (!metGoal("Part3"))
          self ! WorkOn("Part3")
      }
      self ! RegisterTestingCallback(TestingPhase.WorkingPhase, callback)
      // Now, we have to kick start the process with the first work call.
      callback()
    }
    case cmd: Tut_RunAForXDays => {
      myPartner ! cmd
    }
    case Tut_SendBProject => {
      myPartner ! Tut_SendBProject
    }
  }
  def A_readingFinishedGoalsInfo: Receive = LoggingReceive {
    case FinishedReadingInfo => {
      buttonStateProj2 = true
      buttonState +=("Part1" -> true, "Part2" -> true, "Part3" -> true)
      curUIStateCmd = ViewActor.A_WorkingOnProj1And2(isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
      testHarnessCall(TestingPhase.WorkingPhase)
      become(handleAlways orElse handleChat orElse A_workingExtra orElse A_tutorialMessages, discardOld = true)
    }
  }

  def A_workingExtra: Receive = LoggingReceive {
    case WorkOn(part) if (daysLeft <= 0 || buttonState(part) == false) => {
      log.error("Received a request to work on part: {} when daysLeft = {} or buttonState(part) = {}. Attempting to refreshUI.",
        part, daysLeft, buttonState(part))
      viewActor ! RefreshUI(makeUIStateBeanItem)
    }
    case WorkOn(part) if (daysLeft > 0) => {
      log.debug("working on part {} will send a helpfulBeh, daysLeft after this: {}", part, daysLeft - 1)
      // We need to guard against doing more than one work at a time. So effectively LOCK this actor
      // and block it from doing more work until this one is finished.
      if (workLock.compareAndSet(false, true)) {
        myGame ! GetNextPartStep(part)
      }
      else {}
      // do nothing in response.
    }
    /* DidWork is the response from above. Not using an ask because that was blocking. */
    case DidWork(part, pStep, partHasNext) => {
      // Now release the workLock to allow another work event.
      // We can release it now because actors are single-threaded.
      workLock.set(false)

      pStepCur(part) = pStep
      buttonState(part) = if (partHasNext) true else false
      daysLeft -= 1
      //    if (daysLeft < 0)
      //      log.error("--A WorkOnProj2-- daysLeft was {}. Trace1: --doWorkAndSetResults -- " +
      //        "Worked on part: {}, daysLeft: {}, metGoal: {}", daysLeft, part, daysLeft, metGoal(part))
      //    log.debug("Trace1: --doWorkAndSetResults -- Worked on part: {}, daysLeft: {}, metGoal: {}",
      //      part, daysLeft, metGoal(part))
      testHarnessCall(TestingPhase.WorkingPhase)

      myGame ! HelpfulBeh
      A_workingExtraCleanup()
    }
    case WorkOnProj2(days) => {
      recordOfProj2Work(curRnd) += days
      daysLeft -= days
      if (daysLeft < 0) log.error("--A WorkOnProj2-- daysLeft was {}", daysLeft)
      A_workingExtraCleanup()
      testHarnessCall(TestingPhase.WorkingPhase)
    }
  }
  private def A_workingExtraCleanup() {
    // send partner an update, check if out of workdays, update state, etc.
    myPartner ! PartnerState(makeUIStateBeanItem)
    if (daysLeft <= 0) {
      buttonState +=("Part1" -> false, "Part2" -> false, "Part3" -> false)
      buttonStateProj2 = false
      curUIStateCmd = ViewActor.A_FinishedWorkDays(isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
      become(handleAlways orElse handleChat orElse A_readingFinishedWorkDaysInfo orElse A_tutorialMessages, discardOld = true)
    } else
      viewActor ! SetState(makeUIStateBeanItem)
  }

  def A_readingFinishedWorkDaysInfo: Receive = LoggingReceive {
    case FinishedReadingInfo => {
      // B is sent the ASentThisProjectData from the game object, not from A's Player actor.
      myGame ! ASentProject(recordOfProj2Work.toMap)
      curUIStateCmd = ViewActor.A_WaitingForB(isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
      become(handleAlways orElse handleChat orElse A_waitingForB, discardOld = true)
    }
  }
  def B_watchingA: Receive = LoggingReceive {
    case SendMeRaceResults(fn) => {
      fn(raceResults | generateBlankRaceResults())
    }
    case PartnerState(pState) => {
      //watchStatusBar = ""
      // For now, we will use the UIData bean to hold the partner's data. We will let the view
      // display it how it wishes. Obviously, it won't allow Role.B to work on the project.
      // But, maintain current state, not the state in the partner's bean.
      viewActor ! SetState(curUIStateWhenWatchingPartner(pState))
    }
    case PartnerFinishedPersGoals(pState) => {
      //watchStatusBar = "Your partner has finished their personal goals. They will now choose between working " +
      //  "on the team project (the F1 car) or their own personal project for their managers."
      // for now we aren't doing anything special, just a normal setState like usual.
      viewActor ! SetState(curUIStateWhenWatchingPartner(pState))
    }
    case d: ASentThisProjectData => {
      setupBsState(d)
      curRndAHelpBehs = d.numOfHelpBehs
      curUIStateCmd = ViewActor.B_ASentProj(d)
      viewActor ! SetState(makeUIStateBeanItem)
      become(handleAlways orElse handleChat orElse B_readingSentProjectInfo, discardOld = true)
    }
  }
  def B_readingSentProjectInfo: Receive = LoggingReceive {
    case FinishedReadingInfo => {
      curUIStateCmd = ViewActor.B_WorkingOnProj(isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
      testHarnessCall(TestingPhase.WorkingPhase)
      become(handleAlways orElse handleChat orElse B_workingOnGoals orElse B_tutorialMessages, discardOld = true)
    }
  }
  def B_workingOnGoals: Receive = LoggingReceive {
    case WorkOn(part) if (daysLeft <= 0 || metGoal(part)) => {
      log.error("Received a request to work on part: {} when daysLeft = {} or buttonState = {} or metGoal = {}. Attempting to refreshUI.",
        part, daysLeft, buttonState(part), metGoal(part))
      viewActor ! RefreshUI(makeUIStateBeanItem)
    }
    case WorkOn(part) => {
      // We need to guard against doing more than one work at a time. So effectively LOCK this actor
      // and block it from doing more work until this one is finished.
      if (workLock.compareAndSet(false, true)) {
        myGame ! GetNextPartStep(part)
      }
      else {}
      // do nothing in response.
    }
    /* DidWork is the response from above. Not using an ask because that was blocking. */
    case DidWork(part, pStep, partHasNext) => {
      // Now release the workLock to allow another work event.
      // We can release it now because actors are single-threaded.
      workLock.set(false)

      pStepCur(part) = pStep
      buttonState(part) = if (partHasNext) true else false
      daysLeft -= 1
      //    if (daysLeft < 0)
      //      log.error("--A WorkOnProj2-- daysLeft was {}. Trace1: --doWorkAndSetResults -- " +
      //        "Worked on part: {}, daysLeft: {}, metGoal: {}", daysLeft, part, daysLeft, metGoal(part))
      //    log.debug("Trace1: --doWorkAndSetResults -- Worked on part: {}, daysLeft: {}, metGoal: {}",
      //      part, daysLeft, metGoal(part))
      testHarnessCall(TestingPhase.WorkingPhase)

      // since we are B, if we have reached our goal we don't need to work any longer.
      buttonState(part) = metGoal(part) ? false | true
      myPartner ! PartnerState(makeUIStateBeanItem)

      // send partner an update, check if out of workdays, update state, etc.
      if (metAllGoals) {
        buttonState +=("Part1" -> false, "Part2" -> false, "Part3" -> false)
        // A is sent the PartnerSentProject msg from the game object, not from B's Player actor.
        myGame ! BSentProject
        curUIStateCmd = ViewActor.B_FinishedTeamProj(daysLeft, isTutorial = true)
        viewActor ! SetState(makeUIStateBeanItem)
        become(handleAlways orElse bothReadingFinishedInfo, discardOld = true)
      } else if (daysLeft <= 0) {
        buttonState +=("Part1" -> false, "Part2" -> false, "Part3" -> false)
        // A is sent the PartnerSentProject msg from the game object, not from B's Player actor.
        myGame ! BSentProject
        val shortfall = math.abs(Lobby.settings.eqnVal(GameEqn.A_minHelp)(CurGameManipRnd(manipulation, curRnd)) - curRndAHelpBehs)
        curUIStateCmd = ViewActor.B_RanOutOfWorkDays(currentCatPic, shortfall)
        if (currentCatPic >= numCatPics) currentCatPic = 1 else currentCatPic += 1
        viewActor ! SetState(makeUIStateBeanItem)
        become(handleAlways orElse bothReadingFinishedInfo, discardOld = true)
      } else {
        viewActor ! SetState(makeUIStateBeanItem)
      }
    }
  }
  def A_waitingForB: Receive = LoggingReceive {
    // We don't need this so far, because A doesn't watch B during B's work.
    case PartnerState(pState) => {
      watchStatusBar = ""
      //viewActor ! SetState(curUIStateWhenWatchingPartner(pState))
    }
    case PartnerSentProject => {
      //log.error("Implement ShowRace for A")
      curUIStateCmd = ViewActor.A_BFinishedRound
      viewActor ! SetState(makeUIStateBeanItem)
      become(handleAlways orElse bothReadingFinishedInfo, discardOld = true)
    }
    // For tutorial
    // A calls this from TutorialJSViewControl to start B working.
    case ReadyForBToDoTheirWork => {
      // At the moment B is in reading B_readingSentProjectInfo
      myPartner ! FinishedReadingInfo
      myPartner ! ReadyForBToDoTheirWork
    }
  }
  def bothReadingFinishedInfo: Receive = {
    case FinishedReadingInfo => {
      myGame ! ReadyForRace
    }
    case RaceDetails(raceHistory, histOfPosChanges, overallStandings,
    changeInPoints, thisRoundRanking, changeInRanking, historyOfWins, finalPartData) => {
      // Record the finalPartData for later.
      curRndFinalPartData = finalPartData

      raceResults = ViewActor.RaceResults(overallStandings, raceHistory.last, changeInPoints,
        thisRoundRanking, changeInRanking, curRnd, historyOfWins, isTutorial = true).some
      curUIStateCmd = ViewActor.WatchingRace(raceHistory, histOfPosChanges, isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
      become(handleAlways orElse watchingRace(raceResults.get), discardOld = true)
    }
    case WorkOn(part) => {
      //      log.error("Received a request to work on part: {} when daysLeft = {} or buttonState(part) = {}. Attempting to refreshUI.",
      //        part, daysLeft, buttonState(part))
      viewActor ! RefreshUI(makeUIStateBeanItem)
    }
  }
  def watchingRace(raceResults: RaceResults): Receive = LoggingReceive {
    case RaceFinished =>
      curUIStateCmd = raceResults
      viewActor ! SetState(makeUIStateBeanItem)
      become(handleAlways orElse endOfRound)
  }
  def endOfRound: Receive = LoggingReceive {
    case FinishedRaceResults => {
      curUIStateCmd = ViewActor.DamageReport(isTutorial = true)
      viewActor ! SetState(makeUIStateBeanItem)
    }
    case FinishedReadingInfo => {
      // Because this is the Tutorial, we need to display the control questions.
      curUIStateCmd = ViewActor.TutorialSummaryAndQs
      viewActor ! SetState(makeUIStateBeanItem)
    }
    case KillSelfAndGame => {
      myGame ! KillSelfAndGame
    }
  }


  //  /**
  //   * Functions factored out because they are used in more than one state:
  //   */
  //  def doWorkAndSetResults(part: String) {
  //    val fut_ure = myGame ? GetNextPartStep(part, self)
  //    val (pStep, partHasNext) = Await.result(fut_ure, timeout.duration).asInstanceOf[(PartStepData, Boolean)]
  //    pStepCur(part) = pStep
  //    buttonState(part) = if (partHasNext) true else false
  //    daysLeft -= 1
  //    //    if (daysLeft < 0)
  //    //      log.error("--A WorkOnProj2-- daysLeft was {}. Trace1: --doWorkAndSetResults -- " +
  //    //        "Worked on part: {}, daysLeft: {}, metGoal: {}", daysLeft, part, daysLeft, metGoal(part))
  //    //    log.debug("Trace1: --doWorkAndSetResults -- Worked on part: {}, daysLeft: {}, metGoal: {}",
  //    //      part, daysLeft, metGoal(part))
  //    testHarnessCall(TestingPhase.WorkingPhase)
  //  }
  private def curUIStateWhenWatchingPartner(partnerState: UIStateBeanItem): UIStateBeanItem = {
    // don't copy over our team-level goals with the partner's goals.
    val teamGoals = curRndGoals
    val newBean = partnerState.bean
    newBean.part1Goal = teamGoals("Part1")
    newBean.part2Goal = teamGoals("Part2")
    newBean.part3Goal = teamGoals("Part3")
    // don't change our own view.
    newBean.uiCmd = curUIStateCmd
    new UIStateBeanItem(newBean)
  }
  private def makeUIStateBeanItem: UIStateBeanItem = {
    val s = new UIState
    s.uiCmd = curUIStateCmd
    s.curRound = curRnd
    s.daysLeft = daysLeft
    s.part1Name = pStepCur("Part1").curName
    s.part1Next = pStepCur("Part1").nextName
    s.part1StatusBar = pStepCur("Part1").statusBar
    s.part2Name = pStepCur("Part2").curName
    s.part2Next = pStepCur("Part2").nextName
    s.part2StatusBar = pStepCur("Part2").statusBar
    s.part3Name = pStepCur("Part3").curName
    s.part3Next = pStepCur("Part3").nextName
    s.part3StatusBar = pStepCur("Part3").statusBar
    s.partsStartEnd = partsStartEnd
    s.part1Chance = pStepCur("Part1").chance
    s.part2Chance = pStepCur("Part2").chance
    s.part3Chance = pStepCur("Part3").chance
    s.part1NextData = pStepCur("Part1").nextData
    s.part2NextData = pStepCur("Part2").nextData
    s.part3NextData = pStepCur("Part3").nextData
    s.part1CurData = pStepCur("Part1").curData
    s.part2CurData = pStepCur("Part2").curData
    s.part3CurData = pStepCur("Part3").curData
    s.part1Goal = curRndGoals("Part1")
    s.part2Goal = curRndGoals("Part2")
    s.part3Goal = curRndGoals("Part3")
    s.goal1Reached = metGoal("Part1")
    s.goal2Reached = metGoal("Part2")
    s.goal3Reached = metGoal("Part3")
    s.part1Work = buttonState("Part1")
    s.part2Work = buttonState("Part2")
    s.part3Work = buttonState("Part3")
    s.workOnProj2 = buttonStateProj2
    s.proj2SliderMax = daysLeft
    s.proj2DaysWorked = recordOfProj2Work.toMap
    s.watchStatusBar = watchStatusBar
    s.trackNum = curRnd + 1
    s.damagePart1 = damageInSteps("Part1")
    s.damagePart2 = damageInSteps("Part2")
    s.damagePart3 = damageInSteps("Part3")
    s.finalPartData = curRndFinalPartData

    UIStateBeanItem(s)
  }
  def setupInitialRoundState(d: StartNewRoundData) {
    curRnd = d.rnd
    daysLeft = d.workDays
    curRndGoals +=("Part1" -> d.goals.p1DataGoal, "Part2" -> d.goals.p2DataGoal, "Part3" -> d.goals.p3DataGoal)
    pStepCur +=("Part1" -> d.part1, "Part2" -> d.part2, "Part3" -> d.part3)
    buttonState ++= d.buttonStates
    damageInSteps = d.damageInSteps
    partsStartEnd = d.partsMinMax
  }
  def setupBsState(d: ASentThisProjectData) {
    daysLeft = d.workDays
    curRndGoals +=("Part1" -> d.goals.p1DataGoal, "Part2" -> d.goals.p2DataGoal, "Part3" -> d.goals.p3DataGoal)
    pStepCur +=("Part1" -> d.part1.copy(statusBar = ""), "Part2" -> d.part2.copy(statusBar = ""),
      "Part3" -> d.part3.copy(statusBar = ""))
    val p1But = !metGoal("Part1") // if not metGoal, button is enabled (true)
    val p2But = !metGoal("Part2")
    val p3But = !metGoal("Part3")
    buttonState +=("Part1" -> p1But, "Part2" -> p2But, "Part3" -> p3But)
  }
  def metGoal(partNum: String): Boolean = partNum match {
    case "Part1" => (pStepCur("Part1").curData >= curRndGoals("Part1"))
    case "Part2" => (pStepCur("Part2").curData <= curRndGoals("Part2"))
    case "Part3" => (pStepCur("Part3").curData >= curRndGoals("Part3"))
  }
  def metAllGoals: Boolean = (metGoal("Part1") && metGoal("Part2") && metGoal("Part3"))
}



