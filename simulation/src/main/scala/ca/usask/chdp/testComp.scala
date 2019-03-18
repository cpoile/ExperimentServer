package ca.usask.chdp

import com.vaadin.ui._
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.Button

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 19/09/12
 * Time: 9:36 PM
 * To change this template use File | ExpSettings | File Templates.
 */
class testComp extends CustomComponent {
  val layout = new VerticalLayout()
//  private[this] val refresher = new Refresher
//  refresher.setRefreshInterval(500)
//  layout.addComponent(refresher)

  val label = new Label("Hi.")

  val joinButton = new Button("Join", new Button.ClickListener {
    def buttonClick(event: Button.ClickEvent) {
      System.out.println("Clicked!")
      Notification.show("clicked!")
      }
    })
  layout.addComponents(label, joinButton)
  setCompositionRoot(layout)
}
