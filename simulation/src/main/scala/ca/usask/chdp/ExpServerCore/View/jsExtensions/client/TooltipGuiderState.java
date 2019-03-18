package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.JavaScriptExtensionState;

import java.util.Map;

public class TooltipGuiderState extends JavaScriptExtensionState {
   public int step = 0;
   public String ttgName;
   public Map<String, Object> viewSettings;

   public void setStep(int step) {
      this.step = step;
   }
   // Not using:
   public void setViewSettings(Map<String, Object> newMap) {
      this.viewSettings = newMap;
   }
}
