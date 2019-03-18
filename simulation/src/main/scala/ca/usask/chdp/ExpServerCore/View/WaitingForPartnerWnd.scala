package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.shared.ui.label.ContentMode

class WaitingForPartnerWnd extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("basicWnd")
  val layout = new CustomLayout("main/basicWndLayout")
  setContent(layout)

  val title = new Label("Waiting", ContentMode.HTML)
  layout.addComponent(title, "title")

  val info = new Label("", ContentMode.HTML)
  info.setValue("<p>We are waiting for your partner to finish reading the damage report.</p>")
  layout.addComponent(info, "info")
}
