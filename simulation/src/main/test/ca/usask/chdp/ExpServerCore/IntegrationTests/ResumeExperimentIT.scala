package ca.usask.chdp.ExpServerCore.IntegrationTests

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby

class ResumeExperimentIT (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with FlatSpec with PropertyChecks with ShouldMatchers
with BeforeAndAfterEach with BeforeAndAfterAll {

//  implicit var s = new GameState()
//  implicit val actorSystem = _system

  def this() = this {
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
    Lobby.actorSystemInitialize(Some(testCfg))
  }

  override def afterAll() {
//    system.shutdown()
//    system.awaitTermination()
  }

  "LoginSystem" should "have the following:" in {
    (pending)
  }
  it should "Save and retreive user and their password, which are encrypted" in {

  }

}
