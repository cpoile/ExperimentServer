package ca.usask.chdp.ExpServerCore.View

import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor.ViewMsg
import scala.collection.immutable.Stack

trait ViewManager {
  // to make type ReceiveMsg known in subclasses without import
  type ReceiveMsg =  PartialFunction[ViewMsg, Unit]

  // this is the partial function that must be defined by any ViewManager.
  // similar to Actor's receive.
  def receiveMsg: ReceiveMsg

  // all of the below is adapted from Akka.Actor
  def become(behavior: ReceiveMsg) {
    pushBehavior(behavior)
  }
  def apply(msg: ViewMsg) {
    val head = behaviorStack.head
    head.apply(msg)
  }
  /**
   * Yes, this could be changed to a ! like an actor, but it's not an actor -- don't want to
   * confuse things too much.
   */
  def send(msg: ViewMsg) {
    apply(msg)
  }

  private def pushBehavior(behavior: ReceiveMsg) {
    behaviorStack = behaviorStack.push(behavior)
  }
  private def clearBehaviorStack() {
    behaviorStack = Stack.empty[ReceiveMsg].push(behaviorStack.last)
  }

  private var behaviorStack: Stack[ReceiveMsg] = Stack.empty[ReceiveMsg].push(receiveMsg)
}

