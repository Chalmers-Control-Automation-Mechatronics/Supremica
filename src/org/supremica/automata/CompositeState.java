
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.*;
import org.supremica.log.*;

public class CompositeState extends State
{
	//private ArrayList theStates;
//	private int[] theStates;

	private static Logger logger = LoggerFactory.createLogger(CompositeState.class);
	
	/** The indices of the underlying states */
//rivate int[] compositeIndices = null;
	
	/** The costs corresponding to the underlying states */
	private int[] compositeCosts = null;

	/** 
	 * The current costs in this state (only important in a composite state). 
	 * Depend on which automaton is to be fired as well as the path to this state. 
	 * See for ex. T.Liljenvalls Lic. (currentCosts = T_v).
	 */
	private int[] currentCosts = null;
	
	/** 
	 * Stores the cost accumulated from the initial state until this one.
	 * The value depends normally (if synchronized automaton) on the path to this state.
	 */
	private int accumulatedCost = -1;
	
	private ArrayList composingStates;
	
	// Behövs den??? )(och theStates också...)
/*	public CompositeState(int capacity)
	{
		theStates = new ArrayList(capacity);
	}
*/	
	public CompositeState(State state) 
	{
		super(state);	
	}
	
	public CompositeState(String id) {
		super(id);	
	}
	
	public CompositeState(String id, int[] indices, Automata compositeAutomata) 
	{
		this(id);

//setOwnerAutomaton(ownerAutomaton);
		initialize(indices, compositeAutomata);
	}
	
	public void initialize(int[] indices, Automata theAutomata) 
	{
		composingStates = new ArrayList();
		
		// -2 since the last two indices correspond to something funny, not the nbrs of the underlying states. 	
		for (int i=0; i<indices.length-2; i++)
		{
			//Behövs pgs nån bugg
			theAutomata.getAutomatonAt(i).remapStateIndices();
			
			initComposingStates(theAutomata.getAutomatonAt(i).getStateWithIndex(indices[i]));
		}
		
		initCosts();
	}
	
	/**
	 *	Stores the underlying (non-composite) states. 
	 */
	private void initComposingStates(State currState) 
	{
		if (currState instanceof CompositeState)
			composingStates.addAll(((CompositeState) currState).getComposingStates());
		else
			composingStates.add(currState);
	}
	
	/**
	 *	This method should only be called if the current state is initial in the 
	 *	composed automaton. The currentCosts are then set to composedCosts. 
	 */
	public void initCosts() 
	{
		compositeCosts = new int[composingStates.size()];
		currentCosts = new int[composingStates.size()];
		
		for (int i=0; i<currentCosts.length; i++)
		{
			compositeCosts[i] = ((State) composingStates.get(i)).getCost();
			
			if (isInitial()) 
				currentCosts[i] = compositeCosts[i];
			else
				currentCosts[i] = -1;
		}
				
		accumulatedCost = 0;
	}

/*	public State getStateAt(int index)
	{
		return (State) theStates.get(index);
	}

	public void setStateAt(int index, State state)
	{
		theStates.add(index, state);
	}
*/	
	/**
	 *	Returns the indices of the underlying states.
	 */
//	public int[] getCompositeIndices() { return compositeIndices; }
	
	/**
	 *	Stores the indices of the constituting states. 
	 */
/*	protected void setCompositeIndices(int[] indices) 
	{
		if (compositeIndices == null) 
			initialize(indices);
		
		for (int i=0; i<compositeIndices.length; i++) 
			compositeIndices[i] = indices[i];
	}
*/	
	/** 
	 *	Returns the costs corresponding to the underlying states. Overrides 
	 *	the @link getCost() method in org.supremica.automata.State.java. 
	 */
	public int[] getCompositeCosts() { return compositeCosts; }
	
	/**
	 *	Returns the cost accumulated when this state is reached. Note that the 
	 *	path to the state is of importance. 
	 */
	public int getAccumulatedCost() { return accumulatedCost; }
	
