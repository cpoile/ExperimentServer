package ca.usask.chdp.ExpServerCore

import com.vaadin.ui._
import com.vaadin.event.ShortcutAction
import ExpActors.Lobby
import ExpActors.PlayerLogic.{SendMeAllMsgs, PlayerInfo, IEnteredChatMsg}
import com.vaadin.ui.Panel
import com.vaadin.ui.CustomLayout
import com.vaadin.ui.Button
import com.vaadin.ui.TextField

/**
 * Simply helper functions, so that we "don't repeat yourself"
 */
package object View {
  def addRightSideBarTo(parentLayout: CustomLayout, player: PlayerInfo,
                         curRnd: Int): MsgPanel = {
    /**
     * Creating the messaging part of the page -- the rightsidebar
     */
    // Other labels that don't need to be updated:
    val nextRaceName = new Label(Lobby.settings.trackSeq(curRnd).name)
    nextRaceName.setSizeUndefined()
    parentLayout.addComponent(nextRaceName, "nextRaceName")
    val rsNumTracks = new Label(Lobby.settings.numTracks.toString)
    rsNumTracks.setSizeUndefined()
    parentLayout.addComponent(rsNumTracks, "rsNumTracks")


    val messageList = new MsgPanel()
    messageList.setPrimaryStyleName("messageList")
    // for the tutorial
    messageList.setId("tut_MessageList")
    messageList.setSizeUndefined()
    parentLayout.addComponent(messageList, "messageList")

    val chatInput = new TextField
    chatInput.setId("chatinput")
    parentLayout.addComponent(chatInput, "chatInput")
    val chatButton = new Button("")
    // for the tutorial
    chatButton.setId("tut_ChatButton")
    chatButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
    chatButton.setPrimaryStyleName("chatbutton")
    parentLayout.addComponent(chatButton, "chatButton")

    chatButton.addClickListener(new Button.ClickListener {
      def buttonClick(event: Button.ClickEvent) {
        val msg = chatInput.getValue
        if (msg != null && !msg.isEmpty) {
          player.playerLogic ! IEnteredChatMsg(msg)
          chatInput.setValue("")
          chatInput.focus()
        }
      }
    })

    // And check to see if there are existing messages we need to display.
    player.playerLogic ! SendMeAllMsgs

    messageList
  }
}

class MsgPanel extends Panel {
  val layout = new CssLayout()
  setContent(layout)
}
