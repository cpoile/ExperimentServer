package ca.usask.chdp.navlist;

import com.vaadin.shared.ui.JavaScriptComponentState;


public class NavListState extends JavaScriptComponentState {
   public String contentContainerId;

   public NavListState(String contentContainerId) {
      this.contentContainerId = contentContainerId;
   }
}
