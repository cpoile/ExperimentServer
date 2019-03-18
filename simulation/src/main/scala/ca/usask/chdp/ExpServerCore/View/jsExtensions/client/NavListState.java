package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;


public class NavListState extends JavaScriptComponentState {
   public String contentContainerId;

   public NavListState(String contentContainerId) {
      this.contentContainerId = contentContainerId;
   }
}
