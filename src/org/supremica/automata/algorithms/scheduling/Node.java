package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.LabeledEvent;
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
	private State[] theStates = null;
	private Node theParent = null;
	private static Logger logger = LoggerFactory.createLogger(Node.class);

	public Node(State theState)
	{
		this(theState, null);
	}
	
	public Node(State[] theStates) {
		this(theStates, null);
	}
	
	// Något fult..................
	public Node(State[] theStates, Node theParent) {
		currentCosts = new int[theStates.length];
		this.theStates = new State[theStates.length];
		
		for (int i=0; i<theStates.length; i++)
			this.theStates[i] = theStates[i];

		if (theParent != null) {
			this.theParent = theParent;
			int costReduction = 0;
			
			for (int i=0; i<theStates.length; i++) {
				if (theStates[i].getCost() > -1) {
					if (!(theParent.getState(i).getName().equals(theStates[i].getName()))) {
						costReduction = theParent.getCurrentCosts()[i];
						currentCosts[i] = theStates[i].getCost();
						accumulatedCost = theParent.getAccumulatedCost() + costReduction;
					}
				}
				else 
					currentCosts[i] = theStates[i].getCost();
			}		

			for (int i=0; i<theStates.length; i++) {
				if (theStates[i].getCost() > -1) {
					if (theParent.getState(i).getName().equals(theStates[i].getName())) {
						currentCosts[i] = theParent.getCurrentCosts()[i] - costReduction;
						
						if (currentCosts[i] < 0)
							currentCosts[i] = 0;
					}
				}			
			}
		}
		else {				
			for (int i=0; i<theStates.length; i++) 
				currentCosts[i] = theStates[i].getCost();
			
			accumulatedCost = 0;
		}
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
		if (theStates == null)
			return theState.isAccepting();
		else {
			for (int i=0; i<theStates.length; i++) {
				if (!theStates[i].isAccepting())
					return false;
			}
			
			return true;
		}
	}

	public String getName()
	{
		return theState.getName();
	}
	
	public State[] getStates() {
		return theStates;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public State getState(int i) {
		if (theStates != null)
			return theStates[i];
		return null;
	}
	
	public int size() {
		if (theStates == null)
			return 1;
		else
			return theStates.length;
	}
	
	public String toString() {
		String str = "[";
		
		for (int i=0; i<theStates.length-1; i++) {
			str += theStates[i].getName() + " ";
		}	
		str += theStates[theStates.length-1].getName() + "]";
		
		str += ";   g = " + accumulatedCost + ";   Tv = [";
		for (int i=0; i<currentCosts.length-1; i++)
			str += currentCosts[i] + " ";
		str += currentCosts[currentCosts.length-1] + "]";
		
		return str;
	}
/*	
	public String toStringLight() {
		String str = "";
		
		for (int i=0; i<theStates.length-1; i++) 
			str += theStates[i].getName() + ".";
		str += theStates[theStates.length-1].getName() + "   g = " + accumulatedCost + "   Tv = [";
		
		for (int i=0; i<currentCosts.length-1; i++) {
			if (currentCosts[i] > -1)
				str += currentCosts[i] + " ";
		}
		if (currentCosts[currentCosts.length-1] > -1)
			str += currentCosts[currentCosts.length-1];
		str += "]";
		
		return str;
	}
*/	

	/*
	public int hashCode()
	{
		int hash = 1;
		
		for (int i = 0; i < theStates.length; i++) {
			hash += hash * theStates[i].getIndex();
			hash *= 10;
		}
		
		hash += hash * 100;
		
		return hash;
	}
*/
	public boolean equals(Node otherNode) { 
		if (theStates.length != otherNode.getStates().length)
			return false; 
		
		for (int i=0; i<theStates.length; i++) {
			if (theStates[i].getIndex() != otherNode.getState(i).getIndex())
				return false;
		}
			
		return true;
	}
}
