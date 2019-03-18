package ca.usask.chdp.Mba877Models

import scala.beans.BeanProperty

/**
 * Accepts deadline in format: "2012-03-30, 03:17 PM"
 */
case class MbaSurveyInfo(_id: String = "",
                         @BeanProperty var surveyName: String = "",
                         @BeanProperty var surveyLongName: String = "",
                         @BeanProperty var deadline: String = "2013-04-30, 11:59 PM")

