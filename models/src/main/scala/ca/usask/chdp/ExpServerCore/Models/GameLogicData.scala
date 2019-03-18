package ca.usask.chdp.ExpServerCore.Models

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import scala.util.Random
import org.slf4j.LoggerFactory
import scala.collection.mutable
import ca.usask.chdp.Enums._
import ca.usask.chdp.ExpSettings
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import java.io.{File, FileInputStream, ObjectInputStream}
import com.vaadin.server.{FileResource, ThemeResource, ClassResource, VaadinService}
import com.mongodb.util.JSON

/**
 * GameLogicData is the structure that keeps the logic for an entire game.
 * Hardcoded to be 10 rounds long. Can change if we need it to.
 * GameLogicData tree structured like this:
 * data.round("Rnd1").part("Part1").step(0) = first PartStep for Round1, Part1
 *
 * @param _id Set to a unique string, or it will just retrieve one of the already made GameLogics.
 */
case class GameLogicDataCC(_id: String, manipulation: Int, round: Map[String, ThisRoundsParts])
case class ThisRoundsParts(part: Map[String, ThisPartsSteps], aGoals: GoalsData, bGoals: GoalsData,
                           damageInSteps: Map[String, Int])
case class ThisPartsSteps(steps: Seq[PartStepData])
case class EqnUpgr(numOfUpr: Int, daysInEachUpgr: Seq[Int])

/**
 * This object will create a data structure representing the steps that each player is allowed to
 * perform in the entire game. It will be randomly generated each time, if you want a different
 * set of steps for every game (pair of players).
 * If you want the exact same steps (and same decision points) for every game (pair of players), then
 * create a set of game logics and save them to the database using an ID. Use that ID to retrieve the
 * game steps previously saved.
 * If an ID is given that has not been generated, this will create one and save it under that ID>
 * Thereafter, each call using that ID will retrieve that same gameLogic structure, ensuring that every
 * game (pair of players) will receive the exact same game (decision tree) for the entire game.
 *
 * We also have a tutorial gameLogic, which will *NEVER* change.
 */
object GameLogicData {

  // Create our own actor system for logging
  val log = LoggerFactory.getLogger("GameLogicData")

  private var tutorialData: GameLogicDataCC = null


  // inner class, needs access to GameLogicData.create
  object GameLogicDAO {
    lazy val dbName = if (ExpSettings.get.testingMode)
      ExpSettings.get.testing_exp_dbName
    else
      ExpSettings.get.production_exp_dbName
    lazy val coll: Option[MongoCollection] = Model.getMongoColl(dbName, "gameLogic_coll")

    def insert(logic: GameLogicDataCC) {
      if (coll.isDefined)
        coll.get += grater[GameLogicDataCC].asDBObject(logic)
    }
  }
  /**
   * Change the application.properties if you wish to make every game the same.
   */
  def getData(manipulation: Int): GameLogicDataCC = {
    val gameID = ExpSettings.get.getGameLogicDataID
    getSpecificLogicData(gameID, manipulation)
  }
  def getSpecificLogicData(id: String, manipIndex: Int): GameLogicDataCC = {
    import GameLogicDAO.coll
    //    log.debug("Attempting to create GameLogicData id: {}, manipIndex: {}", id, manipIndex)
    // Make sure it's there. If it isn't, create this gameLogic with this ID
    if (ExpSettings.get.testWithoutDB) {
      GameLogicData.create(id, manipIndex)
    } else {
      val logic = coll.get.findOneByID(id).getOrElse {
        // didn't find it. Create new one.
        val newGameLogicData = grater[GameLogicDataCC].asDBObject(GameLogicData.create(id, manipIndex))
        coll.get += newGameLogicData
        newGameLogicData
      }
      grater[GameLogicDataCC].asObject(logic)
    }
  }

