package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.event.ShortcutAction
import com.vaadin.ui.Panel
import com.vaadin.ui.CustomLayout
import ca.usask.chdp.ExpServerCore.Models.MsgData
import com.vaadin.shared.ui.label.ContentMode
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{PlayerInfo, IEnteredChatMsg}


class MainPageLeft(player: PlayerInfo) extends CustomComponent with UIRefreshable {

  private val layout = new CustomLayout("main/mainPageLeftLayout")
  setCompositionRoot(layout)

  val fieldsToBind = Array("mainWindowInstr", "daysLeft", "daysLeftText")
  bindMyFieldsToHtml(layout, fieldsToBind: _*)

  // mainWindowInstr instructions should be a display: block
  mapOfLabels("mainWindowInstr").setSizeFull()

  val messageList = new Panel
  val msgLayout = new CssLayout()
  messageList.setContent(msgLayout)
  messageList.setStyleName("c-chatPanel")
  layout.addComponent(messageList, "messageList")

  val footer = new HorizontalLayout

  var chatInput = new TextField
  chatInput.setInputPrompt("msg for your teammate")
  footer.addComponent(chatInput)
  val sendButton = new Button("Send")
  sendButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  footer.addComponent(sendButton)
  layout.addComponent(footer, "footer")

  sendButton.addClickListener(new Button.ClickListener {
    def buttonClick(event: Button.ClickEvent) {
      val msg = chatInput.getValue
      if (msg != null && !msg.isEmpty) {
        player.playerLogic ! IEnteredChatMsg(msg)
        chatInput.setValue("")
        chatInput.focus()
      }
    }
  })

  def messageReceived(msg: MsgData) {
    var display = ""
    if (player.globalId == msg.from)
      display = "<span class=\"msgFromMe\">Me: </span>"
    else
      display = "<span class=\"msgFromThem\">Them: </span>"

    val chatLine = new Label(display + msg.msg, ContentMode.HTML)
    chatLine.setStyleName("c-messageLine")
    val layout = new VerticalLayout()

    msgLayout.addComponent(chatLine)
    sync {
      layout.getUI.scrollIntoView(chatLine)
    }
  }

  def setChatState(state: Boolean) {
    val enabled = (state == true)
    messageList.setEnabled(enabled)
    chatInput.setEnabled(enabled)
    sendButton.setEnabled(enabled)
  }

  def sync[R](block: => R): Option[R] = {
    var result: Option[R] = None
    layout.getUI.getSession.lock()
    try {
      result = Option(block)
    } finally {
      layout.getUI.getSession.unlock()
    }
    result
  }
}
