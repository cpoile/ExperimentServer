package ca.usask.chdp.template
import javax.servlet.http.Cookie

package object View {
  object ThePageName extends Enumeration {
    val Intro, SignUp = Value
  }

  def makeCookie(name: String, value: String, expiry: Int, path: String): Cookie = {
    val c = new Cookie(name, value)
    c.setMaxAge(expiry)
    c.setPath(path)
    c
  }
}
