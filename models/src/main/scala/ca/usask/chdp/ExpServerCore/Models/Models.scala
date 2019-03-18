package ca.usask.chdp.ExpServerCore.Models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.novus.salat._
import com.novus.salat.global._
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import org.jasypt.util.password.BasicPasswordEncryptor
import reflect.BeanProperty
import java.util.concurrent.atomic.AtomicInteger
import scala.Some
import scala.collection._
import parallel.mutable.ParHashMap
import scalaz._
import Scalaz._
import ca.usask.chdp.ExpSettings
import ca.usask.chdp.models.Msgs.GameInfo


/**
 *
 * The Game Object's data structure.
 * A Game is a "Parent" as described in: https://github.com/novus/salat/wiki/ChildCollection
 * Each game has a collection of children
 *
 * given case class = CC with id = 5, ScalaDAO can:
 * Insert:   xDAO.insert(CC)      // returns Some(5)
 * Find:     xDAO.find( normal MongoDBOject query as in Casbah).sort(...).skip(1).limit(1).toList
 * xDAO.findOneByID(id = 5)  // returns Some(CC)
 * xDAO.ids(MongoDBObject("gr_id" -> MongoDBObject("$gt" -> 2)))  // returns List[Int](3,4,...)
 * Update:   val cr = xDAO.update(MongoDBObject("gr_id" -> 3), MongoDBObject("x" -> "changed"), false, false)
 * // or as a case class
 * val cr = xDAO.update(MongoDBObject("gr_id" -> 3), cc3.copy(x = "changed"), false, false, new WriteConcern)
 * Saving:   val cr = xDAO.save(cc3.copy(x = "changed"))
 * Removing: val cr = xDAO.remove(cc4)
 * Projections:  see https://github.com/novus/salat/wiki/SalatDAO
 *
 **/
object GameRecordDAO {
  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_exp_dbName
  else
    ExpSettings.get.production_exp_dbName

  lazy val coll: Option[MongoCollection] = Model.getMongoColl(dbName, "gameRecord_coll")
  lazy private val helpingBehMap = ParHashMap.empty[String, List[Int]]

  def insertUpdate(game: GameRecord) {
    coll foreach (_ += grater[GameRecord].asDBObject(game))
    if (ExpSettings.get.testingMode && ExpSettings.get.testing_useMemCache) {
      helpingBehMap += (game.gr_id -> game.helpfulBehsPerRound.toList.sortBy(_._1).map(_._2))
    } else {
      // test to make sure the game record was recorded correctly... or maybe that's not worth the trouble.
    }
  }
  def find(id: String): GameRecord = {
    val query = MongoDBObject("gr_id" -> id)
    coll.flatMap(_.findOne(query).map(grater[GameRecord].asObject(_))) | GameRecord("", "", "", 0, "", "", "", "", "", "")
  }
  /**
   * Returns a sorted list of helpful behaviors per round ((0) = round 1, etc.)
   * @param id GameRecord id (_id / gr_id)
   */
  def retrieveHelpfulBehs(id: String): List[Int] = {
    if (ExpSettings.get.testingMode && ExpSettings.get.testing_useMemCache) {
      helpingBehMap(id)
    } else {
      find(id).helpfulBehsPerRound.toList.sortBy(_._1).map(_._2)
    }
  }
  /**
   * Generate a GameInfo bean summarizing this game's record.
   * If we are sending this info when the game just moved to the next round and the gameRecord was saved at the end of
   * last round, we can manually tell the listener that specifyRound is the actual current round.
   */
  def makeGameInfo(id: String, specifyRound: String = ""): GameInfo = {
    val gr = find(id)
    val round = if (specifyRound == "") gr.round else specifyRound
    GameInfo(id, gr.expSessionID, gr.manipulation, gr.p1globalId, gr.p1Role,
        gr.p2globalId, gr.p2Role, round, summarizeMapOfXPerRound(gr.chatMsgsPerRoundFromA),
        summarizeMapOfXPerRound(gr.chatMsgsPerRoundFromB), summarizeMapOfXPerRound(gr.helpfulBehsPerRound))
    }
  def summarizeMapOfXPerRound(hb: Map[String, Int]): String = {
    hb.toList.sortBy(_._1).map(kv => kv._1.drop(3) + ":" + kv._2).mkString(", ")
  }
}
object MsgDAO {
  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_exp_dbName
  else
    ExpSettings.get.production_exp_dbName
  lazy val coll: Option[MongoCollection] = Model.getMongoColl(dbName, "msg_coll")

