package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;

public class Milp 
	implements Scheduler
{
	private static Logger logger = LoggerFactory.createLogger(Milp.class);
	
	public Milp() 
		throws Exception
	{
		logger.info("Nu är vi i Milp.java");
	}

	public int[] schedule()
	{
		return null;
	}

	public Automaton buildScheduleAutomaton(int[] markedNode) 
	{
		return null;
	}

 	public native void jniTest();

}