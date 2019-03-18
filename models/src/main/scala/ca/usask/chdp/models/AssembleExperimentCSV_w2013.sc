import ca.usask.chdp.ExpServerCore.Models.{MsgData, GameRecord}
import ca.usask.chdp.models.{Survey1DataSummary, CourseBean, ParticipantBean}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.novus.salat._
import com.novus.salat.global._
import org.joda.time.format.DateTimeFormat
import scala.Array
import scala.io.Source

import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
RegisterJodaTimeConversionHelpers()


object w2013T1_only_do_once {
  // NOTE:
  // The below has been done. We now have data in w2012_exp.
  // Don't do it again or we will get duplicate records.
  val blackSea_part_coll = MongoConnection()("blackSea_signUp")("participant_coll")
  val blackSea_partList1 = blackSea_part_coll.find().toList.map(grater[ParticipantBean].asObject(_))

  // how many had survey3uuid from blacksea? should be none:
  // blackSea_partList1.count(_.survey3uuid != "") // == 0

  // to find that there were a few named test, I ran:
  //val notFoundIn_chdp = blackSea_partList1.map(p => (p.NSID, p.globalId)).filterNot(bs => chdp_part.exists(_.NSID == bs._1))
  //val emailsNotFoundIn_chdp = notFoundIn_chdp.flatMap(p => blackSea_partList1.find(_.NSID == p._1)).map(_.email)
  val blackSea_part2 = blackSea_partList1.filterNot(_.email.startsWith("test"))

  val blackSea_gameRec_coll = MongoConnection()("blackSea_exp")("gameRecord_coll")
  val blackSea_gameRec = blackSea_gameRec_coll.find().toList.map(grater[GameRecord].asObject(_))

  val blackSea_msg_coll = MongoConnection()("blackSea_exp")("msg_coll")
  val blackSea_msg = blackSea_msg_coll.find().toList.map(grater[MsgData].asObject(_))

  val chdp_part_coll = MongoConnection()("chdp_signUp")("participant_coll")
  val chdp_partList1 = chdp_part_coll.find().toList.map(grater[ParticipantBean].asObject(_))
  val chdp_partList2 = chdp_partList1.filterNot(_.email.startsWith("test"))

  // how many had survey3uuid?
  //chdp_partList2.count(_.survey3uuid != "") // == 191
  // should equal number who finished survey3
  //chdp_partList2.count(_.surveyCompl.getOrElse("survey3", false)) // == 190?  now: 191
  // which one wasn't counted?
  //println(chdp_partList2.find(p => p.survey3uuid != "" && !p.surveyCompl.getOrElse("survey3", false)))
  // player4  and, fixed.

  val courses_coll = MongoConnection()("chdp_signUp")("courses_coll")
  val courses = courses_coll.find().toList.map(grater[CourseBean].asObject(_))
  val chdp_part = chdp_partList2.map(
    b => b.copy(courseInfo = courses.find(_.courseId == b.courseId).get.toString))

  // still have uuids?
  //chdp_part.count(_.survey3uuid != "") // == 191
  //chdp_part.count(_.surveyCompl.getOrElse("survey3", false)) // == 191

  val chdp_gameRec_coll = MongoConnection()("chdp_exp")("gameRecord_coll")
  val chdp_gameRec = chdp_gameRec_coll.find().toList.map(grater[GameRecord].asObject(_))

  val chdp_msg_coll = MongoConnection()("chdp_exp")("msg_coll")
  val chdp_msg = chdp_msg_coll.find().toList.map(grater[MsgData].asObject(_))

  // Check that there aren't duplicate nsids...
  /*
    for (p <- blackSea_part2) {
      if (blackSea_part2.filter(pb => pb.NSID == p.NSID).size > 1) {
        println("Duplicate nsid found in blacksea:")
        blackSea_part2.filter(pb => pb.NSID == p.NSID).foreach(println)
      }
    }
  */
  // duplicates found:
  //    pes525 was registered as player19 (didn't finish) & player 20 (finished game)
  //    mrj882 -- player17 (didn't finish) & player18 (finished)
  //    nek043 - player47 (didn't finish) & player48 (finished)
  // so, remove player19, player17, player47 from blacksea
  val blackSea_duplicates = List("player19", "player17", "player47")
  val blackSea_part = blackSea_part2.filterNot(p => blackSea_duplicates.contains(p.globalId))
  assert(blackSea_part.size == blackSea_part2.size - 3)

