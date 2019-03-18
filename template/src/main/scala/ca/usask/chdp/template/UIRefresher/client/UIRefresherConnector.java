package ca.usask.chdp.template.UIRefresher.client;

import ca.usask.chdp.template.UIRefresher.UIRefresher;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

@Connect(UIRefresher.class)
public class UIRefresherConnector extends AbstractExtensionConnector {
   private UIRefresherRpc rpc = RpcProxy.create(UIRefresherRpc.class, this);

   private Timer timer = new Timer() {
      @Override
      public void run() {
         rpc.refresh();
      }
   };

   @Override
   public void onStateChanged(StateChangeEvent stateChangeEvent) {
      super.onStateChanged(stateChangeEvent);
      timer.cancel();
      if (isEnabled()) {
         timer.scheduleRepeating(this.getState().interval);
      }
   }

   @Override
   public void onUnregister() {
      timer.cancel();
   }


   @Override
   public UIRefresherState getState() {
      return (UIRefresherState) super.getState();
   }
}
