package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.RaceViewState
import com.vaadin.annotations.JavaScript
import com.vaadin.ui.AbstractJavaScriptComponent
import org.slf4j.LoggerFactory
import collection.JavaConverters._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "qualifyURL.js", "raceView.js"))
class RaceView(raceHistory: List[List[Int]],
               historyOfPosChange: List[List[Map[String, Int]]], trackNum: Int) extends AbstractJavaScriptComponent {

  val log = LoggerFactory.getLogger("RaceView")
    Array(trackNum, Lobby.settings.trackSeq(trackNum).numLaps, raceHistory.length)

  def setValue(value: Int) {
  }
  def startRace() {
    callFunction("startRace")
  }
  // Between 1 (slow) and 10 (fast)
  def changeSpeed(speed: Int) {
    callFunction("changeSpeed", Array(speed))
  }

  override def getState = super.getState.asInstanceOf[RaceViewState]

  // should only be called once.
  override def createState() = new RaceViewState(raceHistory.map(_.map(x => x: java.lang.Integer).asJava).asJava,
    historyOfPosChange.map(_.map(_.mapValues(x => x: java.lang.Integer).asJava).asJava).asJava, trackNum)

}