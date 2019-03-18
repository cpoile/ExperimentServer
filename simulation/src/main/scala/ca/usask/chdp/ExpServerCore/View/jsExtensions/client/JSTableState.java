package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class JSTableState extends JavaScriptComponentState {
   public String elementId;
   public String rawHtml;

   public JSTableState(String elementId, String rawHtml) {
      this.elementId = elementId;
      this.rawHtml = rawHtml;
   }
}
