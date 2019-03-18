package ca.usask.chdp.survey.UIRefresher;

import com.vaadin.ui.UI;

public class SetRefresher {

   public UIRefresher uiRefresher;

   public SetRefresher(UI theUI) {
      uiRefresher = new UIRefresher();
      uiRefresher.extend(theUI);
      uiRefresher.setInterval(500);
      uiRefresher.setEnabled(true);
   }
}
