package ca.usask.chdp.models

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import ca.usask.chdp.ExpSettings
import scalaz._
import Scalaz._
import org.slf4j.LoggerFactory

object ExperimentDAO extends DAOTrait[ExperimentBean] {
  val log = LoggerFactory.getLogger("ExperimentDAO")

  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_signUp_dbName
  else
    ExpSettings.get.production_signUp_dbName
  lazy val conn = MongoConnection()
  lazy val coll = conn(dbName)("experiments_coll")

  /**
   * Returns List[CourseBean]
   */
  @Override
  def findAll: List[ExperimentBean] = {
    coll.find().toList.map(grater[ExperimentBean].asObject(_))
  }

  /**
   * Returns Boolean if insert was successful.
   * CAUTION -- WILL OVERWRITE.
   */
  @Override
  def insertUpdate(exp: ExperimentBean): ExperimentBean = {
    // check if this slot exists already, if so, update it.
    coll += grater[ExperimentBean].asDBObject(exp)
    val q = MongoDBObject("_id" -> exp._id)
    val ret = coll.findOne(q).map(e => grater[ExperimentBean].asObject(e)) | ExperimentBean("")
    if (ret != exp)
      log.error("The object read from DB -- {} -- is not same as the one just enetered: -- {} --.",
        ret, exp)
    ret
  }
  @Override
  def remove(exp: ExperimentBean): Boolean = {
    val q = MongoDBObject("_id" -> exp._id)
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
  def validate(exp: ExperimentBean): (Boolean, ExperimentBean) = {
    if (exp.experimentId != "")
      (true, exp.copy(_id = exp.experimentId))
    else
      (false, exp)
  }

  def closeConnection() {
    conn.close()
  }
}
