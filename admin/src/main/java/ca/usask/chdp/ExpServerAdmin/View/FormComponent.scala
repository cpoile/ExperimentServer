package ca.usask.chdp.ExpServerAdmin.View

import com.vaadin.data.util.{BeanItem, BeanContainer}
import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui
import com.vaadin.data.fieldgroup.FieldGroup.CommitException
import ui.Button
import ui.CssLayout
import ui.CustomComponent
import ui.Label
import ui.Notification
import ca.usask.chdp.models.SignUpSlotBean
import ui.Table
import com.vaadin.event.ShortcutAction
import collection.JavaConversions._

class FormComponent[T](form: FormBuilder[T],
                       table: Table,
                       container: BeanContainer[String, T],
                       locationForNewBeanButton: (Button) => Unit,
                       newBeanFn: => T,
                       findAllFn: => List[T],
                       validateFn: (T) => (Boolean, T),
                       insertUpdateFn: (T) => T,
                       removeFn: (T) => Boolean,
                       optionalFnOnTableClick: (BeanItem[SignUpSlotBean]) => Unit = null,
                       optionalFnOnTableClickNull: => Unit = null) extends CustomComponent {
  form.setVisible(false)
  val layout = new CssLayout()
  setCompositionRoot(layout)
  layout.addComponent(form)

  table.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      if (event.getProperty.getValue == null) {
        form.setVisible(false)
        container.removeAllItems()
        container.addAll(findAllFn)
        optionalFnOnTableClickNull
      }
      container.getItem(table.getValue) match {
        case v: BeanItem[_] => {
          form.binder.setItemDataSource(v)
          form.setVisible(true)
          form.binder.getFields.toList(0).focus()

          // Display the list of participants enrolled in the session and let us delete them.
          if (optionalFnOnTableClick != null)
            optionalFnOnTableClick(v.asInstanceOf[BeanItem[SignUpSlotBean]])
        }
        case x@_ => println("clicked on: " + x)
      }
    }
  })
  table.setSelectable(true)
  table.setImmediate(true)

  val newBeanButton = new Button("Add Row", new ClickListener {
    def buttonClick(p1: ClickEvent) {
      p1.getButton.setEnabled(false)
      table.select(null)
      table.setEnabled(false)
      p1.getButton.setEnabled(false)
      form.binder.setItemDataSource(newBeanFn)
      form.setVisible(true)
      form.binder.getFields.toList(0).focus()
    }
  })
  newBeanButton.setSizeUndefined()
  newBeanButton.setPrimaryStyleName("btn btn-primary")
  locationForNewBeanButton(newBeanButton)

  var deleteCount = 0
  val deleteNotice = new Label("Press again to confirm delete.")
  def removeDeleteWarning() {
    deleteCount = 0
    form.removeComponent(deleteNotice)
  }

  val formFooter = new ui.HorizontalLayout()
  val submitButton = new Button("Submit", new ClickListener {
    def buttonClick(p1: ClickEvent) {
      removeDeleteWarning()
      try {
        form.binder.commit()
        val item = form.binder.getItemDataSource
        // try to persist and add to the table.
        val (success, beanFixed) =validateFn(item.getBean)
        if (success) {
          insertUpdateFn(beanFixed)
          form.setVisible(false)
          table.setEnabled(true)
          table.select(null)
          newBeanButton.setEnabled(true)
          container.removeAllItems()
          container.addAll(findAllFn)
        }
        else
          throw new CommitException("Something was wrong.")
      } catch {
        case e: CommitException => Notification.show("Commit failed. Reason: " + e)
      }
    }
  })

  submitButton.setSizeUndefined()
  submitButton.setPrimaryStyleName("btn btn-primary")
  submitButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
  formFooter.addComponent(submitButton)


  val cancelButton =
    new Button("Cancel", new ClickListener {
      def buttonClick(p1: ClickEvent) {
        removeDeleteWarning()
        form.binder.discard()
        form.setVisible(false)
        table.setEnabled(true)
        table.select(null)
        newBeanButton.setEnabled(true)
        container.removeAllItems()
        container.addAll(findAllFn)
      }
    })
  cancelButton.setSizeUndefined()
  cancelButton.setPrimaryStyleName("btn btn-primary")
  formFooter.addComponent(cancelButton)

  val deleteButton =
    new Button("Delete Item", new ClickListener {
      def buttonClick(event: ClickEvent) {
        deleteCount += 1
        if (deleteCount < 2) {
          form.addComponent(deleteNotice)
        } else {
          removeDeleteWarning()
          try {
            form.binder.commit()
            val item = form.binder.getItemDataSource
            val ok = removeFn(item.getBean)
            if (ok) {
              form.setVisible(false)
              table.setEnabled(true)
              newBeanButton.setEnabled(true)
              container.removeAllItems()
              container.addAll(findAllFn)
            }
            else
              throw new CommitException("Something went wrong.")
          } catch {
            case e: CommitException => Notification.show("Commit failed. Reason: " + e)
          }
        }
      }
    })
  deleteButton.setSizeUndefined()
  deleteButton.setPrimaryStyleName("btn btn-primary")
  formFooter.addComponent(deleteButton)
  formFooter.setSpacing(true)
  form.addComponent(formFooter)
}
