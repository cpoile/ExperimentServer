package ca.usask.chdp.ExpServerCore.ExpActors

import akka.actor._
import scala.concurrent.duration._
import ca.usask.chdp.ExpServerCore.View.ViewManager
import util.Random
import ca.usask.chdp.ExpServerCore.Models.{GameRecordDAO, GameRecord, Model}
import org.joda.time.DateTime
import com.typesafe.config.{ConfigFactory, Config}
import ca.usask.chdp.ExpServerCore.ExpActors.ExpCoreSupervisor.{CreateViewActorForAReconnectingWaitingPlayer, ReconnectViewActor, CreateViewActor}
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID
import scalaz._
import Scalaz._
import ca.usask.chdp.ExpSettings
import ca.usask.chdp.ExpServerCore.Game.RegisterAdminListener
import Lobby._
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic._
import ca.usask.chdp.models.Msgs._
import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.{ViewMsg, LobbyStats, YouAreRegisteredInLobby}
import ca.usask.chdp.ExpServerCore.Tutorial.{FakeViewActor, TutorialGame}
import scala.annotation.tailrec

/**
  * counter(dt.toString("yyyy-MM-dd") + "-sess")
  */
class Lobby extends Actor with ActorLogging {

  import context._
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy = OneForOneStrategy() {
    case _: Exception ⇒ Resume
  }

  /**
    * Lobby state.
    */
  private var playersGettingReady = List.empty[WaitingPlayer]
  private var playersReady = List.empty[WaitingPlayer]
  private var listenersForWaitingPlayers = List.empty[ActorRef]
  private var listenersForReadyPlayers = List.empty[ActorRef]
  private var games = List.empty[GameIdAndActor]
  // start at the end so that first assignment is first.
  private var lastAssignedManipRole: (Int, String) = (2, "RoleB")

  // Index: gr_Id
  private var finishedGames = List.empty[String]
  private var listenersForGames = List.empty[ActorRef]
  private var expSessionID: String = "Uninitialized @ " + DateTime.now
  private var isGameStarted = false
  private var allowLoginCountdownCancellable: Option[Cancellable] = None

  /**
    * Set up a recurring event that will tell all waiting players the lobbyStats. If the waiting players
    * have an attached viewManager (they won't if they are test probes, for example.) But all real WaitingPlayers
    * Will have a viewActor, @see ca.usask.chdp.ExpServerCore.View.VaadinViewManager#receive case YouHaveAViewActor
    */
  val cancellable_waitingInLobbyStatsTick = system.scheduler.schedule(1 second,
    3 seconds,
    self,
    NotifyWaitingLobbyStats)

  /**
    * Set up a one time event to auto start the game during testing.
    */

  import context.dispatcher

