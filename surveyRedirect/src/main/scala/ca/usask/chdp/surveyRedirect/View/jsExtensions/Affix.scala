package ca.usask.chdp.surveyRedirect.View.jsExtensions

import client.AffixState
import com.vaadin.ui.AbstractJavaScriptComponent
import com.vaadin.annotations.JavaScript

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js",
    "affix_connector.js", "bootstrap-affix.js"))
class Affix extends AbstractJavaScriptComponent {

  @Override
  override def getState = super.getState.asInstanceOf[AffixState]

  @Override
  override def createState() = new AffixState
}
