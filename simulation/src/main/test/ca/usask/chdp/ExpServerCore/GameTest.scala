package ca.usask.chdp.ExpServerCore

import ExpActors.Lobby
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Gen
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import ca.usask.chdp.Enums._
import Game._

class GameTest extends FlatSpec with ShouldMatchers with PropertyChecks {
  "GetFinishedPos" should "return a pos in the valid range given the amount of help" in {
    val myGen = for {
      a <- Gen.choose(0, 100)
      b <- Gen.choose(0, 100)
      c <- Gen.choose(6, 20)
    } yield (a, b, c)

    forAll(myGen) {
      (p: (Int, Int, Int)) =>
        val (help, helpRequired, numTeams) = p
        val pos = getOurFinishPos(help, helpRequired, numTeams)
        if (help > helpRequired)
          pos should be === 0
        if (help == helpRequired)
          pos should (be >= 0 and be <= 2)
        if (help < helpRequired) {
          pos should (be <= numTeams - 1 and be > 2)
        }

    }
  }
  "CreateRace" should "return a winning race if we help over minimum each round" in {
    Lobby.initSettings()
    val myGen = for {
      trackNum <- Gen.choose(0, Lobby.settings.numTracks - 1)
      helpBehs <- Gen.choose(11, 20) // always helping more than needed
      curRnd <- Gen.choose(0, Lobby.settings.numRounds - 1)
      numTeams <- Gen.choose(6, 20)
    } yield (trackNum, helpBehs, curRnd, numTeams)

    forAll(myGen) {
      (p: (Int, Int, Int, Int)) =>
      //println("Trying this combination (trackNum, helpBehs, curRnd, numTeams) = " + p)
        val (trackNum, helpBehs, curRnd, numTeams) = p
        val track = Lobby.settings.trackSeq(trackNum)
        val gameManRnd = CurGameManipRnd(Lobby.settings.getManipIndexForTesting, curRnd)
        val res = createRace(track, helpBehs, numTeams, gameManRnd.manipulation, gameManRnd.manipulation)
        res.last(0) should be === 0 // we should be first at the end of the race
    }
  }
  it should "put us in top three if we help bare minimum" in {
    Lobby.initSettings()
    val myGen = for {
      trackNum <- Gen.choose(0, Lobby.settings.numTracks - 1)
      curRnd <- Gen.choose(0, Lobby.settings.numRounds - 1)
      numTeams <- Gen.choose(6, 20)
    } yield (trackNum, curRnd, numTeams)

    forAll(myGen) {
      (p: (Int, Int, Int)) =>
      //println("Trying this combination (trackNum, curRnd, numTeams) = " + p)
        val (trackNum, curRnd, numTeams) = p
        val track = Lobby.settings.trackSeq(trackNum)
        val gameManRnd = CurGameManipRnd(Lobby.settings.getManipIndexForTesting, curRnd)
        val res = createRace(track, 10, numTeams, gameManRnd.manipulation, gameManRnd.manipulation)
        res.last.indexOf(0) should (be >= 0 and be <= 2) // we should be in top 3e
    }
  }
  it should "give us the right number of laps" in {
    Lobby.initSettings()
    val myGen = for {
      trackNum <- Gen.choose(0, Lobby.settings.numTracks - 1)
      curRnd <- Gen.choose(0, Lobby.settings.numRounds - 1)
      numTeams <- Gen.choose(6, 20)
    } yield (trackNum, curRnd, numTeams)

    forAll(myGen) {
      (p: (Int, Int, Int)) =>
      //println("Trying this combination (trackNum, curRnd, numTeams) = " + p)
        val (trackNum, curRnd, numTeams) = p
        val track = Lobby.settings.trackSeq(trackNum)
        val gameManRnd = CurGameManipRnd(Lobby.settings.getManipIndexForTesting, curRnd)
        val res = createRace(track, 10, numTeams, gameManRnd.manipulation, gameManRnd.manipulation)
        res.length should be === track.numLaps
    }
  }
  it should "not let a driver jump more than one rank per lap" in {
    Lobby.initSettings()
    val myGen = for {
      trackNum <- Gen.choose(0, (Lobby.settings.numTracks - 1))
      curRnd <- Gen.choose(0, Lobby.settings.numRounds - 1)
      helpBehs <- Gen.choose(0, Lobby.settings.eqnVal(GameEqn.A_discretionary)(CurGameManipRnd(1, curRnd)))
      numTeams <- Gen.choose(6, 20)
    } yield (trackNum, curRnd, helpBehs, numTeams)

    forAll(myGen) {
      (p: (Int, Int, Int, Int)) => {
        //println("Trying this combination (trackNum, helpBehs, curRnd, numTeams) = " + p)
        val (trackNum, curRnd, helpBehs, numTeams) = p
        val track = Lobby.settings.trackSeq(trackNum)
        val gameManRnd = CurGameManipRnd(Lobby.settings.getManipIndexForTesting, curRnd)
        val res = createRace(track, helpBehs, numTeams, gameManRnd.manipulation, gameManRnd.manipulation)
        for (i <- 1 until track.numLaps) {
          val lastLap = res(i - 1)
          val curLap = res(i)
          for (pos <- 0 until numTeams) {
            val curDriver = curLap(pos)
            assert(getDriversOneSlotAroundPos(pos, lastLap).contains(curDriver))
          }
        }
      }
    }

  }
  def getDriversOneSlotAroundPos(pos: Int, ranking: Seq[Int]): Seq[Int] = {
    if (pos == 0)
    //println("ranking is: " + ranking + " ranking 0 and 1 are: " + ranking(0) + "; " + ranking(1))
      Seq(ranking(0), ranking(1))
    else if (pos == ranking.length - 1)
      Seq(ranking(pos), ranking(pos - 1))
    else
      Seq(ranking(pos - 1), ranking(pos), ranking(pos + 1))
  }
  it should "give correct standings for each position" in {
    Lobby.initSettings()
    val officialPoints = Map(0 -> 25, 1 -> 18, 2 -> 15, 3 -> 12, 4 -> 10, 5 -> 8, 6 -> 6,
      7 -> 4, 8 -> 2, 9 -> 1)
    val myGen = for {
      helpBehs <- Gen.choose(0, Lobby.settings.eqnVal(GameEqn.A_discretionary)(CurGameManipRnd(1, 1)))
      numTeams <- Gen.choose(6, 20)
    } yield (helpBehs, numTeams)

    forAll(myGen) {
      p: (Int, Int) => {
        //println("Trying this combination (helpBehs, numTeams) = " + p)
        val (helpBehs, numTeams) = p
        val gameManRnd = CurGameManipRnd(Lobby.settings.getManipIndexForTesting, 1)
        val A_MinimumHelp = Lobby.settings.eqnVal(GameEqn.A_minHelp)(gameManRnd)
        val finalPsns = getAllFinishPositions(numTeams, helpBehs, A_MinimumHelp, gameManRnd.manipulation, gameManRnd.manipulation)
        val lastRndStandings = (for (i <- 0 until numTeams) yield (i.toString, 0)).toMap
        val curStandings = Map(Round(0) -> lastRndStandings)
        val numPlacesWithPoints = if (numTeams <= officialPoints.size) numTeams else officialPoints.size
        val finalPoints = lastRndStandings ++ (for (i <- 0 until numPlacesWithPoints) yield {
          val team = finalPsns(i)
          (team.toString, officialPoints(i))
        }).toMap
        val points = updateOverallPoints(finalPsns, curStandings, 0, numTeams)
        points should be === finalPoints
      }
    }
  }
  it should "give correct standings for each position with prev Standings" in {
    Lobby.initSettings()
    val officialPoints = Map(0 -> 25, 1 -> 18, 2 -> 15, 3 -> 12, 4 -> 10, 5 -> 8, 6 -> 6,
      7 -> 4, 8 -> 2, 9 -> 1)
    val myGen = for {
      helpBehs <- Gen.choose(0, Lobby.settings.eqnVal(GameEqn.A_discretionary)(CurGameManipRnd(1, 1)))
      numTeams <- Gen.choose(6, 20)
    } yield (helpBehs, numTeams)

    forAll(myGen) {
      p: (Int, Int) => {
        println("Trying this combination (helpBehs, numTeams) = " + p)
        val (helpBehs, numTeams) = p
        val gameManRnd = CurGameManipRnd(Lobby.settings.getManipIndexForTesting, 1)
        val A_MinimumHelp = Lobby.settings.eqnVal(GameEqn.A_minHelp)(gameManRnd)
        val finalPsns = getAllFinishPositions(numTeams, helpBehs, A_MinimumHelp, gameManRnd.manipulation, gameManRnd.manipulation)
        val lastRndStandings = (for (i <- 0 until numTeams) yield
          (i.toString, Gen.choose(1, 100).sample.get)).toMap
        val curStandings = Map(Round(0) -> lastRndStandings)
        val numPlacesWithPoints = if (numTeams <= officialPoints.size) numTeams else officialPoints.size
        val finalPoints = lastRndStandings ++ (for (i <- 0 until numPlacesWithPoints) yield {
          val team = finalPsns(i)
          (team.toString, lastRndStandings(team.toString) + officialPoints(i))
        }).toMap
        val points = updateOverallPoints(finalPsns, curStandings, 1, numTeams)
        points should be === finalPoints
      }
    }
  }
}