  def receive = {
    /**
      * These are interactions with the admin actor (and lobby tester):
      */
    case NotifyWaitingLobbyStats => {
      playersReady foreach {
        _.viewActor foreach { p =>
          p ! LobbyStats(playersGettingReady.length + playersReady.length, playersReady.length)
        }
      }
    }
    case GetWaitingPlayersInAllStages => {
      sender ! playersGettingReady ++ playersReady
    }
    case RegisterListenerForReadyUsers(listener) => {
      // first send all existing users, then send all further users.
      log.debug("ExpServerCore -- Received: RegisterListenerForReadyUsers from --- {}", listener)

      val allReady = (for (pr <- playersReady) yield ReadyPlayerInfo(pr.globalId,
        pr.manipulation, pr.role, pr.location)).toList
      listener ! AllReadyPlayerInfo(allReady)
      if (!listenersForReadyPlayers.contains(listener)) {
        listenersForReadyPlayers = listener :: listenersForReadyPlayers
      }
    }
    case RegisterListenerForGettingReadyUsers(listener) => {
      // first send all existing users, then send all later users.
      log.debug("ExpServerCore -- Received: RegisterListenerForGettingReadyUsers from --- {}", listener)

      // Send them all at first, to refresh the admin view.
      val allGettingReady = (for (pr <- playersGettingReady) yield WaitingPlayerInfo(pr.globalId,
        pr.manipulation, pr.role, pr.location)).toList
      listener ! AllWaitingPlayerInfo(allGettingReady)
      if (!listenersForWaitingPlayers.contains(listener)) {
        listenersForWaitingPlayers = listener :: listenersForWaitingPlayers
      }
    }
    case RegisterListenerForGames(listener) => {
      log.debug("ExpServerCore received: RegisterListenerForGames from --- {}", listener)
      for (g <- games) {
        g.gameActor ! RegisterAdminListener(listener)
      }

      // refresh the new listener will all games, underway and finished:
      val allGames = (for (g <- games) yield GameRecordDAO.makeGameInfo(g.gr_id)).toList
      listener ! AllGameInfo(allGames)
      val allFinishedGames = (for (g <- finishedGames) yield GameRecordDAO.makeGameInfo(g)).toList
      listener ! AllFinishedGameInfo(allFinishedGames)

      // However, if we already have them as a listener, don't add them again.
      // TODO: how to remove dead listeners?
      if (!listenersForGames.contains(listener)) {
        listenersForGames = listener :: listenersForGames
      }
    }
    case NewSessionId => {
      // Unique, database-wide experiment session ID.
      val dt = DateTime.now
      expSessionID = Model.counter(dt.toString("yyyy-MM-dd") + "-sess")
      log.warning("Lobby: starting a new game. ID: {} on {}", expSessionID, DateTime.now)
    }
    case AllowLogins(allow) => {
      // first cancel any pending automatic change because we've manually changed the state.
      allowLoginCountdownCancellable foreach (_.cancel())
      _isLoginAllowed = allow
      // setup countdown so that we don't admit students in next session before starting a new session.
      if (allow)
        allowLoginCountdownCancellable =
          Lobby.system.scheduler.scheduleOnce(60 minutes) {
            _isLoginAllowed = false
          }.some
    }
    case StartGameMatchPlayers => {
      isGameStarted = true
      self ! MatchPlayersInLobby
    }
    case LobbyStopGame => {
      isGameStarted = false
    }
    case ResetSession_DANGEROUS => {
      log.warning("*************************** ResetSession called.")
      playersGettingReady = Nil
      playersReady = Nil
      listenersForWaitingPlayers = Nil
      games = Nil
      listenersForGames = Nil
      isGameStarted = false
      lastAssignedManipRole = (2, "RoleB")
      self ! NewSessionId
    }
    case NotifyListenersOfFinishedGame(gameId) => {
      finishedGames = gameId :: finishedGames
      games = games.filterNot(_.gr_id == gameId)
      for (l <- listenersForGames) {
        l ! FinishedGameInfo(GameRecordDAO.makeGameInfo(gameId))
      }
    }
    case RequestUpdateOnSessionInfo => {
      sender ! SessionInfo(expSessionID, _isLoginAllowed, isGameStarted, playersGettingReady.length, playersReady.length,
        playersGettingReady.length + playersReady.length)
    }
    case RequestUpdateOnGettingStartedUsers => {
      val allGettingReady = (for (pr <- playersGettingReady) yield WaitingPlayerInfo(pr.globalId,
        pr.manipulation, pr.role, pr.location)).toList
      sender ! AllWaitingPlayerInfo(allGettingReady)
    }
    case RequestUpdateOnReadyUsers => {
      val allReady = (for (pr <- playersReady) yield ReadyPlayerInfo(pr.globalId,
        pr.manipulation, pr.role, pr.location)).toList
      sender ! AllReadyPlayerInfo(allReady)
    }
    case ChangeWaitingPlayerInfo(newInfo) => {
      // find the global id in the waiting list and replace it with this one.
      val oldInfo = playersGettingReady.find(_.globalId == newInfo.globalId) | WaitingPlayer("", "")
      val newWaitingPlayer = oldInfo.copy(manipulation = newInfo.manipulation, role = newInfo.role)
      Lobby.unRegisterWaitingPlayerInfo(oldInfo.globalId)
      Lobby.registerWaitingPlayerInfo(newWaitingPlayer)
      playersGettingReady = newWaitingPlayer :: playersGettingReady.filterNot(_.globalId == newInfo.globalId)
    }
    case ChangeReadyPlayerInfo(newInfo) => {
      // find the global id in the waiting list and replace it with this one.
      val oldInfo = playersReady.find(_.globalId == newInfo.globalId) | WaitingPlayer("", "")
      val newWaitingPlayer = oldInfo.copy(manipulation = newInfo.manipulation, role = newInfo.role)
      playersReady = newWaitingPlayer :: playersReady.filterNot(_.globalId == newInfo.globalId)
      Lobby.unRegisterWaitingPlayerInfo(oldInfo.globalId)
      Lobby.registerWaitingPlayerInfo(newWaitingPlayer)
    }
    case RemoveWaitingPlayerInfo(info) => {
      playersGettingReady = playersGettingReady.filterNot(_.globalId == info.globalId)
      Lobby.unRegisterWaitingPlayerInfo(info.globalId)
    }
    case RemoveReadyPlayerInfo(info) => {
      playersReady = playersReady.filterNot(_.globalId == info.globalId)
      Lobby.unRegisterWaitingPlayerInfo(info.globalId)
    }

    /**
      * Interaction with Players:
      * ------------------------------------------------------------------------------------------------------
      */
    case newPlayer: WaitingPlayer => {
      // First, attach their viewActor to the the waitingPlayer.
      // This was given when the ViewManager sent Lobby.giveMeViewActor
      val newPlayerWithVA = newPlayer.copy(viewActor = Lobby.idToViewActor.get(newPlayer.globalId))

      // Assign arriving players a manipulation and role.
      // For now, whether we use one or two manipulations is set in the application.conf.
      log.debug("assigning new player manip and role: {}", newPlayerWithVA)

      val transNewPlayer = if (settings.usingBothManipulations) {
        if (settings.diffLocationsMatter)
          assignManipRoleInSeqForTwoManipulations(newPlayerWithVA, lastAssignedManipRole)
        else
          throw (new IllegalArgumentException("Have not setup system to match 2 locations AND 2 manipulations."))
      } else if (settings.diffLocationsMatter && settings.diffLocationsAreMatchedAcrossLocation) {
        val newRole = roleForOneManipMultipleLocations(newPlayerWithVA.location, settings.allLocations,
          getMapOfRolesListByLocation)
        newPlayerWithVA.copy(manipulation = settings.manipIfNotUsingBoth, role = newRole)
      } else {
        val newRole = roleForOneManipulation(playersGettingReady.map(_.role) ++ playersReady.map(_.role))
        newPlayerWithVA.copy(manipulation = settings.manipIfNotUsingBoth, role = newRole)
      }

      log.debug("Received newplayer --- {} ", transNewPlayer)
      playersGettingReady = transNewPlayer :: playersGettingReady
      for (l <- listenersForWaitingPlayers) {
        l ! WaitingPlayerInfo(transNewPlayer.globalId,
          transNewPlayer.manipulation, transNewPlayer.role, transNewPlayer.location)
      }
      log.info("Lobby now contains usersGettingReady: {}", playersGettingReady)

      // Tell the user's ViewActor (and the ViewManager) that they have a manip and role
      Lobby.idToViewActor(transNewPlayer.globalId) ! YouAreRegisteredInLobby(transNewPlayer)
    }
    case StartTutorial(waitingPlayer) => {
      // We need to set up a fake opponent.
      // Give them a fake view actor, then make a fake waitingPlayer with a fake globalID,
      // then register the fake view actor with the fake globalID
      val fakeActorRole = if (waitingPlayer.role == "RoleA") "RoleB" else "RoleA"
      val fakeViewActor = context.actorOf(Props[FakeViewActor], name = Model.counter("fakeViewActor"))
      val fakeOpponent = WaitingPlayer(Model.counter("fakeOpponent"), "fakeEmail",
        waitingPlayer.manipulation, fakeActorRole, Some(fakeViewActor))
      Lobby.registerViewActor(fakeOpponent.globalId, fakeViewActor)

      // Set up a fake game using the tutorial gameLogic. Start the fake game.
      val gr_id = Model.counter("tutorial")
      val gameRecord = GameRecord(gr_id, gr_id, expSessionID, waitingPlayer.manipulation,
        waitingPlayer.globalId, waitingPlayer.role, waitingPlayer.email,
        fakeOpponent.globalId, fakeOpponent.role, fakeOpponent.email)

      // No need to send a message to initialize game, the game inits itself when created.
      val game = actorOf(Props(new TutorialGame(gameRecord)), gr_id)
      log.info("Lobby: starting Tutorial for --- {}", waitingPlayer.globalId)
    }
    case PlayerFinishedInstructions(waitingPlayer) => {
      // get the player as they exist in the lobby's list, not the one from their UI. Because it
      // might have been changed by admin i n the meantime.
      val curWaitingPlayer = playersGettingReady.find(_.globalId == waitingPlayer.globalId) | WaitingPlayer("", "")
      playersReady = curWaitingPlayer :: playersReady
      // they are no longer getting ready
      // use globalId just incase something changed.
      playersGettingReady = playersGettingReady.filterNot(_.globalId == waitingPlayer.globalId)
      for (l <- listenersForWaitingPlayers) {
        l ! RemoveWaitingPlayers(curWaitingPlayer.globalId :: Nil)
      }

      for (l <- listenersForReadyPlayers) {
        l ! ReadyPlayerInfo(curWaitingPlayer.globalId,
          curWaitingPlayer.manipulation, curWaitingPlayer.role, curWaitingPlayer.location)
      }
      log.info("Lobby now contains usersReady: {}", playersReady)

      if (isGameStarted) {
        self ! MatchPlayersInLobby
      }
    }
    case MatchTheseTwoPlayers(p1, p2) => {
      startGameForPair(MatchedPair(p1, p2))
    }
    case MatchPlayersInLobby => {
      log.info("Lobby: asked to match all players. playerswaiting: {}", playersReady.length)
      // Every pair will have a possible partner, because of how we are assigning roles.
      // match the first player in the list, if we can.

      /**
        * This is where we choose which matching strategy to use.
        */
      val matchingStrategy: (List[WaitingPlayer]) => PossibleMatchedPair
      = if (ExpSettings.get.diffLocationsMatter && ExpSettings.get.diffLocationsAreMatchedAcrossLocation) {
        matchBetweenLocationsStrategy
      } else {
        simpleRandomMatchingStrategy
      }

      val pairsAndUnmatched = matchAllPlayers(playersReady, matchingStrategy)
      for (pair <- pairsAndUnmatched.matchedPairs) {
        if (!settings.testing_unitTests_doNotStartGames) {
          startGameForPair(pair)
        }
        playersReady = playersReady.diff(List(pair.plr1, pair.plr2))
        // Also remove them from the lobby's tracking id -> waitingPlayer, then they won't
        // receive one when reconnecting (could be bad, but maybe not)
        Lobby.unRegisterWaitingPlayerInfo(pair.plr1.globalId)
        Lobby.unRegisterWaitingPlayerInfo(pair.plr2.globalId)
      }
      log.debug("pairsAndUnmatched.unMatched: {} --- playersReady -- {}",
        pairsAndUnmatched.unMatched, playersReady)
      assert(pairsAndUnmatched.unMatched.length == playersReady.length)
    }
  }

