package net.sourceforge.waters.gui.flexfact;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.gui.simulator.Simulation;

public class LocalServer implements Runnable {
	public static List<String> events = new ArrayList<String>();
	static ServerSocket awaitingSocket;
	static Socket localSocket;
	Thread localThread;
    Thread flexfact = null;
    Simulation sim;

    public LocalServer(final Simulation _sim) {
      sim = _sim;
    };

	@Override
    public void run(){

  		try{
  		  // ServerSocket to grab Flexfact's waiting connection
  		  awaitingSocket = new ServerSocket(40001);

  		  while(true){
    		  localSocket = awaitingSocket.accept();
              localThread = new Thread(new LocalSocket());
              localThread.start();
              flexfact = new Thread(new Flexfact(sim));
              flexfact.start();
  		  }

  		}
  		catch(final Exception e) {
  		  System.out.println("Closing Local...");
            try {
              awaitingSocket.close();
              localSocket.close();
            } catch (final IOException exception) {
              exception.printStackTrace();
            }
  		}
  	}

}

