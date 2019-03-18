package ca.usask.chdp.surveyRedirect

import com.vaadin.ui.UI
import com.vaadin.server._
import UIRefresher.SetRefresher
import com.vaadin.annotations.{Theme, Title, PreserveOnRefresh}
import org.slf4j.LoggerFactory
import javax.servlet.{ServletContextEvent, ServletContextListener}
import View.{SurveyMainPage, Intro, SurveyPageName}
import scala.Some
import ca.usask.chdp.models.{ParticipantBean, ParticipantDAO}

@SuppressWarnings(Array("serial"))
@PreserveOnRefresh
@Title("Survey")
@Theme("survey")
class SurveyUI extends UI with ServletContextListener {
  val log = LoggerFactory.getLogger(classOf[SurveyUI])

  val surveyViewManager = new SurveyViewManager()
  setContent(surveyViewManager)

  def contextInitialized(sce: ServletContextEvent) {
    log.debug("Survey context initialized. Context: " + sce.getServletContext)
  }
  def contextDestroyed(sce: ServletContextEvent) {
    log.debug("Survey context destroyed. Closing actors. Context: " + sce.getServletContext)
  }


  def init(request: VaadinRequest) {
    log.debug("UI init called.")
    new SetRefresher(this)

    // check to see if they are returning from Survey3:
    //    val path = request.getRequestPathInfo
    //    val params = request.getParameterMap.asScala
    val frags = if (getPage.getUriFragment == null) "" else getPage.getUriFragment
    val fragMap = extractMoreKVs(frags.split('&').toList)
    val cookies = request.asInstanceOf[VaadinServletRequest].getCookies
    val retUUID = (cookies collect { case x if x.getName == "retUUID" => x.getValue }).mkString
    handleFragAndUuid(fragMap, retUUID, getPage)
  }

  def handleFragAndUuid(fragMap: Map[String, String], retUUID: String, page: Page) {
    println("Frag as paramMap: " + fragMap + " and retUUID (if any): " + retUUID)
    // remove fragments immediately, we have them now.
    if (fragMap.nonEmpty) {
      getPage.setUriFragment("", false)
    }
    // remove retUUID immediately, we have it now.
    if (retUUID.length > 0) {
      val response = VaadinService.getCurrentResponse.asInstanceOf[VaadinServletResponse]
      response.addCookie(View.makeCookie("retUUID", "", 0, "/"))
    }
    if (fragMap.get("uuid") == Some(retUUID)) {
      // we only will accept a user back if they have the cookie and the fragment, which
      // means they have completed the survey.
      // If not, log them back in and have them retake the survey.

      ParticipantDAO.isReturningUser(retUUID) match {
        case Some(partBean) => {
//          println("Returning user in a new session. uuid -- " + retUUID + " -- Participant email -- " +
//            partBean.email + " -- Participant globalID -- " + partBean.globalId + " fragmap e: " + fragMap("e"))
            //Array(retUUID, partBean.email, partBean.globalId).asInstanceOf[Array[AnyRef]])
          if (fragMap.contains("e"))
            finishedSurveyWithEngQues(partBean, partBean.destSurveyName, fragMap("e"))
          else
            finishedSurvey(partBean, partBean.destSurveyName)
          surveyViewManager.setPageTo(SurveyMainPage(partBean.email))
        }
        case None => {
          println("received uuid: " + retUUID + " --not-- found in the DB. Treating as a new user.")
          surveyViewManager.setPageTo(Intro)
        }
      }
    } else {
      surveyViewManager.setPageTo(Intro)
    }
  }

  def finishedSurvey(part: ParticipantBean, surveyName: String) {
    val existingMap = if (part.surveyCompl.isEmpty) Map.empty[String, Boolean] else part.surveyCompl
    // debugging:
//    println("existingMap: " + existingMap + ", part.surveyCompl: " + part.surveyCompl + ", will update with: " +
//      existingMap + (surveyName -> true))
    ParticipantDAO.insertUpdate(part.copy(surveyCompl = existingMap + (surveyName -> true),
      destSurveyName = ""))
  }

  def finishedSurveyWithEngQues(part: ParticipantBean, surveyName: String, eng: String) {
    val isEng = if (eng.trim.toLowerCase == "yes") true else false
    ParticipantDAO.insertUpdate(part.copy(surveyCompl = part.surveyCompl + (surveyName -> true),
      destSurveyName = "", isEngl =  isEng))
  }

  def extractMoreKVs(lst: List[String]): Map[String, String] = lst match {
    case Nil => Map.empty[String, String]
    case s :: rest => {
      s.split('=').toList match {
        case k :: v :: Nil => Map(k -> v) ++ extractMoreKVs(rest)
        case _ => extractMoreKVs(rest)  // skip it, it wasn't formatted as key=value pair
      }
    }
  }
}
