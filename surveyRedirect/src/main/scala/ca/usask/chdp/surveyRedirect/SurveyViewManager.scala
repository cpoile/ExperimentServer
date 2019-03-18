package ca.usask.chdp.surveyRedirect

import com.vaadin.ui.{UI, CssLayout, CustomComponent}
import View._
import ca.usask.chdp.models.{EscapeUtils, ParticipantDAO}
import ca.usask.chdp.ExpSettings
import com.vaadin.server.{Page, VaadinService, VaadinServletResponse}

class SurveyViewManager extends CustomComponent {

  val layout = new CssLayout()
  setCompositionRoot(layout)
  layout.setSizeUndefined()

  var curEmail: String = ""

  def setPageTo(page: SurveyPageName) {
    page match {
      case Intro => {
        layout.removeAllComponents()
        layout.addComponent(new SurveyIntroView(this))
      }
        // TODO: should not pass email in with the page command.
      case SurveyMainPage(email) => {
        curEmail = email
        layout.removeAllComponents()
        layout.addComponent(new SurveyMainPageView(this, curEmail))
      }
        // TODO: this should be calling the Models external survey fn
      case Survey1 => {
        // get a returning UUID and send them off to the survey.
        val uuid = ParticipantDAO.registerLeavingUser(curEmail, "survey1")
        val loc = ExpSettings.get.preSessionSurvey1Location_w2014comm105 + "?uuid=" + uuid
        VaadinService.getCurrentResponse.asInstanceOf[VaadinServletResponse]
          .addCookie(makeCookie("retUUID", uuid, 3*60*60, "/"))
        // And we have to add the Vaadin server's address to the get parameter, so we can return here after.
        val curloc = Page.getCurrent.getLocation
        val encodedParams = "&origin=" + EscapeUtils.encodeURIComponent(curloc.getHost + curloc.getPath)
        Page.getCurrent.setLocation(loc + encodedParams)
        VaadinService.getCurrent.closeSession(UI.getCurrent.getSession)
      }
    }
  }
}


