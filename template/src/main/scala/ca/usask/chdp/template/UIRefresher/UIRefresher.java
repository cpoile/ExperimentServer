package ca.usask.chdp.template.UIRefresher;

import ca.usask.chdp.template.UIRefresher.client.UIRefresherRpc;
import ca.usask.chdp.template.UIRefresher.client.UIRefresherState;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

import java.util.ArrayList;
import java.util.List;

public class UIRefresher extends AbstractExtension {

   public void extend(UI target) {
      super.extend(target);
   }

   public interface UIRefresherListener {
      public void refresh(UIRefresher source);
   }
   private List<UIRefresherListener> listeners = new ArrayList<UIRefresherListener>();

   public UIRefresher() {
      registerRpc(new UIRefresherRpc() {

         @Override
         public void refresh() {
            for (UIRefresherListener listener: listeners) {
               listener.refresh(UIRefresher.this);
            }
         }
      });
   }

   @Override
   protected UIRefresherState getState() {
      return (UIRefresherState) super.getState();
   }
   public void addListener(UIRefresherListener listener) {
      listeners.add(listener);
   }
   public void removeListener(UIRefresherListener listener) {
      listeners.remove(listener);
   }

   public void setInterval(int millis) {
      getState().interval = millis;
   }
   public int getInterval() {
      return getState().interval;
   }
   public void setEnabled(boolean enabled) {
      getState().enabled = enabled;
   }
   public boolean isEnabled() {
      return getState().enabled;
   }
}
