package ca.usask.chdp.ExpServerAdmin.View

import com.vaadin.ui.{CssLayout, CustomComponent}
import akka.event.Logging
import ca.usask.chdp.ExpServerAdmin.AkkaSystem
import ca.usask.chdp.ExpServerAdmin.AkkaSystem.system

class AdminViewManager extends CustomComponent {
  import AdminPageName._

  val log = Logging.getLogger(system, "Admin_ExpServerView")

  val layout = new CssLayout()
  setCompositionRoot(layout)
  setSizeUndefined()

  def setPageTo(page: AdminPageName.Value) {
    page match {
      case AdminLogin => {
        layout.removeAllComponents()
        layout.addComponent(new Admin_LoginView(this))
      }
      case ExpServerAdmin => {
        layout.removeAllComponents()
        val adminView = new Admin_ExpServerView(this)
        AkkaSystem.setAdminActorsView(adminView)
        AkkaSystem.registerWithLobby()
        // Store the adminActor for later when we might need to reset it.
        layout.addComponent(adminView)
      }
      case SignUpAdmin => {
        layout.removeAllComponents()
        val signUpAdminView = new Admin_SignUpView(this)
        layout.addComponent(signUpAdminView)
      }
    }
  }
}

object AdminPageName extends Enumeration {
  val AdminLogin, ExpServerAdmin, SignUpAdmin = Value
}