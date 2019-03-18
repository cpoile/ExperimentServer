package ca.usask.chdp.ExpServerCore.Tutorial

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.event.LoggingReceive
import ca.usask.chdp.ExpServerCore.Models._
import org.joda.time.{Duration, Instant}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic._
import ca.usask.chdp.Enums._
import ca.usask.chdp.ExpServerCore.Game._
import scala.collection._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.DidWork
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.StartNewRoundData
import ca.usask.chdp.ExpServerCore.Models.PartStepData
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.ASentThisProjectData
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.RaceDetails
import ca.usask.chdp.ExpServerCore.Models.GameRecord
import ca.usask.chdp.ExpServerCore.Models.MsgData
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.NotifyListenersOfFinishedGame
import ca.usask.chdp.ExpServerCore.Game.GetNextPartStep
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.ChatMsgReceived
import ca.usask.chdp.ExpServerCore.Game.RegisterAdminListener
import ca.usask.chdp.ExpServerCore.Game.MsgEntered
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.FoundMatch
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import ca.usask.chdp.ExpServerCore.Game.ASentProject

/**
 * A tutorial game is the same as a regular game, except:
 * - uses a tutorial playerLogic.
 * - uses a tutorial gameLogic.
 * @param gameRecord
 */
class TutorialGame(var gameRecord: GameRecord) extends Actor with ActorLogging {

  import context._
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy = OneForOneStrategy( ) { case _: Exception ⇒ Resume
  }

  // this implicit is set at the beginning of every round.
  implicit var gameManipRnd: CurGameManipRnd = null

  // cannot go into prestart because we want these to be object-wide fields.
  val p1 = actorOf(
    Props(
      new TutorialPlayerLogic(
        gameRecord.p1globalId,
        gameRecord.gr_id,
        gameRecord.p1Role,
        gameRecord.manipulation,
        self)), gameRecord.p1globalId)
  val p2 = actorOf(
    Props(
      new TutorialPlayerLogic(
        gameRecord.p2globalId,
        gameRecord.gr_id,
        gameRecord.p2Role,
        gameRecord.manipulation,
        self)), gameRecord.p2globalId)

  // Startup
  // Who is Player A?
  val roleAActor = if (gameRecord.p1Role == "RoleA") p1 else p2
  val roleBActor = if (gameRecord.p1Role == "RoleB") p1 else p2
  assert( roleAActor != roleBActor)
  // just to make sure.
  val role2plrActor = immutable.Map[String, ActorRef]( "RoleA" -> roleAActor, "RoleB" -> roleBActor)
  val plrActor2role = Map[ActorRef, String]( roleAActor -> "RoleA", roleBActor -> "RoleB")

  val plrActor2partnerActor = Map[ActorRef, ActorRef]( p1 -> p2, p2 -> p1)
  val plrActor2globalId = Map[ActorRef, String](
    p1 -> gameRecord.p1globalId,
    p2 -> gameRecord.p2globalId)

  // register with the global map so users can reconnect if they lose their session.
  Lobby.registerActivePlayer( gameRecord.p1globalId, p1, plrActor2role( p1))
  Lobby.registerActivePlayer( gameRecord.p2globalId, p2, plrActor2role( p2))


  var readyForNewRound = immutable.Map.empty[ActorRef, Boolean]
  // build the map of helpful behaviors (and race performance) so we default to 0.
  var initHelpBehMap = Map.empty[String, Int]
  var overallPoints = immutable.Map.empty[String, immutable.Map[String, Int]]
  for (i <- 0 until Lobby.settings.numRounds) {
    initHelpBehMap += (Round( i) -> 0)
    overallPoints += (Round( i) -> Map.empty[String, Int])
  }
  gameRecord = gameRecord.copy( helpfulBehsPerRound = initHelpBehMap)
  log.debug( "initHelpBehMap = {}", initHelpBehMap)

