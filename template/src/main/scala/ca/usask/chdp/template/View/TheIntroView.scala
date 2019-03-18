package ca.usask.chdp.template.View

import ca.usask.chdp.models.ParticipantDAO
import com.vaadin.event.FieldEvents.{FocusEvent, FocusListener}
import com.vaadin.event.ShortcutAction
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui._
import ca.usask.chdp.template.TheViewManager

class TheIntroView(viewMgr: TheViewManager) extends CustomComponent {
  val layout = new CustomLayout("introLayout")
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
  existingEmail.addFocusListener(new FocusListener {
    def focus(event: FocusEvent) {
      existingSubmit.setClickShortcut(ShortcutAction.KeyCode.ENTER)
    }
  })

  val existingPassword = new PasswordField()
  existingPassword.setSizeUndefined()
  layout.addComponent(existingPassword, "existingPassword")

  val existingSubmit = new Button("Submit", new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (existingEmail.getValue != null && existingPassword.getValue != null) {
        if (ParticipantDAO.find(existingEmail.getValue)._id == "") {
          Notification.show("This Email is not registered.", Notification.Type.WARNING_MESSAGE)
        } else if (ParticipantDAO.isLoginValid(existingEmail.getValue, existingPassword.getValue)) {
          Notification.show("Welcome back.", Notification.Type.TRAY_NOTIFICATION)
          event.getButton.setEnabled(false)
          // go to the survey section. or reveal it.
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
      }
  })
  goToRegistration.setSizeUndefined()
  goToRegistration.addStyleName("btn btn-primary")
  //goToRegistration.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  layout.addComponent(goToRegistration, "goToRegistration")


}