  def startGameForPair(pair: MatchedPair) {
    val gr_id = Model.counter("game")
    val gameRecord = GameRecord(gr_id, gr_id, expSessionID, pair.plr1.manipulation,
      pair.plr1.globalId, pair.plr1.role, pair.plr1.email, pair.plr2.globalId, pair.plr2.role, pair.plr2.email)
    GameRecordDAO.insertUpdate(gameRecord)
    // No need to send a message to initialize game, the game inits itself when created.
    val game = actorOf(Props(new Game(gameRecord)), gr_id)
    games = GameIdAndActor(gr_id, game) :: games

    // Now update listeners about new game and removal of waiting players.
    // TODO: how do we remove dead waiting players?
    for (l <- listenersForGames) {
      game ! RegisterAdminListener(l)
    }
    for (l <- listenersForWaitingPlayers) {
      l ! RemoveReadyPlayers(List(pair.plr1.globalId, pair.plr2.globalId))
      log.info("Lobby: Matched two players --- {} --- {}", pair.plr1.globalId, pair.plr2.globalId)
    }
  }

  def getMapOfRolesListByLocation: Map[String, List[String]] = {
    val loc1 = settings.defaultLocation
    val loc2 = settings.otherLocation

    Map(loc1 -> (playersGettingReady ++ playersReady).filter(_.location == loc1).map(_.role),
      loc2 -> (playersGettingReady ++ playersReady).filter(_.location == loc2).map(_.role))
  }

}

