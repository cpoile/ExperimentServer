package ca.usask.chdp.survey.View

import ca.usask.chdp.survey.SurveyViewManager
import com.vaadin.ui._
import jsExtensions.Affix
import org.slf4j.LoggerFactory
import com.vaadin.ui.Button.{ClickListener, ClickEvent}
import ca.usask.chdp.models.{QBean, ParticipantDAO}
import com.vaadin.server.Page
import ca.usask.chdp.ExpSettings

class Survey2ViewPart2(viewMgr: SurveyViewManager, curUser: String,
                       part1Answers: Seq[QBean]) extends CustomComponent {
  val log = LoggerFactory.getLogger(classOf[Survey2ViewPart2])
  val svsItems = List("EQUALITY (equal opportunity for all",
    "INNER HARMONY (at peace with myself)",
    "SOCIAL POWER (control over others, dominance)",
    "PLEASURE (gratification of desires)",
    "FREEDOM (freedom of action and thought)",
    "A SPIRITUAL LIFE (emphasis on spiritual not material matters)",
    "SENSE OF BELONGING (feeling that others care about me)",
    "SOCIAL ORDER (stability of society)",
    "AN EXCITING LIFE (stimulating experiences)",
    "MEANING IN LIFE (a purpose in life)",
    "POLITENESS (courtesy, good manners)",
    "WEALTH (material possessions, money)",
    "NATIONAL SECURITY (protection of my nation from enemies)",
    "SELF RESPECT (belief in one's own worth)",
    "RECIPROCATION OF FAVOURS (avoidance of indebtedness)",
    "CREATIVITY (uniqueness, imagination)",
    "A WORLD AT PEACE (free of war and conflict)",
    "RESPECT FOR TRADITION (preservation of time honoured customs)",
    "MATURE LOVE (deep emotional & spiritual intimacy)",
    "SELF DISCIPLINE (self restraint, resistance to temptation)",
    "PRIVACY (the right to have a private sphere)",
    "FAMILY SECURITY (safety for loved ones)",
    "SOCIAL RECOGNITION (respect, approval by others)",
    "UNITY WITH NATURE (fitting into nature)",
    "A VARIED LIFE (filled with challenge, novelty and change)",
    "WISDOM (a mature understanding of life)",
    "AUTHORITY (the right to lead or command)",
    "TRUE FRIENDSHIP (close, supportive friends)",
    "A WORLD OF BEAUTY (beauty of nature and the arts)",
    "SOCIAL JUSTICE (correcting injustice, care for the weak)",
    "INDEPENDENT (self reliant, self sufficient)",
    "MODERATE (avoiding extremes of feeling & action)",
    "LOYAL (faithful to my friends, group)",
    "AMBITIOUS (hard working, aspiring)",
    "BROADMINDED (tolerant of different ideas and beliefs)",
    "HUMBLE (modest, self effacing)",
    "DARING (seeking adventure, risk)",
    "PROTECTING THE ENVIRONMENT (preserving nature)",
    "INFLUENTIAL (having an impact on people and events)",
    "HONOURING OF PARENTS AND ELDERS (showing respect)",
    "CHOOSING OWN GOALS (selecting own purposes)",
    "HEALTHY (not being sick physically or mentally)",
    "CAPABLE (competent, effective, efficient)",
    "ACCEPTING MY PORTION IN LIFE (submitting to life's circumstances)",
    "HONEST (genuine, sincere)",
    "PRESERVING MY PUBLIC IMAGE (protecting my \"face\")",
    "OBEDIENT (dutiful, meeting obligations)",
    "INTELLIGENT (logical, thinking)",
    "HELPFUL (working for the welfare of others)",
    "ENJOYING LIFE (enjoying food, sex, leisure, etc.)",
    "DEVOUT (holding to religious faith & belief)",
    "RESPONSIBLE (dependable, reliable)",
    "CURIOUS (interested in everything, exploring)",
    "FORGIVING (willing to pardon others)",
    "SUCCESSFUL (achieving goals)",
    "CLEAN (neat, tidy)",
    "SELF-INDULGENT (doing pleasant things)")

  val layout = new CustomLayout("survey2LayoutPart2")
  setCompositionRoot(layout)
  val affix = new Affix
  layout.addComponent(affix, "affix")


  val hidden = new Label("")
  hidden.setSizeUndefined()
  //hidden.setVisible(false)
  layout.addComponent(hidden, "hidden")

  val listBoxes = for (i <- 30 until 57) yield {
    val box = new TextField()
    box.addStyleName("svsItem")
    layout.addComponent(box, "itemBox" + (i + 1))
    box
  }
  // Now set up Item names
  val listLabels = for (i <- 30 until 57) yield {
    val label = new Label(svsItems(i))
    label.setSizeUndefined()
    layout.addComponent(label, "itemCaption" + (i + 1))
    label
  }


  val submit = new Button("Submit Ratings", new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (areAllBoxesChecked) {
        if (allBoxesValid) {
          ParticipantDAO.findByEmail(curUser) foreach { p =>
            val newCompl = p.surveyCompl + ("survey2" -> true)
            val data = part1Answers ++ genListAnswers
            val newBean = p.copy(surveyCompl = newCompl, survey2Data = data)
            ParticipantDAO.insertUpdate(newBean)
            viewMgr.setPageTo(SurveyMainPage(newBean.email))
          }
        } else {
          Notification.show("Ratings must be between -1 and 7.", Notification.Type.WARNING_MESSAGE)
        }
      } else {
        Notification.show("There is at least one box left unanswered.", Notification.Type.WARNING_MESSAGE)
      }
    }
  })
  submit.setSizeUndefined()
  submit.addStyleName("btn btn-primary")
  layout.addComponent(submit, "submit")

  // notify user of change.
  Notification.show("You are now in page two of the values survey.", Notification.Type.HUMANIZED_MESSAGE)

  def areAllBoxesChecked: Boolean = {
    var allChecked = true
    for (i <- 30 until 57) {
      if (ExpSettings.get.testingMode) listBoxes(i - 30).setValue("2")
      val boxVal = listBoxes(i - 30).getValue
      if (boxVal == null || boxVal == "") {
        listLabels(i - 30).setStyleName("svsItemError")
        allChecked = false
      } else {
        if (listLabels(i - 30).getStyleName.contains("svsItemError"))
          listLabels(i - 30).removeStyleName("svsItemError")
      }
    }
    allChecked
  }
  def allBoxesValid: Boolean = {
    var allValid = true
    for (i <- 30 until 57) {
      val boxValStr = listBoxes(i - 30).getValue
      if (boxValStr != null && boxValStr != "") {
        val boxVal = boxValStr.toInt
        if (boxVal < -1 || boxVal > 7) {
          listLabels(i - 30).setStyleName("svsItemError")
          allValid = false
        } else {
          if (listLabels(i - 30).getStyleName.contains("svsItemError"))
            listLabels(i - 30).removeStyleName("svsItemError")
        }
      }
    }
    allValid
  }
  def genListAnswers: Seq[QBean] =
    for (i <- 30 until 57) yield QBean(i + 1, listBoxes(i - 30).getValue.toInt)

  override def attach() {
    super.attach()
    hidden.getUI.scrollIntoView(hidden)
  }
}
