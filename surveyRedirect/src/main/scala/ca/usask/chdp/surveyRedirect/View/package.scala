package ca.usask.chdp.surveyRedirect

import javax.servlet.http.Cookie

package object View {
   trait SurveyPageName
  case object Intro extends SurveyPageName
  // FIXME: should not have to repass email string around.
  case class SurveyMainPage(curEmail: String) extends SurveyPageName
  case object Survey1 extends SurveyPageName

  def makeCookie(name: String, value: String, expiry: Int, path: String): Cookie = {
    val c = new Cookie(name, value)
    c.setMaxAge(expiry)
    c.setPath(path)
    c
  }
}
