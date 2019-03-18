package ca.usask.chdp.ExpServerCore.Tutorial

import com.vaadin.ui.{JavaScriptFunction, Button, CustomLayout, CustomComponent}
import ca.usask.chdp.ExpServerCore.View.jsExtensions.TooltipGuider
import com.vaadin.server.ClientConnector.{AttachEvent, AttachListener}
import org.json.JSONArray
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo

trait WatchingRaceTutorialView {
  this: CustomComponent =>

  val layout: CustomLayout
  val startButton: Button
  val player: PlayerInfo

  val tooltipGuide = player.role match {
    case "RoleA" => new TooltipGuider("tutorialARaceView")
    case _ => new TooltipGuider("tutorialBRaceView")
  }
  tooltipGuide.extend(this)
  this.addAttachListener(new AttachListener {
    def attach(event: AttachEvent) {
      registerTutorialCallbacks()
      tooltipGuide.initGuiders()
      tooltipGuide.start()
      startButton.setEnabled(false)
    }
  })

  def registerTutorialCallbacks() {
    layout.getUI.getPage.getJavaScript.addFunction("chdp_WatchingRaceTutorialView_enableStartButton", new JavaScriptFunction {
      def call(arguments: JSONArray) {
        startButton.setEnabled(true)
      }
    })
  }
}