  /**
   * String is PartNum (Part1, Part2, Part3)
   * The Game actor holds the single access to the game's data.
   * It gives each player it's next actions (PartSteps)
   */
  private var pStepIter = immutable.Map[String, Iterator[PartStepData]]( )

  /**
   * Change for TUTORIAL:
   */
  //val gameLogicData = GameLogicData.getData(gameRecord.manipulation)
  val gameLogicData = GameLogicData.getTutorialData


  var roleDaysLeft = Map[String, Int]( )
  var curRnd: Int = 0
  var recordOfProj2Work: Map[Int, Int] = null

  /**
   * String is PartNum (Part1, Part2, Part3)
   * initial setup of PartSteps. This tells us where the player is currently at, and where they go next.
   */
  private var pStepCur = Map.empty[String, PartStepData]
  // Key is PartNum (Part1, etc.), Value is that part's starting PartMark (MarkII, etc.)
  private var curRndBsStartingPartMark = immutable.Map.empty[String, String]

  // stats to record
  var startTimeA: Instant = _
  var persWorkEndTimeA: Instant = _
  var endTimeA: Instant = _
  var helpBehsCount = 0
  var chatMsgCountA = 0
  var chatMsgCountB = 0
  var chatMsgList: List[MsgData] = Nil

  // End of round we send both players the same race data:
  var curRndRaceHistory: List[List[Int]] = null
  var curRndHistoryOfPosChange: List[List[immutable.Map[String, Int]]] = null
  var historyOfWins = mutable.Map.empty[String, Int]
  for (i <- 0 until Lobby.settings.numTeams) {
    historyOfWins += (i.toString -> 0)
  }
  /**
   * To record the final levels the team reached.
   * String is the PartNum (Part1, etc.)
   */
  var finalPartData = immutable.Map.empty[String, Int]

  var adminListeners = List.empty[ActorRef]

  /**
   * Tranform B's partData.
   * We have to transform the pStep based on how far A got.
   * If A got to MarkIV on a part, B is starting fom MarkIV. So make MarkIV into MarkI
   * since from B's point of view it is their first part.
   */
  def transformPartMarkForB(partNum: String, curPartMark: String, incPartBy: Int = 0): String = {
    PartMark(
      PartMarkID( curPartMark) - PartMarkID( curRndBsStartingPartMark( partNum)) + incPartBy)
  }
  /**
   * Returns a partStep that is transformed for B's use. <see>this#transformPartMark
   */
  def transformPartStepForB(partNum: String, pStep: PartStepData): PartStepData = {
    val curName = pStep.getCurName
    val newCurName = transformPartMarkForB( partNum, curName)
    // also change the status bar if this is a completed part
    val statusBar = pStep.statusBar.split( " ")
    val newStatusBar = if (statusBar.last.startsWith( "Mark")) {
      val ret = statusBar.clone( )
      ret( ret.length - 1) = newCurName + "!"
      log.debug( "changed statusBar partMark from {} to {}", statusBar.last, ret( ret.length - 1))
      ret
    } else statusBar
    pStep.copy(
      curName = newCurName,
      nextName = transformPartMarkForB( partNum, curName, 1),
      statusBar = newStatusBar.mkString( " "))
  }

  override def preStart() {
    self ! StartGame
    log.info( "Game created for: {} and {}", p1, p2)
    log.info( "Game partner map: {}", plrActor2partnerActor)
  }
  def setupRound(r: Int) {
    gameManipRnd = CurGameManipRnd( gameRecord.manipulation, 0)
    curRnd = r
    startTimeA = Instant.now
    persWorkEndTimeA = null
    endTimeA = null
    helpBehsCount = 0
    chatMsgCountA = 0
    chatMsgCountB = 0
    val iter1 = gameLogicData.round( Round( curRnd)).part( "Part1").steps.iterator
    val iter2 = gameLogicData.round( Round( curRnd)).part( "Part2").steps.iterator
    val iter3 = gameLogicData.round( Round( curRnd)).part( "Part3").steps.iterator
    pStepIter = immutable.Map( "Part1" -> iter1, "Part2" -> iter2, "Part3" -> iter3)
    pStepCur = immutable.Map(
      "Part1" -> pStepIter( "Part1").next( ),
      "Part2" -> pStepIter( "Part2").next( ),
      "Part3" -> pStepIter( "Part3").next( ))
    roleDaysLeft +=("RoleA" -> Lobby.settings.eqnVal( GameEqn.A_max), "RoleB" ->
      Lobby.settings.eqnVal( GameEqn.B_max))

    // Notify admin listeners
    for (l <- adminListeners) {l ! GameRecordDAO.makeGameInfo( gameRecord.gr_id, Round( curRnd)) }
  }

