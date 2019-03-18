package ca.usask.chdp.surveyRedirect.UIRefresher;

import com.vaadin.ui.UI;

public class SetRefresher {

   public UIRefresher uiRefresher;

   public SetRefresher(UI theUI) {
      uiRefresher = new UIRefresher();
      uiRefresher.extend(theUI);
      uiRefresher.setInterval(5000);
      uiRefresher.setEnabled(true);
   }
}
