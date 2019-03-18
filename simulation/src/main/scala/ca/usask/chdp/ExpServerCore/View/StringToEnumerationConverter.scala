package ca.usask.chdp.ExpServerCore.View

import java.util.Locale
import com.vaadin.data.util.converter.Converter

/**
 * A converter that converts from {@link String} to {@link Enumeration} and back.
 * The String representation is given by Enumeration.toString
 * <p>
 * Leading and trailing white spaces are ignored when converting from a String.
 * </p>
 *
 */

class StringToEnumerationConverter[T <: Enumeration](implicit man: Manifest[T])
  extends Converter[String, T#Value] {

  def convertToModel(value: String, locale: Locale): T#Value = {
    if (value == null) {
      null
    } else {
      val newVal = value.trim
      val obj = man.erasure.getField("MODULE$").get(man.erasure).asInstanceOf[T]
      if (obj.values.map(_.toString).contains(newVal)) {
        obj.withName(newVal).asInstanceOf[T#Value]
      } else {
        throw new Converter.ConversionException("Cannot convert " + newVal + " to " + getModelType.getName)
      }
    }
  }
  def convertToPresentation(value: T#Value, locale: Locale): String = {
    if (value == null) {
      return null
    }
    value.toString
  }
  def getModelType: Class[T#Value] = {
    classOf[T#Value]
  }
  def getPresentationType: Class[String] = {
    classOf[String]
  }
}