/**
  * companion object for helper functions and case object/class messages to Lobby.
  */
object Lobby {

  import collection.JavaConversions._
  import scalaz._
  import Scalaz._

  // Data for the lobby to use. Only accessed by lobby and Lobby, please.
  private var _isLoginAllowed = false

  /**
    * For passing data around, not actor messages
    */
  case class PossibleMatchedPair(plr1: Option[WaitingPlayer], plr2: Option[WaitingPlayer]) {
    def hasPlayer(p: WaitingPlayer): Boolean =
      (plr1.exists(_.globalId == p.globalId) || plr2.exists(_.globalId == p.globalId))

    def isDefined: Boolean = (plr1.isDefined && plr2.isDefined)

    def asList: List[WaitingPlayer] = List(plr1, plr2).flatten

    // If both exist, returns Some(MatchedPair) else None
    def toMatchedPair: Option[MatchedPair] = if (isDefined) MatchedPair(plr1.get, plr2.get).some else None
  }

  case class MatchedPair(plr1: WaitingPlayer, plr2: WaitingPlayer)

  case class PairsAndUnmatched(matchedPairs: List[MatchedPair], unMatched: List[WaitingPlayer])

  case class GameIdAndActor(gr_id: String, gameActor: ActorRef)

  /**
    * Actor messages:
    */
  sealed trait LobbyMsgs

