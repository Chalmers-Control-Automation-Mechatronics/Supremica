package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;
import java.util.Iterator;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;


public class OneProductRelaxer
	implements Relaxer
{
    private final NodeExpander expander;
    private final ModifiedAstar scheduler;
    private final Automata plantAutomata;

    /**
     * The remaining cost for each state of each robot (if run independently of other robots).
     * int[] = [robot_index, state_index].
     */
    private double[][] remainingCosts;

    public OneProductRelaxer(final NodeExpander expander, final ModifiedAstar scheduler)
            throws Exception
    {
            this.expander = expander;
            this.scheduler = scheduler;
            plantAutomata = scheduler.getPlantAutomata();

            initRemainingCosts();
    }

    /**
     * Calculates the remaining (independent) cycle times for each robot (i.e. as if there
     * would only be one robot in the cell) and stores them in remainingCosts.
     * It is equal to the minimal remaining time from each state to the marked state.
     * This information is used by different heuristics (e.g. 1-prod-relax, 2-prod-relax
     * and visibility graph).
     */
    private void initRemainingCosts()
        throws Exception
    {
        remainingCosts = new double[plantAutomata.size()][];

        for (int i=0; i<plantAutomata.size(); i++)
        {
            final Automaton theAuto = plantAutomata.getAutomatonAt(i);

            // An accepting state is found for the current automaton. In order
            // for the algorithm to work properly, it is assumed that the model is
            // such that the accepting state is also the last state in the sequence(s)
            // of operations, described by the current automaton.
            State markedState = null;

            for (final Iterator<State> stateIt = theAuto.stateIterator(); stateIt.hasNext(); )
            {
                markedState = stateIt.next();

                if (markedState.isAccepting())
                {
                    break;
                }
            }

            if (! markedState.isAccepting())
			{
                throw new Exception("No accepting state for " + theAuto.getName() + " was found during preprocessing...");
            }

            final ArrayList<State> estList = new ArrayList<State>();

            remainingCosts[i] = new double[theAuto.nbrOfStates()];
            for (int j=0; j<remainingCosts[i].length; j++)
            {
                remainingCosts[i][j] = -1;
            }

            if (markedState == null)
            {
                return;
            }
            else
            {
                remainingCosts[i][expander.getIndexMap().getStateIndex(theAuto, markedState)] = markedState.getCost();

                estList.add(markedState);

                while (!estList.isEmpty())
                {
                    final Iterator<Arc> incomingArcIterator = estList.remove(0).incomingArcsIterator();

                    while (incomingArcIterator.hasNext())
                    {
                        final Arc currArc = incomingArcIterator.next();
                        final State currState = currArc.getFromState();
                        final State nextState = currArc.getToState();
                        final int currStateIndex = expander.getIndexMap().getStateIndex(theAuto, currState);
                        final int nextStateIndex = expander.getIndexMap().getStateIndex(theAuto, nextState);

                        if (remainingCosts[i][currStateIndex] == -1)
                        {
                            remainingCosts[i][currStateIndex] = remainingCosts[i][nextStateIndex] + nextState.getCost();
                            estList.add(currState);
                        }
                        else
                        {
                            final double newRemainingCost = nextState.getCost() + remainingCosts[i][nextStateIndex];

                            if (newRemainingCost < remainingCosts[i][currStateIndex])
                            {
                                remainingCosts[i][currStateIndex] = newRemainingCost;
                                estList.add(currState);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
	 * This returns the one-product relaxation value for the supplied node.
	 * This is done by calculating the remaining cost for each robot/plant.
	 * The maximum remaining cost is returned to be used as an estimate of
	 * the total remaining cost of the system.
	 *
	 * @param node the current node
	 * @return double the heuristic function, h(n), that guides the search,
	 * in this case it is the "1-product relaxation"
	 */
    @Override
    public double getRelaxation(final Node node)
        throws Exception
    {
        double estimate = 0;
        final double[] currCosts = expander.getCosts(node);
        @SuppressWarnings("unused")
        final
		int[] activeAutomataIndex = scheduler.getActiveAutomataIndex();

        for (int i=0; i<scheduler.getActiveLength(); i++)
        {
            final double altEstimate = currCosts[i] + remainingCosts[i][(int)node.getValueAt(scheduler.getActiveAutomataIndex()[i])];

            if (altEstimate > estimate)
            {
                estimate = altEstimate;
            }
        }

        return estimate;
    }

	public double[][] getRemainingCosts()
	{
		return remainingCosts;
	}
}