  /**
   * Get the tutorial Game Data, which is saved here in code to make absolute sure we have the
   * EXACT same tutorial game for every tutorial.
   */
  def initTutorialData(vs: VaadinService) {
    if (tutorialData == null) {
      tutorialData = tutorialDataFromDisk(vs)
    }
  }
  def tutorialDataFromDisk(vs: VaadinService): GameLogicDataCC = {
    val basepath = vs.getBaseDirectory.getAbsolutePath
    val glin = new ObjectInputStream(new FileInputStream(basepath + "/WEB-INF/tutorialGameLogic.javaobj"))
    val savedGLstring = glin.readObject().asInstanceOf[String]
    val savedGLdbo = JSON.parse(savedGLstring).asInstanceOf[DBObject]
    grater[GameLogicDataCC].asObject(savedGLdbo)
  }
  def getTutorialData: GameLogicDataCC = tutorialData
  def getDBOLogicData: DBObject = {
    val glcc = GameLogicData.getSpecificLogicData("tutorial", 2)
    val gldbo =  grater[GameLogicDataCC].asDBObject(glcc)
    gldbo
  }

  /**
   * The main logic of the game is generated here. Each player's move is mapped out here as PartSteps.
   *
   *
   */
  private def create(id: String, manip: Int): GameLogicDataCC = {
    //  // Keep track of where A and B are on the partSteps.
    //  val upgradeProgress = Map[PlayerAorB, Int](
    //    A -> 0, B -> 0
    //  )

    //    val file = new java.io.PrintWriter("partsteps.txt")

    // Here is the GameLogicData we will build
    var gameLogicData = GameLogicDataCC(id, manip, Map.empty)

    // Now convert each equation value into upgrades.
    // We need to know how many upgrades are in each, how many days each of those upgrades are,
    // and we need to make sure the upgrades and days all end up = BGoals.
    for (i <- 0 until ExpSettings.get.numRounds) {
      val r: Int = i
      val firstStepThisRound = mutable.Map[String, Boolean]("Part1" -> true, "Part2" -> true, "Part3" -> true)

      // Create this round's parts steps which we will add to as we generate them.
      var rndParts = ThisRoundsParts(Map(
        // Add the starting points for every PlayerA.
        "Part1" -> ThisPartsSteps(Seq.empty[PartStepData]),
        "Part2" -> ThisPartsSteps(Seq.empty[PartStepData]),
        "Part3" -> ThisPartsSteps(Seq.empty[PartStepData])
      ), null, null, null) // record A's and B's minimums (goals) for this round, for each part.

      /**
       * for each round, find the total upgrade path.
       * upgrades must allow for: A_min + A_minHelp + A_extraHelp + B_max
       * (even though B wouldn't go over their B_max - B_surplus)
       * also would allow: A_min + A_minHelp + B_max
       * or: A_min + B_max
       */
      implicit val gameInfo = CurGameManipRnd(manip, r)
      val eachEqnUpgr = getEachEqnUpgrade

      // Now allocate each upgrade path among the parts
      // (3 parts, each for A and B, even though they are named differently, each part corresponds to the
      //  same upgrade data, and A or B can do any upgrade after A has finished A_min)
      // This will determine A's goals, given +1 kph and -.2 sec per upgrade. (changeable above)


      //      log.debug("A_min: {}, A_minHelp: {}, A_bonusHelp: {}, B_max: {}", Array(ExpSettings.get.eqnVal(manip, r, A_min),
      //        ExpSettings.get.eqnVal(manip, r, A_minHelp), ExpSettings.get.eqnVal(manip, r, A_bonusHelp), ExpSettings.get.eqnVal(manip, r, B_max)))
      //      log.debug("eachEqnUpgrades: {}", eachEqnUpgr)

      for (eqn <- eachEqnUpgr) {
        val partsToBeUpgr = randListParts(eqn.numOfUpr) // which parts will this eqn portion be upgrading?
        var upgrIndex = 0 // track the index of this part in partsToBeUpgr
        val firstStepThisPart = mutable.Map[String, Boolean]("Part1" -> true, "Part2" -> true, "Part3" -> true)

        for (p <- partsToBeUpgr) {
          var wasPreviousStepComplete: Boolean = false
          var partStepWeAreBuilding = firstStepThisRound(p) match {
            case true => {
              // treat the first part differently -- we have it specified above.
              firstStepThisRound(p) = false
              wasPreviousStepComplete = false
              val (prevRndStep, prevRndDmg) = if (r > 0)
                (gameLogicData.round(Round(RoundIfAddX(r, -1))).part(p).steps.last,
                  gameLogicData.round(Round(RoundIfAddX(r, -1))).damageInSteps)
              else
                (null, null)
              startParts(p, r, prevRndStep, prevRndDmg)
            }
            case false => {
              // first day of a new upgrade, build the newPart based on last step's data
              val prevStep = rndParts.part(p).steps.last
              val curName = prevStep.nextName
              val curData = prevStep.nextData
              val nextName = PartMarkIfAddX(curName, 1)
              val nextData = curData + ExpSettings.get.amtChange(p)
              wasPreviousStepComplete = prevStep.getIsComplAfterThis
              PartStepData(curName, curData, nextName, nextData)
            }
          }
          val upgrChances = getChances(eqn.daysInEachUpgr(upgrIndex)) // start with 25 percent as bottom limit
          // for each day in this upgrade, build the partStep
          for (day <- 1 to eqn.daysInEachUpgr(upgrIndex)) {
            // they completed a component last click. But display this message only once after completing it.
            val curStatusBar =
              if (wasPreviousStepComplete == true)
                "Congratulations, you upgraded to a " + partStepWeAreBuilding.curName + "!"
              else ""
            wasPreviousStepComplete = false

            val upgrChance = upgrChances.take(day).sum
            val complAfterThis = (day == eqn.daysInEachUpgr(upgrIndex))

            partStepWeAreBuilding = partStepWeAreBuilding.copy(chance = upgrChance, statusBar = curStatusBar,
              isComplAfterThis = complAfterThis)
            // finished constructing this PartStep, now add it.
            // Only adding ONE PartStep, but the datastructure is immutable, so we have to build a new structure.
            // take rndParts.part(p).step (Vector[PartStep]) and add the newPartStep to the end of it.
            val oldThisPartsSteps = rndParts.part(p)
            val newThisPartsSteps = ThisPartsSteps(oldThisPartsSteps.steps :+ partStepWeAreBuilding)
            // replace oldThisPartsStep with the new one.
            rndParts = rndParts.copy(part = rndParts.part + (p -> newThisPartsSteps))

          }
          upgrIndex += 1
        }
        // check if this was the A_min portion of eqn
        //    (always the first part of the eqn we fill out, therefore it's index is 0).
        // If so, record these as PlayerA's goals.
        if (eqn == eachEqnUpgr(0)) {
          // count the number of steps that have "isCompleAfterThis" which means that it is a complete upgrade.
          //val numUpgrForP1 = rndParts.part("Part1").step.filter(_.isComplAfterThis == RoundComplAfterThis.True).length
          //val numUpgrForP2 = rndParts.part("Part2").step.filter(_.isComplAfterThis == RoundComplAfterThis.True).length
          //val numUpgrForP3 = rndParts.part("Part3").step.filter(_.isComplAfterThis == RoundComplAfterThis.True).length
          val Part1Data = rndParts.part("Part1").steps.last.nextData
          val Part2Data = rndParts.part("Part2").steps.last.nextData
          val Part3Data = rndParts.part("Part3").steps.last.nextData
          rndParts = rndParts.copy(aGoals = GoalsData(Part1Data, Part2Data, Part3Data))
        } else if (eqn == eachEqnUpgr.last) {
          // just finished B_max
          val Part1Data = rndParts.part("Part1").steps.last.nextData
          val Part2Data = rndParts.part("Part2").steps.last.nextData
          val Part3Data = rndParts.part("Part3").steps.last.nextData
          rndParts = rndParts.copy(bGoals = GoalsData(Part1Data, Part2Data, Part3Data))
          // And add one more day to each part, so that the player can move past it to "complete" it and change their
          // curData statistic to be the data of the Part they just finished (and be able to reach B_max)
          for (prt <- PartNumValues) {
            val oldPart1Steps = rndParts.part(prt)
            val oldP = oldPart1Steps.steps.last
            val newP = PartStepData(oldP.nextName, oldP.nextData, PartMarkIfAddX(oldP.nextName, 1),
              oldP.nextData + ExpSettings.get.amtChange(prt), 45, "Congratulations, you have finished upgrading!")
            val newPart1Steps = ThisPartsSteps(oldPart1Steps.steps :+ newP)
            // replace oldThisPartsStep with the new one.
            rndParts = rndParts.copy(part = rndParts.part + (prt -> newPart1Steps))
          }
        }
      }
      //      val numDays = rndParts.part("Part1").steps.length + rndParts.part("Part2").steps.length +
      //        rndParts.part("Part3").steps.length
      //      file.println("Finished Round " + r + ". Days of work in this round, total: " + numDays)
      //      file.println("Part1 PartSteps:")
      //      rndParts.part("Part1").steps.map(file.println(_))
      //      file.println("Part2 PartSteps:")
      //      rndParts.part("Part2").steps.map(file.println(_))
      //      file.println("Part3 PartSteps:")
      //      rndParts.part("Part3").steps.map(file.println(_))

      // add damage. Must not let the next round get to 11.
      // say max is around 7 or 8, so reduce part down to 1 to 3
      val finalPartName = Map("Part1" -> rndParts.part("Part1").steps.last.getCurName,
        "Part2" -> rndParts.part("Part2").steps.last.getCurName,
        "Part3" -> rndParts.part("Part3").steps.last.getCurName)
      val newDamage = Map("Part1" -> getDamage(finalPartName("Part1")),
        "Part2" -> getDamage(finalPartName("Part2")),
        "Part3" -> getDamage(finalPartName("Part3")))

      rndParts = rndParts.copy(damageInSteps = newDamage)
      // Add the round to the GameLogicData map.
      gameLogicData = gameLogicData.copy(round = gameLogicData.round + (Round(r) -> rndParts))
    }
    //    file.close()

    // return the gameLogic
    gameLogicData
  }

