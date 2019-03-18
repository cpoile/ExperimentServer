package ca.usask.chdp.surveyRedirect.View

import scalaz._
import Scalaz._
import ca.usask.chdp.models._
import ca.usask.chdp.surveyRedirect.SurveyViewManager
import com.vaadin.ui.Button.{ClickListener, ClickEvent}
import com.vaadin.ui.{Label, Button, CustomLayout, CustomComponent}
import org.slf4j.LoggerFactory
import com.vaadin.shared.ui.label.ContentMode
import ca.usask.chdp.{TimeTools, ExpSettings}
import org.joda.time.{Period, DateTime}
import org.joda.time.format.PeriodFormatterBuilder
import ca.usask.chdp.models.ParticipantBean

class SurveyMainPageView(viewMgr: SurveyViewManager, curUser: String) extends CustomComponent {
  val log = LoggerFactory.getLogger(classOf[SurveyMainPageView])


  val layout = new CustomLayout("surveyMainPageLayout")
  setCompositionRoot(layout)

  val survey1 = new Button("Survey 1", new ClickListener {
    def buttonClick(event: ClickEvent) {
      viewMgr.setPageTo(Survey1)
    }
  })
  survey1.setSizeUndefined()
  survey1.addStyleName("btn btn-primary")
  layout.addComponent(survey1, "survey1")

  val survey1ComplMsg = new Label()
  survey1ComplMsg.addStyleName("c-inline")
  val complMsg1 = getSurveyComplMsg("survey1", survey1)
  layout.addComponent(survey1ComplMsg, "survey1ComplMsg")
  survey1ComplMsg.setValue(complMsg1)

  def insertFinishedAllMsg() {
    val innerHtml: StringBuilder = new StringBuilder( """<div class="page-header">
                                                                <h1>Congratulations, you are finished!</h1>
                                                            </div>
                                                            <p>You have finished the surveys and are ready to participate in the experiment.</p>
                                                            <p>Remember, your experiment session is: """)
    innerHtml ++= ParticipantDAO.findByEmail(curUser).getOrElse(ParticipantBean("")).signUpSlotInfo + "</p>"
    val ifFinishedAllMsg = new Label(innerHtml.mkString, ContentMode.HTML)
    layout.addComponent(ifFinishedAllMsg, "ifFinishedAllMsg")
  }
  val mapOfAllCompl = ParticipantDAO.findByEmail(curUser).getOrElse(ParticipantBean("")).surveyCompl
  val status1 = mapOfAllCompl.getOrElse("survey1", false)
  if (status1)
    insertFinishedAllMsg()

  def getSurveyComplMsg(surveyKey: String, button: Button): String = {
    val part = ParticipantDAO.findByEmail(curUser).getOrElse(ParticipantBean(""))
    val mapCompl = part.surveyCompl
    val signUp = SignUpSlotDAO.find(part.signUpSlotId)
    val isComplete = mapCompl.getOrElse(surveyKey, false)
    if (isComplete) {
      button.setEnabled(false)
      "Complete, thank you."
    } else if (TimeTools.hasTime(DateTime.now, signUp.getDateAsDateTime, "4:00") || part.waive12HourSurveyRule) {
      button.setEnabled(true)
      "Not completed."
    } else {
      button.setEnabled(false)
      "Not completed. And it is less than 4 hours before the start of the experiment. You will not be able to participate in the experiment."
    }
  }
}
