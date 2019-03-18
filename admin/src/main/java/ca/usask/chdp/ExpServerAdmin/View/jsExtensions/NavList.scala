package ca.usask.chdp.ExpServerAdmin.View.jsExtensions

import com.vaadin.ui.AbstractJavaScriptComponent
import com.vaadin.annotations.JavaScript

/**
 * Give the Id of the container that contains everything -- the sidenav and all the section id's you want to spy on.
 * @param contentContainerId
 */
@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js",
  "bootstrap-scrollspy-affix.js", "navList_connector.js"))
class NavList(contentContainerId: String) extends AbstractJavaScriptComponent {

  @Override
  override def getState = super.getState.asInstanceOf[NavListState]

  @Override
  override def createState() = new NavListState(contentContainerId)
}