  /**
   * Helpers
   *
   */
  def startParts(partNum: String, round: Int, prevStep: PartStepData, damage: Map[String, Int]): PartStepData = {
    if (round == 0)
      Map(
        // Add the starting points for every PlayerA.
        "Part1" -> PartStepData("MarkI", 270, "MarkII", (270 + ExpSettings.get.amtChange("Part1")), 45, "", false),
        "Part2" -> PartStepData("MarkI", 50, "MarkII", (50 + ExpSettings.get.amtChange("Part2")), 55, "", false),
        "Part3" -> PartStepData("MarkI", 155, "MarkII", (155 + ExpSettings.get.amtChange("Part3")), 50, "", false)
      )(partNum)
    else {
      // calculate starting round part by damaging their previous parts.
      // remember that the last step's curName was where the round ends.
      val curName = PartMarkIfAddX(prevStep.curName, -damage(partNum))
      val curData = prevStep.curData - damage(partNum) * ExpSettings.get.amtChange(partNum)
      val nextName = PartMarkIfAddX(curName, 1)
      val nextData = curData + ExpSettings.get.amtChange(partNum)
      PartStepData(curName, curData, nextName, nextData)
    }
  }

  def getDamage(partMark: String): Int = {
    // want to start at between 1 and 3 (but zero indexed)
    val newMark = Random.nextInt(2) + 1
    PartMarkID(partMark) - newMark
  }

