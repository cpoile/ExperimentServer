package ca.usask.chdp.ExpServerCore.IntegrationTests

import akka.actor._
import akka.actor.Actor._
import java.util.concurrent.{TimeUnit, BlockingDeque, LinkedBlockingDeque}
import scala.concurrent.duration._
import annotation.tailrec
import akka.util.BoxedType
import akka.AkkaException

class TestViewActorProbe(id: String) {
  import TestViewActorProbe._

  /**
   * Because we are class, we are not using an object like TestKit, we need to simulate
   * "getting an actorRef" for the testProbe. In this case, it's just us.
   */
  var ref: ActorRef = null

  val queue = new LinkedBlockingDeque[Message]()
  private var lastMessage: Message = NullMessage

  private var end: Duration = Duration.Undefined

  /**
   * if last assertion was expectNoMsg, disable timing failure upon within()
   * block end.
   */
  private var lastWasNoMsg = false


  /**
   * Obtain time remaining for execution of the innermost enclosing `within`
   * block or missing that it returns the properly dilated default for this
   * case from settings (key "akka.test.single-expect-default").
   *
   * Chris: removed dilated
   */
  def remaining: Duration = if (end == Duration.Undefined) (20 seconds) else end - now

  /**
   * Query queue status.
   */
  def msgAvailable = !queue.isEmpty


  /**
   * Execute code block while bounding its execution time between `min` and
   * `max`. `within` blocks may be nested. All methods in this trait which
   * take maximum wait times are available in a version which implicitly uses
   * the remaining time governed by the innermost enclosing `within` block.
   *
   * Note that the timeout is scaled using Duration.dilated, which uses the
   * configuration entry "akka.test.timefactor", while the min Duration is not.
   *
   * <pre>
   * val ret = within(50 millis) {
   *         test ! "ping"
   *         expectMsgClass(classOf[String])
   *       }
   * </pre>
   */
  def within[T](min: FiniteDuration, max: FiniteDuration)(f: ⇒ T): T = {
    // Chris: removed dilated
    val _max = max
    val start = now
    val rem = if (end == Duration.Undefined) Duration.Inf else end - start
    assert(rem >= min, "required min time " + min + " not possible, only " + format(min.unit, rem) + " left")

    lastWasNoMsg = false

    val max_diff = _max min rem
    val prev_end = end
    end = start + max_diff

    val ret = try f finally end = prev_end

    val diff = now - start
    assert(min <= diff, "block took " + format(min.unit, diff) + ", should at least have been " + min)
    if (!lastWasNoMsg) {
      assert(diff <= max_diff, "block took " + format(_max.unit, diff) + ", exceeding " + format(_max.unit, max_diff))
    }

    ret
  }

  /**
   * Same as calling `within(0 seconds, max)(f)`.
   */
  def within[T](max: FiniteDuration)(f: ⇒ T): T = within(0 seconds, max)(f)

  /**
   * Same as `expectMsgClass(remaining, c)`, but correctly treating the timeFactor.
   */
  def expectMsgClass[C](c: Class[C]): C = expectMsgClass_internal(remaining, c)

  private def expectMsgClass_internal[C](max: Duration, c: Class[C]): C = {
    val o = receiveOne(max)
    assert(o ne null, "timeout (" + max + ") during expectMsgClass waiting for " + c)
    assert(BoxedType(c) isInstance o, "expected " + c + ", found " + o.getClass)
    o.asInstanceOf[C]
  }

  /**
   * Receive one message from the internal queue of the TestActor. If the given
   * duration is zero, the queue is polled (non-blocking).
   *
   * This method does NOT automatically scale its Duration parameter!
   */
  def receiveOne(max: Duration): AnyRef = {
    val message =
      if (max == 0.seconds) {
        queue.pollFirst
      } else if (max.isFinite()) {
        queue.pollFirst(max.length, max.unit)
      } else {
        queue.takeFirst
      }
    lastWasNoMsg = false
    message match {
      case null ⇒
        lastMessage = NullMessage
        null
      case RealMessage(msg, _) ⇒
        lastMessage = message
        msg
    }
  }

