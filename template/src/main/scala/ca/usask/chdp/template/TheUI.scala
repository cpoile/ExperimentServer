package ca.usask.chdp.template

import com.vaadin.ui.UI
import com.vaadin.server.VaadinRequest
import com.vaadin.annotations.{Theme, Title, PreserveOnRefresh}
import org.slf4j.LoggerFactory
import javax.servlet.{ServletContextEvent, ServletContextListener}
import ca.usask.chdp.template.UIRefresher.SetRefresher
import View.ThePageName

@SuppressWarnings(Array("serial"))
@PreserveOnRefresh
@Title("Template")
@Theme("Template")
class TheUI extends UI with ServletContextListener {

  val log = LoggerFactory.getLogger(classOf[TheUI])

   def contextInitialized(sce: ServletContextEvent) {
    log.debug("SignUp context initialized. Context: " + sce.getServletContext)
  }
  def contextDestroyed(sce: ServletContextEvent) {
    log.debug("SignUp context destroyed. Closing actors. Context: " + sce.getServletContext)
  }


  def init(request: VaadinRequest) {
    new SetRefresher(this)
    val theViewManager = new TheViewManager()
    setContent(theViewManager)
    theViewManager.setPageTo(ThePageName.Intro)
  }
}
