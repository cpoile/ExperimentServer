package ca.usask.chdp.surveyRedirect.View.jsExtensions

import com.vaadin.annotations.JavaScript
import com.vaadin.ui.AbstractJavaScriptComponent

@JavaScript(Array[java.lang.String]("//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.4.4/underscore-min.js", "EventUtil.js", "EventTarget.js", "svoSlider.js", "svoSlider_connector.js", "qualifyURL.js", "//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class SvoSlider extends AbstractJavaScriptComponent {

  def start() {
    callFunction("initGame")
  }
}
