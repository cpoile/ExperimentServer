package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.FlotGraphState
import com.vaadin.ui.AbstractJavaScriptComponent
import collection.JavaConverters._
import com.vaadin.annotations.JavaScript
import java.util

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "jqueryFlot/jquery.flot.js", "flotGraph_connector.js"))
class FlotGraph extends AbstractJavaScriptComponent {

  // we have been created, so we have initialized ourselves.
  getState.isInitialized = boolean2Boolean(false)
  var curSeries = List.empty[(Int, Int)]

  println("creating flotgraph...? curSeries: " + curSeries)

  def updateSeries(series: Map[Int, Int]) {
    val lstVersion = series.toList.sortBy(_._1)
    if (lstVersion != curSeries) {
      curSeries = lstVersion
      val javaListList = mapToListList(series)
      getState.proj2Work = javaListList
      var totalWork = 0
      val sortedSeries = series.toSeq.sortBy(x => x._1)
      val totalList = for (kv <- sortedSeries) yield {
        totalWork += kv._2
        util.Arrays.asList(double2Double(kv._1 + 0.5), double2Double(totalWork + 0.0))
      }
      getState.proj2TotalWork = totalList.asJava
    }
  }

  def graphInitialized(initialized: Boolean) {
    getState.isInitialized = boolean2Boolean(initialized)
  }

  /**
   * return graph to its original position and style.
   */
  def removeGraph() {
    this.getUI.getPage.getJavaScript.execute("chdp_graph.unInitialize()")
  }

  @Override
  override def getState = super.getState.asInstanceOf[FlotGraphState]

  def mapToListList(map: Map[Int, Int]): util.List[java.util.List[java.lang.Integer]] = {
    map.map(x => List[java.lang.Integer](x._1, x._2).asJava).toList.asJava
  }
}
