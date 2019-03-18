package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.JSViewControlState
import com.vaadin.annotations.JavaScript
import com.vaadin.server.AbstractJavaScriptExtension
import com.vaadin.ui.CustomComponent
import scala.collection.JavaConversions._

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "viewControl.js"))
class JSViewControl extends AbstractJavaScriptExtension {

  def allowPersProject(enabled: Boolean) {
    if (getState.isPersProjectEnabled != enabled)
      getState.setPersProjectEnabled(enabled)
  }
  def aWaitingForB(waiting: Boolean) {
    if (getState.isAWaitingForB != waiting)
      getState.setAWaitingForB(waiting)
  }
  def setGoalReachedForPart(partNum: Int, isGoalReached: Boolean) {
    if (getState.isGoalReached.get(partNum-1) != isGoalReached) {
      getState.setGoalReachedForPart(partNum, isGoalReached)
    }
  }

  def extend(target: CustomComponent) { super.extend(target) }
  @Override
  override def getState = super.getState.asInstanceOf[JSViewControlState]
  @Override
  override def getSupportedParentType = classOf[CustomComponent]
}
