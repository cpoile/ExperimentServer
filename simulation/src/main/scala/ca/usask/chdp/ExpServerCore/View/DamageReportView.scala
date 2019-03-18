package ca.usask.chdp.ExpServerCore.View

import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{FinishedReadingInfo, PlayerInfo}
import com.vaadin.ui.{Button, Label, CustomLayout, CustomComponent}
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import akka.event.Logging
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import jsExtensions.Photohover
import com.vaadin.annotations.JavaScript
import akka.actor.ActorSystem

@JavaScript(Array("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js", "qualifyURL.js"))
class DamageReportView(val player: PlayerInfo, stateBean: UIState) extends CustomComponent {
  val log = Logging(Lobby.system, "DamageReportView")

  val layout = new CustomLayout("main/damageReportLayout")
  setCompositionRoot(layout)

  val (realDamage1, damageLevel1, part1NextRoundStart) = getRealDamage("Part1")
  val (realDamage2, damageLevel2, part2NextRoundStart) = getRealDamage("Part2")
  val (realDamage3, damageLevel3, part3NextRoundStart) = getRealDamage("Part3")

  val part1DamLabel = new Label(getDamageText(damageLevel1))
  part1DamLabel.setStyleName(getColor(damageLevel1))
  part1DamLabel.setSizeUndefined()
  val part2DamLabel = new Label(getDamageText(damageLevel2))
  part2DamLabel.setStyleName(getColor(damageLevel2))
  part2DamLabel.setSizeUndefined()
  val part3DamLabel = new Label(getDamageText(damageLevel3))
  part3DamLabel.setStyleName(getColor(damageLevel3))
  part3DamLabel.setSizeUndefined()

  layout.addComponent(part1DamLabel, "part1DamLabel")
  layout.addComponent(part2DamLabel, "part2DamLabel")
  layout.addComponent(part3DamLabel, "part3DamLabel")

  val part1FinalData = new Label(stateBean.finalPartData("Part1").toString)
  part1FinalData.setSizeUndefined()
  layout.addComponent(part1FinalData, "part1FinalData")
  val part1NewData = new Label(part1NextRoundStart.toString)
  part1NewData.setSizeUndefined()
  layout.addComponent(part1NewData, "part1NewData")

  val part2FinalData = new Label("%.1f".format(stateBean.finalPartData("Part2").toDouble / 10))
  part2FinalData.setSizeUndefined()
  layout.addComponent(part2FinalData, "part2FinalData")
  val part2NewData = new Label("%.1f".format(part2NextRoundStart.toDouble / 10))
  part2NewData.setSizeUndefined()
  layout.addComponent(part2NewData, "part2NewData")

  val part3FinalData = new Label(stateBean.finalPartData("Part3").toString)
  part3FinalData.setSizeUndefined()
  layout.addComponent(part3FinalData, "part3FinalData")
  val part3NewData = new Label(part3NextRoundStart.toString)
  part3NewData.setSizeUndefined()
  layout.addComponent(part3NewData, "part3NewData")

  val partName1 = new Label(getPartNameText(player.role, "Part1"))
  partName1.setSizeUndefined()
  layout.addComponent(partName1, "partName1")
  val partName2 = new Label(getPartNameText(player.role, "Part2"))
  partName2.setSizeUndefined()
  layout.addComponent(partName2, "partName2")
  val partName3 = new Label(getPartNameText(player.role, "Part3"))
  partName3.setSizeUndefined()
  layout.addComponent(partName3, "partName3")


