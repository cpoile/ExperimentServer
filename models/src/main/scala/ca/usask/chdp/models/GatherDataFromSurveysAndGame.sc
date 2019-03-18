
object GatherDataFromSurveysAndGame {

  import com.mongodb.casbah.Imports._
  import com.novus.salat._
  import com.novus.salat.global._
  import scala.io.Source
  import org.joda.time.DateTime
  import org.joda.time.format.DateTimeFormat
  import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
  import ca.usask.chdp.ExpServerCore.Models.GameRecord
  RegisterJodaTimeConversionHelpers()

  //  lazy val log = LoggerFactory.getLogger("SignUpSlotDAO")
  //val coll = MongoConnection()("production_signUp_gameDB")("signUpSlot_coll")
  //  val coll = MongoConnection()("production_signUp_gameDB")("participant_coll")

  def getRcds2_6 = {
    val fname = "h:/My Documents/School/Experiments/Fall 2012 Sessions/rnd2_6_survey.csv"
    val lines = Source.fromFile(fname).getLines().toList
    val headers = lines(0)
    val data = lines.drop(1)
    val dateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm")
    for (line <- data) yield {
      val cols = line.split(",")
      val time = dateFormatter.parseDateTime(cols(0))
      val prArr = Array(cols(4).toInt, cols(5).toInt, cols(6).toInt, cols(7).toInt)
      MongoDBObject("_created_at" -> time, "role" -> cols(1), "globalId" -> cols(2),
        "gameId" -> cols(3), "PR_1to4" -> prArr, "PRAvg" -> prArr.sum.toDouble / prArr.length)
    }
  }
  def getRcds4 = {
    val fname = "h:/My Documents/School/Experiments/Fall 2012 Sessions/rnd4_survey.csv"
    val lines = Source.fromFile(fname).getLines().toList
    val headers = lines(0)
    val data = lines.drop(1)
    val dateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm")
    for (line <- data) yield {
      val cols = line.split(",")
      val time = dateFormatter.parseDateTime(cols(0))
      val prArr = Array(cols(4).toInt, cols(5).toInt, cols(6).toInt, cols(7).toInt)
      val gcArr = Array(cols(8).toInt, cols(9).toInt, cols(10).toInt)
      val tnArr = Array(cols(11).toInt, cols(12).toInt, cols(13).toInt)
      MongoDBObject("_created_at" -> time, "role" -> cols(1), "globalId" -> cols(2),
        "gameId" -> cols(3), "PR_1to4" -> prArr, "PRAvg" -> prArr.sum.toDouble / prArr.length,
        "GC_1to3" -> gcArr, "GCAvg" -> gcArr.sum.toDouble / gcArr.length,
        "TN_1to3" -> tnArr, "TNAvg" -> tnArr.sum.toDouble / tnArr.length)
    }
  }
  def getRcds8For(role: String): List[DBObject] = {
    val fname = role match {
      case "A" => "h:/My Documents/School/Experiments/Fall 2012 Sessions/rnd8_survey_A.csv"
      case "B" => "h:/My Documents/School/Experiments/Fall 2012 Sessions/rnd8_survey_B.csv"
    }
    val lines = Source.fromFile(fname).getLines().toList
    val headers = lines(0)
    val data = lines.drop(1)
    val dateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm")
    for (line <- data) yield {
      val cols = line.split(",")
      val time = dateFormatter.parseDateTime(cols(0))
      val prArr = Array(cols(4).toInt, cols(5).toInt, cols(6).toInt, cols(7).toInt)
      val gcArr = Array(cols(8).toInt, cols(9).toInt, cols(10).toInt)
      val tnArr = Array(cols(11).toInt, cols(12).toInt, cols(13).toInt)
      val rlArr = Array(cols(14).toInt, cols(15).toInt, cols(16).toInt)
      val depArr = Array(cols(17), cols(18))
      val ffArr = Array(cols(19), cols(20), cols(21), cols(22), cols(23))
      MongoDBObject("_created_at" -> time, "role" -> cols(1), "globalId" -> cols(2),
        "gameId" -> cols(3), "PR_1to4" -> prArr, "PRAvg" -> prArr.sum.toDouble / prArr.length,
        "GC_1to3" -> gcArr, "GCAvg" -> gcArr.sum.toDouble / gcArr.length,
        "TN_1to3" -> tnArr, "TNAvg" -> tnArr.sum.toDouble / tnArr.length,
        "RL_1to3" -> rlArr, "RLAvg" -> rlArr.sum.toDouble / rlArr.length,
        "DEP_1to2" -> depArr, "FF_1to5" -> ffArr)
    }
  }
  def getRcds8: List[DBObject] = {
    getRcds8For("A") ::: getRcds8For("B")
  }

