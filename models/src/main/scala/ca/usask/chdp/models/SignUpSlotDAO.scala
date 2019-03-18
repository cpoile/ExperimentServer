package ca.usask.chdp.models

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.{MongoConnection, MongoCollection}
import com.mongodb.casbah.commons.conversions.scala._
import com.mongodb.casbah.Imports.WriteConcern
import com.novus.salat._
import com.novus.salat.global._
import ca.usask.chdp.ExpSettings
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._


/**
 * This is a single access point for the AdminDB.
 * All messages return an answer.
 */
object SignUpSlotDAO extends DAOTrait[SignUpSlotBean] {
  RegisterJodaTimeConversionHelpers()

  val log = LoggerFactory.getLogger("SignUpSlotDAO")

  lazy val dbName = if (ExpSettings.get.testingMode)
    ExpSettings.get.testing_signUp_dbName
  else
    ExpSettings.get.production_signUp_dbName
  lazy val conn = MongoConnection()
  lazy val coll = conn(dbName)("signUpSlot_coll")

  /**
   * Returns List[SignUpSlotBean]
   */
  @Override
  def findAll: List[SignUpSlotBean] = {
    //    val query = MongoDBObject("expID" -> experimentID)
    //    coll.find(query).toList.map(grater[SignUpSlotBean].asObject(_))
    coll.find().toList.map(grater[SignUpSlotBean].asObject(_))
  }
  def find(id: String): SignUpSlotBean = {
    val q = MongoDBObject("_id" -> id)
    val res = coll.find(q).toList.map(grater[SignUpSlotBean].asObject(_))
    if (res.length > 0)
      res(0)
    else
      SignUpSlotBean("")
  }

  @Override
  def remove(slot: SignUpSlotBean): Boolean = {
    val q = MongoDBObject("_id" -> slot._id)
    val res = coll.find(q).toList
    if (res.length > 0) {
      coll -= res(0)
    } else {
      false
    }
    coll.getLastError().ok()
  }

  /**
   * Returns Boolean if insert was successful.
   * CAUTION -- WILL OVERWRITE.
   */
  @Override
  def insertUpdate(slot: SignUpSlotBean): SignUpSlotBean = {
    //    val q = MongoDBObject("experimentId" -> slot.experimentId, "location" -> slot.location,
    //      "startTime" -> slot.startTime, "endTime" -> slot.endTime)
    //    val res = coll.find(q).toList
    coll += grater[SignUpSlotBean].asDBObject(slot)
    val q = MongoDBObject("_id" -> slot._id)
    val ret = coll.findOne(q).map(s => grater[SignUpSlotBean].asObject(s)) | SignUpSlotBean("")
    if (ret != slot)
      log.error("The object read from DB -- {} -- is not same as the one just enetered: -- {} --.",
        ret, slot)
    ret
  }

  /**
   * Atomically update the slot to see if there was still a spot open.
   * Returns Boolean if insert was successfull. <br/>
   * Ways to do this: <br/>
   *
   * From: http://www.mongodb.org/display/DOCS/Schema+Design#SchemaDesign-AtomicOperations
   * db.posts.update( { _id : ObjectId("4e77bb3b8a3e000000004f7a"),
                     voters : { $nin : "calvin" } },
                   { votes : { $inc : 1 }, voters : { $push : "calvin" } );
   *
   * Or (same type:) http://www.mongodb.org/display/DOCS/Atomic+Operations
   * > t=db.inventory
   * > s = t.findOne({sku:'abc'})
   * {"_id" : "49df4d3c9664d32c73ea865a" , "sku" : "abc" , "qty" : 30}
   * > qty_old = s.qty;
   * > --s.qty;
   * > t.update({_id:s._id, qty:qty_old}, s); db.$cmd.findOne({getlasterror:1});
   * {"err" :  , "updatedExisting" : true , "n" : 1 , "ok" : 1} // it worked
   *
   * Or, using $inc:
   * > t.update({sku:"abc",qty:{$gt:0}}, { $inc : { qty : -1 } } ) ; db.$cmd.findOne({getlasterror:1})
   * {"err" : , "updatedExisting" : true , "n" : 1 , "ok" : 1} // it worked
   * > t.update({sku:"abcz",qty:{$gt:0}}, { $inc : { qty : -1 } } ) ; db.$cmd.findOne({getlasterror:1})
   * {"err" : , "updatedExisting" : false , "n" : 0 , "ok" : 1} // did not work
   *
   */

