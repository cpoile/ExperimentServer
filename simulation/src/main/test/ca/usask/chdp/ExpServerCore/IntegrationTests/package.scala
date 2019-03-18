package ca.usask.chdp.ExpServerCore

import ExpActors.Lobby
import ExpActors.Lobby._
import ExpActors.PlayerLogic._
import ExpActors.ViewActor._
import Models._
import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import util.Random
import org.scalatest.matchers.ShouldMatchers._
import org.scalacheck.Gen
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import java.util.concurrent.ConcurrentHashMap
import collection.JavaConverters._
import akka.event.Logging
import ca.usask.chdp.Enums._
import ca.usask.chdp.ExpServerCore.Game.GetGameRecord
import View._

package object IntegrationTests {
  val log = Logging(Lobby.system, "ITPackageObject")

  val concStates: collection.mutable.Map[String, Boolean] = new ConcurrentHashMap[String, Boolean].asScala

  def addSysState(sysStateId: String) {
    concStates += (sysStateId -> true)
    log.warning("Concurrent System states: {}", concStates.size)
  }
  def remSysState(sysStateId: String) {
    concStates.remove(sysStateId)
  }

  def setup2Players(implicit _system: ActorSystem): GameState = {
    val s = new GameState(TestViewActorProbe(Model.counter("TestProbe"), _system),
      TestViewActorProbe(Model.counter("TestProbe"), _system))

    Lobby.registerViewActor(s.id1, s.vaProbe1.ref)
    Lobby.registerViewActor(s.id2, s.vaProbe2.ref)

    // instead of letting the Lobby randomly assign them to a role and Manipulation, specify it here
    val manip = Lobby.settings.getManipIndexForTesting
    s.wp1 = WaitingPlayer(s.id1, s.em1, manip, "RoleA")
    s.wp2 = WaitingPlayer(s.id2, s.em2, manip, "RoleB")
    s.probe = s.probe + ("RoleA" -> s.vaProbe1, "RoleB" -> s.vaProbe2)

    Lobby.lobby ! Lobby.MatchTheseTwoPlayers(s.wp1, s.wp2)

    s.vaProbe1.expectMsgClass(classOf[WeHaveAPlayerLogic]) match {
      case WeHaveAPlayerLogic(plr) => {
        s.plr = s.plr + ("RoleA" -> plr)
        s.idToPlr = s.idToPlr + (s.id1 -> plr)
      }
    }
    s.vaProbe2.expectMsgClass(classOf[WeHaveAPlayerLogic]) match {
      case WeHaveAPlayerLogic(plr) => {
        s.plr = s.plr + ("RoleB" -> plr)
        s.idToPlr = s.idToPlr + (s.id2 -> plr)
      }
    }
    assert(s.probe.contains("RoleA") && s.probe.contains("RoleB"),
      "s contained: " + s)
    s.probe("RoleA").expectMsgClass(classOf[SetState])
    s.probe("RoleB").expectMsgClass(classOf[SetState])
    s
  }
  def nextPartCircular(partNum: String) = {
    PartNum((PartNumID(partNum) + 1) % 3)
  }
  def partEnabled(partNum: String, state: UIState): Boolean = partNum match {
    case "Part1" => state.part1Work
    case "Part2" => state.part2Work
    case "Part3" => state.part3Work
  }
  def goalReached(partNum: String, state: UIState): Boolean = partNum match {
    case "Part1" => state.goal1Reached
    case "Part2" => state.goal2Reached
    case "Part3" => state.goal3Reached
  }
  /**
   * Checks if there are any work days left for this part. If there are it returns the first set
   * of UIStates. Sorry for returning nulls, it's too much pain to refactor to use option.
   */
  def getState(role: String, s: GameState): UIState = {
    implicit val timeout = Timeout(60 seconds)
    val future = s.plr(role) ? GetState
    Await.result(future, timeout.duration).asInstanceOf[UIStateBeanItem].bean
  }
  def getGameRecord(s: GameState): GameRecord = {
    implicit val timeout = Timeout(60 seconds)
    val future = s.plr("RoleA") ? GetGameActor
    val game = Await.result(future, timeout.duration).asInstanceOf[ActorRef]
    val future2 = game ? GetGameRecord
    Await.result(future2, timeout.duration).asInstanceOf[GameRecord]
  }
  def canWorkOn(role: String, partNum: String, s: GameState): Boolean = {
    partEnabled(partNum, getState(role, s)) == true
  }
  def workAll(role: String, partNum: String, s: GameState): (UIState, UIState) = {
    s.plr(role) ! WorkOn(partNum)
    s.daysWorked = s.daysWorked + (role -> (s.daysWorked(role) + 1))
    //    if (role == "RoleB"){
    //      s.log.error("B working on: {} in round: {} with daysWorked: {}", Array(partNum, s.curRnd, s.daysWorked).asInstanceOf[Array[AnyRef]])
    //    }
    val aRes = s.probe(role).expectMsgClass(classOf[SetState]).state.bean

    val canWork: Boolean = canWorkOn(role, partNum, s)
    // A is no longer receiving updates while B is doing work.
    val otherRes = if (role == "RoleA") {
      s.probe(s.otherRole(role)).expectMsgClass(classOf[SetState]).state.bean
    } else {
      new UIState()
    }
    //s.log.debug("Trace1: -- workAll -- role: {}, part: {}, s.daysWorked: {}", Array(role, partNum, s.daysWorked(role)).asInstanceOf[Array[AnyRef]])
    if (canWork)
      workAll(role, partNum, s)
    else
      (aRes, otherRes)
  }
  def completePersGoalsForA(s: GameState) {
    // clear days worked for both.
    s.daysWorked = Map("RoleA" -> 0, "RoleB" -> 0)
    for (part <- PartNumValues)
      workAll("RoleA", part, s)
    val aState = getState("RoleA", s)
    assert(aState.goal1Reached === true)
    assert(aState.goal2Reached === true)
    assert(aState.goal3Reached === true)
    assert(aState.uiCmd === A_FinishedGoals)
  }
  def completeAllWorkForAStartingWithPart(part: String, s: GameState) {
    completePersGoalsForA(s)
    s.plr("RoleA") ! FinishedReadingInfo
    s.probe("RoleA").expectMsgClass(classOf[SetState])
    val (aState, bState) = workAll("RoleA", part, s)
    assert(partEnabled(part, aState) === false)

    // if not finished yet, finish with Part2.
    if (s.daysWorked("RoleA") < Lobby.settings.A_max) workAll("RoleA", nextPartCircular(part), s)

    s.plr("RoleA") ! FinishedReadingInfo
    s.probe("RoleA").expectMsgClass(classOf[SetState])
    s.probe("RoleB").expectMsgClass(classOf[SetState])
    s.plr("RoleB") ! FinishedReadingInfo
    val bState3 = s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
    assert(bState3.uiCmd === B_WorkingOnProj)
    assert(s.daysWorked("RoleA") === Lobby.settings.A_max)
  }

  /**
   * Ends with B ready to work and A watching B.
   */
  def A_complAllWorkRandomlyAfterPersGoals(numDaysTeamProj: Int, numDaysPersProj: Int,
                                           s: GameState,
                                           seqOfMessages: Option[Seq[TestChatMsg]] = None)
                                          (implicit gameManipRnd: CurGameManipRnd) {
    assert(numDaysTeamProj + numDaysPersProj === settings.eqnVal(GameEqn.A_discretionary))
    completePersGoalsForA(s)
    s.plr("RoleA") ! FinishedReadingInfo
    assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[A_WorkingOnProj1And2])

    for (i <- 1 to numDaysTeamProj) {
      // check if we can work on this part, if not choose another.
      var part = PartNum(Random.nextInt(3))
      if (!canWorkOn("RoleA", part, s)) {
        part = nextPartCircular(part)
      }
      if (!canWorkOn("RoleA", part, s)) {
        part = nextPartCircular(part)
      }
      s.plr("RoleA") ! WorkOn(part)
      s.daysWorked = s.daysWorked + ("RoleA" -> (s.daysWorked("RoleA") + 1))

      val aUICmd = s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd
      if (s.daysWorked("RoleA") == settings.eqnVal(GameEqn.A_max)) {
        assert(aUICmd.isInstanceOf[A_FinishedWorkDays], aUICmd + " was not instance of " + A_FinishedWorkDays +
          " when numDaysTeamProj: " + numDaysTeamProj + " and numDaysPersProj: " + numDaysPersProj +
          " and s.daysWorked('RoleA'): " + s.daysWorked("RoleA"))
      } else {
        assert(aUICmd.isInstanceOf[A_WorkingOnProj1And2], aUICmd + " did not equal " + A_WorkingOnProj1And2 +
          " when numDaysTeamProj: " + numDaysTeamProj + " and numDaysPersProj: " + numDaysPersProj +
          " and s.daysWorked('RoleA'): " + s.daysWorked("RoleA"))

      }
      s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
      //      s.log.error("Sent message from normal, sendPnDyasWorked: {}, sent by role: {}, sentByID: {},  who has WorkedDays: {}, " +
      //        "message: {}", Array(y.sendOnDaysWorked, y.roleSender, y.sentByID, s.daysWorked("RoleA"), y.msg).asInstanceOf[Array[AnyRef]])
    }
    // try to simulate random chatting, this time by A.
    seqOfMessages foreach (_ filter (_.roleSender == "RoleA") foreach {
      m =>
        s.plr("RoleA") ! IEnteredChatMsg(m.msg)
        s.probe("RoleA").expectMsgClass(classOf[ChatMessageLst])
        s.probe("RoleB").expectMsgClass(classOf[ChatMessageLst])
    })

    if (numDaysPersProj > 0) {
      s.plr("RoleA") ! WorkOnProj2(numDaysPersProj)
      s.daysWorked = s.daysWorked + ("RoleA" -> (s.daysWorked("RoleA") + numDaysPersProj))
      s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean
      assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[A_FinishedWorkDays])
    }
    s.plr("RoleA") ! FinishedReadingInfo
    assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[A_WaitingForB])
    s.probe("RoleB").expectMsgClass(classOf[SetState])
    s.plr("RoleB") ! FinishedReadingInfo
    assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[B_WorkingOnProj])
  }
  def assertMetGoalsAndFinishedRound(state: UIState) {
    assert(state.daysLeft === 0)
    (state.goal1Reached, state.goal2Reached, state.goal3Reached) should be(true, true, true)
    (state.part1Work, state.part2Work, state.part3Work) should be(false, false, false)
  }
  def runOneRoundWithANotHelpingBy(aShortfall: Int,  s: GameState)
                                  (implicit gameManipRnd: CurGameManipRnd) {
    val aPersGoal = settings.eqnVal(GameEqn.A_persGoal)
    val aMax = settings.eqnVal(GameEqn.A_max)
    val aMinHelp = settings.eqnVal(GameEqn.A_minHelp)
    val aDiscretionary = settings.eqnVal(GameEqn.A_discretionary)
    val aExtraHelp = settings.eqnVal(GameEqn.A_extraHelp)
    assert(aShortfall <= aMinHelp)

    assert(aPersGoal + aMinHelp + aExtraHelp === aMax)
    assert(aMinHelp - aShortfall === aDiscretionary - (aExtraHelp + aShortfall))
    assert((aMinHelp - aShortfall) + (aExtraHelp + aShortfall) + aPersGoal === aMax)

    A_complAllWorkRandomlyAfterPersGoals(aMinHelp - aShortfall, aExtraHelp + aShortfall, s)
    val aState = getState("RoleA", s)
    assert(aState.uiCmd === A_WaitingForB)
    assertMetGoalsAndFinishedRound(aState)
    assert(aState.proj2DaysWorked(0) === aExtraHelp + aShortfall)


    for (part <- PartNumValues if canWorkOn("RoleB", part, s)) {
      workAll("RoleB", part, s)
    }
    val bState = getState("RoleB", s)
    assert(bState.part1Work === false)
    assert(bState.part2Work === false)
    assert(bState.part3Work === false)
    assert(bState.uiCmd.isInstanceOf[B_RanOutOfWorkDays])
    assert(bState.daysLeft === 0)
    // finish up.
    readingFinishedRoundInfo_toStartOfNextRound(s)
  }
  /**
   * ends on RaceFinished. Must check for SetState if this is not the last round.
   *
   */
  def runOneRoundWithAHelping(aHelpfulBehs: Int,
                              s: GameState,
                              seqOfMessages: Option[Seq[TestChatMsg]] = None)
                             (implicit gameManipRnd: CurGameManipRnd) {
    assert(aHelpfulBehs >= 0 && aHelpfulBehs <= settings.eqnVal(GameEqn.A_discretionary))
    val aPersGoal = settings.eqnVal(GameEqn.A_persGoal)
    val aMax = settings.eqnVal(GameEqn.A_max)
    val aMinHelpNeeded = settings.eqnVal(GameEqn.A_minHelp)
    val aMaxExtraHelp = settings.eqnVal(GameEqn.A_extraHelp)
    val aExtraBehsPerformed = math.max(aHelpfulBehs - aMinHelpNeeded, 0)
    val aDiscretionary = settings.eqnVal(GameEqn.A_discretionary)
    val aShortfall = math.max(aMinHelpNeeded - aHelpfulBehs, 0)
    val aWorkOnPersProj = aDiscretionary - aHelpfulBehs

    assert(aExtraBehsPerformed <= aDiscretionary - aMinHelpNeeded)
    assert(aShortfall <= aMinHelpNeeded)
    assert(aHelpfulBehs + aShortfall + (aMaxExtraHelp - aExtraBehsPerformed) === aDiscretionary)
    assert(aMinHelpNeeded + aMaxExtraHelp + aPersGoal === aMax)
    assert(aPersGoal + aHelpfulBehs + aWorkOnPersProj === aMax)

    A_complAllWorkRandomlyAfterPersGoals(aHelpfulBehs, aWorkOnPersProj, s, seqOfMessages)
    val aState = getState("RoleA", s)
    assert(aState.uiCmd.isInstanceOf[A_WaitingForB])
    assertMetGoalsAndFinishedRound(aState)
    assert(aState.proj2DaysWorked(gameManipRnd.round) === aWorkOnPersProj)

    // Now have B send messages to A.
    seqOfMessages foreach (_ filter (_.roleSender == "RoleB") foreach {
      m =>
        s.plr("RoleB") ! IEnteredChatMsg(m.msg)
        s.probe("RoleA").expectMsgClass(classOf[ChatMessageLst])
        s.probe("RoleB").expectMsgClass(classOf[ChatMessageLst])
    })

    for (part <- PartNumValues if canWorkOn("RoleB", part, s)) {
      workAll("RoleB", part, s)
    }
    val bState = getState("RoleB", s)
    assert(bState.part1Work === false)
    assert(bState.part2Work === false)
    assert(bState.part3Work === false)
    val finalCmd = if (aShortfall == 0) B_FinishedTeamProj else B_RanOutOfWorkDays
    bState.uiCmd match {
      case cmd: B_FinishedTeamProj if(aShortfall == 0) =>
      case cmd: B_RanOutOfWorkDays if(aShortfall != 0) =>
      case x => fail(x + " was not as expected.")
    }
    assert(bState.daysLeft === aExtraBehsPerformed)
    // finish up.
    readingFinishedRoundInfo_toStartOfNextRound(s)
  }
  def readingFinishedRoundInfo_toStartOfNextRound(s: GameState)(implicit gameManipRnd: CurGameManipRnd) {
    assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === A_BFinishedRound)
    s.plr("RoleA") ! FinishedReadingInfo
    s.plr("RoleB") ! FinishedReadingInfo
    s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd match {
      case cmd: WatchingRace =>
      case x => fail(x + " was not a WatchingRace object.")
    }
    s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd match {
      case cmd: WatchingRace =>
      case x => fail(x + " was not a WatchingRace object.")
    }
    s.plr("RoleA") ! RaceFinished
    s.plr("RoleB") ! RaceFinished
    s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd match {
      case cmd: RaceResults =>
      case x => fail(x + " was not a RaceResults object.")
    }
    s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd match {
      case cmd: RaceResults =>
      case x => fail(x + " was not a RaceResults object.")
    }
    s.plr("RoleA") ! FinishedReadingInfo
    s.plr("RoleB") ! FinishedReadingInfo
    if (Lobby.settings.surveyAfterRounds.contains(gameManipRnd.round + 1)) {
      s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd match {
        case cmd: TakingSurvey =>
        case x => fail(x + " was not a TakingSurvey object. It was: " + x)
      }
      s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd match {
        case cmd: TakingSurvey =>
        case x => fail(x + " was not a TakingSurvey object. It was: " + x)
      }
      s.plr("RoleA") ! FinishedSurvey
      s.plr("RoleB") ! FinishedSurvey
      assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === WaitingForPartnerToFinishSurvey)
      assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd === WaitingForPartnerToFinishSurvey)
    } else {
      assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === WaitingForPartner)
      assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd === WaitingForPartner)
    }
  }

  def rndDisc(r: Int) = settings.eqnVal(GameEqn.A_discretionary)(CurGameManipRnd(settings.getManipIndexForTesting, r))
  def validAHelp(r: Int) = (Gen.choose(0, rndDisc(r)))

  /*val validArray = for {
  a <- validAHelp(0)
  b <- validAHelp(1)
  c <- validAHelp(2)
  d <- validAHelp(3)
  e <- validAHelp(4)
  f <- validAHelp(5)
  g <- validAHelp(6)
  h <- validAHelp(7)
  i <- validAHelp(8)
  j <- validAHelp(9)
  } yield Seq(a, b, c, d, e, f, g, h, i, j)*/

  def validArray = for (i <- 0 until Lobby.settings.numRounds) yield (validAHelp(i).sample.get)

  def genHistogram(map: Seq[(Int, Int)]): String = {
    map.map(x => x._1 + ": " + "*" * x._2).mkString("\n")
  }

  /**
   * Make a histogram of a collection of each game's part1, part2, part3 in each round (30 data points per game for a 10 round game)
   * @param data  in form: Seq(game1(part1 in round1,numUpgrs),(part2 in round1,numUpgrs)..., game2(...))
   * @return     nicely formatted histogram
   */
  def groupIntFreqDist(data: Seq[Seq[(String, Int)]]): String = {
    // gives us Seq( game1(part1 -> (numupgrsRnd1, numUpgrsRnd2, ...), part2 -> (...)...), game2(...))
    // i.e.: part 1 ends like this at the end of each round, per game
    val res2 = data.map(_.groupBy(_._1).mapValues(_.map(_._2)))

    // gives us Seq((part1Game1, Seq(numUpgsRnd1, numUpgsRnd2, ...), (part1Game2, Seq(..)), ...)
    val res3 = res2.flatMap(_.toSeq.sortBy(_._1))

    // gives us Map(part1 -> Seq(numUpgradesRnd1, numUpgradesRnd2, ... for all games))
    val res4 = res3.groupBy(_._1).mapValues(_.flatMap(_._2))

    genHistByPart(res4)
  }
  /**
   * Make a histogram given each part, and data points for that part.
   * @param data in form: Map[Part -> Seq(data1, data2, ...)]
   * @return
   */
  def genHistByPart(data: Map[String, Seq[Int]]): String = {
    // gives us Seq((Part1,Seq(...)), (Part2, Seq(...)), ...)
    val res5 = data.toSeq.sortBy(_._1)
    val res6 = res5.map(x => (x._1, x._2.groupBy(identity).mapValues(_.size).toSeq.sortBy(_._1)))
    val res7 = for (part <- res6) yield {
      val res = for (freq <- part._2) yield {
        //        freq._1 + ": " + "*" * freq._2
        freq._1 + ": " + freq._2
      }
      part._1 + " => \n" + res.mkString("\n")
    }
    res7.mkString("\n\n")
  }
  def genArrayChat(round: Int, senderID: String, sentToID: String, roleSender: String,
                   roleSentTo: String): Seq[TestChatMsg] = {
    for (i <- 1 to Gen.choose(1, 5).sample.get) yield {
      // need to decide when A and be will chat, so randomly place the messages
      // in A's discretionary work days or B's work days.
      //      val minWorkDay = settings.eqnVal(GameEqn.A_persGoal)(CurGameManipRnd(1, Round.Rnd1)) + 1
      //      val maxWorkDay = settings.A_max + settings.B_max - 2

      TestChatMsg(round, senderID, sentToID, roleSender, roleSentTo, Gen.alphaStr.sample.get)
    }
  }
}