  def extractFirstPlayer(plrs: List[DBObject]): List[(String, String, String, DBObject)] = {
    val (gameId, playerId, role) = (plrs(0).as[String]("gameId"),
      plrs(0).as[String]("globalId"), plrs(0).as[String]("role"))
    val allSurveys = plrs.filter(r =>
      r.as[String]("gameId") == gameId &&
        r.as[String]("globalId") == playerId && r.as[String]("role") == role)
    // should always have 4 -- 2, 4, 6, 8
    assert(allSurveys.length == 4, "allSurveys.length should have been 4, it was: " + allSurveys.length)
    val sorted = allSurveys.sortWith(
      (O1, O2) => O1.as[DateTime]("_created_at").isBefore(O2.as[DateTime]("_created_at")))
    val rl = if (role == "RoleA") "_A" else "_B"
    val bldr = MongoDBObject.newBuilder
    bldr += "PR_2" + rl -> sorted(0).as[Array[Int]]("PR_1to4")
    bldr += "PR_4" + rl -> sorted(1).as[Array[Int]]("PR_1to4")
    bldr += "GC_4" + rl -> sorted(1).as[Array[Int]]("GC_1to3")
    bldr += "TN_4" + rl -> sorted(1).as[Array[Int]]("TN_1to3")
    bldr += "PR_6" + rl -> sorted(2).as[Array[Int]]("PR_1to4")
    bldr += "PR_8" + rl -> sorted(3).as[Array[Int]]("PR_1to4")
    bldr += "GC_8" + rl -> sorted(3).as[Array[Int]]("GC_1to3")
    bldr += "TN_8" + rl -> sorted(3).as[Array[Int]]("TN_1to3")
    bldr += "RL_8" + rl -> sorted(3).as[Array[Int]]("RL_1to3")
    bldr += "DEP_8" + rl -> sorted(3).as[Array[Int]]("DEP_1to2")
    bldr += "FF_8" + rl -> sorted(3).as[Array[String]]("FF_1to5")

    val remLst = plrs.filterNot(r =>
      r.as[String]("gameId") == gameId &&
        r.as[String]("globalId") == playerId && r.as[String]("role") == role)
    assert(remLst.length == (plrs.length - 4), "remLst.length should have been: " + remLst.length + " but it was: " + remLst.length)
    if (remLst.length == 0)
      (gameId, playerId, role, bldr.result()) :: Nil
    else
      (gameId, playerId, role, bldr.result()) :: extractFirstPlayer(remLst)
  }

  // Returns (gameId, playerId, role, suveyData)
  def extractAllSurveysPerPlayer(plrs: List[DBObject]): List[(String, String, String, DBObject)] = {
    // this should be refactored into: extractFirstPlayer extracts another player, and this function
    // does the recursion and assembly. But it's not that big a deal now.
    extractFirstPlayer(plrs)
  }