  // have the list.
  // None finished the game.
  // Have to manually check each NSID with blackSea list to see if they finished game there.
  /*
    def finishedGameOnBlackSea(nsid: String): Boolean = {
      val part = blackSea_part.find(p => p.NSID == nsid)
      (part.isDefined && part.get.finishedGame)
    }

    // Now find dupes in chdp. and see if they finished on blackSea
    for (nsid: String <- chdp_part.map(_.NSID).toSet) {
      if (chdp_part.filter(pb => pb.NSID == nsid).size > 1) {
        println("\nDuplicate nsid found in chdp:")
        chdp_part.filter(pb => pb.NSID == nsid).foreach(
          p2 => println(
            p2.NSID + ", chdpID: " + p2.globalId + ", finished on blackSea: " +
              finishedGameOnBlackSea(p2.NSID) + ", survey3uuid: " + p2.survey3uuid + ", " + p2.email +
              ", finishedGame on chdp: " + p2.finishedGame + ", surveyrnd0: " +
              p2.internalSurveyRnd0 + ", surveysCompl: " +
              p2.surveyCompl /*+ ", in gameRecord? as game: "*/))
      }
    }
  */
  // from inspection, drop the following globalIDs from chdp because that record didn't finish on blackSea OR on chdp:
  //  - "player155", "player177", "player236", "player126", "player112", "player234", "player216", "player67", "player246"
  //
  val removeFromChdp = List(
    "player155",
    "player177",
    "player236",
    "player126",
    "player112",
    "player234",
    "player216",
    "player67",
    "player246")
  val chdp_part4 = chdp_part.filterNot(p => removeFromChdp.contains(p.globalId))
  assert(chdp_part4.size == chdp_part.size - 9)

  // still have uuids?
  //chdp_part4.count(_.survey3uuid != "") // == 191
  //chdp_part4.count(_.surveyCompl.getOrElse("survey3", false)) // == 191

  // Now we have no dupes in chdp.
  val chdp_nsid_set = chdp_part4.map(_.NSID).toSet
  assert(chdp_nsid_set.size == chdp_part4.size)

  // Now, when an nsid is in blackSea, it corresponds to an NSID on chdp.
  val blackSea_nsid_set = blackSea_part.map(_.NSID).toSet
  assert(blackSea_nsid_set.size == blackSea_part.size)
  assert(blackSea_nsid_set.diff(chdp_nsid_set).size == 0)

  // Now we need to:
  // - merge the part record from blackSea to chdp => part_merged
  // - convert blackSea gameRecord p1globalId and p2globalId to their part_merged id.  (test that emails between the gameRecord and partBean are the same
  // - give blackSea games new gameRecord Id, starting from highest chdp_gameRecord id.
  // - need to convert msg_data from blackSea; convert: gameParentId, from (globalId)
  val blackSea_merged = blackSea_part.map(
    p => chdp_part4.find(_.NSID == p.NSID).get.copy(
      finishedGame = p.finishedGame,
      internalSurveyRnd0 = p.internalSurveyRnd0))
  assert(blackSea_merged.length == blackSea_part.length)


  // 6 participants were in the blackSea db but didn't finish... I thought it was 4... okay, probably those 2 didn't match with partners.
  //blackSea_merged.filterNot(_.finishedGame).length
  //blackSea_part4.filterNot(_.finishedGame).foreach(p2 => println(p2.NSID + ", chdpID: " + p2.globalId +  ", survey3uuid: " + p2.survey3uuid + ", " + p2.email + ", finishedGame on chdp: " + p2.finishedGame + ", surveyrnd0: " + p2.internalSurveyRnd0 + ", surveysCompl: " + p2.surveyCompl))
  // Merge all records, with blackSea_merged overwriting chdp because blackSea_merged have been merged.
  val part_merged = blackSea_merged ++ chdp_part4.filterNot(p => blackSea_merged.exists(_.NSID == p.NSID))

  // still have uuids?
  //part_merged.count(_.survey3uuid != "") // == 191
  //part_merged.count(_.surveyCompl.getOrElse("survey3", false)) // == 191

  // Now make functions to map bs_id => correctId
  def blackSeaId2FinalId(bsId: String): String = {
    // blackSeaID => NSID
    //println("looking up bsId: " + bsId)
    val nsid = blackSea_part.find(_.globalId == bsId).get.NSID
    // NSID => correct globalId
    val part = part_merged.filter(_.NSID == nsid)
    assert(part.size == 1)
    part.head.globalId
  }

  // FOR USE WHEN survey 8 ids need to be converted to final IDs
  val blackSea_globalId_set = blackSea_part.map(_.globalId).toSet
  assert(blackSea_globalId_set.size == blackSea_part.size)
  val bsIdToFinalId = blackSea_part.map(p => (p.globalId, blackSeaId2FinalId(p.globalId))).toMap
  val bsidToFinalId_coll = MongoConnection()("w2013_exp")("blackSeaGlobalId_to_finalId")
  // was size: 88
  bsidToFinalId_coll += bsIdToFinalId.asDBObject

  // double check:
  //val bsidobj = bsidToFinalId_coll.find().toList.head
  //val bsidmap = (for ((k, v) <- bsidobj if k != "_id") yield (k.toString, v.toString)).toMap
  //bsidmap.size == blackSea_part.size
  // size == 88

  // remove tutorials
  //blackSea_gameRec.filter(_.p2globalId.startsWith("fake"))
  //blackSea_gameRec.filter(_.p2globalId.startsWith("fake")).size  // == 88
  val bs_gameRec = blackSea_gameRec.filterNot(_.p2globalId.startsWith("fake"))
  //bs_gameRec.size // == 43
  //blackSea_gameRec.size // == 131
  assert(bs_gameRec.size == blackSea_gameRec.size - 88)

  // now we have gameRecords with correct globalIDs
  val bs_gameRecord_corr_globalIds = bs_gameRec.map(
    gr => gr.copy(
      p1globalId = blackSeaId2FinalId(gr.p1globalId),
      p2globalId = blackSeaId2FinalId(gr.p2globalId)))

  def checkEmailsAndIdsMatch(blackSea: List[GameRecord], parts: List[ParticipantBean]) {
    blackSea.foreach {
      gr => {
        val bsEmail1 = gr.p1EncryptedEmail
        val chdpEmail1 = parts.find(_.globalId == gr.p1globalId).get.email
        assert(
          bsEmail1 == chdpEmail1,
          "bsEmail1: " + bsEmail1 + " not equal to chdpEmail1: " + chdpEmail1)
        val bsEmail2 = gr.p2EncryptedEmail
        val chdpEmail2 = parts.find(_.globalId == gr.p2globalId).get.email
        assert(
          bsEmail2 == chdpEmail2,
          "bsEmail2: " + bsEmail2 + " not equal to chdpEmail2: " + chdpEmail2)
      }
    }
  }

  // had trouble with bsEmail1: nek043
  val nek043_gr = bs_gameRecord_corr_globalIds.find(_.p1EncryptedEmail == "nek043").get
  val bs_corr_2 = nek043_gr.copy(p1EncryptedEmail = "nek043@mail.usask.ca") :: bs_gameRecord_corr_globalIds.filterNot(_.p1EncryptedEmail == "nek043")
  checkEmailsAndIdsMatch(bs_corr_2, part_merged)

  def extractId(prefix: String, str: String): Int = {
    if (str.startsWith(prefix))
      str.split(prefix)(1).toInt
    else
      0
  }
  // okay, all good.
  // Now, give every blackSea gameRecord a new gr_id
  def grIDtoInt(grID: String): Int = {
    extractId("game", grID)
  }


  var maxIdNum = chdp_gameRec.map(gr => grIDtoInt(gr.gr_id)).max
  var bsOldGameIdtoNewId = Map.empty[String, String]
  val bs_corr_3b = bs_corr_2.map(
    gr => gr.copy(
      gr_id = {
        maxIdNum += 1
        val newId = "game" + maxIdNum
        bsOldGameIdtoNewId += (gr.gr_id -> newId)
        newId
      }))
  // BUT -- have to also change _id to the new gr_id:
  val bs_corr_3 = bs_corr_3b.map(gr => gr.copy(_id = gr.gr_id))

  // And we need to save this for reference
  val bsGameIdToFinalGameId_coll = MongoConnection()("w2013_exp")("blackSeaGameId_to_finalGameId")
  bsGameIdToFinalGameId_coll += bsOldGameIdtoNewId.asDBObject

  // double check:
  //val bsGameIdFromDB = bsGameIdToFinalGameId_coll.find().toList.head
  //val bsGameIdFromDBMap = (for ((k, v) <- bsGameIdFromDB if k != "_id") yield (k.toString, v.toString)).toMap
  //bsGameIdFromDBMap.size // == 43

  // new ids work, and mapping from old -> new works
  //bs_corr_3.map(_.gr_id)
  //bsOldGameIdtoNewId
  // Now we have to go through all the msgs send in blackSea and tell them all:
  //   - new gameParentId
  //   - new from
  //   - new msgID
  // First, remove tutorials
  val blackSea_msg_noTut = blackSea_msg.filterNot(_.gameParentID.startsWith("tutorial"))
  val blackSea_msg_2 = blackSea_msg_noTut.map(
    m => m.copy(
      gameParentID = bsOldGameIdtoNewId(m.gameParentID), from = blackSeaId2FinalId(m.from)))
  // now fix msgIds
  var maxMsgIdNum = chdp_msg.map(m => extractId("msg", m._id)).max
  val blackSea_msg_3 = blackSea_msg_2.map(m => m.copy(_id = {maxMsgIdNum += 1; "msg" + maxMsgIdNum}))

  // remove chdp's tutorial msgs
  val chdp_msg_noTut = chdp_msg.filterNot(_.gameParentID.startsWith("tutorial"))

  // check that all msgIds are unique:
  val bs_msg_set = blackSea_msg_3.map(_._id).toSet
  assert(bs_msg_set.size == blackSea_msg_3.size)
  val chdp_msg_set = chdp_msg_noTut.map(_._id).toSet
  assert(chdp_msg_set.size == chdp_msg_noTut.size)

  // now combine all gameRecords and all msgs
  //chdp_gameRec.filter(_.p2globalId.startsWith("fake")).size
  val chdp_gr_noTut = chdp_gameRec.filterNot(_.p2globalId.startsWith("fake"))

  // Final collections.
  val gameRec_total2 = chdp_gr_noTut ++ bs_corr_3
  //remove one test gameRecord
  val gameRec_total = gameRec_total2.filterNot(_.p1EncryptedEmail.startsWith("test"))
  // size = 87
  //gameRec_total.size
  // test that they are all uniqueIds
  val gameRec_total_grId_set = gameRec_total.map(_.gr_id).toSet
  val gameRec_total_id_set = gameRec_total.map(_._id).toSet
  assert(gameRec_total_grId_set.size == gameRec_total.size)
  assert(gameRec_total_id_set.size == gameRec_total.size)

  val msg_total = chdp_msg_noTut ++ blackSea_msg_3 // size = 2354
  //msg_total.size
  // And remember we have total list of participants:
  //part_merged.size   // size = 227
  //part_merged.filter(_.finishedGame).size /2
  //    == 85  (plus the 6/2 games that didn't finish. == 88 game rec above. good.

  import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

  RegisterJodaTimeConversionHelpers()

  // Lets save these to Mongo, so that we don't have to do the above again, unless really necessary.
  val w2013_gameRec = MongoConnection()("w2013_exp")("gameRec")
  gameRec_total.foreach(w2013_gameRec += grater[GameRecord].asDBObject(_))
  val w2013_msg = MongoConnection()("w2013_exp")("msg")
  msg_total.foreach(w2013_msg += grater[MsgData].asDBObject(_))
  val w2013_participant = MongoConnection()("w2013_exp")("participant")
  part_merged.foreach(w2013_participant += grater[ParticipantBean].asDBObject(_))
  // Good. Counts in mongo match.
}

