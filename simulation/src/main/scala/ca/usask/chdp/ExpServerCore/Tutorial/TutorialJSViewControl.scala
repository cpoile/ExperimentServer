package ca.usask.chdp.ExpServerCore.Tutorial

import ca.usask.chdp.ExpServerCore.View.jsExtensions.TooltipGuider
import com.vaadin.ui.{JavaScriptFunction, CustomComponent, CustomLayout}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{Tut_SendBProject, Tut_RunAForXDays, ReadyForBToDoTheirWork, PlayerInfo}
import com.vaadin.server.ClientConnector.{AttachEvent, AttachListener}
import org.json.JSONArray
import ca.usask.chdp.ExpServerCore.View.{UIState, UIRefreshable}
import scala.collection.mutable
import java.util.UUID
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpSettings
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.{A_WorkingOnProj1, B_WorkingOnProj}

trait TutorialJSViewControl {
  this: CustomComponent with UIRefreshable =>

  val layout: CustomLayout
  val player: PlayerInfo
  val stateBean: UIState
  val tooltipGuide: TooltipGuider = setupTutorial()
  var tutButtonState = mutable.Map[String, Boolean]("part1Work" -> false, "part2Work" -> false,
    "part3Work" -> false)

  this.addAttachListener(new AttachListener {
    def attach(event: AttachEvent) {
      // only handle buttons at the start for A. B will have this done later.
      if (player.role == "RoleA") registerCustomButtonHandlers()
      tooltipGuide.initGuiders()
      registerTutorialCallbacks(player.role)

      tooltipGuide.start(testing = ExpSettings.get.testing_autoTutorialWalkthrough)
    }
  })

  def setupTutorial() = {
    val ttgName = stateBean.uiCmd match {
      case cmd: A_WorkingOnProj1 => "tutorialA"
      case cmd: B_WorkingOnProj => "tutorialB_Working"
      case _ => "tutorialB_Watching"
    }
    val tttut = new TooltipGuider(ttgName)
    tttut.extend(this)
    tttut
  }

  // First, register a custom update for the work buttons so that the tutorial
  // has the final control over their state.
  def registerCustomButtonHandlers() {
    registerCustomUpdate("part1Work", (value) => updateButtons())
    registerCustomUpdate("part2Work", (value) => updateButtons())
    registerCustomUpdate("part3Work", (value) => updateButtons())
  }
  def updateButtons() {
    for ((n, b) <- tutButtonState) mapOfButtons(n).setEnabled(b)
  }

  def registerTutorialCallbacks(role: String) {
    role match {
      case "RoleA" => setupJSFunctionsForA()
      case "RoleB" => setupJSFunctionsForB()
    }
  }

