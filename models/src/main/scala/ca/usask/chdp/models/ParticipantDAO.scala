package ca.usask.chdp.models

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import ca.usask.chdp.ExpSettings
import org.jasypt.util.password.BasicPasswordEncryptor
import scalaz._
import Scalaz._
import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpServerCore.Models.Model
import java.util.UUID

object ParticipantDAO extends DAOTrait[ParticipantBean] {
  val log = LoggerFactory.getLogger("ParticipantDAO")

  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_signUp_dbName
  else
    ExpSettings.get.production_signUp_dbName
  lazy val conn = MongoConnection()
  lazy val coll = conn(dbName)("participant_coll")
  lazy val pwdEncryp = new BasicPasswordEncryptor()

  /**
   * Returns List[Participant]
   */
  @Override
  def findAll: List[ParticipantBean] = {
    var list: List[ParticipantBean] = coll.find().toList.map(grater[ParticipantBean].asObject(_))
    list = list.map(bean => bean.copy(courseInfo = CourseDAO.find(bean.courseId).toString))
    list.map(bean => bean.copy(signUpSlotInfo = SignUpSlotDAO.find(bean.signUpSlotId).toString))
  }

  /**
   * Finds a participant by Id, or returns None.
   */
  def findByEmail(emailAnd_Id: String): Option[ParticipantBean] = {
    val q = MongoDBObject("_id" -> emailAnd_Id)
    coll.findOne(q).map(grater[ParticipantBean].asObject(_))
  }
  def findByGlobalId(globalId: String): Option[ParticipantBean] = {
    val q = MongoDBObject("globalId" -> globalId)
    coll.findOne(q).map(grater[ParticipantBean].asObject(_))
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
   * Returns the participantBean (possibly transformed if they were entered into a slot)
   */
  def insertNewUser(email: String, pwd: String, nsid: String, courseId: String, courseInfo: String): Option[ParticipantBean] = {
    def encPwd: String = pwdEncryp.encryptPassword(pwd)
    val part = ParticipantBean(email, email, Model.counter("player"), encPwd, nsid, false, courseId, courseInfo)
    insertParticipantBean(part)
  }
  def insertParticipantBean(bean: ParticipantBean): Option[ParticipantBean] = {
    coll += grater[ParticipantBean].asDBObject(bean)
    /**
     * now check that insert worked.
     */
    val ret = findByEmail(bean._id)
    if (ret.isEmpty || ret.get != bean) {
      log.error("The object read from DB -- {} -- is not same as the one just entered: -- {} --.",
        ret, bean)
      None
    } else ret
  }

  /**
   * Return participant bean that was either:
   * * transformed and inserted because it had a new signUpSlot
   * * inserted as is because its signUpSlot was the same as before.
   * CAUTION -- WILL OVERWRITE.
   */

  /**
   * refactored... kind of. Will just insert participant into DB.
   * @param part
   * @return
   */
  @Override
  def insertUpdate(part: ParticipantBean): ParticipantBean = {

    // TODO: THis is undbelievably bad. Refactor.
    // check if the player's signUpSlot has changed. If so, add it through the SignUpSlotDAO.
    // Let it handle the atomic update.
    val newSlotId = part.signUpSlotId
    val oldSlotId = findByEmail(part._id).map(_.signUpSlotId)
    val posTransPart = if (oldSlotId.exists(_.length > 2) && newSlotId != oldSlotId.get) {
      log.debug("participant: {} -- adding them to slot: {} from their old slot: {}",
        Array(part._id, newSlotId, oldSlotId).asInstanceOf[Array[AnyRef]])
      val transPart = SignUpSlotDAO.removePartFromTheirCurSlotInDB(part)
      SignUpSlotDAO.scheduleAParticipant(newSlotId, transPart)
    } else {
      part
    }
    coll += grater[ParticipantBean].asDBObject(posTransPart)

    /**
     * now check that insert worked.
     */
    val ret = findByEmail(part._id) | ParticipantBean("")
    if (ret != posTransPart)
      log.error("The object read from DB -- {} -- is not same as the one just enetered: -- {} --.",
        ret, posTransPart)
    ret
  }

  @Override
  def remove(part: ParticipantBean): Boolean = {
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
  @Override
  def validate(part: ParticipantBean): (Boolean, ParticipantBean) = {
    if (part.email != "") {
      // encrypt new password, if there is any.
      if (part.changePwd == true) {
        part.pwd = pwdEncryp.encryptPassword(part.pwd)
        part.changePwd = false
      }
      // give them their globally unique playerId
      Model.counter("player")
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
  def registerLeavingUser(email: String, destSurvey: String): String = {
    val uuid = UUID.randomUUID().toString
    val transPartBean = findByEmail(email).map(x => x.copy(returningUuid = uuid,
      destSurveyName = destSurvey,
      extSurveyUuids = x.extSurveyUuids + (destSurvey -> uuid)))
    if (transPartBean.isEmpty) {
      log.error("***** Serious problem. User was registering to leave for a survey, but they have no record in DB. user email: {}, destSurvey: {}", email, destSurvey)
    } else {
      coll += grater[ParticipantBean].asDBObject(transPartBean.get)
    }
    uuid
  }
  def isReturningUser(uuid: String): Option[ParticipantBean] = {
    val q = MongoDBObject("returningUuid" -> uuid)
    // if we find them, remove their returningUUID.
    val retUser = coll.findOne(q).map(grater[ParticipantBean].asObject(_).copy(returningUuid = ""))
    retUser foreach (insertUpdate(_))
    retUser
  }

  def closeConnection() {
    conn.close()
  }
}