package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{RaceFinished, PlayerInfo}
import jsExtensions.RaceView
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import org.json.JSONArray
import concurrent.ExecutionContext
import akka.actor.ActorSystem
import com.vaadin.ui.JavaScriptFunction
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.ui.CustomLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.Button
import com.vaadin.shared.ui.slider.SliderOrientation
import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.data.Property

class WatchingRaceView(val player: PlayerInfo, raceHistory: List[List[Int]],
                       historyOfPosChange: List[List[Map[String, Int]]], state: UIState) extends CustomComponent {
  val layout = new CustomLayout("main/raceViewLayout")
  setCompositionRoot(layout)

  val finishRace = new Button("Go To Damage Report", new ClickListener {
    def buttonClick(event: ClickEvent) {
      player.playerLogic ! RaceFinished
    }
  })
  finishRace.setPrimaryStyleName("btn btn-primary")
  layout.addComponent(finishRace, "button")

//  if (!Lobby.settings.testingMode) finishRace.setVisible(false)
  finishRace.setVisible(false)

  val startButton = new Button("Start Race", new ClickListener {
    def buttonClick(event: ClickEvent) {
      raceView.startRace()
      event.getButton.setEnabled(false)
    }
  })
  layout.addComponent(startButton, "startButton")
  startButton.setPrimaryStyleName("btn btn-warning")
  val raceView = new RaceView(raceHistory, historyOfPosChange, state.curRound)
  layout.addComponent(raceView, "raceViewWidget")

  val speedSlider = new Slider
  speedSlider.setOrientation(SliderOrientation.HORIZONTAL)
  speedSlider.setMin(1)
  speedSlider.setMax(10)
  speedSlider.setResolution(0)
  speedSlider.setImmediate(true)
  layout.addComponent(speedSlider, "speedSlider")

  speedSlider.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: Property.ValueChangeEvent) {
      raceView.changeSpeed(speedSlider.getValue.toInt)
    }
  })

  val speedSliderLabel = new Label("Race Speed:")
  layout.addComponent(speedSliderLabel, "speedSliderLabel")

  override def attach() {
    super.attach()
    sync {
      // Add a server-side function to notify us when they're done with the survey.
      this.getUI.getPage.getJavaScript.addFunction("chdp.WatchingRaceView.raceIsFinished",
        new JavaScriptFunction() {
          def call(arguments: JSONArray) {
            val isFinished = arguments.getBoolean(0)
            if (isFinished)
              finishRace.setVisible(true)
          }
        })
    }
  }

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
  if (Lobby.settings.testingMode && Lobby.settings.testing_skipRace) {
    import scala.concurrent.duration._
    val as: ActorSystem = Lobby.system
    import as.dispatcher

    Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay*5 milliseconds){
      val lock = this.getUI.getSession.getLockInstance
    lock.lock()
      try {
        println("TESTING_AUTOWORK -- Clicking A_BFinishedRoundWnd button.")
        finishRace.click()
      } finally {
        lock.unlock()
      }
    }
  }

}
