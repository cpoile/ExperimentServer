package ca.usask.chdp.signup.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.data.util.BeanContainer
import ca.usask.chdp.models._
import collection.JavaConversions._
import ca.usask.chdp.signup.AkkaSystem
import scala.concurrent.duration._
import reflect.BeanProperty
import com.vaadin.data.Property
import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import ca.usask.chdp.models.SignUpSlotBean
import ca.usask.chdp.{TimeTools, Email}
import com.vaadin.server.{VaadinService, Page}
import akka.actor.ActorSystem
import akka.actor.Cancellable
import com.vaadin.annotations.JavaScript
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import com.vaadin.event.FieldEvents.{FocusEvent, FocusListener}
import com.vaadin.event.ShortcutAction

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class SignUpView(signUpViewManager: SignUpViewManager, passedUserId: String = "") extends CustomComponent {
  // these imports are used to create an implicit execution context. See the akka docs on the
  // switch from Akka 2.0 to 2.1

  import scala.concurrent.duration._

  val as: ActorSystem = AkkaSystem.system

  import as.dispatcher

  val log = LoggerFactory.getLogger(this.getClass)
  val layout = new CustomLayout("signupLayout")
  setCompositionRoot(layout)

  /**
   * Component State
   */
  private[this] var curUserId = ""

  /**
   * Component Definition
   */
  val toAltAssignSignUp = new Button("Alternative Assignment SignUp")
  layout.addComponent(toAltAssignSignUp, "toAltAssignSignUp")

  val anonUnderstoodButton = new Button("I understand that my behavior is anonymous")
  anonUnderstoodButton.setSizeUndefined()
  anonUnderstoodButton.setPrimaryStyleName("btn btn-primary")
  anonUnderstoodButton.setEnabled(false)
  layout.addComponent(anonUnderstoodButton, "anonUnderstoodButton")

  val consentLongerVers = new Button("Click to see longer version of consent form")
  consentLongerVers.setSizeUndefined()
  consentLongerVers.setPrimaryStyleName("btn btn-primary btn-small")
  consentLongerVers.setEnabled(false)
  layout.addComponent(consentLongerVers, "consentLongerVers")

  val consentPhrase = new TextField()
  consentPhrase.setSizeUndefined()
  consentPhrase.setWidth("600px")
  consentPhrase.setImmediate(false)
  layout.addComponent(consentPhrase, "consentPhrase")

  val consentAgree = new Button("I have read and I Agree")
  consentAgree.setSizeUndefined()
  consentAgree.setPrimaryStyleName("btn btn-primary")
  consentAgree.setEnabled(false)
  layout.addComponent(consentAgree, "consentAgree")

  val sessionButtons = new HorizontalLayout()
  sessionButtons.setSpacing(true)

  val slotChoiceSubmit = new Button("Submit Choice")
  slotChoiceSubmit.addStyleName("btn btn-primary")
  slotChoiceSubmit.setDisableOnClick(true)
  slotChoiceSubmit.setEnabled(false)

  val sessionUnRegister = new Button("Un-Register From My Session")
  sessionUnRegister.setSizeUndefined()
  sessionUnRegister.setEnabled(false)
  sessionUnRegister.addStyleName("btn btn-primary")

  sessionButtons.addComponents(slotChoiceSubmit, sessionUnRegister)
  layout.addComponent(sessionButtons, "sessionButtons")

  val goToSurveys = new Button("Surveys")
  goToSurveys.setSizeUndefined()
  goToSurveys.addStyleName("btn btn-primary")
  layout.addComponent(goToSurveys, "goToSurveys")


  /**
   * Component Behavior:
   */
  toAltAssignSignUp.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      signUpViewManager.setPageTo(AltAssign(curUserId))
    }
  })
  goToSurveys.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      Page.getCurrent.setLocation("http://chp3.usask.ca/survey/")
      VaadinService.getCurrent.closeSession(UI.getCurrent.getSession)
    }
  })

  /**
   * Page Behavior:
   */
  def updateFeedbackAndButtons(userId: String) {
    ParticipantDAO.findByEmail(userId).foreach { p =>
      if (!p.signUpInfo.hasSignedAnonymityAgreement) {
        anonUnderstoodButton.setEnabled(true)
        consentLongerVers.setEnabled(false)
        consentPhrase.setEnabled(false)
        consentAgree.setEnabled(false)
      } else if (p.signUpInfo.hasSignedAnonymityAgreement &&
        !p.signUpInfo.hasSignedConsent) {
        Page.getCurrent.getJavaScript.execute("$('#confidentiality').hide('slow');")
        consentAgree.setEnabled(true)
        consentLongerVers.setEnabled(true)
        consentPhrase.setEnabled(true)
      } else if (p.signUpInfo.hasSignedAnonymityAgreement &&
        p.signUpInfo.hasSignedConsent) {
        Page.getCurrent.getJavaScript.execute("$('#confidentiality').hide('slow');")
        Page.getCurrent.getJavaScript.execute("$('#consent').hide('slow');")
        slotTable.setEnabled(!p.signUpInfo.isSignedUpForAltAssignment)
        sessionComboBox.setEnabled(!p.signUpInfo.isSignedUpForAltAssignment)
        slotChoiceSubmit.setEnabled(!p.signUpInfo.isSignedUpForAltAssignment)
        sessionUnRegister.setEnabled(p.signUpSlotId != "")
        // Do we show survey info?
        goToSurveys.setEnabled(p.signUpSlotId != "")
        if (p.signUpSlotId != "") {
          Page.getCurrent.getJavaScript.execute("$('#preSessionQuestions').show('slow');")
        } else {
          Page.getCurrent.getJavaScript.execute("$('#preSessionQuestions').hide('slow');")
        }
      }
    }
    setPageFeedback(userId)
  }
  def setPageFeedback(userId: String) {
    bookingFeedback.removeAllComponents()
    ParticipantDAO.findByEmail(userId).foreach { p =>
      if (p.signUpSlotId == "" && p.signUpInfo.isSignedUpForAltAssignment) {
        val note = new Label("You are registered for the alternative assignment already. " +
          "You need to un-register from the alternative assignment before you can " +
          "sign up to an experiment session.")
        val linkToAltAssign = new Button("Go To Alternative Assignment Page", new ClickListener {
          def buttonClick(event: ClickEvent) {
            signUpViewManager.setPageTo(AltAssign(userId))
          }
        })
        linkToAltAssign.setSizeUndefined()
        linkToAltAssign.addStyleName("btn btn-small btn-warning")
        bookingFeedback.addComponents(note, linkToAltAssign)
      } else if (p.signUpSlotId == "") {
        bookingFeedback.addComponents(new Label("You are not booked into a session yet."))
      } else {
        bookingFeedback.addComponent(new Label(p.signUpSlotInfo))
      }
    }
  }

  /**
   * --------------------------------------------------------------------
   * Consent Forms
   * --------------------------------------------------------------------
   */
  consentLongerVers.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      Page.getCurrent.getJavaScript.execute("$('#consentLongForm').toggle();")
    }
  })
  def userSignedAnonymityAgreement(userId: String) {
    ParticipantDAO.findByEmail(userId) foreach {
      p =>
        ParticipantDAO.insertParticipantBean(
          p.copy(signUpInfo = p.signUpInfo.copy(hasSignedAnonymityAgreement = true)))
    }
  }
  def userSignedConsent(userId: String) {
    ParticipantDAO.findByEmail(userId) foreach {
      p =>
        ParticipantDAO.insertParticipantBean(
          p.copy(signUpInfo = p.signUpInfo.copy(hasSignedConsent = true)))
    }
  }
  def userHasTroubleWithSimpleInstructions(userId: String) {
    ParticipantDAO.findByEmail(userId) foreach {
      p =>
        ParticipantDAO.insertParticipantBean(
          p.copy(signUpInfo = p.signUpInfo.
            copy(hasTroubleWithSimpleInstructions = p.signUpInfo.hasTroubleWithSimpleInstructions + 1)))
    }
  }
  anonUnderstoodButton.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      userSignedAnonymityAgreement(curUserId)
      updateFeedbackAndButtons(curUserId)
    }
  })
  consentAgree.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (consentPhrase.getValue.toLowerCase.trim ==
        "When I do the simulation game, I will be alone for about 45 minutes.".toLowerCase) {
        userSignedConsent(curUserId)
        updateFeedbackAndButtons(curUserId)
      } else {
        Notification.show("Please read the instructions carefully.", Notification.Type.WARNING_MESSAGE)
        userHasTroubleWithSimpleInstructions(curUserId)
      }
    }
  })


  /**
   * --------------------------------------------------------------------
   * Picking a time slot
   * --------------------------------------------------------------------
   */
  val bookingFeedback = new VerticalLayout()
  layout.addComponent(bookingFeedback, "bookingFeedback")

  val timeSlotContainer = new BeanContainer[String, SignUpSlotBean](classOf[SignUpSlotBean])
  timeSlotContainer.setBeanIdProperty("slotId")
  timeSlotContainer.addAll(SignUpSlotDAO.findAll)
  timeSlotContainer.sort(Array("slotId"), Array(true))
  val slotTable = new Table("", timeSlotContainer)
  slotTable.setPageLength(12)
  //slotTable.setWidth("900px")
  slotTable.setVisibleColumns(Array("location", "date", "length", "spacesTotal", "spacesFree"))
  slotTable.setSortEnabled(true)
  slotTable.sort(Array("slotId").asInstanceOf[Array[AnyRef]], Array(true))
  layout.addComponent(slotTable, "experimentSessions")

  val availableSessions = SignUpSlotDAO.findAll.filter(isRegisterableTimeSlot).map(b => SimpleSlotBean(b._id, b.toString))
  val simpleSessionContainer = new BeanContainer[String, SimpleSlotBean](classOf[SimpleSlotBean])
  simpleSessionContainer.setBeanIdProperty("id")
  simpleSessionContainer.addAll(availableSessions)
  simpleSessionContainer.sort(Array("id").asInstanceOf[Array[AnyRef]], Array(true))
  val sessionComboBox = new ComboBox("Sessions with openings:", simpleSessionContainer)
  sessionComboBox.setItemCaptionPropertyId("info")

  layout.addComponent(sessionComboBox, "sessionComboBox")

  slotChoiceSubmit.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (sessionComboBox.getValue != null) {
        val slotId = sessionComboBox.getValue.asInstanceOf[String]
        val signUpSlot = SignUpSlotDAO.find(slotId)
        ParticipantDAO.findByEmail(curUserId) foreach { p =>
          if (p.signUpSlotId != "") {
            Notification.show("Must unregister from your curret session  before registering for a new one.", Notification.Type.WARNING_MESSAGE)
          } else if (SignUpSlotDAO.isValidUpdate(slotId, curUserId) && isRegisterableTimeSlot(signUpSlot)) {
            // TODO: Refactor me.
            // ask signUpSlotDAO to insert them into their new slot.
            val scheduledParticipant = SignUpSlotDAO.scheduleAParticipant(slotId, p)
            if (scheduledParticipant.signUpSlotId == slotId) {
              // scheduling was successful. Now update DB.
              ParticipantDAO.insertParticipantBean(scheduledParticipant)
              val note = new Notification("Signed up to slot: " + scheduledParticipant.signUpSlotInfo,
                Notification.Type.HUMANIZED_MESSAGE)
              note.setDelayMsec(4000)
              note.show(Page.getCurrent)
              sendConfirmationEmail(curUserId, scheduledParticipant.signUpSlotInfo)
            } else {
              Notification.show("Was not able to sign up. Try Again.", Notification.Type.WARNING_MESSAGE)
            }
          } else {
            Notification.show("There are not enough spaces, you are already registered,\n or it is too late to register for this slot. Try again.", Notification.Type.WARNING_MESSAGE)
          }
        }
      }
      updateFeedbackAndButtons(curUserId)
    }
  })

  sessionUnRegister.addClickListener(new ClickListener {
    def buttonClick(event: ClickEvent) {
      ParticipantDAO.findByEmail(curUserId) foreach { p =>
        val signUp = SignUpSlotDAO.find(p.signUpSlotId)
        //log.debug("signup currently is: {} and now is: {} and has time? {} ",
        //  Array[AnyRef](signUp.getDateAsDateTime, DateTime.now, boolean2Boolean(TimeTools.hasTime(DateTime.now, signUp.getDateAsDateTime, "4:00"))))
        if (TimeTools.hasTime(DateTime.now, signUp.getDateAsDateTime, "4:00")){
          val transPartBean = SignUpSlotDAO.removePartFromTheirCurSlotInDB(p)
          ParticipantDAO.insertParticipantBean(transPartBean)
          val note = new Notification("You have been removed from your experiment booking.", Notification.Type.HUMANIZED_MESSAGE)
          note.setDelayMsec(4000)
          note.show(Page.getCurrent)
        } else {
          val note = new Notification("It is less than 4 hours until your experiment session (or it is over).\nYou are unable to withdraw from the session.", Notification.Type.ERROR_MESSAGE)
          note.setDelayMsec(4000)
          note.show(Page.getCurrent)
        }
      }
      updateFeedbackAndButtons(curUserId)
    }
  })

  sessionComboBox.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      // enable the submit button when the user has an item selected.
      if (event.getProperty.getValue == null) {
        slotChoiceSubmit.setEnabled(false)
      } else {
        slotChoiceSubmit.setEnabled(true)
      }
    }
  })

  def isRegisterableTimeSlot(s: SignUpSlotBean): Boolean = s.spacesFree > 0 && TimeTools.hasTime(DateTime.now, s.getDateAsDateTime, "4:00")

  /*Set up refresher.*/
  val timeSlotListener: Cancellable = AkkaSystem.system.scheduler.schedule(5 seconds, 5 seconds) {
    // if we are still attached...
    if (layout.getUI == null || layout.getUI.getSession == null) {
      log.debug("Cancelled timeslot listener -- from the listener.")
      timeSlotListener.cancel()
    } else {
      val slots = SignUpSlotDAO.findAll
      for (slot <- slots) {
        sync {
          if (timeSlotContainer.containsId(slot.slotId) && timeSlotContainer.getItem(slot.slotId).getBean != slot) {
            timeSlotContainer.removeItem(slot.slotId)
            timeSlotContainer.addBean(slot)
            slotTable.sort(Array("slotId").asInstanceOf[Array[AnyRef]], Array(true))
          } else {
            timeSlotContainer.addBean(slot)
            slotTable.sort(Array("slotId").asInstanceOf[Array[AnyRef]], Array(true))
          }
        }
      }
      val newAvailableSessions = slots.filter(isRegisterableTimeSlot).map(b => SimpleSlotBean(b._id, b.toString))
      // if there are fewer slots now than before, just refresh the whole thing.
      sync {
        if (simpleSessionContainer.size() != newAvailableSessions.length) {
          simpleSessionContainer.removeAllItems()
          simpleSessionContainer.addAll(newAvailableSessions)
        } else {
          for (slot <- newAvailableSessions) {
            if (simpleSessionContainer.containsId(slot.id) &&
              simpleSessionContainer.getItem(slot.id).getBean.info != slot.info) {
              simpleSessionContainer.getContainerProperty(slot.id, "info").asInstanceOf[Property[String]].setValue(slot.info)
            } else {
              simpleSessionContainer.addBean(slot)
            }
          }
        }
      }
    }
  }

  /**
   * Initially disabled buttons. and inputs/displays.
   */
  anonUnderstoodButton.setEnabled(false)
  consentAgree.setEnabled(false)
  slotTable.setEnabled(false)
  sessionComboBox.setEnabled(false)
  slotChoiceSubmit.setEnabled(false)


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
    } else {
      curUserId = passedUserId
      updateFeedbackAndButtons(curUserId)
    }
  }
  override def detach() {
    // remove the scheduled timeSlotListener
    super.detach()
    log.debug("On Detach called for user: {}", curUserId)
    timeSlotListener.cancel()
    log.debug("Cancelled timeslot listener -- from detach.")
  }

  /**
   * Helpers. Probably should be in a separate class.
   */
  def sendConfirmationEmail(curUserId: String, sessInfo: String) {
    val body = Email.ConfirmationMsg(sessInfo)
    Email.sendEmail(body, "Comm 105 - Research Pool Confirmation",
      curUserId, "Comm 105 Research Pool", "devstudy@edwards.usask.ca")
  }
  private def sync[R](block: => R): Option[R] = {
    var result: Option[R] = None
    val lock = layout.getUI.getSession.getLockInstance
    lock.lock()
    try {
      result = Option(block)
    } finally {
      lock.unlock()
    }
    result
  }
}

case class SimpleSlotBean(@BeanProperty id: String, @BeanProperty var info: String)
case class SimpleCourseBean(@BeanProperty id: String, @BeanProperty info: String)

/**
 * the JS commands we were using back when relying on JS for removing divs (stupid).
 */

/*
  val persistentCmds = new JSPersistentCmds(
    List("hideDisablingDiv('.step1.disablingDiv')", // 0
      "hideDisablingDiv('.step2.disablingDiv')", // 1
      "hideDisablingDiv('.step3.disablingDiv')", // 2
      "removeId('confidentiality'); removeId('consent'); removeId('register'); " +
        "hideDisablingDiv('.step3.disablingDiv')", // 3
      "removeId('register')", // 4
      "hideDisablingDiv('.step4.disablingDiv')"), // 5
    List(false, false, false, false, false, false))
  layout.addComponent(persistentCmds, "persistentCmds")
*/

