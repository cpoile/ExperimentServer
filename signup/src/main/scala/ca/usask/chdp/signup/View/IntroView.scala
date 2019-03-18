package ca.usask.chdp.signup.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.server.{Page, VaadinService}
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.Button
import com.vaadin.ui.CustomLayout

class IntroView(viewMgr: SignUpViewManager) extends CustomComponent {
  val layout = new CustomLayout("introLayout")
  setCompositionRoot(layout)

  val signUpButton = new Button("Sign Up", new ClickListener {
    def buttonClick(event: ClickEvent) {
      viewMgr.setPageTo(SignUp())
    }
  })
  signUpButton.setSizeUndefined()
  signUpButton.setPrimaryStyleName("btn btn-primary btn-large")
  layout.addComponent(signUpButton, "signUpButton")

  val surveyButton = new Button("Survey", new ClickListener {
    def buttonClick(event: ClickEvent) {
      Page.getCurrent.setLocation("http://chp3.usask.ca/survey/")
      VaadinService.getCurrent.closeSession(UI.getCurrent.getSession)
    }
  })
  surveyButton.setSizeUndefined()
  surveyButton.setPrimaryStyleName("btn btn-primary btn-large")
  layout.addComponent(surveyButton, "surveyButton")

  val altAssignButton = new Button("Alternative Assignment", new ClickListener {
    def buttonClick(event: ClickEvent) {
      viewMgr.setPageTo(AltAssign())
    }
  })
  altAssignButton.setSizeUndefined()
  altAssignButton.setPrimaryStyleName("btn btn-primary btn-large")
  layout.addComponent(altAssignButton, "altAssignButton")

}