  def getDamageText(damageLevel: Int): String = damageLevel match {
    case 1 => "minor wear and tear"
    case 2 => "moderate and expected"
    case 3 => "severe and unanticipated"
  }
  def getPartNameText(role: String, partNum: String): String = role match {
    case "RoleA" => {
      partNum match {
        case "Part1" => "Engine Damage"
        case "Part2" => "Powertrain Damage"
        case "Part3" => "Wheel Assembly Damage"
      }
    }
    case "RoleB" =>
      partNum match {
        case "Part1" => "Air Intake Damage"
        case "Part2" => "Damage to Wings"
        case "Part3" => "Suspension Damage"
      }
  }
  def getColor(damageLevel: Int): String = damageLevel match {
    case 1 => "green"
    case 2 => "yellow"
    case 3 => "red"
  }
  def getDamageLevel(numPartMarksDamage: Int): Int = numPartMarksDamage match {
    case d if (0 to 2 contains d) => 1
    case d if (3 to 4 contains d) => 2
    case d if (5 to 15 contains d) => 3
  }
  def getRealDamage(partNum: String): (Int, Int, Int) = {
    val damage = partNum match {
      case "Part1" => stateBean.damagePart1
      case "Part2" => stateBean.damagePart2
      case "Part3" => stateBean.damagePart3
    }
    val partDataAfterDamage = stateBean.partsStartEnd(partNum)._2 - damage * Lobby.settings.amtChange(partNum)
    val finalPartData = partNum match {
      case "Part1" => stateBean.finalPartData("Part1")
      case "Part2" => stateBean.finalPartData("Part2")
      case "Part3" => stateBean.finalPartData("Part3")
    }
    val realDamage = partNum match {
      case "Part2" => partDataAfterDamage - finalPartData
      case _ => finalPartData - partDataAfterDamage
    }
    val realNumPartMarks = math.abs(realDamage / Lobby.settings.amtChange(partNum))
    val damageLevel = getDamageLevel(realNumPartMarks)
    log.debug("Part: {}; PartStart: {}; PartEnd: {}; Damage: {}; PartDataAfterDamage: {}; FinalPartData: {}; " +
      "RealDamage: {}; RealNumPartMarks: {}; DamageLevel: {}", Array(partNum, stateBean.partsStartEnd(partNum)._1,
      stateBean.partsStartEnd(partNum)._2, damage, partDataAfterDamage, finalPartData,
      realDamage, realNumPartMarks, damageLevel))
    (realDamage, damageLevel, partDataAfterDamage)
  }

  val continue = new Button("Continue", new ClickListener {
    def buttonClick(event: ClickEvent) {
      player.playerLogic ! FinishedReadingInfo
      event.getButton.setEnabled(false)
    }
  })
  continue.setPrimaryStyleName("btn btn-warning")
  layout.addComponent(continue, "continue")

  def getPartPhoto(role: String, partNum: String, damageLevel: Int): String = partNum match {
    case "Part1" => damageLevel match {
      case 1 => role match {
        case "RoleA" => "images/car/engine_green.png"
        case "RoleB" => "images/car/intake_green.png"
      }
      case 2 => role match {
        case "RoleA" => "images/car/engine_yellow.png"
        case "RoleB" => "images/car/intake_yellow.png"
      }
      case 3 => role match {
        case "RoleA" => "images/car/engine_red.png"
        case "RoleB" => "images/car/intake_red.png"
      }
    }
    case "Part2" => damageLevel match {
      case 1 => role match {
        case "RoleA" => "images/car/drive_green.png"
        case "RoleB" => "images/car/wings_green.png"
      }
      case 2 => role match {
        case "RoleA" => "images/car/drive_yellow.png"
        case "RoleB" => "images/car/wings_yellow.png"
      }
      case 3 => role match {
        case "RoleA" => "images/car/drive_red.png"
        case "RoleB" => "images/car/wings_red.png"
      }
    }
    case "Part3" => damageLevel match {
      case 1 => role match {
        case "RoleA" => "images/car/tires_green.png"
        case "RoleB" => "images/car/susp_green.png"
      }
      case 2 => role match {
        case "RoleA" => "images/car/tires_yellow.png"
        case "RoleB" => "images/car/susp_yellow.png"
      }
      case 3 => role match {
        case "RoleA" => "images/car/tires_red.png"
        case "RoleB" => "images/car/susp_red.png"
      }
    }

  }
  val photohover = new Photohover("images/car/base_colourimage.png",
    getPartPhoto(player.role, "Part1", damageLevel1),
    getPartPhoto(player.role, "Part2", damageLevel2),
    getPartPhoto(player.role, "Part3", damageLevel3),
    "images/car/base_reducedimage.png")
  layout.addComponent(photohover, "photohover")

  if (Lobby.settings.testingMode && Lobby.settings.testing_autoClickDamageReport) {
    import scala.concurrent.duration._
    val as: ActorSystem = Lobby.system
    import as.dispatcher

    Lobby.system.scheduler.scheduleOnce(Lobby.settings.autoWorkDelay milliseconds){
      val lock = this.getUI.getSession.getLockInstance
    lock.lock()
      try {
        println("TESTING_AUTOWORK -- Clicking A_BFinishedRoundWnd button.")
        continue.click()
      } finally {
        lock.unlock()
      }
    }
  }
}