  /**
   * These functions are used by both A and B.
   */
  def setupJSCallbackOnChange() {
    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_chdp_registerCallbackOnChange", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("recived registerCallbackOnChange call with arguments: {}", arguments)
        // arguments are: field name to be notified on, number of changes before calling back, callback to call
        // let the client be notified when a field is updated, then remove it after that update fires one time.
        // BUT -- only if that update is new.
        val field = arguments.optString(0, "")
        val numChanges = arguments.optInt(1, 0)
        val callback = arguments.optString(2, "")
        if (field.length > 0 && numChanges != 0 && callback.length > 0) {
          registerCallbackOnChange(field, numChanges, callback)
        }
      }
    })
  }

  def registerCallbackOnChange(field: String, numChanges: Int, jsCallback: String) {
    val id = UUID.randomUUID()
    if (!uiData.getItemPropertyIds.contains(field))
      log.error("uiData doesn't contain field: {}", field)
    else {
      var currentValue = uiData.getItemProperty(field).getValue
      var changesCount = 0
      registerCustomUpdate(field, (value) => {
        if (value != currentValue) {
          changesCount += 1
          log.debug("custom update called on field: {} -- waiting for numChanges: {} -- changesCount: {} -- curData: {} -- newValue: {}",
            Array(field, numChanges, changesCount, currentValue, value).asInstanceOf[Array[AnyRef]])
          currentValue = value
          if (changesCount >= numChanges) {
            layout.getUI.getPage.getJavaScript.execute("console.log('customUpdateField called. attempting: " + jsCallback + "'); " + jsCallback + "(); console.log('finished calling: " + jsCallback + "');")
            removeCustomUpdate(id)
          }
        }
      }, id)
    }
  }
  /**
   * These are specific to A and B
   */
  def setupJSFunctionsForA() {
    setupJSCallbackOnChange()

    // add specific proj2 button for A:
    tutButtonState = tutButtonState + ("workOnProj2" -> false)
    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_chdp_setInputStates", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("recived setInputStates call with arguments: {}", arguments)
        // arguments are: part1Work, 2, 3,
        for (i <- 1 to 3) tutButtonState("part" + i + "Work") = arguments.get(i - 1).asInstanceOf[Boolean]
        tutButtonState("workOnProj2") = arguments.get(3).asInstanceOf[Boolean]
        updateButtons()
      }
    })

    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_chdp_setWorkProj2ButtonToXDays", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("recived setWorkProj2ButtonToXDays call with arguments: {}", arguments)
        // store the previous click listener, remove it, attach new listener
        // when condition is met replace original listener
        val numDays = arguments.optInt(0, 0)
        if (numDays > 0) {
          val button = mapOfButtons("workOnProj2")
          val origListeners = mapOfButtonClickListeners("workOnProj2")
          val newListener = new ClickListener {
            def buttonClick(event: ClickEvent) {
              if (slider.getValue.toInt == numDays) {
                button.removeClickListener(this)
                for (l <- origListeners) button.addClickListener(l)
                button.setEnabled(true) // because it is set to be disabled on click.
                button.click()
              } else {
                button.setEnabled(true) // because it is set to be disabled on click.
              }
            }
          }
          for (l <- origListeners) button.removeClickListener(l)
          button.addClickListener(newListener)
          log.debug("Original listeners: {} -- new listener: {}", origListeners, newListener)
        }
      }
    })

    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_chdp_workOnProj2ForXDays", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("recived workOnProj2ForXDays call with arguments: {}", arguments)
        val numDays = arguments.optInt(0, 0)
        if (numDays > 0) {
          slider.setValue(numDays)
          mapOfButtons("workOnProj2").click()
        }
      }
    })

    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_chdp_fastForwardThroughBsWork", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("recived fastForwardThroughBsWork call with arguments: {}", arguments)
        player.playerLogic ! ReadyForBToDoTheirWork
      }
    })
  }

  def setupJSFunctionsForB() {
    setupJSCallbackOnChange()

    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_chdp_setInputStates", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("recived setInputStates call with arguments: {}", arguments)
        // arguments are: part1Work, 2, 3,
        for (i <- 1 to 3) tutButtonState("part" + i + "Work") = arguments.get(i - 1).asInstanceOf[Boolean]
        updateButtons()
      }
    })

    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_TutorialJSViewControl_RunAForXDaysOn", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("received Tut_RunAForXDays call with arguments: {}", arguments)
        val partName = arguments.optString(0, "")
        val numDays = arguments.optInt(1, 0)
        val millisDelay = arguments.optInt(2, 0)
        val callback = arguments.optString(3, "")
        if (numDays > 0 && millisDelay != 0 && callback != "") {
          registerCallbackOnChange("daysLeft", numDays, callback)
          player.playerLogic ! Tut_RunAForXDays(partName, numDays, millisDelay)
        }
      }
    })

    layout.getUI.getPage.getJavaScript.addFunction("ca_usask_TutorialJSViewControl_DoneWatchingA", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        log.debug("received Tut_SendBProject call with arguments: {}", arguments)
        player.playerLogic ! Tut_SendBProject
      }
    })
  }

  /**
   * Receive the new tutorial commands.
   * Not using.
   */
  def setViewSettings(settings: Map[String, Object]) {
    log.debug("setViewSettings called.")
    if (tooltipGuide != null) tooltipGuide.setViewSettings(settings)
  }
}
