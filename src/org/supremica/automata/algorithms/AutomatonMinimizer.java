
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.log.*;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.StateSet;

// Factory object for generating the correct class according to prefs
class EqClassFactory
{
	static EquivalenceClass getEqClass()
	{
		return new EqClass();
	}
	static EquivalenceClass getEqClass(EquivalenceClass eqc)
	{
		return new EqClass(eqc);
	}
}

public class AutomatonMinimizer
{
	private Automaton theAutomaton;
	private Alphabet theAlphabet;

	private static Logger logger = LoggerFactory.createLogger(AutomatonMinimizer.class);
	
	public AutomatonMinimizer(Automaton theAutomaton)
	{
		this.theAutomaton = theAutomaton;
		this.theAlphabet = theAutomaton.getAlphabet();
	}

	public Automaton getMinimizedAutomaton()
		throws Exception
	{
		EquivalenceClass acceptingStates = EqClassFactory.getEqClass();
		EquivalenceClass forbiddenStates = EqClassFactory.getEqClass();
		EquivalenceClass rejectingStates = EqClassFactory.getEqClass();
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isForbidden())
			{
				currState.setStateClass(forbiddenStates); // setEquivalenceClass(forbiddenStates);
				forbiddenStates.add(currState);
			}
			else if (currState.isAccepting())
			{
				currState.setStateClass(acceptingStates); // setEquivalenceClass(acceptingStates);
				acceptingStates.add(currState);
			}
			else
			{
				currState.setStateClass(rejectingStates); // setEquivalenceClass(rejectingStates);
				rejectingStates.add(currState);
			}
		}

		EquivalenceClasses equivClasses = new EquivalenceClasses();
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

	// This one's not used (a call existed in ActionMan::1145, but is commented out)
	// What is it good for?
	public Automaton getMinimizedAutomaton(boolean sameEquivClassInitially)
		throws Exception
	{
		if (sameEquivClassInitially)
		{
			EquivalenceClasses equivClasses = new EquivalenceClasses();
			EquivalenceClass initialClass = new EquivalenceClass();
			Iterator stateIt = theAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();

				currState.setStateClass(initialClass); // setEquivalenceClass(initialClass);
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
		throws Exception
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
			EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();
			State currState = currEquivClass.getState(newAutomaton);
		}

		// Build all transitions
		equivClassIt = equivClasses.iterator();

		while (equivClassIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();
			State fromState = currEquivClass.getState(newAutomaton);
			Iterator outgoingArcsIt = currEquivClass.outgoingArcsIterator();

			while (outgoingArcsIt.hasNext())
			{
				Arc currArc = (Arc) outgoingArcsIt.next();
				String currEventId = currArc.getEventId();
				State oldToState = currArc.getToState();
				EquivalenceClass nextEquivalenceClass = (EquivalenceClass) oldToState.getStateClass(); // getEquivalenceClass();
				State toState = nextEquivalenceClass.getState(newAutomaton);
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
				EquivalenceClass currClass = (EquivalenceClass) classIt.next();

				refined = doMinimization(equivClasses, currClass) || refined;
			}
		}
		while (refined);
	}

	private boolean doMinimization(EquivalenceClasses equivClasses, EquivalenceClass equivClass)
	{
		boolean refined = false;
		Iterator eventIt = theAlphabet.eventIterator();

		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();

			refined = doMinimization(equivClasses, equivClass, currEvent) || refined;
		}

		return refined;
	}

	private boolean doMinimization(EquivalenceClasses equivClasses, EquivalenceClass equivClass, LabeledEvent e)
	{

		// System.err.println("A iteration in doMinimization <classes, class, event>");
		EquivalenceClassHolder newEquivClassHolder = equivClass.split(e);

		// System.err.println(newEquivClassHolder.size() + " new equivalence classes");
		if (newEquivClassHolder.size() > 1)
		{

			// System.err.println("------------------");
			// System.err.println("equivClasses" + equivClasses);
			// System.err.println("newEquivClasses" + newEquivClassHolder);
			// Remove the current class from all equivalenceClasses
			// System.err.println("Before equivClasses" + equivClasses);
			equivClasses.remove(equivClass);

			// System.err.println("After equivClasses" + equivClasses);
			equivClass.clear();

			// Set the new equivalence class in all states
			// newEquivClassHolder.update();
			// Add the new classes to equivClasses
			equivClasses.addAll(newEquivClassHolder);

			// System.err.println("equivClasses" + equivClasses);
			// System.err.println("newEquivClasses" + newEquivClassHolder);
			// System.err.println("------------------");
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
	
	public static void main(String[] args)
	{
		logger.setLogToConsole(true);

		Automaton automaton = new Automaton("Minimizer Test");
		
		State q0 = new State("q0"); automaton.addState(q0); automaton.setInitialState(q0);
		State q1 = new State("q1"); automaton.addState(q1);
		State q2 = new State("q2"); automaton.addState(q2);
		State q3 = new State("q3"); automaton.addState(q3);
		State q4 = new State("q4"); automaton.addState(q4);
		State q5 = new State("q5"); automaton.addState(q5);
		
		LabeledEvent a = new LabeledEvent("a"); automaton.getAlphabet().addEvent(a, false);
		LabeledEvent b = new LabeledEvent("b"); automaton.getAlphabet().addEvent(b, false);
		LabeledEvent c = new LabeledEvent("c"); automaton.getAlphabet().addEvent(c, false);
		LabeledEvent d = new LabeledEvent("d"); automaton.getAlphabet().addEvent(d, false);

		automaton.addArc(new Arc(q0, q1, a));
		automaton.addArc(new Arc(q1, q1, a));
		automaton.addArc(new Arc(q1, q2, b));
		automaton.addArc(new Arc(q1, q3, c));
		automaton.addArc(new Arc(q2, q4, d));
		automaton.addArc(new Arc(q3, q5, d));	
		
		AutomatonMinimizer minimizer = new AutomatonMinimizer(automaton);
		try
		{
			Automaton minauto = minimizer.getMinimizedAutomaton();
			AutomatonToDsx todsx = new AutomatonToDsx(minauto);
			todsx.serialize(new java.io.PrintWriter(System.out));
		}
		catch(Exception excp)
		{
			logger.error(excp);
			excp.printStackTrace();
			return;
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
			EquivalenceClass currEquivClass = (EquivalenceClass) equivIt.next();

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
		return ((LinkedList) equivClasses.clone()).iterator();
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
			EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();

			sb.append(currEquivClass);
		}

		sb.append(")");

		return sb.toString();
	}
}

class EquivalenceClass
	extends StateSet 	// at the moment, this is only for being able to use get/setStateClaa
						// will StateSet do most of the job correctly?
{
	// private LinkedList states = new LinkedList();
	protected State newState = null;
	private EquivalenceClass nextClass = null;

	public EquivalenceClass() {}

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
		Iterator stateIt = /* states. */ iterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			currState.setStateClass(this); // setEquivalenceClass(this);
		}
	}

