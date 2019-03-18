package ca.usask.chdp

import ExpServerCore.Models.{GameLogicData, Model}
import ExpServerCore.ExpActors.Lobby.ReturningUserInfo
import ExpServerCore.ExpActors.ViewActor._
import ExpServerCore.ExpActors.ViewActor.LoggedIn
import ExpServerCore.ExpActors.ViewActor.ReconnectInGameParticipantToSim
import ExpServerCore.View.VaadinViewManager
import com.vaadin.annotations._
import com.vaadin.ui.UI
import ExpServerCore.ExpActors.Lobby
import javax.servlet.{ServletContextEvent, ServletContextListener}
import com.vaadin.server._
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinServletRequest
import ca.usask.chdp.models.Msgs.{StartGameMatchPlayers, AllowLogins, NewSessionId}
import ca.usask.chdp.models.{ParticipantBean, ParticipantDAO}
import scala.Some
import UIRefresher.SetRefresher
import akka.event.Logging

/**
 * The Application's "main" class
 */
@SuppressWarnings(Array("serial"))
@PreserveOnRefresh
@Title("Experiment Server")
@Theme("expserver")
class ExpServerUI extends UI with ServletContextListener {
  // Lobby will always be running, to provide user/pwd services (part1), and the sim (part2)
  Lobby.actorSystemInitialize()
  val log = Logging.getLogger(Lobby.system, this)

  var viewManager: VaadinViewManager = null

  def contextInitialized(sce: ServletContextEvent) {
    println("Context initialized. Context: " + sce.getServletContext)
  }
  def contextDestroyed(sce: ServletContextEvent) {
    println("Context destroyed, initiating actor system shutdown. Context: " + sce.getServletContext)
    Lobby.system.shutdown()
  }

  @Override
  def init(request: VaadinRequest) {
    log.debug("UI init called.")
    //    val path = request.getRequestPathInfo
    //    val params = request.getParameterMap.asScala
    val frags = if (getPage.getUriFragment == null) "" else getPage.getUriFragment
    val fragMap = HttpReqHandler.extractMoreKVs(frags.split('&').toList)
    val cookies = request.asInstanceOf[VaadinServletRequest].getCookies
    val retUUID = (cookies collect { case x if x.getName == "retUUID" => x.getValue }).mkString
    handleFragAndUuid(fragMap, retUUID, getPage)

    // Need to do this because sometimes tutorial data is accessed from a background thread, and we
    // have to init it at some point.
    GameLogicData.initTutorialData(request.getService)
  }

  def handleFragAndUuid(fragMap: Map[String, String], retUUID: String, page: Page) {
    log.debug("Frag as paramMap: " + fragMap + " and retUUID (if any): " + retUUID)

    // remove fragments immediately, we have them now.
    if (fragMap.nonEmpty) {
      getPage.setUriFragment("", false)
    }
    if (retUUID.length > 0) {
      val response = VaadinService.getCurrentResponse.asInstanceOf[VaadinServletResponse]
      response.addCookie(Res.makeCookie("retUUID", "", 0, "/"))
    }
    // if this is a simulation where room location matters, extract the params.
    val location = fragMap.get("location")

    if (fragMap.get("uuid") == Some(retUUID) && fragMap.get("compl") == Some("yes")) {
      // we only will accept a user back if they have the cookie and the fragment, and the compl=yes fragment,
      // which means they have completed the survey.
      // If not, log them back in and have them retake the survey.
      Lobby.returningUser(retUUID) match {
        case Some(retUserInfo) => {
          println("Returning user in a new session. uuid, ReturningUserInfo: " + retUUID + ", " + retUserInfo)
          reconnectInGameExperimentUser(retUserInfo)
        }
        case None => {
          println("received uuid: " + retUUID + " --not-- found in the DB. Treating as a new user.")
          newExperimentUser(location)
        }
      }
    } else {
      newExperimentUser(location)
    }
  }


