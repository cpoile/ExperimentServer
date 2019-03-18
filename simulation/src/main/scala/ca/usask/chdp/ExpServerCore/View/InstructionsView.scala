package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.ui.Button.{ClickListener, ClickEvent}
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.WaitingInLobbyPage
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.WaitingPlayer
import com.vaadin.shared.ui.label.ContentMode
import jsExtensions.Gauge
import org.slf4j.LoggerFactory
import akka.actor.ActorSystem

class InstructionsView(plr: WaitingPlayer, viewMgr: ViewManager) extends CustomComponent {
  val log = LoggerFactory.getLogger("InstructionsView")

  val layout = if (plr.role == "RoleA") {
    new CustomLayout("instructionsLayoutA")
  } else {
    new CustomLayout("instructionsLayoutB")
  }

  setCompositionRoot(layout)

  val inCompetitionQ = new ComboBox()
  inCompetitionQ.addItem("Yes")
  inCompetitionQ.addItem("No")
  inCompetitionQ.setWidth("50px")
  layout.addComponent(inCompetitionQ, "inCompetitionQ")

  val inCompetitionQWrong = new Label("<---", ContentMode.HTML)
  inCompetitionQWrong.setSizeUndefined()
  layout.addComponent(inCompetitionQWrong, "inCompetitionQWrong")
  inCompetitionQWrong.setVisible(false)

  val numRacesQ = new ComboBox()
  numRacesQ.addItem("7")
  numRacesQ.addItem("8")
  numRacesQ.addItem("9")
  numRacesQ.setWidth("50px")
  layout.addComponent(numRacesQ, "numRacesQ")

  val numRacesQWrong = new Label("<---", ContentMode.HTML)
  numRacesQWrong.setSizeUndefined()
  layout.addComponent(numRacesQWrong, "numRacesQWrong")
  numRacesQWrong.setVisible(false)

  val numProj2WorkDaysQ = new ComboBox()
  numProj2WorkDaysQ.addItem("X")
  numProj2WorkDaysQ.addItem("Y")
  numProj2WorkDaysQ.setWidth("50px")
  layout.addComponent(numProj2WorkDaysQ, "numProj2WorkDaysQ")

  val numProj2WorkDaysQWrong = new Label("<---", ContentMode.HTML)
  numProj2WorkDaysQWrong.setSizeUndefined()
  layout.addComponent(numProj2WorkDaysQWrong, "numProj2WorkDaysQWrong")
  numProj2WorkDaysQWrong.setVisible(false)

  val levelOfCarQ = new ComboBox()
  levelOfCarQ.addItem("X")
  levelOfCarQ.addItem("Y")
  levelOfCarQ.setWidth("50px")
  layout.addComponent(levelOfCarQ, "levelOfCarQ")

  val levelOfCarQWrong = new Label("<---", ContentMode.HTML)
  levelOfCarQWrong.setSizeUndefined()
  layout.addComponent(levelOfCarQWrong, "levelOfCarQWrong")
  levelOfCarQWrong.setVisible(false)

  val submit = new Button("Submit", new ClickListener {
    def buttonClick(event: ClickEvent) {
      if (controlQsCorrect()) {
        viewMgr.send(WaitingInLobbyPage(plr))
      } else {
        Notification.show("You have some control questions incorrect.",
          Notification.Type.TRAY_NOTIFICATION)
      }
    }
  })
  submit.setSizeUndefined()
  submit.addStyleName("btn btn-primary")
  layout.addComponent(submit, "submit")

  def controlQsCorrect(): Boolean = {
    var ret = true
    if (inCompetitionQ.getValue != "No") {
      ret = false
      inCompetitionQWrong.setVisible(true)
    } else
      inCompetitionQWrong.setVisible(false)
    if (numRacesQ.getValue != "8") {
      ret = false
      numRacesQWrong.setVisible(true)
    } else
      numRacesQWrong.setVisible(false)
    if (numProj2WorkDaysQ.getValue != "X") {
      ret = false
      numProj2WorkDaysQWrong.setVisible(true)
    } else
      numProj2WorkDaysQWrong.setVisible(false)
    if (levelOfCarQ.getValue != "Y") {
      ret = false
      levelOfCarQWrong.setVisible(true)
    } else
      levelOfCarQWrong.setVisible(false)
    ret
  }

  val gauge = new Gauge("part1GaugeA", 2, 10, 4, 6, 1, isReversed = false)

  layout.addComponent(gauge, "gauge")

  if (Lobby.settings.testing_autoControlQuestions) {
    import scala.concurrent.duration._
    val as: ActorSystem = Lobby.system
    import as.dispatcher

    Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay milliseconds) {
      val lock = this.getUI.getSession.getLockInstance
      lock.lock()
      try {
        viewMgr.send(WaitingInLobbyPage(plr))
      } finally {
        lock.unlock()
      }
    }
  }
}
