package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import jsExtensions._
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic._
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.CustomLayout
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.shared.ui.slider.SliderOrientation
import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.data.Property
import scalaz._
import Scalaz._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import util.Random
import akka.actor.ActorSystem
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.WorkOn
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.RegisterTestingCallback
import ca.usask.chdp.ExpServerCore.Models.MsgData
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.WorkOnProj2
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import scala.concurrent.duration._

class WorkingView(val stateBean: UIState, val player: PlayerInfo) extends CustomComponent with UIRefreshable {
  val as: ActorSystem = Lobby.system

  import as.dispatcher

  // The JSViewControl will allow us to communicate with the JavaScript part of the UI.
  val jsViewControl = new JSViewControl
  jsViewControl.extend(this)

  var isPersProj2Enabled = false

  val layoutFile = player.role match {
    case "RoleA" => "main/workingLayoutA"
    case "RoleB" => "main/workingLayoutB"
  }
  val layout = new CustomLayout(layoutFile)

  var fields = Array(("daysLeft", "daysLeft"), "part1Name", "part1CurData", "part1Goal", "part1NextData",
    "part1Next", "part1Chance", "goal1Reached",
    "part2Name", "part2Next", "part2Chance", "goal2Reached",
    "part3Name", "part3CurData", "part3Goal", "part3NextData", "part3Next", "part3Chance",
    "goal3Reached", "trackNum")

  println("player is: " + player.role)
  if (player.role == "RoleA") // they have one more label they see that B doesn't.
    fields = fields :+("daysLeft", "daysLeft2")

  // And make sure to remove red tooltip when the goal is reached for each part.
  registerCustomUpdate("goal1Reached", value => {
    setGoalReached(value, 1)
    if (value.asInstanceOf[Boolean] && !isPersProj2Enabled) {
      mapOfButtons("part1Work").setEnabled(false)
    }
    //    // also, a way to prevent button from reactivating... Big hack.
    //    // FUTURE: fixme.
    //    if (value.asInstanceOf[Boolean] && !isPersProj2Enabled) {
    //      Lobby.system.scheduler.scheduleOnce(500 milliseconds) {
    //        mapOfButtons("part1Work").setEnabled(false)
    //      }
    //    }
  })
  registerCustomUpdate("goal2Reached", value => {
    setGoalReached(value, 2)
    if (value.asInstanceOf[Boolean] && !isPersProj2Enabled) {
      mapOfButtons("part2Work").setEnabled(false)
    }
    //    if (value.asInstanceOf[Boolean] && !isPersProj2Enabled) {
    //      Lobby.system.scheduler.scheduleOnce(500 milliseconds) {
    //        mapOfButtons("part2Work").setEnabled(false)
    //      }
    //    }
  })
  registerCustomUpdate("goal3Reached", value => {
    setGoalReached(value, 3)
    if (value.asInstanceOf[Boolean] && !isPersProj2Enabled) {
      mapOfButtons("part3Work").setEnabled(false)
    }
    //    if (value.asInstanceOf[Boolean] && !isPersProj2Enabled) {
    //      Lobby.system.scheduler.scheduleOnce(500 milliseconds) {
    //        mapOfButtons("part3Work").setEnabled(false)
    //      }
    //    }
  })
  def setGoalReached(value: Any, partNum: Int) {
    val reached = value.toString.toBoolean
    if (reached) jsViewControl.setGoalReachedForPart(partNum, reached)
  }

  //    ("part1CurData", "part1CurData_2"), "part1Goal", "goal1Reached",
  //    "part1Next", "part1NextData", "part1Chance", ("part1Next", "part1Next_2"), "part1StatusBar",
  //    "part2Name", ("part2CurData", "part2CurData_2"), "part2Goal", "goal2Reached",
  //    "part2Next", "part2NextData", "part2Chance", ("part2Next", "part2Next_2"), "part2StatusBar",
  //    "part3Name", ("part3CurData", "part3CurData_2"), "part3Goal", "goal3Reached",
  //    "part3Next", "part3NextData", "part3Chance", ("part3Next", "part3Next_2"), "part3StatusBar")

  bindMyFieldsToHtml(layout, fields: _*)

