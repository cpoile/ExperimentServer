package ca.usask.chdp.ExpServerCore.Models

import com.mongodb.casbah.Imports._
import java.util.UUID
import org.scalatest.matchers.ShouldMatchers
import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpSettings
import com.novus.salat.grater
import com.novus.salat.global._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import com.typesafe.config.ConfigFactory
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import ca.usask.chdp.Enums._


class GameLogicDBTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  //import Lobby.settings
  val log = LoggerFactory.getLogger("GameLogicDBTest")
  var settings: ExpSettings = null
  var dbName: String = _

  override protected def beforeAll() {
    val originalCfg = ConfigFactory.load()
    val testCfg = originalCfg.getConfig("testDB").withFallback(originalCfg)
    Lobby.initSettings(Some(testCfg))
    settings = Lobby.settings
    log.debug("testWithoutDB = {}", Lobby.settings.testWithoutDB)
    super.beforeAll()
    dbName = if (settings.testingMode)
      settings.testing_exp_dbName
    else
      settings.production_exp_dbName
  }


  override protected def afterAll() {
    super.afterAll()
  }

  "salat" should "save simple enums" in {
    val coll = MongoConnection()(dbName)("test_coll")
    val id = UUID.randomUUID.toString

    val x = PartTest(id, "Part1", "testString")

    val x_dbo = grater[PartTest].asDBObject(x)
    coll += x_dbo
    val x_ = coll.findOneByID(id).get
    val x_* = grater[PartTest].asObject(x_)
    assert(x == x_*)
  }
  it should "save and retrieve just PartStep" in {
    val x = PartStepData("MarkI", 120, "MarkII", 123, 44, "", false)
    val coll = MongoConnection()(dbName)("gameLogic_coll")

    val x_dbo = grater[PartStepData].asDBObject(x)
    coll += x_dbo
    val x_ = coll.findOne(x_dbo).get
    val x_* = grater[PartStepData].asObject(x_)
    assert(x == x_*)
  }
  it should "retrieve simple enumTest object" in {
    val coll = MongoConnection()(dbName)("gameLogic_coll")

    //    val rnd1_arBuf = Map(
    //      // Add the starting points for every PlayerA.
    //      Part1 -> ArrayBuffer[PartStep]( PartStep(MarkI, 270, MarkII, (270 + amtOfChange(Part1)), 45, "", false) ),
    //      Part2 -> ArrayBuffer[PartStep]( PartStep(MarkI, 5.0, MarkII, (5.0 + amtOfChange(Part2)), 55, "", false) ),
    //      Part3 -> ArrayBuffer[PartStep]( PartStep(MarkI, 155, MarkII, (155 + amtOfChange(Part3)), 50, "", false)))
    val id = UUID.randomUUID.toString
    val x = EnumTest(id, Map("Part1" -> 1, "Part2" -> 3242))
    //val x = GLTest(id, GameLogicCreator.arBufToArray(rnd1_arBuf) )

    val x_dbo = grater[EnumTest].asDBObject(x)
    coll += x_dbo
    val x_ = coll.findOne(x_dbo).get
    val x_* = grater[EnumTest].asObject(x_)

    assert(x_dbo === x_)

    System.out.println(x)
    System.out.println(x_*)

    assert(x.toString === x_*.toString)
  }
  it should "retrieve simple enumTest2 object with List" in {
    val coll = MongoConnection()(dbName)("gameLogic_coll")

    //    val rnd1_arBuf = Map(
    //      // Add the starting points for every PlayerA.
    //      Part1 -> ArrayBuffer[PartStep]( PartStep(MarkI, 270, MarkII, (270 + amtOfChange(Part1)), 45, "", false) ),
    //      Part2 -> ArrayBuffer[PartStep]( PartStep(MarkI, 5.0, MarkII, (5.0 + amtOfChange(Part2)), 55, "", false) ),
    //      Part3 -> ArrayBuffer[PartStep]( PartStep(MarkI, 155, MarkII, (155 + amtOfChange(Part3)), 50, "", false)))
    val id = UUID.randomUUID.toString
    val x = EnumTest2(id, List(1, 2, 3), List(3, 2, 1))

    val x_dbo: MongoDBObject = grater[EnumTest2].asDBObject(x)
    coll += x_dbo
    val x_ = coll.findOneByID(id).get
    val x2 = grater[EnumTest2].asObject(x_)

    assert(x_dbo.toString === x_.toString)

    System.out.println(x)
    System.out.println(x2)
    System.out.println(x2.getClass)
    x2.productIterator.map(println)

    assert(x.toString === x2.toString)
  }
  it should "retrieve embedded caseclasses" in {
    val coll = MongoConnection()(dbName)("gameLogic_coll")
    val id = UUID.randomUUID.toString
    val id2 = UUID.randomUUID.toString
    val id3 = UUID.randomUUID.toString
    val x = EnumTest2(id, List(1, 2, 3), List(3, 2, 1))
    val x2 = EnumTest2(id2, List(4, 5, 6), List(6, 5, 4))
    val x3 = EnumTestEmbed(id3, x, x2)
    val x3_dbo = grater[EnumTestEmbed].asDBObject(x3)
    coll += x3_dbo
    val x3_ = coll.findOneByID(id3).get
    val x3_* = grater[EnumTestEmbed].asObject(x3_)

    println(x3)
    println(x3_dbo)
    println(x3_)
    println(x3_*)
    assert(x3_dbo === x3_)
    assert(x3_* === x3)
  }
  it should "retrieve a simple GameLogicTree with Enums" in {

    val coll = MongoConnection()(dbName)("gameLogic_coll")
    val id = UUID.randomUUID.toString
    // build the tree
    val ps1 = ThisPartsSteps(Seq(PartStepData("MarkI", 270, "MarkII", (270 + settings.amtOfChangePart1), 45, "", false),
      PartStepData("MarkII", 275, "MarkIII", (270 + settings.amtOfChangePart1), 45, "", false)))
    val ps2 = ThisPartsSteps(Seq(PartStepData("MarkIII", 270, "MarkIV", (270 + settings.amtOfChangePart1), 45, "", false),
      PartStepData("MarkIV", 275, "MarkV", (270 + settings.amtOfChangePart1), 45, "", false)))
    val rp = ThisRoundsParts(Map("Part1" -> ps1, "Part2" -> ps2), GoalsData(0, 0, 0), GoalsData(0,0,0), Map.empty[String,Int])
    val glt = GameLogicDataCC(id, 1, Map(Round(0) -> rp))
    println("ThisPartsSteps 1:  " + ps1)
    println("ThisPartsSteps 2:  " + ps2)
    println("RoundParts:   " + rp)
    println("GameLogicTree:  " + glt)

    val glt_dbo = grater[GameLogicDataCC].asDBObject(glt)
    coll += glt_dbo
    val glt_ = coll.findOneByID(id).get
    val g2 = grater[GameLogicDataCC].asObject(glt_)

    assert(glt_.toString === glt_dbo.toString)
    println("g2:     " + g2)
    println("glt:     " + glt)

    val glt_2 = g2.copy()
    println("g2:     " + g2)
    println("glt:     " + glt)
    println("glt_2:   " + glt_2)

    assert(g2.toString === glt.toString)
    assert(g2._id === glt._id)

    assert(g2.round(Round(0)) === glt.round(Round(0)))
    val g2round = g2.round(Round(0))
    val gltround = glt.round(Round(0))
    val g2ps1 = g2round.part("Part1")
    val gltps1 = gltround.part("Part1")

    assert(g2ps1.steps(0).curName === gltps1.steps(0).curName)
    assert(g2ps1.steps(0).curName === "MarkI")
    assert(g2.round === glt.round)
    assert(g2 === glt)
  }
  it should "not retrieve a nested List inside a Map" in {
    val coll = MongoConnection()(dbName)("gameLogic_coll")

    //    val rnd1_arBuf = Map(
    //      // Add the starting points for every PlayerA.
    //      Part1 -> ArrayBuffer[PartStep]( PartStep(MarkI, 270, MarkII, (270 + amtChange(Part1)), 45, "", false) ),
    //      Part2 -> ArrayBuffer[PartStep]( PartStep(MarkI, 5.0, MarkII, (5.0 + amtChange(Part2)), 55, "", false) ),
    //      Part3 -> ArrayBuffer[PartStep]( PartStep(MarkI, 155, MarkII, (155 + amtChange(Part3)), 50, "", false)))
    val id = UUID.randomUUID.toString
    val x = EnumTest3(id, Map("Part1" -> List(1, 2, 3), "Part2" -> List(3, 2, 1)))
    //val x = GLTest(id, GameLogicCreator.arBufToArray(rnd1_arBuf) )

    val x_dbo: MongoDBObject = grater[EnumTest3].asDBObject(x)
    coll += x_dbo
    val x_ = coll.findOne(x_dbo).get
    val x2 = grater[EnumTest3].asObject(x_)

    assert(x_dbo.toString === x_.toString)

    println(x)
    println(x2)
    println(x2.getClass)
    println(x_)
    val y = x_.as[BasicDBObject]("theMap")
    println(y)
    val y4 = y.as[MongoDBList]("Part1")
    println(y4)
    //val y5 = y4.toList
    //println(y5)
    //    val y5 = y4.toList
    //    println(y5)
    //    x2.theMap("Part1").toList.map(println)

    x should not equal x2
  }
  it should "Create new gamelogic, save it, retrive it." in {
    val r_uuid = UUID.randomUUID().toString
    Lobby.initSettings()
    val logic = GameLogicData.getSpecificLogicData(r_uuid, 1)
    GameLogicData.GameLogicDAO.insert(logic)
    val retrieved = GameLogicData.getSpecificLogicData(r_uuid, 1)
    assert(logic === retrieved)
  }
  "GameLogic is verified to" should "give us the gameStart partSteps" in {

    val testManipulation = 1
    val data = GameLogicData.getData(testManipulation)
    val t = data.round(Round(0))

    t.part("Part1").steps(0) match {
        case PartStepData("MarkI", 270, "MarkII", 271, _, "", _) =>
        case other: PartStepData => fail("Part1 did not match: " + other)
      }
    assert (t.part("Part2").steps(0) match {
      case PartStepData("MarkI", 50, "MarkII", 48, _, "", _) => true
      case other: PartStepData => fail("Part2 did not match: " + other)
    })
    assert (t.part("Part3").steps(0) match {
      case PartStepData("MarkI", 155, "MarkII", 156, _, "", _) => true
      case other: PartStepData => fail("Part3 did not match: " + other)
    })

    val beWithinLimitsP1 = be >= 271 and be <= 274
    val beWithinLimitsP2 = be <= 48 and be >= 42
    val beWithinLimitsP3 = be >= 156 and be <= 159
    t.aGoals.p1DataGoal should beWithinLimitsP1
    t.aGoals.p2DataGoal should beWithinLimitsP2
    t.aGoals.p3DataGoal should beWithinLimitsP3
  }

  it should "disable Part2 Button when reached its goal" in {

  }
  it should "give us the enough days to work if A and B upgraded only Part1 after meeting goals" in {
    (pending)
  }
  it should "give us the enough days to work if A and B upgraded only Part2 after meeting goals" in {
    (pending)
  }
  it should "give us the enough days to work if A and B upgraded only Part3 after meeting goals" in {
    (pending)
  }
  it should "allow A to work their minimum number of days and move them to extraWorkDays state" in  {
    (pending)
  }
  it should "allow A to work all their extra days in Part1" in {
    (pending)
  }
  it should "allow A to work all their extra days in Part2" in {
    (pending)
  }
  it should "allow A to work all their extra days in Part3" in {
    (pending)
  }
  it should "allow A to work all their extra days in their personal proj" in {
    (pending)
  }
  it should "allow A to send project to B after"

}

/**
 * Strign is PartNum (Part1, ...)
 */
case class EnumTest(_id: String, theMap: Map[String, Int])
case class EnumTest2(_id: String, Part1: List[Int], Part2: List[Int])
case class EnumTestEmbed(_id: String, case1: EnumTest2, case2: EnumTest2)
case class EnumTest3(_id: String, theMap: Map[String, List[Int]])
case class GLTest(_id: String, // unique identifier for this particular game logic sequence of partSteps
                  rnd1: Map[String, Seq[PartStepData]])
/**
 * String is PartNum (Part1, ...)
 */
case class PartTest(_id: String, cur: String, test: String)
