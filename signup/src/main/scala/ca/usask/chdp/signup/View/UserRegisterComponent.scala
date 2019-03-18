package ca.usask.chdp.signup.View

import com.vaadin.ui._
import com.vaadin.event.FieldEvents.{FocusEvent, FocusListener}
import com.vaadin.event.ShortcutAction
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.models.{CourseDAO, ParticipantDAO}
import com.vaadin.data.util.BeanContainer
import org.apache.commons.validator.routines.EmailValidator
import com.vaadin.server.Page
import org.slf4j.LoggerFactory
import com.vaadin.annotations.JavaScript
import scala.collection.JavaConversions._

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class UserRegisterComponent(onLoginCallback: (String) => Unit) extends CustomComponent {
  val log = LoggerFactory.getLogger(this.getClass)
  val layout = new CustomLayout("userRegisterLayout")
  setCompositionRoot(layout)

  val existingEmail = new TextField()
  existingEmail.setSizeUndefined()
  existingEmail.setInputPrompt("Email")
  existingEmail.setImmediate(true)
  layout.addComponent(existingEmail, "existingEmail")
  existingEmail.addFocusListener(new FocusListener {
    def focus(event: FocusEvent) {
      existingSubmit.setClickShortcut(ShortcutAction.KeyCode.ENTER)
      submit.removeClickShortcut()
    }
  })

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
          hideAndCallback(existingEmail.getValue)
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

  /**
   * New user signUp
   * --------------------------------------------------------------------
   */
  val newEmail = new TextField()
  newEmail.setSizeUndefined()
  newEmail.setInputPrompt("Email")
  newEmail.setImmediate(true)
  layout.addComponent(newEmail, "newEmail")
  newEmail.addFocusListener(new FocusListener {
    def focus(event: FocusEvent) {
      submit.setClickShortcut(ShortcutAction.KeyCode.ENTER)
      existingSubmit.removeClickShortcut()
    }
  })

  val newEmail2 = new TextField()
  newEmail2.setSizeUndefined()
  newEmail2.setInputPrompt("Email")
  layout.addComponent(newEmail2, "newEmail2")

  val password1 = new PasswordField()
  password1.setSizeUndefined()
  layout.addComponent(password1, "password1")

  val password2 = new PasswordField()
  password2.setSizeUndefined()
  layout.addComponent(password2, "password2")

  val nsid = new TextField()
  nsid.setSizeUndefined()
  nsid.setInputPrompt("USask NSID")
  layout.addComponent(nsid, "nsid")

  val nsid2 = new TextField()
  nsid2.setSizeUndefined()
  nsid2.setInputPrompt("USask NSID")
  layout.addComponent(nsid2, "nsid2")

  val availableSections = CourseDAO.findAll.map(b => SimpleCourseBean(b._id, b.toString))
  val sectionsContainer = new BeanContainer[String, SimpleCourseBean](classOf[SimpleCourseBean])
  sectionsContainer.setBeanIdProperty("id")
  sectionsContainer.addAll(availableSections)
  sectionsContainer.sort(Array("id"), Array(true))
  val sectionComboBox = new ComboBox()
  sectionComboBox.setContainerDataSource(sectionsContainer)
  sectionComboBox.setItemCaptionPropertyId("info")
  layout.addComponent(sectionComboBox, "sectionComboBox")

  val submit = new Button("Submit", new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (newEmail.getValue == "" ||
        newEmail.getValue != newEmail2.getValue ||
        !EmailValidator.getInstance().isValid(newEmail.getValue)) {
        failed("email")
      } else if (password1.getValue == "" ||
        password1.getValue != password2.getValue) {
        failed("password")
      } else if (nsid.getValue == "" || nsid.getValue != nsid2.getValue) {
        failed("nsid")
      } else if (!isNsidValid(nsid.getValue)) {
        failed("invalidNsid")
      } else if (sectionComboBox.getValue == null) {
        failed("section")
      } else if (ParticipantDAO.isDefined(newEmail.getValue)) {
        failed("exists")
      } else {
        val courseId = sectionComboBox.getValue.asInstanceOf[String]
        val courseInfo = sectionsContainer.getItem(courseId).getBean.info
        val partBean = ParticipantDAO.insertNewUser(newEmail.getValue, password1.getValue, nsid.getValue,
          courseId, courseInfo)
        if (partBean.isDefined && partBean.get.email == newEmail.getValue) {
          Notification.show("Welcome", Notification.Type.TRAY_NOTIFICATION)
          hideAndCallback(partBean.get.email)
        } else {
          log.error("Participant bean for user -- {} -- did not have correct email address. " +
            "returend bean: -- {} --", newEmail.getValue, partBean)
          Notification.show("There was an error. Please contact the research pool administrator " +
            "-- poile@edwards.usask.ca.", Notification.Type.ERROR_MESSAGE)
        }
      }
    }
  })
  submit.setSizeUndefined()
  submit.addStyleName("btn btn-primary")
  layout.addComponent(submit, "submit")

  def failed(field: String) {
    field match {
      case "email" => Notification.show("Emails do not match or are not valid.", Notification.Type.WARNING_MESSAGE)
      case "password" => Notification.show("Passwords do not match.", Notification.Type.WARNING_MESSAGE)
      case "invalidNsid" => Notification.show("Invalid NSIDs.", Notification.Type.WARNING_MESSAGE)
      case "nsid" => Notification.show("NSIDs do not match.", Notification.Type.WARNING_MESSAGE)
      case "section" => Notification.show("Choose a class section.", Notification.Type.WARNING_MESSAGE)
      case "exists" => Notification.show("A user with that email is already registered. If you forgot your password " +
        "please contact the research coordinator (poile@edwards.usask.ca).", Notification.Type.ERROR_MESSAGE)
      case _ => Notification.show("Login Failed.", Notification.Type.WARNING_MESSAGE)
    }
  }
  def isNsidValid(nsid: String): Boolean = {
    (nsid.length == 6 &&
      nsid.substring(0, 3).forall(p => p.isLetter) &&
      nsid.substring(3).forall(p => p.isDigit))
  }
  def turnOffSignInButtons() {
    submit.setEnabled(false)
    existingSubmit.setEnabled(false)
  }
  def hideAndCallback(userId: String) {
    Page.getCurrent.getJavaScript.execute("$('#register').hide('slow');")
    onLoginCallback(userId)
  }
}