  // returns: List[(globalID, DBObject of survey answers)]
  def getRcdsPreSurvey(plrID_to_role: Map[String, String]): List[(String, DBObject)] = {
    val fname = "h:/My Documents/School/Experiments/Fall 2012 Sessions/pre_experiment_survey.csv"
    val lines = Source.fromFile(fname, "Windows-1252").getLines().toList
    // first two lines are the index and the column headers
    val headers = lines(1)
    val data = lines.drop(2)
    val dateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm")
    val completionTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss")
    // go through each line of the pre survey, but proceed only if that global id eventually
    // participated in the experiment.
    for (line <- data if plrID_to_role.exists(_._1 == line.split(",")(1))) yield {
      val cols = line.split(",")
      val role = if (plrID_to_role(cols(1)) == "RoleA") "_A" else "_B"
      val created_at = dateFormatter.parseDateTime(cols(0))
      val globalId = cols(1)
      val completion_time = completionTimeFormatter.parseDateTime(cols(2))
      val hexaco = for (idx <- 3 to 62) yield cols(idx)
      val gelfand = for (idx <- 63 to 68) yield cols(idx)
      val cultural_iq = for (idx <- 69 to 88) yield cols(idx)
      val demographic = for (idx <- 89 to 96) yield cols(idx)
      val cross_cultural_ff = for (idx <- 97 to 99) yield cols(idx)

      (globalId, MongoDBObject(("pre_survey_created_at" + role) -> created_at,
        ("completion_time" + role) -> completion_time, ("hexaco" + role) -> hexaco,
        ("gelfand" + role) -> gelfand,
        ("cultural_iq" + role) -> cultural_iq, ("demographic" + role) -> demographic,
        ("cross_cultural_ff" + role) -> cross_cultural_ff))
    }
  }

  // Returns List(gameId, playerId, role, suveyData)
  def gatherAllSurveyData(surveyDataFromPreGame: List[(String, DBObject)],
                          surveyDataFromInGame: List[(String, String, String, DBObject)]):
    List[(String, String, String, DBObject)]= {
    // now add the pre experiment data
    for (plr <- surveyDataFromInGame) yield {
      assert(surveyDataFromPreGame.exists(_._1 == plr._2), "presurveys should have contained plr Id: " + plr._2 + " but did not.")
      val preSurvey = surveyDataFromPreGame.find(_._1 == plr._2).get
      (plr._1, plr._2, plr._3, plr._4 ++ preSurvey._2)
    }
  }

