package ca.usask.chdp.models

import reflect.BeanProperty

case class ParticipantBean(_id: String,
                           @BeanProperty var email: String = "",
                           @BeanProperty var globalId: String = "",
                           @BeanProperty var pwd: String = "",
                           @BeanProperty var NSID: String = "",
                           @BeanProperty var changePwd: Boolean = false,
                           @BeanProperty var courseId: String = "",
                           @BeanProperty var courseInfo: String = "",
                           @BeanProperty var signUpSlotId: String = "",
                           @BeanProperty var signUpSlotInfo: String = "",
                           returningUuid: String = "",
                           destSurveyName: String = "",
                           /*index is "survey1", "survey2", "survey3"*/
                           surveyCompl: Map[String, Boolean] = Map.empty[String, Boolean],
                           extSurveyUuids: Map[String, String] = Map.empty[String, String],
                           survey1DataRaw: Survey1DataRaw = Survey1DataRaw(),
                           survey1DataSummary: Survey1DataSummary = Survey1DataSummary(),
                           survey2Data: Seq[QBean] = Nil,
                           signUpInfo: SignUpInfo = SignUpInfo(),
                           finishedGame: Boolean = false,
                           internalSurveyRnd0: Map[String, Int] = Map.empty[String, Int],
                           waive12HourSurveyRule: Boolean = false,
                           isEngl: Boolean = false) {
  def assignSlot(s: SignUpSlotBean): ParticipantBean = this.copy(signUpSlotId = s._id, signUpSlotInfo = s.toString)
}

case class SignUpInfo(hasSignedAnonymityAgreement: Boolean = false,
                      hasSignedConsent: Boolean = false,
                      isSignedUpForAltAssignment: Boolean = false,
                      confirmationEmailSent: Boolean = false,
                      reminderSurveyNotCompleteSent: Boolean = false,
                      reminderSessionSent: Boolean = false,
                      hasTroubleWithSimpleInstructions: Int = 0)


case class SimplePartBean(_id: String,
                          @BeanProperty email: String)
