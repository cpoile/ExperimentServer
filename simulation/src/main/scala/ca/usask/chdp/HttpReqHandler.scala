package ca.usask.chdp

import collection.mutable

object HttpReqHandler {
  def scanForExpServerArgs(path: String, params: mutable.Map[String, Array[String]]): Boolean = {
    // now parse any keys that also have paramters stuck in them.
    println("Entered httpRequestHandler")
    val paramsStr = paramMap2paramString(params)
    println("httpRequestHandler: paramterStr: " + paramsStr)
    val paramsMap = extractMoreKVs(paramsStr.split('&').toList)
    println("httpReqHandler: paramsMap: " + paramsMap)
    false
  }
  def extractMoreKVs(lst: List[String]): Map[String, String] = lst match {
    case Nil => Map.empty[String, String]
    case s :: rest => {
      s.split('=').toList match {
        case k :: v :: Nil => Map(k -> v) ++ extractMoreKVs(rest)
        case _ => extractMoreKVs(rest)  // skip it, it wasn't formatted as key=value pair
      }
    }
  }
  def paramMap2paramString(params: mutable.Map[String, Array[String]]): String = {
    val paramsStr = for ((k, v) <- params) yield ((k.mkString, v.mkString))
    //println("paramMap2 -- paramterStr: " + paramsStr)
    val kvs = paramsStr.filterNot(_._1 == "theme")
    //println("paramMap2 -- paramStr2 w/o theme: " + kvs)
    val paramsStr2 = kvs.map(kv => kv._1 + "=" + kv._2)
    paramsStr2.mkString("&")
  }
  def javaParamMap2scalaParamMap(params: mutable.Map[String, Array[String]]): Map[String, Array[String]] = {
    val paramsStr = for ((k, v) <- params) yield ((k.mkString, v.map(_.mkString)))
    paramsStr.toMap
  }
}
  //    if (path == null || path.length == 1) {
//      println("ExpServerRequestHandler: received null request, passing request through.")
//      false
//    } else {
//      val reqArray = path.split('/').map(_.trim).filterNot(_ == "")
//      println("ExpServerRequestHandler: received request: " + reqArray.deep.mkString(" ") +
//        " length of: " + reqArray.length)
//      reqArray match {
//        case Array("admin") =>
//          //createAdmin(session, response, request.getParameterMap.asScala)
//          true
//        case Array("survey", "2") => {
//          val id = params.getOrElse("id", "").asInstanceOf[Array[String]].mkString
//          println("I received: survey #2 from id: " + id)
//          false
//        }
//        case Array("survey", "4") => false
//        case Array("survey", "6") => false
//        case _ => false
//      }
//    }
  //  def extractkvs(kvs: mutable.Map[String, String]): Map[String, String] = {
  //    // make it into a giant string so we can extract it all ourselves.
  //    for ((k, v) <- kvs) yield {
  //      val lst = v.mkString.split('?').toList
  //      lst match {
  //        case s :: Nil =>
  //      }
  //      if (lst.length > 1) {
  //        // this has more than one kv pair
  //        (Map(k -> lst) ++ extractMoreKVs(lst.drop(1))).flatten
  //      } else
  //        (k -> v)
  //    }
  //  }

