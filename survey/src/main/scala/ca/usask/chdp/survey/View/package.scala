package ca.usask.chdp.survey

import javax.servlet.http.Cookie
import collection.JavaConverters._
import ca.usask.chdp.models.{RowDouble, RowInt, QBean}

package object View {
  implicit def ListInteger2ScalaList(x: java.util.List[java.lang.Integer]): List[Int] = {
    x.asScala.map(Integer2int).toList
  }
  implicit def ListDouble2ScalaList(x: java.util.List[java.lang.Double]): List[Double] = {
    x.asScala.map(Double2double).toList
  }
  implicit def ListString2ScalaList(x: java.util.List[java.lang.String]): List[String] = {
    x.asScala.toList
  }

  implicit def traversableInt2RowInt(orig: TraversableOnce[Int]): RowInt = RowInt(orig.toList)
  implicit def traversableDouble2RowDouble(orig: TraversableOnce[Double]): RowDouble = RowDouble(orig.toList)

  trait SurveyPageName
  case object Intro extends SurveyPageName
  // fixme. should not have to repass email string around.
  case class SurveyMainPage(curEmail: String) extends SurveyPageName
  case object Survey1 extends SurveyPageName
  case object Survey2Part1 extends SurveyPageName
  case class Survey2Part2(listAnswers: Seq[QBean]) extends SurveyPageName
  case object Survey3 extends SurveyPageName

  def makeCookie(name: String, value: String, expiry: Int, path: String): Cookie = {
    val c = new Cookie(name, value)
    c.setMaxAge(expiry)
    c.setPath(path)
    c
  }
}
