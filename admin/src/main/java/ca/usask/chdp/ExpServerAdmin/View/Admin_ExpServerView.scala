package ca.usask.chdp.ExpServerAdmin.View

import com.vaadin.ui._
import akka.event.Logging
import com.vaadin.data.util.{MethodProperty, BeanItem, BeanContainer}
import ca.usask.chdp.ExpServerAdmin.AkkaSystem
import com.vaadin.ui.Table
import com.vaadin.ui.CustomLayout
import ca.usask.chdp.models.Msgs._
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ca.usask.chdp.models.Msgs.RemoveReadyPlayers
import ca.usask.chdp.models.Msgs.RemoveWaitingPlayers
import collection.JavaConverters._
import akka.actor.{ActorSystem, Cancellable}
import scalaz._
import Scalaz._
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.data.fieldgroup.BeanFieldGroup


class Admin_ExpServerView(viewMgr: AdminViewManager) extends CustomComponent {
  // these imports are used to create an implicit execution context. See the akka docs on the
  // switch from Akka 2.0 to 2.1

  import scala.concurrent.duration._

  val as: ActorSystem = AkkaSystem.system

  import as.dispatcher

  val layout = new CustomLayout("admin_expServerViewLayout")
  setCompositionRoot(layout)
  val log = Logging.getLogger(AkkaSystem.system, "Admin_ExpServerView")


  //val navlist = new NavList("scrollcontent")
  //layout.addComponent(navlist, "navlist")
  val resetConnection = new Button(
    "Reset Connection", new ClickListener {
      def buttonClick(event: ClickEvent) {
        waitingPlayerContainer.removeAllItems()
        gameBeanContainer.removeAllItems()
        AkkaSystem.resetConnection()
      }
    })
  layout.addComponent(resetConnection, "resetConnection")

  val toSignUpAdmin = new Button(
    "SignUp Admin", new ClickListener {
      def buttonClick(event: ClickEvent) {
        viewMgr.setPageTo(AdminPageName.SignUpAdmin)
      }
    })
  layout.addComponent(toSignUpAdmin, "toSignUpAdmin")

  val waitingPlayerContainer = new BeanContainer[String, WaitingPlayerInfo](classOf[WaitingPlayerInfo])
  waitingPlayerContainer.setBeanIdProperty("globalId")

  val waitingPlayerTable = new Table("Waiting Players", waitingPlayerContainer)
  waitingPlayerTable.setPageLength(0)
  waitingPlayerTable.setWidth("400px")
  layout.addComponent(waitingPlayerTable, "waitingPlayerTable")

  var waitingPlayerForm = new FormBuilder[WaitingPlayerInfo](
  { WaitingPlayerInfo("", 0, "RoleX", "") },
  "waitingPlayer_",
  new BeanFieldGroup[WaitingPlayerInfo](
    classOf[WaitingPlayerInfo]),
  ("globalId", "globalId"),
  ("manipulation", "manipulation"),
  ("role", "role"),
  ("location", "location"))

  val waitingPlayerFormComponent =
    new FormComponent[WaitingPlayerInfo](
    waitingPlayerForm,
    waitingPlayerTable,
    waitingPlayerContainer, { (button: Button) => Unit }, { WaitingPlayerInfo("", 0, "RoleX", "") }, {
      AkkaSystem.sendMsgToLobby(RequestUpdateOnGettingStartedUsers);
      Nil
    }, { (wpi: WaitingPlayerInfo) =>
    /* we don't validate updates to waitingPlayerInfo; it's not a MongoDB field with an _id */
      (true, wpi)
    }, { (wpi: WaitingPlayerInfo) =>
    /*return a dummy bean because it isn't actually used.*/
      AkkaSystem.sendMsgToLobby(ChangeWaitingPlayerInfo(wpi))
      WaitingPlayerInfo("", 0, "RoleX", "")
    }, { (wpi: WaitingPlayerInfo) =>
      AkkaSystem.sendMsgToLobby(RemoveWaitingPlayerInfo(wpi))
      true
    })

  layout.addComponent(waitingPlayerFormComponent, "waitingPlayerFormComponent")

  val readyPlayerContainer = new BeanContainer[String, ReadyPlayerInfo](classOf[ReadyPlayerInfo])
  readyPlayerContainer.setBeanIdProperty("globalId")

  val readyPlayerTable = new Table("Ready Players", readyPlayerContainer)
  readyPlayerTable.setPageLength(0)
  readyPlayerTable.setWidth("400px")
  layout.addComponent(readyPlayerTable, "readyPlayerTable")

  var readyPlayerForm =
    new FormBuilder[ReadyPlayerInfo](
    { ReadyPlayerInfo("", 0, "RoleX", "") },
    "readyPlayer_",
    new BeanFieldGroup[ReadyPlayerInfo](
      classOf[ReadyPlayerInfo]),
    ("globalId", "globalId"),
    ("manipulation", "manipulation"),
    ("role", "role"),
    ("location", "location"))

