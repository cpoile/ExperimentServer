package ca.usask.chdp.ExpServerCore.View

import ca.usask.chdp.ExpServerCore.ExpActors.ViewActor._
import com.vaadin.data.Property
import com.vaadin.data.util.{MethodProperty, BeanItem}
import org.slf4j.LoggerFactory
import reflect.BeanProperty
import collection.JavaConversions._

class UIState() extends Serializable {
  var uiCmd: UIStateCmd = WaitingForPartner
  @BeanProperty var curRound: Int = 0
  @BeanProperty var trackNum: Int = 1
  @BeanProperty var daysLeft: Int = 999
  @BeanProperty var part1Name: String = "MarkI"
  @BeanProperty var part1Next: String = "MarkII"
  @BeanProperty var part1StatusBar: String = "temp"
  @BeanProperty var part2Name: String = "MarkI"
  @BeanProperty var part2Next: String = "MarkII"
  @BeanProperty var part2StatusBar: String = "temp"
  @BeanProperty var part3Name: String = "MarkI"
  @BeanProperty var part3Next: String = "MarkII"
  @BeanProperty var part3StatusBar: String = "temp"
  @BeanProperty var partsStartEnd: Map[String, (Int, Int)] = Map.empty[String, (Int, Int)]
  @BeanProperty var part1Chance: Int = 99
  @BeanProperty var part2Chance: Int = 99
  @BeanProperty var part3Chance: Int = 99
  @BeanProperty var part1NextData: Int = 99
  @BeanProperty var part2NextData: Int = 99
  @BeanProperty var part3NextData: Int = 99
  @BeanProperty var part1CurData: Int = 99
  @BeanProperty var part2CurData: Int = 99
  @BeanProperty var part3CurData: Int = 99
  @BeanProperty var part1Goal: Int = 99
  @BeanProperty var part2Goal: Int = 99
  @BeanProperty var part3Goal: Int = 99
  @BeanProperty var goal1Reached: Boolean = false
  @BeanProperty var goal2Reached: Boolean = false
  @BeanProperty var goal3Reached: Boolean = false
  @BeanProperty var part1Work: Boolean = false
  @BeanProperty var part2Work: Boolean = false
  @BeanProperty var part3Work: Boolean = false
  @BeanProperty var workOnProj2: Boolean = false
  @BeanProperty var proj2SliderMax: Int = 99
  @BeanProperty var proj2DaysWorked = Map.empty[Int, Int]
  @BeanProperty var watchStatusBar: String = ""
  @BeanProperty var damagePart1: Int = 0
  @BeanProperty var damagePart2: Int = 0
  @BeanProperty var damagePart3: Int = 0
  @BeanProperty var finalPartData: Map[String, Int] = Map.empty[String, Int]
}

case class UIStateBeanItem(bean: UIState) extends BeanItem[UIState](bean) with BeanUtils[UIState]

object BeanUtilsComp {
  def myGetItemProperty(targetID: Any, idList: List[_], bean: BeanItem[_]): Property[_] =
    idList match {
      case Nil => null
      case `targetID` :: _ => bean.getItemProperty(targetID)
      case head :: tail if bean.getItemProperty(head).isInstanceOf[BeanItem[_]] =>
        myGetItemProperty(
          targetID,
          bean.getItemProperty(head).asInstanceOf[BeanItem[_]].getItemPropertyIds.toArray.toList,
          bean.getItemProperty(head).asInstanceOf[BeanItem[_]])
      case head :: tail => myGetItemProperty(targetID, tail, bean)
    }
}
trait BeanUtils[T] {
  this: UIStateBeanItem =>
  val log = LoggerFactory.getLogger("BeanUtils")

  // set up a map of manifests for each of the bean fields to use when we are retrieving that
  // field's methodProperty


  def refreshUI() {

    // TODO: fix this with manifests:

    for (id <- this.getItemPropertyIds) {
      val prop = this.getItemProperty(id)
      val methodProp = prop.getValue match {
        case p: Int => this.getItemProperty(id).asInstanceOf[MethodProperty[Int]]
        case p: Boolean => this.getItemProperty(id).asInstanceOf[MethodProperty[Boolean]]
        case p: Map[String, Int] => this.getItemProperty(id).asInstanceOf[MethodProperty[Map[String, Int]]]
        case _ => this.getItemProperty(id).asInstanceOf[MethodProperty[String]] // Default to String object
      }
      methodProp.fireValueChange()

    }
  }

