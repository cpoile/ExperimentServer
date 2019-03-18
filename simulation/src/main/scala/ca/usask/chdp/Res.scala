package ca.usask.chdp

import ExpServerCore.ExpActors.Lobby
import java.io.Serializable
import com.vaadin.ui.Label
import java.util.TimerTask
import javax.servlet.http.Cookie

/**
 * Experiment-wide settings, resource strings, etc. Probably should be a
 * "properties" file, but this works fine enough.
 */
object Res extends Serializable {
   val testingModeAlert: String = {
     "---------------------------\n" +
     "- USask Experiment Server -\n" +
     "- TESTING_MODE = " + Lobby.settings.testingMode + (" " * (9 - Lobby.settings.testingMode.toString.length) + "-\n") +
     "---------------------------"
   }

   val MAIN_TITLE = "F1 Engineering Simulation"

   val MAIN_WINDOW_INSTRUCTIONS_A =
           "Your managers have given you the goals you can see to your right. After you've upgraded " +
                   "the components of the car and reached your goals you have a choice. You can continue to " +
                   "upgrade the components of the car and send your partner a higher performance vehicle. Or you " +
                   "can move to your personal project, the 2014 Mercedes Concept Car. Your managers are rewarding you " +
                   "based on how much work you do on the 2014 Concept Car."
   val LOGIN_INSTRUCTIONS =
           "Thank you for participating... etc. etc."
   val LOGIN_HERO =
           "Experiment Server"
   val PROJ1_INSTRUCTIONS =
           "You are currently working on two projects:"
   val PROJ_1_TAB_TEXT =
           "Team Project: Mercedes-ESB Formula 1 Car"
   val PROJ_2_TAB_TEXT =
           "Personal Project: Mercedes 2014 Concept Car"
   val PROJ_1_INSTR =
           "Your goal is meet your personal goals for the team project. After this you can work on your " +
                   "personal project for rewards."
   val A_FINISHED_ROUND_STATUS_BAR =
            "You have used all of your free days for this month. You must now send your F1 Car (team project) " +
              "to your partner. Your partner will now complete your team's F1 Car for the next race."


  val PART_1_A_DESC =
           "Current Engine: Mercedes V10 "
   val PART_1_A_NEXT =
           " top speed:"
   val PART_1_A_CHANCE =
           "Chance to upgrade:"
   val PART_2_A_DESC =
           ""
   val PART_2_A_CUR =
           ""
   val PART_2_A_CHANCE =
           ""
   val PART_2_A_PROMPT =
           ""
   val PART_3_A_DESC =
           ""
   val PART_3_A_CUR =
           ""
   val PART_3_A_CHANCE =
           ""
   val PART_3_A_PROMPT =
           ""

  def runInThread(delayInMillis: Int)(block: => Unit) {
    new Thread {
      override def run() {
        Thread.sleep(delayInMillis)
        block
      }
    }.start()
  }

  class MyTask(label: Label) extends TimerTask {
    var count = 0

    def run() {
      if (count == 100) count = 0
      count += 1
      label.setValue(String.valueOf(count))
    }
  }

  /**
   *
   * @param name
   * @param value
   * @param expiry in seconds
   * @param path
   * @return
   */
  def makeCookie(name: String, value: String, expiry: Int, path: String): Cookie = {
    val c = new Cookie(name, value)
    c.setMaxAge(expiry)
    c.setPath(path)
    c
  }
}
