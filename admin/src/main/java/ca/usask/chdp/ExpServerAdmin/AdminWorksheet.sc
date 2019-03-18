import akka.actor
import akka.event.Logging
import akka.pattern.AskTimeoutException
import akka.pattern.ask
import akka.util.Timeout
import ca.usask.chdp.models.Msgs.RegisterListenerForGettingReadyUsers
import scala.concurrent.duration._
import ca.usask.chdp.ExpServerAdmin.AkkaSystem
import scala.concurrent.Await
val log = Logging.getLogger(AkkaSystem.system, "Admin_ExpServerView")





































































try {
  AkkaSystem.connectToLobby
}catch{
  case e:AskTimeoutException => println("It's not there: "+e)
  case e:Exception => println("It's not there2 : "+e)
}
val me = AkkaSystem.adminActor
implicit val timeout = Timeout(5 seconds)
val future = AkkaSystem.lobby ?  RegisterListenerForGettingReadyUsers(me)
try{
  Await.result(future, 5 seconds)
}catch{
  case e:AskTimeoutException => println("It's not there: "+e)
  case e:Exception => println("It's not there2 : "+e)
}


