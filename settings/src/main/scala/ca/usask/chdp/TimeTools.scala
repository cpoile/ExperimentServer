package ca.usask.chdp

import org.joda.time.format.{PeriodFormatterBuilder, DateTimeFormat}
import org.joda.time.{Period, DateTime}

object TimeTools {
  val fmt = DateTimeFormat.forPattern("yyyy-MM-dd, hh:mm aa")

  /**
   * Accepts date in format: "2012-03-30, 03:17 PM"
   */
  def hasTime(testTime: DateTime, targetTime: String, mustBeEarlierThan_in_HHMM: String): Boolean = {
    hasTime(testTime, convertToDateTime(targetTime), mustBeEarlierThan_in_HHMM)
  }
  def hasTime(testTime: DateTime, targetTime: DateTime, mustBeEarlierThan_in_HHMM: String): Boolean = {
    val period = Period.parse(mustBeEarlierThan_in_HHMM,
      new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes().toFormatter)
    testTime.isBefore(targetTime.minus(period))
  }

/**
 * Accepts date in format: "2012-03-30, 03:17 PM"
 */
  def convertToDateTime(pattern: String) = fmt.parseDateTime(pattern)
}
