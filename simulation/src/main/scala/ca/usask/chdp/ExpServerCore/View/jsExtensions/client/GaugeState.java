package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class GaugeState extends JavaScriptComponentState {
   public int curValue, maxValue;
   public String rgbColor;
   public String elementId;
   public Boolean isReversed;

   public GaugeState(String elementId, int curValue, int maxValue, String rgbColor, Boolean isReversed) {
      this.elementId = elementId;
      this.curValue = curValue;
      this.maxValue = maxValue;
      this.rgbColor = rgbColor;
      this.isReversed = isReversed;
   }
}

/**
 * Back when we had to move lots of data using maps:
 */
//public Map<String, Object> params;
//public Map<String, Object>[] highlights;
//public String[] majorTicks;

//   public GaugeState(Map<String, Object> params, Map<String, Object>[] highlights,
//                     String[] majorTicks, String subtitle, String tooltipId, int startValue) {
//      this.params = params;
//      this.highlights = highlights;
//      this.majorTicks = majorTicks;
//      this.subtitle = subtitle;
//      this.tooltipId = tooltipId;
//      this.curValue = startValue;
//   }
