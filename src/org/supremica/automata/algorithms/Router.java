/************************** Router.java ******************/
// Given an Automata and a set of composite states valid 
// for those automata, generate a trace leading from the 
// initial state to the given state. The set of states is
// given as an array of State-arrays (is this good?)
// Router is a simple wrapper for AutomataSynchronizer

//-- owner: MF

package org.supremica.automata.algorithms;

import org.supremica.automata.Automata;
import org.supremica.automata.State;
import org.supremica.gui.MonitorableThread;

public class Router
	extends MonitorableThread
{
	private /* volatile */ boolean stop_requested;
	
	public Router(Automata automata, State[][] composite)
	{
		setPriority(Thread.MIN_PRIORITY);

	}
	
	// MonitorableThread implementation
	public int getProgress()
	{
		return 0;
	}
	
	public String getActivity()
	{
		return "Routing...";
	}
	
	public void stopTask()
	{
		stop_requested = true;
	}
	
	public boolean wasStopped()
	{
		return stop_requested;
	}
}