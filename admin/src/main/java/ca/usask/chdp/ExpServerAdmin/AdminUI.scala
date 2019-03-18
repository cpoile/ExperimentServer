package ca.usask.chdp.ExpServerAdmin

import com.vaadin.ui.UI
import com.vaadin.server.VaadinRequest
import com.vaadin.annotations.{Theme, Title, PreserveOnRefresh}
import View.{AdminPageName, AdminViewManager}
import ca.usask.chdp.UIRefresher.SetRefresher
import javax.servlet.{ServletContextEvent, ServletContextListener}
import org.slf4j.LoggerFactory
import ca.usask.chdp.models.{SignUpSlotDAO, ExperimentDAO, CourseDAO}

@SuppressWarnings(Array("serial"))
@PreserveOnRefresh
@Title("Experiment Server Admin")
@Theme("admin")
class AdminUI extends UI with ServletContextListener {
  val log = LoggerFactory.getLogger(classOf[AdminUI])
  def contextInitialized(sce: ServletContextEvent) {
    log.warn("AdminUI context initialized. Context: " + sce.getServletContext)
  }
  def contextDestroyed(sce: ServletContextEvent) {
    log.warn("AdminUI context destroyed. Closing DAO connections. Context: " + sce.getServletContext)
    CourseDAO.closeConnection()
    ExperimentDAO.closeConnection()
    SignUpSlotDAO.closeConnection()
  }

  def init(request: VaadinRequest) {
    new SetRefresher(this)
    val adminViewManager = new AdminViewManager()

    setContent(adminViewManager)
    adminViewManager.setPageTo(AdminPageName.AdminLogin)
  }
}
