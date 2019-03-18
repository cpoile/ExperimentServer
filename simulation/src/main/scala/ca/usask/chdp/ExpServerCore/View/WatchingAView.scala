package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.annotations.JavaScript
import ca.usask.chdp.ExpServerCore.Models.MsgData
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.ui.CustomLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.shared.ui.label.ContentMode
import jsExtensions.JSViewControlWatchingA
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.RaceResults

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class WatchingAView(val stateBean: UIState, val player: PlayerInfo) extends CustomComponent with UIRefreshable {

  // The JSViewControl will allow us to communicate with the JavaScript part of the UI.
  val jsViewControl = new JSViewControlWatchingA
  jsViewControl.extend(this)

  val layout = new CustomLayout("main/watchingALayout")
  setCompositionRoot(layout)

  val fields = Array("daysLeft", "part1CurData", "part1Goal", "part3CurData", "part3Goal", "trackNum")
  bindMyFieldsToHtml(layout, fields: _*)

  // part2CurData, part2Goal, part2NextData are displayed differently, with a decimal place
  bindFieldWithCustomUpdate(layout, "part2CurData", "part2CurData", (value) => {
    mapOfLabels("part2CurData").setValue("%.1f".format(value.asInstanceOf[Int].toDouble / 10))
  })
  bindFieldWithCustomUpdate(layout, "part2Goal", "part2Goal", (value) => {
    mapOfLabels("part2Goal").setValue("%.1f".format(value.asInstanceOf[Int].toDouble / 10))
  })

  /**
   * Setup a custom alert when partner upgrades a part.
   */
  private[this] val prevPartVal = collection.mutable.Map("Part1" -> stateBean.part1CurData,
    "Part2" -> stateBean.part2CurData, "Part3" -> stateBean.part3CurData)
  registerCustomUpdate("part1CurData", (value) => {
    val v = value.asInstanceOf[Int]
    if (v != prevPartVal("Part1")) {
      prevPartVal("Part1") = v
      val note = new Notification("Teammate completed work", "Your teammate upgraded the Engine, increasing Top Speed.\nThis means you need to do less work on the Air Intake System to reach your goals.", Notification.Type.TRAY_NOTIFICATION)
      sync(note.show(layout.getUI.getPage))
    }
  })
  registerCustomUpdate("part2CurData", (value) => {
    val v = value.asInstanceOf[Int]
    if (v != prevPartVal("Part2")) {
      prevPartVal("Part2") = v
      val note = new Notification("Teammate completed work", "Your teammate upgraded the Powertrain, increasing Linear Acceleration.\nThis means you need to do less work on the Wing Aerodynamics to reach your goals.", Notification.Type.TRAY_NOTIFICATION)
      sync(note.show(layout.getUI.getPage))
    }
  })
  registerCustomUpdate("part3CurData", (value) => {
    val v = value.asInstanceOf[Int]
    if (v != prevPartVal("Part3")) {
      prevPartVal("Part3") = v
      val note = new Notification("Teammate completed work", "Your teammate upgraded the Wheel Assembly, increasing Lateral Top Speed.\nThis means you need to do less work on the Suspension System to reach your goals.", Notification.Type.TRAY_NOTIFICATION)
      sync(note.show(layout.getUI.getPage))
    }
  })

  /**
   * Creating the messaging part of the page -- the rightsidebar
   */
  val messageList = addRightSideBarTo(layout, player, stateBean.curRound)

  def setupRaceResults(prevRoundRaceResults: RaceResults) {
    /**
     * Must sync, because this is being called directly by an actor... (dangerous...?)
     */
    sync {
      val addRaceTitleText = if (stateBean.curRound == 0) " (not yet run)" else ""
      val raceResultsComponent = new RaceResultsComponent(player, stateBean, prevRoundRaceResults, addRaceTitleText)
      raceResultsComponent.setSizeUndefined()
      layout.addComponent(raceResultsComponent, "raceResultsComponent")
    }
  }

  def messageReceived(msgLst: List[MsgData]) {
    for (msg <- msgLst) {
      var display = ""
      val chatLine = if (player.globalId == msg.from) {
        display = "<span class=\"msgSender\">Me: </span>"
        val lbl = new Label(display + msg.msg, ContentMode.HTML)
        lbl.setSizeUndefined()
        lbl.setPrimaryStyleName("msgLine msgFromMe")
        lbl
      } else {
        display = "<span class=\"msgSender\">Them: </span>"
        val lbl = new Label(display + msg.msg, ContentMode.HTML)
        lbl.setSizeUndefined()
        lbl.setPrimaryStyleName("msgLine msgFromThem")
        lbl
      }
      sync {
        messageList.layout.addComponent(chatLine)
        layout.getUI.scrollIntoView(chatLine)
      }
    }
  }

  val jsCmd = "$('body').css({background:'url(/_pix/bg.png) repeat,-webkit-gradient(linear,left top,left bottom,color-stop(0,#45504A),color-stop(1,#e2e4e1))', background:'url(/_pix/bg.png) repeat,-webkit-linear-gradient(top,#45504A 0%,#e2e4e1 100%)',background:'url(/_pix/bg.png) repeat,-moz-linear-gradient(top,#45504A 0%,#e2e4e1 100%)',background:'url(/_pix/bg.png) repeat,-ms-linear-gradient(top,#45504A 0%,#e2e4e1 100%)',background:'url(/_pix/bg.png) repeat,-o-linear-gradient(top,#45504A 0%,#e2e4e1 100%)', background:'url(/_pix/bg.png) repeat,linear-gradient(top,#45504A 0%,#e2e4e1 100%)', 'background-attachment':'fixed'})"
  override def attach() {
    super.attach()
    sync {
      this.getUI.getPage.getJavaScript.execute("$('body').removeClass('loginView');")
      this.getUI.getPage.getJavaScript.execute(jsCmd)
    }
  }

  def sync[R](block: => R): Option[R] = {
    var result: Option[R] = None
    layout.getUI.getSession.lock()
    try {
      result = Option(block)
    } finally {
      layout.getUI.getSession.unlock()
    }
    result
  }
}
