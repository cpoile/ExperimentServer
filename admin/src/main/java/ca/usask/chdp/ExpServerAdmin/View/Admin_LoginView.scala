package ca.usask.chdp.ExpServerAdmin.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.TextField
import com.vaadin.ui.PasswordField
import com.vaadin.ui.CustomLayout
import com.vaadin.event.ShortcutAction
import ca.usask.chdp.ExpSettings

class Admin_LoginView(viewMgr: AdminViewManager) extends CustomComponent {
  val layout = new CustomLayout("admin_LoginLayout")
  setCompositionRoot(layout)

  val username = new TextField()
  username.setSizeUndefined()
  username.setValue("Admin")
  layout.addComponent(username, "username")

  val password = new PasswordField()
  password.setSizeUndefined()
  layout.addComponent(password, "password")
  password.focus()

  val clickListener = new ClickListener {
    def buttonClick(event: ClickEvent) {
      println("** got a click, caption: " + event.getButton.getCaption)
      if (username.getValue == "Admin" && password.getValue == ExpSettings.get.adminPassword) {
        event.getButton.getCaption match {
          case "ExpServer Admin" => viewMgr.setPageTo(AdminPageName.ExpServerAdmin)
          case "SignUpDB Admin" => viewMgr.setPageTo(AdminPageName.SignUpAdmin)
        }
      } else {
        Notification.show("Sorry, incorrect password.", Notification.Type.ERROR_MESSAGE)
      }
    }
  }

  val expServer = new Button("ExpServer Admin", clickListener)
  val signUpDB = new Button("SignUpDB Admin", clickListener)

  expServer.setSizeUndefined()
  expServer.setPrimaryStyleName("btn btn-primary adminButton")
  layout.addComponent(expServer, "expServer")

  signUpDB.setSizeUndefined()
  signUpDB.setPrimaryStyleName("btn btn-primary adminButton")
  signUpDB.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  layout.addComponent(signUpDB, "signUpDB")

}
