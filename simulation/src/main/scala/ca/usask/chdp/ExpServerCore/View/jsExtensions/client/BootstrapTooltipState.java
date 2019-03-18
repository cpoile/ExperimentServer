package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;

import java.util.UUID;

public class BootstrapTooltipState extends JavaScriptComponentState {
   public String value = "";
   public String tooltipClass = UUID.randomUUID().toString();
   public String targetClass, containerClass;

   public BootstrapTooltipState(String targetClass, String containerClass) {
      this.targetClass = targetClass;
      this.containerClass = containerClass;
   }
}
