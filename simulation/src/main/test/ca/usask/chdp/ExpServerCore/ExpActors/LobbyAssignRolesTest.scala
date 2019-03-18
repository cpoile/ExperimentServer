package ca.usask.chdp.ExpServerCore.ExpActors

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import scala.util.Random
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby.{WaitingPlayer, MatchedPair}

class LobbyAssignRolesTest extends FlatSpec with ShouldMatchers with GeneratorDrivenPropertyChecks with BeforeAndAfterAll {
  /*
    val customConfig = ConfigFactory.parseString(
      """
        testCustom {
          akka.loglevel = DEBUG
          exp.testing_integrationTesting = true
          exp.testing_doNotStartGames = true
        }
      """).getConfig("testCustom")
    val defaultCfg = ConfigFactory.load()
    val testCfg = customConfig.withFallback(defaultCfg.getConfig("testActorSystem").withFallback(defaultCfg))
    implicit val system = Lobby.actorSystemInitialize(Some(testCfg))
    implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    override def beforeAll() {
      Lobby.lobby ! NewSessionId
    }
    override def afterAll() {
      system.shutdown()
      system.awaitTermination()
    }
  */


  def genRoleList(numRoleAs: Int, numRoleBs: Int) =
    Random.shuffle(List.fill(numRoleAs)("RoleA") ++ List.fill(numRoleBs)("RoleB"))
  def genId = Random.nextInt(Int.MaxValue)
  def genPartList(numRoleAs: Int,
                  numRoleBs: Int, loc: String, manip: Int = 2): List[WaitingPlayer] = {
    val As = for (a <- 1 to numRoleAs) yield {
      val id = genId
      WaitingPlayer(id.toString, id.toString, manip, "RoleA", location = loc)
    }
    val Bs = for (a <- 1 to numRoleBs) yield {
      val id = genId
      WaitingPlayer(id.toString, id.toString, manip, "RoleB", location = loc)
    }
    Random.shuffle(As.toList ++ Bs.toList)
  }

  "removeBalancedRoles" should "balance a variety of different lists" in {
    Lobby.removeBalancedRoles(genRoleList(3, 3), genRoleList(3, 3)) match {
      case (l1, l2) => {
        l1 should be(List())
        l2 should be(List())
      }
    }

    val (lst1, lst2) = Lobby.removeBalancedRoles(genRoleList(3, 4), genRoleList(3, 3))
    assert(lst1 === List("RoleB"))
    assert(lst2 === List())

    Lobby.removeBalancedRoles(genRoleList(3, 4), genRoleList(3, 3)) match {
      case (l1, l2) => {
        l1 should be(List("RoleB"))
        l2 should be(List())
      }
    }
    Lobby.removeBalancedRoles(genRoleList(3, 4), genRoleList(3, 4)) match {
      case (l1, l2) => {
        l1 should be(List("RoleB"))
        l2 should be(List("RoleB"))
      }
    }
    Lobby.removeBalancedRoles(genRoleList(4, 4), genRoleList(3, 4)) match {
      case (l1, l2) => {
        l1 should be(List("RoleB"))
        l2 should be(List())
      }
    }
    Lobby.removeBalancedRoles(genRoleList(40, 42), genRoleList(42, 42)) match {
      case (l1, l2) => {
        l1 should be(List())
        l2 should be(List("RoleB", "RoleB"))
      }
    }
    Lobby.removeBalancedRoles(genRoleList(590, 590), genRoleList(590, 590)) match {
      case (l1, l2) => {
        l1 should be(List())
        l2 should be(List())
      }
    }
  }

  /**
   * Yes, these tests are brittle.
   * TODO: use settings file, fix ExpSettings so we can use settings without starting up Akka.
   */
  "whichRoleForLoc1" should "give RoleA when all is balanced" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"
    // this will tax the matcher, just to make sure it's working.

    forAll(Gen.choose(1, 300)) { case base =>

      val map = Map((loc1 -> List.empty[String]), (loc2 -> List.empty[String]))
      Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map) should be("RoleA")

