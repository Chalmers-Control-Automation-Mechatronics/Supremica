package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.automata.*;
import org.supremica.util.ActionTimer;

public class VisibilityGraphRelaxer
	implements Relaxer
{
	/*
	 * The total cycle times of each robot (when run independently)
	 */
	private double[] cycleTimes;

    /**
     * Hashtable containing the estimated cost for a given time configuration
     * (as returned by the visibility graph)
     */
    protected Hashtable<String, Double> visGraphRelax = null;
       
    /** The Visibility Graphs, used for relaxation. Can handle two robots at a time */
    protected Hashtable<String, VisGraphScheduler> visibilityGraphs = null;

	private volatile boolean relaxFromNodes;
	private Automata plantAutomata;
	private NodeExpander expander;
	private ModifiedAstar scheduler;
	private OneProductRelaxer oneProdRelaxer;
	private ActionTimer timer;

	public VisibilityGraphRelaxer(NodeExpander expander, ModifiedAstar scheduler, boolean relaxFromNodes)
		throws Exception
	{
		this.expander = expander;
		this.scheduler = scheduler;
		this.relaxFromNodes = relaxFromNodes;

		timer = new ActionTimer();
		plantAutomata = scheduler.getPlantAutomata();
		oneProdRelaxer = new OneProductRelaxer(expander, scheduler);

		init();
	}

	public void init()
		throws Exception
	{
		visGraphRelax = new Hashtable<String, Double>();
		cycleTimes = new double[plantAutomata.size()];

		for (int i=0; i<cycleTimes.length; i++)
		{
			State initState = plantAutomata.getAutomatonAt(i).getInitialState();
			cycleTimes[i] = oneProdRelaxer.getRemainingCosts()[i][initState.getIndex()] + initState.getCost();
		}

		timer.restart();
		preprocessVisibilityGraphs();
		scheduler.addToMessages("\tvisibility graphs calculated in " + timer.elapsedTime() + "ms\n", 
                        SchedulingConstants.MESSAGE_TYPE_INFO); 
	}

	public double getRelaxation(double[] node)
		throws Exception
	{
		double[] effTimePoint = new double[plantAutomata.size()];
		String timePointKey = "";

		// Calculate the visibility graph time coordinate, corresponding to the current node
		for (int i=0; i<plantAutomata.size(); i++)
		{
			double remainingCycleTime = oneProdRelaxer.getRemainingCosts()[i][(int)node[scheduler.getActiveAutomataIndex()[i]]];
			if (!relaxFromNodes)
			{
				remainingCycleTime += node[ModifiedAstar.CURRENT_COSTS_INDEX + i];
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
							scheduler.sleep(1);
						}
					}
					catch (InterruptedException ex)
					{
						scheduler.addToMessages("INTERRUPTED_EXCEPTION in ModifiedAstar.calcEstimatedCost()...", 
                                                        SchedulingConstants.MESSAGE_TYPE_INFO); 
						throw(ex);
					}				

// 					//Tillf
// 					scheduleFromTime += relaxTimer.elapsedTime();
// 					scheduleFromCounter++;
					
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
			double minCurrentCost = Double.MAX_VALUE;
			for (int i=0; i<plantAutomata.size(); i++)
			{
				if (node[ModifiedAstar.CURRENT_COSTS_INDEX + i] < minCurrentCost)
				{
					minCurrentCost = node[ModifiedAstar.CURRENT_COSTS_INDEX + i];
				}
			}

			estimatedRemainingCost += minCurrentCost;
		}

		//Tillf
		boolean approximation = false;
		if (approximation)
		{
			double xWeight = 200;
			double yWeight = 100;
			double depth = 0;
			for (int i=0; i<scheduler.getActiveLength(); i++)
			{
				depth += Math.pow(node[scheduler.getActiveAutomataIndex()[i]], 2);
			}
			depth = Math.sqrt(depth);
			return (new Double(estimatedRemainingCost * ( 1 + xWeight / (yWeight + depth)))).doubleValue();

// 			double addition = 1;
// 			for (int i=0; i<scheduler.getActiveLength(); i++)
// 			{
// 				int automataIndex = scheduler.getActiveAutomataIndex()[i];

// 				double currAddition = oneProdRelaxer.getRemainingCosts()[automataIndex][(int)node[automataIndex]] / cycleTimes[automataIndex];			
// 				if (currAddition < addition)
// 				{
// 					addition = currAddition;
// 				}
// 			}

// 			return estimatedRemainingCost +  addition;
		}

		return estimatedRemainingCost;
	}

	public double getRelaxation(Node node)
		throws Exception
	{
		return getRelaxation(node.getBasis());
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

				VisGraphScheduler vgSched = new VisGraphScheduler(automataPair, scheduler.getAllAutomata().getSpecificationAutomata(), false, true);
				try 
				{
					while (!vgSched.isInitialized())
					{
						scheduler.sleep(1);
					}
				}
				catch (InterruptedException ex)
				{
					scheduler.addToMessages("EXCEPTION in ModifiedAstar.preprocessVisibilityGraphs()...",
                                                SchedulingConstants.MESSAGE_TYPE_INFO); 
					throw(ex);
				}

				visibilityGraphs.put(key, vgSched);
			}
		}
	}
}