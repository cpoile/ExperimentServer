package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{ASentThisProjectData, FinishedReadingInfo, PlayerInfo}
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.event.ShortcutAction
import com.vaadin.shared.ui.label.ContentMode
import akka.actor.ActorSystem

class B_ASentProjWnd(player: PlayerInfo, data: ASentThisProjectData) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)

  addStyleName("B_ASentProjWnd")
  val layout = new CustomLayout("main/B_ASentProjLayout")
  layout.addStyleName("layoutMargin")
  setContent(layout)

  // make the text a bit easier to read:

  /**
   * Reports:
   */
  val engineReport = new Label("", ContentMode.HTML)
  val part1NumSteps = (data.goals.p1DataGoal - data.part1.curData)/2
  engineReport.setValue("Your partner sent you an Engine that produces <b>" + data.part1.curData + "kph</b>." +
    "This means you need to make <b>" + part1NumSteps + " upgrades</b> of the Air Intake System in " +
    "order to reach your goal of <b>" + data.goals.p1DataGoal + " kpm</b>.")
  engineReport.setWidth("330px")
  layout.addComponent(engineReport, "engineReport")

  val part2NumSteps = (data.part2.curData - data.goals.p2DataGoal)/2
  val powerTrainReport = new Label("", ContentMode.HTML)
  powerTrainReport.setValue("Your partner sent you a Powertrain that lets you reach 0-100kph in <b>" + "%.1f".format(data.part2.curData.toDouble / 10) + " sec</b>. This means you need to make <b>" + part2NumSteps +
    " upgrades</b> of the Wing Aerodynamics in order to reach your goal of a <b>" + "%.1f".format(data.goals.p2DataGoal.toDouble / 10) +
    " sec</b> 0-100 kph.")
  powerTrainReport.setWidth("330px")
  layout.addComponent(powerTrainReport, "powerTrainReport")

  val part3NumSteps = (data.goals.p3DataGoal - data.part3.curData)/2
  val wheelReport = new Label("", ContentMode.HTML)
  wheelReport.setValue("Your partner sent you a Wheel Assembly that allows your car to reach a cornering speed of <b>" + data.part3.curData +
    " kph</b>. This means you need to make <b>" + part3NumSteps + " upgrades</b> of the Suspension System to reach your goal of <b>" +
    data.goals.p3DataGoal + " kph</b>.")
  wheelReport.setWidth("330px")
  layout.addComponent(wheelReport, "wheelReport")

  val okButton = new Button("Continue", new ClickListener {
    def buttonClick(event: ClickEvent) {
      player.playerLogic ! FinishedReadingInfo
      close()
    }
  })
  okButton.setSizeUndefined()
  okButton.addStyleName("btn btn-primary")
  okButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  layout.addComponent(okButton, "okButton")

  /**
   * TESTING
   */
  if (Lobby.settings.testingMode && Lobby.settings.testing_autoClickWindows) {
    import scala.concurrent.duration._
    val as: ActorSystem = Lobby.system
    import as.dispatcher


    Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay milliseconds){
      val lock = this.getUI.getSession.getLockInstance
    lock.lock()
      try {
        println("TESTING_AUTOWORK -- Clicking finishedGoalsWnd button.")
        okButton.click()
      } finally {
        lock.unlock()
      }
    }
  }

}
