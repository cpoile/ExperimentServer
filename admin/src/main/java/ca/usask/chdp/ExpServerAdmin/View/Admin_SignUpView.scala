package ca.usask.chdp.ExpServerAdmin.View

import com.vaadin.ui._
import akka.event.Logging
import com.vaadin.data.util.{BeanItem, BeanContainer}
import collection.JavaConversions._
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.Table
import com.vaadin.ui.CustomLayout
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import ca.usask.chdp.ExpServerAdmin.AkkaSystem
import ca.usask.chdp.models._

class Admin_SignUpView(viewMgr: AdminViewManager) extends CustomComponent {
  val layout = new CustomLayout("admin_signUpViewLayout")
  setCompositionRoot(layout)
  val log = Logging.getLogger(AkkaSystem.system, "SignUpAdminView")

  /**
   * NavBar
   */
  val toExpAdmin = new Button("Experiment Admin", new ClickListener {
    def buttonClick(event: ClickEvent) {
      viewMgr.setPageTo(AdminPageName.ExpServerAdmin)
    }
  })
  layout.addComponent(toExpAdmin, "toExpAdmin")

  /**
   * NavList
   */
//  val navList = new NavList("scrollcontent")
//  layout.addComponent(navList, "navList")

  val expContainer = new BeanContainer[String, ExperimentBean](classOf[ExperimentBean])
  expContainer.setBeanIdProperty("experimentId")
  expContainer.addAll(ExperimentDAO.findAll)
  val expTable = new Table("", expContainer)
  expTable.setPageLength(0)
  expTable.setWidth("120px")
  layout.addComponent(expTable, "experiments")

  var expForm = new FormBuilder[ExperimentBean]({ExperimentBean("")}, "Experiment_",
  new BeanFieldGroup[ExperimentBean](classOf[ExperimentBean]), ("Experiment ID", "experimentId"))
  val expFormComponent = new FormComponent[ExperimentBean](expForm,
    expTable,
    expContainer,
    {(button: Button) => layout.addComponent(button, "newExp")},
    {ExperimentBean("")},
    {ExperimentDAO.findAll},
    {(exp: ExperimentBean) => ExperimentDAO.validate(exp)},
    {(exp: ExperimentBean) => ExperimentDAO.insertUpdate(exp)},
    {(exp: ExperimentBean) => ExperimentDAO.remove(exp)})
  layout.addComponent(expFormComponent, "newExpForm")

  val timeSlotContainer = new BeanContainer[String, SignUpSlotBean](classOf[SignUpSlotBean])
  timeSlotContainer.setBeanIdProperty("slotId")
  timeSlotContainer.addAll(SignUpSlotDAO.findAll)
  val slotTable = new Table("", timeSlotContainer)
  slotTable.setPageLength(0)
  slotTable.setWidth("900px")
  slotTable.setVisibleColumns(Array("slotId", "location", "date", "length",
    "spacesTotal", "spacesFree", "experimentId"))
  slotTable.sort(Array("experimentId", "slotId"), Array(true, true))
  layout.addComponent(slotTable, "timeSlots")

  val participantsPerSlotContainer = new BeanContainer[String, SimplePartBean](classOf[SimplePartBean])
  participantsPerSlotContainer.setBeanIdProperty("email")
  val participantsPerSlotTable = new Table("", participantsPerSlotContainer)
  participantsPerSlotTable.setPageLength(0)
  layout.addComponent(participantsPerSlotTable, "participantsPerSlotTable")
  participantsPerSlotTable.setVisible(false)
  participantsPerSlotTable.setSelectable(true)
  participantsPerSlotTable.setImmediate(true)


  var slotForm = new FormBuilder[SignUpSlotBean]({SignUpSlotBean("")}, "SignUpSlot_",
  new BeanFieldGroup[SignUpSlotBean](classOf[SignUpSlotBean]),
  ("Experiment ID", "experimentId"), ("Slot Id", "slotId"), ("Room Location", "location"), ("Date (2013-04-3, 02:30 PM)", "date"), ("Length (1:30)", "length"), ("Max Spaces Available", "spacesTotal"), ("Spaces Free", "spacesFree"))
  val slotFormComp = new FormComponent[SignUpSlotBean](slotForm,
    slotTable,
    timeSlotContainer,
    {(button: Button) => layout.addComponent(button, "newSlot")},
    {SignUpSlotBean("")},
    {SignUpSlotDAO.findAll},
    {(ss: SignUpSlotBean) => SignUpSlotDAO.validate(ss)},
    {(ss: SignUpSlotBean) => SignUpSlotDAO.insertUpdate(ss)},
    {(ss: SignUpSlotBean) => SignUpSlotDAO.remove(ss)},
    {bean => addPartsInThisSlot(bean)},
    {removePartsInThisSlotTable()})
  layout.addComponent(slotFormComp, "newSlotForm")
  var deleteCount = 0
  var curSelectedPart = ParticipantBean("")
  var curSelSignUpSlot: SignUpSlotBean = null