  case class GameActor(g: ActorRef) extends LobbyMsgs

  case class WaitingPlayer(globalId: String, email: String, manipulation: Int = 0, role: String = "",
                           viewActor: Option[ActorRef] = None,
                           location: String = "",
                           // brittle, I know.
                           currentStageOfWaiting: String = "") extends LobbyMsgs

  case object MatchPlayersInLobby extends LobbyMsgs

  case class ReturningUserInfo(playerInfo: PlayerInfo, msg: PlayerMsgs) extends LobbyMsgs

  case object NotifyWaitingLobbyStats extends LobbyMsgs

  case class PlayerFinishedInstructions(waitingPlayer: WaitingPlayer) extends LobbyMsgs

  // for tutorial
  case class StartTutorial(waitingPlayer: WaitingPlayer) extends LobbyMsgs

  // for testing
  case class MatchTheseTwoPlayers(p1: WaitingPlayer, p2: WaitingPlayer) extends LobbyMsgs

  case object GetWaitingPlayersInAllStages extends LobbyMsgs

  case class NotifyListenersOfFinishedGame(gameId: String) extends LobbyMsgs


  val log = LoggerFactory.getLogger("Lobby-Object")

  private var mySystem: Option[ActorSystem] = None
  private var expCoreSup: Option[ActorRef] = None
  private var mySettings: Option[ExpSettings] = None
  private val leavingUsers: collection.concurrent.Map[String, ReturningUserInfo] = new ConcurrentHashMap[String, ReturningUserInfo]
  val idToViewActor: collection.concurrent.Map[String, ActorRef] = new ConcurrentHashMap[String, ActorRef]
  val idToPlayerLogic: collection.concurrent.Map[String, ActorRef] = new ConcurrentHashMap[String, ActorRef]
  val idToRole: collection.concurrent.Map[String, String] = new ConcurrentHashMap[String, String]
  val idToViewManager: collection.concurrent.Map[String, ViewManager] = new ConcurrentHashMap[String, ViewManager]
  val idToWaitingPlayer: collection.concurrent.Map[String, WaitingPlayer] = new java.util.concurrent.ConcurrentHashMap[String, WaitingPlayer]
  var lobbyRef: Option[ActorRef] = None

  def system: ActorSystem = {
    mySystem.getOrElse(throw new IllegalStateException("Have not initialized Lobby yet, cannot get actor System."))
  }

