
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
 *going
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.standard.Determinizer;
import org.supremica.properties.SupremicaProperties;

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
	private static Logger logger = LoggerFactory.createLogger(AutomatonMinimizer.class);

	/** The "original" automaton (possible saturated). */
	private Automaton theAutomaton;
	private MinimizationOptions options;

	public AutomatonMinimizer(Automaton theAutomaton)
	{
		this.theAutomaton = theAutomaton;
	}

	/**
	 * Returns minimized automaton, minimized with respect to the supplied equivalence relation
	 */
	public Automaton getMinimizedAutomaton(MinimizationOptions options)
		throws Exception
	{
		this.options = options;

		// If we're about to modify theAutomaton, make a copy first!!
		if (options.getMinimizationType() == EquivalenceRelation.ObservationEquivalence)
		{
			theAutomaton = new Automaton(theAutomaton);
		}
		
		// Find the initial sets of states for the minimization
		EquivalenceClasses equivClasses = new EquivalenceClasses();
		
		// Divide the state space into three initial equivalence classes, based on markings
		EquivalenceClass acceptingStates = EqClassFactory.getEqClass();
		EquivalenceClass forbiddenStates = EqClassFactory.getEqClass();
		EquivalenceClass rejectingStates = EqClassFactory.getEqClass();
		
		// Examine each state for which class it fits into
		StateIterator stateIt = theAutomaton.stateIterator();
		while (stateIt.hasNext())
		{
			State currState = stateIt.nextState();
			
			if (currState.isForbidden())
			{
				currState.setStateSet(forbiddenStates);
				forbiddenStates.add(currState);
			}
			else if (currState.isAccepting() && !options.getIgnoreMarking())
			{
				currState.setStateSet(acceptingStates);
					acceptingStates.add(currState);
			}
			else
			{
				currState.setStateSet(rejectingStates);
				rejectingStates.add(currState);
			}
		}
			
		// Put these new classes into a single object
		// Only if there are any states in the class...
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

		// Do the minimization, starting from these classes
		EquivalenceRelation equivalenceRelation = options.getMinimizationType();
		try
		{
			if (equivalenceRelation == EquivalenceRelation.LanguageEquivalence)
			{
				// Minimize
				doLanguageEquivalenceMinimization(equivClasses);
			}
			else if (equivalenceRelation == EquivalenceRelation.ObservationEquivalence)
			{
				// Make copy, we're going to modify things...
				doTransitiveClosure(theAutomaton);
				equivClasses.update();

				// Now... language equivalence minimization will yield the (considering states)
				// minimal observation equivalent automaton
				doLanguageEquivalenceMinimization(equivClasses);
			}
			else
			{
				throw new Exception("Unknown equivalence relation");
			}
		}
		catch (Exception ex)
		{
			logger.debug(ex.getStackTrace());

			throw ex;
		}

		Automaton newAutomaton = buildAutomaton(equivClasses);

		// Should we remove redundant transitions to minimize also with respect to transitions?
		if (options.getAlsoTransitions())
		{
			removeRedundantTransitions(newAutomaton);
		}

		// Return the result of the minimization!
		return newAutomaton;
	}

	/**
	 * Returns the minimized automaton
	 */
	private Automaton buildAutomaton(EquivalenceClasses equivClasses)
		throws Exception
	{
		Automaton newAutomaton = new Automaton();

		newAutomaton.setType(theAutomaton.getType());
		newAutomaton.getAlphabet().union(theAutomaton.getAlphabet()); // Odd... it works, of course, but why like this?

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

				LabeledEvent currEvent = currArc.getEvent();
				State oldToState = currArc.getToState();
				EquivalenceClass nextEquivalenceClass = (EquivalenceClass) oldToState.getStateSet(); 
				State toState = nextEquivalenceClass.getState(newAutomaton);
				
				Arc newArc = new Arc(fromState, toState, currEvent);
				
				// If we should minimize the number of transitions, make sure a transition is never
				// present more than once (this is performed in an ugly way below)
				if (!(options.getAlsoTransitions() && newAutomaton.containsArc(newArc)))
				{
					// Add arc
					newAutomaton.addArc(newArc);
				}
			}
		}

		// Give the automaton an appropriate comment
		newAutomaton.setComment("min(" + theAutomaton.getName() + ")");

		// Return the new automaton!
		return newAutomaton;
	}

	/**
	 * Splits the equivalence classes with respect to language equivalence, based
	 * on this, an automaton with a minimal number of state can be generated from
	 * the equivalence classes!
	 */
	private void doLanguageEquivalenceMinimization(EquivalenceClasses equivClasses)
	{
		boolean refined;

		do
		{
			refined = false;

			Iterator classIt = equivClasses.safeIterator();

			while (classIt.hasNext())
			{
				EquivalenceClass currClass = (EquivalenceClass) classIt.next();

				refined = doLanguageEquivalenceMinimization(equivClasses, currClass) || refined;
			}
		}
		while (refined);
	}
	private boolean doLanguageEquivalenceMinimization(EquivalenceClasses equivClasses, EquivalenceClass equivClass)
	{
		boolean refined = false;

		for (Iterator eventIt = theAutomaton.getAlphabet().iterator();
				eventIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();

			refined = doLanguageEquivalenceMinimization(equivClasses, equivClass, currEvent) || refined;
		}

		return refined;
	}
	private boolean doLanguageEquivalenceMinimization(EquivalenceClasses equivClasses, EquivalenceClass equivClass, LabeledEvent e)
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

	/**
	 * Add transitions to cover for the epsilon events. More formally, each time there is a
	 * transition "p =a=> q", after completing the transitive closure (or "saturation"), 
	 * there is also a transition "p -a-> q".
	 */
	public void doTransitiveClosure(Automaton aut)
	{
		// For calculating the epsilon closure, we need this
		Determinizer determinizer = new Determinizer(aut);
		
		// Find epsilon closure for each state, put this info in each state
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			State currState = stateIt.nextState();

			// Find closure, associate it with this state
			currState.setStateSet(null); // This is unfortunately necessary!!
			StateSet closure = determinizer.epsilonClosure(currState);
			currState.setStateSet(closure);
		}

		// From each state add transitions that are present in its closure
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			State currState = stateIt.nextState();
			StateSet closure = currState.getStateSet();

			// Iterate over outgoing arcs in the closure
			for (StateIterator closureIt = closure.iterator(); closureIt.hasNext(); )
			{
				for (ArcIterator arcIt = closureIt.nextState().safeOutgoingArcsIterator(); arcIt.hasNext(); )
				{
					Arc arc = arcIt.nextArc();
					
					// Where may we end up if we move along this transition? 
					// Anywhere in the epsilon closure of the toState...
					StateSet toClosure = arc.getToState().getStateSet();					
					for (StateIterator toIt = toClosure.iterator(); toIt.hasNext(); )
					{
						State toState = toIt.nextState();
					   
						// Don't add already existing transitions
						if (!(currState.equals(arc.getFromState()) && toState.equals(arc.getToState())))
						{
							Arc newArc = new Arc(currState, toState, arc.getEvent());
							aut.addArc(newArc);
						}
					}
				}
			}
			
			// Add silent self-loop
			LabeledEvent tau = new LabeledEvent("tau");
			tau.setEpsilon(true);
			if (!aut.getAlphabet().contains(tau))
			{
				aut.getAlphabet().addEvent(tau);
			}
			aut.addArc(new Arc(currState, currState, tau));
		}
	} 

	/**
	 * Algorithm inspired by "Minimizing the Number of Transitions with Respect to Observation Equivalence"
	 * by Jaana Eloranta. 
	 * Removes all transitions that are redundant.
	 */
	public void removeRedundantTransitions(Automaton aut)
	{
		// Are there any silent-self-loops? Remove them, they are redundant! 
		// Note! This is not Jaana Elorantas definition of redundant transitions! 
		// Her "redundant transitions" are removed below (which requires that all 
		// silent self-loops have already been removed).
		boolean hasSilentSelfloop = false;
		// Put them in a list, remove afterwards
		LinkedList toBeRemoved = new LinkedList();
		for (ArcIterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();
			
			if (currArc.isSelfLoop() && currArc.getEvent().isEpsilon())
			{
				toBeRemoved.add(currArc);
			}
		}
		while (toBeRemoved.size() > 0)
		{
			aut.removeArc((Arc) toBeRemoved.remove(0));
		}
		
		// Put redundant arcs in set, remove after all have been found
		toBeRemoved.clear();
		loop: for (ArcIterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();
			
			// Using Elorantas notation... (s1, s2 and s3)
			State s1 = arc.getFromState();
			State s2 = arc.getToState();

			// Is the criteria fulfilled? (does there exist a s3 such that either
			// (s1 -a-> a3 and s3 -tau-> s2) or (s1 -tau-> s3 and s3 -a-> s2) holds?)
			test: for (ArcIterator outIt = s1.outgoingArcsIterator(); outIt.hasNext(); )
			{
				Arc firstArc = outIt.nextArc();
				LabeledEvent firstEvent = firstArc.getEvent();
				
				if (firstEvent.isEpsilon() || firstEvent.equals(arc.getEvent()))
				{
					State s3 = firstArc.getToState();

					for (ArcIterator inIt = s2.incomingArcsIterator(); inIt.hasNext(); )
					{
						Arc secondArc = inIt.nextArc();
						if (s3.equals(secondArc.getFromState()))
						{
							LabeledEvent secondEvent = secondArc.getEvent();
							// The order (wrt ||) in this if-clause is important!!
							if ((secondEvent.isEpsilon() && (!firstEvent.isEpsilon() || arc.getEvent().isEpsilon())) || (secondEvent.equals(arc.getEvent()) && !firstEvent.equals(secondEvent)))
							{
								// Redundant!!!!
								toBeRemoved.add(arc);
								break test;
							}
						}
					}
				}
			}
		}	   
		while (toBeRemoved.size() > 0)
		{
			aut.removeArc((Arc) toBeRemoved.remove(0));
		}
	}

	/**
	 * Splits equivClasses with respect to observation equivalence.
	 */
	private void doObservationEquivalenceMinimization(EquivalenceClasses equivClasses)
	{
		/*
		// The same notation as in "An Implementation of an Efficient 
		// Algorithm for Bisimulation Equivalence". 
		EquivalenceClasses W = (EquivalenceClasses) equivClasses.clone();
		EquivalenceClasses q = equivClasses;
		
		// The alphabet, used several times below
		Alphabet A = theAutomaton.getAlphabet();
		
		// The main loop
		while (W.size() != 0)
		{
		    // Get and remove a StateSet from W
		    StateSet splitter = W.getFirst();
		    W.remove((EquivalenceClass) splitter);
		
			// Loop over the "actions"
			for (EventIterator evIt = A.iterator(); evIt.hasNext();)
			{
				LabeledEvent a = evIt.nextEvent();
			}
		}
		*/
	}

	public static void main(String[] args)
	{
		logger.setLogToConsole(true);

		Automaton automaton = new Automaton("Minimizer Test");
		State q0 = new State("q0");

		automaton.addState(q0);
		automaton.setInitialState(q0);

		State q1 = new State("q1");

		automaton.addState(q1);

		State q2 = new State("q2");

		automaton.addState(q2);

		State q3 = new State("q3");

		automaton.addState(q3);

		State q4 = new State("q4");

		automaton.addState(q4);

		State q5 = new State("q5");

		automaton.addState(q5);

		LabeledEvent a = new LabeledEvent("a");

		automaton.getAlphabet().addEvent(a);

		LabeledEvent b = new LabeledEvent("b");

		automaton.getAlphabet().addEvent(b);

		LabeledEvent c = new LabeledEvent("c");

		automaton.getAlphabet().addEvent(c);

		LabeledEvent d = new LabeledEvent("d");

		automaton.getAlphabet().addEvent(d);
		automaton.addArc(new Arc(q0, q1, a));
		automaton.addArc(new Arc(q1, q1, a));
		automaton.addArc(new Arc(q1, q2, b));
		automaton.addArc(new Arc(q1, q3, c));
		automaton.addArc(new Arc(q2, q4, d));
		automaton.addArc(new Arc(q3, q5, d));

		AutomatonMinimizer minimizer = new AutomatonMinimizer(automaton);

		try
		{
			MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
			Automaton minauto = minimizer.getMinimizedAutomaton(options);
			org.supremica.automata.IO.AutomatonToDsx todsx = new org.supremica.automata.IO.AutomatonToDsx(minauto);

			todsx.serialize(new java.io.PrintWriter(System.out));
		}
		catch (Exception excp)
		{
			logger.error(excp);
			logger.debug(excp.getStackTrace());

			// excp.printStackTrace();
			return;
		}
	}
}