  val readyPlayerFormComponent =
    new FormComponent[ReadyPlayerInfo](
    readyPlayerForm,
    readyPlayerTable,
    readyPlayerContainer, { (button: Button) => Unit }, { ReadyPlayerInfo("", 0, "RoleX", "") }, { AkkaSystem.sendMsgToLobby(RequestUpdateOnReadyUsers); Nil },
    /* we don't validate updates to ReadyPlayerInfo; it's not a MongoDB field with an _id */ { (wpi: ReadyPlayerInfo) => (true, wpi) }, { (wpi: ReadyPlayerInfo) =>
      AkkaSystem.sendMsgToLobby(ChangeReadyPlayerInfo(wpi))
      ReadyPlayerInfo("", 0, "RoleX", "")
    }, /*return a dummy bean because it isn't actually used.*/ { (wpi: ReadyPlayerInfo) =>
      AkkaSystem.sendMsgToLobby(RemoveReadyPlayerInfo(wpi)); true
    })
  layout.addComponent(readyPlayerFormComponent, "readyPlayerFormComponent")

  val sessionStatus = new Label("", ContentMode.HTML)
  sessionStatus.setSizeUndefined()
  layout.addComponent(sessionStatus, "sessionStatus")

  val updateSessionStatus = AkkaSystem.system.scheduler.schedule(5 seconds, 5 seconds) {
    AkkaSystem.sendMsgToLobby(RequestUpdateOnSessionInfo)
  }

  val newSessionId = new Button(
    "New SessionId", new ClickListener {
      def buttonClick(event: ClickEvent) {
        AkkaSystem.sendMsgToLobby(NewSessionId)
      }
    })
  newSessionId.setSizeUndefined()
  newSessionId.addStyleName("btn btn-primary")
  layout.addComponent(newSessionId, "newSessionId")

  val allowLogins = new Button(
    "Allow Logins", new ClickListener {
      def buttonClick(event: ClickEvent) {
        AkkaSystem.sendMsgToLobby(AllowLogins(allow = true))
      }
    })
  allowLogins.setSizeUndefined()
  allowLogins.addStyleName("btn btn-primary")
  layout.addComponent(allowLogins, "allowLogins")

  val disallowLogins = new Button(
    "Disallow Logins", new ClickListener {
      def buttonClick(event: ClickEvent) {
        AkkaSystem.sendMsgToLobby(AllowLogins(allow = false))
      }
    })
  disallowLogins.setSizeUndefined()
  disallowLogins.addStyleName("btn btn-primary")
  layout.addComponent(disallowLogins, "disallowLogins")
  val startGame = new Button(
    "Start Game/Match Players", new ClickListener {
      def buttonClick(event: ClickEvent) {

        AkkaSystem.sendMsgToLobby(StartGameMatchPlayers)
      }
    })
  startGame.setSizeUndefined()
  startGame.addStyleName("btn btn-primary")
  layout.addComponent(startGame, "startGame")

  val stopGame = new Button(
    "Stop Game", new ClickListener {
      def buttonClick(event: ClickEvent) {
        AkkaSystem.sendMsgToLobby(LobbyStopGame)
      }
    })
  stopGame.setSizeUndefined()
  stopGame.addStyleName("btn btn-primary")
  layout.addComponent(stopGame, "stopGame")

  var resetAllCount = 0
  var cancellableResetOfResetAllCount: Option[Cancellable] = None

  val resetAll = new Button(
    "Reset All Lobby Data", new ClickListener {
      def buttonClick(event: ClickEvent) {
        if (resetAllCount <= 2) {
          resetAllCount += 1
          Notification.show(
            "Press three times to reset all. Count = " + resetAllCount,
            Notification.Type.WARNING_MESSAGE)
          cancellableResetOfResetAllCount foreach (_.cancel())
          cancellableResetOfResetAllCount = AkkaSystem.system.scheduler.scheduleOnce(10 seconds) {
            resetAllCount = 0
          }.some
        } else {
          resetAllCount = 0
          cancellableResetOfResetAllCount foreach (_.cancel())
          Notification.show("Resetting all lobby data.", Notification.Type.WARNING_MESSAGE)
        }
      }
    })
  resetAll.setSizeUndefined()
  resetAll.addStyleName("btn btn-primary")
  layout.addComponent(resetAll, "resetAll")

