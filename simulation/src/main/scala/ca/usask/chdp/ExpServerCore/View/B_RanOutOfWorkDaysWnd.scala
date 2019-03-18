package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.FinishedReadingInfo
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.event.ShortcutAction
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.annotations.JavaScript
import akka.actor.ActorSystem

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "jsExtensions/qualifyURL.js"))
class B_RanOutOfWorkDaysWnd(catPicNum: Int, shortfall: Int, player: PlayerInfo) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("basicWndWithPicture")
  val layout = new CustomLayout("main/b_RanOutOfWorkDaysLayout")
  setContent(layout)

  val title = new Label("You ran out of work days", ContentMode.HTML)
  layout.addComponent(title, "title")

  val info = new Label("", ContentMode.HTML)
  val msg = "<p>Sadly, you were unable to reach your team manager's goals. They are dissapointed.</p>" +
    "<p>After reviewing the month's work, your managers estimate that you needed about <u><b>" +
    shortfall + "</b></u> more days of work to reach the team goals. They're blaming you for the missed " +
    "goal.</p>" +
    "<p>You have a feeling that if your partner had done a bit more work you would have made your " +
    "target.</p> " +
    "<p>Your team isn't looking forward to the next race, as the car isn't as fast as they wanted. But " +
    "miracles do happen...</p>"
  info.setValue(msg)
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

//  // which cat pic are we at?
//  val catPicSrc = "/VAADIN/themes/expserver/images/working/sadcat" + catPicNum + ".jpg"
//  override def attach() {
//    super.attach()
//    sync {
//      this.getUI.getPage.getJavaScript.execute("$('#catImg').attr('src',qualifyURL('" + catPicSrc + "'));")
//    }
//  }

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
