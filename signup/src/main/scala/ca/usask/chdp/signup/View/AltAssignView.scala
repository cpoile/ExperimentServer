package ca.usask.chdp.signup.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.models.{ParticipantDAO}
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import ca.usask.chdp.{TimeTools, ExpSettings}

class AltAssignView(signUpViewManager: SignUpViewManager, passedUserId: String = "") extends CustomComponent {

  val log = LoggerFactory.getLogger(this.getClass)
  val layout = new CustomLayout("altAssignLayout")
  setCompositionRoot(layout)

  /**
   * Component State
   */
  private[this] var curUserId = ""

  /**
   * Component Definition
   */
  val altAssignRegister = new Button("Register For Alternative Assignment")
  altAssignRegister.setSizeUndefined()
  altAssignRegister.setEnabled(false)
  altAssignRegister.addStyleName("btn btn-primary")
  val altAssignWithdrawRegistration = new Button("Un-register from Alternative Assignment")
  altAssignWithdrawRegistration.setSizeUndefined()
  altAssignWithdrawRegistration.addStyleName("btn btn-primary")

  val altAssignRegisterButtons = new HorizontalLayout()
  altAssignRegisterButtons.setSpacing(true)
  altAssignRegisterButtons.addComponents(altAssignRegister, altAssignWithdrawRegistration)
  layout.addComponent(altAssignRegisterButtons, "altAssignRegisterButtons")

  val toExperimentSessionSignUp = new Button("Experiment Session SignUp")
  layout.addComponent(toExperimentSessionSignUp, "toExperimentSessionSignUp")

  val altAssignFeedback = new VerticalLayout()
  layout.addComponent(altAssignFeedback, "altAssignFeedback")

  /**
   * --------------------------------------------------------------------
   * Page Behavior:
   * --------------------------------------------------------------------
   */

  // For user feedback:
  def updateFeedbackAndButtons(userId: String) {
    altAssignFeedback.removeAllComponents()
    val part = ParticipantDAO.findByEmail(userId)
    if (part.isEmpty) {
      altAssignFeedback.addComponent(new Label("You are not signed in."))
    } else if (part.get.signUpSlotId != "") {
      val note = new Label("You have registered for an experiment session already. " +
        "You need to remove your booking for that session before you can sign up to the alternative assignment.")
      val linkToBooking = new Button("Go To Session Booking Page", new ClickListener {
        def buttonClick(event: ClickEvent) {
          signUpViewManager.setPageTo(SignUp(userId))
        }
      })
      linkToBooking.setSizeUndefined()
      linkToBooking.addStyleName("btn btn-small btn-warning")
      altAssignFeedback.addComponents(note, linkToBooking)
    } else if (part.get.signUpInfo.isSignedUpForAltAssignment)
      altAssignFeedback.addComponent(new Label("You have registered for the alternative assignment. " +
        "You will receive an email on Tues Apr. 9th with the assignment information. Thank-you."))
    else if (!isBeforeDeadline) {
      altAssignFeedback.addComponent(new Label("The deadline has passed to register for the alternative assignment."))
    } else
    altAssignFeedback.addComponent(new Label("You are not registered for the alternative assignment."))
    checkAndUpdateButtons(userId)
  }
  def checkAndUpdateButtons(userId: String) {
    val part = ParticipantDAO.findByEmail(userId)
    altAssignRegister.setEnabled(isEligibleToSignUp(userId)
    )
    altAssignWithdrawRegistration.setEnabled(part.isDefined && part.get.signUpInfo.isSignedUpForAltAssignment && isBeforeDeadline)
  }

  def isEligibleToSignUp(userId: String): Boolean = {
    val part = ParticipantDAO.findByEmail(userId)
    (part.isDefined && !part.get.signUpInfo.isSignedUpForAltAssignment &&
      part.get.signUpSlotId == "" && isBeforeDeadline)
  }

  def isBeforeDeadline: Boolean = {
    TimeTools.hasTime(DateTime.now, ExpSettings.get.deadlineToRegisterForAltAssignment, "0:00")
  }

  /**
   * Component Behavior
   */
  altAssignRegister.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (curUserId != "") {
        setUsersAltAssignRegistration(curUserId, isRegistered = true)
      }
      checkAndUpdateButtons(curUserId)
      updateFeedbackAndButtons(curUserId)
    }
  })
  altAssignWithdrawRegistration.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      setUsersAltAssignRegistration(curUserId, isRegistered = false)
      checkAndUpdateButtons(curUserId)
      updateFeedbackAndButtons(curUserId)
    }
  })
  toExperimentSessionSignUp.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      signUpViewManager.setPageTo(SignUp(curUserId))
    }
  })
  def setUsersAltAssignRegistration(curUserId: String, isRegistered: Boolean) {
    val part = ParticipantDAO.findByEmail(curUserId)
    part.foreach(p =>
      ParticipantDAO.insertParticipantBean(
        p.copy(signUpInfo = p.signUpInfo.copy(isSignedUpForAltAssignment = isRegistered))))
  }

  /**
   * Startup logic:
   */
  override def attach() {
    super.attach()
    if (passedUserId == "") {
      layout.addComponent(new UserRegisterComponent((newUserId) => {
        curUserId = newUserId
        updateFeedbackAndButtons(curUserId)
      }), "userRegisterComponent")
      altAssignRegister.setEnabled(false)
      altAssignWithdrawRegistration.setEnabled(false)
    } else {
      curUserId = passedUserId
      updateFeedbackAndButtons(curUserId)
    }
  }
  /**
   * Ending logic:
   */
  override def detach() {
    super.detach()
    log.debug("On Detach called for user: {}", curUserId)
  }

}