  // part2CurData, part2Goal, part2NextData are displayed differently, with a decimal place
  bindFieldWithCustomUpdate(layout, "part2CurData", "part2CurData", (value) => {
    mapOfLabels("part2CurData").setValue("%.1f".format(value.asInstanceOf[Int].toDouble / 10))
  })
  bindFieldWithCustomUpdate(layout, "part2Goal", "part2Goal", (value) => {
    mapOfLabels("part2Goal").setValue("%.1f".format(value.asInstanceOf[Int].toDouble / 10))
  })
  bindFieldWithCustomUpdate(layout, "part2NextData", "part2NextData", (value) => {
    mapOfLabels("part2NextData").setValue("%.1f".format(value.asInstanceOf[Int].toDouble / 10))
  })

  bindButtonToHtml(layout, "part1Work", "work", { workOnPart("Part1", "part1Work") })
  mapOfButtons("part1Work").setPrimaryStyleName("work")
  bindButtonToHtml(layout, "part2Work", "work", { workOnPart("Part2", "part2Work") })
  mapOfButtons("part2Work").setPrimaryStyleName("work")
  bindButtonToHtml(layout, "part3Work", "work", { workOnPart("Part3", "part3Work") })
  mapOfButtons("part3Work").setPrimaryStyleName("work")

  // For the tutorial process. A guider will be attached to this id, and id will be used to count clicks.
  mapOfButtons("part1Work").setId("tut_WorkButton1")
  mapOfButtons("part2Work").setId("tut_WorkButton2")
  mapOfButtons("part3Work").setId("tut_WorkButton3")

  val part1StatusBar = new Label("", ContentMode.HTML)
  part1StatusBar.setSizeUndefined()
  layout.addComponent(part1StatusBar, "part1StatusBar")

  val part2StatusBar = new Label("", ContentMode.HTML)
  part2StatusBar.setSizeUndefined()
  layout.addComponent(part2StatusBar, "part2StatusBar")

  val part3StatusBar = new Label("", ContentMode.HTML)
  part3StatusBar.setSizeUndefined()
  layout.addComponent(part3StatusBar, "part3StatusBar")
  val statusBarMap = Map[String, Label](
    "Part1" -> part1StatusBar,
    "Part2" -> part2StatusBar,
    "Part3" -> part3StatusBar)
  // Ad for the actual status:
  registerCustomUpdate("part1StatusBar", (value) => {
    log.debug("statusBar1 status was: {}", value.toString)
    part1StatusBar.setValue(value.toString)
    // TODO: Testing -- with the way it is now, button will pop back on. Why would I do that???
    //    mapOfButtons("part1Work").setEnabled(true)
    Lobby.system.scheduler.scheduleOnce(5000 milliseconds) {
      part1StatusBar.setValue("")
    }
  })
  registerCustomUpdate("part2StatusBar", (value) => {
    log.debug("statusBar2 status was: {}", value.toString)
    part2StatusBar.setValue(value.toString)
    //    mapOfButtons("part2Work").setEnabled(true)
    Lobby.system.scheduler.scheduleOnce(5000 milliseconds) {
      part2StatusBar.setValue("")
    }
  })
  registerCustomUpdate("part3StatusBar", (value) => {
    log.debug("statusBar3 status was: {}", value.toString)
    part3StatusBar.setValue(value.toString)
    //    mapOfButtons("part3Work").setEnabled(true)
    Lobby.system.scheduler.scheduleOnce(5000 milliseconds) {
      part3StatusBar.setValue("")
    }
  })

  def workOnPart(partNum: String, workButFieldName: String) {
    statusBarMap(partNum).setValue("Working...")
    if (Lobby.settings.testingMode && Lobby.settings.testing_skipStatusBarDelay) {
      player.playerLogic ! WorkOn(partNum)
    } else {
      Lobby.system.scheduler.scheduleOnce(300 milliseconds) {
        statusBarMap(partNum).setValue("Working...Working...")
      }

      Lobby.system.scheduler.scheduleOnce(600 milliseconds) {
        statusBarMap(partNum).setValue("")
        player.playerLogic ! WorkOn(partNum)
      }
    }
  }

