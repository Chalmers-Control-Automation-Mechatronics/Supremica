
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

public class CompositeState extends State
{
	//private ArrayList theStates;
//	private int[] theStates;
	
	/** The indices of the underlying states */
	private int[] compositeIndices = null;
	
	/** The costs corresponding to the underlying states */
	private int[] compositeCosts = null;

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
	
	public CompositeState(String id, int[] indices) 
	{
		this(id);
		initialize(indices);
	}
	
	public CompositeState(String id, int[] indices, Automata theAutomata) 
	{
		this(id);
		initialize(indices, theAutomata);
	}
	
	public void initialize(int[] indices) 
	{
		// -2 since the last two indices correspond to something funny, not the nbrs of the underlying states. 
		compositeIndices = new int[indices.length-2];
//		nextCosts = new int[indices.length-2];
//		theStates = new int[indices.length-2];
		compositeCosts = new int[indices.length-2];
	}
	
	public void initialize(int[] indices, Automata theAutomata) 
	{
		initialize(indices);
		
		for (int i=0; i<compositeCosts.length; i++) 
			compositeCosts[i] = theAutomata.getAutomatonAt(i).getStateWithIndex(indices[i]).getCost();
			
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
	public int[] getCompositeIndices() { return compositeIndices; }
	
	/**
	 *	Stores the indices of the constituting states. 
	 */
	protected void setCompositeIndices(int[] indices) 
	{
		if (compositeIndices == null) 
			initialize(indices);
		
		for (int i=0; i<compositeIndices.length; i++) 
			compositeIndices[i] = indices[i];
	}
	
	/** 
	 *	Returns the costs corresponding to the underlying states. Overrides 
	 *	the @link getCost() method in org.supremica.automata.State.java. 
	 */
	public int[] getCompositeCosts() { return compositeCosts; }
	
	/**
	 *	Returns the cost accumulated when this state is reached. Note that the 
	 *	path to the state is of importance. 
	 */
/*	public int getAccumulatedCost() { return accumulatedCost; }
	
	public void setAccumulatedCost(int cost) {} 
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
}
