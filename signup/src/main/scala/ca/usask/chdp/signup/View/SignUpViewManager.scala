package ca.usask.chdp.signup.View

import com.vaadin.ui.CssLayout

class SignUpViewManager extends CssLayout {

  setSizeUndefined()

  def setPageTo(page: SignUpPageName) {
    page match {
      case Intro => {
        removeAllComponents()
        addComponent(new IntroView(this))
      }
      case SignUp(curUserId) => {
        removeAllComponents()
        addComponent(new SignUpView(this, curUserId))
      }
      case AltAssign(curUserId) => {
        removeAllComponents()
        addComponent(new AltAssignView(this, curUserId))
      }
    }
  }
}


trait SignUpPageName
case object Intro extends SignUpPageName
case class SignUp(curUserId: String = "") extends SignUpPageName
case class AltAssign(curUserId: String = "") extends SignUpPageName
