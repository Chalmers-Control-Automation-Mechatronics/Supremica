/**************** SimpleEstimator.java **********************/
// Does an on-the-fly "one product relaxation"
package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;

public class SimpleEstimator
	implements Estimator
{
	private static Logger logger = LoggerFactory.createLogger(SimpleEstimator.class);

	private Automata automata;
	
	public SimpleEstimator(Automata automata)	// Here we should precalculate the estimates
	{
		this.automata = automata;
	}
	
	public Automata getAutomata()		// Return the stored automata
	{
		return automata;
	}
	
	public int h(Element state)			// For this composite state, return an estimate
	{
		int max = 0;
		int indx = 0;
		for(Iterator it = automata.iterator(); it.hasNext(); ++indx)
		{
			Automaton automaton = (Automaton)it.next();
			if(automaton.isSpecification())
			{
				int c = calc(automaton, automaton.getStateWithIndex(state.getStateArray()[indx])) + state.getTimeArray()[automaton.getIndex()];
				if(max < c)
				{
					max = c;
				}
			}
		}
		return max;
	}
	// Calc the remaining time from this state, just sum up the costs to the accepting state
	// Note - there may be more than a single route to an accepting state
	int calc(Automaton automaton, State state)
	{
		// So as not to upset the indexes for the original problem, make a clone
		Automaton clone = new Automaton(automaton);
		
		clone.setInitialState(state);	// start from here
		Automata tmp = new Automata();
		tmp.addAutomaton(clone);
		
		try
		{
			ModifiedAstar mastar = new ModifiedAstar(tmp);	// gets default estimate (zero estimate);
			Element elem = mastar.walk3();
			int cost = elem.getCost();
			clone = null; // will this help any?
			return cost;
		}
		catch(Exception excp)
		{
			// What the f*** now?
			logger.error(excp);
			logger.error(excp.getStackTrace());
			return -1;
		}
	}
}
	