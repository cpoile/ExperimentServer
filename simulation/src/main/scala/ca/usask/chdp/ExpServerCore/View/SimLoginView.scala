package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.Button
import com.vaadin.event.ShortcutAction
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.LoggedIn
import com.vaadin.ui.Button.{ClickListener, ClickEvent}
import ca.usask.chdp.models.{ParticipantBean, ParticipantDAO}
import com.vaadin.annotations.JavaScript
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import scalaz._
import Scalaz._
import org.slf4j.LoggerFactory

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class SimLoginView(viewMgr: ViewManager, location: Option[String]) extends CustomComponent {
  val log = LoggerFactory.getLogger("SimLoginView")

  val layout = if (Lobby.settings.serverDownAllowNewLogins)
    new CustomLayout("simLoginLayout2")
  else
    new CustomLayout("simLoginLayout")
  setCompositionRoot(layout)

  val emailField = new TextField()
  emailField.setSizeUndefined()
  emailField.setInputPrompt("Email")
  emailField.setImmediate(true)
  emailField.focus()
  layout.addComponent(emailField, "emailField")

  val passwordField = new PasswordField()
  passwordField.setSizeUndefined()
  passwordField.setImmediate(true)
  layout.addComponent(passwordField, "passwordField")

  var loc: String = ""
  var locationSet: Boolean = false
  if (Lobby.settings.diffLocationsMatter) {
    log.debug("diffLocationsMatter evaled to true")
    if (!Lobby.settings.allLocations.contains(location | "")) {
      loc = "Error: location should have been specified (url/#location=x)"
      locationSet = false
    } else {
      loc = location | ""
      locationSet = true
    }
    layout.addComponent(new Label(loc), "locationLabel")
  }

  /**
   * If server down
   */
  val passwordField2 = new PasswordField()
  val nsidField = new TextField()
  if (Lobby.settings.serverDownAllowNewLogins) {
    passwordField.setSizeUndefined()
    passwordField.setImmediate(true)
    layout.addComponent(passwordField2, "passwordField2")

    passwordField.setSizeUndefined()
    passwordField.setImmediate(true)
    layout.addComponent(nsidField, "nsidField")
  }


  def areServerDownFieldsVerified: Boolean = {
    if (passwordField2.getValue != null
      && passwordField.getValue == passwordField2.getValue
      && nsidField.getValue != null
      && isNsidValid(nsidField.getValue)) {
      true
    } else {
      if (passwordField2.getValue == null || passwordField.getValue != passwordField2.getValue)
        Notification.show("Password fields must match.", Notification.Type.WARNING_MESSAGE)
      else if (nsidField.getValue == null || !isNsidValid(nsidField.getValue))
        Notification.show("NSID must be entered.", Notification.Type.WARNING_MESSAGE)
      false
    }
  }
  def isNsidValid(nsid: String): Boolean = {
    (nsid.length == 6 &&
      nsid.substring(0, 3).forall(p => p.isLetter) &&
      nsid.substring(3).forall(p => p.isDigit))
  }

  val submitLogin = new Button("Login", new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (Lobby.settings.diffLocationsMatter && !locationSet) {
        Notification.show("Your experiment location is not set.\nPlease notify your experiment facilitator.", Notification.Type.WARNING_MESSAGE)
      } else if (emailField.getValue != null && passwordField.getValue != null) {
        val partBean = ParticipantDAO.findByEmail(emailField.getValue).getOrElse(ParticipantBean(""))
        if (partBean._id == "") {
          // if we are testing, insert this user and then reclick.
          if (Lobby.settings.testingMode && Lobby.settings.testing_loginViewAutoRegister) {
            ParticipantDAO.insertNewUser(emailField.getValue, passwordField.getValue, "", "", "")
            Notification.show("Good. Username registered. Press Login Again.", Notification.Type.WARNING_MESSAGE)
          }
          else if (Lobby.settings.serverDownAllowNewLogins) {
            if(areServerDownFieldsVerified )
            {
              ParticipantDAO.insertNewUser(emailField.getValue,
                passwordField.getValue, nsidField.getValue, "", "")
              Notification.show("Username registered. Press Login Again.", Notification.Type.WARNING_MESSAGE)
            }
           }
          else {
            Notification.show("This Email is not registered.", Notification.Type.WARNING_MESSAGE)
          }
        } else if (partBean.finishedGame) {
          Notification.show("You have completed the simulation already. Thank you!", Notification.Type.ERROR_MESSAGE)
        } else if (!Lobby.isLoginAllowed) {
          Notification.show("The simulation server is not ready yet, please wait.",
            Notification.Type.WARNING_MESSAGE)
        } else if (ParticipantDAO.isLoginValid(emailField.getValue, passwordField.getValue)) {
          Notification.show("Welcome back.", Notification.Type.TRAY_NOTIFICATION)
          restoreColoredBackground()
          viewMgr.send(LoggedIn(emailField.getValue, passwordField.getValue,
            partBean.globalId, location | ""))
        } else {
          Notification.show("Incorrect Login. You need to enter the email and password you used " +
            "when you took the survey part of this experiment", Notification.Type.WARNING_MESSAGE)
        }
      }
    }
  })
  submitLogin.setSizeUndefined()
  submitLogin.addStyleName("btn btn-primary")
  submitLogin.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  layout.addComponent(submitLogin, "submitLogin")

  def setWhiteBackground() {
    layout.getUI.getPage.getJavaScript.execute("$('body').addClass('loginView');")
  }
  def restoreColoredBackground() {
    layout.getUI.getPage.getJavaScript.execute("$('body').removeClass('loginView');")
  }

  override def attach() {
    super.attach()
    sync {
      //this.getUI.getPage.getJavaScript.execute("$('body').css('background-image', 'none');")
      setWhiteBackground()
    }
  }

  // For testing
  def autoLogin(email: String, pwd: String) {
    emailField.setValue(email)
    passwordField.setValue(pwd)
    sync {
      submitLogin.click()
    }
  }

  private def sync[R](block: => R): Option[R] = {
    var result: Option[R] = None
    val lock = this.getUI.getSession.getLockInstance
    lock.lock()
    try {
      result = Option(block)
    } finally {
      lock.unlock()
    }
    result
  }

}

