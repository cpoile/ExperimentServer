package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui.{CustomLayout, CustomComponent}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{PlayerInfo, WorkOn}
import com.vaadin.annotations.JavaScript

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "photohover.js"))
class Proj1View(stateBean: UIState, player: PlayerInfo) extends CustomComponent with UIRefreshable {

  val layout = new CustomLayout("main/proj1Layout")
  layout.setStyleName("proj1Layout")

  //val photo = new Embedded("", new ExternalResource("http://placehold.it/900x400", "image/jpeg"))
  //layout.addComponent(photo, "photo")

  val fields = Array( "part1Name", "part1CurData",

    ("part1CurData", "part1CurData_2"), "part1Goal", "goal1Reached",
    "part1Next", "part1NextData", "part1Chance", ("part1Next", "part1Next_2"), "part1StatusBar",
    "part2Name", ("part2CurData", "part2CurData_2"), "part2Goal", "goal2Reached",
    "part2Next", "part2NextData", "part2Chance", ("part2Next", "part2Next_2"), "part2StatusBar",
    "part3Name", ("part3CurData", "part3CurData_2"), "part3Goal", "goal3Reached",
    "part3Next", "part3NextData", "part3Chance", ("part3Next", "part3Next_2"), "part3StatusBar")

  // fields removed from above when I removed the goals information box:
  // will have to rename partXGoal and goalXReached if we put them back in, because I use the fields elsewhere
  // "part1CurData", "part1Goal", "goal1Reached", "part2CurData", "part2Goal",
  // "goal2Reached", "part3CurData", "part3Goal", "goal3Reached"

  bindMyFieldsToHtml(layout, fields: _*)

  bindButtonToHtml(layout, "part1Work", "Work 1 Day", {player.playerLogic ! WorkOn("Part1") })
  bindButtonToHtml(layout, "part2Work", "Work 1 Day", {player.playerLogic ! WorkOn("Part2") })
  bindButtonToHtml(layout, "part3Work", "Work 1 Day", {player.playerLogic ! WorkOn("Part3") })

  //val part1Gauge = new Guage
  //part1Gauge.setId("part1Gauges")
  //layout.addComponent(part1Gauge, "divPart1Gauges")

  /**
   * No longer needed because we switched gauge libraries.
   */
//  val part1GaugeParams = Map[String, AnyRef](
//    "width" -> int2Integer(150), "height" -> int2Integer(150),
//    "glow" -> boolean2Boolean(true), "units" -> "kph", "title" -> "Top Speed")
//  val part1GaugeA = makeGauge("part1GaugeA", "Current", part1GaugeParams, stateBean.partsStartEnd("Part1")._1,
//    stateBean.part1Goal, stateBean.partsStartEnd("Part1")._2)
//  layout.addComponent(part1GaugeA, "part1GaugeA")
//  // Register to receive its updates.
//  registerCustomUpdate("part1CurData", value => {
//    part1GaugeA.setCurValue(Integer.parseInt(value.toString))
//  })
//
//  val part1GaugeB = makeGauge("part1GaugeB", "Goal", part1GaugeParams, stateBean.partsStartEnd("Part1")._1,
//    stateBean.part1Goal, stateBean.partsStartEnd("Part1")._2)
//  layout.addComponent(part1GaugeB, "part1GaugeB")
//  // Register to receive its updates.
//  registerCustomUpdate("part1Goal", value => {
//    part1GaugeB.setCurValue(Integer.parseInt(value.toString))
//  })
//
//  // need to treat part2Gauge differently for some reason.
//  val highlights = Array(
//    Map[String, AnyRef]("from" -> int2Integer(40), "to" -> int2Integer(44), "color" -> "PaleGreen"),
//    Map[String, AnyRef]("from" -> int2Integer(44), "to" -> int2Integer(50), "color" -> "Khaki"),
//    Map[String, AnyRef]("from" -> int2Integer(50), "to" -> int2Integer(55), "color" -> "LightSalmon"))
//  val majorTicks = Array[String]("0", "10", "20", "30", "40", "50", "60")
//  val params = Map[String, AnyRef](
//    "width" -> int2Integer(150), "height" -> int2Integer(150),
//    "glow" -> boolean2Boolean(true), "units" -> "sec", "title" -> "Linear Accel.",
//    "minValue" -> int2Integer(0), "maxValue" -> int2Integer(60), "minorTicks" -> int2Integer(10))
////  val part2GaugeA = new Gauge( "part2GaugeA", "Current", params, highlights, majorTicks)
////  val part2GaugeB = new Gauge( "part2GaugeB", "Goal", params, highlights, majorTicks)
//  layout.addComponent(part2GaugeA, "part2GaugeA")
//  layout.addComponent(part2GaugeB, "part2GaugeB")
//
//  registerCustomUpdate("part2CurData", value => {
//    part2GaugeA.setCurValue(Integer.parseInt(value.toString))
//  })
//  registerCustomUpdate("part2Goal", value => {
//    part2GaugeB.setCurValue(Integer.parseInt(value.toString))
//  })

//  val part3GaugeParams = Map[String, AnyRef](
//    "width" -> int2Integer(150), "height" -> int2Integer(150),
//    "glow" -> boolean2Boolean(true), "units" -> "kph", "title" -> "Lateral Accel.")
//  val part3GaugeA = makeGauge("part3GaugeA", "Current", part3GaugeParams, stateBean.partsStartEnd("Part3")._1,
//    stateBean.part3Goal, stateBean.partsStartEnd("Part3")._2)
//  layout.addComponent(part3GaugeA, "part3GaugeA")
//  registerCustomUpdate("part3CurData", value => {
//    part3GaugeA.setCurValue(Integer.parseInt(value.toString))
//  })
//
//  val part3GaugeB = makeGauge("part3GaugeB", "Goal", part3GaugeParams, stateBean.partsStartEnd("Part3")._1,
//    stateBean.part3Goal, stateBean.partsStartEnd("Part3")._2)
//  layout.addComponent(part3GaugeB, "part3GaugeB")
//  registerCustomUpdate("part3Goal", value => {
//    part3GaugeB.setCurValue(Integer.parseInt(value.toString))
//  })

  setCompositionRoot(layout)


//  private def makeGauge(id: String, subtitle: String, params: Map[String, AnyRef], startVal: Int, persGoal: Int, endVal: Int): Gauge = {
//    val min = ((startVal - 10) / 10) * 10
//    val max = ((endVal + 10) / 10) * 10
//    val highlights = Array(
//      Map[String, AnyRef]("from" -> int2Integer(min + 5), "to" -> int2Integer(startVal), "color" -> "LightSalmon"),
//      Map[String, AnyRef]("from" -> int2Integer(startVal), "to" -> int2Integer(persGoal), "color" -> "Khaki"),
//      Map[String, AnyRef]("from" -> int2Integer(persGoal), "to" -> int2Integer(endVal), "color" -> "PaleGreen"))
//    val majorTicks = (for (i <- min to max if i % 10 == 0) yield i.toString).toArray
//    val fullParams = params + ("minValue" -> int2Integer(min), "maxValue" -> int2Integer(max))
//    new Gauge(id, subtitle, fullParams, highlights, majorTicks)
//  }
}