  val deleteParticipantButton = new Button("Delete Item", new ClickListener {
    def buttonClick(event: ClickEvent) {
      deleteCount += 1
      if (deleteCount < 2) {
        Notification.show("Press a second time to delete.", Notification.Type.WARNING_MESSAGE)
      } else {
        SignUpSlotDAO.removePartFromThisSlot(curSelSignUpSlot._id,
          SimplePartBean(curSelectedPart._id, curSelectedPart.email))
        deleteCount = 0
        reloadPartsInThisSlotTable()
      }
    }
  })
  deleteParticipantButton.setSizeUndefined()
  deleteParticipantButton.setPrimaryStyleName("btn btn-primary")
  layout.addComponent(deleteParticipantButton, "deleteParticipantButton")
  deleteParticipantButton.setVisible(false)

  participantsPerSlotTable.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      if (event.getProperty.getValue == null) {
        deleteParticipantButton.setVisible(false)
        deleteCount = 0
      } else {
        curSelectedPart = ParticipantDAO.
          findByEmail(event.getProperty.getValue.asInstanceOf[String]).getOrElse(ParticipantBean(""))
        deleteParticipantButton.setVisible(true)
        log.debug("clicked on email: {}", event.getProperty.getValue.asInstanceOf[String])
      }
    }
  })

  def addPartsInThisSlot(bean: BeanItem[SignUpSlotBean]) {
    curSelSignUpSlot = bean.getBean
    reloadPartsInThisSlotTable()
    participantsPerSlotTable.setVisible(true)
    deleteCount = 0
  }

  def removePartsInThisSlotTable() {
    participantsPerSlotTable.setVisible(false)
    deleteParticipantButton.setVisible(false)
  }
  def reloadPartsInThisSlotTable() {
    participantsPerSlotContainer.removeAllItems()
    val listOfSimpleParts = SignUpSlotDAO.find(curSelSignUpSlot._id).registeredParticipants
    participantsPerSlotContainer.addAll(listOfSimpleParts)
  }

  val courseContainer = new BeanContainer[String, CourseBean](classOf[CourseBean])
  courseContainer.setBeanIdProperty("courseId")
  courseContainer.addAll(CourseDAO.findAll)
  val courseTable = new Table("", courseContainer)
  courseTable.setPageLength(0)
  courseTable.setWidth("600px")
  courseTable.setVisibleColumns(Array("courseId", "name", "section", "courseTime", "professor"))
  courseTable.setSortEnabled(true)
  courseTable.sort(Array("courseId"), Array(true))
  layout.addComponent(courseTable, "courses")

  var courseForm = new FormBuilder[CourseBean]({CourseBean("")}, "Course_",
  new BeanFieldGroup[CourseBean](classOf[CourseBean]),
  ("Course ID", "courseId"), ("Name", "name"), ("Section #", "section"), ("Course Timeslot", "courseTime"),
  ("Instructor Name", "professor"))
  val courseFormComp = new FormComponent[CourseBean](courseForm,
    courseTable,
    courseContainer,
    {(button: Button) => layout.addComponent(button, "newCourse")},
    {CourseBean("")},
    {CourseDAO.findAll},
    {(cb: CourseBean) => CourseDAO.validate(cb)},
    {(cb: CourseBean) => CourseDAO.insertUpdate(cb)},
    {(cb: CourseBean) => CourseDAO.remove(cb)})
  layout.addComponent(courseFormComp, "newCourseForm")

  val participantContainer = new BeanContainer[String, ParticipantBean](classOf[ParticipantBean])
  participantContainer.setBeanIdProperty("email")
  participantContainer.addAll(ParticipantDAO.findAll)
  val participantTable = new Table("", participantContainer)
  participantTable.setPageLength(0)
  participantTable.setWidth("1150px")
  participantTable.setVisibleColumns(Array("email", "globalId", "courseId", "courseInfo",
    "signUpSlotId", "signUpSlotInfo"))
  participantTable.setSortEnabled(true)
  participantTable.sort(Array("signUpSlotId", "email"), Array(true, true))

  layout.addComponent(participantTable, "participants")

  var participantForm = new FormBuilder[ParticipantBean]({ParticipantBean("")},
  "Participant_",
  new BeanFieldGroup[ParticipantBean](classOf[ParticipantBean]),
  ("Email", "email"), ("GlobalId", "globalId"), ("Set new Password?", "changePwd"), ("Password", "pwd"),
  ("Course Id", "courseId"), ("Course", "courseInfo"), ("SignUpSlot Id", "signUpSlotId"),
  ("Signed Up for Session", "signUpSlotInfo"))
  val participantFormComp = new FormComponent[ParticipantBean](participantForm,
    participantTable,
    participantContainer,
    {(button: Button) => layout.addComponent(button, "newParticipant")},
    {ParticipantBean("")},
    {ParticipantDAO.findAll},
    {(pb: ParticipantBean) => ParticipantDAO.validate(pb)},
    {(pb: ParticipantBean) => ParticipantDAO.insertUpdate(pb)},
    {(pb: ParticipantBean) => ParticipantDAO.remove(pb)})
  layout.addComponent(participantFormComp, "newParticipantForm")
}