  def create_allData_coll() {
    val grcoll = MongoConnection()("f2012_exp")("gameRecord_coll")
    val msgcoll = MongoConnection()("f2012_exp")("msg_coll")
    val pcoll = MongoConnection()("f2012_exp")("participant_coll")
    val ccoll = MongoConnection()("f2012_signUp")("courses_coll")

    // now sort and combine by player
    val allRecs = getRcds2_6 ::: getRcds4 ::: getRcds8

    // create map of id to role.
    val gameSurveyData = extractAllSurveysPerPlayer(allRecs)
    val idToRole = (for (p <- gameSurveyData) yield (p._2, p._3)).toMap
    val preSurveys = getRcdsPreSurvey(idToRole)


    // This gives us all the survey data in the form (gameId, playerId, role, surveyAnswers)
    val surveyData = gatherAllSurveyData(preSurveys, gameSurveyData)

    // Insert into new "all-data" database
    // Feb 22 2013 -- changed to outputting arrays and messages/words per round (instead of every
    //        other round. So changed to f2012_exp allData_coll
    //val allDatacoll = MongoConnection()("production_expDB")("allData_coll")
    val allDatacoll = MongoConnection()("f2012_exp")("allData_coll")

    // Now gather all session data and output to a new database
    // This will be the format:
    // manip, hb1_2, hb3_4, hb1_4, hb5_6, hb7_8, hb5_8, hb1_8,
    //        msgA1_2, msgA3_4, msgA1_4, msgA5_6, msgA7_8, msgA5_8, msgA1_8,
    //        msgB1_2, msgB3_4, msgB1_4, msgB5_6, msgB7_8, msgB5_8, msgB1_8,
    //        msgAll1_2, msgAll3_4, msgAll1_4, msgAll5_6, msgAll7_8, msgAll5_8, msgAll1_8,
    //        frA2, frA4, frA6, frA8,
    //        svoA, svoB,
    //        psA? psB?
    //
    // Changed to return hb_1, hb_2, etc. Later we can create new variables out of those if we need to.
    //
    // TODO: Could use time to complete sections of each round as an IV
    //
    // Remember -- manipulation 1 = highDep -> lowDep; man 2 = lowDep -> highDep
    //
    for (manip <- 1 to 2) {
      val manipFactor = if(manip == 1) "highToLow" else "lowToHigh"
      val q = MongoDBObject("manipulation" -> manip)
      val resL = grcoll.find(q).toList.map(grater[GameRecord].asObject(_))
      for (gr <- resL) {
        val gameId = gr.gr_id
        val bldr = MongoDBObject.newBuilder
        // game's list of helpbehs per round
        val sortedHB = gr.helpfulBehsPerRound.toList.sortBy(_._1).map(_._2)
        bldr += "hb" -> sortedHB.toArray

        val numMsgA = for (rnd <- 1 to 8) yield {
          val msgQ1 = MongoDBObject("gameParentID" -> gameId, "round" -> ("Rnd" + rnd),
            "senderRole" -> "RoleA")
          msgcoll.find(msgQ1).toList.length
        }
        val numWordsA = for (rnd <- 1 to 8) yield {
          val msgQ1 = MongoDBObject("gameParentID" -> gameId, "round" -> ("Rnd" + rnd),
            "senderRole" -> "RoleA")
          val allMsgsIn1Rnds = msgcoll.find(msgQ1).toList
          allMsgsIn1Rnds.map(_.as[String]("msg")).mkString(" ").split(" ").filterNot(_ == "").length
        }
        val numMsgB = for (rnd <- 1 to 8) yield {
          val msgQ1 = MongoDBObject("gameParentID" -> gameId, "round" -> ("Rnd" + rnd),
            "senderRole" -> "RoleB")
          msgcoll.find(msgQ1).toList.length
        }
        val numWordsB = for (rnd <- 1 to 8) yield {
          val msgQ1 = MongoDBObject("gameParentID" -> gameId, "round" -> ("Rnd" + rnd),
            "senderRole" -> "RoleB")
          val allMsgsIn1Rnds = msgcoll.find(msgQ1).toList
          allMsgsIn1Rnds.map(_.as[String]("msg")).mkString(" ").split(" ").filterNot(_ == "").length
        }
        bldr += "numMsg_A" -> numMsgA.toArray
        bldr += "numWords_A" -> numWordsA.toArray
        bldr += "numMsg_B" -> numMsgB.toArray
        bldr += "numWords_B" -> numWordsB.toArray

        // now we have the games stats, lets add the player's survey data
        // surveyData is of the form (gameId, playerId, role, surveyAnswers)
        val twoPlayers = surveyData.filter(_._1 == gameId)
        assert(twoPlayers.length == 2 && twoPlayers(0)._3 != twoPlayers(1)._3,
          "twoPlayers.length == 2; twoPlayers.length was: " + twoPlayers.length +
            " and twoPlayers(0)._3 != twoPlayers(1)._3, where twoPlayers(0)._3 = " + twoPlayers(0)._3 + " and " +
            "twoPlayers(1)._3 = " + twoPlayers(1)._3)
        val (plr1, plr2) = if (twoPlayers(0)._2 == gr.p1globalId)
          (twoPlayers(0), twoPlayers(1))
        else
          (twoPlayers(1), twoPlayers(0))
        assert(plr1._2 == gr.p1globalId && plr2._2 == gr.p2globalId && plr1._3 == gr.p1Role && plr2._3 == gr.p2Role,
          "plr1._2 == gr.p1globalId && plr2._2 == gr.p2globalId && plr1._3 == gr.p1Role && plr2._3 == gr.p2Role --- but: " + "\n" +
            "plr1._2 = " + plr1._2 + "\n" +
            "gr.p1globalId = " + gr.p1globalId + "\n" +
            "plr2._2 = " + plr2._2 + "\n" +
            "gr.p2globalId = " + gr.p2globalId + "\n" +
            "plr1._3 = " + plr1._3 + "\n" +
            "gr.p1Role = " + gr.p1Role + "\n" +
            "plr2._3 = " + plr2._3 + "\n" +
            "gr.p2Role = " + gr.p2Role)
        val (roleA, roleB) = if (plr1._3 == "RoleA") (plr1, plr2) else (plr2, plr1)

        // so we have the right players, let's assemble the final game record.
        val finalRecord = MongoDBObject("gameId" -> gameId, "roleA_Id" -> roleA._2, "roleB_Id" -> roleB._2,
          "manip" -> manipFactor, "location" -> "colocated") ++ bldr.result() ++ roleA._4 ++ roleB._4

        allDatacoll += finalRecord
      } // we finished this game record in this manipulation
    }
  }
  create_allData_coll()
}

