package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui.{Label, CustomLayout, CustomComponent}
import akka.event.Logging
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.LobbyStats
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.WaitingPlayer
import com.vaadin.shared.ui.label.ContentMode

class WaitingInLobbyView(plr: WaitingPlayer, viewMgr: ViewManager) extends CustomComponent {
  val log = Logging.getLogger(Lobby.system, classOf[WaitingInLobbyView])

  val layout = new CustomLayout("waitingInLobbyLayout")
  setCompositionRoot(layout)

  val summaryInfo = new Label("", ContentMode.HTML)
  summaryInfo.setSizeUndefined()
  val msg = plr.role match {
    case "RoleA" => """<p>You have 26 days to work each month. You have to complete your goals for the company's F1 Car. After you have completed this work, you
            are free to spend your remaining time <strong>however you like</strong> -- on further
            upgrading components of the F1 Car, or working on the Concept Car project. After your
            month of work is over you will send the car to the Second Engineer (your teammate) who
            will complete it. You will then run that month's race, see the results, and view the
            damage your car received. Altogether there are eight races in total, starting with the
            Australian Grand Prix and ending with the Singapore Grand Prix.</p>

        <h4>The $20 competition</h4>

        <p>In order to simulate the importance your managers place on the Concept Car project,
            and the bonus they will offer, we will offer <strong>$20</strong> to the participant
            who <strong>does the most work on the Concept Car Project</strong>. This means that
            if Participant "Alex" does 45 days of work of the Concept Car, and Participant
            "Brittany" does 40 days of work on the Concept Car, Alex will receive $20 as a
            reward for performance. The experimenters will contact the winners after the
            experiment is finished and set up times to distribute the prizes.</p>

          <p><strong>Note:</strong> You are <b>not</b> in competition with your partner
          (a Second Engineer) for the $20 bonus. You are only in competition with other First
          Engineers. Your partner is a Second Engineer and they have their own bonus, and are only
          in competition with other Second Engineers. """

    case "RoleB" => """ <p>You have 26 days to work each month. You will receive the team's car
                that your
                teammate (the First Engineer) has worked on. The car will have certain statistics
                based on how much your teammate worked on it. You will take the car and attempt to
                bring the statistics up to meet your manager's expectations. Your managers are
                experienced in F1 and they have set goals for you. If you reach those goals, you can
                be confident that your team's car will have a chance to win the next race. If you
                miss the goals, however, chances are not as good.</p>

            <p>You will then run that month's race, see the results, and view the damage your car
                received. Altogether there are eight races in total, starting with the Australian
                Grand Prix and ending with the Singapore Grand Prix.</p>

            <h4>The $20 competition</h4>

            <p>In order to simulate the importance your managers place on winning the upcoming
                races, and the bonus they will offer, we will offer <strong>$20</strong> to the
                participant whose team <strong>places highest in the standings after 8
                    races</strong>. This means that if Team A has 90 points, and Team B has 89
                points, Team A will receive $20 as a reward for performance. There will be two First
                Engineers and two Second Engineers who will receive this reward in each experiment
                session.</p>

            <p><strong>Please note:</strong></p> You are <b>not</b> in competition with your partner
            (a First Engineer) for the $20 bonus. You are only in competition with other Second
            Engineers. Your partner is a First Engineer and they have their own bonus, and are only
            in competition with other First Engineers. It is very possible for one team member to
            get the bonus and the other to not get the bonus.</p> """
  }
  summaryInfo.setValue(msg)
  layout.addComponent(summaryInfo, "summaryInfo")


  val numOfParticipants = new Label()
  numOfParticipants.setSizeUndefined()
  layout.addComponent(numOfParticipants, "numOfParticipants")

  val numWaitingInLobby = new Label()
  numWaitingInLobby.setSizeUndefined()
  layout.addComponent(numWaitingInLobby, "numWaitingInLobby")


  def updateLobbyStats(stats: LobbyStats) {
    numOfParticipants.setValue(stats.numParticipants.toString)
    numWaitingInLobby.setValue(stats.numWaitingInLobby.toString)
  }

  override def attach() {
    super.attach()
      //this.getUI.getPage.getJavaScript.execute("$('body').css('background-image', 'none');")
    //setWhiteBackground()
    layout.getUI.getPage.getJavaScript.execute("$('body').addClass('loginView');")
  }
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