object pullOutParticipantList_w2013 {
  // The following is what was used to pull out the participant list to assign participation marks
  // Now, the list of participants:
  /*
  val finishedGame = part_merged.filter(p => p.finishedGame)
  def finSurveys(p: ParticipantBean) = p.surveyCompl.getOrElse("survey1", false) && p.surveyCompl.getOrElse("survey2", false) && p.surveyCompl.getOrElse("survey3", false)

  val finAll = finishedGame.filter(finSurveys)

  // To get all participants:
  //finAll.sortBy(p => (p.courseId, p.NSID)).foreach{ p =>
  //  println(p.courseInfo + ", " + p.NSID)
  //}

  // To get those who participated but didn't do the surveys:
  /*
  val notFinAll = finishedGame.filterNot(finSurveys)
  notFinAll.foreach(p => println(p.courseInfo + ", " + p.NSID))
  notFinAll.map(p => part_merged.filter(_.NSID == p.NSID))
  */
  /*
  Output:
  Comm 105, sec 06, Time: TTH 11:30-12:50 ESB 243, asg935
  Comm 105, sec 04, Time: MW 1:00-2:20 ESB 243, jar552
  Comm 105, sec 12, Time: TTH 4:00-5:20 ESB 144, mgo984
  Comm 105, sec 06, Time: TTH 11:30-12:50 ESB 243, aew177
  Comm 105, sec 08, Time: TTH 1:00-2:20 ESB 243, rtm014
  Comm 105, sec 10, Time: TTH 2:30-3:50 ESB 144, sam312
  Comm 105, sec 06, Time: TTH 11:30-12:50 ESB 243, sem405
  Comm 105, sec 02, Time: MW 11:30-12:50 ESB 243, shh791
  */
  */
}

// returns Map[globalID -> dbobject of final survey
def getRcds8For(filename: String,
  globalId_col: Int,
  surveystart_col: Int,
  blackSeaToFinalId: Map[String, String],
  blackSeaToFinalGameId: Map[String, String]): Map[String, DBObject] = {
  val src = Source.fromFile(filename)
  val lines = src.getLines().toList
  src.close()
  val headers = lines(0)
  val data = lines.drop(1)
  val dateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm")
  (for (line <- data) yield {
    val cols = line.split(",")
    val time = dateFormatter.parseDateTime(cols(0))

    // Now, if it was on April 5, convert that blackSea globalId to the actual final globalId
    val (globalId, gameId) = if (time.dayOfMonth().get == 5) {
      // this was a game on blackSea, so we need to get the actual final globalId and final gameId
      (blackSeaToFinalId(cols(globalId_col)), blackSeaToFinalGameId(cols(globalId_col + 1)))
    } else {
      (cols(globalId_col), cols(globalId_col + 1))
    }

    val ss = surveystart_col // eg, 4
    val prArr = Array(cols(ss).toInt, cols(ss + 1).toInt, cols(ss + 2).toInt, cols(ss + 3).toInt)
    val gcArr = Array(cols(ss + 4).toInt, cols(ss + 5).toInt, cols(ss + 6).toInt)
    val tnArr = Array(cols(ss + 7).toInt, cols(ss + 8).toInt, cols(ss + 9).toInt)
    val rlArr = Array(cols(ss + 10).toInt, cols(ss + 11).toInt, cols(ss + 12).toInt)
    val depArr = Array(cols(ss + 13), cols(ss + 14))
    val ffArr = Array(cols(ss + 15), cols(ss + 16), cols(ss + 17), cols(ss + 18), cols(ss + 19))

    // removing redundant data
    (globalId, MongoDBObject(
      //"_created_at" -> time,
      //"uuid" -> cols(globalId_col - 1),
      //"globalId" -> globalId,
      //"gameId" -> gameId,
      "PR_1to4" -> prArr,
      "GC_1to3" -> gcArr,
      "TN_1to3" -> tnArr,
      "RL_1to3" -> rlArr,
      "DEP_1to2" -> depArr,
      "FF_1to5" -> ffArr))
  }).toMap
}

def assembleSurvey3AndRnd8(blackSea2FinalIdMap: Map[String, String],
  blackSea2FinalGameIdMap: Map[String, String],
  participants: List[ParticipantBean],
  gameRecs: List[GameRecord]) {
  // NOTE: This has been done.  Don't do it again.
  def getRcds8: Map[String, DBObject] = {
    getRcds8For(
      "c:/Users/Chris/Documents/School/Experiments/Winter 2013 Sessions/rnd8_survey_A_2013T1.csv",
      2,
      4,
      blackSea2FinalIdMap,
      blackSea2FinalGameIdMap) ++ getRcds8For(
      "c:/Users/Chris/Documents/School/Experiments/Winter 2013 Sessions/rnd8_survey_B_2013T1.csv",
      2,
      4,
      blackSea2FinalIdMap,
      blackSea2FinalGameIdMap)
  }

  // returns: (global, DBObject of survey answers)
  def getRcdsPreSurvey(doesSurvey3uuidHaveGameRecord: (String) => Boolean,
    uuidToGlobalIdGameIdRole: (String) => (String, String, String)): Map[String, DBObject] = {
    val fname = "c:/Users/Chris/Documents/School/Experiments/Winter 2013 Sessions/pre_survey_3_2013T1.csv"

    val src = Source.fromFile(fname)
    val lines = src.getLines().toList
    src.close()
    //lines.size // == 194
    // first two lines are the index and the column headers
    val headers = lines(1)
    val data = lines.drop(2)
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
    val completionTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss")
    // go through each line of the pre survey, but proceed only if that global id eventually
    // participated in the experiment.
    // testing:
    val listOfLinesInCols = (for (line <- data) yield {
      line.split(",")
    }).toList
    //listOfLinesInCols.size // 192
    //listOfLinesInCols.count(p => doesSurvey3uuidHaveGameRecord(p(1))) // == 170


    (for (cols <- listOfLinesInCols if doesSurvey3uuidHaveGameRecord(cols(1))) yield {
      val uuid = cols(1)
      //println(uuid)
      val (globalId, gameId, fullRoleString) = uuidToGlobalIdGameIdRole(uuid)
      val role = if (fullRoleString == "RoleA") "_A" else "_B"
      val created_at = dateFormatter.parseDateTime(cols(0))
      val completion_time = completionTimeFormatter.parseDateTime(cols(2))
      val hexaco = for (idx <- 3 to 62) yield cols(idx)
      val gelfand = for (idx <- 63 to 68) yield cols(idx)
      val cultural_iq = for (idx <- 69 to 88) yield cols(idx)
      val demographic = for (idx <- 89 to 96) yield cols(idx)
      val cross_cultural_ff = for (idx <- 97 to 99) yield cols(idx)

      (globalId, MongoDBObject(
        ("pre_survey_created_at" + role) -> created_at,
        ("completion_time" + role) -> completion_time,
        ("hexaco" + role) -> hexaco,
        ("gelfand" + role) -> gelfand,
        ("cultural_iq" + role) -> cultural_iq,
        ("demographic" + role) -> demographic,
        ("cross_cultural_ff" + role) -> cross_cultural_ff))
    }).toMap
  }

  def uuidToGlobalIdGameIdRole(survey3uuid: String): (String, String, String) = {
    val part = participants.find(_.survey3uuid == survey3uuid).get
    val globalId = part.globalId
    val game = gameRecs.find(_.p1globalId == globalId)
               .getOrElse(gameRecs.find(_.p2globalId == globalId).get)
    val role = if (game.p1globalId == globalId) game.p1Role else game.p2Role
    (globalId, game.gr_id, role)
  }

  def doesSurvey3uuidHaveGameRecord(survey3uuid: String): Boolean = {
    val part = participants.find(_.survey3uuid == survey3uuid)
    part.map {
      p =>
        gameRecs.find(gr => gr.p1globalId == p.globalId || gr.p2globalId == p.globalId).isDefined
    }.getOrElse(false)
  }

  import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

  RegisterJodaTimeConversionHelpers()

  val globalIdToSurvey3Map = getRcdsPreSurvey(
    doesSurvey3uuidHaveGameRecord,
    uuidToGlobalIdGameIdRole)

  // globalIdToSurvey3Map.size // == 169
  // gameRecs.size*2 // == 174
  // find out who is Missing:
  //def idHasSurvey3uuid(id: String): Boolean = {
  //  participants.find(_.globalId == id).map(_.survey3uuid != "").getOrElse(false)
  //}
  //def idHasSurvey3uuid2(id: String): Boolean = {
  //  globalIdToSurvey3Map.get(id).isDefined
  //}
  //def partIfNoSurvey3uuid(id: String): Option[ParticipantBean] = {
  //  if (!idHasSurvey3uuid2(id)) Some(participants.find(_.globalId == id).get) else None
  //}
  //val playersWithoutSurvey3uuid = gameRecs.flatMap(gr => List(partIfNoSurvey3uuid(gr.p1globalId),  partIfNoSurvey3uuid(gr.p1globalId)).flatten)
  // There are 8 people who played but didn't fill out the survey.
  //playersWithoutSurvey3uuid.size
  //playersWithoutSurvey3uuid.map(_.globalId)
  //playersWithoutSurvey3uuid.foreach(println)

  // And save so that we don't have to do this again
  val w2013_idToSurvey3 = MongoConnection()("w2013_exp")("globalIdToSurvey3")
  for ((k, v) <- globalIdToSurvey3Map) {
    w2013_idToSurvey3 += MongoDBObject("globalId" -> k, "survey3" -> v)
  }

  val globalIdToRnd8SurveyMap = getRcds8
  //globalIdToRnd8SurveyMap.size  // == 170
  //globalIdToSurvey3Map.size   // == 169

  // And save so that we don't have to do this again
  val w2013_idToRnd8 = MongoConnection()("w2013_exp")("globalIdToRnd8SurveyMap")
  for ((k, v) <- globalIdToRnd8SurveyMap) {
    w2013_idToRnd8 += MongoDBObject("globalId" -> k, "rnd8Survey" -> v)
  }
} // FINISHED: assembleSurvey3AndRnd8


