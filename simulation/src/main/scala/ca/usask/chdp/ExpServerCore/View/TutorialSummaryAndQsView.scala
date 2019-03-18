package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.ui.Button.{ClickListener, ClickEvent}
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.{FinishedTutorial, WaitingInLobbyPage}
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.WaitingPlayer
import org.slf4j.LoggerFactory
import akka.actor.ActorSystem
import com.vaadin.server.{ThemeResource, Sizeable}


class TutorialSummaryAndQsView(plr: WaitingPlayer, viewMgr: ViewManager) extends CustomComponent {
  val log = LoggerFactory.getLogger("InstructionsView")

  val layout = if (plr.role == "RoleA") {
    new CustomLayout("tutorialSummaryAndQsLayoutA")
  } else {
    new CustomLayout("tutorialSummaryAndQsLayoutB")
  }
  setCompositionRoot(layout)

  case class ControlQuestion(answer: String, box: ComboBox, error: Embedded)
  val qContainer = new VerticalLayout
  layout.addComponent(qContainer, "qContainer")

  def makeControlQBox(fieldName: String, question: String, answers: List[String],
                      correctAnswer: String, width: Int): ControlQuestion = {
    val label = new Label(question)
    label.setStyleName("controlQ")
    val boxContainer = new HorizontalLayout()
    boxContainer.setStyleName("controlBoxContainer")
    val box = new ComboBox()
    for (a <- answers) box.addItem(a)
    box.setWidth(width, Sizeable.Unit.PIXELS)
    //    val errorSpace = new Label("  ")
    val errorSpace = new Embedded(null, new ThemeResource("img/redx.png"))
    //    errorSpace.setId(fieldName + "_err")
    //    errorSpace.setPrimaryStyleName("controlQError")
    errorSpace.setVisible(false)
    boxContainer.addComponents(box, errorSpace)
    qContainer.addComponents(label, boxContainer)
    //qList = (correctAnswer, box, errorSpace) :: qList
    ControlQuestion(correctAnswer, box, errorSpace)
  }

  def makeQuestionListForA: List[ControlQuestion] = {
    makeControlQBox("roleQ", "Are you taking the role of the First Engineer or the Second Engineer?",
      List("First Engineer", "Second Engineer"), "First Engineer", 100) ::
    makeControlQBox("inCompetitionQ", "You are in competition for $20. Who are you competing with for the $20 prize? -- Other First Engineers; Other Second Engineers; or My Partner",
      List("Other First Engineers", "Other Second Engineers", "My Partner"), "Other First Engineers", 200) ::
    makeControlQBox("rewardedBasedOnQ", "Your managers care about one thing only. It is: The number of days I work on the Concept Car; or The placement of our team in the final standings", List("The number of days I work on the Concept Car", "The placement of our team in the final standings"), "The number of days I work on the Concept Car", 300) ::
    makeControlQBox("whoIsTheWinnerQA", "Suppose you work on the Concept Car for 30 days, and another First Engineer works on the Concept Car for 32 days. If it is only between you two, who would earn the $20 prize from this experiment?", List("I would earn the $20", "The other First Engineer would earn the $20"), "The other First Engineer would earn the $20", 200) ::
    Nil
  }

  def makeQuestionListForB: List[ControlQuestion] = {
    makeControlQBox("roleQ", "Are you taking the role of the First Engineer or the Second Engineer?",
      List("First Engineer", "Second Engineer"), "Second Engineer", 100) ::
    makeControlQBox("inCompetitionQ", "You are in competition for $20. Who are you competing with for the $20 prize? -- Other First Engineers; Other Second Engineers; or My Partner",
      List("Other First Engineers", "Other Second Engineers", "My Partner"), "Other Second Engineers", 200) ::
    makeControlQBox("rewardedBasedOnQ", "Your managers care about one thing only. It is: How well my partner does with their goals; or The placement of our team in the final standings", List("How well my partner does with their goals", "The placement of our team in the final standings"), "The placement of our team in the final standings", 300) ::
    makeControlQBox("whoIsTheWinnerQA", "Suppose your team gets 110 points after 8 races, and another Second Engineer's team gets 106 points. If it is only between you two, who would earn the $20 prize from this experiment?", List("I would earn the $20", "The other Second Engineer would earn the $20"), "I would earn the $20", 200) ::
    Nil
  }

  val qList = if (plr.role == "RoleA") makeQuestionListForA else makeQuestionListForB

  val submit = new Button("Submit", new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (controlQsCorrect(qList)) {
        restoreColoredBackground()
        viewMgr.send(FinishedTutorial(plr))
      } else {
        Notification.show("You have some questions incorrect.",
          Notification.Type.TRAY_NOTIFICATION)

      }
    }
  })
  submit.setSizeUndefined()
  submit.addStyleName("btn btn-primary")
  layout.addComponent(submit, "submit")

  def controlQsCorrect(listOfQs: List[ControlQuestion]): Boolean = {
    // start with a true result.
    // check each control question
    var ret = true
    for (q <- listOfQs) {
      val res = if (q.box.getValue != q.answer) {
        q.error.setVisible(true)
        log.debug("setting error visible")
        false
      } else {
        q.error.setVisible(false)
        log.debug("setting error not visible")
        true
      }
      ret = ret && res
    }
    ret
  }


  if (Lobby.settings.testing_autoControlQuestions) {
    val as: ActorSystem = Lobby.system
    import as.dispatcher
    import scala.concurrent.duration._

    Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay milliseconds) {
      val lock = this.getUI.getSession.getLockInstance
      lock.lock()
      try {
        viewMgr.send(FinishedTutorial(plr))
      } finally {
        lock.unlock()
      }
    }
  }
  override def attach() {
    super.attach()
      //this.getUI.getPage.getJavaScript.execute("$('body').css('background-image', 'none');")
      setWhiteBackground()
  }
  def setWhiteBackground() {
    layout.getUI.getPage.getJavaScript.execute("$('body').addClass('loginView');")
  }
  def restoreColoredBackground() {
    layout.getUI.getPage.getJavaScript.execute("$('body').removeClass('loginView');")
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
