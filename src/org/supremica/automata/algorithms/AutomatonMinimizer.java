
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
import org.supremica.automata.*;
import org.supremica.automata.algorithms.standard.Determinizer;
import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.ActionMan;
import java.awt.Toolkit;

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
	implements Stoppable
{
	private static Logger logger = LoggerFactory.createLogger(AutomatonMinimizer.class);

	// Stoppable stuff
	private boolean stopRequested = false;

	/** The automaton being minimized (may be a copy of the original). */
	private Automaton theAutomaton;

	/** The supplied options. */
	private MinimizationOptions options;

	/**
	 * Basic constructor.
	 */
	public AutomatonMinimizer(Automaton theAutomaton)
	{
		this.theAutomaton = theAutomaton;
	}

	/**
	 * Returns minimized automaton, minimized with respect to the supplied options.
	 */
	public Automaton getMinimizedAutomaton(MinimizationOptions options)
		throws Exception
	{
		this.options = options;
		
		// Are the options valid?
		if (!options.isValid())
		{
			return null;
		}

		// Do we have to care about the original?
		if (options.getKeepOriginal())
		{
			// We need a copy since we must make the system reachable (and maybe deterministic) first!
			theAutomaton = new Automaton(theAutomaton);
		}

		// Make reachable
		AutomatonSynthesizer synth = new AutomatonSynthesizer(theAutomaton, SynthesizerOptions.getDefaultSynthesizerOptions());
		synth.doReachable();
		for (Iterator it = theAutomaton.safeStateIterator(); it.hasNext(); )
		{
			State state = (State) it.next();
			if ((state.getCost() == State.MAX_COST) && !state.isForbidden())
			{
				theAutomaton.removeState(state);
			}
		}

		// The current partitioning
		EquivalenceClasses equivClasses = new EquivalenceClasses();

		// Find out what to do
		EquivalenceRelation equivalenceRelation = options.getMinimizationType();
		try
		{
			if (equivalenceRelation == EquivalenceRelation.LanguageEquivalence)
			{
				// Is this automaton nondeterministic?
				if (!theAutomaton.isDeterministic())
				{
					// Make deterministic
					Determinizer determinizer = new Determinizer(theAutomaton);
					determinizer.execute();
					theAutomaton = determinizer.getNewAutomaton();
				}

				// Now we're ready for minimization!
			}
			else if (equivalenceRelation == EquivalenceRelation.ObservationEquivalence)
			{
				// Merge silent loops and other obvious OE stuff (to save computation later)
				int count = preMinimizationMergeObservationEquivalentStates(theAutomaton); 
				if (count > 0)
				{
					logger.debug("Removed " + count + " observation equivalent states " + 
								"before running the minimization.");
				}

				// Add automaton to gui (for debugging purposes! This should not be the standard procedure!!)
				//ActionMan.getGui().addAutomaton(new Automaton(theAutomaton));
				
				// Saturate
				doTransitiveClosure(theAutomaton);
				
				// Now... language equivalence minimization will yield the (considering states)
				// minimal observation equivalent automaton
			}
			else if (equivalenceRelation == EquivalenceRelation.ConflictEquivalence)
			{
				// Merge silent loops and other obvious OE stuff (to save computation later)
				int count = preMinimizationMergeObservationEquivalentStates(theAutomaton); 
				if (count > 0)
				{
					logger.debug("Removed " + count + " observation equivalent states " + 
								"before running minimization.");
				}
					
				// Add automaton to gui (for debugging purposes! This should not be the standard procedure!!)
				//ActionMan.getGui().addAutomaton(new Automaton(theAutomaton));

				// Saturate
				doTransitiveClosure(theAutomaton);

				// Adjust marking based on epsilon transitions (it IS ok (actually necessary) 
				// to do this AFTER doTransitiveClosure) this is not an expensive computation
				adjustMarking(theAutomaton); // Not OE
								
				// Now... language equivalence minimization will yield a smaller (considering states)
				// conflict equivalent automaton
			}
			else
			{
				throw new Exception("Unknown equivalence relation");
			}

			// After the above preparations, we can do the minimization in the same way for all cases!

			// Find initial partitioning
			equivClasses = findInitialPartitioning(theAutomaton);
			
			if (stopRequested)
			{
				return null;
			}
			
			// Minimize
			doLanguageEquivalenceMinimization(equivClasses);
		}
		catch (Exception ex)
		{
			logger.debug(ex.getStackTrace());

			throw ex;
		}

		// Build the minimized automaton
		Automaton newAutomaton = buildAutomaton(equivClasses);

		if (stopRequested)
		{
			return null;
		}

		// Should we remove redundant transitions to minimize also with respect to transitions?
		if (options.getAlsoTransitions())
		{
			removeRedundantTransitions(newAutomaton);
		}

		// Post minimization adjustments
		if (equivalenceRelation == EquivalenceRelation.ObservationEquivalence)
		{
			// The minimization sometimes misses stuff that is easy to see (for the trained eye...)
			// (BUG!)
			int count = preMinimizationMergeObservationEquivalentStates(newAutomaton);
			if (count > 0)
			{
				logger.debug("Removed " + count + " observation equivalent states " + 
							"after running the minimization (it didn't work properly).");
			}
		}
		else if (equivalenceRelation == EquivalenceRelation.ConflictEquivalence)
		{
			// Merge conflict equivalent states and stuff...
			// (FEATURE!)
			int count = postMinimizationMergeConflictEquivalentStates(newAutomaton); // Not OE
			if (count > 0)
			{
				logger.debug("Removed " + count + " conflict equivalent states after " + 
							"running observation equivalence minimization.");
			}
		}

		// Remove from alphabet epsilon events that are never used
		removeUnusedEpsilonEvents(newAutomaton);

		// Return the result of the minimization!
		return newAutomaton;
	}

	/**
	 * Find the initial partitioning of this automaton, based on the marking and forbidden
	 * states (marking can be ignored using the minimization option ignoreMarking.
	 */
	private EquivalenceClasses findInitialPartitioning(Automaton aut)
	{
		// Find the initial partitioning for the minimization
		EquivalenceClasses equivClasses = new EquivalenceClasses();

		// Divide the state space into three initial equivalence classes, based on markings
		EquivalenceClass acceptingStates = EqClassFactory.getEqClass();
		EquivalenceClass forbiddenStates = EqClassFactory.getEqClass();
		EquivalenceClass rejectingStates = EqClassFactory.getEqClass();

		// Examine each state for which class it fits into
		StateIterator stateIt = aut.stateIterator();
		while (stateIt.hasNext())
		{
			State currState = stateIt.nextState();

			if (currState.isForbidden() && !options.getIgnoreMarking())
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

		return equivClasses;
    }


	/**
	 * Returns the minimized automaton, based on the partitioning in equivClasses.
	 */
	private Automaton buildAutomaton(EquivalenceClasses equivClasses)
		throws Exception
	{
		Automaton newAutomaton = new Automaton();

		newAutomaton.setType(theAutomaton.getType());
		newAutomaton.getAlphabet().union(theAutomaton.getAlphabet()); // Odd... but it works. Why like this?

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
			if (stopRequested)
			{
				return null;
			}

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
				if (!(options.getAlsoTransitions() && newArc.getFromState().containsOutgoingArc(newArc)))
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
	 * Merges states, preserving conflict equivalence.
	 *
	 * @return Number of states that have been removed by merging.
	 */
	public int postMinimizationMergeConflictEquivalentStates(Automaton aut)
		throws Exception
	{
		StateSet statesToExamine = new StateSet(aut.getStateSet());

		// Count the removed states
		int count = 0;

		// Mark noncoreachable states with State.MAX_COST
		AutomatonSynthesizer synth = new AutomatonSynthesizer(aut, SynthesizerOptions.getDefaultSynthesizerOptions());
		synth.doCoreachable();
		for (Iterator it = aut.stateIterator(); it.hasNext(); )
		{
			State state = (State) it.next();
			if ((state.getCost() == State.MAX_COST))
			{
				if (state.isAccepting())
					logger.fatal("EEEEEEERRRRRRRRRRRROOOOOOOOOOOORRRRRRRRRRRRRRRRRRRR!");
				
				// We will never want to propagate from here... 
				// ... or from any state in the backwards epsilon closure!!
				StateSet statesToModify = new StateSet();
				statesToModify.add(state);
				while (statesToModify.size() != 0)
				{
					State currState = statesToModify.get();
					statesToModify.remove(currState);

					// Accepting states that by epsilons may reach a block are modified to be nonaccepting
					// an exception is made if this is the initial state!
					if (currState.isAccepting())
					{
						if (currState.isInitial())
						{
							// Do nothing...
							continue;
						}
						else
						{
							currState.setAccepting(false);
						}
					}

					// We won't want to propagate from here
					currState.removeOutgoingArcs();	
					//logger.info("I removed arcs from " + currState);
					
					// Follow all epsilon transitions backwards
					for (ArcIterator arcIt = currState.incomingArcsIterator(); arcIt.hasNext(); )
					{
						Arc arc = arcIt.nextArc();
						if (arc.getEvent().isEpsilon())
						{
							statesToModify.add(arc.getFromState());
						}
					}
				}
				
				// Reset cost...
				if (!state.isForbidden())
				{
					state.setCost(State.UNDEF_COST);
				}
			}
		}
		// Make reachable
		synth.doReachable();
		for (Iterator it = aut.safeStateIterator(); it.hasNext(); )
		{
			State state = (State) it.next();
			if ((state.getCost() == State.MAX_COST) && !state.isForbidden())
			{
				aut.removeState(state);
			}
		}

		// Do the merging
		while (statesToExamine.size() != 0)
		{
			// Get and remove arbitrary state
			State one = statesToExamine.get();
			statesToExamine.remove(one);

			// If there is only one incoming transition to this state AND it is an epsilon-transition
			// AND there is at least one outgoing epsilon transition
			// merge this and previous state.
			if (one.nbrOfIncomingArcs() == 1)
			{
				Arc arc = one.incomingArcsIterator().nextArc();
				State two = arc.getFromState();

				if ((!one.equals(two)) && arc.getEvent().isEpsilon())
				{
					// Is there at least one epsilon outgoing?
					boolean ok = false;
					for (ArcIterator outIt = one.outgoingArcsIterator(); outIt.hasNext(); )
					{
						if (outIt.nextArc().getEvent().isEpsilon())
						{
							ok = true;
							break;
						}
					}
					
					// OK to merge?
					if (ok)
					{
						count++;
						aut.removeArc(arc); // We can remove this one, it will be an epsilon self-loop!
						statesToExamine.remove(two);
						State mergeState = aut.mergeStates(two, one);
						statesToExamine.add(mergeState);
						// Add states that may have changed to stack
						for (ArcIterator arcIt = mergeState.outgoingArcsIterator(); arcIt.hasNext(); )
						{
							Arc epsilonArc = arcIt.nextArc();
							if (epsilonArc.getEvent().isEpsilon())
							{
								statesToExamine.add(epsilonArc.getToState());
							}
						}
						//logger.info("I merged " + one + " and " + two + " since I thought they were CE");
						continue; // Get next "one" from stack
					}
				}
			}
		}		

		return count;
	}

	/**
	 * Merges all states in loops of silent transitions and "single-outgoing-epsilon-transition-states".
	 *
	 * @return Number of states that have been removed by merging.
	 */
	public int preMinimizationMergeObservationEquivalentStates(Automaton aut)
	{
		StateSet statesToExamine = new StateSet(aut.getStateSet());

		// Count the removed states
		int count = 0;

		// Do the merging
		while (statesToExamine.size() != 0)
		{
			if (stopRequested)
			{
				return 0;
			}

			// Get and remove arbitrary state
			State one = statesToExamine.get();
			statesToExamine.remove(one);

			// If there is only one transition out of this state AND it is an epsilon-transition
			// merge this and next state.
			if (one.nbrOfOutgoingArcs() == 1)
			{
				Arc arc = one.outgoingArcsIterator().nextArc();
				State two = arc.getToState();

				// If 1!=2 etc.
				boolean markingOK = options.getIgnoreMarking() || one.hasEqualMarking(two);
				if (!one.equals(two) && arc.getEvent().isEpsilon() && markingOK)
				{
					count++;
					aut.removeArc(arc); // We can remove this one, it will be an epsilon self-loop!
					statesToExamine.remove(two);
					//statesToExamine.add(aut.mergeStates(one, two));
					State mergeState = aut.mergeStates(one, two);
					statesToExamine.add(mergeState);
					// Add states that may have changed to stack 
					for (ArcIterator arcIt = mergeState.incomingArcsIterator(); arcIt.hasNext(); )
					{
						Arc epsilonArc = arcIt.nextArc();
						if (epsilonArc.getEvent().isEpsilon())
						{
							statesToExamine.add(epsilonArc.getFromState());
						}
					}
					//logger.info("I merged " + one + " and " + two + " since I thought they were OE.");
					continue; // Get next "one" from stack
				}
			}

			// Merge loops

			// Find epsilon-closure for this state
			one.setStateSet(null); // This is unfortunately necessary!!
			Determinizer determinizer = new Determinizer(aut);
			StateSet closureOne = determinizer.epsilonClosure(one);
			closureOne.remove(one); // Don't examine

			// Find, in closure, if there is a state which has the first state in its closure
			for (StateIterator closureIt = closureOne.iterator(); closureIt.hasNext(); )
			{
				State two = closureIt.nextState();
				two.setStateSet(null);
				StateSet closureTwo = determinizer.epsilonClosure(two);

				// Good to merge?
				boolean markingOK = options.getIgnoreMarking() || one.hasEqualMarking(two);
				//if (closureTwo.contains(one) && markingOK)
				if (closureTwo.contains(one))
				{
					count++;
					
					// Remove the other state from stack, merge and add
					// new state to stack
					statesToExamine.remove(two);
					statesToExamine.add(aut.mergeStates(one, two));
					break; // Get next "one" from stack
				}
			}
			/* // Alternative implementation, appears to suck
			// Find, in closure, if there is a state which has the first state in its closure
			State mergeState = null;
			for (StateIterator closureIt = closureOne.iterator(); closureIt.hasNext(); )
			{
				State two = closureIt.nextState();
				two.setStateSet(null);
				StateSet closureTwo = determinizer.epsilonClosure(two);

				// Good to merge?
				boolean markingOK = !options.ignoreMarking() || one.hasEqualMarking(two);
				if (closureTwo.contains(one) && markingOK)
				{
					count++;

					// Remove the other state from stack, merge 
					statesToExamine.remove(two);
					if (mergeState == null)
					{
						mergeState = aut.mergeStates(one, two);
					}
					else
					{
						mergeState = aut.mergeStates(mergeState, two);
					}
				}
			}
			if (mergeState != null)
			{
				statesToExamine.add(mergeState);
				continue;
			}
			*/
		}

		return count;
	}

	/**
	 * Add transitions to cover for the epsilon events. More formally, each time there is a
	 * transition "p =a=> q", after completing the transitive closure (or "saturation"),
	 * there is also a transition "p -a-> q".
	 */
	public void doTransitiveClosure(Automaton aut)
	{
		// For calculating the epsilon-closure, we need this
		Determinizer determinizer = new Determinizer(aut);

		// Find epsilon-closure for each state, put this info in each state
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			if (stopRequested) 
			{
				return;
			}

			State currState = stateIt.nextState();

			// Find closure, associate it with this state
			currState.setStateSet(null); // This is unfortunately necessary!!
			StateSet closure = determinizer.epsilonClosure(currState);
			currState.setStateSet(closure);
		}

		// From each state add transitions that are present in its closure
		LinkedList toBeAdded = new LinkedList();
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			if (stopRequested) 
			{
				return;
			}

			State currState = stateIt.nextState();
			StateSet closure = currState.getStateSet();

			// Iterate over outgoing arcs in the closure
			for (StateIterator closureIt = closure.iterator(); closureIt.hasNext(); )
			{
				for (ArcIterator arcIt = closureIt.nextState().outgoingArcsIterator(); arcIt.hasNext(); )
				{
					Arc arc = arcIt.nextArc();

					// We can safely ignore the closure, we'll get there, don't worry
					if (arc.getEvent().isEpsilon())
					{
						// Don't add self-loops of epsilons (we will do that later in each state)
						if (!currState.equals(arc.getToState()))
						{
							toBeAdded.add(new Arc(currState, arc.getToState(), arc.getEvent()));
						}

						// Ignore the below loop
						continue;
					}

					// Where may we end up if we move along this transition?
					// Anywhere in the epsilon-closure of the toState...
					StateSet toClosure = arc.getToState().getStateSet();
					for (StateIterator toIt = toClosure.iterator(); toIt.hasNext(); )
					{
						State toState = toIt.nextState();

						// Don't add already existing transitions
						if (!(currState.equals(arc.getFromState()) && toState.equals(arc.getToState())))
						{
							toBeAdded.add(new Arc(currState, toState, arc.getEvent()));
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
			toBeAdded.add(new Arc(currState, currState, tau));
		}
		// Add the new
		logger.debug("Added " + toBeAdded.size() + " transitions to " + aut + ".");
		while (toBeAdded.size() != 0)
		{
			// Add if not already there
			Arc arc = (Arc) toBeAdded.remove(0);
			if (!arc.getFromState().containsOutgoingArc(arc))
			{
				aut.addArc(arc);
			}
		}
	}

	/**
	 * All states could as well be marked in an epsilon-loop where at least one state is marked.
	 * This method adjusts this.
	 *   States that can reach marked states by epsilon events are also considered marked
	 *
	 *   NOTE: This method assumes that the epsilon-closure of each state is already calculated
	 * and that the closure is returned by each state's getStateSet-method. (This is true if
	 * doTransitiveClosure was called previously.)
	 */
	public void adjustMarking(Automaton aut)
	{
		LinkedList toBeMarked = new LinkedList();
		/*
		// States in epsilon loops where there are at least one marked state can be considered marked
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			State markedState = stateIt.nextState();
			if (markedState.isAccepting())
			{
				// Is markedState in the closure of a state in its closure?
				// Then that state should be marked.
				StateSet closure = markedState.getStateSet();
				for (StateIterator closureIt = closure.iterator(); closureIt.hasNext(); )
				{
					State otherState = closureIt.nextState();
					if (!otherState.isAccepting())
					{
						StateSet otherClosure = otherState.getStateSet();
						if (otherClosure.contains(markedState))
						{
							toBeMarked.add(otherState);
						}
					}
				}
			}
		}
		*/

		// States that can reach marked states by epsilon events only can be considered marked
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			State currState = stateIt.nextState();
			if (!currState.isAccepting() && !currState.isInitial())
			{
				StateSet closure = currState.getStateSet();
				for (StateIterator closureIt = closure.iterator(); closureIt.hasNext(); )
				{
					State otherState = closureIt.nextState();
					if (otherState.isAccepting())
					{
						toBeMarked.add(currState);
					}
				}
			}
		}

		// Adjust the marking!
		while (toBeMarked.size() != 0)
		{
			((State) toBeMarked.remove(0)).setAccepting(true);
		}
	}

	/**
	 * Algorithm inspired by "Minimizing the Number of Transitions with Respect to Observation Equivalence"
	 * by Jaana Eloranta. Removes all transitions that are redundant.
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

			// Using Elorantas notation... (s1, s2 and (later) s3)
			State s1 = arc.getFromState();
			State s2 = arc.getToState();

			// Is the criteria fulfilled? (I.e. does there exist a s3 such that either
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
	 * Removes epsilon events that are never used from alphabet.
	 */
	public void removeUnusedEpsilonEvents(Automaton aut)
	{
		Alphabet alpha = aut.getAlphabet();

		// Put them in a list, remove afterwards
		LinkedList toBeRemoved = new LinkedList();
		loop: for (EventIterator evIt = alpha.iterator(); evIt.hasNext(); )
		{
			LabeledEvent event = evIt.nextEvent();

			// Epsilon?
			if (event.isEpsilon())
			{
				// Does there exist a transition like this?
				for (ArcIterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
				{
					// Is this transition of the right event? If so continue the outer loop.
					if (event.equals(arcIt.nextArc().getEvent()))
					{
						continue loop;
					}
				}
				toBeRemoved.add(event);
			}
		}
		while (toBeRemoved.size() > 0)
		{
			alpha.removeEvent((LabeledEvent) toBeRemoved.remove(0));
		}
	}

	/**
	 * Method that stops AutomatonMinimizer as soon as possible.
	 *
	 * @see  ExecutionDialog
	 */
	public void requestStop()
	{
		stopRequested = true;

		logger.debug("AutomatonMinimizer requested to stop.");
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
