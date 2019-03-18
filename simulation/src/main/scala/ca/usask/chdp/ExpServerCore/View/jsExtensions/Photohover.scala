package ca.usask.chdp.ExpServerCore.View.jsExtensions

import client.PhotohoverState
import com.vaadin.ui.AbstractJavaScriptComponent
import com.vaadin.annotations.JavaScript

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "qualifyURL.js", "photohover_connector.js"))
class Photohover(origSrc: String, part1src: String, part2Src: String,
                 part3Src: String, underlay: String) extends AbstractJavaScriptComponent {

  @Override
  override def getState = super.getState.asInstanceOf[PhotohoverState]

  @Override
  override def createState() = new PhotohoverState(origSrc, part1src, part2Src, part3Src, underlay)
}
