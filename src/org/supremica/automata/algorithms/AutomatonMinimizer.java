
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

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.standard.Determinizer;
import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.ActionMan;
import java.awt.Toolkit;
import org.supremica.util.ActionTimer;

// Factory object for generating the correct class according to prefs
/*
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
*/

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

	// Local debug flag... 
	public static final boolean debug = false;

	// Use short names
	private boolean useShortNames = false;

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
			// We need a copy since we must make the system reachable 
			// (and maybe deterministic) first!
			theAutomaton = new Automaton(theAutomaton);
		}
		
		ActionTimer preMinimizationTimer = new ActionTimer();
		ActionTimer saturationTimer = new ActionTimer();
		ActionTimer adjustMarkingTimer = new ActionTimer();
		ActionTimer postMinimizationTimer = new ActionTimer();
		ActionTimer partitioningTimer = new ActionTimer();
		ActionTimer removeTransitionsTimer = new ActionTimer();
		ActionTimer automataBuildTimer = new ActionTimer();

		// Make reachable
        SynthesizerOptions synthOptions = SynthesizerOptions.getDefaultSynthesizerOptions();
		AutomatonSynthesizer synth = new AutomatonSynthesizer(theAutomaton, synthOptions);
		synth.doReachable(true);
		LinkedList toBeRemoved = new LinkedList();
		for (StateIterator it = theAutomaton.stateIterator(); it.hasNext(); )
		{
			State state = it.nextState();
			if ((state.getCost() == State.MAX_COST) && !state.isForbidden())
			{
				// logger.fatal("The state " + state + " is not reachable.");
				toBeRemoved.add(state);
			}

			if (state.isForbidden())
			{
				// logger.fatal("The state " + state + " is forbidden.");
			}
		}
		while (toBeRemoved.size() != 0)
		{
			theAutomaton.removeState((State) toBeRemoved.remove(0));
		}

		if (debug)
		{
			Thread.sleep(0);
			preMinimizationTimer.start();
		}

		// Find out what to do
		EquivalenceRelation equivalenceRelation = options.getMinimizationType();
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

			if (debug)
			{
				preMinimizationTimer.stop();
				logger.fatal("Determinization: " + preMinimizationTimer);
				Thread.sleep(0);
				partitioningTimer.start();
			}

			// Now we're ready for partitioning!
		}
		else if (equivalenceRelation == EquivalenceRelation.ObservationEquivalence || 
				 equivalenceRelation == EquivalenceRelation.ConflictEquivalence)  
		{
			// Merge silent loops and other obvious OE stuff (to save computation later)...
			// This is actually NOT entirely OE, considering the marked states!! 
			if (theAutomaton.getAlphabet().size() > 1)
			{
				int count = preMinimizationMergeObservationEquivalentStates(theAutomaton);
				if (count > 0 && debug)
				{
					logger.fatal("Removed " + count + " conflict equivalent states " +
								 "before running the partitioning.");
				}
			}

			if (debug)
			{
				preMinimizationTimer.stop();
				logger.fatal("Pre partitioning: " + preMinimizationTimer);
				Thread.sleep(0);
				saturationTimer.start();
			}

			// Saturate
			int transitions = 0;
			//int transitions = doTransitiveClosure(theAutomaton);

			if (debug)
			{
				saturationTimer.stop();	
				//logger.fatal("Saturation: " + saturationTimer + 
				//		  	   " (added " + transitions + " new transitions)");
				Thread.sleep(0);
			}
			
			// Some stuff that are unique to the conflict equivalence
			if (equivalenceRelation == EquivalenceRelation.ConflictEquivalence)  
			{
				if (stopRequested)
				{
					return null;
				}

				////////////
				// RULE D //
				////////////
				
				if (debug)
				{
					adjustMarkingTimer.start();	
				}

				// Adjust marking based on epsilon transitions 
				adjustMarking(theAutomaton); // Not OE
				
				if (debug)
				{
					adjustMarkingTimer.stop();	
					logger.fatal("Adjust markings: " + adjustMarkingTimer);
					Thread.sleep(0);
				}
			}
			
			if (debug)
			{
				partitioningTimer.start();
			}
			
			// Now we're ready for partitioning!
		}
		else
		{
			throw new Exception("Unknown equivalence relation");
		}

		if (stopRequested)
		{
			return null;
		}

		// After the above preparations, we can do the minimization 
		// in the same way for all cases! 
	
		// Find the coarsest partitioning
		EquivalenceClasses equivClasses = new EquivalenceClasses();
		try
		{
			// Find initial partitioning (based on marking, basically)
			equivClasses = findInitialPartitioning(theAutomaton);

			if (stopRequested)
			{
				return null;
			}

			// Minimize
			findCoarsestPartitioning(equivClasses);
		}
		catch (Exception ex)
		{
			requestStop();
			logger.debug(ex.getStackTrace());

			throw ex;
		}
		
		if (debug)
		{
			partitioningTimer.stop();	
			logger.fatal("Partitioning: " + partitioningTimer);
			Thread.sleep(0);
			automataBuildTimer.start();
		}

		// Build the minimized automaton based on the partitioning in equivClasses
		Automaton newAutomaton = buildAutomaton(equivClasses);

		if (stopRequested)
		{
			return null;
		}

		if (debug)
		{
			automataBuildTimer.stop();
			logger.fatal("Automaton build: " + automataBuildTimer);
			Thread.sleep(0);
			removeTransitionsTimer.start();
		}

		// Should we remove redundant transitions to minimize also with respect to transitions?
		if (options.getAlsoTransitions())
		{
			doTransitiveClosure(newAutomaton);
			removeRedundantTransitions(newAutomaton);
		}

   		if (debug)
		{
			removeTransitionsTimer.stop();
			if (options.getAlsoTransitions())
				logger.fatal("Remove transitions: " + removeTransitionsTimer);
			Thread.sleep(0);
			postMinimizationTimer.start();
		}
		
		// Post minimization adjustments
		if (equivalenceRelation == EquivalenceRelation.ConflictEquivalence)
		{
			// Merge conflict equivalent states and stuff...
			// (This is a FEATURE!)
			int count = postMinimizationMergeConflictEquivalentStates(newAutomaton); // Not OE
			if (count > 0)
			{
				logger.debug("Removed " + count + " conflict equivalent states " +
							 "after running partitioning.");
			}
		}
		
		// Remove from alphabet epsilon events that are never used
		removeUnusedEpsilonEvents(newAutomaton);

		if (debug)
		{
			postMinimizationTimer.stop();
			logger.fatal("Post partitioning: " + postMinimizationTimer);
			Thread.sleep(0);
		}

		// Return the result of the minimization!
		return newAutomaton;
	}

	/**
	 * Find the initial partitioning of this automaton, based on the marking and forbidden
	 * states (marking can be ignored using the minimization option ignoreMarking.
	 */
	private EquivalenceClasses findInitialPartitioning(Automaton aut)
	{
		if (aut == null)
		{
			return null;
		}

		// Divide the state space into three initial equivalence classes, based on markings
		EquivalenceClass acceptingStates = new EquivalenceClass(); //EqClassFactory.getEqClass();
		EquivalenceClass forbiddenStates = new EquivalenceClass(); //EqClassFactory.getEqClass();
		EquivalenceClass ordinaryStates = new EquivalenceClass(); //EqClassFactory.getEqClass();

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
				currState.setStateSet(ordinaryStates);
				ordinaryStates.add(currState);
			}
		}

		// Put these new classes into a single object
		// Only if there are any states in the class...
		EquivalenceClasses equivClasses = new EquivalenceClasses();
		if (acceptingStates.size() > 0)
		{
			equivClasses.add(acceptingStates);
		}

		if (ordinaryStates.size() > 0)
		{
			equivClasses.add(ordinaryStates);
		}

		if (forbiddenStates.size() > 0)
		{
			equivClasses.add(forbiddenStates);
		}

		return equivClasses;
    }

	/**
	 * Generate states with short names!
	 */
	public void useShortStateNames(boolean bool)
	{
		useShortNames = bool;
	}

	/**
	 * Returns the minimized automaton, based on the partitioning in equivClasses.
	 */
	private Automaton buildAutomaton(EquivalenceClasses equivClasses)
		throws Exception
	{
		Automaton newAutomaton = new Automaton();

		newAutomaton.setType(theAutomaton.getType());
		newAutomaton.getAlphabet().union(theAutomaton.getAlphabet()); // Odd... but it works.

		// Associate one state with each equivalence class
		int stateNumber = 1; // Number 0 is dedicated to the initial state
		Iterator equivClassIt = equivClasses.iterator();
		while (equivClassIt.hasNext())
		{
			EquivalenceClass currEquivClass = (EquivalenceClass) equivClassIt.next();
			State currState = currEquivClass.getSingleStateRepresentation();
			if (currEquivClass.hasInitialState())
			{
				currState.setInitial(true);
				if (useShortNames)
				{
					currState.setName("q" + 0);
				}
			}
			else
			{
				if (useShortNames)
				{
					currState.setName("q" + stateNumber++);
				}
			}
			newAutomaton.addState(currState);
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
			State fromState = currEquivClass.getSingleStateRepresentation();

			// Note that the below only returns an iterator to SOME of the outgoing arc
			// from the EquivalenceClass (from one state only). This is OK, though, since 
			// all states in the equivalence class should have the same outgoing transitions 
			// (with respect to equivalence classes), otherwise they are not really equivalent! 
			//Iterator outgoingArcsIt = currEquivClass.get().outgoingArcsIterator();

			// Since the automaton isn't saturated, we have to loop through all arcs!
			Iterator outgoingArcsIt = currEquivClass.outgoingArcsIterator();
			while (outgoingArcsIt.hasNext())
			{
				Arc currArc = (Arc) outgoingArcsIt.next();
				LabeledEvent currEvent = currArc.getEvent();
				State oldToState = currArc.getToState();

				// Add the new arc!
				EquivalenceClass nextEquivalenceClass = (EquivalenceClass) oldToState.getStateSet();
				State toState = nextEquivalenceClass.getSingleStateRepresentation();
				Arc newArc = new Arc(fromState, toState, currEvent);

				// If we should minimize the number of transitions, make sure a transition is never
				// present more than once (this is performed in an ugly way below)
				//if (!(options.getAlsoTransitions() && newArc.getFromState().containsOutgoingArc(newArc)))
				if (!newArc.getFromState().containsOutgoingArc(newArc))
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
	 * Finds the coarsest partitioning of the supplied equivalence classes.
	 * In each partition, all states have corresponding outgoing arcs. Based
	 * on this, an automaton with a minimal number of states can be generated...
	 */
	private void findCoarsestPartitioning(EquivalenceClasses equivClasses)
	{
		boolean refined;

		// Repeat until no refinement occurs.
		do
		{
			refined = false;

			if (stopRequested)
			{
				return;
			}

			// Split all current equivClasses
			Object[] array = equivClasses.toArray();
			for (int i=0; i<array.length; i++)
			{
				EquivalenceClass currClass = (EquivalenceClass) array[i];
				
				// Don't try to refine single-state classes!
				if (currClass.size() > 1)
				{
					// refined = refined || partition(equivClasses, currClass); // WRONG!
					refined = partition(equivClasses, currClass) || refined;
				}
			}
		}
		while (refined);
	}
	private boolean partition(EquivalenceClasses equivClasses, EquivalenceClass equivClass)
	{
		boolean refined = false;

		for (Iterator eventIt = theAutomaton.getAlphabet().iterator(); eventIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();
			// refined = partition(equivClasses, equivClass, currEvent) || refined; // WRONG!
			refined = refined || partition(equivClasses, equivClass, currEvent);
		}

		return refined;
	}
	private boolean partition(EquivalenceClasses equivClasses, EquivalenceClass equivClass, LabeledEvent e)
	{
		// "Split" class on event 'e', i.e. based on where the 'e'-transitions lead
		EquivalenceClassHolder newEquivClassHolder = equivClass.split(e);

		// Do the states in equivClass have different behaviour on 'e'?
		if (newEquivClassHolder.size() > 1)
		{
			// Throw old class away!
			equivClasses.remove(equivClass);
			equivClass.clear();

			// Add the new classes to equivClasses
			equivClasses.addAll(newEquivClassHolder);

			// Help garbage collector
			newEquivClassHolder.clear();

			return true;
		}
		else
		{
			// All states in equivClass classifies to the same class
			// (at this stage)
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

		// Remove all outgoing arcs from these states
		for (Iterator it = aut.stateIterator(); it.hasNext(); )
		{
			State state = (State) it.next();
			if ((state.getCost() == State.MAX_COST))
			{
				if (state.isAccepting())
				{
					// How did this happen!? An accepting state is always coreachable!?!?
					logger.fatal("The state " + state + " forb: " + state.isForbidden() + 
								 " accept: " + state.isAccepting() + " init: " + state.isInitial());
					throw new Exception("Error in AutomatonMinimizer, coreachability failed!");
				}

				// We will never want to propagate from here...
				// ... or from any state in the backwards epsilon closure!!
				StateSet statesToModify = new StateSet();
				statesToModify.add(state);
				while (statesToModify.size() != 0)
				{
					State currState = statesToModify.get();
					statesToModify.remove(currState);

					// Accepting states that by epsilons may reach a block are modified to be 
					// nonaccepting an exception is made if this is the initial state!
					if (currState.isAccepting())
					{
						if (currState.isInitial())
						{
							if (debug)
								logger.info("The system was found to be blocking.");
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
		LinkedList toBeRemoved = new LinkedList();
		for (StateIterator it = aut.stateIterator(); it.hasNext(); )
		{
			State state = it.nextState();
			if ((state.getCost() == State.MAX_COST) && !state.isForbidden())
			{
				toBeRemoved.add(state);
			}
		}
		while (toBeRemoved.size() != 0)
		{
			count++;
			aut.removeState((State) toBeRemoved.remove(0));
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
						aut.removeArc(arc); // We can remove this one, it will become an epsilon self-loop!
						statesToExamine.remove(two);
						State mergeState = aut.mergeStates(two, one, useShortNames);
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
		int count2 = 0;

		// Do the merging
		while (statesToExamine.size() != 0)
		{
			count2++;

			if (stopRequested)
			{
				aut = null;
				return 0;
			}

			// Get and remove arbitrary state
			State one = statesToExamine.remove();

			// If there is only one transition out of this state AND it is an epsilon-transition
			// merge this and next state.
			ArcIterator arcIt = one.outgoingArcsIterator();
			if (arcIt.hasNext())
			{
				// So this arc may be OK, just check that there are no more arcs in the iterator!
				Arc arc = arcIt.nextArc();
				State two = arc.getToState();
				boolean arcOK = !arcIt.hasNext() && arc.getEvent().isEpsilon() && !arc.isSelfLoop();
				boolean markingOK = options.getIgnoreMarking() || one.hasEqualMarking(two);
				arcIt = null;
				if (arcOK && markingOK)
				{
					// We can remove this arc, it will become an epsilon self-loop!
					aut.removeArc(arc); 

					// Merge!
					count++;
					statesToExamine.remove(two);
					State mergeState = aut.mergeStates(one, two, useShortNames);
					statesToExamine.add(mergeState);
					// Add states that may have changed to stack
					for (ArcIterator it = mergeState.incomingArcsIterator(); it.hasNext(); )
					{
						Arc epsilonArc = it.nextArc();
						if (epsilonArc.getEvent().isEpsilon())
						{
							statesToExamine.add(epsilonArc.getFromState());
						}
					}
					//logger.info("I merged " + one + " and " + two + 
					//            " since I thought they were OE.");
					continue; // Get next "one" from stack
				}
			}

			// Merge loops

			// Find forwards and backwards closures
			StateSet forwardsClosure = one.epsilonClosure();
			StateSet backwardsClosure = one.backwardsEpsilonClosure();
			int forwSize = forwardsClosure.size();

			// Merge all states in the intersection!
			forwardsClosure.remove(one); // Skip self
			while (forwardsClosure.size() != 0)
			{
				State two = forwardsClosure.remove();

				if (backwardsClosure.contains(two))
				{
					// Merge!
					count++;
					statesToExamine.remove(two);
					one = aut.mergeStates(one, two, useShortNames);
					//logger.info(one);
				}
			}

			/*
			// Remove statesToExamine based on this...
			StateSet search = new StateSet();
			search.add(one);
			int count3 = 0;
			while (search.size() != 0)
			{
				State current = search.remove();
				ArcIterator outIt = current.outgoingArcsIterator();
				while (outIt.hasNext())
				{
					Arc arc = outIt.nextArc();
					if (arc.getEvent().isEpsilon())
					{
						State target = arc.getToState();
						
						// If there are no incoming epsilons here except for "arc" there can be
						// no other loops where "target" is involved
						boolean fail = false;
						ArcIterator inIt = target.incomingArcsIterator();
						while (inIt.hasNext())
						{
							Arc incoming = inIt.nextArc();
							if (arc.getEvent().isEpsilon() && !arc.equals(incoming))
							{
								fail = true;
								break;
							}
						}
						if (!fail)
						{
							// This state does not have to be examined later!
							count3++;
							statesToExamine.remove(target);
							search.add(target);
						}
					}
				}
			}
			search.add(one);
			while (search.size() != 0)
			{
				State current = search.remove();
				ArcIterator outIt = current.incomingArcsIterator();
				while (outIt.hasNext())
				{
					Arc arc = outIt.nextArc();
					if (arc.getEvent().isEpsilon())
					{
						State target = arc.getFromState();
						
						// If there are no outgoing epsilons here except for "arc" there can be
						// no other loops where "target" is involved
						boolean fail = false;
						ArcIterator inIt = target.outgoingArcsIterator();
						while (inIt.hasNext())
						{
							Arc incoming = inIt.nextArc();
							if (arc.getEvent().isEpsilon() && !arc.equals(incoming))
							{
								fail = true;
								break;
							}
						}
						if (!fail)
						{
							// This state does not have to be examined later!
							count3++;
							statesToExamine.remove(target);
							search.add(target);
						}
					}
				}
			}
			logger.info("Forw: " + forwSize + " back: " + backwardsClosure.size() + " Count3: " + count3);
			*/
			
			/*
			StateSet closureOne = one.epsilonClosure();
			closureOne.remove(one); // Don't examine self

			boolean change = true;
			boolean newOne = false;
			while (change)
			{
				if (stopRequested)
				{
					return 0;
				}
				
				change = false;
				StateSet previousStates = new StateSet();
				ArcIterator it = one.incomingArcsIterator();
				while (it.hasNext())
				{
					Arc arc = it.nextArc();
					if (arc.getEvent().isEpsilon())
					{
						previousStates.add(arc.getFromState());
					}
				}
				while (previousStates.size() != 0)
				{
					State two = previousStates.remove();
					
					// If a state in the epsilonclosure can reach one by an epsilon transition... 
					if (closureOne.contains(two))
					{
						// Merge! 
						count++;
						statesToExamine.remove(two);
						one = aut.mergeStates(one, two, useShortNames);
						change = true;
					}
				}
				if (change)
				{
					newOne = true;
				}
			}
			if (newOne)
			{
				statesToExamine.add(one);
			}
			*/

			/*
			// Find epsilon-closure for this state
			StateSet closureOne = one.epsilonClosure();
			closureOne.remove(one); // Don't examine self

			// Find, in closure, if there is a state which has the first state in its closure
			for (StateIterator closureIt = closureOne.iterator(); closureIt.hasNext(); )
			{
				State two = closureIt.nextState();
				StateSet closureTwo = two.epsilonClosure();

				// Good to merge?
				//boolean markingOK = options.getIgnoreMarking() || one.hasEqualMarking(two);
				//if (closureTwo.contains(one) && markingOK)
				if (closureTwo.contains(one)) 
				{	   			
					// Remove the other state from stack, merge and add
					// new state to stack
					count++;
					statesToExamine.remove(two);
					State merge = aut.mergeStates(one, two, useShortNames);
					statesToExamine.add(merge);

					//one = merge;
					break; // Get next "one" from stack
				}
			}
			*/

			/*
			// Alternative implementation, appears to suck
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

		if (debug)
			logger.error("Prepartitioning turns in loop: " + count2);

		return count;
	}

	/**
	 * Add transitions to cover for the epsilon events (aka "saturate"). More formally, each
	 * time there is a transition "p =a=> q", after completing the transitive closure (or
	 * "saturation"), there is also a transition "p -a-> q".
	 */
	public int doTransitiveClosure(Automaton aut)
	{
		if (aut == null)
		{
			return -1;
		}

		// Find epsilon-closure for each state, put this info in each state
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			if (stopRequested)
			{
				aut = null;
				return -1;
			}

			State currState = stateIt.nextState();

			// Find closure, associate it with this state
			StateSet closure = currState.epsilonClosure();
			currState.setStateSet(closure);
		}

		// From each state add transitions that are present in its closure
		LinkedList toBeAdded = new LinkedList();
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			if (stopRequested)
			{
				aut = null;
				return -1;
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

		// Add the new arcs
		int amount = toBeAdded.size();
		int added = 0;
		//logger.debug("Added " + toBeAdded.size() + " transitions to " + aut + ".");
		while (added != amount)
		{
			// Add if not already there
			Arc arc = (Arc) toBeAdded.remove(0);
			if (!arc.getFromState().containsOutgoingArc(arc))
			{
				aut.addArc(arc);
			}

			added++;
		}

		if (stopRequested)
		{
			aut = null;
			return -1;
		}

		return amount;
	}

	/**
	 * All states that can reach marked states by epsilon events are also considered marked.
	 */
	private void adjustMarking(Automaton aut)
	{
		if (aut == null || stopRequested)
		{
			return;
		}

		/*
		// States in epsilon loops where there are at least one marked state can be considered marked
		LinkedList toBeMarked = new LinkedList();
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

		/*
		*
		* NOTE: This method assumes that the epsilon-closure of each state is already calculated
		* and that the closure is returned by each state's getStateSet-method. (This is true if
	    * doTransitiveClosure was called previously.)
		// States that can reach marked states by epsilon events only can be considered marked
		LinkedList toBeMarked = new LinkedList();
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			State currState = stateIt.nextState();
			//if (!currState.isAccepting() && !currState.isInitial())
			if (!currState.isAccepting())
			{
				StateSet closure = currState.getStateSet();
				if (closure == null) 
				{
					// The closure was not calculated? Bail out!
					logger.fatal("Transitive closure must be calculated before calling " + 
								 "the method AutomatonMinimizer.adjustMarking.");
					aut = null;
					return;
				}
				for (StateIterator closureIt = closure.iterator(); closureIt.hasNext(); )
				{
					State otherState = closureIt.nextState();
					if (otherState.isAccepting())
					{
						toBeMarked.add(currState);
						break;
					}
				}
			}
		}
		*/

		// States that can reach marked states by epsilon events only can be considered marked
		LinkedList toBeMarked = new LinkedList();
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			State currState = stateIt.nextState();
			if (!currState.isAccepting())
			{
				StateSet closure = currState.epsilonClosure();
				for (StateIterator closureIt = closure.iterator(); closureIt.hasNext(); )
				{
					State otherState = closureIt.nextState();
					if (otherState.isAccepting())
					{
						toBeMarked.add(currState);
						break;
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
	 * Algorithm inspired by "Minimizing the Number of Transitions with Respect to Observation 
	 * Equivalence" by Jaana Eloranta. Removes all transitions that are redundant.
	 */
	private void removeRedundantTransitions(Automaton aut)
	{
		// Are there any silent-self-loops? They can be removed!
		// Note! These are not "redundant" by Jaana Elorantas definition, but must be
		// removed before removing her "redundant transitions" (see below).

		// Put silent self-loops in a list, remove afterwards
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

		// Put redundant arcs in list, remove after all have been found
		toBeRemoved.clear();
		loop: for (ArcIterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();

			// Using Elorantas notation... (s1, s2 and (later) s3)
			State s1 = arc.getFromState();
			State s2 = arc.getToState();

			// Is the criteria fulfilled? (I.e. does there exist a s3 such that either
			// (s1 -a-> s3 and s3 -tau-> s2) or (s1 -tau-> s3 and s3 -a-> s2) holds?)
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
							if ((secondEvent.isEpsilon() && 
								 (!firstEvent.isEpsilon() || arc.getEvent().isEpsilon())) || 
								(secondEvent.equals(arc.getEvent()) && 
								 !firstEvent.equals(secondEvent)))
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
	private void removeUnusedEpsilonEvents(Automaton aut)
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

class EquivalenceClass
	extends StateSet
{
	/**
	 * Split the equivalence class according to what can be reached on this event
	 * Return all new equivalence classes in the holder
	 * If the holder contains only one entry, all reached states have the same eq class 
	 * (at this point)
	 */
	public EquivalenceClassHolder split(LabeledEvent e)
	{
		boolean oldversion = false;
		if (oldversion)
		{
			// System.err.println("Splitting " + e.getLabel());
			EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();

			// Build a list of equivalance classes that e transfers to
			// from each of the states in this eq-class
			// Note, for each state there is only one successor state 
			// for this event (determinism)
			// NOOOOOOOOOOOOOOOOO! NOT IT'S NOT DETERMINISTIC!!
			Iterator stateIt = iterator();
			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();
				State nextState = currState.nextState(e);
				EquivalenceClass nextEquivalenceClass = null;
				if (nextState != null)
				{
					nextEquivalenceClass = (EquivalenceClass) nextState.getStateSet();
				}
				newEquivalenceClassHolder.addState(currState, nextEquivalenceClass);
			}

			return newEquivalenceClassHolder;
		}
		else
		{
			EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();

			// Build a list of equivalence classes that e transfers to 
			// from each of the states in this eq-class
			// Note, for each state there may be several successor state 
			// for this event (nondeterminism)
			Iterator stateIt = iterator();
			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();
				StateSet nextStates = currState.nextStateSet(e, true);
				EquivalenceClass nextClass = new EquivalenceClass();
				for (StateIterator nextIt = nextStates.iterator(); nextIt.hasNext(); )
				{
					State nextState = nextIt.nextState();
					EquivalenceClass thisNextClass = (EquivalenceClass) nextState.getStateSet();
					nextClass.union(thisNextClass);
				}
				newEquivalenceClassHolder.addState(currState, nextClass);
			}

			return newEquivalenceClassHolder;
		}
	}
}

/**
 * Fabians play-around version of EquivalencClass
 * This class itself generates the state that it corresponds to
 * It sets the state-name as the concatenation of the original state names
 * This has the effect that it keeps the same name for singleton equivalence classes
 */
/*
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
}
*/

/**
 * Temporary help object for storing new equivalence classes.
 *
 *@author  ka
 *@created  November 28, 2001
 */
class EquivalenceClassHolder
	extends HashMap
{
	private static final long serialVersionUID = 1L;

	public void addState(State state, EquivalenceClass nextClass)
	{
		// If the next equivalence class does not exist create it
		if (!containsKey(nextClass))
		{
			EquivalenceClass newEquivClass = new EquivalenceClass(); //EqClassFactory.getEqClass(nextClass);

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
