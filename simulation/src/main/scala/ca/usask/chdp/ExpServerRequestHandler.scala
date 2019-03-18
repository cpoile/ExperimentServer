package ca.usask.chdp

import com.vaadin.server._
import collection.JavaConverters._
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinResponse
import com.vaadin.server.RequestHandler

/**
 * No longer used.
 */
class ExpServerRequestHandler extends RequestHandler {
  def handleRequest(session: VaadinSession,
                    request: VaadinRequest, response: VaadinResponse) = {
    //HttpReqHandler.scanForExpServerArgs(request.getRequestPathInfo, request.getParameterMap.asScala)

    //println("ExpServerRequestHandler called")

    val paramStr = HttpReqHandler.paramMap2paramString(request.getParameterMap.asScala)
    println("ExpServerRequestHandler recieved: " + paramStr)
    val uis = session.getUIs.asScala.toList
    println("UIs: " + uis)
    val oldFrags = uis(0).getPage.getUriFragment
    uis(0).getPage.setUriFragment(oldFrags + paramStr)
    false
  }
}

//
//  def createAdmin(session: VaadinServiceSession, response: VaadinResponse, params: mutable.Map[String, Array[String]]) {
//    println("ExpServerRequestHandler: creating admin with params: " + params.map(_._2.mkString(" ")).mkString)
//    if (params.contains("email") && params.contains("pwd")) {
//      val (email, pwd) = (params("email")(0), params("pwd")(0))
//      println("email: " + email + " pwd: " + pwd)
//      val res = Model.overrideAndEncryptUser(email, pwd)
//      response.getWriter.append("Command received. Result = " + res)
//    } else {
//      response.getWriter.append("No valid command receive. Parameters: \n" +
//        (for ((k, v) <- params) yield ("Key: " + k + " Values: " + v.map(_.toString + " ") +
//          "\n")).mkString)
//    }
//  }
//}


//            Lobby.checkSessionTrue
//            val layout = new CssLayout()
//            layout.addComponent(new Label(Res.getSurvey, ContentMode.HTML))
//            session.getUIs.iterator().next().setContent(layout)
//            //response.getWriter.append(Res.getSurvey)


//      case "survey2" :: Nil => {
//        println("I received: admin. they get a new window.")
//        Lobby.checkSessionTrue
//        val layout = new CssLayout()
//layout.addComponent(new Label(Res.getSurvey, ContentMode.HTML))
//session.getUIs.iterator().next().setContent(layout)
//        println("base directory is: " + session.getService.getBaseDirectory)
//        val resource = session.getService.getClassLoader.getResource("/ca/usask/chdp/surveys/survey.html").getContent.asInstanceOf[BufferedInputStream]
//        val br = new BisReader(resource)
//        val brChar = br.map(_.toChar)
//        val id = "ThisIsAnID1234"
//        val inXml = XML.loadString(brChar.mkString)
//        response.getWriter.append(inXml.mkString)
//        true
//      }
