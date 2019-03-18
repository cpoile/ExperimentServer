package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.TooltipGuiderState
import com.vaadin.annotations.{StyleSheet, JavaScript}
import org.slf4j.LoggerFactory
import com.vaadin.server.{AbstractClientConnector, AbstractJavaScriptExtension}
import scala.collection.JavaConversions._

@StyleSheet(Array("guider.css", "tutA5.css"))
@JavaScript(Array("qualifyURL.js", "underscore.js", "tooltipGuider_connector.js", "tooltipGuider_tutorialA.js", "tooltipGuider_tutorialB.js", "tooltipGuider_animation.js", "http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "guider.js"))
class TooltipGuider(ttgName: String) extends AbstractJavaScriptExtension {
  getState().ttgName = ttgName
  getState().step = 0
  // start with no tooltip.

  val log = LoggerFactory.getLogger("TooltipGuider")

  def initGuiders() {
    callFunction("initGuiders")
  }
  def start(testing: Boolean = false) {
    if (testing) {
      callFunction("testing_doAutoWork_" + ttgName)
    } else
      moveToStep(1)
  }

  def moveToStep(num: Int) {
    callFunction("gotoStep", Array(num))
  }
  def callJSFunction(fn: String, args: String*) {
    log.debug("Calling JS function: " + fn)
    callFunction(fn, args: _*)
  }
  override def getState = super.getState.asInstanceOf[TooltipGuiderState]
  override def extend(target: AbstractClientConnector) { super.extend(target) }

  // Not using:
  def setViewSettings(settings: Map[String, AnyRef]) {
    getState.setViewSettings(settings)
  }
}

