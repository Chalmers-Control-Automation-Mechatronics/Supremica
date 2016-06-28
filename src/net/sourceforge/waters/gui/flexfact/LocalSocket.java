package net.sourceforge.waters.gui.flexfact;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class LocalSocket implements Runnable {
    public static List<String> events = new ArrayList<String>();
    public LocalSocket(){};
    static PrintWriter localOut;
    Thread localThread;
    @Override
  public void run(){

        try{
          // For sending Flexfact commands/notifications
          localOut = new PrintWriter(LocalServer.localSocket.getOutputStream(), true);

          localThread = new Thread(new Read(LocalServer.localSocket, true, null));
          localThread.start();

        }
        catch(final Exception e) {
          System.out.println("Closing Local...");
          try {
            LocalServer.localSocket.close();
          } catch (final IOException exception) {
            exception.printStackTrace();
          }
          localOut.close();
        }
    }

    public static void SendEvent(final String event){

      System.out.println(event);
      localOut.println("<Notify> " + event + " </Notify>");
    }
}