  def isValidUpdate(slotId: String, partId: String): Boolean = {
    val slotQuery = MongoDBObject("_id" -> slotId)
    val newPartQ = grater[SimplePartBean].asDBObject(SimplePartBean(partId, partId))
    // only update if there is space left and the participant isn't already registered.
    val fullQuery = ("spacesFree" $gt 0) ++ ("registeredParticipants" $nin Array(newPartQ)) ++ slotQuery
    log.debug("full query: {}", fullQuery)
    (coll.find(fullQuery).toList.length > 0)
  }
  /**
   * Schedule this participant and transform their bean to indicate their new session status.
   * Does not save the participant bean that has been transformed.
   * SIDE EFFECTS: saves the new signUpSlot (with part in its list).
   *
   */
  def scheduleAParticipant(slotId: String, partBean: ParticipantBean): ParticipantBean = {
    val slotQ = MongoDBObject("_id" -> slotId)
    val newPartQ = grater[SimplePartBean].asDBObject(SimplePartBean(partBean._id, partBean.email))
    val update = $inc("spacesFree" -> -1) ++ $addToSet("registeredParticipants" -> newPartQ)
    // only update if there is space left and the participant isn't already registered.
    val fullQuery = ("spacesFree" $gt 0) ++ ("registeredParticipants" $nin Array(newPartQ)) ++ slotQ
    coll.update(fullQuery, update)
    log.debug("scheduling participant. full query: {} --- update: {}", fullQuery, update)
    // check if insert worked, if it did, add that participant's booking to their record.
    val checkQ = ("registeredParticipants" $in Array(newPartQ)) ++ slotQ

    //    val slot = coll.find(checkQ).toList
    //    if (slot.length > 0) {
    //      val slotBean = grater[SignUpSlotBean].asObject(slot(0))
    //      partBean.copy(signUpSlotId = slotBean._id, signUpSlotInfo = slotBean.toString)
    //    } else partBean
    //
    // the following replaces the 5 lines above.
    // find slot1 if it contains participant's info (checkQ), if it exists take the signUpSlot
    // and give it to the partBean which will add that slot to the part's data.
    val ret = coll.findOne(checkQ) map (s => grater[SignUpSlotBean].asObject(s) |> partBean.assignSlot)
    // If this all works return the new partBean; if not return the original (unchanged) partBean.
    ret | partBean
  }

  /**
   * If the user is enrolled in a session, remove them. If not, do nothing. Return the changed or unchanged bean.
   */
  def removePartFromTheirCurSlotInDB(part: ParticipantBean): ParticipantBean = {
    ParticipantDAO.findByEmail(part._id) foreach { p =>
      if (p.signUpSlotId != "")
        removePartFromThisSlot(p.signUpSlotId, SimplePartBean(part._id, part.email))
    }
    // wither they were successfully removed from their slot, or they weren't in it to begin with. No big deal.
    // Now remove their internal record of that slot. Even though this is not needed if they weren't in one.
    part.copy(signUpSlotId = "", signUpSlotInfo = "")
  }

  /**
   * Remove this participant from this signUpSlot. Do only that.
   */
  def removePartFromThisSlot(slotId: String, simplePart: SimplePartBean): Boolean = {
    val registeredPartQ = grater[SimplePartBean].asDBObject(simplePart)
    val slotQ = MongoDBObject("_id" -> slotId)
    val checkQ = ("registeredParticipants" $in Array(registeredPartQ)) ++ slotQ
    if (coll.findOne(checkQ).isEmpty) {
      log.error("Participant -- {} -- was asked to be removed from slot -- {} -- but they were not found in it.",
        simplePart._id, slotId)
      false
    } else {
      val update = $inc("spacesFree" -> 1) ++ $pull(MongoDBObject("registeredParticipants" -> registeredPartQ))
      coll.update(checkQ, update)
      if (coll.findOne(checkQ).isDefined) {
        log.error("Participant -- {} -- was asked to be removed from slot -- {} -- but they were not.",
          simplePart._id, slotId)
        false
      } else true
    }
  }
  /**
   * Returns (valid, newBean if the bean's id field had to be changed.)
   */
  def validate(slot: SignUpSlotBean): (Boolean, SignUpSlotBean) = {
    if (slot.experimentId != "" && slot.location != "" && slot.length != "")
      (true, slot.copy(_id = slot.slotId))
    else
      (false, slot)
  }

  def closeConnection() {
    conn.close()
  }
}