	/**
	 *	Returns the current costs associated to this state (keeping in mind the 
	 *	path to this state). 
	 */
	public int[] getCurrentCosts() { return currentCosts; }
	
	/**
	 *	Calculates and updates the currentCosts-vector and the accumulated cost. 
	 *	The costs in the previously visited state must be submitted as parameters. 
	 *	If the current state does not have any underlying cost(s) associated, the 
	 *	accumulatedCost of the previously visited state is kept. 
	 */
	public void updateCosts(int[] prevCurrentCosts, boolean[] firingAutomata, int prevAccumulatedCost) 
	{		
		int costAddition = 0;
		
		// The value of costAddition is set as the maximal cost for the firing/active automata
		for (int i=0; i<firingAutomata.length; i++)
		{
			if ((firingAutomata[i] == true) && (prevCurrentCosts[i] > costAddition))
				costAddition = prevCurrentCosts[i];	
		}
		
		// The currentCosts-vector is updated
		for (int i=0; i<firingAutomata.length; i++)
		{
			if (firingAutomata[i] == false) 
			{
				if (prevCurrentCosts[i] > -1)
					currentCosts[i] = Math.max(0, prevCurrentCosts[i] - costAddition);
				else
					currentCosts[i] = -1;
			}
			else
				currentCosts[i] = compositeCosts[i]; 	
		}
		
		// The accumulatedCost is updated
		accumulatedCost = prevAccumulatedCost + costAddition;		
	}
	
	/**
	 *	Calculates the firing automata and other necessary parameters and calls 
	 *	updateCosts(int[], boolean[], int);
	 */
	public void updateCosts(CompositeState prevState) 
	{
		if (prevState.isUpdatingCosts()) 
		{
			if (prevState.isTimed())
			{
				boolean[] firingAutomata = new boolean[composingStates.size()];
				ArrayList prevComposingStates = prevState.getComposingStates();
				
				for (int i=0; i<firingAutomata.length; i++) 
				{
					if (composingStates.get(i).equals(prevComposingStates.get(i)))
						firingAutomata[i] = false;
					else
						firingAutomata[i] = true;
				}	
				
				updateCosts(prevState.getCurrentCosts(), firingAutomata, prevState.getAccumulatedCost());
			}
			else
				accumulatedCost = prevState.getAccumulatedCost();
		}
	}
	
	/**
	 *	The cost cannot be updated if the path from the initial state to the current
	 *	state is not remembered. Then the updating shoul be closed. 
	 */
/*	public void closeCostUpdating() 
	{
		accumulatedCost = null;	
	}
*/	
	/**
	 *	This method checks if this state has underlying costs associated to it. 
	 *	Otherwise the updating of costs would not make sense. 
	 */
	private boolean isTimed() 
	{
		boolean timed = false;
		
		for (int i=0; i<compositeCosts.length; i++)
		{
			if (compositeCosts[i] > -1)
				timed = true;
		}	
		
		return timed;
	}
	
	/**
	 *	Checks if the cost updating has not been closed, i.e. if the path to 
	 *	this state is known. 
	 */
	private boolean isUpdatingCosts() 
	{
		return (accumulatedCost	> -1);
	}
	
	/**
	 *	Returns the compositeIndices on a string form (maybe not always very necessary).
	 */
/*	public String getCompositeIndicesAsString() 
	{
		StringBuffer str = new StringBuffer("[");
		for (int i=0; i<compositeIndices.length-1; i++)
		{
			str.append(compositeIndices[i] + " ");
		}
		
		str.append(compositeIndices[compositeIndices.length-1] + "]");
		
		return new String(str);
	}
*/	
	/**
	 *	Returns the cost vector representing the cost for choosing to move every 
	 *	one of the composing automata. 
	 */
/*	public int[] getNextCosts() { return nextCosts; }
	
	public void setNextCosts(int[] costs) {}
*/	
/*	public boolean equals(Object state)
	{
		return theStates.equals(((CompositeState) state).theStates);
	}
*/
/*	public int hashCode()
	{
		return theStates.hashCode();
	}	
*/

	public ArrayList getComposingStates() { return composingStates; }
}