  def actorSystemInitialize(config: Option[Config] = None, system: Option[ActorSystem] = None): ActorSystem = {
    mySystem | {
      val cfg = initSettings(config)
      mySystem = Some(system.getOrElse(ActorSystem("Exp", cfg)))
      expCoreSup = Some(mySystem.get.actorOf(Props[ExpCoreSupervisor], "Sys"))
      log.debug("started system: {} and expCoreSup: {}", mySystem.get.name, expCoreSup.get.path)
      mySystem.get
    }
  }

  def registerLobby(theLobby: ActorRef) {
    lobbyRef = theLobby.some
  }

  def lobby: ActorRef = {
    lobbyRef | (throw new IllegalStateException("Have not initialized Lobby yet, cannot add a new player."))
  }

  def reInitialize(config: Option[Config] = None, system: Option[ActorSystem] = None): ActorSystem = {
    mySystem match {
      case None =>
        actorSystemInitialize(config, system)
      case Some(x) => {
        x.shutdown()
        x.awaitTermination()
        actorSystemInitialize(config, system)
      }
    }
  }

  def giveMeViewActor(globalID: String, email: String, location: String, viewManager: ViewManager) {
    // get the player's view actor from the expCoreSup just so that if it crashed it can be restarted.
    expCoreSup.getOrElse(
      throw new IllegalStateException("Have not initialized Lobby yet, cannot add a new player.")
    ) ! CreateViewActor(globalID, email, location, viewManager)
  }

  def giveMeViewActorForAReconnectingWaitingPlayer(wp: WaitingPlayer, vm: ViewManager) {
    // get the player's view actor from the expCoreSup just so that if it crashed it can be restarted.
    expCoreSup.getOrElse(
      throw new IllegalStateException("Have not initialized Lobby yet, cannot add a new player.")
    ) ! CreateViewActorForAReconnectingWaitingPlayer(wp, vm)
  }

  def retrieveMyViewActor(globalID: String, viewManager: ViewManager, userClosure: ReturningUserInfo) {
    expCoreSup.getOrElse(throw new IllegalStateException("Have not initialized Lobby yet, cannot add a new player.")
    ) ! ReconnectViewActor(globalID, viewManager, userClosure)
  }

  def registerActivePlayer(globalID: String, playerLogic: ActorRef, role: String) {
    // TODO: send to adminView
    idToPlayerLogic += (globalID -> playerLogic)
    idToRole += (globalID -> role)
  }

  def registerViewActor(globalID: String, viewActor: ActorRef) {
    log.debug("Lobby registerViewActor -- globalID: {} -- viewactor: {}", globalID, viewActor)
    idToViewActor += (globalID -> viewActor)
  }

  def registerViewManager(globalID: String, viewManager: ViewManager) {
    idToViewManager += (globalID -> viewManager)
  }

  def registerWaitingPlayerInfo(waitingPlayer: WaitingPlayer) {
    log.debug("Lobby registerWaitingPlayerInfo -- {}", waitingPlayer)
    idToWaitingPlayer += (waitingPlayer.globalId -> waitingPlayer)
  }

  def unRegisterWaitingPlayerInfo(waitingPlayerId: String) {
    log.debug("Lobby UN -registerWaitingPlayerInfo -- {}", idToWaitingPlayer(waitingPlayerId))
    idToWaitingPlayer.remove(waitingPlayerId)
  }

  def removePreviousViewManager(globalID: String): Option[ViewManager] = {
    idToViewManager.remove(globalID)
  }

  def isUserResuming(globalId: String): Option[ReturningUserInfo] = {
    if (idToPlayerLogic.contains(globalId) && idToRole.contains(globalId) && idToViewActor.contains(globalId))
      ReturningUserInfo(PlayerInfo(globalId, idToRole(globalId), idToPlayerLogic(globalId)), ReturningUser).some
    else
      none
  }

  def newPlayer(player: WaitingPlayer) {
    lobby ! player
  }

  // Can't join the server after 45 into a session. Triggers us to remember to reset.
  def isLoginAllowed: Boolean = _isLoginAllowed