      val map2 = Map((loc1 -> genRoleList(base, base)), (loc2 -> genRoleList(base, base)))
      Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map2) should be("RoleA")
    }
  }
  it should "give RoleB when there is RoleA already waiting in their location" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    val map1 = Map((loc1 -> genRoleList(21, 20)), (loc2 -> genRoleList(20, 20)))
    Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map1) should be("RoleB")

    val map2 = Map((loc1 -> genRoleList(20, 20)), (loc2 -> genRoleList(21, 20)))
    Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map2) should be("RoleB")
  }
  it should "give RoleA when there is RoleB already waiting in their location" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    val map1 = Map((loc1 -> genRoleList(20, 21)), (loc2 -> genRoleList(20, 20)))
    Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map1) should be("RoleA")

    val map2 = Map((loc1 -> genRoleList(20, 20)), (loc2 -> genRoleList(20, 21)))
    Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map2) should be("RoleA")
  }
  it should "give RoleA when there is RoleB waiting in other location" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    val map1 = Map((loc1 -> genRoleList(20, 21)), (loc2 -> genRoleList(20, 20)))
    Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map1) should be("RoleA")

    val map2 = Map((loc1 -> genRoleList(20, 20)), (loc2 -> genRoleList(20, 21)))
    Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map2) should be("RoleA")
  }
  it should "give RoleB when there is RoleA waiting in other location" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    val map1 = Map((loc1 -> genRoleList(21, 20)), (loc2 -> genRoleList(20, 20)))
    Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map1) should be("RoleB")

    val map2 = Map((loc1 -> genRoleList(20, 20)), (loc2 -> genRoleList(21, 20)))
    Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map2) should be("RoleB")
  }
  it should "give two RoleA when there is 1 RoleB in other, 1 RoleB in same loc" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    val map1 = Map((loc1 -> genRoleList(20, 21)), (loc2 -> genRoleList(20, 21)))
    Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map1) should be("RoleA")

    val map2 = Map((loc1 -> genRoleList(21, 21)), (loc2 -> genRoleList(20, 21)))
    Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map2) should be("RoleA")
  }
  it should "give two RoleB when there is 1 RoleA in other, 1 RoleA in same loc" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"


    val map1 = Map((loc1 -> genRoleList(21, 20)), (loc2 -> genRoleList(21, 20)))
    Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map1) should be("RoleB")

    val map2 = Map((loc1 -> genRoleList(21, 20)), (loc2 -> genRoleList(21, 21)))
    Lobby.roleForOneManipMultipleLocations(loc1, Seq(loc1, loc2), map2) should be("RoleB")
  }
  it should "give Role B, A, B, A in order when behind 4 in other" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    forAll(Gen.choose(1, 300)) { case base =>
      val map = Map((loc1 -> genRoleList(base + 2, base + 2)), (loc2 -> genRoleList(base, base)))
      Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map) should be("RoleB")
    }
    forAll(Gen.choose(1, 300)) { case base =>
      val map = Map((loc1 -> genRoleList(base + 2, base + 2)), (loc2 -> genRoleList(base, base + 1)))
      Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map) should be("RoleB")
    }
    forAll(Gen.choose(1, 300)) { case base =>
      val map = Map((loc1 -> genRoleList(base + 2, base + 2)), (loc2 -> genRoleList(base, base + 2)))
      Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map) should be("RoleA")
    }
    forAll(Gen.choose(1, 300)) { case base =>
      val map = Map((loc1 -> genRoleList(base + 2, base + 2)), (loc2 -> genRoleList(base + 1, base + 2)))
      Lobby.roleForOneManipMultipleLocations(loc2, Seq(loc1, loc2), map) should be("RoleA")
    }
  }
  def isValidMatchWhenLocMatters(pair: MatchedPair): Boolean = {
    assert(pair.plr1.role !== pair.plr2.role)
    assert(pair.plr1.location !== pair.plr2.location)
    assert(pair.plr1.manipulation === pair.plr2.manipulation)
    true
  }

  val smallPosNums = Gen.choose(1, 50) suchThat (n => n > 0 && n < 50)
  "matchBetweenLocationsStrategy" should "match all if balanced" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    forAll(smallPosNums) { case base => {
      val allParts = Random.shuffle(genPartList(base, base, loc1) ++ genPartList(base, base, loc2))

      val pairsAndUnmatched = Lobby.matchAllPlayers(allParts, Lobby.matchBetweenLocationsStrategy)
      pairsAndUnmatched.unMatched.length should be (0)
      pairsAndUnmatched.matchedPairs foreach (isValidMatchWhenLocMatters(_) should be (true))
    }
    }
  }
  it should "have one RoleA left over if other has one less RoleB" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    forAll(smallPosNums) { case base => {
      val allParts = Random.shuffle(genPartList(base+1, base, loc1) ++ genPartList(base, base, loc2))

      val pairsAndUnmatched = Lobby.matchAllPlayers(allParts, Lobby.matchBetweenLocationsStrategy)
      pairsAndUnmatched.matchedPairs foreach (isValidMatchWhenLocMatters(_) should be (true))
      pairsAndUnmatched.unMatched.length should be (1)
      pairsAndUnmatched.unMatched.head.role should be ("RoleA")
      pairsAndUnmatched.unMatched.head.location should be (loc1)
    }
    }
    forAll(smallPosNums) { case base => {
      val allParts = Random.shuffle(genPartList(base, base, loc1) ++ genPartList(base+1, base, loc2))

      val pairsAndUnmatched = Lobby.matchAllPlayers(allParts, Lobby.matchBetweenLocationsStrategy)
      pairsAndUnmatched.matchedPairs foreach (isValidMatchWhenLocMatters(_) should be (true))
      pairsAndUnmatched.unMatched.length should be (1)
      pairsAndUnmatched.unMatched.head.role should be ("RoleA")
      pairsAndUnmatched.unMatched.head.location should be (loc2)
    }
    }
  }
  it should "have one RoleB left over if other has one less RoleA" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    forAll(smallPosNums) { case base => {
      val allParts = Random.shuffle(genPartList(base-1, base, loc1) ++ genPartList(base, base, loc2))

      val pairsAndUnmatched = Lobby.matchAllPlayers(allParts, Lobby.matchBetweenLocationsStrategy)
      pairsAndUnmatched.matchedPairs foreach (isValidMatchWhenLocMatters(_) should be (true))
      pairsAndUnmatched.unMatched.length should be (1)
      pairsAndUnmatched.unMatched.head.role should be ("RoleB")
      pairsAndUnmatched.unMatched.head.location should be (loc2)
    }
    }
    forAll(smallPosNums) { case base => {
      val allParts = Random.shuffle(genPartList(base, base, loc1) ++ genPartList(base-1, base, loc2))

      val pairsAndUnmatched = Lobby.matchAllPlayers(allParts, Lobby.matchBetweenLocationsStrategy)
      pairsAndUnmatched.matchedPairs foreach (isValidMatchWhenLocMatters(_) should be (true))
      pairsAndUnmatched.unMatched.length should be (1)
      pairsAndUnmatched.unMatched.head.role should be ("RoleB")
      pairsAndUnmatched.unMatched.head.location should be (loc1)
    }
    }
  }

  it should "have RoleB left over if other has one less RoleA" in {
    val loc1 = "Lab44"
    val loc2 = "Lab142"

    forAll(smallPosNums) { case base => {
      val allParts = Random.shuffle(genPartList(base-1, base, loc1) ++ genPartList(base, base, loc2))

      val pairsAndUnmatched = Lobby.matchAllPlayers(allParts, Lobby.matchBetweenLocationsStrategy)
      pairsAndUnmatched.matchedPairs foreach (isValidMatchWhenLocMatters(_) should be (true))
      pairsAndUnmatched.unMatched.length should be (1)
      pairsAndUnmatched.unMatched.head.role should be ("RoleB")
      pairsAndUnmatched.unMatched.head.location should be (loc2)
    }
    }
    forAll(smallPosNums) { case base => {
      val allParts = Random.shuffle(genPartList(base, base, loc1) ++ genPartList(base-1, base, loc2))

      val pairsAndUnmatched = Lobby.matchAllPlayers(allParts, Lobby.matchBetweenLocationsStrategy)
      pairsAndUnmatched.matchedPairs foreach (isValidMatchWhenLocMatters(_) should be (true))
      pairsAndUnmatched.unMatched.length should be (1)
      pairsAndUnmatched.unMatched.head.role should be ("RoleB")
      pairsAndUnmatched.unMatched.head.location should be (loc1)
    }
    }
  }

}
