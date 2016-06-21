package net.sourceforge.waters.gui.flexfact;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Local implements Runnable {
	public static List<String> events = new ArrayList<String>();
	public Local(){};
	static PrintWriter localOut;
	static ServerSocket awaitingSocket;
	static Socket localSocket;
	@Override
  public void run(){

		try{
		  awaitingSocket = new ServerSocket(40001);   // Server Socket to grab Flexfact's waiting connection
		  getComms();

          final Thread localThread = new Thread(new Read(localSocket, true, null));
          localThread.start();

		}
		catch(final Exception e) {
		  System.out.println("Closing...");
          try {
            awaitingSocket.close();
            localSocket.close();
          } catch (final IOException exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
          }
          localOut.close();
		}
	}

	public static void getComms(){
	  try {
      localSocket = awaitingSocket.accept();
      localOut = new PrintWriter(localSocket.getOutputStream(), true); // For sending Flexfact commands/notifications

    } catch (final IOException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    }  // This is for sending commands

	}

	public static void SendEvent(final String event){

	  System.out.println(event);
	  localOut.println("<Notify> " + event + " </Notify>");
	}

	public static void KillThread(){
	  System.out.println("Closing...");
	  try{
        awaitingSocket.close();
        localSocket.close();
        localOut.close();
	  }catch(final IOException e) {
	    e.printStackTrace();
	  }
	}
}

