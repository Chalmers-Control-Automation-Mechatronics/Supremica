package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.State;
import org.supremica.automata.CompositeState;
import org.supremica.log.*;

/**
 *      This class represent an instance of State with a fixed cost, depending on the
 *      path to the underlying State. It is needed in scheduling since the same state
 *      is visited several times (with different costs).
 */
public class Node
{
	private int accumulatedCost = -1;
	private int[] currentCosts = null;
	private State theState = null;
	private Node theParent = null;
	private static Logger logger = LoggerFactory.createLogger(Node.class);

	public Node(State theState)
	{
		this(theState, null);
	}

	public Node(State theState, Node theParent)
	{
		if (theParent != null)
		{
			this.theParent = theParent;

			if (theState instanceof CompositeState)
			{
				boolean[] firingAutomata = ((CompositeState) theState).getFiringAutomata((CompositeState) theParent.getCorrespondingState());

				((CompositeState) theState).updateCosts(theParent.getCurrentCosts(), firingAutomata, theParent.getAccumulatedCost());
			}
			else
			{
				theState.updateCosts(theParent.getCorrespondingState(), theParent.getAccumulatedCost());
			}
		}
		else
		{
			theState.initCosts();
		}

		if (theState instanceof CompositeState)
		{
			int[] currentCosts = ((CompositeState) theState).getCurrentCosts();

			this.currentCosts = new int[currentCosts.length];

			for (int i = 0; i < currentCosts.length; i++)
			{
				this.currentCosts[i] = currentCosts[i];
			}
		}

		this.theState = theState;
		this.accumulatedCost = theState.getAccumulatedCost();
	}

	public int getAccumulatedCost()
	{
		return accumulatedCost;
	}

	public int[] getCurrentCosts()
	{
		return currentCosts;
	}

	public State getCorrespondingState()
	{
		return theState;
	}

	public Node getParent()
	{
		return theParent;
	}

	public boolean isAccepting()
	{
		return theState.isAccepting();
	}

	public String getName()
	{
		return theState.getName();
	}

	public String toString()
	{
		return theState.toString();
	}
}