  // For integration testing.
  def matchTheseTwoPlayers(p1: WaitingPlayer, p2: WaitingPlayer) {
    expCoreSup.getOrElse(
      throw new IllegalStateException("Have not initialized Lobby yet, cannot add a new player.")
    ) ! MatchTheseTwoPlayers(p1, p2)
  }

  def settings = mySettings.getOrElse(throw new IllegalStateException("Have not initialized yet, cannot get config."))

  def initSettings(config: Option[Config] = None): Config = {
    val cfg = config.getOrElse(ConfigFactory.load())
    mySettings = new ExpSettings(cfg).some
    //mySettings = Some(mySettings.getOrElse(new ExpSettings(cfg)))
    cfg
  }

  def registerLeavingUser(closure: ReturningUserInfo): String = {
    // check if this user has already left. if so, invalidate old UUID. Give them new UUID.
    for ((k, v) <- leavingUsers) {
      if (v.playerInfo.globalId == closure.playerInfo.globalId) {
        leavingUsers.remove(k)
      }
    }
    val uuid = UUID.randomUUID().toString
    leavingUsers += (uuid -> closure)
    uuid
  }

  def returningUser(uuid: String): Option[ReturningUserInfo] = {
    leavingUsers.remove(uuid)
  }

  // Get another index, different from the first
  val r = new scala.util.Random

  def nextDiffIndex(first: Int, length: Int): Int = {
    val x = r.nextInt(length)
    if (x != first) x else nextDiffIndex(first, length)
  }

  def otherRole(role: String): String = role match {
    case "RoleA" => "RoleB"
    case "RoleB" => "RoleA"
  }

  //  def getATypedObj[T](item: Any)(implicit m: Manifest[T]): Option[T] = {
  //    Some(m.erasure.getField("MODULE$").get(m.erasure).asInstanceOf[T])
  //  }

  /**
    * Support for the Lobby Actor matching players
    */
  /**
    * Returns paired and unmatched players.
    */
  def matchAllPlayers(players: List[WaitingPlayer],
                      matchingStrategy: List[WaitingPlayer] => PossibleMatchedPair): PairsAndUnmatched = {
    // pick a random player. Can we match him or her? If so, match them.
    // If not, try the next random person and the rest of the list.
    // If not and no more players, we are finished.
    // If no more players, we are finished.
    var playersToBeMatched = players
    var allMatchedPairs = List.empty[PossibleMatchedPair]
    var unMatchedPlayers = List.empty[WaitingPlayer]
    while (playersToBeMatched.length >= 2) {
      val matchedPair = matchingStrategy(playersToBeMatched)
      if (matchedPair.isDefined) {
        allMatchedPairs = matchedPair :: allMatchedPairs
        playersToBeMatched = playersToBeMatched.filterNot(matchedPair.hasPlayer(_))
      } else {
        playersToBeMatched = playersToBeMatched.filterNot(matchedPair.hasPlayer(_))
        unMatchedPlayers = matchedPair.asList ++ unMatchedPlayers
      }
    }
    // And then, put the remaining in unmatched.
    unMatchedPlayers = playersToBeMatched ++ unMatchedPlayers
    PairsAndUnmatched(allMatchedPairs.flatMap(_.toMatchedPair), unMatchedPlayers)
  }

  def simpleRandomMatchingStrategy(playersToBeMatched: List[WaitingPlayer]): PossibleMatchedPair = {
    val plr1 :: rest = Random.shuffle(playersToBeMatched)
    val plr2 = rest.find(p2 => p2.manipulation == plr1.manipulation && p2.role != plr1.role)
    PossibleMatchedPair(Some(plr1), plr2)
  }

  def matchBetweenLocationsStrategy(playersToBeMatched: List[WaitingPlayer]): PossibleMatchedPair = {
    val plr1 :: rest = Random.shuffle(playersToBeMatched)
    val plr2 = rest.find(p2 => p2.manipulation == plr1.manipulation
      && p2.role != plr1.role
      && p2.location != plr1.location)
    PossibleMatchedPair(Some(plr1), plr2)
  }

