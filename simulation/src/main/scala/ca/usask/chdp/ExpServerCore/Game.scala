package ca.usask.chdp.ExpServerCore

import akka.actor.ActorRef
import ExpActors.Lobby
import util.Random
import collection.mutable
import ca.usask.chdp.ExpSettings.{TrackInfo, CurGameManipRnd}
import ca.usask.chdp.Enums._
import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpSettings

object Game {

  val log = LoggerFactory.getLogger("GameObject")

  sealed trait GameMsgs

  case class RegisterAdminListener(admin: ActorRef) extends GameMsgs
  case object StartGame extends GameMsgs
  case class InitGame(p1: ActorRef, p2: ActorRef) extends GameMsgs
  case class MsgEntered(msg: String) extends GameMsgs
  case class GetNextPartStep(partnum: String) extends GameMsgs
  case object AFinishedPersGoals extends GameMsgs
  case class ASentProject(recordOfProj2Work: Map[Int, Int]) extends GameMsgs
  case object HelpfulBeh extends GameMsgs
  case class BSentProject(b_metAllGoals: Boolean) extends GameMsgs
  case object ReadyForRace extends GameMsgs
  case class RoundFinished(surveyName: Option[String] = None,
    questionReport: Option[Map[String, Int]] = None) extends GameMsgs
  case class DoIDisplayEndOfRoundSurvey(player: ActorRef) extends GameMsgs
  case object SurveyFinished extends GameMsgs

  // The following is for testing.
  case object GetGameRecord extends GameMsgs

  /**
   * Create the race history using A's helpful behaviors.
   * Returns a seq of lap 1 to the last lap of that track.
   * For each lap there is a seq of that lap's standings.
   * The team and track are zero-based index referring to the corresponding array of info.
   * We are always team 0.
   *
   */
  def createRace(track: TrackInfo, helpfulBehs: Int,
                 numTeams: Int, curManip: Int, curRound: Int): List[List[Int]] = {
    val A_MinimumHelp = Lobby.settings.eqnVal(GameEqn.A_minHelp)(CurGameManipRnd(curManip, curRound))
    val finalPos: List[Int] = getAllFinishPositions(numTeams, helpfulBehs, A_MinimumHelp, curManip, curRound)
    val history = mutable.Buffer(finalPos)
    // now walk back the cat and build the race by moving around the cars randomly up and down each lap.
    // Strictly speaking we don't need to reverse the range below, but congnitively it makes it easier to understand.
    for (lap <- (1 until track.numLaps).reverse) {
      val lastLap = history.last.toList
      // keep track of how many times a driver has moved positions. max movement should be one place per lap.
      history += moveDriversByMaxOnePos(lastLap)
    }
    history.reverse.toList
  }
  def moveDriversByMaxOnePos(curPsns: List[Int]): List[Int] = curPsns match {
    case dr1 :: Nil => dr1 :: Nil
    case Nil => Nil
    case dr1 :: dr2 :: rest => {
      // randomly move them.
      // If so, they are both finished moving this round. If not, the first driver is finished.
      if (Random.nextDouble() > .5)
        dr2 :: dr1 :: moveDriversByMaxOnePos(rest)
      else
        dr1 :: (moveDriversByMaxOnePos(dr2 :: rest))
    }
  }
  /**
   * Generates a history of the changes in positions for the race.
   * If driver in position 3 moves to position 4, and 4 moves to position 3, then that lap's changes would
   * include (3,4), (4,3)  -- meaning: pos 3 to moved to pos 4, and pos 4 moved to pos 3
   *
   * take two laps, find the changes, repeat.
   */
  def genHistoryOfPosChange(posHist: List[List[Int]]): List[List[Map[String, Int]]] = posHist match {
    case lap1 :: lap2 :: rest => {
      findChanges(lap1, lap2) :: genHistoryOfPosChange(lap2 :: rest)
    }
    case lastLap :: Nil => genHistoryOfPosChange(Nil)
    case Nil => List.empty[List[Map[String, Int]]]
  }
  /**
   * Find out if a driver changed position each lap.
   * Store it in a Map because that's what JavaScript uses, and this is intended for JS.
   */
  def findChanges(lap1: List[Int], lap2: List[Int]): List[Map[String, Int]] = {
    var ret = mutable.Buffer.empty[Map[String, Int]]
    for (i <- 0 until lap1.length) {
      if (lap1(i) != lap2(i)) {
        // the driver changed position.
        ret += Map("team" -> lap1(i), "from" -> i, "to" -> lap2.indexOf(lap1(i)))
      }
    }
    ret.toList
  }
  /**
   * Return a list of the final race position. We are placed based on A's helpful behs, everyone else is
   * placed randomly because it doesn't matter where they place.
   */
  def getAllFinishPositions(numTeams: Int, helpfulBehs: Int, a_minHelp: Int, curManip: Int, curRound: Int): List[Int] = {
    val teamsWithoutUs = (1 until numTeams).toList
    //val A_MinimumHelp = Lobby.settings.eqnVal(GameEqn.A_minHelp)(CurGameManipRnd(curManip, curRound))
    val howWellWeDid = (0, getOurAbility(helpfulBehs, a_minHelp))
    val teamsAndAbility = teamsWithoutUs.map{i => (i, ExpSettings.get.teamSeq(i).ability)}
    val teamsAndAbilityWithUs = teamsAndAbility ++ Seq(howWellWeDid)
    val finalPosWithUs = getFinalPositions(teamsAndAbilityWithUs, generateProbList3)
    //val ourFinish = getOurFinishPos(helpfulBehs, Lobby.settings.eqnVal(GameEqn.A_minHelp)(CurGameManipRnd(curManip, curRound)), numTeams)
    log.debug("2 - our finish: {} -- A_minHelp: {} -- Our actual help: {}",
      Array[AnyRef](int2Integer(finalPosWithUs.indexOf(0)), int2Integer(a_minHelp), int2Integer(helpfulBehs)))
    finalPosWithUs.toList
  }