  /**
   * Find the next chance, between the (previous chances + 1) and 90.
   */
  val r = new scala.util.Random
  def getChances(numUpgr: Int): Seq[Int] = {
    assert(numUpgr > 0)
    val start = Random.nextInt(21) + 20  // between 20 and 40
    if (numUpgr == 1) {
      Seq(start)
    } else {
      val maxPerUpgr = ((90 - start) / (numUpgr-1)) // divide the rest of the upgrades up evenly
      val nextUpgrOutOf = maxPerUpgr
      val res = for (i <- 1 to numUpgr-1) yield {
        nextUpgrOutOf - Random.nextInt(nextUpgrOutOf/3)  // next upgrade should be between 2/3rds of the max and the max
        //val thisUpgr = Gen.choose(nextUpgrOutOf/3, nextUpgrOutOf).sample.get
//        nextUpgrOutOf += nextUpgrOutOf - thisUpgr
      }
      Seq(start) ++ res
    }
  }
  /**
   * Takes number of days and converts it to a tuple of (number of upgrades, vector of each upgrade's number of days).
   * @return (number of upgrades, vector of each upgrade's number of days)
   */
  def getEachEqnUpgrade(implicit gameState: CurGameManipRnd) = List(
    convertDaysToUpgrades(ExpSettings.get.eqnVal(GameEqn.A_persGoal)), //
    convertDaysToUpgrades(ExpSettings.get.eqnVal(GameEqn.A_minHelp)),
    convertDaysToUpgrades(ExpSettings.get.eqnVal(GameEqn.A_extraHelp)),
    convertDaysToUpgrades(ExpSettings.get.eqnVal(GameEqn.B_min))
  )
  def convertDaysToUpgrades(numDays: Int): EqnUpgr = {
    ExpSettings.get.PartUpgrGenerator match {
      case ExpSettings.RANDOM_WEIGHT => GameLogicData.convertDaysToUpgradesRandom(numDays)
      case ExpSettings.WEIGHTED_HIGHER => GameLogicData.convertDaysToUpgradesWeighted(numDays)
    }

  }
  def convertDaysToUpgradesWeighted(numDays: Int): EqnUpgr = {
    var daysLeft: Int = numDays
    var upgrSeq = Seq.empty[Int]
    while (daysLeft >= weightedDist.lowerLimit) {
      val next: Int = weightedDist.getSample
      if (next <= daysLeft) {
        upgrSeq = upgrSeq :+ next
        daysLeft -= next
      }
    }
    if (daysLeft > 0)
      upgrSeq = upgrSeq :+ daysLeft
    EqnUpgr(upgrSeq.length, upgrSeq)
  }
  def convertDaysToUpgradesRandom(numDays: Int): EqnUpgr = {
    var daysLeft = numDays
    var numUpgr = 0
    var upgrVec = Vector[Int]()
    while (daysLeft > 0) {
      val days: Int = numDaysForThisUpgrade(daysLeft)
      upgrVec :+= days
      numUpgr += 1
      daysLeft -= days
    }
    EqnUpgr(numUpgr, upgrVec)
  }
  object weightedDist {
    def upperLimit = 4
    def lowerLimit = 2
    def getSample = {
      // fill the "weighted distribution"
      val dist = Array(2, 3, 3, 4, 4, 4)
      dist( Random.nextInt(dist.length) )
    }
    //    def getSample = {
    //      Gen.frequency(
    //        (1, 2),
    //        (2, 3),
    //        (3, 4)
    //      ).sample.get
    //    }
  }
  def numDaysForThisUpgrade(daysLeftToWork: Int): Int = {
    // first make sure we have at least 1 day left to work.
    if (daysLeftToWork <= 0) {
      //val log = LoggerFactory.getLogger("GameLogicCreatorHelpers # roundsThisUpgrade")
      //log.error("Should not have been called when days of work left = {}", daysLeftToWork)
      log.error("GameLogicData -- numDaysForThisUpgrade -- Should not have been called when days of work left")
      return 0
    }
    // If there are 3 or more days available, each upgrade time is random between 1 and 3 days
    // If there are 1 or 2 days available, the upgrade time is those remaining days.
    if (daysLeftToWork >= 3) {
      r.nextInt(3) + 1
    } else {
      daysLeftToWork
    }
  }

  def randListParts(num: Int): List[String] = {
    // random parts, but evenly distributed among them.
    val r = new scala.util.Random
    val start = r.nextInt(3)
    var ret = List[String]()
    for (i <- 0 until num) {
      // next part num, starting with start, add to list.
      val curPartNum = PartNum((start + i) % 3)
      ret ::= curPartNum
    }
    r.shuffle(ret)
  }

}
