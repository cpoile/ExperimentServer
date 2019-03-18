package ca.usask.chdp.ExpServerAdmin.View.jsExtensions;

import com.vaadin.shared.ui.JavaScriptComponentState;


public class NavListState extends JavaScriptComponentState {
   public String contentContainerId;

   public NavListState(String contentContainerId) {
      this.contentContainerId = contentContainerId;
   }
}
