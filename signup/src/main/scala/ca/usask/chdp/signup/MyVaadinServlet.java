package ca.usask.chdp.signup;

import com.vaadin.server.VaadinServlet;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MyVaadinServlet extends VaadinServlet {
   static {
      // call only once during initialization time of your application
      SLF4JBridgeHandler.install();
   }
}
