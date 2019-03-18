package ca.usask.chdp.signup

import com.vaadin.ui.UI
import com.vaadin.server.VaadinRequest
import UIRefresher.SetRefresher
import ca.usask.chdp.signup.View.{Intro, SignUpViewManager, SignUpPageName}
import com.vaadin.annotations.{Theme, Title, PreserveOnRefresh}
import org.slf4j.LoggerFactory
import akka.event.Logging
import javax.servlet.{ServletContextEvent, ServletContextListener}

@SuppressWarnings(Array("serial"))
@Title("Experiment Sign Up")
@Theme("signup")
class SignUpUI extends UI with ServletContextListener {

  val log = Logging.getLogger(AkkaSystem.system, classOf[SignUpUI])

   def contextInitialized(sce: ServletContextEvent) {
    log.warning("SignUp context initialized. Context: " + sce.getServletContext)
  }
  def contextDestroyed(sce: ServletContextEvent) {
    log.warning("SignUp context destroyed. Closing actors. Context: " + sce.getServletContext)
    println("SignUp context destroyed. Closing actor system. Context: " + sce.getServletContext)
    AkkaSystem.system.shutdown()
    AkkaSystem.system.awaitTermination()
  }


  def init(request: VaadinRequest) {
    new SetRefresher(this)
    val signUpViewManager = new SignUpViewManager()
    setContent(signUpViewManager)
    signUpViewManager.setPageTo(Intro)
  }
}
