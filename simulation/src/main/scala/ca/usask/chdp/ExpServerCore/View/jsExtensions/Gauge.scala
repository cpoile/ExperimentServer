package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.GaugeState
import com.vaadin.ui.AbstractJavaScriptComponent
import com.vaadin.annotations.JavaScript
import scalaz._
import Scalaz._

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "gauge.min.js", "gauge_connector.js"))
//class Gauge(id: String, subtitle: String, params: Map[String, AnyRef],
//             highlights: Array[Map[String, AnyRef]],
//             majorTicks: Array[String]) extends AbstractJavaScriptComponent {
class Gauge(id: String, begValue: Int, curValue: Int, personalGoal: Int,
            endValue: Int, incrementAmt: Int, isReversed: Boolean) extends AbstractJavaScriptComponent {

  //  val log = LoggerFactory.getLogger("Gauge")
  //  this.setPrimaryStyleName("c-gaugeWidget")
  //

  // We start with these default values, but they may change in the next round (probably will)
  var (rawBeg, rawCur, rawEnd, rawPersGoal) = (begValue, curValue, endValue, personalGoal)
  // these hold our state in steps.
  var begStep, curStep, endStep, goalStep = 0
  var curColor = ""

  def setCurValue(value: Int) {
    //    if (id.startsWith("part2"))
    //      log.error("gauge: {} has value of {}", id, value)
    rawCur = value
    update()
  }
  def setBegValue(value: Int) {
    rawBeg = value
    update()
  }
  def setPersGoal(value: Int) {
    rawPersGoal = value
    update()
  }
  def setEndValue(value: Int) {
    rawEnd = value
    update()
  }
  def update() {
    val res = redefineState()
    updateState()
  }

  override def getState = super.getState.asInstanceOf[GaugeState]

  // should only be called once.
  override def createState() = {
    // initialize our state.
    redefineState()
    new GaugeState(id, curStep, endStep, curColor, isReversed)
  }

  //    params.asJava, for (hl <- highlights) yield hl.asJava,
  //    majorTicks, subtitle, id, params("begValue").asInstanceOf[Int])

  /**
   * Given a change in state, redefine:
   * minimum -- will be the starting value. In steps, it will be 1.
   * maximum -- will be the maxValue. In steps it will be maxValue - begValue / stepValue
   */
  /**
   *
   * @return (minStep, curStep, maxStep, color)
   */
  def redefineState() {
    import math.abs
    val numSteps = abs(rawEnd - rawBeg) / abs(incrementAmt)
    endStep = numSteps
    curStep = (abs(rawCur - rawBeg) / abs(incrementAmt))
    goalStep = abs(rawPersGoal - rawBeg) / abs(incrementAmt)

    // to get colour, find percentage finished, where max is persGoal (green)
    // if over max, use 1.
    val percCompl: Float = (curStep >= goalStep) ? 1f | (curStep * 1f / goalStep)
    curColor = getColorBetweenRedAndGreen(percCompl)

    //println(id + " --- cur: " + curStep + " goal: " + goalStep + " percGreen: " + percCompl + " curColor: " + curColor)
  }
  def updateState() {
    // Now, update if I need to.
    if (getState.curValue != curStep) {
      getState.curValue = curStep
      //print(id + " -- curStep updated to: " + curStep)
    }
    // endstep is where B's goal is. For a reversed gauge, it is NOT the maxValue.
    // Reversed gauge's max value is the (rawBeg - rawEnd) / inc
    // But-- this is calculated above and put into numSteps, and into endStep.
    if (getState.maxValue != endStep) {
      getState.maxValue = endStep
      //print(id + " -- maxValue updated to: " + endStep)
    }
    if (getState.rgbColor != curColor) {
      getState.rgbColor = curColor
      //print(id + " -- rgbColor updated to: " + curColor)
    }
  }
  def getColorBetweenRedAndGreen(percGreen: Float): String = {
    import java.awt._
    // I want it to be green if 1, but scale the other colors more towards red, therefore real percent *.8
    val scalingFactor = 0.6f
    // Hue --- see http://stackoverflow.com/questions/340209/generate-colors-between-red-and-green-for-a-power-meter
    val h: Float = (percGreen == 1) ? (percGreen * 0.4f) | (percGreen * scalingFactor * 0.4f)

    val (s, b) = (0.9f, 0.9f) // saturation, brightness
    val clr = Color.getHSBColor(h, s, b)
    "#%06X".format(0xFFFFFF & clr.getRGB)
  }
}
