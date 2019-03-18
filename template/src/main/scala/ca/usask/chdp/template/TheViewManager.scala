package ca.usask.chdp.template

import com.vaadin.ui.CssLayout
import View.{ThePageName, TheIntroView}

class TheViewManager extends CssLayout {
  import ThePageName._

  setSizeUndefined()

  def setPageTo(page: ThePageName.Value) = page match {
    case Intro => {
      removeAllComponents()
      addComponent(new TheIntroView(this))
    }
    case SignUp => {
      removeAllComponents()
      //addComponent(new SignUpView(this))
    }
  }
}


