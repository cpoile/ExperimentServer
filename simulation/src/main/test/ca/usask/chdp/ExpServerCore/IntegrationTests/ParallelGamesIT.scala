package ca.usask.chdp.ExpServerCore.IntegrationTests

import org.scalatest.{ParallelTestExecution, BeforeAndAfterAll, FlatSpec}
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers
import ca.usask.chdp.ExpServerCore.ExpActors._
import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor._
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby._
import ca.usask.chdp.ExpServerCore.Models._
import com.typesafe.config.ConfigFactory
import collection.mutable.ListBuffer
import java.util.concurrent.Executors
import scala.Some
import util.Random
import ca.usask.chdp.ExpSettings.CurGameManipRnd
import ca.usask.chdp.Enums._
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import collection.parallel.mutable.ParHashMap
import collection.mutable

class ParallelGamesIT extends FlatSpec with PropertyChecks with ShouldMatchers
with BeforeAndAfterAll with ParallelTestExecution {
  val chatPerGame = mutable.HashMap.empty[String, Seq[TestChatMsg]]
  // (round, sender, msg)
  val helpPerGame = mutable.HashMap.empty[String, Seq[Int]]

  /**
   * need to make a new startup for actors, since we can't use testprobes.
   */
  val customConfig = ConfigFactory.parseString(
    """
      testCustom {
        akka.loglevel = WARNING
        #exp.PartUpgrGenerator = WEIGHTED_HIGHER
        #exp.A_max = 999
      }
    """).getConfig("testCustom")
  val defaultCfg = ConfigFactory.load()
  val testCfg = customConfig.withFallback(defaultCfg.getConfig("testActorSystem").withFallback(defaultCfg))
  implicit val system = Lobby.actorSystemInitialize(Some(testCfg))
  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  override def afterAll() {
    system.shutdown()
    system.awaitTermination()
  }

  def runXGamesInYThreads(x: Int, y: Int, startWithinMillis: Int, game: => Boolean) {
    val futures = ListBuffer.empty[Future[Boolean]]


  }
  def runXGamesInARow(x: Int, startWithinMillis: Int, game: => Boolean)(implicit ec: ExecutionContext): Boolean = {
    for (i <- 1 to x) {
      val res = Future {
        Thread.sleep(Random.nextInt(startWithinMillis))
        game
      }
      Await.result(res, 5 minutes)
    }
    true
  }
  def oneFullGame: Boolean = {
    val arrayOfAHelp = validArray
    val s = setup2Players
    val theGame = getGameRecord(s)
    helpPerGame += (theGame.gr_id -> arrayOfAHelp)

    for (i <- 0 until Lobby.settings.numRounds) {
      val rnd = i
      s.curRnd = i

      // chat
      val aChat = genArrayChat(rnd, s.id1, s.id2, "RoleA", "RoleB")
      val bChat = genArrayChat(rnd, s.id2, s.id1, "RoleB", "RoleA")
      val seqMsgs = (aChat ++ bChat)
      val msgSoFar = chatPerGame.getOrElse(theGame.gr_id, Seq.empty[TestChatMsg])
      val newMsgSoFar = seqMsgs ++ msgSoFar
      chatPerGame += (theGame.gr_id -> newMsgSoFar)

      val gameManipRnd = CurGameManipRnd(settings.getManipIndexForTesting, rnd)
      val aWork = arrayOfAHelp(rnd)
      runOneRoundWithAHelping(aWork, s, Some(seqMsgs))(gameManipRnd)
      val gameRec = getGameRecord(s)
      assert(gameRec.helpfulBehsPerRound(Round(rnd)) === aWork)
      assert(seqMsgs.length === gameRec.chatMsgsPerRoundFromA(Round(rnd)) + gameRec.chatMsgsPerRoundFromB(Round(rnd)))
      if (rnd != Lobby.settings.numRounds - 1) {
        // not the last round, so the View gets setStates for the next round. Clear those:
        assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === A_WorkingOnProj1)
        assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[B_WatchingA])
      }
    }
    assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[ShowFinishedGame])
    assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[ShowFinishedGame])
    val gameRec = getGameRecord(s)
    val totalHelp = gameRec.helpfulBehsPerRound.map(_._2).sum
