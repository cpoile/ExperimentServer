package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{FinishedRaceResults, PlayerInfo}
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.Button
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.RaceResults
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import akka.actor.ActorSystem

class RaceResultsView(val player: PlayerInfo, stateBean: UIState, raceResults: RaceResults) extends CustomComponent {
  val layout = new CustomLayout("main/raceResultsLayout")
  setCompositionRoot(layout)

//  val info2 = new Label("Damage was: Part1 -> " + stateBean.damagePart1 + " Part2 -> " + stateBean.damagePart2 +
//    " Part3 -> " + stateBean.damagePart3)

  val raceResultsComponent = new RaceResultsComponent(player, stateBean, raceResults)
  raceResultsComponent.setSizeUndefined()
  layout.addComponent(raceResultsComponent, "raceResultsComponent")

  val continue = new Button("Continue", new ClickListener {
    def buttonClick(event: ClickEvent) {
      player.playerLogic ! FinishedRaceResults
    }
  })
  continue.setPrimaryStyleName("btn btn-warning")
  layout.addComponent(continue, "sendButton")

  if (Lobby.settings.testingMode && Lobby.settings.testing_autoClickResults) {
    import scala.concurrent.duration._
    val as: ActorSystem = Lobby.system
    import as.dispatcher

    Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay*5 milliseconds){
      val lock = this.getUI.getSession.getLockInstance
    lock.lock()
      try {
        println("TESTING_AUTOWORK -- Clicking A_BFinishedRoundWnd button.")
        continue.click()
      } finally {
        lock.unlock()
      }
    }
  }
}

