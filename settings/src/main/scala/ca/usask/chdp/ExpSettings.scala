package ca.usask.chdp

import com.typesafe.config.{ConfigFactory, Config}
import util.Random
import java.util.UUID
import ca.usask.chdp.Enums._
import ca.usask.chdp.ExpSettings.{TrackInfo, TeamInfo, CurGameManipRnd}
import org.slf4j.LoggerFactory
import org.joda.time.format.DateTimeFormat
import scala.collection.JavaConversions._

/**
 * Eagerly initializes settings from application.conf so that problems are found at startup and access is typesafe
 * and checked by the IDE.  (Already had problems mispelling a setting and the system crashing. Baaaad.)
 * Yes, it's more work. But it's less work than running, crashing, and fixing a string.
 */
class ExpSettings(c: Config) {
  val log = LoggerFactory.getLogger( classOf[ExpSettings])
  log.debug( "Loading new config factory.")

  // This uses the standard default Config, if none is provided,
  // which simplifies apps willing to use the defaults
  def this() {
    this( ConfigFactory.load( ))
  }

  // This verifies that the Config is sane and has our
  // reference config. Importantly, we specify the "exp"
  // path so we only validate settings that belong to this
  // library. Otherwise, we might throw mistaken errors about
  // settings we know nothing about.
  c.checkValid( ConfigFactory.defaultReference( ), "exp")

  // non-lazy fields, we want all exceptions at construct time
  val portSimulation = c.getInt( "exp.port.simulation")
  val portAdmin = c.getInt( "exp.port.admin")
  val portSignUp = c.getInt( "exp.port.signup")
  val portSurvey = c.getInt( "exp.port.survey")

  var testingMode = {
    log.warn( "config variable accessed: testingMode")
    c.getBoolean("exp.testingMode")
  }
  val testing_exp_dbName = c.getString( "exp.testing_exp_dbName")
  val production_exp_dbName = c.getString( "exp.production_exp_dbName")
  val testing_signUp_dbName = c.getString( "exp.testing_signUp_dbName")
  val production_signUp_dbName = c.getString( "exp.production_signUp_dbName")

  val testing_mba877_dbName = c.getString( "exp.testing_mba877_dbName")
  val production_mba877_dbName = c.getString( "exp.production_mba877_dbName")

  val testing_autoLogin = c.getBoolean( "exp.testing_autoLogin")
  val testing_autoWork = c.getBoolean( "exp.testing_autoWork")
  val testing_skipTutorial = c.getBoolean( "exp.testing_skipTutorial")
  val testing_skipRace = c.getBoolean( "exp.testing_skipRace")
  val testing_skipSurveyLogin = c.getBoolean( "exp.testing_skipSurveyLogin")
  var testing_useSynchronousDB = c.getBoolean( "exp.testing_useSynchronousDB")
  var testing_useMemCache = c.getBoolean( "exp.testing_useMemCache")
  val testing_htmlPort_signup = c.getInt( "exp.testing.htmlPort.signup")
  val testing_integrationTesting = c.getBoolean( "exp.testing_integrationTesting")
  val testing_unitTests_doNotStartGames = c.getBoolean( "exp.testing_unitTests_doNotStartGames")
  val testing_autoControlQuestions = c.getBoolean( "exp.testing_autoControlQuestions")
  val testing_autoTutorialWalkthrough = c.getBoolean( "exp.testing_autoTutorialWalkthrough")
  val testing_autoGameStart = c.getBoolean( "exp.testing_autoGameStart")
  val testing_autoClickWindows = c.getBoolean( "exp.testing_autoClickWindows")
  val testing_autoClickResults = c.getBoolean( "exp.testing_autoClickResults")
  val testing_autoClickDamageReport = c.getBoolean( "exp.testing_autoClickDamageReport")
  val testing_autoClickInternalSurvey = c.getBoolean( "exp.testing_autoClickInternalSurvey")
  val testing_skipStatusBarDelay = c.getBoolean( "exp.testing_skipStatusBarDelay")
  val testing_loginViewAutoRegister = c.getBoolean("exp.testing_loginViewAutoRegister")
  val autoWorkDelay = c.getInt( "exp.autoWorkDelay")


  //val adminPassword = c.getString( "exp.admin")
  val adminPassword = System.getenv("EXP_ADMIN_PWD")

  val fmt = DateTimeFormat.forPattern( "yyyy-MM-dd, hh:mm aa")
  val deadlineToRegisterForAltAssignment = fmt.parseDateTime(
    c.getString( "exp.deadlineToRegisterForAltAssignment"))

  private val isRandomGameLogicID = c.getBoolean( "exp.isRandomGameLogicID")
  private val gameLogicIDIfNotRandom = c.getString( "exp.gameIDIfNotRandom")
  def getGameLogicDataID: String = {
    if (isRandomGameLogicID)
      UUID.randomUUID( ).toString
    else
      gameLogicIDIfNotRandom
  }