  /**
   * Gauge for Part1
   */
  val part1GaugeA = new Gauge("part1GaugeA", stateBean.partsStartEnd("Part1")._1,
    stateBean.partsStartEnd("Part1")._1, stateBean.part1Goal, stateBean.partsStartEnd("Part1")._2,
    Lobby.settings.amtOfChangePart1, isReversed = false)
  val part1GaugeB = new Gauge("part1GaugeB", stateBean.partsStartEnd("Part1")._1,
    stateBean.part1Goal, stateBean.part1Goal, stateBean.partsStartEnd("Part1")._2,
    Lobby.settings.amtOfChangePart1, isReversed = false)
  layout.addComponent(part1GaugeA, "part1GaugeA")
  layout.addComponent(part1GaugeB, "part1GaugeB")

  registerCustomUpdate("part1CurData", value => {
    part1GaugeA.setCurValue(Integer.parseInt(value.toString))
  })

  /**
   * Gauge for Part2
   */
  val part2GaugeA = new Gauge("part2GaugeA", stateBean.partsStartEnd("Part2")._1,
    stateBean.part2CurData, stateBean.part2Goal, stateBean.partsStartEnd("Part2")._2,
    Lobby.settings.amtOfChangePart2, isReversed = true)
  val part2GaugeB = new Gauge("part2GaugeB", stateBean.partsStartEnd("Part2")._1,
    stateBean.part2Goal, stateBean.part2Goal, stateBean.partsStartEnd("Part2")._2,
    Lobby.settings.amtOfChangePart2, isReversed = true)
  //println("creating part2GaugeB with start, cur, goal, end: " + stateBean.partsStartEnd("Part2")._1 + " , " +
  //  stateBean.part2Goal + " , " + stateBean.part2Goal + " , " + stateBean.partsStartEnd("Part2")._2)
  layout.addComponent(part2GaugeA, "part2GaugeA")
  layout.addComponent(part2GaugeB, "part2GaugeB")

  registerCustomUpdate("part2CurData", value => {
    part2GaugeA.setCurValue(Integer.parseInt(value.toString))
  })

  /**
   * Gauge for Part3
   */
  val part3GaugeA = new Gauge("part3GaugeA", stateBean.partsStartEnd("Part3")._1,
    stateBean.part3CurData, stateBean.part3Goal, stateBean.partsStartEnd("Part3")._2,
    Lobby.settings.amtOfChangePart3, isReversed = false)
  val part3GaugeB = new Gauge("part3GaugeB", stateBean.partsStartEnd("Part3")._1,
    stateBean.part3Goal, stateBean.part3Goal, stateBean.partsStartEnd("Part3")._2,
    Lobby.settings.amtOfChangePart3, isReversed = false)
  layout.addComponent(part3GaugeA, "part3GaugeA")
  layout.addComponent(part3GaugeB, "part3GaugeB")

  registerCustomUpdate("part3CurData", value => {
    part3GaugeA.setCurValue(Integer.parseInt(value.toString))
  })

  /**
   * Update all gauges when their mins and maxes change (on a new round).
   *
   * NOTE: remember that custom updates are called on every update. They do not get checked
   * to see if there was a change. That is up to the customUpdate function.
   */
  registerCustomUpdate("partsStartEnd", value => {
    part1GaugeA.setBegValue(value.asInstanceOf[Map[String, (Int, Int)]]("Part1")._1)
    part1GaugeA.setEndValue(value.asInstanceOf[Map[String, (Int, Int)]]("Part1")._2)
    part2GaugeA.setBegValue(value.asInstanceOf[Map[String, (Int, Int)]]("Part2")._1)
    part2GaugeA.setEndValue(value.asInstanceOf[Map[String, (Int, Int)]]("Part2")._2)
    part3GaugeA.setBegValue(value.asInstanceOf[Map[String, (Int, Int)]]("Part3")._1)
    part3GaugeA.setEndValue(value.asInstanceOf[Map[String, (Int, Int)]]("Part3")._2)
  })

  /**
   * And update the second gauge's curValue when their goal's change (also on a new round.
   */
  registerCustomUpdate("part1Goal", value => {
    part1GaugeB.setCurValue(Integer.parseInt(value.toString))
  })
  registerCustomUpdate("part2Goal", value => {
    part2GaugeB.setCurValue(Integer.parseInt(value.toString))
  })
  registerCustomUpdate("part3Goal", value => {
    part3GaugeB.setCurValue(Integer.parseInt(value.toString))
  })

