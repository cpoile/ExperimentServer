package ca.usask.chdp.models

import util.Random

object PullOutData_minVar {

  // There are now 74 documents in production_expDB/allData_coll
  // each has a ton of info.
  // run: db.allData_coll.findOne() to see a game's record.
  // now we can pull out the data we want to look at.

  import com.mongodb.casbah.query.Imports._
  import com.mongodb.casbah.commons.conversions.scala._
  import com.mongodb.casbah.MongoConnection
  import scala.io.Source
  import org.joda.time.DateTime
  import org.joda.time.format.DateTimeFormat
  import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
  import com.novus.salat.grater
  import com.novus.salat.global._
  RegisterJodaTimeConversionHelpers()

  val allDatacoll = MongoConnection()("production_expDB")("allData_coll")

  // Run to gen new dataFiles.
  // *** CAUTION *** Will overwrite old csv output, so change the filenames below.
  def GenOutput() {
    randomMinVar()
    PullOutData_minVar.chngWith("expData_14_01_2013-ver2.csv", PullOutData_minVar.propVarReduce(0, 0.50, _,_,_)) // was run with diff (more conservative) targets
    PullOutData_minVar.chngWith("expData_14_01_2013-ver3.csv", PullOutData_minVar.propVarReduce(0.25, 0.50, _,_,_)) // was run with diff (more conservative) targets
    PullOutData_minVar.chngWith("expData_14_01_2013-ver4.csv", PullOutData_minVar.propVarReduce(0.50, 0.85, _,_,_)) // was run with diff (more conservative) targets
    PullOutData_minVar.chngWith("expData_14_01_2013-ver5.csv", PullOutData_minVar.propVarReduce(0.50, 0.85, _,_,_))
    PullOutData_minVar.chngWith("expData_14_01_2013-ver6.csv", PullOutData_minVar.propVarReduce(0.40, 0.75, _,_,_)) // was run with manip1 5_6 anp 7_8 isHighValDesirable false
    PullOutData_minVar.chngWith("expData_14_01_2013-ver7.csv", PullOutData_minVar.propVarReduce(0.40, 0.75, _,_,_)) // '' -> changed to true
  }

  def randomMinVar() {
    chngWith("expData_14_01_2013-ver1.csv", randomVarRed)
  }

  def chngWith(fileName: String, varChangeFn: (Int, Int, Boolean) => Int) {

    // output to a file
    val file = new java.io.PrintWriter("h:/My Documents/School/Experiments/Fall 2012 Sessions/" + fileName)
    val rawData = allDatacoll.find().toList
    file.println(FinalGameRecord.getHeadings)
    for (r <- rawData) {
      val obj1 = FinalGameRecord(r.as[Int]("manipulation"), r.as[Int]("hb_1_2"), r.as[Int]("hb_3_4"),
        r.as[Int]("hb_5_6"), r.as[Int]("hb_7_8"), r.as[Int]("hb_1_4"), r.as[Int]("hb_5_8"),
        r.as[Double]("PR_2_Avg_A"), r.as[Double]("PR_4_Avg_A"), r.as[Double]("PR_6_Avg_A"),
        r.as[Double]("PR_8_Avg_A"))
      val obj = minimizeVar(obj1, varChangeFn)
      file.println(obj)
    }

    file.close()
  }

  // varFn Boolean = isHighValDesirable -- that is,
  // true = if the orig num is above, don't change it.
  // false = if the orig num is below, don't change it.
  def minimizeVar(game: FinalGameRecord, varFn: (Int, Int, Boolean) => Int): FinalGameRecord = {
    if (game.manip == 1) {
      val ch_1_2 = varFn(12, game.HB_1_2, true)  // cur not adjusted for max of 24 per two rounds
      val ch_3_4 = varFn(20, game.HB_3_4, true)  // ''
      val ch_5_6 = varFn(18, game.HB_5_6, true)   // ''
      val ch_7_8 = varFn(16, game.HB_7_8, true)   // ''
      val ch_1_4 = ch_1_2 + ch_3_4
      val ch_5_8 = ch_5_6 + ch_7_8

      game.copy(HB_1_2 = ch_1_2, HB_3_4 = ch_3_4, HB_5_6 = ch_5_6, HB_7_8 = ch_7_8,
        HB_1_4 = ch_1_4, HB_5_8 = ch_5_8)
    } else {
      val ch_1_2 = varFn(3, game.HB_1_2, false)
      val ch_3_4 = varFn(3, game.HB_3_4, false)
      val ch_5_6 = varFn(16, game.HB_5_6, true)
      val ch_7_8 = varFn(20, game.HB_7_8, true)
      val ch_1_4 = ch_1_2 + ch_3_4
      val ch_5_8 = ch_5_6 + ch_7_8

      game.copy(HB_1_2 = ch_1_2, HB_3_4 = ch_3_4, HB_5_6 = ch_5_6, HB_7_8 = ch_7_8,
        HB_1_4 = ch_1_4, HB_5_8 = ch_5_8)
    }
  }

