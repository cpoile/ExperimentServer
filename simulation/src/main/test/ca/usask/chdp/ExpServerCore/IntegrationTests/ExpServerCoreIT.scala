package ca.usask.chdp.ExpServerCore.IntegrationTests

import org.scalatest.FlatSpec
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.ShouldMatchers

class ExpServerCoreIT extends FlatSpec with ShouldMatchers {

  "ConfigSystem" should "give precedence to my custom values over defualts" in {
    val customConfig = ConfigFactory.parseString(
      """
      testCustom {
        exp.A_max = 999
      }
      """)
    val defaultCfg = ConfigFactory.load()
    val finalCfg = customConfig.getConfig("testCustom").withFallback(defaultCfg)
    assert (finalCfg.getInt("exp.A_max") === 999)
    assert (defaultCfg.getInt("exp.A_max") === 26)
  }



  //val system = Lobby.initialize(customConfig.withFallback(config))


}
