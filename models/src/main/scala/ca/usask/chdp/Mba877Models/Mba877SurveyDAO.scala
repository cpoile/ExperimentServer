package ca.usask.chdp.Mba877Models

import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpSettings
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import org.jasypt.util.password.BasicPasswordEncryptor
import java.util.UUID
import ca.usask.chdp.ExpServerCore.Models.Model

object Mba877SurveyDAO {
  val log = LoggerFactory.getLogger(this.getClass)

  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_mba877_dbName
  else
    ExpSettings.get.production_mba877_dbName
  lazy val conn = MongoConnection()
  lazy val coll = conn(dbName)("surveyInfo_coll")

  /**
   * Returns List[MbaSurveyInfo]
   */
  def findAll: List[MbaSurveyInfo] = {
    coll.find().toList.map(grater[MbaSurveyInfo].asObject(_))
  }
  def findName(name: String): Option[MbaSurveyInfo] = {
  coll.findOne(MongoDBObject("surveyName" -> name)).map(grater[MbaSurveyInfo].asObject(_))
  }

  def insert(survey: MbaSurveyInfo): Option[MbaSurveyInfo] = {
    coll += grater[MbaSurveyInfo].asDBObject(survey)
    val q = MongoDBObject("surveyName" -> survey.surveyName)
    coll.findOne(q).map(grater[MbaSurveyInfo].asObject(_))
  }
  def remove(survey: MbaSurveyInfo): Boolean = {
    val q = MongoDBObject("surveyName" -> survey.surveyName)
    coll.findOne(q).foreach(coll -= _)
    coll.getLastError().ok()
  }
  /**
   * Returns (valid, original bean)
   * */
  def validate(survey: MbaSurveyInfo): (Boolean, MbaSurveyInfo) = {
    // make sure shortName is the Id.
    (true, survey.copy(_id = survey.surveyName))
  }

  def closeConnection() {
    conn.close()
  }
}


// find by name?
//  def findByGlobalId(globalId: String): Option[Mba877PartBean] = {
//    val q = MongoDBObject("globalId" -> globalId)
//    coll.findOne(q).map(grater[Mba877PartBean].asObject(_))
//  }

