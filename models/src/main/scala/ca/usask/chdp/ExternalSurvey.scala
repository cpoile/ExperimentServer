package ca.usask.chdp

import ca.usask.chdp.Mba877Models.Mba877DAO
import com.vaadin.server.{Page, VaadinServletResponse, VaadinService}
import ca.usask.chdp.models.EscapeUtils
import javax.servlet.http.Cookie

/**
 * Functions to help to send user to FluidSurveys
 */
object ExternalSurvey {
  def makeCookie(name: String, value: String, expiry: Int, path: String): Cookie = {
    val c = new Cookie(name, value)
    c.setMaxAge(expiry)
    c.setPath(path)
    c
  }

  def sendTo(surveyName: String, surveyLoc: String, globalId: String, curPage: Page) {
    // get a returning UUID and send them off to the survey.
    val uuid = Mba877DAO.registerLeavingUser(globalId, surveyName)
    val loc = surveyLoc + "?uuid=" + uuid + "&origin="
    VaadinService.getCurrentResponse.asInstanceOf[VaadinServletResponse]
      .addCookie(makeCookie("retUUID", uuid, 3*60*60, "/"))
    // And we have to add the Vaadin server's address to the get parameter, so we can return here after.
    val encodedParams = EscapeUtils.encodeURIComponent(curPage.getLocation.toString.split('#')(0).split('?')(0))
    Page.getCurrent.setLocation(loc + encodedParams)
  }
}

