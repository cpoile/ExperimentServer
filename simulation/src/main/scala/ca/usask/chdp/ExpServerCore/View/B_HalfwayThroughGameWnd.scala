package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.event.ShortcutAction
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.shared.ui.label.ContentMode
import akka.actor.ActorSystem

class B_HalfwayThroughGameWnd(player: PlayerInfo, manipulation: Int) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("basicWndWithPicture")
  val layout = if (manipulation == 1)
    new CustomLayout("main/b_HalfwayThroughGameLayoutManip1")
  else
    new CustomLayout("main/b_HalfwayThroughGameLayoutManip2")

  setContent(layout)

  val title = new Label("Warning -- New Information From Managers", ContentMode.HTML)
  layout.addComponent(title, "title")

  val info = new Label("", ContentMode.HTML)
  val customMsg = if (manipulation == 1) {
    "<p>New rules and regulations have been put in place. Teams are not allowed to use custom electronic control units in the cars.</p>" +
    "This means your performance targets have been <strong>lowered</strong>. Now, each component " +
      "you work on has a <strong>lower</strong> goal. In other words, your goals are easier to reach.</p>" +
      "<p>Now you don't need your teammate to work as many days on their parts, " +
      "and your team will still have a good chance of winning races.</p>"
  } else {
    "<p>New rules and regulations have been put in place. Teams are not allowed to use custom electronic control units in the cars.</p>" +
      "This means your performance targets have been <strong>raised</strong>. Now, each component " +
      "you work on has a <strong>higher</strong> goal. In other words, your goals are harder to reach.</p>" +
      "<p>Now you need your teammate to work harder on their parts " +
      "if you want your team to still have a good chance of winning races.</p>"
  }
  info.setValue(customMsg)
  layout.addComponent(info, "info")

  val okButton = new Button("Continue", new ClickListener {
    def buttonClick(event: ClickEvent) {
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
  if (false && Lobby.settings.testingMode && Lobby.settings.testing_autoClickWindows) {
    import scala.concurrent.duration._
    val as: ActorSystem = Lobby.system
    import as.dispatcher

    Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay milliseconds) {
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
