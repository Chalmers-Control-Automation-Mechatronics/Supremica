package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.automata.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.*;
import org.supremica.util.ActionTimer;

public class ModifiedAstarUsingVisGraphRelaxation
	extends ModifiedAstar
{
	/** 
	 * Hashtable containing the estimated cost for a given time configuration 
	 * (as returned by the visibility graph)
	 */
	private Hashtable<String, Integer> visGraphRelax = null;

	/**
	 * The output stream
	 */
	private static Logger logger = LoggerFactory.createLogger(ModifiedAstarUsingVisGraphRelaxation.class);


	public ModifiedAstarUsingVisGraphRelaxation(Automata theAutomata, boolean manualExpansion, boolean buildSchedule, ScheduleDialog gui) 
		throws Exception 
	{
			super(theAutomata, manualExpansion, buildSchedule, gui);
    }

	public void init (boolean manualExpansion)
		throws Exception
	{
		super.init(manualExpansion);

		visGraphRelax = new Hashtable<String, Integer>();

		timer.restart();
		preprocessVisibilityGraphs();
		infoStr += "\tvisibility graphs calculated in " + timer.elapsedTime() + "ms\n";
	}

	public int calcEstimatedCost(int[] node)
		throws Exception
	{
		int[] effTimePoint = new int[plantAutomata.size()];
		String timePointKey = "";

		// Calculate the visibility graph time coordinate, corresponding to the current node
		for (int i=0; i<plantAutomata.size(); i++)
		{
			State initState = plantAutomata.getAutomatonAt(i).getInitialState();
			int totalCycleTime = remainingCosts[i][initState.getIndex()] + initState.getCost();
			int remainingCycleTime = remainingCosts[i][node[activeAutomataIndex[i]]] + node[CURRENT_COSTS_INDEX + i];

			effTimePoint[i] = totalCycleTime - remainingCycleTime;
			timePointKey += effTimePoint[i] + "_";
		}

		Integer previousVisibilityRelaxation = visGraphRelax.get(timePointKey);
		if (previousVisibilityRelaxation == null)
		{
			double visibilityRelaxation = -1;
				
			for (int i=0; i<plantAutomata.size() - 1; i++)
			{
				for (int j=i+1; j<plantAutomata.size(); j++)
				{
					String visibilityGraphsKey = plantAutomata.getAutomatonAt(i).getName() + "_" + plantAutomata.getAutomatonAt(j).getName();
					VisGraphScheduler relaxScheduler = visibilityGraphs.get(visibilityGraphsKey);
				
					double currVisibilityRelaxation = relaxScheduler.scheduleFrom(new double[]{effTimePoint[i], effTimePoint[j]});
						
					if (visibilityRelaxation < currVisibilityRelaxation)
						visibilityRelaxation = currVisibilityRelaxation;
				}
			}

			//tillfälligt så här fullt
			int estimatedCost = (int) Math.round(visibilityRelaxation);
			visGraphRelax.put(timePointKey, estimatedCost);
			return estimatedCost + node[ACCUMULATED_COST_INDEX];
		}
		else
		{
			return previousVisibilityRelaxation.intValue() + node[ACCUMULATED_COST_INDEX];
		}
	}

	private void preprocessVisibilityGraphs()
		throws Exception
	{
		visibilityGraphs = new Hashtable<String, VisGraphScheduler>();

		for (int i=0; i<plantAutomata.size() - 1; i++)
		{
			for (int j=i+1; j<plantAutomata.size(); j++)
			{
				String key = "";
				Automata automataPair = new Automata();

				Automaton currAuto = plantAutomata.getAutomatonAt(i);
				automataPair.addAutomaton(currAuto);
				key += currAuto.getName() + "_";
				
				currAuto = plantAutomata.getAutomatonAt(j);
				automataPair.addAutomaton(currAuto);
				key += currAuto.getName();				

				VisGraphScheduler vgSched = new VisGraphScheduler(automataPair, theAutomata.getSpecificationAutomata(), false, true, gui);
				try 
				{
					while (!vgSched.isInitialized())
					{
						astarThread.sleep(1);
					}
				}
				catch (InterruptedException ex)
				{
					logger.error("EXCEPTION in AbstractAstar.preprocessVisibilityGraphs()...");
					throw(ex);
				}

				visibilityGraphs.put(key, vgSched);
			}
		}
	}
}