  def insert(msg: MsgData) {
    coll foreach (_ += grater[MsgData].asDBObject(msg))
  }
  //  def retrieve(gameParentID: String, from: String): Seq[(String, String, String)] = {
  /**
   * Returns: (round, From, Msg)
   */
  def retrieve(gameParentID: String, from: String): Seq[(String, String, String)] = {
    val query = $and("gameParentID" -> gameParentID, "from" -> from)
    val col: MongoCollection = coll match {
      case None => null
      case Some(c) => {
        c
      }
    }
    if (col != null) {
      val res = for (x <- col.find(query)) yield grater[MsgData].asObject(x)
      res.toArray.map(y => (y.round, y.from, y.msg))
    } else {
      Seq()
    }
  }
}

/**
 *
 * @param gr_id Please set to Models.counter("game")
 * @param expSessionID Unique database-wide Experiment Session ID
 */
case class GameRecord(_id: String,
                      @BeanProperty gr_id: String, // Unique experiment-wide ID
                      @BeanProperty expSessionID: String,
                      @BeanProperty manipulation: Int,
                      @BeanProperty p1globalId: String,
                      @BeanProperty p1Role: String,
                      p1EncryptedEmail: String,
                      @BeanProperty p2globalId: String,
                      @BeanProperty p2Role: String,
                      p2EncryptedEmail: String,
                      dateCreated: DateTime = DateTime.now,
                      @BeanProperty round: String = "Rnd0",
                      helpfulBehsPerRound: Map[String, Int] = Map.empty[String, Int],
                      chatMsgsPerRoundFromA: Map[String, Int] = Map.empty[String, Int],
                      chatMsgsPerRoundFromB: Map[String, Int] = Map.empty[String, Int],
                      totalTimeForAPerRound: Map[String, Long] = Map.empty[String, Long],
                      totalTimeForBPerRound: Map[String, Long] = Map.empty[String, Long],
                      persGoalsTimeForAPerRound: Map[String, Long] = Map.empty[String, Long],
                      extraWorkTimeForAPerRound: Map[String, Long] = Map.empty[String, Long],
                      overallStandingsPerTeamPerRound: Map[String, Map[String, Int]] = Map.empty[String, Map[String, Int]],
                      surveyResults: Map[String, Map[String, Int]] = Map.empty[String, Map[String, Int]])


/**
 * "child" case class to Parent "GameRecord"
 *
 * @param _id Please set to Models.counter("msg")
 */
case class MsgData(_id: String,
                   gameParentID: String,
                   from: String,
                   msg: String,
                   round: String,
                   senderRole: String,
                   d: DateTime = DateTime.now)

object Model {
  RegisterJodaTimeConversionHelpers()
  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_exp_dbName
  else
    ExpSettings.get.production_exp_dbName

  def getMongoColl(db: String, coll: String): Option[MongoCollection] = {
    if (ExpSettings.get.testWithoutDB)
      None
    else if (ExpSettings.get.testingMode && ExpSettings.get.testing_useSynchronousDB) {
      // set writeconcern to syncronous so that we can do integration tests where we need to write data
      // and read it back immediately.
      // MongoDBAddress("localhost"), MongoOptions(true, 1, 100)
      val conn = MongoConnection()(db)(coll)
      conn.setWriteConcern(WriteConcern.FsyncSafe)
      conn.some
    } else {
      Some(MongoConnection()(db)(coll))
    }
  }

