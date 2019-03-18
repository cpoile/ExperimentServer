package ca.usask.chdp.ExpServerCore.ExpActors

import akka.actor.ActorSystem
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import scala.concurrent.duration._
import akka.pattern.ask
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import Lobby._
import ca.usask.chdp.ExpServerCore.Models.Model
import akka.util.Timeout
import ca.usask.chdp.models.{ParticipantBean, ParticipantDAO}
import util.Random
import ca.usask.chdp.models.Msgs._
import concurrent.{Future, Await}

class LobbyTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with FlatSpec with PropertyChecks with ShouldMatchers with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val actorSystem = _system
  implicit val timeout = Timeout(1 seconds)

  import actorSystem.dispatcher


  def this() = this {
    val customConfig = ConfigFactory.parseString(
      """
      testCustom {
        akka.loglevel = DEBUG
        exp.testingMode = true
        exp.testing_integrationTesting = true
        exp.testing_doNotStartGames = true
      }
      """).getConfig("testCustom")
    val defaultCfg = ConfigFactory.load()
    val testCfg = customConfig.withFallback(defaultCfg.getConfig("testActorSystem").withFallback(defaultCfg))
    //    val root = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
    //    root.setLevel(Level.OFF)
    Lobby.actorSystemInitialize(Some(testCfg))
  }
  override def beforeAll() {
    Lobby.lobby ! NewSessionId
  }
  override def afterAll() {
    system.shutdown()
    system.awaitTermination()
  }
  override def beforeEach() {

  }
  override def afterEach() {
    Lobby.lobby ! LobbyStopGame
    Lobby.lobby ! ResetSession_DANGEROUS
  }

  def addOnePlayer(manip: Int, role: String): WaitingPlayer = {
    val email = Model.counter("LobbyTest_player")
    ParticipantDAO.insertNewUser(email, email, "", "", "")
    val id = ParticipantDAO.findByEmail(email).getOrElse(ParticipantBean("")).globalId
    val plr = Lobby.WaitingPlayer(id, email, manip, role)
    val probe = TestProbe()
    Lobby.registerViewActor(id, probe.ref)
    Lobby.newPlayer(plr)
    Lobby.lobby ! PlayerFinishedInstructions(plr)
    plr
  }
  def getId = Random.nextInt(99999999).toString

  "Lobby" should "not match 1 player" in {
    val plr = addOnePlayer(1, "RoleA")

    Lobby.lobby ! StartGameMatchPlayers
    val res = Await.result((Lobby.lobby ? GetWaitingPlayersInAllStages), 1 second).asInstanceOf[List[WaitingPlayer]]
    assert(res.length === 1)
    assert(res.head.globalId === plr.globalId)
  }
  it should "not match two if not started" in {
    val plrs = List(addOnePlayer(1, "RoleA"), addOnePlayer(1, "RoleB"))
    val res = Await.result((Lobby.lobby ? GetWaitingPlayersInAllStages), 1 second).asInstanceOf[List[WaitingPlayer]]
    res.length should be(2)
    assert(res.forall(wp => plrs.exists(_.globalId == wp.globalId) ))
  }
  it should "match two if started" in {
    Lobby.lobby ! StartGameMatchPlayers
    val plr1 = addOnePlayer(1, "RoleA")
    val plr2 = addOnePlayer(1, "RoleB")
    val fut = Future {
      Thread.sleep(100)
      Await.result((Lobby.lobby ? GetWaitingPlayersInAllStages), 1 second).asInstanceOf[List[WaitingPlayer]]
    }
    val res = Await.result(fut,.5 seconds).asInstanceOf[List[WaitingPlayer]]
    res.length should be(0)
  }
  "LobbyObject" should "not match two players who aren't in same manipulation" in {
    var plrs = List(WaitingPlayer(getId, getId, 2, "RoleA"), WaitingPlayer(getId, getId, 1, "RoleB"))
    val res = Lobby.matchAllPlayers(plrs, Lobby.simpleRandomMatchingStrategy)
    var unMatched = res.unMatched
    plrs = plrs.sortBy(_.globalId)
    unMatched = unMatched.sortBy(_.globalId)
    plrs should be(unMatched)
    res.matchedPairs.length should be(0)
  }
  it should "match all four players if they are different roles same manip" in {
    var plrs = List(WaitingPlayer(getId, getId, 1, "RoleA"), WaitingPlayer(getId, getId, 1, "RoleB"),
      WaitingPlayer(getId, getId, 2, "RoleA"), WaitingPlayer(getId, getId, 2, "RoleB"))
    val res = Lobby.matchAllPlayers(plrs, Lobby.simpleRandomMatchingStrategy)
    var matched = res.matchedPairs.flatMap(mp => List(mp.plr1, mp.plr2))
    plrs = plrs.sortBy(_.globalId)
    matched = matched.sortBy(_.globalId)
    plrs should be(matched)
    res.unMatched.length should be(0)
  }
  it should "assignManipRoleInSequence will produce 12 matches, and 1 that isn't matched" in {
    var lastAssignedManipRole = (2, "RoleB")

    var plrs = List(Lobby.assignManipRoleInSeqForTwoManipulations(WaitingPlayer(getId, getId), lastAssignedManipRole))
    lastAssignedManipRole = (plrs.head.manipulation, plrs.head.role)
    // yes, imperative, I know, but it's late.
    for (i <- 1 to 12) {
      plrs = Lobby.assignManipRoleInSeqForTwoManipulations(WaitingPlayer(getId, getId), lastAssignedManipRole) :: plrs
      lastAssignedManipRole = (plrs.head.manipulation, plrs.head.role)
    }
    val res = Lobby.matchAllPlayers(plrs, Lobby.simpleRandomMatchingStrategy)
    val matched = res.matchedPairs.flatMap(mp => List(mp.plr1, mp.plr2))
    var (shouldBeMatched, leftOut) = (plrs.init, plrs.takeRight(1))
    // can't compare exactly, a random one won't be matched.
    shouldBeMatched.length should be(matched.length)
    res.unMatched.length should be(1)
    res.unMatched(0).manipulation should be(1)
    res.unMatched(0).role should be("RoleA")
  }
  it should "assignManipRoleInSequenr 4 players when not using both manipulations" in {
    var plrs = List(WaitingPlayer(getId, getId, 1, "RoleA"), WaitingPlayer(getId, getId, 1, "RoleB"),
      WaitingPlayer(getId, getId, 1, "RoleA"), WaitingPlayer(getId, getId, 1, "RoleB"))
    val res = Lobby.matchAllPlayers(plrs, Lobby.simpleRandomMatchingStrategy)
    var matched = res.matchedPairs.flatMap(mp => List(mp.plr1, mp.plr2))
    plrs = plrs.sortBy(_.globalId)
    matched = matched.sortBy(_.globalId)
    plrs should be(matched)
    res.unMatched.length should be(0)
  }
  it should "assignManipRoleInSequence will produce matches and one left over when only using one manip" in {
    var plrs = List.empty[WaitingPlayer]

    for (i <- 1 to 13) {
      val role = roleForOneManipulation(plrs.map(_.role))
      plrs = WaitingPlayer(getId, getId, Lobby.settings.manipIfNotUsingBoth, role) :: plrs
    }
    val res = Lobby.matchAllPlayers(plrs, Lobby.simpleRandomMatchingStrategy)
    val matched = res.matchedPairs.flatMap(mp => List(mp.plr1, mp.plr2))
    var (shouldBeMatched, leftOut) = (plrs.init, plrs.takeRight(1))
    // can't compare exactly, a random one won't be matched.
    shouldBeMatched.length should be(matched.length)
    res.unMatched.length should be(1)
    res.unMatched(0).manipulation should be(2)
    res.unMatched(0).role should be("RoleA")
    // hardcoded, I know
    assert( res.matchedPairs.forall(mp => mp.plr1.manipulation == 2 && mp.plr2.manipulation == 2))
  }

}