class EquivalenceClasses
	extends StateSets
{
	public void addAll(EquivalenceClassHolder equivClassHolder)
	{
		Iterator equivIt = equivClassHolder.iterator();

		while (equivIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass) equivIt.next();

			currEquivClass.update();
			super.add(currEquivClass);
		}
	}

	/*
	private LinkedList equivClasses = new LinkedList();

	public void add(EquivalenceClass equivClass)
	{
		equivClasses.add(equivClass);
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
	*/
}

// at the moment, this is only for being able to use get/setStateClaa
// will StateSet do most of the job correctly?

// I've extended StateSet with most of these methods, but I didn't have the guts for and neither did I feel
// for changing the above to use StateSet (and StateSets) instead...  /hguo
class EquivalenceClass
	extends StateSet
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

			currState.setStateSet(this);    // setEquivalenceClass(this);
		}
	}

/*      public boolean add(State state)
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
		State currState = get();    // (State) states.getFirst();

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
		State currState = get();    // (State) states.getFirst();

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
		if (newState == null)
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
		State currState = get();    // (State) states.getFirst();
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
				nextEquivalenceClass = (EquivalenceClass) nextState.getStateSet();    // getEquivalenceClass();
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
		if (newState == null)
		{
			// create a new state named as the concatenation of all state-names
			StringBuffer str = new StringBuffer();
			Iterator it = iterator();

			while (it.hasNext())
			{
				str.append(((State) it.next()).getName() + SupremicaProperties.getStateSeparator());
			}
			str.setLength(str.length() - SupremicaProperties.getStateSeparator().length());    

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