object PullOutData_Orig {
  // 08/06/2013 (June 8th)
  // The above has been completed -- no need to do it again. In fact, don't or else you'll get
  // duplicate game documents.
  // There are now 74 documents in f2012_exp/allData_coll
  // each has a ton of info.
  // run: db.allData_coll.findOne() to see a game's record.
  // now we can pull out the data we want to look at.
  //
  // Manipulation is coded by string: lowToHigh and highToLow

  import com.mongodb.casbah.query.Imports._
  import com.mongodb.casbah.MongoConnection
  import org.joda.time.DateTime
  import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
  import com.novus.salat._
  import com.novus.salat.global._
  RegisterJodaTimeConversionHelpers()

  val filename = "f2012_1GamePerRow_data_summary.csv"

  def pull_out_data() {
    val allDatacoll = MongoConnection()("f2012_exp")("allData_coll")
    val partcoll = MongoConnection()("f2012_exp")("participant_coll")
    val partlist = partcoll.find().toList

    // output to a file
    val out = new java.io.PrintWriter(filename)
    val rawData = allDatacoll.find().toList
    val headings = List("gameId", "manip", "location",
      "hb_1", "hb_2", "hb_3", "hb_4", "hb_5", "hb_6", "hb_7", "hb_8",
      "PRAQ1_2", "PRAQ2_2", "PRAQ3_2", "PRAQ4_2",
      "PRAQ1_4", "PRAQ2_4", "PRAQ3_4", "PRAQ4_4",
      "GCA1_4", "GCA2_4", "GCA3_4", "TNA1_4", "TNA2_4", "TNA3_4",
      "PRBQ1_4", "PRBQ2_4", "PRBQ3_4", "PRBQ4_4",
      "GCB1_4", "GCB2_4", "GCB3_4", "TNB1_4", "TNB2_4", "TNB3_4",
      "PRAQ1_6", "PRAQ2_6", "PRAQ3_6", "PRAQ4_6",
      "PRAQ1_8", "PRAQ2_8", "PRAQ3_8", "PRAQ4_8",
      "GCA1_8", "GCA2_8", "GCA3_8", "TNA1_8", "TNA2_8", "TNA3_8", "RLA1_8", "RLA2_8", "RLA3_8",
      "GCB1_8", "GCB2_8", "GCB3_8", "TNB1_8", "TNB2_8", "TNB3_8", "RLB1_8", "RLB2_8", "RLB3_8",
      "msgsA_1", "msgsA_2",  "msgsA_3", "msgsA_4", "msgsA_5", "msgsA_6", "msgsA_7", "msgsA_8",
      "msgsB_1", "msgsB_2",  "msgsB_3", "msgsB_4", "msgsB_5", "msgsB_6", "msgsB_7", "msgsB_8",
      "wordsA_1", "wordsA_2",  "wordsA_3", "wordsA_4", "wordsA_5", "wordsA_6", "wordsA_7", "wordsA_8",
      "wordsB_1", "wordsB_2",  "wordsB_3", "wordsB_4", "wordsB_5", "wordsB_6", "wordsB_7", "wordsB_8",
      "depA_8", "depB_8", "roleAId", "roleBId")

    // New info, added June 08 2013
    val svoLabels = List("perc", "firstSixAngle", "sessionStart", "first_item_timestamp", "altr_value", "indiv_value", "ineqav_value", "jointgain_value", "firstSixCat", "secondRes", "transitHolds")


    // presurvey info is: complTime, hexaco, gelfand, cultural iq, demographic Qs, preSurvey free-form Qs,
    // SVS questions, SVO summary
    val preSurvHeadings = (for (r <- List("A", "B")) yield {
      List("complTime" + r) ++ (for(i <- 1 to 60) yield "HEX" + r + "Q" + i) ++ (for (i <- 1 to 6) yield "GELF"+r+"Q" + i) ++ (for (i <- 1 to 20) yield "CULIQ"+r+"Q" + i) ++ (for (i <- 1 to 8) yield "DEMO"+r+"Q" + i) ++
      // *************** NOTE: not outputting freeform here; too much of a pain.
        //(for (i <- 1 to 3) yield "PREFF"+r+"Q" + i) ++
        (for (i <- 1 to 57) yield "SVS"+r+"Q" + i) ++ svoLabels.map(_ + r)
    }).flatten

    out.println((headings ++ preSurvHeadings).mkString(","))


    def svslistToSortedSvsList(l: Seq[(Int, Int)]) = {
      // (ques, answ) pairs
      l.sortBy(_._1).map(_._2).toList
    }
    def dbListToSvsList(dbl: MongoDBList) = {
      val dbolist = dbl.map(x => x.asInstanceOf[DBObject])
      val list = for (o <- dbolist) yield {
        (o.as[Int]("qNum"), o.as[Int]("qAns"))
      }
      svslistToSortedSvsList(list)
    }

    for (r <- rawData) {

      // FOR TESTING:
      //val r = rawData(1)

      val info = r.as[String]("gameId") :: r.as[String]("manip"):: r.as[String]("location") :: r.as[MongoDBList]("hb").toList ::: Nil
      val prAB = (r.as[MongoDBList]("PR_2_A") ++ r.as[MongoDBList]("PR_4_A") ++ r.as[MongoDBList]("GC_4_A") ++ r.as[MongoDBList]("TN_4_A") ++ r.as[MongoDBList]("PR_4_B") ++ r.as[MongoDBList]("GC_4_B") ++ r.as[MongoDBList]("TN_4_B") ++ r.as[MongoDBList]("PR_6_A") ++ r.as[MongoDBList]("PR_8_A") ++ r.as[MongoDBList]("GC_8_A") ++ r.as[MongoDBList]("TN_8_A") ++ r.as[MongoDBList]("RL_8_A") ++ r.as[MongoDBList]("GC_8_B") ++ r.as[MongoDBList]("TN_8_B") ++ r.as[MongoDBList]("RL_8_B") ++ r.as[MongoDBList]("numMsg_A") ++ r.as[MongoDBList]("numMsg_B") ++ r.as[MongoDBList]("numWords_A") ++ r.as[MongoDBList]("numWords_B")).toList

      val roles = r.as[String]("roleA_Id") :: r.as[String]("roleB_Id") :: Nil

      // first make sure we only have one part by this id:
      val bothRolePreSurveyData = (for (roleNum <- 0 to 1) yield {
        val role = if(roleNum == 0) "A" else "B"

        val rolePreSurv = List(r.as[DateTime]("completion_time_"+role).toString()) ++ r.as[MongoDBList]("hexaco_"+role) ++ r.as[MongoDBList]("gelfand_"+role) ++ r.as[MongoDBList]("cultural_iq_"+role) ++ r.as[MongoDBList]("demographic_"+role)
        // not outputting FF
        //++ r.as[MongoDBList]("cross_cultural_ff_"+role)

        val roleList = partlist.find(_.as[String]("globalId") == roles(roleNum))
        assert(roleList.size == 1)
        val part = roleList.head

        val svsObj = part.getAs[MongoDBList]("survey2Data")
        val svsList = if (!svsObj.isDefined) {
          println(roles(roleNum) + " does not have survey2Data")
          List.fill(57)(99)
        } else {
          dbListToSvsList(svsObj.get)
        }

        val svoObj = part.getAs[DBObject]("survey1DataSummary")
        val svoList = if (!svoObj.isDefined) {
          println(roles(roleNum) + " does not have survey1Data")
          List.fill(11)("NA")
        } else {
          List(svoObj.get.as[Int](svoLabels(0)), svoObj.get.as[Double](svoLabels(1)), svoObj.get.as[Double](svoLabels(2)), svoObj.get.as[Double](svoLabels(3)), svoObj.get.as[Double](svoLabels(4)), svoObj.get.as[Double](svoLabels(5)), svoObj.get.as[Double](svoLabels(6)), svoObj.get.as[Double](svoLabels(7)), svoObj.get.as[String](svoLabels(8)), svoObj.get.as[String](svoLabels(9)), svoObj.get.as[Int](svoLabels(10)))
        }

        // all of this role's preSurvey data:
        rolePreSurv ++ svsList ++ svoList
      }).toList.flatten


      // convert dependency to 1 or 2
      val depARaw = r.as[MongoDBList]("DEP_8_A").mkString(",")
      assert(depARaw == "1,0" || depARaw == "0,1", "depARaw for " + r.as[String]("gameId") + " was " + depARaw + " when it should have been \"1,0\" or \"0,1\"")
      val depA = if (depARaw == "1,0") 1 else 2
      val depBRaw = r.as[MongoDBList]("DEP_8_B").mkString(",")
      assert(depBRaw == "1,0" || depBRaw == "0,1", "depBRaw for " + r.as[String]("gameId") + " was " + depBRaw + " when it should have been \"1,0\" or \"0,1\"")
      val depB = if (depBRaw == "1,0") 1 else 2


      out.println((info ::: prAB ::: depA :: depB :: roles ::: bothRolePreSurveyData).mkString(","))
    }

    out.close()
  }

