import ca.usask.chdp.ExpServerCore.Game
import scala.util.Random

/**
 * Do a simulation based on the team's abilities
 */
/*

// for testing : teamIndexs.map{i => (i, ExpSettings.get.teamSeq(i).ability)} will give:
var teamsAndAbility = Seq((1,8), (2,9), (3,9), (4,7), (5,6), (6,5), (7,1), (
  8,4), (9,2), (10,1), (11,3))


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
    case 2 => 3
    case 1 => 2
    case 0 => 1
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
    case 4 => 16
    case 3 => 15
    case 2 => 13
    case 1 => 12
    case 0 => 10
    case -1 => 6
    case -2 => 4
    case -3 => 2
    case -4 => 1
  }
}
println("with our ability at 9")
teamsAndAbility ++= Seq((0, 10))
//println("version 1")
//for (i <- 1 to 20) {
//  println(getFinalPositions(teamsAndAbility, generateProbList1))
//}
//println("version 2")
//for (i <- 1 to 20) {
//  println(getFinalPositions(teamsAndAbility, generateProbList2))
//}
println("version 3")
for (i <- 1 to 20) {
  println(getFinalPositions(teamsAndAbility, generateProbList3))
}
*/
for (i <- 1 to 20) {
 println(Game.getAllFinishPositions(12, 11, 2, 1, 5))
}









































































