  def fireValChange(id: String) {
    val prop = this.getItemProperty(id)
    val methodProp = prop.getValue match {
      case p: Int => this.getItemProperty(id).asInstanceOf[MethodProperty[Int]]
      case p: Boolean => this.getItemProperty(id).asInstanceOf[MethodProperty[Boolean]]
      case p: Map[String, Int] => this.getItemProperty(id).asInstanceOf[MethodProperty[Map[String, Int]]]
      case _ => this.getItemProperty(id).asInstanceOf[MethodProperty[String]] // Default to String object
    }
    methodProp.fireValueChange()
  }

  def updateThisWithThat(bean: UIStateBeanItem) {
    for (id <- this.getItemPropertyIds) {
      try {
        val prop = bean.getItemProperty(id)

        val methodProp = prop.getValue match {
          case p: Int => bean.getItemProperty(id).asInstanceOf[MethodProperty[Int]]
          case p: Boolean => bean.getItemProperty(id).asInstanceOf[MethodProperty[Boolean]]
          case p: Map[String, Int] => bean.getItemProperty(id).asInstanceOf[MethodProperty[Map[String, Int]]]
          case _ => bean.getItemProperty(id).asInstanceOf[MethodProperty[String]] // Default to String object
        }
        try {
          //log.debug("Got methodProp from bean. Type of prop: {}, methodProp Class: {}", methodProp.getType, methodProp.getClass)
          val newVal = methodProp.getValue
          try {
            val prop2 = this.getItemProperty(id)

            prop2.getValue match {
              case p: Int => {
                val mp2 = this.getItemProperty(id).asInstanceOf[MethodProperty[Int]]
                if(mp2.getValue != newVal) {
                  mp2.setValue(newVal.asInstanceOf[Int])
                }
              }
              case p: Boolean => {
                val mp2 = this.getItemProperty(id).asInstanceOf[MethodProperty[Boolean]]
                if(mp2.getValue != newVal) {
                  mp2.setValue(newVal.asInstanceOf[Boolean])
                }
              }
              case p: Map[String, Int] => {
                val mp2 = this.getItemProperty(id).asInstanceOf[MethodProperty[Map[String, Int]]]
                if(mp2.getValue != newVal) {
                  mp2.setValue(newVal.asInstanceOf[Map[String, Int]])
                }
              }
              case _ => {
                val mp2 = this.getItemProperty(id).asInstanceOf[MethodProperty[String]]
                if(mp2.getValue != newVal) {
                  mp2.setValue(newVal.asInstanceOf[String])
                }
              } // Default to String object
            }
            //log.debug("prop2 class: {}", methodProp2.getType)
//            try {
//
//              val myVal = methodProp2.getValue
//              //              log.debug("Checking to see if we will setproperty: {}, of type: {}, from: {}, to: {}",
//              //                id, methodProp2.getType, myVal, newVal)
//              try {
//                if (myVal != newVal)
//                  methodProp2.setValue(newVal)
//              } catch {
//                case e: Exception => log.error("Could not set myVal: {}, to newVal: {} for prop: {}, exception: {}",
//                  Array(myVal, newVal, id, e))
//              }
//            } catch {
//              case e: Exception => log.error("Couldn't get myVal from this. Exception for prop: {}, methodProp class: {}, exception: {}",
//                id, methodProp2.getClass, e)
//            }
          } catch {
            case e: Exception => log.error("Couldn't get methodprop2 from this. Exception for prop: {}, exception: {}", id, e)
          }
        } catch {
          case e: Exception => log.error("Couldn't get newVal from bean. Exception for prop: {}, methodProp class: {}, exception: {}",
            Array(id, methodProp.getClass, e).asInstanceOf[Array[AnyRef]])
        }
      } catch {
        case e: Exception => log.error("Couldn't get methodprop from bean. Exception for prop: {}, exception: {}", id, e)
      }
    }
  }
}
case class ButtonState(part1Work: String = "Disabled",
                       part2Work: String = "Disabled",
                       part3Work: String = "Disabled")
/**
 * Not using this.
 *
 *
 */
class Event[T] extends Serializable {
  private var callbacks: List[T] = List()

  def apply(callback: T) { addCallback(callback) }
  def addCallback(callback: T) {
    callbacks = callback :: callbacks
  }

  def fireValueChange(f: (T) => Unit) {
    for (c <- callbacks) {
      f(c)
    }
  }
}
trait ValueChangeListenable[T] extends Serializable {
  val onValueChangeEvent = new Event[(T) => Unit]
}