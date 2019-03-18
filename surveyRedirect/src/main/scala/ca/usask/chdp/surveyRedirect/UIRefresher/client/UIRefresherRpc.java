package ca.usask.chdp.surveyRedirect.UIRefresher.client;


import com.vaadin.shared.communication.ServerRpc;

public interface UIRefresherRpc extends ServerRpc {
   public void refresh();
}
