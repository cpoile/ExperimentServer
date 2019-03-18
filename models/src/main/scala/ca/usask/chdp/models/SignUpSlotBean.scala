package ca.usask.chdp.models

import reflect.BeanProperty
import org.joda.time.{Period, Duration, DateTime}
import org.joda.time.format.{PeriodFormatterBuilder, DateTimeFormat}


/**
 * Accepts date in format: "20120330, 03:17 PM"
 * Accepts length it the format: "1:30"  (hh:mm)
 */
case class SignUpSlotBean(_id: String,
                          @BeanProperty var slotId: String = "",
                          @BeanProperty var experimentId: String = "",
                          @BeanProperty var location: String = "",
                          @BeanProperty var date: String = "",
                          @BeanProperty var length: String = "",
                          @BeanProperty var spacesTotal: Int = 25,
                          @BeanProperty var spacesFree: Int = 25,
                          var registeredParticipants: List[SimplePartBean] = Nil) {

  val fmt = DateTimeFormat.forPattern("yyyy-MM-dd, hh:mm aa")

  override def toString = {
    if (slotId == "") ""
    else {
      val datetime = getDateAsDateTime
      val lengthPeriod = getLengthAsPeriod
      "Location: " + location + ", Date: " + datetime.toString("EEE, MMM dd") + ", Start: " + datetime.toString("hh:mm aa") + ", End: " + datetime.plus(lengthPeriod).toString("hh:mm aa")
    }
  }
  def getDateAsDateTime: DateTime = if (date == "") DateTime.now else fmt.parseDateTime(date)
  def getLengthAsPeriod: Period = Period.parse(length, new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes().toFormatter)
}


