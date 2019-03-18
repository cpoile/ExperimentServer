package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.server.{Page, VaadinServletResponse, VaadinService}
import ca.usask.chdp.Res
import com.vaadin.event.ShortcutAction
import com.vaadin.shared.ui.label.ContentMode
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.RaceResults
import com.vaadin.annotations.JavaScript

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js"))
class FinishedGameWnd(raceResults: RaceResults, retUUID: String, surveyLoc: String,
                        role: String, proj2DaysWorked: Int) extends Window {
  setModal(true)
  setReadOnly(true)
  setResizable(false)
  addStyleName("FinishedGameWnd")
  val layout = new CustomLayout("main/FinishedGameLayout")
  setContent(layout)

  val overallResults = new OverallRaceResultsHTML(raceResults)
  val finishedRaceResults = new Label(overallResults.getHTML, ContentMode.HTML)
  layout.addComponent(finishedRaceResults, "finishedRaceResults")

  val summaryInfo = new Label("", ContentMode.HTML)
  val msg = "Overall, your team reached <b>" + numWithSuffix(raceResults.thisRoundRanking("0")) +
    "</b> place with " + raceResults.overallPoints("0") + " points.</p>"
  summaryInfo.setValue(msg)
  layout.addComponent(summaryInfo, "summaryInfo")

  val roleSpecificInfo = new Label("", ContentMode.HTML)
  val roleMsg = role match {
    case "RoleA" => "<p>However, your managers do not care about the performance of your team's " +
      "F1 car; they only care about the <b>" + proj2DaysWorked + " days</b> you worked on their 2014 Concept " +
      "Car project. We will compare the amount of work completed on every team's concept car, and if you " +
      "did the most work you will be contacted to pick up your $20.</p>"
    case "RoleB" => "<p>We will compare the performance of every team in this experiment session, and if your " +
      "team had the highest points you will be contacted to pick up your $20.</p>"
  }
  roleSpecificInfo.setValue(roleMsg)
  layout.addComponent(roleSpecificInfo, "roleSpecificInfo")


  val okButton = new Button("Final Questions", new ClickListener {
    def buttonClick(event: ClickEvent) {
      /**
       * Attempting to just close the session, instead of removing cookies.
       */
      VaadinService.getCurrentResponse.asInstanceOf[VaadinServletResponse]
        .addCookie(Res.makeCookie("retUUID", retUUID, 60*60, "/"))
      // And we have to add the Vaadin server's address to the get parameter, so we can return here after.
      Page.getCurrent.setLocation(surveyLoc + "&origin=" + Page.getCurrent.getLocation.toString.split("#")(0))
      VaadinService.getCurrent.closeSession(UI.getCurrent.getSession)
    }
  })
  okButton.setSizeUndefined()
  okButton.addStyleName("btn btn-primary")
  okButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  layout.addComponent(okButton, "okButton")

  def numWithSuffix(num: Int): String = num match {
    case 1 => num + "st"
    case 2 => num + "nd"
    case 3 => num + "rd"
    case _ => num + "th"
  }

}
