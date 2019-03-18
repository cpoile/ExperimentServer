package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.FinishedReadingInfo
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.event.ShortcutAction
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.shared.ui.label.ContentMode
import akka.actor.ActorSystem

class A_FinishedWorkDaysWnd(player: PlayerInfo) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("basicWnd")
  val layout = new CustomLayout("main/a_FinishedWorkDaysLayout")
  setContent(layout)

  val title = new Label("Finished this month", ContentMode.HTML)
  layout.addComponent(title, "title")

  val info = new Label("", ContentMode.HTML)
  info.setValue("<p>You have used all of your free days for this month, good work.</p> <p>You must now send the " +
    "F1 car to your partner. Your partner will then complete your team's F1 car for the race.</p>" +
    "<p>You can communicate with your partner while they work, but you have no other actions this month.</p>")
  layout.addComponent(info, "info")

  val okButton = new Button("Send F1 car to my partner", new ClickListener {
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
