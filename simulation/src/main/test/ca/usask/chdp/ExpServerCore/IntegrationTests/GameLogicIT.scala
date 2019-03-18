package ca.usask.chdp.ExpServerCore.IntegrationTests

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic._
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.settings
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Gen
import scala.Some
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import ca.usask.chdp.Enums._
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}

class GameLogicIT(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with FlatSpec with PropertyChecks with ShouldMatchers
with BeforeAndAfterAll {

  val customConfig = ConfigFactory.parseString(
    """
      testCustom {
        akka.loglevel = WARNING
      }
    """).getConfig("testCustom")
  val defaultCfg = ConfigFactory.load()
  val testCfg = customConfig.withFallback(defaultCfg.getConfig("testActorSystem").withFallback(defaultCfg))
  override implicit val system = Lobby.actorSystemInitialize(Some(testCfg))

  "Player_A" should "take A exactly A_min to reach their goals" in {
    val s = setup2Players
    workAll("RoleA", "Part1", s)
    workAll("RoleA", "Part2", s)
    val (aState, bState) = workAll("RoleA", "Part3", s)
    assert(aState.uiCmd === A_FinishedGoals)
    // No longer sending this message.
    //    assert(bState.watchStatusBar === "Your partner has finished their personal goals. They will now choose between working " +
    //      "on the team project (the F1 car) or their own personal project for their managers.")
    assert(s.daysWorked("RoleA") === Lobby.settings.A_persGoal(1, 1))
    s.close()
  }
  it should "do as much work on Part1 after reaching goals as possible" in {
    val s = setup2Players
    workAll("RoleA", "Part1", s)
    workAll("RoleA", "Part2", s)
    val (aState, bState) = workAll("RoleA", "Part3", s)
    assert(aState.uiCmd === A_FinishedGoals)
    // No longer sending this message.
    //assert(bState.watchStatusBar === "Your partner has finished their personal goals. They will now choose between working " +
    //  "on the team project (the F1 car) or their own personal project for their managers.")
    assert(s.daysWorked("RoleA") === Lobby.settings.A_persGoal(1, 1))

    s.plr("RoleA") ! FinishedReadingInfo

    assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === A_WorkingOnProj1And2)
    val (aState2, bState2) = workAll("RoleA", "Part1", s)
    assert(aState2.part1Work === false)

    // if not finished yet, finish with Part2.
    if (s.daysWorked("RoleA") < Lobby.settings.A_max) {
      assert(aState2.part2Work == true)
      assert(aState2.part3Work == true)
      workAll("RoleA", "Part2", s)
    }

    s.plr("RoleA") ! FinishedReadingInfo
    val aCmds2 = s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd
    assert(aCmds2 === A_WaitingForB)

    s.probe("RoleB").expectMsgClass(classOf[SetState])
    s.plr("RoleB") ! FinishedReadingInfo
    val bState3 = s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
    assert(bState3.uiCmd === B_WorkingOnProj)
    assert(s.daysWorked("RoleA") === Lobby.settings.A_max)
    s.close()
  }
  it should "work as much on Part2 after reaching goals as possible" in {
    val s = setup2Players
    completePersGoalsForA(s)
    s.plr("RoleA") ! FinishedReadingInfo
    val aState = s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean
    assert(aState.uiCmd === A_WorkingOnProj1And2)
    val (aState2, bState) = workAll("RoleA", "Part2", s)
    assert(aState2.part2Work === false)

    // if not finished yet, finish with Part2.
    if (s.daysWorked("RoleA") < Lobby.settings.A_max) {
      assert(aState2.part1Work == true)
      assert(aState2.part3Work == true)
      workAll("RoleA", "Part1", s)
    }

    s.plr("RoleA") ! FinishedReadingInfo
    val aCmds2 = s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd
    assert(aCmds2 === A_WaitingForB)

    val bState2 = s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
    assert(bState2.uiCmd === B_ASentProj)
    s.plr("RoleB") ! FinishedReadingInfo
    val bState3 = s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
    assert(bState3.uiCmd === B_WorkingOnProj)
    assert(s.daysWorked("RoleA") === Lobby.settings.A_max)
    s.close()
  }
  it should "work as much on Part3 after reaching goals as possible" in {
    val s = setup2Players
    completePersGoalsForA(s)
    s.plr("RoleA") ! FinishedReadingInfo
    val aState = s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean
    assert(aState.uiCmd === A_WorkingOnProj1And2)
    val (aState2, bState) = workAll("RoleA", "Part3", s)
    assert(aState2.part3Work === false)

    // if not finished yet, finish with Part2.
    if (s.daysWorked("RoleA") < Lobby.settings.A_max) {
      assert(aState2.part1Work == true)
      assert(aState2.part2Work == true)
      workAll("RoleA", "Part1", s)
    }

    s.plr("RoleA") ! FinishedReadingInfo
    val aCmds2 = s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd
    assert(aCmds2 === A_WaitingForB)
    val bState2 = s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
    assert(bState2.uiCmd === B_ASentProj)
    s.plr("RoleB") ! FinishedReadingInfo
    val bState3 = s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
    assert(bState3.uiCmd === B_WorkingOnProj)
    assert(s.daysWorked("RoleA") === Lobby.settings.A_max)
    s.close()
  }
  it should "receive refresh UI if sent doWork 10 times after reaching persGoal Part1" in {
    val s = setup2Players
    workAll("RoleA", "Part1", s)
    // now send mistake WorkOn
    for (i <- 1 to 10) {
      s.plr("RoleA") ! WorkOn("Part1")
      val aMsg = s.probe("RoleA").expectMsgClass(classOf[RefreshUI]).bean
      assert(aMsg.bean.part1Work === false)
    }
    s.close()
  }
  it should "receive refresh UI if sent doWork 10 times after reaching persGoal Part2" in {
    val s = setup2Players
    workAll("RoleA", "Part2", s)
    // now send mistake WorkOn
    for (i <- 1 to 10) {
      s.plr("RoleA") ! WorkOn("Part2")
      val aMsg = s.probe("RoleA").expectMsgClass(classOf[RefreshUI]).bean
      assert(aMsg.bean.part2Work === false)
    }
    s.close()
  }
  it should "receive refresh UI if sent doWork 10 times after reaching persGoal Part3" in {
    val s = setup2Players
    workAll("RoleA", "Part3", s)
    // now send mistake WorkOn
    for (i <- 1 to 10) {
      s.plr("RoleA") ! WorkOn("Part3")
      val aMsg = s.probe("RoleA").expectMsgClass(classOf[RefreshUI]).bean
      assert(aMsg.bean.part3Work === false)
    }
    s.close()
  }
  "Player_B" should "reach her goals with exactly B_surplus days left over" in {
    implicit val gameManipRnd = CurGameManipRnd(settings.getManipIndexForTesting, 0)

    val s = setup2Players

    completeAllWorkForAStartingWithPart("Part1", s)
    for (part <- PartNumValues) {
      if (canWorkOn("RoleB", part, s)) {
        val (bStateTmp, _) = workAll("RoleB", part, s)
        assert(goalReached(part, bStateTmp) === true)
        assert(partEnabled(part, bStateTmp) === false)
      }
    }
    val bState = getState("RoleB", s)
    assert(bState.part1Work === false)
    assert(bState.part2Work === false)
    assert(bState.part3Work === false)
    assert(bState.goal1Reached === true)
    assert(bState.goal2Reached === true)
    assert(bState.goal3Reached === true)
    s.plr("RoleB") ! FinishedReadingInfo
    assert(s.daysWorked("RoleB") === (settings.B_max - settings.eqnVal(GameEqn.A_extraHelp)))
    s.close()
  }
  it should "not be able reach her goals if A does not help" in {
    implicit val gameManipRnd = CurGameManipRnd(settings.getManipIndexForTesting, 0)
    val s = setup2Players

    val aMinHelp = settings.eqnVal(GameEqn.A_minHelp)
    val aShortfall = 1
    runOneRoundWithANotHelpingBy(aShortfall, s)
    val gameState = getGameRecord(s)
    assert(gameState.helpfulBehsPerRound(Round(0)) === aMinHelp - aShortfall)
    s.close()
  }
  it should "not be able to reach goals given a range of A's not helping enough" in {
    implicit val gameManipRnd = CurGameManipRnd(settings.getManipIndexForTesting, 0)

    val aMinHelp = settings.eqnVal(GameEqn.A_minHelp)
    forAll(Gen.choose(1, aMinHelp)) {
      shortfall =>
        val s = setup2Players
        runOneRoundWithANotHelpingBy(shortfall, s)
        val gameState = getGameRecord(s)
        assert(gameState.helpfulBehsPerRound(Round(0)) === aMinHelp - shortfall)
        s.close()
    }
  }
  "CompleteGame" should "do the following" in { (pending) }
  it should "record A's helpful behavior accurately" in {
    implicit var gameManipRnd = CurGameManipRnd(settings.getManipIndexForTesting, 0)
    val aDiscr = settings.eqnVal(GameEqn.A_discretionary)
    forAll(Gen.choose(0, aDiscr)) {
      helpfulBehs =>
      //    whenever(helpfulBehs >= 0 && helpfulBehs <= aDiscr) {
        val s = setup2Players
        runOneRoundWithAHelping(helpfulBehs, s)
        val gameState = getGameRecord(s)
        assert(gameState.helpfulBehsPerRound(Round(0)) === helpfulBehs)
        s.close()
      //  }
    }
  }
  it should "run all rounds and received a finished game from each player" in {
    implicit var gameManipRnd: CurGameManipRnd = null
    val s = setup2Players

    for (i <- 0 until Lobby.settings.numRounds) {
      val rnd = i
      gameManipRnd = CurGameManipRnd(settings.getManipIndexForTesting, rnd)
      runOneRoundWithAHelping(2, s)
      val gameRec = getGameRecord(s)
      assert(gameRec.helpfulBehsPerRound(Round(rnd)) === 2)
      if (rnd != Lobby.settings.numRounds - 1) {
        // not the last round, so the View gets setStates for the next round. Clear those:
        assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === A_WorkingOnProj1)
        assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd === B_WatchingA)
      }
    }
    assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === ShowFinishedGame)
    assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd === ShowFinishedGame)
    s.close()
  }
}
