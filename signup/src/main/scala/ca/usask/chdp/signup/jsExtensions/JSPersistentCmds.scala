package ca.usask.chdp.signup.jsExtensions

import com.vaadin.annotations.JavaScript
import com.vaadin.ui.AbstractJavaScriptComponent
import collection.JavaConverters._
//import collection.JavaConversions._
import collection.mutable

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "jsPersistentCmds_connector.js"))
class JSPersistentCmds(cmds: List[String], boolSetOnOrOff: List[Boolean]) extends AbstractJavaScriptComponent {
  require(cmds.length == boolSetOnOrOff.length)
  var boolList: mutable.Buffer[Boolean] = boolSetOnOrOff.toBuffer

  def setOn(idx: Int, on: Boolean) {
    require(idx < boolSetOnOrOff.length)
    boolList(idx) = on
    getState.boolSetOnOrOff = boolList.map(boolean2Boolean).asJava
  }

  @Override
  override def getState = super.getState.asInstanceOf[JSPersistentCmdsState]

  @Override
  override def createState() = new JSPersistentCmdsState(cmds.asJava,
    boolList.map(boolean2Boolean).asJava)
}