  private def format(u: TimeUnit, d: Duration) = "%.3f %s".format(d.toUnit(u), u.toString.toLowerCase)
}

 class TVActor(queue: BlockingDeque[TestViewActorProbe.Message]) extends Actor {
   import TestViewActorProbe._

   var ignore: Ignore = None

   def receive = {
    case SetIgnore(ign) ⇒ ignore = ign
    case x@Watch(ref) ⇒ context.watch(ref); queue.offerLast(RealMessage(x, self))
    case x@UnWatch(ref) ⇒ context.unwatch(ref); queue.offerLast(RealMessage(x, self))
    case x: AnyRef ⇒
      val observe = ignore map (ignoreFunc ⇒ if (ignoreFunc isDefinedAt x) !ignoreFunc(x) else true) getOrElse true
      if (observe) queue.offerLast(RealMessage(x, sender))
  }

  override def postStop() = {
    import scala.collection.JavaConverters._
    queue.asScala foreach { m ⇒ context.system.deadLetters ! DeadLetter(m.msg, m.sender, self) }
  }
}

object TestViewActorProbe {
  import akka.actor.Props

  type Ignore = Option[PartialFunction[AnyRef, Boolean]]

  case class SetIgnore(i: Ignore)
  case class Watch(ref: ActorRef)
  case class UnWatch(ref: ActorRef)

  trait Message {
    def msg: AnyRef
    def sender: ActorRef
  }
  case class RealMessage(msg: AnyRef, sender: ActorRef) extends Message
  case object NullMessage extends Message {
    override def msg: AnyRef = throw new AkkaException("last receive did not dequeue a message")
    override def sender: ActorRef = throw new AkkaException("last receive did not dequeue a message")
  }

  /**
   * Scala API. Scale timeouts (durations) during tests with the configured
   * 'akka.test.timefactor'.
   * Implicit conversion to add dilated function to Duration.
   * import scala.concurrent.duration._
   * import akka.testkit._
   * 10.milliseconds.dilated
   *
   * Corresponding Java API is available in TestKit.dilated
   */
  implicit def duration2TestDuration(duration: Duration) = new TestDuration(duration)
  /**
   * Wrapper for implicit conversion to add dilated function to Duration.
   */
  class TestDuration(duration: Duration) {
    def dilated(implicit system: ActorSystem): Duration = {
      duration * 1.0
    }
  }
  /**
   * Await until the given condition evaluates to `true` or the timeout
   * expires, whichever comes first.
   *
   * If no timeout is given, take it from the innermost enclosing `within`
   * block.
   *
   * Note that the timeout is scaled using Duration.dilated, which uses the
   * configuration entry "akka.test.timefactor"
   */
  def awaitCond(p: ⇒ Boolean, max: Duration, interval: Duration = 100.millis, noThrow: Boolean = false): Boolean = {
    val stop = now + max

    @tailrec
    def poll(): Boolean = {
      if (!p) {
        val toSleep = stop - now
        if (toSleep <= Duration.Zero) {
          if (noThrow) false
          else throw new AssertionError("timeout " + max + " expired")
        } else {
          Thread.sleep((toSleep min interval).toMillis)
          poll()
        }
      } else true
    }

    poll()
  }

  /**
   * Obtain current timestamp as Duration for relative measurements (using System.nanoTime).
   */
  def now: Duration = System.nanoTime().nanos

  /**
   * Java API. Scale timeouts (durations) during tests with the configured
   * 'akka.test.timefactor'.
   */
  def dilated(duration: Duration, system: ActorSystem): Duration =
    duration * 1.0


  def apply(id: String, system: ActorSystem): TestViewActorProbe = {
    val probe = new TestViewActorProbe(id)
    probe.ref = system.actorOf(Props(new TVActor(probe.queue)), id)
    probe
  }
}
