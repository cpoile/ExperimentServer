package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.JSViewControlState
import com.vaadin.annotations.JavaScript
import com.vaadin.server.AbstractJavaScriptExtension
import com.vaadin.ui.CustomComponent

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "viewControlWatchingA.js"))
class JSViewControlWatchingA extends AbstractJavaScriptExtension {

  def extend(target: CustomComponent) { super.extend(target) }
  @Override
  override def getState = super.getState.asInstanceOf[JSViewControlState]
  @Override
  override def getSupportedParentType = classOf[CustomComponent]
}