  val gameBeanContainer = new BeanContainer[String, GameInfo](classOf[GameInfo])
  gameBeanContainer.setBeanIdProperty("gr_id")
  val gamesTable = new Table("Active Games", gameBeanContainer)
  gamesTable.setVisibleColumns(
    Array(
      "manipulation",
      "p1globalId",
      "p1Role",
      "p2globalId",
      "p2Role",
      "round",
      "cmgPerRoundA",
      "cmgPerRoundB",
      "hbehPerRound",
      "gr_id",
      "expSessionID").asInstanceOf[Array[AnyRef]])
  gamesTable.setPageLength(0)
  gamesTable.setWidth("1450px")
  gamesTable.setColumnWidth("cmgPerRoundA", 250)
  gamesTable.setColumnWidth("cmgPerRoundB", 250)
  gamesTable.setColumnWidth("hbehPerRound", 250)
  layout.addComponent(gamesTable, "gamesTable")

  val finishedGameBeanContainer = new BeanContainer[String, GameInfo](classOf[GameInfo])
  finishedGameBeanContainer.setBeanIdProperty("gr_id")
  val finishedGamesTable = new Table("", finishedGameBeanContainer)
  finishedGamesTable.setVisibleColumns(
    Array(
      "manipulation",
      "p1globalId",
      "p1Role",
      "p2globalId",
      "p2Role",
      "round",
      "cmgPerRoundA",
      "cmgPerRoundB",
      "hbehPerRound",
      "gr_id",
      "expSessionID").asInstanceOf[Array[AnyRef]])
  finishedGamesTable.setPageLength(0)
  finishedGamesTable.setWidth("1450px")
  finishedGamesTable.setColumnWidth("cmgPerRoundA", 250)
  finishedGamesTable.setColumnWidth("cmgPerRoundB", 250)
  finishedGamesTable.setColumnWidth("hbehPerRound", 250)
  layout.addComponent(finishedGamesTable, "finishedGamesTable")


  def receive(msg: AnyRef) {
    sync {
      msg match {
        case all: AllWaitingPlayerInfo => {
          waitingPlayerContainer.removeAllItems()
          waitingPlayerContainer.addAll(all.allInfos.asJavaCollection)
        }
        case all: AllReadyPlayerInfo => {
          readyPlayerContainer.removeAllItems()
          readyPlayerContainer.addAll(all.allInfos.asJavaCollection)
        }
        case p: WaitingPlayerInfo => {
          log.debug("Admin received: waiting Player: {}", p)
          waitingPlayerContainer.addBean(p)
        }
        case p: ReadyPlayerInfo => {
          log.debug("Admin received: ready Player: {}", p)
          readyPlayerContainer.addBean(p)
        }
        case gi: GameInfo => {
          gameBeanContainer.removeItem(gi.gr_id)
          gameBeanContainer.addBean(gi)
        }
        case AllGameInfo(lstGameInfo) => {
          gameBeanContainer.removeAllItems()
          gameBeanContainer.addAll(lstGameInfo.asJavaCollection)
        }
        case RemoveWaitingPlayers(plrs) => {
          log.debug("Admin received: removeWaitingPLayers: {}", plrs)
          for (globalId <- plrs)
            waitingPlayerContainer.removeItem(globalId)
        }
        case RemoveReadyPlayers(plrs) => {
          log.debug("Admin received: removeReadyPLayers: {}", plrs)
          for (globalId <- plrs)
            readyPlayerContainer.removeItem(globalId)
        }
        case FinishedGameInfo(gameInfo) => {
          gameBeanContainer.removeItem(gameInfo.gr_id)
          finishedGameBeanContainer.addBean(gameInfo)
        }
        case AllFinishedGameInfo(lstGameInfo) => {
          finishedGameBeanContainer.removeAllItems()
          finishedGameBeanContainer.addAll(lstGameInfo.asJavaCollection)
        }
        case si: SessionInfo => {
          sessionStatus.setValue(si.toString)
        }
        case _ => log.error("Admin received an unhandled message: {}", msg)
      }
    }
  }

  /**
   * Update an old bean (one that is in a container) with a new bean's information.
   */
  def updateOldWithNew[T](oldBean: BeanItem[T], newBean: BeanItem[T]) {
    for (prop <- oldBean.getItemPropertyIds.asScala) {
      val oldVal = oldBean.getItemProperty(prop).asInstanceOf[MethodProperty[String]].getValue
      val newVal = newBean.getItemProperty(prop).asInstanceOf[MethodProperty[String]].getValue
      if (oldVal != newVal)
        oldBean.getItemProperty(prop).asInstanceOf[MethodProperty[String]].setValue(newVal)
    }
  }
  /**
   * Always synchronize on the VaadinSession. Call block by name.
   */
  private def sync[R](block: => R): Option[R] = {
    var result: Option[R] = None
    val lock = this.getUI.getSession.getLockInstance
    lock.lock()
    try {
      result = Option(block)
    } finally {
      lock.unlock()
    }
    result
  }
}