def assembleCSV_w2013() {
  // START HERE TO RECREATE DATA FILE or make a new one
  import ca.usask.chdp.ExpServerCore.Models.{MsgData, GameRecord}
  import ca.usask.chdp.models.{CourseBean, ParticipantBean, Survey1DataSummary}
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.commons.conversions.scala._
  import com.novus.salat._
  import com.novus.salat.global._
  import org.joda.time.format.DateTimeFormat
  import scala.Array
  import scala.io.Source
  import org.joda.time.DateTime

  import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
   RegisterJodaTimeConversionHelpers()

  class RichDBObject(dbObj: DBObject) {
    def asMap[T]: Map[String, T] = {
      (for((k,v) <- wrapDBObj(dbObj) if k != "_id") yield (k.toString, v.asInstanceOf[T])).toMap
    }
  }
  implicit def dbObj2RichDBObject(dbObject: DBObject): RichDBObject = new RichDBObject(dbObject)

  // Because we had to make blackSea into a server, we need to construct a survey3uuidToGlobalId map, and then a
  // globalIdToGameId map, to make sure we're assembling data from the survey out there to the right player.
  // Okay, problem is that rnd8 surveys created on April 5th -- playerIds are from the blacksea database
  // Need to:
  //    - load blacksea->part-merged map.
  //    - go through round 8 surveys and convert blackSea globalId to final globalId
  // now pull it out:
  val bsidToFinalId_coll = MongoConnection()("w2013_exp")("blackSeaGlobalId_to_finalId")
  val blackSea2FinalIdMap = bsidToFinalId_coll.find().toList.head.asMap[String]
  //blackSea2FinalIdMap.size  // == 88

  // And need to do the same with the gameIds
  val bsGameIdToFinalGameId_coll = MongoConnection()("w2013_exp")("blackSeaGameId_to_finalGameId")
  val blackSea2FinalGameIdMap = bsGameIdToFinalGameId_coll.find().toList.head.asMap[String]
  //blackSea2FinalGameIdMap.size // == 43

  // pull out db lists so we can use them below:
  val w2013_gameRec = MongoConnection()("w2013_exp")("gameRec")
  val gameRecs = w2013_gameRec.find().toList.map(grater[GameRecord].asObject(_))
  val w2013_msg = MongoConnection()("w2013_exp")("msg")
  val msgs = w2013_msg.find().toList.map(grater[MsgData].asObject(_))
  val w2013_participant = MongoConnection()("w2013_exp")("participant")
  val participants = w2013_participant.find().toList.map(grater[ParticipantBean].asObject(_))

  def getIdOfRole(gr: GameRecord, role: String): String = {
    require(role == "RoleA" || role == "RoleB")
    if(gr.p1Role == role) gr.p1globalId else gr.p2globalId
  }

  // reload idToSurvey3 from db:
  val w2013_id2Survey3 = MongoConnection()("w2013_exp")("globalIdToSurvey3")
  val idToSurvey3_2 = w2013_id2Survey3.find().toList
  val idToSurvey3 = idToSurvey3_2.map(dbo => (dbo.getAs[String]("globalId").get, dbo.getAs[DBObject]("survey3").get)).toMap

  // reload idToRnd8
  val w2013_id2Rnd8Survey = MongoConnection()("w2013_exp")("globalIdToRnd8SurveyMap")
  val idToRnd8List = w2013_id2Rnd8Survey.find().toList
  val idToRnd8Survey = idToRnd8List.map(dbo => (dbo.getAs[String]("globalId").get, dbo.getAs[DBObject]("rnd8Survey").get)).toMap

  // START ASSEMBLING DATA FILE

  // Now assemble the data we want to use in R.
  // REMEMBER: manipulation originally coded as 1 = highToLow, 2 = lowToHigh
  // We need data by game, with manipulation, and each role's survey results (pre, during, post), and
  // game data (chatmsgs, timings)
  val filename = "w2013_1GamePerRow_data_summary.csv"
  import java.io.PrintWriter
  val out = new PrintWriter(filename)
  val headings = List("gameId", "manip", "location",
    "hb_1", "hb_2", "hb_3", "hb_4", "hb_5", "hb_6", "hb_7", "hb_8",
    "PRAQ1_0", "PRAQ2_0", "PRAQ3_0", "PRAQ4_0", "OBLAQ1_0", "OBLAQ2_0", "OBLAQ3_0",
    "PRAQ1_2", "PRAQ2_2", "PRAQ3_2", "PRAQ4_2", "OBLAQ1_2", "OBLAQ2_2", "OBLAQ3_2",
    "PRAQ1_4", "PRAQ2_4", "PRAQ3_4", "PRAQ4_4", "OBLAQ1_4", "OBLAQ2_4", "OBLAQ3_4",
    "GCA1_4", "GCA2_4", "GCA3_4", "TNA1_4", "TNA2_4", "TNA3_4",
    "PRBQ1_4", "PRBQ2_4", "PRBQ3_4", "PRBQ4_4", "OBLBQ1_4", "OBLBQ2_4", "OBLBQ3_4",
    "GCB1_4", "GCB2_4", "GCB3_4", "TNB1_4", "TNB2_4", "TNB3_4",
    "PRAQ1_6", "PRAQ2_6", "PRAQ3_6", "PRAQ4_6", "OBLAQ1_6", "OBLAQ2_6", "OBLAQ3_6",
    "PRAQ1_8", "PRAQ2_8", "PRAQ3_8", "PRAQ4_8", "OBLAQ1_8", "OBLAQ2_8", "OBLAQ3_8",
    "GCA1_8", "GCA2_8", "GCA3_8", "TNA1_8", "TNA2_8", "TNA3_8", "RLA1_8", "RLA2_8", "RLA3_8",
    "GCB1_8", "GCB2_8", "GCB3_8", "TNB1_8", "TNB2_8", "TNB3_8", "RLB1_8", "RLB2_8", "RLB3_8",
    "msgsA_1", "msgsA_2",  "msgsA_3", "msgsA_4", "msgsA_5", "msgsA_6", "msgsA_7", "msgsA_8",
    "msgsB_1", "msgsB_2",  "msgsB_3", "msgsB_4", "msgsB_5", "msgsB_6", "msgsB_7", "msgsB_8",
    "wordsA_1", "wordsA_2",  "wordsA_3", "wordsA_4", "wordsA_5", "wordsA_6", "wordsA_7", "wordsA_8",
    "wordsB_1", "wordsB_2",  "wordsB_3", "wordsB_4", "wordsB_5", "wordsB_6", "wordsB_7", "wordsB_8",
    "depA_8", "depB_8",
    "roleAemail", "roleBemail", "roleAId", "roleBId")


  // New info, added June 17 2013
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

  for(gr <- gameRecs) yield {
    // Printlns are for debugging
    val printlnDEBUG = false
    // use this if we need to step through:
    //val gr = gameRecs.find(_.gr_id == "game80").get

    if(printlnDEBUG) println("starting game: " + gr.gr_id)

    val manip = if (gr.manipulation == 1) "highToLow" else "lowToHigh"
    // sessions run on Apr 5th were distributed, othewise colocated
    val location = if (gr.expSessionID.startsWith("2013-04-05")) "distributed" else "colocated"
    val sortedHB = gr.helpfulBehsPerRound.toList.sortBy(_._1).map(_._2)
    val roleAId = getIdOfRole(gr, "RoleA")
    if(printlnDEBUG) println("roleAid: " + roleAId)

    val (roleAemail, roleBemail) = if(gr.p1Role == "RoleA") (gr.p1EncryptedEmail, gr.p2EncryptedEmail) else (gr.p2EncryptedEmail, gr.p1EncryptedEmail)
    val roleBId = getIdOfRole(gr, "RoleB")
    if(printlnDEBUG) println("roleBid: " + roleBId)

    val sortedPRA_pre = participants.find(_.globalId == roleAId).get.internalSurveyRnd0.toList.sortBy(_._1).map(_._2)

    // turn survey results into a map
    if(printlnDEBUG) println("pnt1")
    if(printlnDEBUG) println("gr_id: " + gr.gr_id)
    //println("surveyResults: " + w2013_gameRec.findOneByID(gr.gr_id).get.getAs[BasicDBObject]("surveyResults").get)
    val sRes = w2013_gameRec.findOneByID(gr.gr_id).get.getAs[BasicDBObject]("surveyResults").get
    val sRes2 = sRes.getAs[DBObject]("RoleA_Rnd2").get
    val sResA4 = sRes.getAs[DBObject]("RoleA_Rnd4").get
    val sResB4 = sRes.getAs[DBObject]("RoleB_Rnd4").get
    val sRes6 = sRes.getAs[DBObject]("RoleA_Rnd6").get
    val sortedPRA_2 = (for((k,v) <- sRes2) yield (k.toString,v.toString.toInt)).toMap.toList.sortBy(_._1).map(_._2)
    val sortedPRA_4 = (for((k,v) <- sResA4) yield (k.toString,v.toString.toInt)).toMap.toList.sortBy(kv => (kv._1.length, kv._1)).map(_._2)
    val sortedPRB_4 = (for((k,v) <- sResB4) yield (k.toString,v.toString.toInt)).toMap.toList.sortBy(kv => (kv._1.length, kv._1)).map(_._2)
    val sortedPRA_6 = (for((k,v) <- sRes6) yield (k.toString,v.toString.toInt)).toMap.toList.sortBy(_._1).map(_._2)
    if(printlnDEBUG) println("pnt2")

    // the result of their choice (dep1 or dep2) was given in separate columns, but as 1 or 2, so sum
    // NOTE: some players didn't make it to the rnd8 survey. If they didn't, default to na:
    val defObj = MongoDBObject("PR_1to4" -> MongoDBList(99,99,99,99),
      "GC_1to3" -> MongoDBList(99,99,99),
      "TN_1to3" -> MongoDBList(99,99,99),
      "RL_1to3" -> MongoDBList(99,99,99),
      "DEP_1to2" -> MongoDBList("50","49"),
      "FF_1to5" -> MongoDBList("NA","NA", "NA", "NA", "NA"))

    // get from last survey (8)
    val roleARnd8Obj = idToRnd8Survey.getOrElse(roleAId,defObj)
    val roleBRnd8Obj = idToRnd8Survey.getOrElse(roleBId,defObj)

    val sortedPRA_8 = roleARnd8Obj.as[MongoDBList]("PR_1to4").toList.map(_.toString.toInt) ++ sortedPRA_6.drop(4)
    val gcA1to3 = roleARnd8Obj.as[MongoDBList]("GC_1to3").toList.map(_.toString.toInt)
    val tnA1to3 = roleARnd8Obj.as[MongoDBList]("TN_1to3").toList.map(_.toString.toInt)
    val rlA1to3 = roleARnd8Obj.as[MongoDBList]("RL_1to3").toList.map(_.toString.toInt)

    if(printlnDEBUG) println("pnt3")

    val depA_8 = roleARnd8Obj.as[MongoDBList]("DEP_1to2").toList.map(_.toString).map(v => if (v == "") 0 else v.toString.toInt).sum :: Nil
    val gcB1to3 = roleBRnd8Obj.as[MongoDBList]("GC_1to3").toList.map(_.toString.toInt)
    val tnB1to3 = roleBRnd8Obj.as[MongoDBList]("TN_1to3").toList.map(_.toString.toInt)
    val rlB1to3 = roleBRnd8Obj.as[MongoDBList]("RL_1to3").toList.map(_.toString.toInt)
    val depB_8 = roleBRnd8Obj.as[MongoDBList]("DEP_1to2").toList.map(_.toString).map(v => if (v == "") 0 else v.toString.toInt).sum :: Nil

    if(printlnDEBUG) println("pnt3")

    // final info from game record
    val msgsA_before_check = gr.chatMsgsPerRoundFromA.toList.sortBy(_._1).map(_._2)
    val msgsB_before_check = gr.chatMsgsPerRoundFromB.toList.sortBy(_._1).map(_._2)
    // for the games that don't finish
    val fillA = List.fill(8 - msgsA_before_check.size)(99)
    val fillB = List.fill(8 - msgsB_before_check.size)(99)

    val msgsA = msgsA_before_check ::: fillA
    val msgsB = msgsB_before_check ::: fillB

    // now get msg words
    val allMsgsFromA = msgs.filter(m => m.gameParentID == gr.gr_id && m.senderRole == "RoleA")
    val allMsgsFromAPerRnd = for(i <- (1 to 8).toList) yield allMsgsFromA.filter(_.round=="Rnd" + i)
    //allMsgsFromAPerRnd.map(_.length)
    val allMsgsFromB = msgs.filter(m => m.gameParentID == gr.gr_id && m.senderRole == "RoleB")
    val allMsgsFromBPerRnd = for(i <- (1 to 8).toList) yield allMsgsFromB.filter(_.round=="Rnd" + i)
    //allMsgsFromBPerRnd.map(_.length)

    // Take the msg record as the truth
    // I could just replace it, without the if check.
    val msgsACorr = if (msgsA != allMsgsFromAPerRnd.map(_.length)) {
      println("MADE A CORRECTION. gameRecord msg count replaced with msgRecord msg count. Details: ")
      println("gameId: " + gr.gr_id + " -- GameRecord / msgRecord total msgs A: equal? " + (msgsA == allMsgsFromAPerRnd.map(_.length)) + " -- " + msgsA.sum + "/" + allMsgsFromAPerRnd.map(_.length).sum)
      allMsgsFromAPerRnd.map(_.length)
    } else msgsA

    val msgsBCorr = if (msgsB != allMsgsFromBPerRnd.map(_.length)) {
      println("MADE A CORRECTION. gameRecord msg count replaced with msgRecord msg count. Details: ")
      println("gameId: " + gr.gr_id + " -- GameRecord / msgRecord total msgs B: equal? " + (msgsB == allMsgsFromBPerRnd.map(_.length)) + " -- " + msgsB.sum + "/" + allMsgsFromBPerRnd.map(_.length).sum )
      allMsgsFromBPerRnd.map(_.length)
    } else msgsB

//    println("gameId: " + gr.gr_id + " -- GameRecord says msgsA = " + msgsA + ";  msg records say msgsA = " + allMsgsFromAPerRnd.map(_.length))
//    println("gameId: " + gr.gr_id + " -- GameRecord says msgsB = " + msgsB + ";  msg records say msgsB = " + allMsgsFromBPerRnd.map(_.length))
//    println("gameId: " + gr.gr_id + " -- GameRecord / msgRecord total msgs A -- B: equal? " + (msgsA == allMsgsFromAPerRnd.map(_.length) && msgsB == allMsgsFromBPerRnd.map(_.length)) + " -- " + msgsA.sum + "/" + allMsgsFromAPerRnd.map(_.length).sum + " -- " + msgsB.sum + "/" + allMsgsFromBPerRnd.map(_.length).sum )


    val wordsA = allMsgsFromAPerRnd.map(_.flatMap(_.msg.split(" ")).length)
    val wordsB = allMsgsFromBPerRnd.map(_.flatMap(_.msg.split(" ")).length)
//    val allMsgsFromB = msgs.filter(m => m.gameParentID == gr.gr_id && m.senderRole == "RoleB")
//    allMsgsFromB.length
    //val wordsA_before_check =


    // Now get survey3 data.
    val roleIds = Array(roleAId, roleBId)
    val bothRolePreSurveyData = (for(roleNum <- 0 to 1) yield {
      // testing:
      // val roleNum = 0
      val role = if(roleNum == 0) "A" else "B"
      val r = idToSurvey3.get(roleIds(roleNum))
      val rolePreSurv = if (!r.isDefined) {
        println(roleIds(roleNum) + " does not have survey3Data")
        List.fill(95)("NA")
      } else {
        List(r.get.as[DateTime]("completion_time_"+role).toString()) ++ r.get.as[MongoDBList]("hexaco_"+role) ++ r.get.as[MongoDBList]("gelfand_"+role) ++ r.get.as[MongoDBList]("cultural_iq_"+role) ++ r.get.as[MongoDBList]("demographic_"+role)
      // not outputting FF
      //++ r.as[MongoDBList]("cross_cultural_ff_"+role)
      }

      val roleList = participants.find(_.globalId == roleIds(roleNum))
      assert(roleList.size == 1)
      val part = roleList.head

      val svsQBeans = part.survey2Data
      val svsList = if (svsQBeans.size == 0) {
        println(roleIds(roleNum) + " does not have survey2Data")
        List.fill(57)("NA")
      } else {
        svsQBeans.sortBy(_.qNum).map(_.qAns)
      }

      val svo = part.survey1DataSummary
      val svoList = if (svo == Survey1DataSummary()) {
        println(roleIds(roleNum) + " does not have survey1Data")
        List.fill(11)("NA")
      } else {
        List(svo.perc, svo.firstSixAngle, svo.sessionStart, svo.first_item_timestamp, svo.altr_value, svo.indiv_value, svo.ineqav_value, svo.jointgain_value, svo.firstSixCat, svo.secondRes, svo.transitHolds)
      }

      // all of this role's preSurvey data:
      rolePreSurv ++ svsList ++ svoList
    }).toList.flatten

    // now combine, flatten, and make into a comma-separated row
    out.println(List(List(gr.gr_id), List(manip), List(location), sortedHB, sortedPRA_pre, sortedPRA_2,
      sortedPRA_4, sortedPRB_4, sortedPRA_6,
      sortedPRA_8, gcA1to3, tnA1to3, rlA1to3, gcB1to3, tnB1to3, rlB1to3,
      msgsACorr, msgsBCorr, wordsA, wordsB, depA_8, depB_8, List(roleAemail), List(roleBemail), List(roleAId),
      List(roleBId), bothRolePreSurveyData).flatten.mkString(","))
  }
  out.close()
}// FINISHED ASSEMBLING DATA FILE

