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

package org.supremica.automata.algorithms;

import org.supremica.automata.*;
import java.util.*;

public class AutomatonMinimizer
{
	private Automaton theAutomaton;
	private Alphabet theAlphabet;

	public AutomatonMinimizer(Automaton theAutomaton)
	{
		this.theAutomaton = theAutomaton;
		theAlphabet = theAutomaton.getAlphabet();
	}

	public Automaton getMinimizedAutomaton()
		throws Exception
	{
		EquivalenceClasses equivClasses = new EquivalenceClasses();

		EquivalenceClass acceptingStates = new EquivalenceClass();
		EquivalenceClass forbiddenStates = new EquivalenceClass();
		EquivalenceClass rejectingStates = new EquivalenceClass();

		Iterator stateIt =  theAutomaton.stateIterator();
		while (stateIt.hasNext())
		{
			State currState = (State)stateIt.next();

			if (currState.isForbidden())
			{
				currState.setEquivalenceClass(forbiddenStates);
				forbiddenStates.add(currState);
			}
			else if (currState.isAccepting())
			{
				currState.setEquivalenceClass(acceptingStates);
				acceptingStates.add(currState);
			}
			else
			{
				currState.setEquivalenceClass(rejectingStates);
				rejectingStates.add(currState);
			}
		}

		if (acceptingStates.size() > 0)
		{
			equivClasses.add(acceptingStates);
		}
		if (rejectingStates.size() > 0)
		{
			equivClasses.add(rejectingStates);
		}
		if (forbiddenStates.size() > 0)
		{
			equivClasses.add(forbiddenStates);
		}

		try
		{
			doMinimization(equivClasses);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw e;
		}

		Automaton newAutomaton = buildAutomaton(equivClasses);
		return newAutomaton;
	}

	public Automaton getMinimizedAutomaton(boolean sameEquivClassInitially)
		throws Exception
	{
		if (sameEquivClassInitially)
		{
			EquivalenceClasses equivClasses = new EquivalenceClasses();

			EquivalenceClass initialClass = new EquivalenceClass();

			Iterator stateIt =  theAutomaton.stateIterator();
			while (stateIt.hasNext())
			{
				State currState = (State)stateIt.next();

				currState.setEquivalenceClass(initialClass);
				initialClass.add(currState);
			}
			equivClasses.add(initialClass);

			try
			{
				doMinimization(equivClasses);
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
				throw e;
			}

			Automaton newAutomaton = buildAutomaton(equivClasses);
			return newAutomaton;
		}
		else
		{
			return getMinimizedAutomaton();
		}
	}

	private Automaton buildAutomaton(EquivalenceClasses equivClasses)
	{

		Automaton newAutomaton = new Automaton();
		newAutomaton.setType(theAutomaton.getType());
		newAutomaton.setAlphabet(theAlphabet);
		newAutomaton.setName("min" + theAutomaton.getName());

		// Associate one state with each equivalence class
		int currNbrOfStates = 0;
		Iterator equivClassIt = equivClasses.iterator();
		while (equivClassIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass)equivClassIt.next();
			State currState = new State();
			currState.setId("q" + currNbrOfStates++);

			if (currEquivClass.isInitial())
			{
				currState.setInitial(true);
			}

			if (currEquivClass.isForbidden())
			{
				currState.setForbidden(true);
			}
			else if (currEquivClass.isAccepting())
			{
				currState.setAccepting(true);
			}

			newAutomaton.addState(currState);
			currEquivClass.setState(currState);
		}

		// Build all transitions
		equivClassIt = equivClasses.iterator();
		while (equivClassIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass)equivClassIt.next();
			State fromState = currEquivClass.getState();
			Iterator outgoingArcsIt = currEquivClass.outgoingArcsIterator();
			while (outgoingArcsIt.hasNext())
			{
				Arc currArc = (Arc)outgoingArcsIt.next();
				String currEventId = currArc.getEventId();
				State oldToState = currArc.getToState();
				EquivalenceClass nextEquivalenceClass = (EquivalenceClass)oldToState.getEquivalenceClass();
				State toState = nextEquivalenceClass.getState();
				Arc newArc = new Arc(fromState, toState, currEventId);
				newAutomaton.addArc(newArc);
			}
		}

		return newAutomaton;
	}

	private void doMinimization(EquivalenceClasses equivClasses)
	{
		boolean refined;
		do
		{
			refined = false;
			Iterator classIt = equivClasses.safeIterator();
			while (classIt.hasNext())
			{
				EquivalenceClass currClass = (EquivalenceClass)classIt.next();
				refined = doMinimization(equivClasses, currClass) || refined;
			}
		} while (refined);
	}

	private boolean doMinimization(EquivalenceClasses equivClasses, EquivalenceClass equivClass)
	{
		boolean refined = false;
		Iterator eventIt = theAlphabet.eventIterator();
		while (eventIt.hasNext())
		{
			Event currEvent = (Event)eventIt.next();
			refined = doMinimization(equivClasses, equivClass, currEvent) || refined;
		}
		return refined;
	}

	private boolean doMinimization(EquivalenceClasses equivClasses, EquivalenceClass equivClass, Event e)
	{
		//System.err.println("A iteration in doMinimization <classes, class, event>");
		EquivalenceClassHolder newEquivClassHolder = equivClass.split(e);
		//System.err.println(newEquivClassHolder.size() + " new equivalence classes");
		if (newEquivClassHolder.size() > 1)
		{
			//System.err.println("------------------");
			//System.err.println("equivClasses" + equivClasses);
			//System.err.println("newEquivClasses" + newEquivClassHolder);

			// Remove the current class from all equivalenceClasses
			//System.err.println("Before equivClasses" + equivClasses);
			equivClasses.remove(equivClass);
			//System.err.println("After equivClasses" + equivClasses);
			equivClass.clear();

			// Set the new equivalence class in all states
			//newEquivClassHolder.update();
			// Add the new classes to equivClasses
			equivClasses.addAll(newEquivClassHolder);
			//System.err.println("equivClasses" + equivClasses);
			//System.err.println("newEquivClasses" + newEquivClassHolder);
			//System.err.println("------------------");
			return true;
		}
		else
		{
			// All classifies to the same class.
			// No need to do anything except maybe helping
			// the garbage collector
			newEquivClassHolder.clear();
			return false;
		}
	}
}

