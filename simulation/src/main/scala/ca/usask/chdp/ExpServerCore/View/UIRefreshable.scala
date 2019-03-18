package ca.usask.chdp.ExpServerCore.View

import com.vaadin.ui._
import akka.event.Logging
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import ca.usask.chdp.ExpServerCore.ExpActors.Lobby
import com.vaadin.data.util.converter.StringToBooleanConverter
import com.vaadin.data.util.converter.StringToIntegerConverter
import scala.Some
import java.util.UUID

/**
 * Trait for any component that needs to keep track of the labels it's binding, and then
 * be able to update them.
 */
trait UIRefreshable {
  // we only need a UIStateBeanItem so that we can set up the field bindings.
  val uiData: UIStateBeanItem = new UIStateBeanItem(new UIState())

  protected var mapOfLabels = Map.empty[String, Label]
  protected var mapOfButtons = Map.empty[String, Button]
  val log = Logging(Lobby.system, "Refreshable trait")
  private var children = List.empty[UIRefreshable]
  private var lastUpdate: UIStateBeanItem = null
  private var listOfCustomUpdates = List.empty[(String, Any => Unit, UUID)]
  protected var mapOfButtonClickListeners = Map.empty[String, List[Button.ClickListener]]
  protected val slider = new Slider

  /**
   * NOTE: remember that custom updates are called on every update. They do not get checked
   *  to see if there was a change. That is up to the customUpdate function.
   */
  def registerCustomUpdate(fieldName: String, update: Any => Unit, optionalId: UUID = null) {
    listOfCustomUpdates = (fieldName, update, optionalId) :: listOfCustomUpdates
  }
  def removeCustomUpdate(id: UUID) {
    log.debug("removeCustomUpdate called to remove --- id: {}.\n----CustomUpdates before: {}",
      id, listOfCustomUpdates)
    listOfCustomUpdates = listOfCustomUpdates.filterNot{ case (_, _, uuid) => uuid == id}
    log.debug("CustomUpdates before: {}", listOfCustomUpdates)
  }

  /**
   * This is called every time a stateChange is sent to the player. That is _often._
   */
  def setState(bean: UIStateBeanItem) {
    // First update the whole UIData object which updates all Labels by calling label.setValue(newValue) on them
    //
    // Only do this once, because every UI Component will be asked to refresh (for it's buttons)
    if (lastUpdate != bean) {
      uiData.updateThisWithThat(bean)
      lastUpdate = bean
    }
    // Then update our buttons, which can't be as easily updated as a label, so we do it here.
    for ((id, button) <- mapOfButtons) {
      val prop2 = bean.getItemProperty(id)
      val newState = prop2.getValue match {
        case p: Boolean => p
        case _ => false
      }
      //log.debug("old state is: {} and new state is: {}", curState.toString, newState.toString)
      button.setEnabled(newState == true)
    }

    // Then update any special components that are linked to the UI data structure by calling an
    // update function that was registered when the component was added to that view.
    // Let each update fn figure out if it needs to make a change or not.
    for ((id, updateFn, uuid) <- listOfCustomUpdates) {
      val prop = uiData.getItemProperty(id)
      if (prop != null) updateFn(prop.getValue)
    }

    //now update children ---
    children foreach (_.setState(bean))
  }

  // Kick every component, just in case there was a hickup.
  def refreshUI(bean: UIStateBeanItem) {
    uiData.updateThisWithThat(bean)
    // First go through each of our labels, and fire a value change.
    uiData.refreshUI()

    for ((id, button) <- mapOfButtons) {
      val prop = uiData.getItemProperty(id)
      val state = prop.getValue match {
        case p: Boolean => p
        case _ => false
      }
      button.setEnabled(state == true)
    }

    for ((id, refreshFn, uuid) <- listOfCustomUpdates) {
      val prop = uiData.getItemProperty(id)
      refreshFn(prop.getValue)
    }

    // now update children ---
    children foreach (_.refreshUI(bean))

  }


  def addChild(child: UIRefreshable) {
    log.debug("adding child: {}", child)
    children = child :: children
  }

  protected def bindButtonToHtml(layout: CustomLayout, field: String,
                                 buttonText: String, f: => Unit) {
    val clickListener = new Button.ClickListener {
      // call by name.
      log.debug("Setting button: {} 's clickEvent", field)
      def buttonClick(event: Button.ClickEvent) { f }
    }
    val btn = new Button(buttonText, clickListener)
    btn.setDisableOnClick(true)
    layout.addComponent(btn, field)
    mapOfButtons = mapOfButtons + (field -> btn)
    mapOfButtonClickListeners = mapOfButtonClickListeners + (field -> List(clickListener))
  }
  protected def bindMyFieldsToHtml(layout: CustomLayout, fields: Any*) {
    // need to create a master list of all levels of the UIState case class
    for (f <- fields) f match {
      case s: String => {
        bindThisFieldToHtml(s, s, layout).foreach(result => mapOfLabels = mapOfLabels + result)
      }
      case (s1: String, s2: String) => {
        bindThisFieldToHtml(s1, s2, layout).foreach(result => mapOfLabels = mapOfLabels + result)
      }
    }
  }
  protected def bindFieldWithCustomUpdate(layout: CustomLayout, field: String,
                                         htmlLocation: String, customUpdate: (Any) => Unit) {
    bindThisFieldWithoutUpdate(field, htmlLocation, layout).foreach(result => mapOfLabels = mapOfLabels + result)
    registerCustomUpdate(field, customUpdate)
  }
  private def bindThisFieldWithoutUpdate(fieldName: String, htmlField: String,
                                          layout: CustomLayout): Option[(String, Label)] = {
    val label = new Label()
    label.setSizeUndefined()
    layout.addComponent(label, htmlField)
    Some(fieldName, label)
  }
  private def bindThisFieldToHtml(fieldName: String, htmlField: String,
                                  layout: CustomLayout): Option[(String, Label)] = {
    // we are going to use a default-valued uiData bean to bind the fields.
    // This will be done once for this view (the view that mixed in the UIRefreshable trait).
    val label = new Label()
    //val prop = BeanUtilsComp.myGetItemProperty(dataField, dataSource.getItemPropertyIds.toList, dataSource)
    val prop = uiData.getItemProperty(fieldName)
    if (prop == null) {
      // couldn't find this property
      log.error("Could not find property: {}", fieldName)
      return None
    }
    prop.getValue match {
      case p: Int => label.setConverter(new StringToIntegerConverter)
      case p: Boolean => label.setConverter(new StringToBooleanConverter)
      case p: String =>  //Do not need a converter for Strings.
      case p => log.error("UIRefreshable: Unexpected type for property.getValue: {}", p)
    }
    label.setPropertyDataSource(prop)
    label.setSizeUndefined()
    //log.debug("Bound prop: {}, from dataField: {}, of type: {}, with converter: {}, with contentMode: {}",
    //      Array(prop.getValue, fieldName, prop.getType, label.getConverter, label.getContentMode))
    layout.addComponent(label, htmlField)
    Some(fieldName, label)
  }
}

