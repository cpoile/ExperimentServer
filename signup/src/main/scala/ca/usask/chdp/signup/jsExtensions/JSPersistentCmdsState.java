package ca.usask.chdp.signup.jsExtensions;

import com.vaadin.shared.ui.JavaScriptComponentState;

import java.util.List;

public class JSPersistentCmdsState extends JavaScriptComponentState {
   public List<String> cmds;
   public List<Boolean> boolSetOnOrOff;

   public JSPersistentCmdsState(List<String> cmds, List<Boolean> boolSetOnOrOff) {
      this.cmds = cmds;
      this.boolSetOnOrOff = boolSetOnOrOff;
   }
}