class EquivalenceClasses
{
	private LinkedList equivClasses = new LinkedList();

	public void add(EquivalenceClass equivClass)
	{
		equivClasses.add(equivClass);
	}

	public void addAll(EquivalenceClassHolder equivClassHolder)
	{
		Iterator equivIt = equivClassHolder.iterator();
		while (equivIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass)equivIt.next();
			currEquivClass.update();
			equivClasses.add(currEquivClass);
		}
	}

	public void remove(EquivalenceClass equivClass)
	{
		equivClasses.remove(equivClass);
	}

	public Iterator iterator()
	{
		return equivClasses.iterator();
	}

	public Iterator safeIterator()
	{
		return ((LinkedList)equivClasses.clone()).iterator();
	}

	public void clear()
	{
		equivClasses.clear();
	}

	public int size()
	{
		return equivClasses.size();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Iterator equivClassIt = equivClasses.iterator();
		sb.append("(");
		while (equivClassIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass)equivClassIt.next();
			sb.append(currEquivClass);
		}
		sb.append(")");
		return sb.toString();
	}
}

class EquivalenceClass
{
	private LinkedList states = new LinkedList();
	private State newState;
	private EquivalenceClass nextClass = null;

	public EquivalenceClass()
	{
	}

	public EquivalenceClass(EquivalenceClass nextClass)
	{
		this.nextClass = nextClass;
	}

	public EquivalenceClass getNextClass()
	{
		return nextClass;
	}


	public void update()
	{
		Iterator stateIt = states.iterator();
		while (stateIt.hasNext())
		{
			State currState = (State)stateIt.next();
			currState.setEquivalenceClass(this);
		}
	}

	public void add(State state)
	{
		states.add(state);
	}

	public void remove(State state)
	{
		states.remove(state);
	}

	public void addAll(Collection c)
	{
		states.addAll(c);
	}

	public Iterator iterator()
	{
		return states.iterator();
	}

	public boolean isInitial()
	{
		Iterator stateIt = states.iterator();
		while (stateIt.hasNext())
		{
			State currState = (State)stateIt.next();
			if (currState.isInitial())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isAccepting()
	{
		State currState = (State)states.getFirst();
		if (currState.isAccepting())
		{
			return true;
		}
		return false;
	}

	public boolean isForbidden()
	{
		State currState = (State)states.getFirst();
		if (currState.isForbidden())
		{
			return true;
		}
		return false;
	}

	/**
	 * Set the state that corresponds to this equivalence class.
	 * This is used while building the minimized automaton.
	 */
	public void setState(State state)
	{
		newState = state;
	}

	public State getState()
	{
		return newState;
	}

	public Iterator outgoingArcsIterator()
	{
		State currState = (State)states.getFirst();
		Iterator currIt = currState.outgoingArcsIterator();
		return currIt;
	}

	/**
	 *
	 */
	public EquivalenceClassHolder split(Event e)
	{
		// System.err.println("Splitting " + e.getLabel());
		EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();
		// Build a list of equivalance classes that e transfers to
		Iterator stateIt = iterator();
		while (stateIt.hasNext())
		{
			State currState = (State)stateIt.next();
			State nextState = currState.nextState(e);

			EquivalenceClass nextEquivalenceClass = null;
			if (nextState != null)
			{
				nextEquivalenceClass = (EquivalenceClass)nextState.getEquivalenceClass();
			}
			newEquivalenceClassHolder.addState(currState, nextEquivalenceClass);
		}
		return newEquivalenceClassHolder;
	}

	public void clear()
	{
		states.clear();
	}

	public int size()
	{
		return states.size();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		Iterator stateIt = states.iterator();
		while (stateIt.hasNext())
		{
			State currState = (State)stateIt.next();
			sb.append(" " + currState.getId());
		}
		sb.append("]");
		return sb.toString();
	}
}


/**
 * Temporary help object for storing new equivalence classes.
 * An integer for the next class is used as the key.
 **/
class EquivalenceClassHolder
	extends HashMap
{
	public void addState(State state, EquivalenceClass nextClass)
	{
		// If the next equivalence class does not exist create it
		if (!containsKey(nextClass))
		{
			EquivalenceClass newEquivClass = new EquivalenceClass(nextClass);
			put(nextClass, newEquivClass);
		}

		// Now get the EquivalenceClass associated with the nextEquivClass
		// and add the state to it.
		EquivalenceClass theEquivalenceClass = (EquivalenceClass)get(nextClass);
		theEquivalenceClass.add(state);
	}

	public Iterator iterator()
	{
		return values().iterator();
	}

	public void update()
	{
		Iterator equivClassIt = iterator();
		while (equivClassIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass)equivClassIt.next();
			currEquivClass.update();
		}
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Iterator equivClassIt = iterator();
		sb.append("(");
		while (equivClassIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass)equivClassIt.next();
			sb.append(currEquivClass);
		}
		sb.append(")");
		return sb.toString();
	}
}
