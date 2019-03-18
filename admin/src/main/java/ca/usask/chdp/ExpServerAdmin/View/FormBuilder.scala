package ca.usask.chdp.ExpServerAdmin.View

import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.ui.FormLayout

class FormBuilder[T](newBeanFn: => T, val counterName: String, val binder: BeanFieldGroup[T],
                     fieldAndPropertyNames: (String, String)*) extends FormLayout {
  binder.setItemDataSource(newBeanFn)
  for (field_prop <- fieldAndPropertyNames) {
    addComponent(binder.buildAndBind(field_prop._1, field_prop._2))
  }
  binder.setBuffered(true)

  def newBean() {
    binder.setItemDataSource(newBeanFn)
  }
  def clear() {
    binder.setItemDataSource(newBeanFn)
  }
}
