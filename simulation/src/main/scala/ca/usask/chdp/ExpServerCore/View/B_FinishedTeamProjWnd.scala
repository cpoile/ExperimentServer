package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.FinishedReadingInfo
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.event.ShortcutAction
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.shared.ui.label.ContentMode
import akka.actor.ActorSystem

class B_FinishedTeamProjWnd(daysLeftOver: Int, player: PlayerInfo) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("basicWndWithPicture")
  val layout = new CustomLayout("main/b_FinishedTeamProjLayout")
  setContent(layout)

  val title = new Label("You met your manager's goals!", ContentMode.HTML)
  layout.addComponent(title, "title")

  val info = new Label("", ContentMode.HTML)
  val msgBuilder: StringBuilder = new StringBuilder("<p>Congratulations, you were able to reach your team manager's goals.")
  val msg = if (daysLeftOver > 0 ) {
    "You even had <b>" + daysLeftOver + "</b> days left, which you used to further improve the team's car."
  } else "And just in time, too. You had <b>0</b> days left after today."
  msgBuilder ++= msg
  msgBuilder ++= "</p><p> Your team management is very pleased and can't wait to race.</p><p>Good luck!</p>"
  info.setValue(msgBuilder.mkString)

  layout.addComponent(info, "info")

  val okButton = new Button("Start Race!", new ClickListener {
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
        println("TESTING_AUTOWORK -- Clicking A_FinishedWorkDaysWnd button.")
        okButton.click()
      } finally {
        lock.unlock()
      }
    }
  }
}