  //  val simpleIdCount = new AtomicInteger(Random.nextInt())
  val simpleIdCount = new AtomicInteger(0)
  lazy val log = LoggerFactory.getLogger("ModelsObject")
  lazy val counterColl = getMongoColl(dbName, "counters")
  lazy val userColl = getMongoColl(dbName, "users")
  lazy val returningColl = getMongoColl(dbName, "returningUsers_coll")
  lazy val pwdEncryp = new BasicPasswordEncryptor()

  def counter(name: String): String = {
    if (counterColl.isDefined) {
      val coll = counterColl.get
      val query = MongoDBObject("gr_id" -> (name + "ID"))
      val inc = $inc("counter" -> 1)

      coll.findOne(query).getOrElse(coll.insert(query ++ MongoDBObject("counter" -> 1)))
      val curCount = coll.findOne(query).get.getAs[Int]("counter").get
      log.debug("counter is: {}", curCount)
      coll.update(query, inc)
      val newCount = coll.findOne(query).get.getAs[Int]("counter").get
      log.debug("counter is now: {}", newCount)

      val oldDoc = coll.findAndModify(query, inc).get
      val count = oldDoc.getAs[Int]("counter").getOrElse({
        log.error("*** Mongo somehow didn't give us a counter. Should not happen. Giving a random int.")
        (math.random * 1000000000).toInt
      })
      name + count
    } else {
      // just give a simple count, it won't be stored in the DB.
      val id = simpleIdCount.incrementAndGet()
      name + id
    }
  }
  def overrideAndEncryptUser(email: String, pwd: String): Boolean = {
    if (userColl.isDefined) {
      val query = MongoDBObject("email" -> email)
      val rec = userColl.get.findOne(query)
      if (rec.isDefined) {
        userColl.get += rec.get + ("pwd" -> pwdEncryp.encryptPassword(pwd))
        true
      } else
        false
    } else {
      false
    }
  }
  /**
   * Returns a globally unique ExpServer player ID.
   * Should have already checked if user exists. This will overwrite existing user.
   */
  def enterAndEncryptUser(email: String, pwd: String): String = {
    if (userColl.isDefined) {
      val query = MongoDBObject("email" -> email)
      val rec = userColl.get.findOne(query)
      val globalId = if (rec.isDefined)
        rec.get.getAs[String]("globalId").get
      else
        Model.counter("player")
      val encrypPwd = pwdEncryp.encryptPassword(pwd)
      userColl.get += query ++("pwd" -> encrypPwd, "globalId" -> globalId)
      globalId
    } else {
      Model.counter("player")
    }

  }
  def getPwdFor(email: String): Option[String] = {
    if (userColl.isDefined) {
      val query = MongoDBObject("email" -> email)
      val res = userColl.get.findOne(query)
      val dbObj = res | MongoDBObject.empty
      dbObj.getAs[String]("pwd")
    } else {
      None
    }
  }
  def getIdFor(email: String): Option[String] = {
    // old version, see how long it is:
    //    if (userColl.isDefined) {
    //      val query = MongoDBObject("email" -> email)
    //      val res = userColl.get.findOne(query)
    //      val dbObj = res | MongoDBObject.empty
    //      dbObj.getAs[String]("globalId")
    //    } else {
    //      None
    //    }
    val q = MongoDBObject("email" -> email)
    // because findOne returns an option, we don't need to wrap it's result in a Some,
    // which is what map does
    userColl.flatMap(_.findOne(q)).flatMap(_.getAs[String]("globalId"))
  }

  /**
   * Set a UUID for the user so that they can return without having to log back in.
   * If this is not called, and they return without a valid session, then
   * they must provide this UUID or they will have to log back in.
   */
  //  def getReturnUUID(globalId: String): String = {
  //    val res = userColl.findOne(MongoDBObject("globalId" -> globalId))
  //    if (res.isEmpty) {
  //      //log.error("userIsLeaving -- could not find record for globalId: {}. This shouldn't happen.", globalId)
  //      ""
  //    } else {
  //      val userRec = res.get
  //      val uuid = UUID.randomUUID().toString
  //      userColl += userRec + ("uuid" -> uuid)
  //      uuid
  //    }
  //  }

}