  /**
   * Message for A to reduce confusion after they are don emonth of work.
   */
  val waitingForPartnerMsg = new Label("", ContentMode.HTML)
  waitingForPartnerMsg.setSizeUndefined()
  waitingForPartnerMsg.setValue("You are now waiting for your teammate to finish their month of " +
    "work (they are the Second Engineer). Your task is finished until the race begins.")
  waitingForPartnerMsg.setVisible(false)
  waitingForPartnerMsg.addStyleName("playerWaitingMsgA")
  layout.addComponent(waitingForPartnerMsg, "waitingForPartnerMsg")

  setCompositionRoot(layout)

  var haveDisplayedTooltip = false

  def allowPersProject() {
    isPersProj2Enabled = true
    // TODO: Is this needed now?
//    Lobby.system.scheduler.scheduleOnce(2000 milliseconds) {
      mapOfButtons("part1Work").setEnabled(true)
      mapOfButtons("part2Work").setEnabled(true)
      mapOfButtons("part3Work").setEnabled(true)
//    }

    jsViewControl.allowPersProject(true)
    if (stateBean.curRound == 0 && !haveDisplayedTooltip) {
      haveDisplayedTooltip = true
      layout.getUI.getPage.getJavaScript.execute("$('span.conceptCar').removeClass('notActive');$('span.conceptCar').attr('onMouseOver','$(\"span.conceptCar\").addClass(\"notActive\")');")
    }
  }
  def aWaitingForB(waiting: Boolean, isTutorial: Boolean = false) {
    jsViewControl.aWaitingForB(waiting)
    waitingForPartnerMsg.setVisible(true)
    if (isTutorial)
      layout.getUI.getPage.getJavaScript.execute("guider.next();")
  }
  def startWorkPhase() {

    jsViewControl.allowPersProject(false)
    jsViewControl.aWaitingForB(false)
    for (i <- 1 to 3)
      jsViewControl.setGoalReachedForPart(i, false)

    /*
        sync {
          layout.getUI.getPage.getJavaScript.addFunction("chdp.session.graphInitialized",
            new JavaScriptFunction() {
              def call(arguments: JSONArray) {
                val initialized = arguments.getBoolean(0)
                //Notification.show("Set graph to initialized state of: " + initialized)
                proj2Graph foreach (_.graphInitialized(initialized))
              }
            })
        }
    */

    testIfTesting()
  }
  def finishedWorkPhase() {
    sync {
      proj2Graph foreach (_.removeGraph())
    }
  }

