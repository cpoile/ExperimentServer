package ca.usask.chdp.ExpServerCore.IntegrationTests

import org.scalatest.FlatSpec
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.typesafe.config.ConfigFactory
import ca.usask.chdp.ExpServerCore.Models.{Model, GameLogicDataCC, GameLogicData}
import com.mongodb.util.JSON
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import java.io.{FileInputStream, ObjectInputStream, ObjectOutputStream, FileOutputStream}

class tutorialGameLogic extends FlatSpec {

  ignore should "retrieve gameLogic" in {
    val system = Lobby.actorSystemInitialize(Some(ConfigFactory.load()))

    // low -> high manip
    val gldbo = GameLogicData.getDBOLogicData
    val out = new ObjectOutputStream(new FileOutputStream("tutorialGameLogic.obj"))
    out.writeObject(gldbo.toString)
    out.close()
  }
  ignore should "load gameLogic data from disk" in {
    val glin = new ObjectInputStream(new FileInputStream("tutorialGameLogic.obj"))
    val savedGLstring = glin.readObject().asInstanceOf[String]
    val savedGLdbo = JSON.parse(savedGLstring).asInstanceOf[DBObject]

    //val gldbo = GameLogicData.getDBOLogicData
    //assert(savedGLdbo === gldbo)

    val tutGLcc = grater[GameLogicDataCC].asObject(savedGLdbo)
    val glcc = GameLogicData.getSpecificLogicData("tutorial", 2)
    assert(tutGLcc === glcc)
  }
  ignore should "load gameLogic data from Models jar" in {
    val glcc = GameLogicData.getSpecificLogicData("tutorial", 2)
    val cl = glcc.getClass.getClassLoader
    println(cl)
    val strm = cl.getResourceAsStream("tutorialGameLogic.javaobj")
    println(strm)

//    val gldisk = GameLogicData.getTutorialData(Model.getClass.getClassLoader)
//
//    assert(gldisk === glcc)
  }


}