  /** ----------------------------------------------
    * Survey settings:
   */
  val giveSurveyBeforeFirstRound = c.getBoolean("exp.giveSurveyBeforeFirstRound")
  val surveyAfterRounds = c.getIntList( "exp.surveyAfterRounds")
  val halfwaySurveyRound = c.getInt( "exp.halfwaySurveyRound")
  val preSessionSurvey3Location = c.getString( "exp.preSessionSurvey3Location")
  val postSessionSurveyLocationA = c.getString( "exp.postSessionSurveyLocationA")
  val postSessionSurveyLocationB = c.getString( "exp.postSessionSurveyLocationB")

  /**
   * surveyRedirect settings:
   */

  val preSessionSurvey1Location_w2014comm105 = c.getString( "exp.preSessionSurvey1Location_w2014comm105")

  /** ----------------------------------------------
   * locations info
   */
  val diffLocationsMatter = c.getBoolean("exp.diffLocationsMatter")
  val diffLocationsAreMatchedAcrossLocation = c.getBoolean("exp.diffLocationsAreMatchedAcrossLocation")
  val allLocations: Seq[String] = c.getStringList("exp.allLocations")
  val defaultLocation = allLocations(c.getInt("exp.defaultLocation"))
  val otherLocation = allLocations(c.getInt("exp.otherLocation"))

  val numRounds = c.getInt( "exp.numRounds")

  /** ----------------------------------------------
    * manipulations info
    */
  private val testing_isRandomManipulation = c.getBoolean( "exp.testing_isRandomManipulation")
  private val numOfManip = c.getInt( "exp.numOfManip")
  val usingBothManipulations = c.getBoolean( "exp.usingBothManipulations")
  val manipIfNotUsingBoth = c.getInt("exp.manipIfNotUsingBoth")

  val serverDownAllowNewLogins = c.getBoolean("exp.serverDownAllowNewLogins")


  private val manip1 = c.getString( "exp.manip.1")
  private val manip2 = c.getString( "exp.manip.2")
  // but make lookup easy:
  private def manip(num: Int): String = {
    assert( num > 0 && num <= numOfManip)
    num match {
      case 1 => manip1
      case 2 => manip2
    }
  }


  def getManipIndexForTesting: Int = {
    if (testing_isRandomManipulation) {
      Random.nextInt( 2) + 1
    } else {
      manipIfNotUsingBoth
    }
  }

  val A_max = c.getInt( "exp.A_max")
  val B_max = c.getInt( "exp.B_max")

  // eager:
  val numOfParts = c.getInt( "exp.numOfParts")
  val amtOfChangePart1 = c.getInt( "exp.amtOfChange.Part1")
  val amtOfChangePart2 = c.getInt( "exp.amtOfChange.Part2")
  val amtOfChangePart3 = c.getInt( "exp.amtOfChange.Part3")
  // ease of use:
  def amtChange(partNum: String) = {
    val partID = PartNumID( partNum)
    partID match {
      case 0 => amtOfChangePart1
      case 1 => amtOfChangePart2
      case 2 => amtOfChangePart3
    }
  }
  def partDataAtRnd(partNum: String, round: String): Int = {
    startingData( partNum) + RoundID( round) * amtChange( partNum)
  }

  val dataStartPart1 = c.getInt( "exp.data.start.Part1")
  val dataStartPart2 = c.getInt( "exp.data.start.Part2")
  val dataStartPart3 = c.getInt( "exp.data.start.Part3")
  def startingData(partNum: String) = {
    val partNumID = PartNumID( partNum)
    c.getInt( "exp.data.start.Part" + partNumID)
  }

  val highDepA_persGoal1 = c.getInt( "exp.highDep.A_persGoal.1")
  val highDepA_persGoal2 = c.getInt( "exp.highDep.A_persGoal.2")
  val highDepA_persGoal3 = c.getInt( "exp.highDep.A_persGoal.3")
  val highDepA_persGoal4 = c.getInt( "exp.highDep.A_persGoal.4")
  val highDepA_persGoal5 = c.getInt( "exp.highDep.A_persGoal.5")
  val highDepA_persGoal6 = c.getInt( "exp.highDep.A_persGoal.6")
  val highDepA_persGoal7 = c.getInt( "exp.highDep.A_persGoal.7")
  val highDepA_persGoal8 = c.getInt( "exp.highDep.A_persGoal.8")
  val highDepA_persGoal9 = c.getInt( "exp.highDep.A_persGoal.9")
  val highDepA_persGoal10 = c.getInt( "exp.highDep.A_persGoal.10")
  val highDepB_dep1 = c.getInt( "exp.highDep.B_dep.1")
  val highDepB_dep2 = c.getInt( "exp.highDep.B_dep.2")
  val highDepB_dep3 = c.getInt( "exp.highDep.B_dep.3")
  val highDepB_dep4 = c.getInt( "exp.highDep.B_dep.4 ")
  val highDepB_dep5 = c.getInt( "exp.highDep.B_dep.5")
  val highDepB_dep6 = c.getInt( "exp.highDep.B_dep.6")
  val highDepB_dep7 = c.getInt( "exp.highDep.B_dep.7")
  val highDepB_dep8 = c.getInt( "exp.highDep.B_dep.8")
  val highDepB_dep9 = c.getInt( "exp.highDep.B_dep.9")
  val highDepB_dep10 = c.getInt( "exp.highDep.B_dep.10")