  /**
   * Chat Messages
   */
  val messageList = addRightSideBarTo(layout, player, stateBean.curRound)
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
      messageList.layout.addComponent(chatLine)
      sync {
        layout.getUI.scrollIntoView(chatLine)
      }
    }
  }

  /**
   *
   * Project 2 section.
   * Only for Player A.
   */
  var proj2Graph: Option[FlotGraph] = None
  if (player.role == "RoleA") {
    addPersProject()
  }

  def addPersProject() {
    // TODO: turn sliderValue into a permanent tooltip.
    //    val sliderValue = new Label
    //    sliderValue.setSizeUndefined()

    slider.addStyleName("c-proj2Slider")
    slider.setOrientation(SliderOrientation.HORIZONTAL)
    slider.setMin(0)
    slider.setMax(0)
    slider.setResolution(0)
    slider.setImmediate(true)
    layout.addComponent(slider, "slider")

    val sliderTooltip = new BootstrapTooltip(".c-proj2Slider .v-slider-base", ".c-proj2Slider")
    layout.addComponent(sliderTooltip, "sliderTooltip")

    slider.addValueChangeListener(new ValueChangeListener {
      def valueChange(event: Property.ValueChangeEvent) {
        sliderTooltip.setValue(slider.getValue.toInt.toString)
      }
    })
    sliderTooltip.setValue("0")


    bindButtonToHtml(layout, "workOnProj2", "work", {
      player.playerLogic ! WorkOnProj2(slider.getValue.toInt)
    })
    mapOfButtons("workOnProj2").setPrimaryStyleName("work")
    mapOfButtons("workOnProj2").setId("tut_WorkButtonProj2")

    registerCustomUpdate("proj2SliderMax", value => {
      val newMax = Integer.parseInt(value.toString)
      slider.setMax(newMax)
    })
    //    layout.addComponent(sliderValue, "sliderValue")

    // Make the graph.
    proj2Graph = (new FlotGraph).some
    println("Making graph: " + proj2Graph.get)
    registerCustomUpdate("proj2DaysWorked", value => {
      val newMap = value.asInstanceOf[Map[Int, Int]]
      proj2Graph.get.updateSeries(newMap)
    })
    layout.addComponent(proj2Graph.get, "proj2GraphContainer")
  }

  val oldjsCmd = "$('body').css({background : '#45504a', background : '-moz-linear-gradient(top, #45504a 0%, #899387 35%, #e2e4e1 100%)', background : '-webkit-gradient(linear, left top, left bottom, color-stop(0%, #45504a), color-stop(35%, #899387), color-stop(100%, #e2e4e1))', background : '-webkit-linear-gradient(top, #45504a 0%, #899387 35%, #e2e4e1 100%)', background : '-o-linear-gradient(top, #45504a 0%, #899387 35%, #e2e4e1 100%)', background : '-ms-linear-gradient(top, #45504a 0%, #899387 35%, #e2e4e1 100%)', background : 'linear-gradient(to bottom, #45504a 0%, #899387 35%, #e2e4e1 100%)'})"

  val jsCmd = "$('body').css({background : '#45504a', background:'url(/_pix/bg.png) repeat,-webkit-gradient(linear,left top,left bottom,color-stop(0,#45504A),color-stop(1,#e2e4e1))', background:'url(/_pix/bg.png) repeat,-webkit-linear-gradient(top,#45504A 0%,#e2e4e1 100%)',background:'url(/_pix/bg.png) repeat,-moz-linear-gradient(top,#45504A 0%,#e2e4e1 100%)',background:'url(/_pix/bg.png) repeat,-ms-linear-gradient(top,#45504A 0%,#e2e4e1 100%)',background:'url(/_pix/bg.png) repeat,-o-linear-gradient(top,#45504A 0%,#e2e4e1 100%)', background:'url(/_pix/bg.png) repeat,linear-gradient(top,#45504A 0%,#e2e4e1 100%)', 'background-attachment':'fixed'})"
  override def attach() {
    super.attach()
    this.getUI.getPage.getJavaScript.execute("$('body').removeClass('loginView');")
    this.getUI.getPage.getJavaScript.execute(jsCmd)
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

  def testIfTesting() {
    if (Lobby.settings.testing_autoWork) {
      println("Registering testingcallback for " + player.role)
      player.playerLogic ! RegisterTestingCallback(TestingPhase.WorkingPhase, () => {
        Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay milliseconds) { doWork() }
      })
      println("Initial schedule for " + player.role)
      Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay milliseconds) { doWork() }
    }
  }

  def debugMsg(action: String) {
    log.debug("**AUTOWORK*** Role -- {}  --- {}  Action: --- {}",
      player.role, player.globalId, action)
  }
  def doWork() {
    val lock = this.getUI.getSession.getLockInstance
    lock.lock()
    try {
      var posChoices = List.empty[() => Unit]
            if (mapOfButtons("part1Work").isEnabled)
      posChoices = (() => {player.playerLogic ! WorkOn("Part1"); debugMsg("Part1Work") }) :: posChoices
            if (mapOfButtons("part2Work").isEnabled)
      posChoices = (() => {player.playerLogic ! WorkOn("Part2"); debugMsg("Part2Work") }) :: posChoices
            if (mapOfButtons("part3Work").isEnabled)
      posChoices = (() => {player.playerLogic ! WorkOn("Part3"); debugMsg("Part3Work") }) :: posChoices
      if (player.role == "RoleA") {
        if (mapOfButtons("workOnProj2").isEnabled)
          posChoices = (() => {player.playerLogic ! WorkOnProj2(1); debugMsg("Proj2Work") }) :: posChoices
      }
      if (posChoices.length > 0) {
        posChoices = Random.shuffle(posChoices)
        posChoices(0)()
      }
    } finally {
      lock.unlock()
    }
  }
}

