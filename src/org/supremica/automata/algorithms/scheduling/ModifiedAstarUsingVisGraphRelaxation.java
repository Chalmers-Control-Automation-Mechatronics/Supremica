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
	//private Hashtable<String, Integer> visGraphRelax = null;

	/**
	 * The output stream
	 */
	private static Logger logger = LoggerFactory.createLogger(ModifiedAstarUsingVisGraphRelaxation.class);

	/*
	 * The total cycle times of each robot (when run independently)
	 */
	private double[] cycleTimes;

	private volatile boolean relaxFromNodes = false;

	public ModifiedAstarUsingVisGraphRelaxation(Automata theAutomata, boolean manualExpansion, boolean buildSchedule, ScheduleDialog gui) 
		throws Exception 
	{
		//	super(theAutomata, manualExpansion, buildSchedule, gui);
		this (theAutomata, manualExpansion, buildSchedule, false, gui);
    }

	public ModifiedAstarUsingVisGraphRelaxation(Automata theAutomata, boolean manualExpansion, boolean buildSchedule, boolean relaxFromNodes, ScheduleDialog gui) 
		throws Exception 
	{
			super(theAutomata, manualExpansion, buildSchedule, gui);
			this.relaxFromNodes = relaxFromNodes;
			isRelaxationProvider = false; //fulhack
    }

	public void init()
		throws Exception
	{
		super.init();

		visGraphRelax = new Hashtable<String, Double>();

		cycleTimes = new double[plantAutomata.size()];
		for (int i=0; i<cycleTimes.length; i++)
		{
			State initState = plantAutomata.getAutomatonAt(i).getInitialState();
			cycleTimes[i] = remainingCosts[i][initState.getIndex()] + initState.getCost();
		}

		timer.restart();
		preprocessVisibilityGraphs();
		outputStr += "\tvisibility graphs calculated in " + timer.elapsedTime() + "ms\n"; 
	}

	double getRelaxation(double[] node)
		throws Exception
	{
		double[] effTimePoint = new double[plantAutomata.size()];
		String timePointKey = "";

		// Calculate the visibility graph time coordinate, corresponding to the current node
		for (int i=0; i<plantAutomata.size(); i++)
		{
			double remainingCycleTime = remainingCosts[i][(int)node[activeAutomataIndex[i]]];
			if (!relaxFromNodes)
			{
				remainingCycleTime += node[CURRENT_COSTS_INDEX + i];
			}

			effTimePoint[i] = cycleTimes[i] - remainingCycleTime;
			timePointKey += effTimePoint[i] + "_";
		}

		double estimatedRemainingCost;
		Double previousVisibilityRelaxation = visGraphRelax.get(timePointKey);
		if (previousVisibilityRelaxation == null)
		{
			double visibilityRelaxation = -1;
				
			for (int i=0; i<plantAutomata.size() - 1; i++)
			{
				for (int j=i+1; j<plantAutomata.size(); j++)
				{
					String visibilityGraphsKey = plantAutomata.getAutomatonAt(i).getName() + "_" + plantAutomata.getAutomatonAt(j).getName();
					VisGraphScheduler relaxScheduler = visibilityGraphs.get(visibilityGraphsKey);
				
					//Tillf
					ActionTimer relaxTimer = new ActionTimer();
					relaxTimer.restart();
					
					double currVisibilityRelaxation = relaxScheduler.scheduleFrom(new double[]{effTimePoint[i], effTimePoint[j]});
					try 
					{
						while (!relaxScheduler.schedulingDone())
						{
							astarThread.sleep(1);
						}
					}
					catch (InterruptedException ex)
					{
						logger.error("INTERRUPTED_EXCEPTION in AbstractAstar.calcEstimatedCost()...");
						throw(ex);
					}				

					//Tillf
					scheduleFromTime += relaxTimer.elapsedTime();
					scheduleFromCounter++;
					
					if (visibilityRelaxation < currVisibilityRelaxation)
					{
						visibilityRelaxation = currVisibilityRelaxation;
					}
				}
			}

			estimatedRemainingCost = visibilityRelaxation;
			visGraphRelax.put(timePointKey, estimatedRemainingCost);
		}
		else
		{
			estimatedRemainingCost = previousVisibilityRelaxation.doubleValue();
		}

	// 	estimatedRemainingCost += node[ACCUMULATED_COST_INDEX];

		// If the estimate is done from the next node, the minimal current cost is added
		if (relaxFromNodes)
		{
			double minCurrentCost = 500; //Double.MAX_VALUE;
			for (int i=0; i<plantAutomata.size(); i++)
			{
				if (node[CURRENT_COSTS_INDEX + i] < minCurrentCost)
				{
					minCurrentCost = node[CURRENT_COSTS_INDEX + i];
				}
			}

			estimatedRemainingCost += minCurrentCost;
		}

		//Tillf
		boolean approximation = false;
		if (approximation)
		{
			double xWeight = 30;
			double yWeight = 40;
			double depth = 0;
			for (int i=0; i<activeAutomataIndex.length; i++)
			{
				depth += Math.pow(node[activeAutomataIndex[i]], 2);
// 				depth += node[activeAutomataIndex[i]];
			}
			depth = Math.sqrt(depth);
// 			depth = depth / activeAutomataIndex.length;
// 		    depth = (int)Math.floor(Math.random() * depth);
			return estimatedRemainingCost * ( 1 + xWeight / (yWeight + depth));
		}

		return estimatedRemainingCost;
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