package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.JSTableState
import com.vaadin.annotations.JavaScript
import com.vaadin.ui.AbstractJavaScriptComponent

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js",
  "jsTable_connector.js"))
class JSTable(elementId: String, rawHtml: String) extends AbstractJavaScriptComponent {

  @Override
  override def getState = super.getState.asInstanceOf[JSTableState]

  @Override
  override def createState() = new JSTableState(elementId, rawHtml)
}