  /**
   * Do a simulation based on the team's abilities
   */
  def pickNext(curRanking: Seq[Int], possibles: Seq[Int]): Seq[Int] = {
    if (possibles.length > 0) {
      val nextTeam = Random.shuffle(possibles).head
      val newRanking = curRanking ++ Seq(nextTeam)
      pickNext(newRanking, possibles.filterNot(_ == nextTeam))
    } else
      curRanking
  }

  def generateProbList1(teamsAndAbility: Seq[(Int, Int)]) = {
    teamsAndAbility.flatMap{case (t,a) => List.fill(a)(t)}
  }
  def generateProbList2(teamsAndAbility: Seq[(Int, Int)]) = {
    teamsAndAbility.flatMap{case (t,a) => List.fill(a * a)(t)}
  }
  def generateProbList3(teamsAndAbility: Seq[(Int, Int)]) = {
    teamsAndAbility.flatMap{case (t,a) => List.fill(a * a * a)(t)}
  }

  def getFinalPositions(teamsAndAbility: Seq[(Int, Int)],
    generateProbListFn: Seq[(Int, Int)] => Seq[Int]): Seq[Int] = {
    val rankedByAbility = teamsAndAbility.sortBy(_._2).reverse
    val probList = generateProbListFn(teamsAndAbility)
    val ranking = pickNext(Seq(), probList)
    ranking
  }

  def getOurAbility(helpfulBehs: Int, minHelpRequired: Int): Int = {
    // max of 10 if low dep condition, 2 if high dep
    // normalize it to: 4 extremely helpful, 2 moderately, 0 bare min, -2 not help, -4 selfish.
    val extraHelp = helpfulBehs - minHelpRequired
    val isHighDep = if (minHelpRequired >= 10) true else false
    val normalizedHelp = if (isHighDep) extraHelp match {
      case 2 => 2
      case 1 => 1
      case 0 => 0
      case h if h >= -2 => -1
      case h if h >= -4 => -2
      case h if h >= -6 => -3
      case _ => -4
    } else extraHelp match {
      case h if h >= 8 => 4
      case h if h >= 6 => 3
      case h if h >= 4 => 2
      case h if h >= 2 => 1
      case h if h >= 0 => 0
      case -1 => -2
      case -2 => -4
    }
    normalizedHelp match {
      case 4 => 14
      case 3 => 13
      case 2 => 13
      case 1 => 11
      case 0 => 10
      case -1 => 6
      case -2 => 4
      case -3 => 2
      case -4 => 1
    }
  }

