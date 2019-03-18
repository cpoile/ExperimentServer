package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui.{Label, Slider, CustomLayout, CustomComponent}
import com.vaadin.shared.ui.slider.SliderOrientation
import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.data.Property
import ca.usask.chdp.ExpServerCore.ExpActors.PlayerLogic.{PlayerInfo, WorkOnProj2}

class Proj2View(player: PlayerInfo) extends CustomComponent with UIRefreshable {

  val layout = new CustomLayout("main/proj2Layout")
  layout.setStyleName("proj2Layout")

  val sliderValue = new Label
  sliderValue.setSizeUndefined()
  slider.setPrimaryStyleName("proj2Slider")
  slider.setOrientation(SliderOrientation.HORIZONTAL)
  slider.setWidth("155px")
  slider.setMin(0)
  slider.setMax(0)
  slider.setResolution(0)
  slider.setImmediate(true)
  slider.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: Property.ValueChangeEvent) {
      sliderValue.setValue(slider.getValue.toInt.toString)
    }
  })
  sliderValue.setValue("0")
  layout.addComponent(slider, "slider")

  bindButtonToHtml(layout, "workOnProj2", "Work on Project 2", {player.playerLogic ! WorkOnProj2(slider.getValue.toInt)})

  registerCustomUpdate("proj2SliderMax", value => {
    val newMax = Integer.parseInt(value.toString)
    slider.setMax(newMax)
  })
  layout.addComponent(sliderValue, "sliderValue")

  val proj2DaysWorked = new Label
  registerCustomUpdate("proj2DaysWorked", value => {
    val newMap = value.asInstanceOf[Map[Int, Int]]
    proj2DaysWorked.setValue(mapToString(newMap))
  })
  layout.addComponent(proj2DaysWorked, "proj2DaysWorked")

  setCompositionRoot(layout)

  def mapToString(map: Map[Int, Int]): String = {
    val ret = for ((k,v) <- map) yield ("key: " + k.toString + " value: " + v.toString + "\n")
    ret.mkString
  }
}
