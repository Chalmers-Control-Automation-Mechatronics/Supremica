package org.supremica.automata.algorithms.scheduling;


import org.supremica.automata.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.*;

public class ModifiedAstarUsingOneProdRelaxation
	extends ModifiedAstar
{
	/**
	 * The output stream
	 */
	private static Logger logger = LoggerFactory.createLogger(ModifiedAstarUsingOneProdRelaxation.class);

	public ModifiedAstarUsingOneProdRelaxation(Automata theAutomata, boolean manualExpansion, boolean buildSchedule, ScheduleDialog gui) 
		throws Exception 
	{
		this(theAutomata, manualExpansion, buildSchedule, false, gui);
    }

	public ModifiedAstarUsingOneProdRelaxation(Automata theAutomata, boolean manualExpansion, boolean buildSchedule, boolean isRelaxationProvider, ScheduleDialog gui) 
		throws Exception 
	{
		super(theAutomata, manualExpansion, buildSchedule, gui);
		
		this.isRelaxationProvider = isRelaxationProvider;
    }

	/**
	 * This method calculates the remaining cost for each robot/plant. The maximum remaining
	 * cost is returned to be used as an estimate of the total remaining cost of the system. 
	 * 
	 * @param node the current node
	 * @return double the heuristic function, h(n), that guides the search, in this case it is the "1-product relaxation"
	 */
  double getRelaxation(double[] node) 
		throws Exception
	{
		double estimate = 0;
		double[] currCosts = expander.getCosts(node);
	
		for (int i=0; i<activeAutomataIndex.length; i++) 
		{
		  // double altEstimate = currCosts[i] + remainingCosts[i][(int)node[activeAutomataIndex[i]]]; 
		  double altEstimate = currCosts[i];
		  if (altEstimate > estimate)
		    estimate = altEstimate;
		}
	
		return estimate;
    }
}
