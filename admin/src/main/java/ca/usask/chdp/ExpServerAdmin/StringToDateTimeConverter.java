package ca.usask.chdp.ExpServerAdmin;

import com.vaadin.data.util.converter.Converter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class StringToDateTimeConverter implements Converter<String, DateTime> {

   @Override
   public DateTime convertToModel(String value, Locale locale) throws ConversionException {
      if (value == null) {
         return null;
      }
      DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd, hh:mm aa");
      return fmt.parseDateTime(value);
   }

   @Override
   public String convertToPresentation(DateTime value, Locale locale) throws ConversionException {
      return value.toString("EEE, MMM dd, hh:mm aa");
   }

   @Override
   public Class<DateTime> getModelType() {
      return DateTime.class;
   }

   @Override
   public Class<String> getPresentationType() {
      return String.class;
   }
}
