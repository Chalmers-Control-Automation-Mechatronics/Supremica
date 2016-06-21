package net.sourceforge.waters.gui.flexfact;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.gui.simulator.Simulation;

public class Flexfact implements Runnable {
	static List<String> events = new ArrayList<String>();
	final Simulation sim;
	static Socket flexFactSocket;
	static PrintWriter flexFactOut;
	Thread sendingThread;

	public Flexfact(final Simulation _sim) {
	  sim = _sim;
	};

	@Override
    public void run(){

		try{
		    flexFactSocket = new Socket(InetAddress.getLocalHost(), 40000);
            flexFactOut = new PrintWriter(flexFactSocket.getOutputStream(), true); // For sending out Subscribe

            sendingThread = new Thread(new Read(flexFactSocket, false, sim));
            sendingThread.start();

			//Start reading what Flexfact has to say locally and on the Flexfact socket.
			System.out.println("This is a Flexfact exec");
			SendMessage();

		}
		catch(final IOException e) {
		  System.out.println("Closing Flexfact...");
          flexFactOut.close();
          try {
            flexFactSocket.close();

          } catch (final IOException exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
          }
		}
	}

	public static void SendMessage(){
	  flexFactOut.println("<Subscribe> </Subscribe>");
	}
}