  // ease:
  def A_persGoal(manipIdx: Int, round: Int) = {
    assert( manipIdx > 0 && manipIdx <= numOfManip && round > 0 && round <= numRounds)
    c.getInt( "exp." + c.getString( "exp.manip." + manipIdx) + ".A_persGoal." + round)
  }
  def B_dep(manipulation: Int, round: Int) = {
    assert( manipulation > 0 && manipulation <= numOfManip && round > 0 && round <= numRounds)
    c.getInt( "exp." + c.getString( "exp.manip." + manipulation) + ".B_dep." + round)
  }
  /**
   *
   * @param eqnVal A_max, B_max, A_persGoal, B_dep, A_minHelp, B_goals, A_extraHelp, B_min, A_discretionary
   * @param gInfo CurGameManipRnd
   * @return
   */
  def eqnVal(eqnVal: GameEqn.Value)(implicit gInfo: CurGameManipRnd): Int = {
    val round = gInfo.round + 1 // enums are 0-based, i.e. Round1.id = 0, so add 1.
    assert(
      gInfo.manipulation > 0 && gInfo.manipulation <= numOfManip && round > 0 && round <= numRounds)

    val aPersGoal = A_persGoal( gInfo.manipulation, round)
    val A_reqHelp = B_dep( gInfo.manipulation, round)

    eqnVal match {
      case GameEqn.A_max => this.A_max
      case GameEqn.B_max => this.B_max
      case GameEqn.A_persGoal => aPersGoal
      case GameEqn.A_minHelp => A_reqHelp
      case GameEqn.B_goals => this.B_max + A_reqHelp
      case GameEqn.A_extraHelp => this.A_max - aPersGoal - A_reqHelp
      // B's minimum they need to complete, IF A helps out completely.
      case GameEqn.B_min => B_max - (this.A_max - aPersGoal - A_reqHelp)
      case GameEqn.A_discretionary => this.A_max - aPersGoal
    }
  }

  var testWithoutDB = c.getBoolean( "exp.testWithoutDB")


  /**
   * Tracks and teams
   */
  val numTracks = c.getInt( "exp.numTracks")
  val trackSeq = for (i <- 0 until numTracks) yield {
    TrackInfo(
      c.getString( "exp.track." + i + ".name"),
      c.getString( "exp.track." + i + ".country"),
      c.getString( "exp.track." + i + ".track"),
      c.getInt( "exp.track." + i + ".laps"),
      c.getString( "exp.track." + i + ".circuitLength"),
      c.getString( "exp.track." + i + ".raceLength"),
      "images/flags/" + c.getString( "exp.track." + i + ".flagImg"),
      "images/tracks/" + c.getString( "exp.track." + i + ".trackImg"),
      "images/weather/" + c.getString( "exp.track." + i + ".conditionsImg"),
      c.getString( "exp.track." + i + ".conditionsText"))
  }
  val numTeams = c.getInt( "exp.numTeams")
  val teamSeq = for (i <- 0 until numTeams) yield {
    TeamInfo(
      c.getString( "exp.team." + i + ".name"),
      c.getString( "exp.team." + i + ".driver"),
      "images/teams/" + c.getString( "exp.team." + i + ".teamLogo"),
      c.getString( "exp.team." + i + ".driverFlag"),
      c.getString( "exp.team." + i + ".teamShortName"),
      c.getInt( "exp.team." + i + ".ability"))
  }

  /**
   * Can set programmatically:
   */
  var PartUpgrGenerator = c.getString( "exp.PartUpgrGenerator")
}

object ExpSettings {
  /**
   * Manipulation: 1 or 2, round is zero-based
   */
  case class CurGameManipRnd(manipulation: Int, round: Int)
  case class TrackInfo(name: String,
    country: String,
    trackName: String,
    numLaps: Int,
    circuitLength: String,
    raceLength: String,
    flagImg: String,
    trackImg: String,
    conditionsImg: String,
    conditionsText: String)
  case class TeamInfo(name: String,
    driver: String,
    teamLogo: String,
    driverFlag: String,
    teamShortName: String,
    ability: Int)
  case class RaceHistory(trackInfo: TrackInfo, lapHistory: Seq[Seq[Int]])

  /**
   * This is used by modules who don't have an akka configuration file already.
   */
  var get = new ExpSettings( ConfigFactory.load( ))
  def reset() {
    ConfigFactory.invalidateCaches( )
    get = new ExpSettings( ConfigFactory.load( ))
  }
  def resetWith(configFile: String) { get = new ExpSettings( ConfigFactory.load( configFile)) }

  val RANDOM_WEIGHT = "RANDOM_WEIGHT"
  val WEIGHTED_HIGHER = "WEIGHTED_HIGHER"
}
