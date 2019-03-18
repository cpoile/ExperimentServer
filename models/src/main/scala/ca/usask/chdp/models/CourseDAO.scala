package ca.usask.chdp.models

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import ca.usask.chdp.ExpSettings
import scalaz._
import Scalaz._
import org.slf4j.LoggerFactory

object CourseDAO extends DAOTrait[CourseBean] {
  val log = LoggerFactory.getLogger("CourseDAO")

  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_signUp_dbName
  else
    ExpSettings.get.production_signUp_dbName
  lazy val conn = MongoConnection()
  lazy val coll = conn(dbName)("courses_coll")

  /**
   * Returns List[CourseBean]
   */
  def findAll: List[CourseBean] = {
    coll.find().toList.map(grater[CourseBean].asObject(_))
  }
  def find(id: String): CourseBean = {
    val q = MongoDBObject("_id" -> id)
    val res = coll.find(q).toList.map(grater[CourseBean].asObject(_))
    if (res.length > 0) {
      res(0)
    } else {
      CourseBean("")
    }
  }

  /**
   * Returns inserted CourseBean if insert was successful. Returns blank CourseBean if not.
   * CAUTION -- WILL OVERWRITE.
   */
  @Override
  def insertUpdate(course: CourseBean): CourseBean = {
    coll += grater[CourseBean].asDBObject(course)
    val q = MongoDBObject("_id" -> course._id)
    val ret = coll.findOne(q).map(c => grater[CourseBean].asObject(c)) | CourseBean("")
    if (ret != course)
      log.error("The object read from DB -- {} -- is not same as the one just enetered: -- {} --.",
        ret, course)
    ret
  }

  @Override
  def remove(course: CourseBean): Boolean = {
    val q = MongoDBObject("_id" -> course._id)
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
  def validate(course: CourseBean): (Boolean, CourseBean) = {
    if (course.name != "" && course.section != "" && course.courseTime != ""
      && course.professor != "" && course.courseId != "")
      (true, course.copy(_id = course.courseId))
    else
      (false, course)
  }

  def closeConnection() {
    conn.close()
  }
}
