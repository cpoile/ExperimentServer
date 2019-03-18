package ca.usask.chdp.survey.View

import ca.usask.chdp.ExpSettings
import ca.usask.chdp.models.ParticipantDAO
import ca.usask.chdp.survey.SurveyViewManager
import com.vaadin.event.ShortcutAction
import com.vaadin.server.Page
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui._

class SurveyIntroView(viewMgr: SurveyViewManager) extends CustomComponent {
  val layout = new CustomLayout("surveyIntroLayout")
  setCompositionRoot(layout)

  /**
   * returning user signUp
   * --------------------------------------------------------------------
   */
  val existingEmail = new TextField()
  existingEmail.setSizeUndefined()
  existingEmail.setInputPrompt("Email")
  existingEmail.setImmediate(true)
  layout.addComponent(existingEmail, "existingEmail")

  val existingPassword = new PasswordField()
  existingPassword.setSizeUndefined()
  layout.addComponent(existingPassword, "existingPassword")

  val existingSubmit = new Button("Submit", new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (existingEmail.getValue != null && existingPassword.getValue != null) {
        if (ParticipantDAO.findByEmail(existingEmail.getValue).isEmpty) {
          Notification.show("This Email is not registered.", Notification.Type.WARNING_MESSAGE)
        } else if (ParticipantDAO.isLoginValid(existingEmail.getValue, existingPassword.getValue)) {
          Notification.show("Welcome back.", Notification.Type.TRAY_NOTIFICATION)
          event.getButton.setEnabled(false)
          viewMgr.setPageTo(SurveyMainPage(existingEmail.getValue))
        } else {
          Notification.show("Incorrect Login.", Notification.Type.WARNING_MESSAGE)
        }
      }
    }
  })

  existingSubmit.setSizeUndefined()
  existingSubmit.addStyleName("btn btn-primary")
  existingSubmit.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  layout.addComponent(existingSubmit, "existingSubmit")

  val goToRegistration = new Button("Registration", new ClickListener {
    def buttonClick(event: ClickEvent) {
      //        val uuid = Lobby.registerLeavingUser(ReturningUserInfo(myPlayerActorInfo, FinishedSurvey))
      //
      //        VaadinService.getCurrentResponse.asInstanceOf[VaadinServletResponse]
      //          .addCookie(makeCookie("retUUID", retUUID, 60*60, "/"))
      //        // And we have to add the Vaadin server's address to the get parameter, so we can return here after.
      //        getUI.getPage.setLocation(surveyLoc + "&origin=" + Page.getCurrent.getLocation.toString.split("#")(0))
      //
      //        Page.getCurrent.setLocation("")


      Notification.show("send them to the signup page.")
      val page = Page.getCurrent
      val loc = page.getLocation.toString.split("/").flatMap(_.split(":")).flatMap(_.split("#")).toList
      val curHost = loc match {
        case "http" :: "" :: host :: tail => host
        case host :: tail => host
        case Nil => "chp3.usask.ca"
      }
      val signupUrl = if (ExpSettings.get.testingMode) {
        "http://" + curHost + ":" + ExpSettings.get.testing_htmlPort_signup + "/signup/"
      } else
        "http://" + curHost + "/signup/"
      page.setLocation(signupUrl)
    }
  })
  goToRegistration.setSizeUndefined()
  goToRegistration.addStyleName("btn btn-primary")
  layout.addComponent(goToRegistration, "goToRegistration")

  println("testing: " + ExpSettings.get.testingMode + "; skipSurvey: " + ExpSettings.get.testing_skipSurveyLogin)
  if (ExpSettings.get.testingMode && ExpSettings.get.testing_skipSurveyLogin) {
    new Thread {
      override def run() {
        Thread.sleep(2000)
        viewMgr.setPageTo(SurveyMainPage("test@test.ca"))
      }
    }.start()
  }
}