/*	public boolean add(State state)
	{
		return states.add(state);
	}

	public boolean remove(State state)
	{
		return states.remove(state);
	}

	public boolean addAll(Collection c)
	{
		return states.addAll(c);
	}

	public Iterator iterator()
	{
		return states.iterator();
	}
*/
	// Returns true if one state in this equivalence class is marked as 'initial'
	public boolean isInitial()
	{
		Iterator stateIt = /* states. */ iterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isInitial())
			{
				return true;
			}
		}

		return false;
	}

	// With regard to accepting, all states have the same designation
	// Thus, we need only check the first state
	public boolean isAccepting()
	{
		State currState = get(); // (State) states.getFirst();

		if (currState.isAccepting())
		{
			return true;
		}

		return false;
	}

	// With regard to forbidden, all states have the same designation
	// Thus, we need only check the first state
	public boolean isForbidden()
	{
		State currState = get(); // (State) states.getFirst();

		if (currState.isForbidden())
		{
			return true;
		}

		return false;
	}

	/**
	 * Set the state that corresponds to this equivalence class.
	 * This is used while building the minimized automaton.
	 *
	 *@param  state The new state value
	 */
	private void setState(State state)
	{
		newState = state;
	}

	public State getState(Automaton theAutomaton)
	{
		if(newState == null)
		{
			createNewState(theAutomaton, "q");
		}
		return newState;
	}

	// Creates and adds a uniquely named state
	protected void createNewState(Automaton theAutomaton, String prefix)
	{
		newState = theAutomaton.createAndAddUniqueState(prefix);

		if (isInitial())
		{
			theAutomaton.setInitialState(newState);
		}

		if (isForbidden())
		{
			newState.setForbidden(true);
		}
		else if (isAccepting())
		{
			newState.setAccepting(true);
		}
	}

	public Iterator outgoingArcsIterator()
	{
		State currState = get(); // (State) states.getFirst();
		Iterator currIt = currState.outgoingArcsIterator();

		return currIt;
	}

	/**
	 * Split the equivalence class according to what can be reached on this event
	 * Return all new equivalence classes in the holder
	 * If the holder contains only one entry, all reached states have the same eq class(?)
	 */
	public EquivalenceClassHolder split(LabeledEvent e)
	{

		// System.err.println("Splitting " + e.getLabel());
		EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();

		// Build a list of equivalance classes that e transfers to from each of the states in this eq-class
		// Note, for each state there is only one successor state for this event (determinism)
		Iterator stateIt = iterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();
			State nextState = currState.nextState(e);
			EquivalenceClass nextEquivalenceClass = null;

			if (nextState != null)
			{
				nextEquivalenceClass = (EquivalenceClass) nextState.getStateClass(); // getEquivalenceClass();
			}

			newEquivalenceClassHolder.addState(currState, nextEquivalenceClass);
		}

		return newEquivalenceClassHolder;
	}
