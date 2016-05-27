package net.sourceforge.waters.gui.flexfact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorStep;

/**
 * Thread to read what comes in from Flexfact
 * @author lkh12
 */
public class Read implements Runnable{

	Socket client;
	BufferedReader in;
	Simulation sim;
	boolean isLocal;

	/**
	 * Constructor to read from the input stream
	 * @param _client - The socket to read from
	 */
	public Read(final Socket _client, final boolean _isLocal, final Simulation _sim) {
		client = _client;
		isLocal = _isLocal;
		sim = _sim;
		try {
			in = new BufferedReader(
			        new InputStreamReader(client.getInputStream()));
		} catch (final IOException e) {}
	}

	/**
	 * Thread execution
	 */
	@Override
	public void run() {
		String line;

		try{
			// Forever reading in lines
			while ((line = in.readLine()) != null){
				if(isLocal)
					System.out.println("local: " + line);
				else
					System.out.println("Flexfact: " + line);

				// If it gives a list of events able to be sent
				// add them to the list of capable commands.
				if(line.startsWith("<Subscribe>")){
					Flexfact.subscribeStr = line; // Unnecessary?
					line = line.replaceAll("</?Subscribe>", "");
					// Fill the list with commands
					Local.events = Arrays.asList(line.split(" +"));
				}
				else if(line.startsWith("<Notify>")){
				  line = line.replaceAll(" *</?Notify> *", "");

				  final List<SimulatorStep> steps = sim.getEnabledSteps();
				  final Iterator<SimulatorStep> i = steps.iterator();
				  SimulatorStep e = null;
				  while(i.hasNext()){
				    final SimulatorStep eventProxy = i.next();
				    if(eventProxy.getEvent().getName().equals(line)){
				      e = eventProxy;
				    }
				  }
				  assert e != null;

				  sim.step(e);
				}
			}
		}catch (final Exception e){
			e.printStackTrace();
		}finally {
			try{
			in.close();
			}
			catch (final IOException e){
				e.printStackTrace();
			}
		}
	}

}