//    val totalMsgs = gameRec.chatMsgsPerRoundFromA.map(_._2).sum +
//      gameRec.chatMsgsPerRoundFromB.map(_._2).sum
    assert(totalHelp === arrayOfAHelp.sum)
    assert(totalHelp === helpPerGame(gameRec.gr_id).sum, totalHelp + " did not = " +
      helpPerGame(gameRec.gr_id).sum + " for game with key: " + gameRec.gr_id)
    s.close()
    true
  }
  ignore should "handle random AHelp and record correct helpful behaviors" in {
    runXGamesInYThreads(10, 10, 30000, game)

    def game: Boolean = {
      val arrayOfAHelp = validArray
      val s = setup2Players
      for (i <- 0 until Lobby.settings.numRounds) {
        val rnd = i
        val gameManipRnd = CurGameManipRnd(Lobby.settings.getManipIndexForTesting, rnd)
        val aWork = arrayOfAHelp(rnd)
        runOneRoundWithAHelping(aWork, s)(gameManipRnd)
        val gameRec = getGameRecord(s)
        assert(gameRec.helpfulBehsPerRound(Round(rnd)) === aWork)
        if (rnd != Lobby.settings.numRounds - 1) {
          // not the last round, so the View gets setStates for the next round. Clear those:
          assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd === A_WorkingOnProj1)
          assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[B_WatchingA])
        }
      }
      assert(s.probe("RoleA").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[ShowFinishedGame])
      assert(s.probe("RoleB").expectMsgClass(classOf[SetState]).state.bean.uiCmd.isInstanceOf[ShowFinishedGame])
      val totalHelp = getGameRecord(s).helpfulBehsPerRound.map(_._2).sum
      assert(totalHelp === arrayOfAHelp.sum)
      s.close()
      true
    }
  }
  "Database" should "store and retrieve correct HelpfulBehs per game" in {
    val dbSettingsPreTest = settings.testWithoutDB
    val usingMemChachePreTest = settings.testing_useMemCache
    val tmode = Lobby.settings.testingMode
    val syncDB = settings.testing_useSynchronousDB
    settings.testWithoutDB = false
    Lobby.settings.testingMode = true
    settings.testing_useMemCache = false
    settings.testing_useSynchronousDB = true

    val numThreads = 80
    val numGamesEachThread = 10
    val sleepDelay = 2000

    def gameThenAnother(numGamesRemaining: Int, sleepDelay: Int): Boolean = {
      if( numGamesRemaining > 1) {
        Thread.sleep(Random.nextInt(sleepDelay))
        oneFullGame && gameThenAnother(numGamesRemaining-1, sleepDelay)
      } else {
        true
      }
    }
    // each of the Futures is a "thread"
    val listOfFutures = Future.traverse((1 to numThreads).toList)(x => Future{
      gameThenAnother(numGamesEachThread, sleepDelay)
    })
    val resultBool = listOfFutures.map(_.reduce(_ && _))
    val finalRes = Await.result(resultBool, 2 minutes)

//    val res = Await.result(listOfFutureLists, 5 minutes)
//    assert(res.foldLeft(true)((r,l) => r == l.reduce(_ == _)))

//    val listOfFutureOfFutureBooleans = for (i <- 1 to numThreads) yield Future {
//      makeFutureListOfGameResults
//    }
//    val futureList = Future.sequence(listOfFutureOfFutureBooleans)
//    listOfFutureOfFutureBooleans.foreach(Await.result(_, 5 minutes))
//    val futureFutureList = futureList.map(Future.sequence(_))
//    val finalBool = futureFutureList.map(_.map(_.reduce(_ == _)))
//    finalBool.foreach(_.foreach(assert(_)))

    // Now check if it was all saved correctly.
    helpPerGame.keys foreach {
      key =>
        val helpfulBehs = GameRecordDAO.retrieveHelpfulBehs(key)
        assert(helpfulBehs === helpPerGame(key), helpfulBehs + " did not = " +
          helpPerGame(key) + " for game with key: " + key)
    }
    chatPerGame.keys foreach {
      key =>
        val msgsFromGame = chatPerGame(key)
        val (oneID, twoID) = (msgsFromGame(0).sentByID, msgsFromGame(0).sentTo)
        val oneMsgsFromDB = MsgDAO.retrieve(key, oneID)
        val twoMsgsFromDB = MsgDAO.retrieve(key, twoID)
        val msgsFromDB = oneMsgsFromDB ++ twoMsgsFromDB

        val totalMsgsFromTest = chatPerGame(key).map(x => (x.sentOnRound, x.sentByID, x.msg))
        assert(msgsFromDB.length === totalMsgsFromTest.length)
        assert(totalMsgsFromTest.map(_._3).sorted === msgsFromDB.map(_._3).sorted)
    }



    settings.testWithoutDB = dbSettingsPreTest
    settings.testing_useMemCache = usingMemChachePreTest
    settings.testingMode = tmode
    settings.testing_useSynchronousDB = syncDB

    assert(finalRes === true)
  }


}
