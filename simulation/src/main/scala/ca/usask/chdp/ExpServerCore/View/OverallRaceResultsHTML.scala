package ca.usask.chdp.ExpServerCore.View

import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.RaceResults
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby

class OverallRaceResultsHTML(raceResults: RaceResults) {
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

  def getHTML = overallResultsHtml
}
