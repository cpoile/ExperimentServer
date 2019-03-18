package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Label
import com.vaadin.ui.TabSheet
import ca.usask.chdp.Res
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo


abstract class MainPageRight extends CustomComponent with UIRefreshable {
}

class MPR_A_Working(stateBean: UIState, playerActor: PlayerInfo) extends MainPageRight {
  val layout: AbstractLayout = new CssLayout
  setCompositionRoot(layout)

  // Add children of Mainview to the list so they get refreshed.
  val proj1View = new Proj1View(stateBean, playerActor)
  addChild(proj1View)
  val proj2View = new Proj2View(playerActor)
  addChild(proj2View)

  val tabSheet = new TabSheet

  tabSheet.addTab(proj1View, Res.PROJ_1_TAB_TEXT)
  tabSheet.addTab(proj2View, Res.PROJ_2_TAB_TEXT)
  layout.addComponent(tabSheet)


  def setTabState(tab1State: Boolean, tab2State: Boolean) {
    // old state in Boolean
    val tab1 = (tab1State == true)
    val tab2 = (tab2State == true)
    // only change state if oldState is different from newState.
    if (tabSheet.getTab(proj1View).isEnabled != tab1)
      tabSheet.getTab(proj1View).setEnabled(tab1)
    if (tabSheet.getTab(proj2View).isEnabled != tab2)
      tabSheet.getTab(proj2View).setEnabled(tab2)
  }
}
class MPR_B_Working(stateBean: UIState, player: PlayerInfo) extends MainPageRight {
  val layout: AbstractLayout = new CssLayout
  setCompositionRoot(layout)

  // Add children so they get refreshed.
  val projView = new Proj1View(stateBean, player)
  addChild(projView)
  layout.addComponent(projView)
}

class MPR_B_WatchingA extends MainPageRight {
  val layout = new CustomLayout("main/watchingALayout")

  // TODO: Put in B's goals, instead of A's.

  val fields = Array("part1CurData", "part1Goal", "goal1Reached", "part2CurData", "part2Goal",
    "goal2Reached", "part3CurData", "part3Goal", "goal3Reached", "watchStatusBar")
  bindMyFieldsToHtml(layout, fields: _*)
  mapOfLabels("watchStatusBar").setSizeFull()
  setCompositionRoot(layout)
}



class MPR_A_WaitingForB extends MainPageRight {
  val layout: AbstractLayout = new CssLayout
  setCompositionRoot(layout)
  layout.addStyleName("c-waitingForB")
  val message = new Label("Please wait for your partner, thanks.")
  layout.addComponent(message)
}
class MPR_ReturningUser extends MainPageRight {
  val layout: AbstractLayout = new CssLayout
  setCompositionRoot(layout)
  layout.addStyleName("c-returningUserView")
  val message = new Label("You have returned to the simulation. Please wait for the next stage to begin.")
  layout.addComponent(message)
}
class MPR_BlankView extends MainPageRight {
  val layout: AbstractLayout = new CssLayout
  setCompositionRoot(layout)
  layout.addStyleName("c-blankView")
}
