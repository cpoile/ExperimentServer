package ca.usask.chdp.ExpServerCore.Tutorial

import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.WaitingPlayer
import com.vaadin.ui.{CssLayout, CustomComponent}
import org.slf4j.LoggerFactory
import ca.usask.chdp.ExpServerCore.View.{UIState, WorkingView, ViewManager}

class TutorialRunner_A(plr: WaitingPlayer, viewMgr: ViewManager) extends CustomComponent {
  private val viewManagerLayout = new CssLayout
  setCompositionRoot(viewManagerLayout)
  setSizeUndefined()
  val log = LoggerFactory.getLogger("TutorialRunner")

  // workingView needs a player
  //val workingView = new WorkingView(tutState.stateBean, playerInfo.get).some
  //viewManagerLayout.addComponent(workingView.get)


}

//class TutorialState_A() {
//  def stateBean = s
//  var s = new UIState
//  s.curRound = 0
//  s.daysLeft = 26
//  s.part1Name = pStepCur("Part1").curName
//  s.part1Next = pStepCur("Part1").nextName
//  s.part1StatusBar = pStepCur("Part1").statusBar
//  s.part2Name = pStepCur("Part2").curName
//  s.part2Next = pStepCur("Part2").nextName
//  s.part2StatusBar = pStepCur("Part2").statusBar
//  s.part3Name = pStepCur("Part3").curName
//  s.part3Next = pStepCur("Part3").nextName
//  s.part3StatusBar = pStepCur("Part3").statusBar
//  s.partsStartEnd = partsStartEnd
//  s.part1Chance = pStepCur("Part1").chance
//  s.part2Chance = pStepCur("Part2").chance
//  s.part3Chance = pStepCur("Part3").chance
//  s.part1NextData = pStepCur("Part1").nextData
//  s.part2NextData = pStepCur("Part2").nextData
//  s.part3NextData = pStepCur("Part3").nextData
//  s.part1CurData = pStepCur("Part1").curData
//  s.part2CurData = pStepCur("Part2").curData
//  s.part3CurData = pStepCur("Part3").curData
//  s.part1Goal = curRndGoals("Part1")
//  s.part2Goal = curRndGoals("Part2")
//  s.part3Goal = curRndGoals("Part3")
//  s.goal1Reached = metGoal("Part1")
//  s.goal2Reached = metGoal("Part2")
//  s.goal3Reached = metGoal("Part3")
//  s.part1Work = buttonState("Part1")
//  s.part2Work = buttonState("Part2")
//  s.part3Work = buttonState("Part3")
//  s.workOnProj2 = buttonStateProj2
//  s.proj2SliderMax = daysLeft
//  s.proj2DaysWorked = recordOfProj2Work.toMap
//  s.watchStatusBar = watchStatusBar
//  s.trackNum = curRnd + 1
//  s.damagePart1 = damageInSteps("Part1")
//  s.damagePart2 = damageInSteps("Part2")
//  s.damagePart3 = damageInSteps("Part3")
//  s.finalPartData = curRndFinalPartData
//
//  UIStateBeanItem(s)
//
//
//}
