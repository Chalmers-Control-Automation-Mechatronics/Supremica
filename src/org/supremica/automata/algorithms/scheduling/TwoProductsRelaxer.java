package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.automata.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.gui.ActionMan;
import org.supremica.log.*;

public class TwoProductsRelaxer
	implements Relaxer
{
	/**
	 * The output stream
	 */
	private static Logger logger = LoggerFactory.createLogger(TwoProductsRelaxer.class);

	/** Hashtable containing the estimated cost for each combination of two robots **/
	private Hashtable<Integer, Double> twoProdRelax = new Hashtable<Integer, Double>();

	private NodeExpander expander;
	private ModifiedAstar scheduler;
	private Automata plantAutomata;
	private Automata theAutomata;

	public TwoProductsRelaxer(NodeExpander expander, ModifiedAstar scheduler)
		throws Exception
	{
		this.expander = expander;
		this.scheduler = scheduler;
		plantAutomata = scheduler.getPlantAutomata();
		theAutomata = scheduler.getAllAutomata();
	}

	/**
	 * This method calculates the remaining costs for each robot pair. The maximum remaining
	 * cost added to the minimum current cost is returned to be used as an estimate of the 
	 * total remaining cost of the system. 
	 * 
	 * @param node the current node
	 * @return double the heuristic function, h(n), that guides the search, in this case it is the "2-product relaxation"
	 */
	public double getRelaxation(double[] node)
		throws Exception
	{
		double relaxationValue = -1;

		Integer key = scheduler.getKey(new BasicNode(node));
		Object relaxationObject = twoProdRelax.get(key);

		// If the relaxation has already been done once, then return the result
		if (relaxationObject != null)
		{
			relaxationValue = ((Double)relaxationObject).doubleValue();
		}
		else
		{
			// else, calculate the relaxation for every pair of plant automata and return the maximum value
			for (int i=0; i<scheduler.getActiveLength()-1; i++)
			{
				for (int j=i+1; j<scheduler.getActiveLength(); j++)
				{
					// Prepare the automata used in the current relaxation and change their initial state to the current state
					Automata relaxationAutomata = new Automata();
					Alphabet activePlantsAlphabet = new Alphabet();

					// The first automaton is prepared
 					Automaton firstRelaxationPlantAutomaton = plantAutomata.getAutomatonAt(scheduler.getActiveAutomataIndex()[i]);
// 					firstRelaxationPlantAutomaton.getInitialState().setInitial(false);
// 					firstRelaxationPlantAutomaton.getStateWithIndex(node[scheduler.getActiveAutomataIndex()[i]]).setInitial(true);
					
// 					relaxationAutomata.addAutomaton(firstRelaxationPlantAutomaton);
					relaxationAutomata.addAutomaton(plantAutomata.getAutomatonAt(scheduler.getActiveAutomataIndex()[i]));

					// The second automaton is prepared
 					Automaton secondRelaxationPlantAutomaton = plantAutomata.getAutomatonAt(scheduler.getActiveAutomataIndex()[j]);
// 					secondRelaxationPlantAutomaton.getInitialState().setInitial(false);
// 					secondRelaxationPlantAutomaton.getStateWithIndex(node[scheduler.getActiveAutomataIndex()[j]]).setInitial(true);
				
// 					relaxationAutomata.addAutomaton(secondRelaxationPlantAutomaton);

					relaxationAutomata.addAutomaton(plantAutomata.getAutomatonAt(scheduler.getActiveAutomataIndex()[j]));

					// Add the specification automata (updating the current initial state)
					relaxationAutomata.addAutomata(plantAutomata.getSpecificationAutomata());


					activePlantsAlphabet.addEvents(firstRelaxationPlantAutomaton.getAlphabet());
					activePlantsAlphabet.addEvents(secondRelaxationPlantAutomaton.getAlphabet());
					for (int k=0; k<theAutomata.size(); k++)
					{
						Automaton specAutomaton = expander.getIndexMap().getAutomatonAt(k);
						if (specAutomaton.isSpecification())
						{
// 							specAutomaton.getInitialState().setInitial(false);
// 							specAutomaton.getStateWithIndex(node[k]).setInitial(true);
// 							relaxationAutomata.addAutomaton(specAutomaton);

							int initialStateIndex = specAutomaton.getInitialState().getIndex();
							if (node[k] != initialStateIndex)
							{
								for (Iterator<Arc> outgoingArcs = expander.getIndexMap().getStateAt(specAutomaton, (int)node[k]).outgoingArcsIterator(); outgoingArcs.hasNext();)
								{
									LabeledEvent currEvent = outgoingArcs.next().getEvent();
									if (!activePlantsAlphabet.contains(currEvent))
									{
										node[k] = initialStateIndex;
									}
								}
							}
						}
					}

					// The scheduling is performed from the current state
					//					logger.warn("Startar ny oneProdRelaxer för robotarna " + firstRelaxationPlantAutomaton.getName() + " och " + secondRelaxationPlantAutomaton.getName() + "; nr_of_plants = " + relaxationAutomata.getPlantAutomata().size());
					ModifiedAstar oneProdRelaxer = new ModifiedAstar(theAutomata, "1-prod relax", scheduler.isManualExpansion(), false, true);
					try 
					{
						while (!oneProdRelaxer.isInitialized())
						{
							scheduler.sleep(1);
						}

						oneProdRelaxer.setActiveAutomataIndex(new int[]{i, j});
						oneProdRelaxer.scheduleFrom(new BasicNode(node));

						while (!oneProdRelaxer.schedulingDone())
						{
							scheduler.sleep(1);
						}
					}
					catch (InterruptedException ex)
					{
						logger.error("INTERRUPTED_EXCEPTION in ModifiedAstar.calcEstimatedCost()...");
						throw(ex);
					}

					// If the current relaxation is higher than the maximal relaxation value up to now, update the maximal relaxation value
					if (relaxationValue < oneProdRelaxer.getMakespan())
					{
						relaxationValue = oneProdRelaxer.getMakespan();
					}
				}
			}
		}

		// Adds the minimal current cost to the relaxation value in order to come close to the optimal value without overshooting
 		double estimate = Double.MAX_VALUE;
		double[] currCosts = expander.getCosts(node);
	
		for (int i=0; i<scheduler.getActiveLength(); i++) 
		{
			double altEstimate = currCosts[i] + relaxationValue;
	    
			if (altEstimate < estimate)
			{
				estimate = altEstimate;
			}
		}

// 		if (key == 636)
// 		{
// 			logger.info("636: relax = " + relaxationValue + "; estim = " + estimate + "; est_index = " + ESTIMATE_INDEX + "; acc_index = " + ACCUMULATED_COST_INDEX + "; curr_index = " + CURRENT_COSTS_INDEX + "; activeAutomataIndex = " + printArray(scheduler.getActiveAutomataIndex()));
// 		}

		return estimate;
	}

	public double getRelaxation(Node node)
		throws Exception
	{
		return getRelaxation(node.getBasis());
	}
}
