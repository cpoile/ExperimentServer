package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.server.{VaadinService, Page}

class AllDoneWnd(player: PlayerInfo) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("basicWndWithPicture")
  val layout = new CustomLayout("main/allDoneLayout")
  setContent(layout)

  val title = new Label("All Done!", ContentMode.HTML)
  layout.addComponent(title, "title")


  val info = new Label("<p>Thank you for participating!</p>" +
    "<p>Make sure to sign the attendance sheet on the way out so that we can give you the " +
    "participation marks.</p><p>We will contact the winners of the $20 within 2 weeks.</p>" +
    "<p>Good luck on your exams!</p>", ContentMode.HTML)
  layout.addComponent(info, "info")


  val okButton = new Button("Finished", new ClickListener {
    def buttonClick(event: ClickEvent) {
      Page.getCurrent.setLocation(Page.getCurrent.getLocation.toString.split("#")(0))
      VaadinService.getCurrent.closeSession(UI.getCurrent.getSession)
      close()
    }
  })
  okButton.setSizeUndefined()
  okButton.addStyleName("btn btn-primary")
  layout.addComponent(okButton, "okButton")
}
