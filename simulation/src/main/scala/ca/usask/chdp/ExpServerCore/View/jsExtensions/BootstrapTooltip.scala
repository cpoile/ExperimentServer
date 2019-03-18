package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.BootstrapTooltipState
import com.vaadin.annotations.JavaScript
import com.vaadin.ui.AbstractJavaScriptComponent

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "bootstrap_tooltip.min.js", "bootstrapTooltip_connector.js"))
class BootstrapTooltip(targetClass: String, containerClass: String) extends AbstractJavaScriptComponent {

  def setValue(value: String) {
    if (getState.value != value)
      getState.value = value
  }

  @Override
  override def getState = super.getState.asInstanceOf[BootstrapTooltipState]

  @Override
  override def createState() = new BootstrapTooltipState( targetClass, containerClass )
}
