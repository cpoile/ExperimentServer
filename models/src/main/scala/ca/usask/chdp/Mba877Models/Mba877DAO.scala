package ca.usask.chdp.Mba877Models

import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpSettings
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import org.jasypt.util.password.BasicPasswordEncryptor
import java.util.UUID
import ca.usask.chdp.ExpServerCore.Models.Model

object Mba877DAO {
  val log = LoggerFactory.getLogger(this.getClass)

  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_mba877_dbName
  else
    ExpSettings.get.production_mba877_dbName
  lazy val conn = MongoConnection()
  lazy val coll = conn(dbName)("participant_coll")
  lazy val pwdEncryp = new BasicPasswordEncryptor()

  /**
   * Returns List[Participant]
   */
  def findAll: List[Mba877PartBean] = {
    coll.find().toList.map(grater[Mba877PartBean].asObject(_))
  }

  /**
   * Finds a participant by Id, or returns None.
   */
  def findByEmail(emailAnd_Id: String): Option[Mba877PartBean] = {
    val q = MongoDBObject("_id" -> emailAnd_Id)
    coll.findOne(q).map(grater[Mba877PartBean].asObject(_))
  }
  def findByGlobalId(globalId: String): Option[Mba877PartBean] = {
    val q = MongoDBObject("globalId" -> globalId)
    coll.findOne(q).map(grater[Mba877PartBean].asObject(_))
  }
  /**
   * Does this email already exist in the system?
   */
  def isDefined(email: String): Boolean = {
    val q = MongoDBObject("_id" -> email)
    coll.findOne(q).isDefined
  }

  /**
   * Inserts new user. Assumes you have checked to make sure there isn't a user with that email already.
   * Will encrypt password.
   * Returns the Mba877PartBean (possibly transformed if they were entered into a slot)
   */
  def insertNewUser(email: String,
                    pwd: String,
                    nsid: String): Option[Mba877PartBean] = {
    def encPwd: String = pwdEncryp.encryptPassword(pwd)
    val part = Mba877PartBean(email, email, Model.counter("participant"), encPwd, nsid)
    insertMba877PartBean(part)
  }
  def insertMba877PartBean(bean: Mba877PartBean): Option[Mba877PartBean] = {
    coll += grater[Mba877PartBean].asDBObject(bean)
    /**
     * now check that insert worked.
     */
    val ret = findByEmail(bean._id)
    if (ret.isEmpty || ret.get != bean) {
      log.error(
        "The object read from DB -- {} -- is not same as the one just entered: -- {} --.",
        ret,
        bean)
      None
    } else ret
  }

  def remove(part: Mba877PartBean): Boolean = {
    val q = MongoDBObject("_id" -> part._id)
    val res = coll.find(q).toList
    if (res.length > 0) {
      coll -= res(0)
    } else {
      false
    }
    coll.getLastError().ok()
  }
  /**
   * Returns (valid, newBean if the bean's id field had to be changed.)
   */
  def validate(part: Mba877PartBean): (Boolean, Mba877PartBean) = {
    if (part.email != "") {
      // encrypt new password, if there is any.
      if (part.changePwd == true) {
        part.pwd = pwdEncryp.encryptPassword(part.pwd)
        part.changePwd = false
      }
      // give them their globally unique playerId
      Model.counter("participant")
      (true, part.copy(_id = part.email))
    } else
      (false, part)
  }

  def isLoginValid(email: String, pwd: String): Boolean = {
    val encPwd = getPwdFor(email)
    if (encPwd.isDefined)
      pwdEncryp.checkPassword(pwd, encPwd.get)
    else
      false
  }
  def getPwdFor(email: String): Option[String] = {
    val query = MongoDBObject("email" -> email)
    coll.findOne(query).flatMap(_.getAs[String]("pwd"))
  }
  /**
   * Assigns a unique UUID to a leaving user and persist that bean in a temporary collection.
   * Returns new partBean with UUID field set.
   */
  def registerLeavingUser(globalId: String, destSurvey: String): String = {
    val uuid = UUID.randomUUID().toString
    val transPartBean = findByGlobalId(globalId).map( b =>
      b.copy(returningUuid = uuid, destSurveyName = destSurvey,
        UUIDtoSurvey = b.UUIDtoSurvey + (uuid -> destSurvey)))
    if (transPartBean.isEmpty) {
      log.error(
        "***** Serious problem. User was registering to leave for a survey, but they have no record in DB. " +
          "user globalId: {}, destSurvey: {}",
        globalId,
        destSurvey)
    } else {
      insertMba877PartBean(transPartBean.get)
    }
    uuid
  }
  def isReturningUser(uuid: String): Option[Mba877PartBean] = {
    val q = MongoDBObject("returningUuid" -> uuid)
    // if we find them, remove their returningUUID.
    val retUser = coll.findOne(q).map(grater[Mba877PartBean].asObject(_).copy(returningUuid = ""))
    retUser foreach (insertMba877PartBean(_))
    retUser
  }

  def closeConnection() {
    conn.close()
  }
}
