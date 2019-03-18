package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.FinishedReadingInfo
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.shared.ui.label.ContentMode
import akka.actor.ActorSystem

class A_FinishedGoalsWnd(player: PlayerInfo) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("basicWndWithPicture")
  val layout = new CustomLayout("main/a_FinishedGoalsLayout")
  setContent(layout)

  val title = new Label("Finished F1 project goals", ContentMode.HTML)
  layout.addComponent(title, "title")


  val info = new Label("<p>You have finished the F1 goals that your managers have given you, good job!</p>" +
    "<h4>You now have two choices:</h4>" +
    "<ul><li>Continue working on your F1 team project</li>" +
    "<li>Work on your personal project, the 2014 Mercedes Concept Car.</li></ul>" +
    "<h4>Remember</h4><p>You are being rewarded only for the amount of work you do on your Concept Car project. " +
    "Your managers do not care about the F1 project and will not reward any effort spent on it.</p>", ContentMode.HTML)
  layout.addComponent(info, "info")


  val okButton = new Button("I Understand", new ClickListener {
    def buttonClick(event: ClickEvent) {
      player.playerLogic ! FinishedReadingInfo
      close()
    }
  })
  okButton.setSizeUndefined()
  okButton.addStyleName("btn btn-primary")
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