/*
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
			State currState = (State) stateIt.next();

			sb.append(" " + currState.getId());
		}

		sb.append("]");

		return sb.toString();
	}*/
}

/**
 * Fabians play-around version of EquivalencClass
 * This class generates itself the state that it corresponds to
 * It sets the state-name as the concatenation of the original state names
 * This has the effect that it keeps the same name for singleton equivalence classes
 */
class EqClass
	extends EquivalenceClass
{

	public EqClass()
	{
		super();
	}
	public EqClass(EquivalenceClass ec)
	{
		super(ec);
	}

	public State getState(Automaton theAutomaton)
	{
		// State newState = super.getState();
		if(newState == null)
		{
			// create a new state named as the concatenation of all state-names
			StringBuffer str = new StringBuffer();
			Iterator it = iterator();
			while(it.hasNext())
			{
				str.append(((State)it.next()).getName());
			}
			createNewState(theAutomaton, str.toString());

		}
		return newState;
	}

	// Add a state to this equivalence class
	// Should the eq-class also add itself to the state?
	public boolean add(State state)
	{
		// System.out.println("EqClass(" + name +")::addState(" + state.getName() + ")");
		return super.add(state);
	}
}

/**
 * Temporary help object for storing new equivalence classes.
 *
 *
 *@author  ka
 *@created  November 28, 2001
 */
class EquivalenceClassHolder
	extends HashMap
{
	public void addState(State state, EquivalenceClass nextClass)
	{

		// If the next equivalence class does not exist create it
		if (!containsKey(nextClass))
		{
			EquivalenceClass newEquivClass = EqClassFactory.getEqClass(nextClass);

			put(nextClass, newEquivClass);
		}

		// Now get the EquivalenceClass associated with the nextEquivClass
		// and add the state to it.
		EquivalenceClass theEquivalenceClass = (EquivalenceClass) get(nextClass);

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
			EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();

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
			EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();

			sb.append(currEquivClass);
		}

		sb.append(")");

		return sb.toString();
	}
}
