/*
 * Supremica Software License Agreement
 * 
 * The Supremica software is not in the public domain However, it is freely
 * available without fee for education, research, and non-profit purposes. By
 * obtaining copies of this and other files that comprise the Supremica
 * software, you, the Licensee, agree to abide by the following conditions and
 * understandings with respect to the copyrighted software:
 * 
 * The software is copyrighted in the name of Supremica, and ownership of the
 * software remains with Supremica.
 * 
 * Permission to use, copy, and modify this software and its documentation for
 * education, research, and non-profit purposes is hereby granted to Licensee,
 * provided that the copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all such copies, and
 * that no charge be made for such copies. Any entity desiring permission to
 * incorporate this software into commercial products or to use it for
 * commercial purposes should contact:
 * 
 * Knut Akesson (KA), knut@supremica.org Supremica, Haradsgatan 26A 431 42
 * Molndal SWEDEN
 * 
 * to discuss license terms. No cost evaluation licenses are available.
 * 
 * Licensee may not use the name, logo, or any other symbol of Supremica nor the
 * names of any of its employees nor any adaptation thereof in advertising or
 * publicity pertaining to the software without specific prior written approval
 * of the Supremica.
 * 
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE SUITABILITY OF THE
 * SOFTWARE FOR ANY PURPOSE. IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED
 * WARRANTY.
 * 
 * Supremica or KA shall not be liable for any damages suffered by Licensee from
 * the use of this software.
 * 
 * Supremica is owned and represented by KA.
 */

package org.supremica.functionblocks.model;

import java.util.*;

public class ECC
{
	

	// Map: String name -> ECState state
	private Map ecStates = new HashMap();
	// List: ECTransition
	private List ecTransitions = new LinkedList();

	private ECState initialState = null;

	ECC()
	{
		//System.out.println("ECC(): Creating empty ECC");
	}

	void addInitialState(String initial)
	{
		addState(initial);
		initialState = getState(initial);
	}
	
	ECState getInitialState()
	{
		return initialState;
	}

	void addState(String state)
	{
		ecStates.put(state,new ECState(state));
	}

	ECState getState(String state)
	{
		return (ECState) ecStates.get(state);
	}

	void addTransition(String source, String dest, String cond)
	{
		ecTransitions.add(new ECTransition(getState(source), getState(dest), new ECCondition(cond)));
	}

	// returns new state if transition clears, otherwise null
	ECState execute(ECState currentECState, Variables vars)
	{

		// find all transitions that have currentECState in their source
		List ecTransitionsWithSameSource = new LinkedList();
		for(Iterator iter = ecTransitions.iterator();iter.hasNext();)
		{
			ECTransition temp = (ECTransition) iter.next();
			if (temp.getSource() == currentECState)
			{
				ecTransitionsWithSameSource.add(temp);
			}
		}
		

		// evalute all of their conditions and remove the ones that can not be taken
		if(ecTransitionsWithSameSource.size()>0)
		{
			for(Iterator iter = ecTransitionsWithSameSource.iterator();iter.hasNext();)
			{
				ECTransition tempTransition = (ECTransition) iter.next();
				Object evaluationResult = tempTransition.getCondition().evaluate(vars);
				if(evaluationResult instanceof Boolean)
				{
					if( ! ((Boolean) evaluationResult).booleanValue() ) iter.remove();
				}
				else
				{
					System.out.println("ECC.execute(): Non Boolean type returned from evaluation of " + tempTransition.getCondition().get());
				}
			}
		}
		else
		{
			System.out.println("ECC.execute(): no possible transitions from state" + currentECState.getName());
			System.out.println("\t Check your ECC for deadlocks!");
		}
		
		// if more than one condition is true report it and don't do anything,
		// otherwise take the transition
		if(ecTransitionsWithSameSource.size()>0)
		{
			if(ecTransitionsWithSameSource.size()==1)
			{
				return ((ECTransition) ecTransitionsWithSameSource.get(0)).getDestination();
			}
			else
			{
				System.out.println("ECC.execute(): more than one possible transitions from state" + currentECState.getName());
				System.out.println("\t Check your ECC for determinism!");
			}		
		}
		
		// didn't find any transitions that could be taken
		return null;
	}	
}
