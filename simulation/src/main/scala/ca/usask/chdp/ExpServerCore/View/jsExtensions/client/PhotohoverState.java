package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class PhotohoverState extends JavaScriptComponentState {
   public String origSrc;
   public String part1Src;
   public String part2Src;
   public String part3Src;
   public String underlaySrc;

   public PhotohoverState(String origSrc, String part1Src, String part2Src, String part3Src, String underlaySrc) {
      this.origSrc = origSrc;
      this.part1Src = part1Src;
      this.part2Src = part2Src;
      this.part3Src = part3Src;
      this.underlaySrc = underlaySrc;
   }



}
