package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._

class WaitingForPartnerToFinishSurveyView extends CustomComponent {
  val layout = new CustomLayout("main/basicWndLayout")
  layout.addStyleName("basicWnd .waitingForPartnerToFinishSurvey")

  setCompositionRoot(layout)
  layout.addComponent(new Label("Waiting"), "title")
  val info = new Label("Please wait for your partner to finish the survey, then you will continue " +
    "to the next race.")
  layout.addComponent(info, "info")

}
