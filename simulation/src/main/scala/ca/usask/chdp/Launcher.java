/*
 * Copyright 2000-2013 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


// borrowed from:
// https://github.com/vaadin/vaadin/blob/master/uitest/src/com/vaadin/launcher/DevelopmentServerLauncher.java

package ca.usask.chdp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;
import java.security.ProtectionDomain;

public class Launcher {

   /** run under root context */
   private static String contextPath = "/";
   /** location where resources should be provided from for VAADIN resources */

   /** port to listen on */
   private static int httpPort = 8092;

   /**
    * Start the server, and keep waiting.
    */
   public static void main(String[] args) throws Exception {

      Server server = new Server();

      ServerConnector connector = new ServerConnector(server);
      connector.setHost("localhost"); // bind jetty to run only from localhost
      connector.setPort(httpPort);

      server.addConnector(connector);

      ProtectionDomain domain = Launcher.class.getProtectionDomain();
      URL location = domain.getCodeSource().getLocation();

      WebAppContext webapp = new WebAppContext();
      webapp.setContextPath(contextPath);
      webapp.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
      webapp.setServer(server);
      webapp.setWar(location.toExternalForm());

      //webapp.setClassLoader(Thread.currentThread().getContextClassLoader());

      server.setHandler(webapp);
      server.start();

      //      // Add help for System.out
      System.out
              .println("-------------------------------------------------\n"
                      + "Starting Vaadin in Development Mode.\n"
                      + "Running in http://localhost:"
                      + httpPort
                      + "\n-------------------------------------------------\n");

      server.join();
   }
}
//
///**
// * Class for running Jetty servlet container within Eclipse project.
// */
//public class Launcher {
//
//   private final static int serverPort = 8090;
//
//   private static String[] __dftConfigurationClasses =
//           {
//                   "org.eclipse.jetty.webapp.WebInfConfiguration",
//                   "org.eclipse.jetty.webapp.WebXmlConfiguration",
//                   "org.eclipse.jetty.webapp.MetaInfConfiguration",
//                   "org.eclipse.jetty.webapp.FragmentConfiguration",
//                   "org.eclipse.jetty.plus.webapp.EnvConfiguration",
//                   "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
//           };
//
//   /** run under root context */
//   private static String contextPath = "/";
//   /** location where resources should be provided from for VAADIN resources */
//   private static String resourceBase = "src/main/webapp";
//
//   /**
//    * Main function for running Jetty.
//    * <p/>
//    * Command line Arguments are passed through to Jetty, see runServer method
//    * for options.
//    *
//    * @param args
//    * @throws Exception
//    */
//   public static void main(String[] args) throws Exception {
//      System.setProperty("java.awt.headless", "true");
//
//      assertAssertionsEnabled();
//
//      // Pass-through of arguments for Jetty
//      final Map<String, String> serverArgs = parseArguments(args);
//      if (!serverArgs.containsKey("shutdownPort")) {
//         serverArgs.put("shutdownPort", "8889");
//      }
//
//      int port = Integer.parseInt(serverArgs.get("shutdownPort"));
//      if (port > 0) {
//         try {
//            // Try to notify another instance that it's time to close
//            Socket socket = new Socket((String) null, port);
//            // Wait until the other instance says it has closed
//            socket.getInputStream().read();
//            // Then tidy up
//            socket.close();
//         } catch (IOException e) {
//            // Ignore if port is not open
//         }
//      }
//
//      // Start Jetty
//      System.out.println("Starting Jetty servlet container.");
//      String url;
//      try {
//         url = runServer(serverArgs, "Development Server Mode");
//      } catch (Exception e) {
//         // NOP exception already on console by jetty
//      }
//   }
//
//   private static void assertAssertionsEnabled() {
//      try {
//         assert false;
//
//         System.err.println("You should run "
//                 + Launcher.class.getSimpleName()
//                 + " with assertions enabled. Add -ea as a VM argument.");
//      } catch (AssertionError e) {
//         // All is fine
//      }
//   }
//
//   /**
//    * Run the server with specified arguments.
//    *
//    * @param serverArgs
//    * @return
//    * @throws Exception
//    * @throws Exception
//    */
//   protected static String runServer(Map<String, String> serverArgs,
//                                     String mode) throws Exception {
//
//      // Assign default values for some arguments
////      assignDefault(serverArgs, "webroot", "WebContent");
////      assignDefault(serverArgs, "httpPort", "" + serverPort);
////      assignDefault(serverArgs, "context", "");
//
//
//      int port = serverPort;
//      try {
//         port = Integer.parseInt(serverArgs.get("httpPort"));
//      } catch (NumberFormatException e) {
//         // keep default value for port
//      }
//
//      // Add help for System.out
//      System.out
//              .println("-------------------------------------------------\n"
//                      + "Starting Vaadin in "
//                      + mode
//                      + ".\n"
//                      + "Running in http://localhost:"
//                      + port
//                      + "\n-------------------------------------------------\n");
//
//      System.setProperty("java.naming.factory.url","org.eclipse.jetty.jndi");
//      System.setProperty("java.naming.factory.initial","org.eclipse.jetty.jndi.InitialContextFactory");
//      final Server server = new Server();
//      final Connector connector = new SelectChannelConnector();
//      connector.setPort(port);
//      server.setConnectors(new Connector[]{connector});
//
//      final WebAppContext webappcontext = new WebAppContext();
//      webappcontext.setConfigurationClasses(__dftConfigurationClasses);
//      webappcontext.setDescriptor("src/main/webapp/WEB-INF/web.xml");
//      webappcontext.setContextPath(contextPath);
//      webappcontext.setClassLoader(Thread.currentThread().getContextClassLoader());
//      webappcontext.setResourceBase(resourceBase);
//
//      server.setHandler(webappcontext);
//
//      try {
//         server.start();
//         server.join();
//
//         if (serverArgs.containsKey("shutdownPort")) {
//            int shutdownPort = Integer.parseInt(serverArgs
//                    .get("shutdownPort"));
//            final ServerSocket serverSocket = new ServerSocket(
//                    shutdownPort, 1, InetAddress.getByName("127.0.0.1"));
//            new Thread() {
//               @Override
//               public void run() {
//                  try {
//                     System.out
//                             .println("Waiting for shutdown signal on port "
//                                     + serverSocket.getLocalPort());
//                     // Start waiting for a close signal
//                     Socket accept = serverSocket.accept();
//                     // First stop listening to the port
//                     serverSocket.close();
//
//                     // Start a thread that kills the JVM if
//                     // server.stop() doesn't have any effect
//                     Thread interruptThread = new Thread() {
//                        @Override
//                        public void run() {
//                           try {
//                              Thread.sleep(5000);
//                              if (!server.isStopped()) {
//                                 System.out
//                                         .println("Jetty still running. Closing JVM.");
//                                 dumpThreadStacks();
//                                 System.exit(-1);
//                              }
//                           } catch (InterruptedException e) {
//                              // Interrupted if server.stop() was
//                              // successful
//                           }
//                        }
//                     };
//                     interruptThread.setDaemon(true);
//                     interruptThread.start();
//
//                     // Then stop the jetty server
//                     server.stop();
//
//                     interruptThread.interrupt();
//
//                     // Send a byte to tell the other process that it can
//                     // start jetty
//                     OutputStream outputStream = accept
//                             .getOutputStream();
//                     outputStream.write(0);
//                     outputStream.flush();
//                     // Finally close the socket
//                     accept.close();
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//               }
//
//            }.start();
//
//         }
//      } catch (Exception e) {
//         server.stop();
//         throw e;
//      }
//
//      return "http://localhost:" + port + serverArgs.get("context");
//   }
//
//   /**
//    * Assign default value for given key.
//    *
//    * @param map
//    * @param key
//    * @param value
//    */
//   private static void assignDefault(Map<String, String> map, String key,
//                                     String value) {
//      if (!map.containsKey(key)) {
//         map.put(key, value);
//      }
//   }
//
//   /**
//    * Parse all command line arguments into a map.
//    * <p/>
//    * Arguments format "key=value" are put into map.
//    *
//    * @param args
//    * @return map of arguments key value pairs.
//    */
//   protected static Map<String, String> parseArguments(String[] args) {
//      final Map<String, String> map = new HashMap<String, String>();
//      for (int i = 0; i < args.length; i++) {
//         final int d = args[i].indexOf("=");
//         if (d > 0 && d < args[i].length() && args[i].startsWith("--")) {
//            final String name = args[i].substring(2, d);
//            final String value = args[i].substring(d + 1);
//            map.put(name, value);
//         }
//      }
//      return map;
//   }
//
//   private static void dumpThreadStacks() {
//      for (Entry<Thread, StackTraceElement[]> entry : Thread
//              .getAllStackTraces().entrySet()) {
//         Thread thread = entry.getKey();
//         StackTraceElement[] stackTraceElements = entry.getValue();
//
//         System.out.println(thread.getName() + " - " + thread.getState());
//         for (StackTraceElement stackTraceElement : stackTraceElements) {
//            System.out.println("    at " + stackTraceElement.toString());
//         }
//         System.out.println();
//      }
//
//   }
//
//}