  def getStartNewRoundData(role: String): StartNewRoundData = {
    val goals = role match {
      case "RoleA" => gameLogicData.round( Round( curRnd)).aGoals
      case "RoleB" => gameLogicData.round( Round( curRnd)).bGoals
    }
    StartNewRoundData(
      role,
      curRnd,
      roleDaysLeft( role),
      goals,
      pStepCur( "Part1"),
      pStepCur( "Part2"),
      pStepCur( "Part3"),
      immutable.Map( "Part1" -> true, "Part2" -> true, "Part3" -> true),
      gameLogicData.round( Round( curRnd)).damageInSteps,
      immutable.Map(
        "Part1" ->(
          gameLogicData.round( Round( curRnd)).part( "Part1").steps( 0).curData, gameLogicData
                                                                                 .round(
          Round( curRnd)).part( "Part1").steps.last.nextData),
        "Part2" ->
          (gameLogicData.round( Round( curRnd)).part( "Part2").steps( 0).curData, gameLogicData
                                                                                  .round(
            Round( curRnd)).part( "Part2").steps.last.nextData),
        "Part3" ->
          (gameLogicData.round( Round( curRnd)).part( "Part3").steps( 0).curData, gameLogicData
                                                                                  .round(
            Round( curRnd)).part( "Part3").steps.last.nextData)))
  }
  def BFinished_AssembleGameRecordWithEndTimeNow: GameRecord = {
    val endTimeB = Instant.now
    val helpBehsMap = gameRecord.helpfulBehsPerRound + (Round( curRnd) -> helpBehsCount)
    val chatMsgsMapA = gameRecord.chatMsgsPerRoundFromA + (Round( curRnd) -> chatMsgCountA)
    val chatMsgsMapB = gameRecord.chatMsgsPerRoundFromB + (Round( curRnd) -> chatMsgCountB)
    val dur = new Duration( startTimeA, endTimeA).getMillis
    val totalTimeA = gameRecord.totalTimeForAPerRound + (Round( curRnd) -> dur)
    val totalTimeB = gameRecord.totalTimeForBPerRound +
      (Round( curRnd) -> new Duration( endTimeA, endTimeB).getMillis)
    val persGoalsTimeA = gameRecord.persGoalsTimeForAPerRound +
      (Round( curRnd) -> new Duration( startTimeA, persWorkEndTimeA).getMillis)
    val extraWorkTimeA = gameRecord.extraWorkTimeForAPerRound +
      (Round( curRnd) -> new Duration( persWorkEndTimeA, endTimeA).getMillis)

    //    log.debug("Trace2: Entered into game object: \n helpBehsMap: {} \n chatMsgsMapA: {} \n chatMsgsMapB: {} \n" +
    //      "totalTimeA: {} \n totalTimeB: {} \n persGoalsTimeA: {} \n extraWorkTimeA: {}",
    //      Array(helpBehsMap, chatMsgsMapA, chatMsgsMapB, totalTimeA, totalTimeB, persGoalsTimeA, extraWorkTimeA))

    gameRecord.copy(
      round = Round( curRnd),
      helpfulBehsPerRound = helpBehsMap,
      chatMsgsPerRoundFromA = chatMsgsMapA,
      chatMsgsPerRoundFromB = chatMsgsMapB,
      totalTimeForAPerRound = totalTimeA,
      totalTimeForBPerRound = totalTimeB,
      persGoalsTimeForAPerRound = persGoalsTimeA,
      extraWorkTimeForAPerRound = extraWorkTimeA,
      overallStandingsPerTeamPerRound = overallPoints)
  }
  def handleAlways: Receive = {
    case RegisterAdminListener( l) => {
      if (!adminListeners.contains( l)) {
        adminListeners = l :: adminListeners
      }
      // Send an update immediately.
      l ! GameRecordDAO.makeGameInfo( gameRecord.gr_id)
    }
    case KillSelfAndGame => {

      context.stop( self)
    }
  }

