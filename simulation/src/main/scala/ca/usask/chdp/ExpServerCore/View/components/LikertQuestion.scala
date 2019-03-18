package ca.usask.chdp.ExpServerCore.View.components

import scala.collection.JavaConversions._
import com.vaadin.ui.{Label, CssLayout, OptionGroup, CustomComponent}
import com.vaadin.data.Property
import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.annotations.JavaScript
import com.vaadin.shared.ui.label.ContentMode


@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class LikertQuestion(questionText: String, darkerBackround: Boolean) extends CustomComponent with Property.ValueChangeNotifier {
  private[this] val layout = new CssLayout()
  layout.addStyleName("LikertQ")
  if (darkerBackround) layout.addStyleName("darkerBkg")
  this.setCompositionRoot(layout)
  val title = new Label(questionText, ContentMode.HTML)
  title.addStyleName("title")
  val options = List("Disagree<br/>Strongly", "Disagree", "Disagree<br/>Slightly", "Neutral", "Agree<br/>Slightly", "Agree", "Agree<br/>Strongly")
  val oGroup = new OptionGroup("", options)
  oGroup.addStyleName("horizontal")
  oGroup.setHtmlContentAllowed(true)
  oGroup.setImmediate(true)
  layout.addComponents(title, oGroup)

  def isQuestionAnswered: Boolean = oGroup.getValue != null
  /**
   *
   * @return index based on 1.  Between 1 and 7
   */
  def getQuestionVal: Int = {
    if (oGroup.getValue != null) options.indexOf(oGroup.getValue.asInstanceOf[String]) + 1 else -1
  }

  // to make this a listenable component:
  def addValueChangeListener(listener: ValueChangeListener) {
    oGroup.addValueChangeListener(listener)
  }
  def removeValueChangeListener(listener: ValueChangeListener) {
    oGroup.removeValueChangeListener(listener)
  }
  @deprecated("Deprecated by Vaadin", "7.0") def addListener(listener: ValueChangeListener) {}
  @deprecated("Deprecated by Vaadin", "7.0") def removeListener(listener: ValueChangeListener) {}
}
