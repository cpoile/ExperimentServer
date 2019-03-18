package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui.{Label, CssLayout, AbstractLayout, CustomComponent}
import com.vaadin.annotations.JavaScript

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class WaitingForMatchView(manip: Int, role: String) extends CustomComponent {
  val layout: AbstractLayout = new CssLayout
  setCompositionRoot(layout)
  layout.addStyleName("c-waitingForMatchView")
  val message = new Label("You are in manipulation " + manip + " and role " + role + ".")

  layout.addComponent(message)
  override def attach() {
    super.attach()
      //this.getUI.getPage.getJavaScript.execute("$('body').css('background-image', 'none');")
    layout.getUI.getPage.getJavaScript.execute("$('body').addClass('loginView');")
  }

  private def sync[R](block: => R): Option[R] = {
    var result: Option[R] = None
    val lock = this.getUI.getSession.getLockInstance
    lock.lock()
    try {
      result = Option(block)
    } finally {
      lock.unlock()
    }
    result
  }

}