  /**
   * Returns zero-based index of our team's final place.
   */
  def getOurFinishPos(helpfulBehs: Int, minHelpRequired: Int, numTeams: Int): Int = {
    // their final position will depend on how much of the minimum help A provided.
    // If A provided 100% of help, the team will get top 3. If A provided over 100%,
    // they will get 1st. Anything less will be the % of minHelp distributed from last to 4th.
    if (helpfulBehs > minHelpRequired)
      0
    else if (helpfulBehs == minHelpRequired)
      Random.nextInt(3)
    else {
      // Out of the remaining max - 2 teams (eg, 9) how well do they do?
      // -2 because they won't have 100% to get the best place in this section, because they would
      //  have been in the top three from the last if.
      // Ex. if they did 1 helpBeh out of 10 required, they receive ((1/10) * 10 = 1) from last place
      // = lastPlace + 1
      val percHelp = helpfulBehs.asInstanceOf[Float] / minHelpRequired
      val placeFromLast = Math.round(percHelp * (numTeams - 2))
      log.debug("Our percHelp: {}, our placeFromLast: {}, our finishedPos: {}, numteams {} minus 1 minus {}",
        Array[AnyRef](double2Double(percHelp), int2Integer(placeFromLast), int2Integer((numTeams - 1) - placeFromLast), int2Integer(numTeams),
          int2Integer(placeFromLast)))
      (numTeams - 1) - placeFromLast
    }
  }
  /**
   *
   * @param finishingPositions Index 0 is first place, 1 is second place, etc.
   * @param curPoints  Current standings (points total). Round is the string index, Map[teamNum, pointsTotal]
   */
  def updateOverallPoints(finishingPositions: List[Int],
                          curPoints: Map[String, Map[String, Int]],
                          round: Int, numTeams: Int): Map[String, Int] = {
    val points = mutable.Map(getLastRoundPointsIfAny(round, numTeams, curPoints).toSeq: _*)
    // Assign the official F1 points to the places
    val officialPoints = Map(0 -> 25, 1 -> 18, 2 -> 15, 3 -> 12, 4 -> 10, 5 -> 8, 6 -> 6,
      7 -> 4, 8 -> 2, 9 -> 1)
    // only assign points for the top 10 places, or the number of teams if lesser...
    val numPlacesWithPoints = if (numTeams <= officialPoints.size) numTeams else officialPoints.size
    for (i <- 0 until numPlacesWithPoints) {
      val team = finishingPositions(i) // which team got ith place?
      points(team.toString) += officialPoints(i) // that team's points increase by the points for that place.
    }
    points.toMap
  }
  /**
   * Get each team's increase in points between standings 1 and standings 2.
   */
  def getChangeInPoints(standings1: Map[String, Int], standings2: Map[String, Int]): Map[String, Int] = {
    val ret = mutable.Map.empty[String, Int]
    for ((k, v) <- standings1) {
      ret(k) = math.abs(standings2(k) - standings1(k))
    }
    ret.toMap
  }
  def getChangeInRanking(standings1: Map[String, Int], standings2: Map[String, Int]): Map[String, Int] = {
    val ret = mutable.Map.empty[String, Int]
    for ((k, v) <- standings1) {
      ret(k) = (standings2(k) - standings1(k)) * -1
    }
    ret.toMap
  }
  /**
   * Get the standings from last round, given the current round and a map of all our standings.
   * Returns a map of 0's if this is the first race they've had.
   */
  def getLastRoundPointsIfAny(round: Int, numTeams: Int,
                              curStandings: Map[String, Map[String, Int]]): Map[String, Int] = {
    if (round > 0) {
      curStandings(Round(RoundIfAddX(round, -1)))
    } else {
      (for (i <- 0 until numTeams) yield (i.toString, 0)).toMap
    }
  }
  def getRoundRankingIfAny(round: Int, numTeams: Int,
                           curStandings: Map[String, Map[String, Int]]): Map[String, Int] = {
    if (round >= 0) {
      val pointsMap = curStandings(Round(round))
      val sortedPointList = pointsMap.toList.sortBy(x => x._2).reverse
      (for (place <- 0 until sortedPointList.length) yield {(sortedPointList(place)._1, place + 1)}).toMap
    } else {
      (for (i <- 0 until numTeams) yield (i.toString, 0)).toMap
    }
  }

}
