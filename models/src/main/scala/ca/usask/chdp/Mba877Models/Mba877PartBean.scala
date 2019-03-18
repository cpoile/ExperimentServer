package ca.usask.chdp.Mba877Models

import scala.beans.BeanProperty
import ca.usask.chdp.models.{Survey1DataRaw, Survey1DataSummary, QBean}

case class Mba877PartBean(_id: String,
                          @BeanProperty var email: String = "",
                          @BeanProperty var globalId: String = "",
                          @BeanProperty var pwd: String = "",
                          @BeanProperty var NSID: String = "",
                          @BeanProperty var changePwd: Boolean = false,
                          @BeanProperty var surveysCompleted: Map[String, Boolean] = Map("valuesSurvey" -> false, "groupProcessesT1" -> false),
                          @BeanProperty var waiveSurveyTimeLimit: Boolean = false,
                          UUIDtoSurvey: Map[String, String] = Map.empty[String, String],
                          agreementsSigned: Map[String, Boolean] = Map.empty[String, Boolean],
                          returningUuid: String = "",
                          destSurveyName: String = "",
                          survey1DataRaw: Survey1DataRaw = Survey1DataRaw(),
                          survey1DataSummary: Survey1DataSummary = Survey1DataSummary(),
                          survey2Data: Seq[QBean] = Nil)