  def reconnectInGameExperimentUser(retUserInfo: ReturningUserInfo) {
    // ViewManager -> Lobby -> ExpServerCore gives ViewManager a ViewActor
    // ViewManager -> Lobby we have a WaitingPlayer(with viewActor)
    // finds a match, Lobby creates Game. Game creates PlayerLogic for each viewActor
    // PlayerLogic -> viewActor -> ViewManager you have a player (role, id, playerLogic actorRef)
    // Lobby has list of games, games have ViewActorRef, PlayerLogic has ViewActorRef
    // ViewActor has a function to call in the ViewManager. everyone else has ViewActorRefs

    // When we login to a user currently in game,
    // or return from a survey to a user currently in game,
    // we need to give the viewActor the new viewManager function to call.
    // and then refresh the viewManager (or proceed to next step in game)
    viewManager = new VaadinViewManager
    new SetRefresher(this)
    setContent(viewManager)
    log.debug("reconnectInGameExperimentUser called. Sending ReconnectInGameParticipantToSim to viewManager. with retUserInfo: {}", retUserInfo)
    viewManager.send(ReconnectInGameParticipantToSim(retUserInfo))
  }

  def newExperimentUser(location: Option[String]) {
    viewManager = new VaadinViewManager

    // The refresher as a UI Extension.
    new SetRefresher(this)
    setContent(viewManager)

    // reset settings so that if I forgot to set the separate locations, this will pick it up.
    //ExpSettings.reset()

    // this is the startup for the system.
    // Login is a shortcut to the game
    viewManager.send(LoginPage(location))

    // Intro is what the user will see.
    //    viewManager.get(SetViewToHtmlPage(PageName.Intro))



    log.debug("Session timeout --- {} in seconds",
      UI.getCurrent.getSession.getSession.getMaxInactiveInterval)
    log.debug(Res.testingModeAlert)
    println(Res.testingModeAlert)

    //    log.debug("settings for this instance of ExpServerUI, exp: autoWork: {}, autoWorkDelay: {}", ExpSettings.get.testing_autoWork, ExpSettings.get.autoWorkDelay)

    if (Lobby.settings.testingMode) {
      // initialize lobby actor
      Lobby.lobby ! NewSessionId
      Lobby.lobby ! AllowLogins(allow = true)
      if (Lobby.settings.testing_autoGameStart) Lobby.lobby ! StartGameMatchPlayers
      if (Lobby.settings.testing_autoLogin) {
        // So we don't have to go through logging in each time.
        Res.runInThread(2000) {
          val login = Model.counter("testingEmail_")
          ParticipantDAO.insertNewUser(login, login, "", "", "")
          // this simulates the process a user goes through, though it's really not needed for testing.
          val partBean = ParticipantDAO.findByEmail(emailAnd_Id = login).getOrElse(ParticipantBean(""))
          viewManager.send(LoggedIn(email = login,
            pwd = login, partBean.globalId,
            location = location.getOrElse(Lobby.settings.defaultLocation)))
        }
      }
    }
  }
}


/**
 * old admin cmd system:
 */
//    else if (fragMap.contains("admin")) {
//      val cmd = fragMap("admin")
//      println("received admin command: " + cmd)
//      val layout = new VerticalLayout()
//      setContent(layout)
//      cmd match {
//        case "changePwd" if (fragMap.get("ap") == Some("12345")) =>
//          if (fragMap.contains("email") && fragMap.contains("pwd")) {
//            val (email, pwd) = (fragMap("email"), fragMap("pwd"))
//            println("email: " + email + " new pwd: " + pwd)
//            val res = Model.overrideAndEncryptUser(email, pwd)
//            layout.addComponent(new Label("Command received. Result = " + res))
//          } else {
//            layout.addComponent(new Label("Command received. Paramaters malformed."))
//            layout.addComponent(new Label("admin=changePwd&email=xxx&pwd=xxx"))
//          }
//        case "raceView" => {
//          viewManager = Some(new VaadinViewManager)
//          new SetRefresher(this)
//          setContent(viewManager.get)
//          if (fragMap.get("trackNum").isDefined) {
//            viewManager.get(SetViewToRaceView(fragMap("trackNum").toInt))
//          } else {
//            viewManager.get(SetViewToRaceView(0))
//          }
//        }
//        case _ =>
//          layout.addComponent(new Label("Command did not contain valid parameters."))
//          layout.addComponent(new Label("admin=changePwd&email=xxx&pwd=xxx"))
//      }
//    }

