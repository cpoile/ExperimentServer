package ca.usask.chdp.survey

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
      case Survey1 => {
        layout.removeAllComponents()
        layout.addComponent(new Survey1View(this, curEmail))
      }
      case Survey2Part1 => {
        layout.removeAllComponents()
        layout.addComponent(new Survey2View(this, curEmail))
      }
     case Survey2Part2(answers) => {
        layout.removeAllComponents()
        layout.addComponent(new Survey2ViewPart2(this, curEmail, answers))
      }
      case Survey3 => {
        // get a returning UUID and send them off to the survey.
        val uuid = ParticipantDAO.registerLeavingUser(curEmail, "survey3")
        val loc = ExpSettings.get.preSessionSurvey3Location + "?uuid=" + uuid
        VaadinService.getCurrentResponse.asInstanceOf[VaadinServletResponse]
          .addCookie(makeCookie("retUUID", uuid, 3*60*60, "/"))
        // And we have to add the Vaadin server's address to the get parameter, so we can return here after.
        val encodedParams = EscapeUtils.encodeURIComponent("&origin=" + Page.getCurrent.getLocation.toString.split('#')(0).split('?')(0))
        Page.getCurrent.setLocation(loc + encodedParams)
        VaadinService.getCurrent.closeSession(UI.getCurrent.getSession)
      }
    }
  }
}


