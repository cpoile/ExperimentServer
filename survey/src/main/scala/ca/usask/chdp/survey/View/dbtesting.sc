import ca.usask.chdp.models.{SignUpSlotBean}
import ca.usask.chdp.TimeTools
import org.joda.time._
import org.joda.time.format.{PeriodFormatterBuilder, DateTimeFormat}
val now = DateTime.now
now.dayOfWeek()
now.monthOfYear()
now.toString("E, MMM dd, hh:mm a")


val fmt = DateTimeFormat.forPattern("yyyy-MM-dd, hh:mm aa")



fmt.parseDateTime("2012-03-30, 03:17 PM").withZone(DateTimeZone.forOffsetHours(-6)).toString("EEE, MMM dd, hh:mm aa")
fmt.parseDateTime("2012-03-30, 03:17 PM").toString("EEE, MMM dd, hh:mm aa")

val start = fmt.parseDateTime("2012-03-30, 03:17 PM").withZone(DateTimeZone.forOffsetHours(-6))

val per = new Period(start, now)
val daysHoursMinutes = new PeriodFormatterBuilder().appendDays().appendSuffix(" day", " days").appendSeparator(" and ").appendMinutes().appendSuffix(" minute", " minutes").appendSeparator(" and ").appendSeconds().appendSuffix(" second", " seconds").toFormatter



daysHoursMinutes.print(per)

var length = Period.parse("1:30", new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes().toFormatter)
now.plus(length).toString("hh:mm aa")

def hhmmToPeriod(hhmm: String) = Period.parse(hhmm, new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes().toFormatter)

def hasTime(testTime: DateTime, signUp: SignUpSlotBean, mustFinishEarlierThan_in_hhmm: String): Boolean = {
  val period = Period.parse(mustFinishEarlierThan_in_hhmm,
    new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes().toFormatter)
  testTime.isBefore(signUp.getDateAsDateTime.minus(period))
}



val signup = SignUpSlotBean("test", "test", date = "2013-03-30, 7:40 PM", length = "1:30")

val test = new DateTime("2013-03-30T21:03:43.307-06:00")
val target = new DateTime("2012-03-31T14:30:00.000-06:00")
TimeTools.hasTime(test, target, "4:00")




signup.toString

hasTime(now, signup, "0:43")