  def receive = handleAlways orElse preStartGame

  def preStartGame: Receive = LoggingReceive { case StartGame => {
    setupRound( 0)
    p1 ! FoundMatch( p2)
    p2 ! FoundMatch( p1)
    p1 ! getStartNewRoundData( plrActor2role( p1))
    p2 ! getStartNewRoundData( plrActor2role( p2))
    become( handleAlways orElse gameStarted)
  }
  }
  def gameStarted: Receive = LoggingReceive { case MsgEntered( msg) => {
    plrActor2role( sender) match {
      case "RoleA" => chatMsgCountA += 1
      case "RoleB" => chatMsgCountB += 1
    }
    // Construct the full ChatMessageLst case object to persist
    val sent = MsgData(
      Model.counter( "msg"),
      gameRecord.gr_id,
      plrActor2globalId( sender),
      msg,
      Round( curRnd),
      plrActor2role( sender))
    MsgDAO.insert( sent)
    chatMsgList = sent :: chatMsgList
    //log.error("Msg received, logged as sent from A, ID: {}, msg info: {}", plrGlobalID(sender), sent, msg )
    p1 ! ChatMsgReceived( sent :: Nil)
    p2 ! ChatMsgReceived( sent :: Nil)
  }
  case SendMeAllMsgs => {
    sender ! ChatMsgReceived( chatMsgList.reverse)
  }
  case GetNextPartStep( part) => {
    // We assume that the command is Valid, because the player's logic is held in their Actor, not here.
    // Our responsibility is only to keep the access to the iterators in a single-threaded place.
    // Also, notify when we are finished working on this part (no more days to work left -- reached B's goals)
    if (pStepIter( part).hasNext) {
      pStepCur = pStepCur + (part -> pStepIter( part).next( ))
      val hasNext = pStepIter( part).hasNext
      if (plrActor2role( sender) == "RoleB") {
        sender ! DidWork( part, transformPartStepForB( part, pStepCur( part)), hasNext)
      } else {
        sender ! DidWork( part, pStepCur( part), hasNext)
      }
    } else {
      log.error( "Ran out of partsteps to use, very bad. Shouldn't happen.")
    }
  }
  case AFinishedPersGoals =>
    persWorkEndTimeA = Instant.now
  case HelpfulBeh => {
    helpBehsCount += 1
    //      log.error("received helpful beh from A. Helpful behs count is: {}, the map is: {}", helpBehsCount, gameRecord.helpfulBehsPerRound)
  }
  case ASentProject( recOfProj2Work) => {
    recordOfProj2Work = recOfProj2Work
    endTimeA = Instant.now
    // Build B's starting data.
    // We have to transform the pStep based on how far A got.
    // If A got to MarkIV on a part, B is starting fom MarkIV. So make MarkIV into MarkI
    // since from B's point of view it is their first part.
    curRndBsStartingPartMark = immutable.Map(
      ("Part1" -> pStepCur( "Part1").getCurName),
      ("Part2" -> pStepCur( "Part2").getCurName),
      ("Part3" -> pStepCur( "Part3").getCurName))

    role2plrActor( "RoleB") ! ASentThisProjectData(
      roleDaysLeft( "RoleB"),
      gameLogicData.round( Round( curRnd)).bGoals,
      transformPartStepForB( "Part1", pStepCur( "Part1")),
      transformPartStepForB( "Part2", pStepCur( "Part2")),
      transformPartStepForB( "Part3", pStepCur( "Part3")),
      helpBehsCount,
      partTopartMark = Map(
        "Part1" -> pStepCur( "Part1").curName,
        "Part2" -> pStepCur( "Part2").curName,
        "Part3" -> pStepCur( "Part3").curName))
  }
  case BSentProject => {
    role2plrActor( "RoleA") ! PartnerSentProject

    // Record the level they reached overall for when we report damage.
    finalPartData = immutable.Map(
      ("Part1" -> pStepCur( "Part1").curData),
      ("Part2" -> pStepCur( "Part2").curData),
      ("Part3" -> pStepCur( "Part3").curData))

    // Generating the race here because we only do it one, then send the same data out to each player.
    // create this round's race based on A's helpful behaviors.
    // Tutorial Change: always 2nd manipulation (low dep then high), and first round.
    curRndRaceHistory = createRace(
      Lobby.settings.trackSeq( curRnd),
      helpBehsCount,
      Lobby.settings.numTeams,
      2,
      0)
    curRndHistoryOfPosChange = genHistoryOfPosChange( curRndRaceHistory)
    historyOfWins( curRndRaceHistory.last( 0).toString) += 1
    overallPoints = overallPoints + (Round( curRnd) ->
      updateOverallPoints( curRndRaceHistory.last, overallPoints, curRnd, Lobby.settings.numTeams))
    readyForNewRound = immutable.Map( p1 -> false, p2 -> false)

    // record this round's information only once.
    // NOTE: we are doing this here, and only once, because assembling the game record
    // records the timing of when B finished (i.e., NOW).
    gameRecord = BFinished_AssembleGameRecordWithEndTimeNow
    GameRecordDAO.insertUpdate( gameRecord)
  }
  case ReadyForRace => {
    val lastRoundPts: immutable.Map[String, Int] = getLastRoundPointsIfAny(
      curRnd,
      Lobby.settings.numTeams,
      overallPoints.toMap)
    val changeInPts = getChangeInPoints( lastRoundPts, overallPoints( Round( curRnd)))
    val lastRoundRanking = getRoundRankingIfAny(
      curRnd - 1,
      Lobby.settings.numTeams,
      overallPoints.toMap)
    val thisRoundRanking = getRoundRankingIfAny(
      curRnd,
      Lobby.settings.numTeams,
      overallPoints.toMap)
    val changeInRanking = getChangeInRanking( lastRoundRanking, thisRoundRanking)
    sender ! RaceDetails(
      curRndRaceHistory,
      curRndHistoryOfPosChange,
      overallPoints( Round( curRnd)),
      changeInPts,
      thisRoundRanking,
      changeInRanking,
      historyOfWins.toMap,
      finalPartData)
    // And after they are finished watching the race, don't start next round until they're both ready.
  }
  case RoundFinished => {
    // one of the players is finished her round. Check logic to see what to do and enter
    // state of waiting
    readyForNewRound += (sender -> true)
    // could do this:
    // if(readyForNewRound.foldLeft(true)((b1, act) => b1 == act._2))
    // or more simply:
    if (readyForNewRound( plrActor2partnerActor( sender))) {
      // if this current round (1-based) is less than the total number of rounds (1 based)
      if (curRnd + 1 < Lobby.settings.numRounds) {
        val nextRnd = RoundIfAddX( curRnd, 1)
        setupRound( nextRnd)
        p1 ! getStartNewRoundData( plrActor2role( p1))
        p2 ! getStartNewRoundData( plrActor2role( p2))
      } else {
        p1 ! FinishedGame
        p2 ! FinishedGame
        log.debug( "notifying listeners of finished game.")
        Lobby.lobby ! NotifyListenersOfFinishedGame( gameRecord.gr_id)
      }
    }
  }
  case GetGameRecord => sender ! gameRecord
  }
}



