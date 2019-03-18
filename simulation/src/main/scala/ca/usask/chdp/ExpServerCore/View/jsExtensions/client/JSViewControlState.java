package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.JavaScriptExtensionState;

import java.util.*;

public class JSViewControlState extends JavaScriptExtensionState {
   public Boolean isPersProjectEnabled = false;
   public Boolean isAWaitingForB = false;
   public List<Boolean> isGoalReached = Arrays.asList(false, false, false);

   public void setPersProjectEnabled(Boolean enabled) {
      isPersProjectEnabled = enabled;
   }
   public void setAWaitingForB(Boolean waiting) {
      isAWaitingForB = waiting;
   }
   public void setGoalReachedForPart(int partNum, Boolean isGoalReached) {
      this.isGoalReached.set(partNum-1, isGoalReached);
   }
}