  /**
    * If you want the player passed in to be assigned to Manip 1, Role A, send in lastAssignedManipRole = (2, "RoleB")
    */
  def assignManipRoleInSeqForTwoManipulations(newPlr: WaitingPlayer,
                                              lastAssignedManipRole: (Int, String)): WaitingPlayer
  = {
    log.debug("assigning new player manip and role: {}", newPlr)
    // be systematic. first Manip1, A. then B. Then Manip 2, A, then B, then repeat.
    val transNewPlr = lastAssignedManipRole match {
      case (2, "RoleB") => newPlr.copy(manipulation = 1, role = "RoleA")
      case (1, "RoleA") => newPlr.copy(manipulation = 1, role = "RoleB")
      case (1, "RoleB") => newPlr.copy(manipulation = 2, role = "RoleA")
      case (2, "RoleA") => newPlr.copy(manipulation = 2, role = "RoleB")
    }
    log.debug("they were assigned the next available manip/role. they are now: {}", transNewPlr)
    transNewPlr
  }

  def roleForOneManipulation(listOfRoles: List[String]): String = {
    // be systematic. first Manip1, A. then B. Then Manip 2, A, then B, then repeat.
    // change to: don't need to know what was assigned last, just balance what we have.
    //            if balanced, make a new A.
    pickRoleToBalanceList(listOfRoles)
  }

  def roleForOneManipMultipleLocations(plrLoc: String, allLocs: Seq[String],
                                       mapOfRolesListByLocation: Map[String, List[String]]): String = {
    // Goal: same number of A's and B's in each location
    //  If there is an uneven number, at least balance what we have.
    // ASSUMING ONLY TWO LOCATIONS for now. That makes it easier, but less general.
    val loc1 = allLocs(0)
    val loc2 = allLocs(1)

    // first, balance what we have.
    val (unbalancedL1, unbalancedL2) = removeBalancedRoles(mapOfRolesListByLocation(loc1),
      mapOfRolesListByLocation(loc2))
    // but we can only balance if the player is in the other location.
    // sort, so we match a's with b's, first.
    plrLoc match {
      case x if (x == loc1 && unbalancedL2.length > 0) => otherRole(unbalancedL2.sorted.head)
      case x if (x == loc1 && unbalancedL2.length == 0) => pickRoleToBalanceList(mapOfRolesListByLocation(loc1))
      case x if (x == loc2 && unbalancedL1.length > 0) => otherRole(unbalancedL1.sorted.head)
      case x if (x == loc2 && unbalancedL1.length == 0) => pickRoleToBalanceList(mapOfRolesListByLocation(loc2))
    }
  }

  def pickRoleToBalanceList(listOfRoles: List[String]): String = {
    // if we have more B's than A, or the same amount, pick an A
    if (listOfRoles.count(_ == "RoleA") <= listOfRoles.count(_ == "RoleB")) "RoleA" else "RoleB"
  }

  @tailrec
  def removeBalancedRoles(l1: List[String], l2: List[String]): (List[String], List[String]) = {
    if (l1.length == 0 || l2.length == 0) {
      // first: l2 must have the remaining role(s), second: l1 must have the remaining role
      (l1, l2)
    } else {
      // must both have roles remaining. Are they different?
      if (l1.contains("RoleA") && l2.contains("RoleB")) {
        val newL1 = l1.splitAt(l1.indexOf("RoleA")) match {
          case (l, r) => l ++ r.tail
        }
        val newL2 = l2.splitAt(l2.indexOf("RoleB")) match {
          case (l, r) => l ++ r.tail
        }
        removeBalancedRoles(newL1, newL2)
      } else if (l1.contains("RoleB") && l2.contains("RoleA")) {
        val newL1 = l1.splitAt(l1.indexOf("RoleB")) match {
          case (l, r) => l ++ r.tail
        }
        val newL2 = l2.splitAt(l2.indexOf("RoleA")) match {
          case (l, r) => l ++ r.tail
        }
        removeBalancedRoles(newL1, newL2)
      } else {
        log.error("RemovingBalancedRoles: they have roles remaining, but they are the same role... (shouldn't happen in a real game). List1: {}, List2: {}", l1, l2)
        (l1, l2)
      }
    }
  }
}