  def randomVarRed(target: Int, current: Int, notUsed: Boolean): Int = {
    val sign = if (target - current < 0) -1 else 1
    Random.nextInt(math.abs(target - current) + 1)*sign + current  // Plus one because nextInt is [0-1)  (includes 0, but not 1)
  }
  def propVarReduce(lowerBound: Double, upperBound: Double, target: Int, current: Int,
                    isHighValDesirable: Boolean): Int = {
    if (current > target && isHighValDesirable) {
      // don't change
      current
    } else if (current < target && !isHighValDesirable) {
      // don't change
      current
    } else if (current == target) {
      // don't change
      current
    } else {
      // change
      val sign = if (target < current) -1 else 1
      // reduce between lower% - upper% of variance
      val diff = math.abs(target - current)
      val (low, upp) = (math.round(diff*lowerBound).toInt, math.round(diff*upperBound).toInt)
      (Random.nextInt(upp-low + 1) + low)*sign + current  // Plus one because nextInt is [0-1)  (includes 0, but not 1)
    }


  }
  def propVarRed2(target: Int, current: Int): Int = {
      val sign = if (target - current < 0) -1 else 1
      // reduce between 25- 75% of variance
      val diff = math.abs(target - current)
      val (low, upp) = (math.round(diff*0.25).toInt, math.round(diff*0.75).toInt)
      (Random.nextInt(upp-low + 1) + low)*sign + current  // Plus one because nextInt is [0-1)  (includes 0, but not 1)
    }
  def propVarReduce3(target: Int, current: Int): Int = {
        val sign = if (target - current < 0) -1 else 1
        // reduce between 50- 90% of variance
        val diff = math.abs(target - current)
        val (low, upp) = (math.round(diff*0.50).toInt, math.round(diff*0.90).toInt)
        (Random.nextInt(upp-low + 1) + low)*sign + current  // Plus one because nextInt is [0-1)  (includes 0, but not 1)
      }

}

object FinalGameRecord {
  def getHeadings = "\"manip\",\"dep_1_4\",\"dep_5_8\",\"hb_1_2\",\"hb_3_4\",\"hb_5_6\",\"hb_7_8\",\"hb_1_4\",\"hb_5_8\"," +
    "\"PR_2_Avg_A\",\"PR_4_Avg_A\",\"PR_6_Avg_A\",\"PR_8_Avg_A\""
}
case class FinalGameRecord(manip: Int, HB_1_2: Int, HB_3_4: Int, HB_5_6: Int, HB_7_8: Int,
                           HB_1_4: Int, HB_5_8: Int,
                           PR_2_Avg_A: Double, PR_4_Avg_A: Double, PR_6_Avg_A: Double,
                           PR_8_Avg_A: Double) {

  // dep 0 = low level of dep
  // dep 1 = high level of dep
  override def toString = {
    val dep_1_4 = if (manip == 1) 1 else 0
    val dep_5_8 = if (manip == 1) 0 else 1
    manip + "," + dep_1_4 + "," + dep_5_8 + "," + HB_1_2 + "," + HB_3_4 + "," +
      HB_5_6 + "," + HB_7_8 + "," + HB_1_4 + "," + HB_5_8 + "," + PR_2_Avg_A + "," + PR_4_Avg_A +
      "," + PR_6_Avg_A + "," + PR_8_Avg_A
  }
}