  // and run like so:
  // pull_out_data()
}

object AssembleParticipantList_f2012 {
  // The following is used to generate the list of attendees.
  // We don't need it anymore, so it's commented out.
  //
  //  val sess = List("2012-12-03-sess5", "2012-12-03-sess6", "2012-12-03-sess8", "2012-12-03-sess9",
  //    "2012-12-04-sess2", "2012-12-04-sess4", "2012-12-05-sess1", "2012-12-05-sess2", "2012-12-05-sess3")
  //
  //  val sess_emailCourse = (for (s <- sess) yield {
  //    val q = MongoDBObject("expSessionID" -> s)
  //    val res = grcoll.find(q)
  //    val resL = res.toList.map(grater[GameRecord].asObject(_))
  //    val sessPartList = resL.flatMap(g => List(g.p1EncryptedEmail, g.p2EncryptedEmail)).sorted
  //    val emailCourseSurcompl = for (p <- sessPartList) yield {
  //      val q = MongoDBObject("_id" -> p)
  //      var courseID = "NO COURSE ID"
  //      var complSurveys = false
  //      pcoll.findOne(q).map(grater[ParticipantBean].asObject(_)).foreach {
  //        p =>
  //          courseID = p.courseId
  //          complSurveys = (p.surveyCompl.getOrElse("survey1", false) &&
  //            p.surveyCompl.getOrElse("survey2", false) &&
  //            p.surveyCompl.getOrElse("survey3", false))
  //      }
  //      (p, courseID, complSurveys)
  //    }
  //    (s -> emailCourseSurcompl)
  //  }).toMap
  //
  //  val file = new java.io.PrintWriter("sess_emailCourse.txt")
  //  for (kv <- sess_emailCourse) {
  //    file.println("Session: " + kv._1 + "\n\nCourseID,Email,Surveys Completed?\n")
  //    for (p <- kv._2) {
  //      file.println(p._1 + "," + p._2 + "," + p._3)
  //    }
  //    file.println("Total: " + kv._2.length)
  //    file.println("\n")
  //  }
  //  file.close()

}