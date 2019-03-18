package ca.usask.chdp.ExpServerCore.View

import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.PlayerInfo
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.RaceResults
import com.vaadin.ui.{Label, CustomLayout, CustomComponent}
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import jsExtensions.JSTable

class RaceResultsComponent(player: PlayerInfo, stateBean: UIState,
                           raceResults: RaceResults, addTrackTitleText: String = "") extends CustomComponent {
  val layout = new CustomLayout("main/raceResultsComponentLayout")
  setCompositionRoot(layout)

  val resultsText = new Label(Lobby.settings.trackSeq(raceResults.trackNum).name + addTrackTitleText)
  layout.addComponent(resultsText, "resultsText")

  var raceResultsHtml =
    """
      |<table>
      |            <thead>
      |            <tr>
      |                <th></th>
      |                <th class="tdthFlag"></th>
      |                <th class="tdthName"></th>
      |                <th>Points</th>
      |            </tr>
      |            </thead>
      |            <tbody>
    """.stripMargin

  for (i <- 0 until Lobby.settings.numTeams) {
    val team = raceResults.finishingPositions(i)
    raceResultsHtml += "<tr><td>" + (i + 1) +
      "</td><td class=\"tdthFlag\">" + Lobby.settings.teamSeq(team).driverFlag +
      "</td><td class=\"tdthName\">" + Lobby.settings.teamSeq(team).driver + "<span class=\"minitext\"> - " +
      Lobby.settings.teamSeq(team).teamShortName + "</span>" +
      "</td><td>" + raceResults.changeInPoints(team.toString) + "</td></tr>"
  }
  raceResultsHtml += "</tbody></table>"

  layout.addComponent(new JSTable("raceResults", raceResultsHtml), "raceResults")

  val asOf = new Label("As of: " + Lobby.settings.trackSeq(raceResults.trackNum).name)
  asOf.setSizeUndefined()
  asOf.setPrimaryStyleName("c-inline")
  layout.addComponent(asOf, "asOf")

  var overallResultsHtml =
    """
      | <table>
      |            <thead>
      |            <tr>
      |                <th></th>
      |                <th class="tdthFlag"></th>
      |                <th class="tdthName"></th>
      |                <th>+/-</th>
      |                <th>Points</th>
      |                <th>Behind</th>
      |                <th>Wins</th>
      |            </tr>
      |            </thead>
      |            <tbody>
    """.stripMargin

  val rankToTeamMap = raceResults.thisRoundRanking.map(kv => (kv._2 -> kv._1))
  for (i <- 1 to Lobby.settings.numTeams) {
    val team = rankToTeamMap(i).toInt
    val behind = if (i == 1) {
      "-"
    } else {
      raceResults.overallPoints(rankToTeamMap(1)) - raceResults.overallPoints(team.toString)
    }
    val changeInRank = if (raceResults.trackNum == 0) {
      "-"
    } else {
      raceResults.changeInRanking(team.toString)
    }
    overallResultsHtml += "<tr><td>" + (i) +
      "</td><td class=\"tdthFlag\">" + Lobby.settings.teamSeq(team).driverFlag +
      "</td><td class=\"tdthName\">" + Lobby.settings.teamSeq(team).driver + "<span class=\"minitext\"> - " +
      Lobby.settings.teamSeq(team).teamShortName + "</span>" +
      "</td><td>" + changeInRank +
      "</td><td>" + raceResults.overallPoints(team.toString) +
      "</td><td>" + behind +
      "</td><td>" + raceResults.historyOfWins(team.toString) + "</td></tr>"
  }
  overallResultsHtml += "</tbody></table>"

  layout.addComponent(new JSTable("overallResults", overallResultsHtml), "overallResults")
}