assembleCSV_w2013()

// **** NOT UPDATED FOR WORDS *******
// updateDB_w2013()

// **** NOT UPDATED FOR WORDS *******
def updateDB_w2013() {
  // **** NOT UPDATED FOR WORDS *******
  /// START HERE TO UPDATE DATA FILE:
  import ca.usask.chdp.ExpServerCore.Models.{MsgData, GameRecord}
  import ca.usask.chdp.models.{CourseBean, ParticipantBean}
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.commons.conversions.scala._
  import com.novus.salat._
  import com.novus.salat.global._
  import org.joda.time.format.DateTimeFormat
  import scala.Array
  import scala.io.Source

  import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
  RegisterJodaTimeConversionHelpers()

  class RichDBObject(dbObj: DBObject) {
    def asMap[T]: Map[String, T] = {
      (for((k,v) <- wrapDBObj(dbObj) if k != "_id") yield (k.toString, v.asInstanceOf[T])).toMap
    }
  }
  implicit def dbObj2RichDBObject(dbObject: DBObject): RichDBObject = new RichDBObject(dbObject)

  def corr(ch: Char) = (ch == 'y' || ch == 'n')
  def yOrN(): Boolean ={
    var ans: Char = ' '
    while(!corr(ans)) {
      ans = readChar()
    }
    (ans == 'y')
  }
  def ques(gameid: String, name: String, newData: AnyRef, oldData: AnyRef): Boolean = {
    print(gameid + ": " + name + " has changed to: " + newData + "; from: " + oldData + ". Change? (y or n): ")
    yOrN()
  }

  // pull out db lists so we can use them below:
  val w2013_gameRec = MongoConnection()("w2013_exp")("gameRec")
  val gameRecs = w2013_gameRec.find().toList.map(grater[GameRecord].asObject(_))
  val w2013_msg = MongoConnection()("w2013_exp")("msg")
  val msgs = w2013_msg.find().toList.map(grater[MsgData].asObject(_))
  val w2013_participant = MongoConnection()("w2013_exp")("participant")
  val participants = w2013_participant.find().toList.map(grater[ParticipantBean].asObject(_))

  def getIdOfRole(gr: GameRecord, role: String): String = {
    require(role == "RoleA" || role == "RoleB")
    if(gr.p1Role == role) gr.p1globalId else gr.p2globalId
  }

  // reload idToSurvey3 from db:
  val w2013_id2Survey3 = MongoConnection()("w2013_exp")("globalIdToSurvey3")
  val idToSurvey3_2 = w2013_id2Survey3.find().toList
  val idToSurvey3 = idToSurvey3_2.map(dbo => (dbo.getAs[String]("globalId").get, dbo.getAs[DBObject]("survey3").get)).toMap

  // reload idToRnd8
  val w2013_id2Rnd8Survey = MongoConnection()("w2013_exp")("globalIdToRnd8SurveyMap")
  val idToRnd8List = w2013_id2Rnd8Survey.find().toList
  val idToRnd8Survey = idToRnd8List.map(dbo => (dbo.getAs[String]("globalId").get, dbo.getAs[DBObject]("rnd8Survey").get)).toMap


  // Because its easier to clean the file in csv and then reimport
  val filename = "w2013_1GamePerRow_data_summary.csv"
  val src = Source.fromFile(filename)
  val lines = src.getLines().toList
  src.close()

  // make sure we're working with the right version
  val headings = List("gameId", "manip", "location",
    "hb_1", "hb_2", "hb_3", "hb_4", "hb_5", "hb_6", "hb_7", "hb_8",
    "PRAQ1_0", "PRAQ2_0", "PRAQ3_0", "PRAQ4_0", "OBLAQ1_0", "OBLAQ2_0", "OBLAQ3_0",
    "PRAQ1_2", "PRAQ2_2", "PRAQ3_2", "PRAQ4_2", "OBLAQ1_2", "OBLAQ2_2", "OBLAQ3_2",
    "PRAQ1_4", "PRAQ2_4", "PRAQ3_4", "PRAQ4_4", "OBLAQ1_4", "OBLAQ2_4", "OBLAQ3_4",
    "GCA1_4", "GCA2_4", "GCA3_4", "TNA1_4", "TNA2_4", "TNA3_4",
    "PRBQ1_4", "PRBQ2_4", "PRBQ3_4", "PRBQ4_4", "OBLBQ1_4", "OBLBQ2_4", "OBLBQ3_4",
    "GCB1_4", "GCB2_4", "GCB3_4", "TNB1_4", "TNB2_4", "TNB3_4",
    "PRAQ1_6", "PRAQ2_6", "PRAQ3_6", "PRAQ4_6", "OBLAQ1_6", "OBLAQ2_6", "OBLAQ3_6",
    "PRAQ1_8", "PRAQ2_8", "PRAQ3_8", "PRAQ4_8", "OBLAQ1_8", "OBLAQ2_8", "OBLAQ3_8",
    "GCA1_8", "GCA2_8", "GCA3_8", "TNA1_8", "TNA2_8", "TNA3_8", "RLA1_8", "RLA2_8", "RLA3_8",
    "GCB1_8", "GCB2_8", "GCB3_8", "TNB1_8", "TNB2_8", "TNB3_8", "RLB1_8", "RLB2_8", "RLB3_8",
    "msgsA_1", "msgsA_2",  "msgsA_3", "msgsA_4", "msgsA_5", "msgsA_6", "msgsA_7", "msgsA_8",
    "msgsB_1", "msgsB_2",  "msgsB_3", "msgsB_4", "msgsB_5", "msgsB_6", "msgsB_7", "msgsB_8",
    "depA_8", "depB_8",
    "roleAemail", "roleBemail", "roleAId", "roleBId")
  val linesInColsPlusHead = lines.map(_.split(","))
  val headers = linesInColsPlusHead(0)
  val linesInCols = linesInColsPlusHead.drop(1)
  assert(headers.toList == headings, "Headers were: --" + headers.toList + "\nWere supposed to be: --" + headings)

  // now just go through each game and check if there are differences.
  // if so, print them out and ask if we want to update the db.
  for(col <- linesInCols) {
    val gameId = col(0)
    println("at: " + gameId)
    val manip = col(1)
    val location = col(2)
    val hb1to8 = col.slice(3, 11).toList.map(_.toInt)
    val pra_0 = col.slice(11, 18).toList.map(_.toInt)
    val pra_2 = col.slice(18, 25).toList.map(_.toInt)
    val pra_4 = col.slice(25, 38).toList.map(_.toInt)
    val prb_4 = col.slice(38, 51).toList.map(_.toInt)
    val pra_6 = col.slice(51, 58).toList.map(_.toInt)
    val pra_8_1to4 = col.slice(58, 65).toList.map(_.toInt)
    val gcA_1to3 = col.slice(65, 68).toList.map(_.toInt)
    val tnA_1to3 = col.slice(68, 71).toList.map(_.toInt)
    val rlA_1to3 = col.slice(71, 74).toList.map(_.toInt)
    val gcB_1to3 = col.slice(74, 77).toList.map(_.toInt)
    val tnB_1to3 = col.slice(77, 80).toList.map(_.toInt)
    val rlB_1to3 = col.slice(80, 83).toList.map(_.toInt)
    val msgsA_1to8 = col.slice(83, 91).toList.map(_.toInt)
    val msgsB_1to8 = col.slice(91, 99).toList.map(_.toInt)
    val depA_1to2 = if (col(99) == "1") List("1", "") else List("", "2")
    val depB_1to2 = if (col(100) == "1") List("1", "") else List("", "2")
    val roleAEmail = col(101)
    val roleBEmail = col(102)
    val roleAId = col(103)
    val roleBId = col(104)

    // queries
    val grQ = MongoDBObject("gr_id" -> gameId)
    val partAQ = MongoDBObject("globalId" -> roleAId)
    val partBQ = MongoDBObject("globalId" -> roleBId)

    val grObj: DBObject = w2013_gameRec.find(grQ).toList.head
    val partAObj: DBObject = w2013_participant.find(partAQ).toList.head
    val partBObj: DBObject = w2013_participant.find(partBQ).toList.head
    val (dbHb1to8Rnds, dbHb1to8) = grObj.as[DBObject]("helpfulBehsPerRound").asMap[Int].toList.sortBy(_._1).unzip
    val (dbPra_0qs, dbPra_0) = partAObj.as[DBObject]("internalSurveyRnd0").asMap[Int].toList.sortBy(_._1).unzip
    val (dbPra_2qs, dbPra_2) = grObj.as[DBObject]("surveyResults").as[DBObject]("RoleA_Rnd2").asMap[Int].toList.sortBy(_._1).unzip
    val (dbPra_4qs, dbPra_4) = grObj.as[DBObject]("surveyResults").as[DBObject]("RoleA_Rnd4").asMap[Int].toList.sortBy(kv => (kv._1.length, kv._1)).unzip
    val (dbPrb_4qs, dbPrb_4) = grObj.as[DBObject]("surveyResults").as[DBObject]("RoleB_Rnd4").asMap[Int].toList.sortBy(kv => (kv._1.length, kv._1)).unzip
    val (dbPra_6qs, dbPra_6) = grObj.as[DBObject]("surveyResults").as[DBObject]("RoleA_Rnd6").asMap[Int].toList.sortBy(_._1).unzip
    //val grPra_0 =

    val defObj = MongoDBObject("PR_1to4" -> MongoDBList(99,99,99,99),
      "GC_1to3" -> MongoDBList(99,99,99),
      "TN_1to3" -> MongoDBList(99,99,99),
      "RL_1to3" -> MongoDBList(99,99,99),
      "DEP_1to2" -> MongoDBList("50","49"),
      "FF_1to5" -> MongoDBList("NA","NA", "NA", "NA", "NA"))

    // get from last survey (8)
    val roleARnd8Obj = idToRnd8Survey.getOrElse(roleAId,defObj)
    val roleBRnd8Obj = idToRnd8Survey.getOrElse(roleBId,defObj)

    val dbPRA_8_preCheck = roleARnd8Obj.as[MongoDBList]("PR_1to4").toList.map(_.toString.toInt)
    val dbPRA_8 = if (dbPRA_8_preCheck.size == 4) dbPRA_8_preCheck ++ dbPra_6.drop(4) else dbPRA_8_preCheck

    val dbGcA1to3 = roleARnd8Obj.as[MongoDBList]("GC_1to3").toList.map(_.toString.toInt)
    val dbTnA1to3 = roleARnd8Obj.as[MongoDBList]("TN_1to3").toList.map(_.toString.toInt)
    val dbRlA1to3 = roleARnd8Obj.as[MongoDBList]("RL_1to3").toList.map(_.toString.toInt)

    val dbDepA_8 = roleARnd8Obj.as[MongoDBList]("DEP_1to2").toList.map(_.toString)
    val dbGcB1to3 = roleBRnd8Obj.as[MongoDBList]("GC_1to3").toList.map(_.toString.toInt)
    val dbTnB1to3 = roleBRnd8Obj.as[MongoDBList]("TN_1to3").toList.map(_.toString.toInt)
    val dbRlB1to3 = roleBRnd8Obj.as[MongoDBList]("RL_1to3").toList.map(_.toString.toInt)
    val dbDepB_8 = roleBRnd8Obj.as[MongoDBList]("DEP_1to2").toList.map(_.toString)

    // final info from game record
    val (dbMsgsARnds, dbMsgsA) = grObj.as[DBObject]("chatMsgsPerRoundFromA").asMap[Int].toList.sortBy(_._1).unzip
    val (dbMsgsBRnds, dbMsgsB) = grObj.as[DBObject]("chatMsgsPerRoundFromB").asMap[Int].toList.sortBy(_._1).unzip

    // Go through each. If different, tell us, and ask us.
    if(hb1to8 != dbHb1to8) {
      if (ques(gameId, "hb1to8", hb1to8, dbHb1to8))
        w2013_gameRec.update(grQ, $set("helpfulBehsPerRound" -> dbHb1to8Rnds.zip(hb1to8).toMap))
    }
    if(pra_0 != dbPra_0) {
      if (ques(gameId, "PRA_0", pra_0, dbPra_0))
        w2013_participant.update(partAQ, $set("internalSurveyRnd0" -> dbPra_0qs.zip(pra_0).toMap))
    }
    if(pra_2 != dbPra_2) {
      if (ques(gameId, "PRA_2", pra_2, dbPra_2))
        w2013_gameRec.update(grQ, $set("surveyResults.RoleA_Rnd2" -> dbPra_2qs.zip(pra_2).toMap))
    }
    if(pra_4 != dbPra_4) {
      if (ques(gameId, "PRA_4", pra_4, dbPra_4))
        w2013_gameRec.update(grQ, $set("surveyResults.RoleA_Rnd4" -> dbPra_4qs.zip(pra_4).toMap))
    }
    if(prb_4 != dbPrb_4) {
      if (ques(gameId, "PRB_4", prb_4, dbPrb_4))
        w2013_gameRec.update(grQ, $set("surveyResults.RoleB_Rnd4" -> dbPrb_4qs.zip(prb_4).toMap))
    }
    if(pra_6 != dbPra_6) {
      if (ques(gameId, "PRA_6", pra_6, dbPra_6))
        w2013_gameRec.update(grQ, $set("surveyResults.RoleA_Rnd6" -> dbPra_6qs.zip(pra_6).toMap))
    }
    // assemble all of these last ones
    val origrnd8SurveyAObj_2 = w2013_id2Rnd8Survey.find(partAQ).toList.head.as[DBObject]("rnd8Survey")
    val origrnd8SurveyAObj = MongoDBObject("rnd8Survey" -> origrnd8SurveyAObj_2)
    val origFF_1to5A = origrnd8SurveyAObj_2.as[MongoDBList]("FF_1to5")
    val rnd8MapA = Map("rnd8Survey" -> Map("PR_1to4" -> MongoDBList(pra_8_1to4.take(4):_*),
      "GC_1to3" -> MongoDBList(gcA_1to3:_*), "TN_1to3" -> MongoDBList(tnA_1to3:_*),
      "RL_1to3" -> MongoDBList(rlA_1to3:_*), "DEP_1to2" -> MongoDBList(depA_1to2:_*),
      "FF_1to5" -> origFF_1to5A))
    val rnd8ObjA = MongoDBObject(rnd8MapA.toSeq:_*)

    // are they different?
    if (rnd8ObjA != origrnd8SurveyAObj) {
      if (ques(gameId, "rnd8_A", rnd8ObjA, origrnd8SurveyAObj))
        w2013_id2Rnd8Survey.update(partAQ, $set("rnd8Survey" -> rnd8MapA("rnd8Survey")))
    }

    //    //testing:
//    val Q = MongoDBObject("globalId" -> "player198")
//    val Q2 = MongoDBObject("globalId" -> "player104")
//    val Q3 = MongoDBObject("globalId" -> "player78")
//    val Q4 = MongoDBObject("globalId" -> "player55")
//    val rnd8Map = Map("rnd8Survey" -> Map("PR_1to4" -> MongoDBList(99, 99, 99, 99),
//      "GC_1to3" -> MongoDBList(99, 99, 99), "TN_1to3" -> MongoDBList(99, 99, 99),
//      "RL_1to3" -> MongoDBList(99, 99, 99), "DEP_1to2" -> MongoDBList(1,""),
//      "FF_1to5" -> MongoDBList("NA", "NA", "NA", "NA", "NA")))
//    w2013_id2Rnd8Survey.update(Q4, $set("rnd8Survey" -> rnd8Map("rnd8Survey")))
//    w2013_id2Rnd8Survey.find(Q4).toList.head.as[DBObject]("rnd8Survey")


    // assemble all of these last ones
    val origrnd8SurveyBObj_2 = w2013_id2Rnd8Survey.find(partBQ).toList.head.as[DBObject]("rnd8Survey")
    val origrnd8SurveyBObj = MongoDBObject("rnd8Survey" -> origrnd8SurveyBObj_2)
    val origFF_1to5B = origrnd8SurveyBObj_2.as[MongoDBList]("FF_1to5")
    val origprb = origrnd8SurveyBObj_2.as[MongoDBList]("PR_1to4")
    val rnd8MapB = Map("rnd8Survey" -> Map("PR_1to4" -> origprb,
      "GC_1to3" -> MongoDBList(gcB_1to3:_*), "TN_1to3" -> MongoDBList(tnB_1to3:_*),
      "RL_1to3" -> MongoDBList(rlB_1to3:_*), "DEP_1to2" -> MongoDBList(depB_1to2:_*),
      "FF_1to5" -> origFF_1to5B))
    val rnd8ObjB = MongoDBObject(rnd8MapB.toSeq:_*)

    // are they different?
    if (rnd8ObjB != origrnd8SurveyBObj) {
      if (ques(gameId, "rnd8_B", rnd8ObjB, origrnd8SurveyBObj))
        w2013_id2Rnd8Survey.update(partBQ, $set("rnd8Survey" -> rnd8MapB("rnd8Survey")))
    }

    if(msgsA_1to8 != dbMsgsA || msgsB_1to8 != dbMsgsB) {
      if (ques(gameId, "(msgsA, msgsB)", List(msgsA_1to8, msgsB_1to8), List(dbMsgsA, dbMsgsB))) {
        w2013_gameRec.update(grQ, $set("chatMsgsPerRoundFromA" -> dbHb1to8Rnds.zip(msgsA_1to8).toMap))
        w2013_gameRec.update(grQ, $set("chatMsgsPerRoundFromB" -> dbHb1to8Rnds.zip(msgsB_1to8).toMap))
      }
    }

    // testing:
    //val grQ = MongoDBObject("gr_id" -> "game80")
    //val msgsa = w2013_gameRec.find(grQ).toList.head.as[DBObject]("chatMsgsPerRoundFromA").asMap[Int]
   //val newMsgsa = msgsa + ("Rnd8" -> 99)
     //w2013_gameRec.update(grQ, $set("chatMsgsPerRoundFromA" -> newMsgsa))

  }
}

// read in the csv and update the db
// **** NOT UPDATED FOR WORDS *******
//updateDB_w2013()
// **** NOT UPDATED FOR WORDS *******

// read the db and output the data csv file
assembleCSV_w2013()

