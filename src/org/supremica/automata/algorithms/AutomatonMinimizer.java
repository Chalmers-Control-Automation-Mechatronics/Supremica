
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
import org.supremica.gui.ExecutionDialog;
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
	private ExecutionDialog executionDialog = null;
	private boolean stopRequested = false;

	/** The automaton being minimized (may be a copy of the original). */
	private Automaton theAutomaton;

	/** The supplied options. */
	private MinimizationOptions options;

	// Local debug stuff... to be erased when things are stable!
	public static final boolean debug = false;
	private static int totalA = 0;
	private static int totalB = 0;
	private static int totalC = 0;
	private static int totalD = 0;
	private static int totalF = 0;
	private static int totalOE = 0;
	private static int totalArcs = 0;

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

		// Message
		int before = theAutomaton.nbrOfStates();
		int epsilons = theAutomaton.nbrOfEpsilonTransitions();
		int total = theAutomaton.nbrOfTransitions();
		logger.verbose("Minimizing " + theAutomaton + " with " + before +
					   " states and " + epsilons + " epsilon transitions (" + 
					   Math.round(100*(((double) epsilons)*100/total))/100.0 + "%).");
		
		// Are the options valid?
		if (!options.isValid())
		{
			return null;
		}
		
		// Just to run quick tests
		if (options.getMinimizationType() == EquivalenceRelation.ConflictEquivalenceMinimizationRules)
		{
			int count = runRules(theAutomaton);
			if (count > 0)
			{
				logger.verbose("Removed " + count + " states based on conflict equivalence " +
							   "after running partitioning.");
			}
			
			/*
			  State s140 = theAutomaton.getStateWithName("s140");
			  State s105 = theAutomaton.getStateWithName("s105");
			  State merge = theAutomaton.mergeStates(s140, s105);
			  
			  State s86 = theAutomaton.getStateWithName("s86");
			  State s71 = theAutomaton.getStateWithName("s71");
			  State s35 = theAutomaton.getStateWithName("s35");
			  State s107 = theAutomaton.getStateWithName("s107");
			  
			  merge = theAutomaton.mergeStates(s86, s71);
			  merge = theAutomaton.mergeStates(merge, s35);
			  merge = theAutomaton.mergeStates(merge, s107);
			*/
			  
			theAutomaton.setName("apa(" + theAutomaton.getName() + ")");
			
			return theAutomaton;
		}

		ActionTimer preMinimizationTimer = new ActionTimer();
		ActionTimer saturationTimer = new ActionTimer();
		ActionTimer adjustMarkingTimer = new ActionTimer();
		ActionTimer postMinimizationTimer = new ActionTimer();
		ActionTimer partitioningTimer = new ActionTimer();
		ActionTimer removeTransitionsTimer = new ActionTimer();
		ActionTimer automataBuildTimer = new ActionTimer();
		ActionTimer rulesTimer = new ActionTimer();

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
				logger.verbose("The state " + state + " will be removed since it is not reachable.");
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
		else if (equivalenceRelation == EquivalenceRelation.ObservationEquivalence)  
		{
			// Merge silent loops and other obvious OE stuff (to save computation later)...
			// This is actually NOT entirely OE, considering the marked states!! 
			int trivialCount = mergeTriviallyObservationEquivalentStates(theAutomaton);
			if (trivialCount > 0)
			{
				logger.verbose("Removed " + trivialCount + " trivially silent states " +
							   "before running the partitioning.");
			}
			int loopCount = mergeEpsilonLoops(theAutomaton); // Not entirely OE if marking is considered!
			if (loopCount > 0)
			{
				logger.verbose("Removed " + loopCount + " states involved in silent loops " +
							   "before running the partitioning.");
			}

			if (debug)
			{
				preMinimizationTimer.stop();
				logger.fatal("Pre partitioning: " + preMinimizationTimer);
				Thread.sleep(0);
				partitioningTimer.start();
			}

			// Now we're ready for partitioning!
		}
		else if (equivalenceRelation == EquivalenceRelation.ConflictEquivalence)  
		{
			// Merge silent loops
			// Don't bother if there is only one event in alphabet (epsilon or not)
			if (theAutomaton.getAlphabet().size() > 1)
			{
				int loopCount = mergeEpsilonLoops(theAutomaton);
				if (loopCount > 0)
				{
					logger.verbose("Removed " + loopCount + " states involved in silent loops " +
								   "before running the partitioning.");
				}
			}
			else if ((theAutomaton.nbrOfEpsilonEvents() == 1) && useShortNames)
			{
				// The conflict equivalent automaton is just one state, the initial state. 
				// Marked if the automaton is nonblocking, nonmarked otherwise.
				Automaton newAutomaton = new Automaton("min(" + theAutomaton.getName() + ")");
				State initial = new State("q0");
				initial.setInitial(true);
				newAutomaton.addState(initial);	

				// Accepting iff nonblocking
				initial.setAccepting(AutomataVerifier.verifyMonolithicNonblocking(new Automata(theAutomaton)));

				return newAutomaton;
			}

			if (debug)
			{
				preMinimizationTimer.stop();
				logger.fatal("Merge loops: " + preMinimizationTimer);
				Thread.sleep(0);
				adjustMarkingTimer.start();	
			}
			
			////////////
			// RULE D //
			////////////
			
			// Adjust marking based on epsilon transitions 
			int countD = ruleD(theAutomaton); // Not OE
			totalD += countD;
			
			if (debug)
			{
				adjustMarkingTimer.stop();	
				logger.fatal("Adjust markings: " + adjustMarkingTimer);
				Thread.sleep(0);
				rulesTimer.start();
			}
	
			//////////////////////
			// RULES A, B, C, F //
			//////////////////////
			
			int count = runRules(theAutomaton);
			if (count > 0)
			{
				logger.verbose("Removed " + count + " states based on conflict equivalence " +
							   "before running partitioning.");
			}
			
			if (debug)
			{
				rulesTimer.stop();	
				logger.fatal("Rules A, B, C and F: " + rulesTimer);
				Thread.sleep(0);
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
		
		// Find the coarsest partitioning
		EquivalenceClasses equivClasses = new EquivalenceClasses();
		try
		{
			// Find initial partitioning (based on marking, basically)
			equivClasses = findInitialPartitioning(theAutomaton);
			
			// Partition
			findCoarsestPartitioning(equivClasses);
			
			if (stopRequested)
			{
				return null;
			}
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
		int diffSize = theAutomaton.nbrOfStates() - newAutomaton.nbrOfStates();
		totalOE += diffSize;
		if (diffSize > 0)
		{
			logger.verbose("Removed " + diffSize + " states based on partitioning with " + 
						   "respect to observation equivalence.");
		}
		
		if (stopRequested)
		{
			return null;
		}

		if (debug)
		{
			automataBuildTimer.stop();
			logger.fatal("Automaton build: " + automataBuildTimer);
			Thread.sleep(0);
			postMinimizationTimer.start();
		}
		
		// Apply rules
		if (equivalenceRelation == EquivalenceRelation.ConflictEquivalence)
		{
			///////////////////
			// RULES A, B, C //
			///////////////////

			int count = runRules(newAutomaton);
			if (count > 0)
			{
				logger.verbose("Removed " + count + " states based on conflict equivalence " +
							   "after running partitioning.");
			}
		}

		if (debug)
		{
			postMinimizationTimer.stop();
			logger.fatal("Conflict equivalence rules: " + postMinimizationTimer);
			Thread.sleep(0);
			removeTransitionsTimer.start();
		}

		// Should we remove redundant transitions to minimize also with respect to transitions?
		int transitionCount = 0;
		if (options.getAlsoTransitions())
		{
			// doTransitiveClosure(newAutomaton);
			transitionCount = newAutomaton.nbrOfTransitions();
			int countArcs = removeRedundantTransitions(newAutomaton);
			totalArcs += countArcs;
		}

   		if (debug)
		{
			removeTransitionsTimer.stop();
			if (options.getAlsoTransitions())
				logger.fatal("Remove transitions: " + removeTransitionsTimer.toString().trim() + ", peak number of transitions: " + transitionCount);
			Thread.sleep(0);
		}

		// Remove from alphabet epsilon events that are never used
		removeUnusedEpsilonEvents(newAutomaton);
		
		// Message
		int after = newAutomaton.nbrOfStates();
		logger.verbose("There were " + before + " states before and " + after + 
					   " states after the minimization. Reduction: " + 
					   Math.round(100*(((double) (before-after))*100/before))/100.0 + "%.");
		
		// Return the result of the minimization!
		return newAutomaton;
	}

	private void checkForEpsilonLoops(Automaton aut)
	{
		StateSet statesToExamine = new StateSet(aut.getStateSet());

		// Count the removed states
		int count = 0;

		// Do the merging
		while (statesToExamine.size() != 0)
		{
			// Get and remove arbitrary state
			State one = statesToExamine.remove();
			
			// Find forwards and backwards closures
			StateSet forwardsClosure = one.epsilonClosure(false);
			StateSet backwardsClosure = one.backwardsEpsilonClosure();
			
			// Merge all states in the intersection!
			forwardsClosure.remove(one); // Skip self
			while (forwardsClosure.size() != 0)
			{
				State two = forwardsClosure.remove();
				
				if (backwardsClosure.contains(two))
				{
					StateInfo infoOne = new StateInfo(one);
					StateInfo infoTwo = new StateInfo(two);
					logger.info("" + infoOne);
					logger.info("" + infoTwo);
					logger.info("Equal character: " + infoOne.equalCharacterization(infoTwo) + infoTwo.equalCharacterization(infoOne) + " equal: " + infoOne.equals(infoTwo) + infoTwo.equals(infoOne));

					// A loop!
					count++;
					//logger.info(one);
				}
			}
		}

		if (count > 0)
		{
			logger.verbose("Loops! " + count);
		}
	}

	private void checkStateIndices(Automaton aut)
	{
		TreeSet sort = new TreeSet();

		for (StateIterator it = aut.stateIterator(); it.hasNext(); )
		{
			sort.add(new Integer(it.nextState().getIndex()));
		}

		for (Iterator it = sort.iterator(); it.hasNext(); )
		{
			logger.error(it.next().toString());
		}
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
			//ArcIterator outgoingArcsIt = currEquivClass.get().outgoingArcsIterator();

			// Since the automaton isn't saturated, we have to loop through all arcs!
			ArcIterator outgoingArcsIt = currEquivClass.outgoingArcsIterator();
			while (outgoingArcsIt.hasNext())
			{
				Arc currArc = outgoingArcsIt.nextArc();
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
		// Repeat until no refinement occurs.
		boolean refined = true;
		while (refined)
		{
			refined = false;

			if (executionDialog != null)
			{
				executionDialog.setValue(equivClasses.size());
			}

			// Split all current equivClasses
			Object[] array = equivClasses.toArray();
			for (int i=0; i<array.length; i++)
			{
				if (stopRequested)
				{
					return;
				}

				EquivalenceClass currClass = (EquivalenceClass) array[i];
				
				// Don't try to refine single-state classes!
				if (currClass.size() > 1)
				{
					// refined = refined || partition(equivClasses, currClass); // WRONG!
					refined = partition(equivClasses, currClass) || refined;
				}
			}
		}
	}
	/**
	 * Partitions equivClass by all events. 
	 *@return true if a partitioning was made, false otherwise.
	 */
	private boolean partition(EquivalenceClasses equivClasses, EquivalenceClass equivClass)
	{
		boolean refined = false;

		for (Iterator eventIt = theAutomaton.getAlphabet().iterator(); eventIt.hasNext(); )
		{
			if (stopRequested)
			{
				return false;
			}

			LabeledEvent currEvent = (LabeledEvent) eventIt.next();
			// refined = partition(equivClasses, equivClass, currEvent) || refined; // WRONG!
			refined = refined || partition(equivClasses, equivClass, currEvent);

			/*
			// Return as soon as a change is found!
			if (refined)
			{
				return true;
			}
			*/
		}

		return refined;
	}
	/**
	 * Partitions equivClass by the event e. 
	 * @return true if a partitioning was made, false otherwise.
	 */
	private boolean partition(EquivalenceClasses equivClasses, EquivalenceClass equivClass, LabeledEvent e)
	{
		// "Split" class on event 'e', i.e. based on where the 'e'-transitions lead
		EquivalenceClassHolder newEquivClassHolder = split(equivClass, e);

		// Do the states in equivClass have different behaviour on 'e'?
		if (newEquivClassHolder.size() > 1)
		{
			// Throw old class away!
			equivClasses.remove(equivClass);
			equivClass.clear();

			// Add the new classes to equivClasses
			equivClasses.addAll(newEquivClassHolder);

			if (executionDialog != null)
			{
				executionDialog.setValue(equivClasses.size());
			}

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
	 * Split an equivalence class according to what can be reached on this event
	 * Return all new equivalence classes in the holder
	 * If the holder contains only one entry, all reached states have the same eq class 
	 * (at this point)
	 *@param eqClass the class to be split.
	 *@param e the event that should be considered.
	 */
	private EquivalenceClassHolder split(StateSet eqClass, LabeledEvent e)
	{
		EquivalenceClassHolder newEquivalenceClassHolder = new EquivalenceClassHolder();

		/*
		// Build a list of equivalance classes that e transfers to
		// from each of the states in this eq-class
		// Note, for each state there is only one successor state 
		// for this event (determinism)
		// NOOOOOOOOOOOOOOOOO! NOT IT'S NOT DETERMINISTIC!!
		StateIterator stateIt = eqClass.iterator();
		while (stateIt.hasNext())
		{
			State currState = stateIt.nextState();
			State nextState = currState.nextState(e);
			EquivalenceClass nextEquivalenceClass = null;
			if (nextState != null)
			{
				nextEquivalenceClass = (EquivalenceClass) nextState.getStateSet();
			}
			newEquivalenceClassHolder.addState(currState, nextEquivalenceClass);
		}

		return newEquivalenceClassHolder;
		*/

		// Build a list of equivalence classes that e transfers to 
		// from each of the states in this eq-class
		// Note, for each state there may be several successor state 
		// for this event (nondeterminism)
		StateIterator stateIt = eqClass.iterator();
		while (stateIt.hasNext())
		{
			if (stopRequested)
			{
				newEquivalenceClassHolder.clear();
				return newEquivalenceClassHolder;
			}

			State currState = stateIt.nextState();
			StateSet nextStates;
			if (e.isEpsilon())
			{
				nextStates = currState.epsilonClosure(true);
			}
			else
			{
				nextStates = currState.nextStates(e, true);
			}
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

	/**
	 * Merges all "single-outgoing-epsilon-transition-states".
	 *
	 * @return Number of states that have been removed by merging or -1 if method didn't complete successfully.
	 */
	public int mergeTriviallyObservationEquivalentStates(Automaton aut)
	{
		StateSet statesToExamine = new StateSet(aut.getStateSet());

		// Count the removed states
		int count = 0;

		// Do the merging
		loop: while (statesToExamine.size() != 0)
		{
			if (stopRequested)
			{
				return -1;
			}
			
			// Get and remove arbitrary state
			State one = statesToExamine.remove();
			
			// If there is only one transition out of this state AND it is an epsilon-transition
			// merge this and next state. (This is actually a special case of rule B.)
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
					State state = aut.mergeStates(one, two, useShortNames);
					statesToExamine.add(state);
					// Add states that may have changed to stack
					for (ArcIterator it = state.incomingArcsIterator(); it.hasNext(); )
					{
						Arc epsilonArc = it.nextArc();
						if (epsilonArc.getEvent().isEpsilon())
						{
							statesToExamine.add(epsilonArc.getFromState());
						}
					}
					//logger.info("I merged " + one + " and " + two + " since I thought they were OE.");
					continue loop; // Get next "one" from stack
				}
			}
		}
		
		return count;
	}
	
	/**
	 * Merges all epsilon loops in automaton. Also merges states with different marking!
	 *
	 * @return the number of states that have been removed or -1 if method didn't complete successfully.
	 */
	public int mergeEpsilonLoops(Automaton aut)
	{
		StateSet statesToExamine = new StateSet(aut.getStateSet());
		
		// Count the removed states
		int count = 0;
		
		// Do the merging
		while (statesToExamine.size() != 0)
		{
			if (stopRequested)
			{
				return -1;
			}
			
			// Get and remove arbitrary state
			State one = statesToExamine.remove();
			
			// Merge loops in which this state is involved

			// Find forwards and backwards closures
			StateSet forwardsClosure = one.epsilonClosure(false);
			StateSet backwardsClosure = one.backwardsEpsilonClosure();

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
				}
			}
		}

		return count;
	}

	/**
	 * Runs some rules for minimization wrt conflict equivalence.
	 *
	 * @return total number of states removed.
	 */
	public int runRules(Automaton aut)
	{
		int total = 0;
			
		try 
		{
			int count;		
			do
			{
				// Remove redundant transitions
				int countArcs = removeRedundantTransitions(aut);
				
				// Merge conflict equivalent states and stuff...
				// Rule A
				int countA = 0;
				if (options.getUseRuleA())
				{
					countA = ruleAA(aut);
					totalA += countA;
				}
				if (countA > 0)
				{
					countArcs += removeRedundantTransitions(aut);
				}
				// Rule B
				int countB = 0;
				if (options.getUseRuleB())
				{
					countB = ruleB(aut);
					totalB += countB;
				}
				if (countB > 0)
				{
					countArcs += removeRedundantTransitions(aut);
				}
				// Rule C
				int countC = ruleC(aut);
				totalC += countC;
				// Rule F
				int countF = 0;
				if (options.getUseRuleF())
				{
					countF = ruleF(aut);
					totalF += countF;
				}

				totalArcs += countArcs;
				if (debug && countArcs > 0)
				{
					logger.verbose("Removed " + countArcs + " redundant transitions.");
				}
				
				if (debug)
					logger.warn("Rule A: " + countA + ", rule B: " + countB + 
								", rule C: " + countC + ", rule F: " + countF);
				count = countA+countB+countC+countF;
				total += count;
			} while (count > 0);
		}
		catch (Exception excp)
		{
			logger.error(excp);
			logger.debug(excp.getStackTrace());
			return -1;
		}

		return total;
	}

	/**
	 * Rule A.
	 *
	 * @return the number of states that have been removed or -1 if method didn't complete successfully.
	 */
	public int ruleA(Automaton aut)
		throws Exception
	{
		////////////
		// RULE A //
		////////////

		// Count the removed states
		int countA = 0;

		// If there is only one incoming transition to a state AND it is an epsilon-transition
		// AND there is at least one outgoing epsilon transition AND this is not the initial state!
		// merge this and previous state.
		StateSet statesToExamine = new StateSet(aut.getStateSet());
		loop: while (statesToExamine.size() != 0)
		{
			if (stopRequested)
			{
				return -1;
			}

			// Get and remove arbitrary state
			State one = statesToExamine.remove();

			// Don't fiddle with the initial state!
			if (one.isInitial())
			{
				continue loop;
			}

			// Examine this state
			ArcIterator it = one.incomingArcsIterator();
			Arc arc = null;
			if (it.hasNext())
			{
				arc = it.nextArc();
			}
			if (arc != null && !it.hasNext())
			{
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
						aut.removeArc(arc); // We can remove this one, it will become an epsilon self-loop!
						statesToExamine.remove(two);
						State mergeState = aut.mergeStates(two, one, useShortNames);
						countA++;
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
						continue loop; // Get next "one" from stack
					}
				}
			}
		}

		return countA;
	}

	/**
	 * Rule A.
	 *
	 * @return the number of states that have been removed or -1 if method didn't complete successfully.
	 */
	public int ruleAA(Automaton aut)
		throws Exception
	{
		////////////
		// RULE A //
		////////////

		// Count the removed states
		int countA = 0;

		// If there is at least one outgoing epsilon transition AND this is not the initial state AND
		// all incoming transitions are epsilon!
		// Copy the outgoing of this state to all the previous states, and remove this state!
		StateSet statesToExamine = new StateSet(aut.getStateSet());
		loop: while (statesToExamine.size() != 0)
		{
			if (stopRequested)
			{
				return -1;
			}

			// Get and remove arbitrary state
			State one = statesToExamine.remove();

			// Don't fiddle with the initial state!
			if (one.isInitial())
			{
				continue loop;
			}

			// Examine this state
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
			
			// Are all incoming epsilon?
			if (ok && (one.nbrOfIncomingArcs() == one.nbrOfIncomingEpsilonArcs()))
			{
				// "Copy" outgoing arcs from one to the previous states.
				for (ArcIterator inIt = one.incomingArcsIterator(); inIt.hasNext(); )
				{
					State fromState = inIt.nextArc().getFromState();
					
					for (ArcIterator outIt = one.outgoingArcsIterator(); outIt.hasNext(); )
					{
						Arc arc = outIt.nextArc();
						State toState = arc.getToState();
						LabeledEvent event = arc.getEvent();

						Arc newArc = new Arc(fromState, toState, event);
						aut.addArc(newArc);
					}
				}
				
				aut.removeState(one);
				countA++;
			}
		}

		return countA;
	}

	/**
	 * Rule B.
	 *
	 * @return the number of states that have been removed or -1 if method didn't complete successfully.
	 */
	public int ruleB(Automaton aut)
		throws Exception
	{
		////////////
		// RULE B //
		////////////

		// Count the removed states
		int countB = 0;

		// If two states has the same incoming arcs (from the same state(s) and with the same 
		// event(s)) AND if both states have the same enabled events, then they can be merged!
		boolean change = true;
		while (change)
		{
			if (stopRequested)
			{
				return -1;
			}

			change = false;

			// The marking can never be changed
			assert(ruleD(aut) == 0);

			/*
			HashSet infoHash = new HashSet(aut.nbrOfStates()*2+1);
			for (StateIterator it = aut.stateIterator(); it.hasNext(); )
			{
				State state = it.nextState();
				StateInfo info = new StateInfo(state);
				infoHash.add(info);
			}

			// In infoHash, the StateInfo is sorted in a way so that in the iterator, 
			// equivalent info will sit next to each other, NOOOOT!
			Iterator it = infoHash.iterator();
			assert(it.hasNext());
			StateInfo last = (StateInfo) it.next();
			while (it.hasNext())
			{
				StateInfo curr = (StateInfo) it.next();
				//logger.fatal(curr.toString());

				if (curr.getState().getName().equals("s140.s105") || curr.getState().getName().equals("s98.s62.s160"))
				{
					logger.fatal(curr.toString());
					logger.fatal(last.toString());
					logger.error("Equal: " + curr.equalCharacterization(last));
				}
				
				// If the two states have the same characterization, they can be merged!
				if (curr.equalCharacterization(last))
				{
					//logger.info("Merging " + last.getState() + " and " + curr.getState() + ".");
					State state = aut.mergeStates(last.getState(), curr.getState(), useShortNames);
					countB++;
					curr.setState(state);
					change = true;
				}

				last = null;
				last = curr;
			}
			*/

			Hashtable infoHash = new Hashtable(aut.nbrOfStates()*2+1);
			StateSet statesToExamine = new StateSet(aut.getStateSet());
			loop: while (statesToExamine.size() != 0)
			{
				State state = statesToExamine.remove();
				StateInfo info = new StateInfo(state);
				
				// Get list of states with same hashcode
				LinkedList list = (LinkedList) infoHash.get(new Integer(info.hashCode()));
				if (list == null)
				{
					list = new LinkedList();
					infoHash.put(new Integer(info.hashCode()), list);
					list.add(info);
					continue loop;
				}
				for (Iterator it = list.iterator(); it.hasNext(); )
				{
					StateInfo old = (StateInfo) it.next();
					if (info.equalCharacterization(old))
					{
						// Merge states!
						//logger.info("Merging " + old.getState() + " and " + info.getState() + ".");
						State merge = aut.mergeStates(old.getState(), info.getState(), useShortNames);
						countB++;
						old.setState(merge);
						change = true;						

						// There can be no more of this characterization in the list
						// Get a new state!
						continue loop;
					}
				}
				list.add(info);
			}
		}

		return countB;
	}

	/**
	 * Rule C.
	 *
	 * @return the number of states that have been removed or -1 if method didn't complete successfully.
	 */
	public int ruleC(Automaton aut)
		throws Exception
	{
		////////////////////////////
		// RULE C (not all cases) //
		////////////////////////////

		// Count the removed states
		int countC = 0;

		// Mark noncoreachable states with State.MAX_COST
		SynthesizerOptions synthOptions = SynthesizerOptions.getDefaultSynthesizerOptions();
		AutomatonSynthesizer synth = new AutomatonSynthesizer(aut, synthOptions);
		synth.doCoreachable();

		// Remove all outgoing arcs from these states
		for (Iterator it = aut.stateIterator(); it.hasNext(); )
		{
			if (stopRequested)
			{
				return -1;
			}

			State state = (State) it.next();
			if ((state.getCost() == State.MAX_COST))
			{
				// This state can not be acepting if the coreachability worked!
				assert(!state.isAccepting());

				// We will never want to propagate from here...
				// ... or from any state in the backwards epsilon closure!!
				StateSet statesToModify = new StateSet();
				statesToModify.add(state);
				while (statesToModify.size() != 0)
				{
					State currState = statesToModify.remove();

					// Accepting states that by epsilons may reach a block are modified to be 
					// nonaccepting an exception is made if this is the initial state!
					// We might as well exchange the below if-clause with 
					// only one statement, "currState.setAccepting(false);".
					if (currState.isAccepting())
					{
						if (currState.isInitial())
						{
							// We know that the system is blocking!
							logger.verbose("The system was found to be blocking.");
							
							// Do nothing...
							//continue;
						}

						currState.setAccepting(false);
					}

					// We won't want to propagate from here
					currState.removeOutgoingArcs();

					////////////////
					// RULE C.1-3 //
					////////////////

					// This stuff actually NEVER occured in the central lock example,
					// perhaps it's not much to hope for?
					// Follow all epsilon transitions backwards
					for (ArcIterator arcIt = currState.incomingArcsIterator(); arcIt.hasNext(); )
					{
						Arc arc = arcIt.nextArc();
						if (arc.getEvent().isEpsilon())
						{
							logger.debug("Rule C.3 came to use.");
							statesToModify.add(arc.getFromState());
						}
						else 
						{
							State previous = arc.getFromState();
							ArcIterator outIt = previous.outgoingArcsIterator();
							boolean fail = false;
							LinkedList toBeRemoved = new LinkedList();
							while (outIt.hasNext())
							{
							 	Arc currArc = outIt.nextArc();
							 	
							 	if (!currArc.getToState().equals(currState))
							 	{									
 									if (currArc.getEvent().equals(arc.getEvent()))
 									{
 										logger.debug("Rule C.1 came to use.");
 										// The arc "currArc" can be removed
 										toBeRemoved.add(currArc);
									}
									else
									{
										fail = true;
									}
								}
							}
							while (toBeRemoved.size() != 0)
							{
								aut.removeArc((Arc) toBeRemoved.remove(0));
							}
							if (!fail)
							{
								logger.debug("Rule C.2 came to use.");
								statesToModify.add(previous);								
							}							
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
		// After the above there may be nonreachable parts... make reachable!
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
			State remove = (State) toBeRemoved.remove(0);
			if (debug)
			{
				logger.fatal("Nonreachable state: " + remove);
			}

			aut.removeState(remove);
			countC++;
		}

		return countC;
	}


	/**
	 * Rule D.
	 *
	 * All states that can reach marked states by epsilon events are also considered marked.
	 * 
	 * @return the number of states that have been marked or -1 if the method didn't complete successfully.
	 */
	//public int adjustMarking(Automaton aut)
	public int ruleD(Automaton aut)
	{
		////////////
		// RULE D //
		////////////
		
		// Count the states that get marked
		int countD = 0;

		if (aut == null || stopRequested)
		{
			return -1;
		}

		/*
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
		*/

		// States that can reach marked states by epsilon events only can be considered marked
		// First find all the marked states, then adjust the states in theit epsilonclosures
		StateSet markedStates = new StateSet();
		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
		{
			State currState = stateIt.nextState();
			if (currState.isAccepting())
			{
				markedStates.add(currState);
			}
		}

		// Adjust the marking!
		while (markedStates.size() != 0)
		{
			StateSet closure = markedStates.remove().backwardsEpsilonClosure();
			for (StateIterator closureIt = closure.iterator(); closureIt.hasNext(); )
			{
				State state = closureIt.nextState();
				if (!state.isAccepting())
				{
					state.setAccepting(true);
					countD++;
				}
			}
		}
		
		return countD;
	}

	/**
	 * Rule F.
	 *
	 * @return the number of states that have been removed or -1 if the method didn't complete successfully.
	 */
	public int ruleF(Automaton aut)
		throws Exception
	{
		StateSet statesToExamine = new StateSet(aut.getStateSet());

		////////////
		// RULE F //
		////////////

		// Count the removed states
		int countF = 0;

		// States that only have epsilon events as outgoing can be bypassed!
		loop: while (statesToExamine.size() != 0)
		{
			if (stopRequested)
			{
				return -1;
			}
			
			State state = statesToExamine.remove();
			//logger.info(state);
			//logger.info(statesToExamine);
	
			// Don't remove the initial state!
			if (state.isInitial())
			{
				continue loop;
			}

			// If this state is marked, we need to make sure that at least one of the
			// following states is marked!
			boolean isMarked = state.isAccepting();
			boolean hasMarkedDecessor = false;
			 
			// All outgoing (at least one!) must be epsilon (and maybe have a marked toState)
			for (ArcIterator outIt = state.outgoingArcsIterator(); outIt.hasNext(); )
			{
				Arc arc = outIt.nextArc();
				if (!arc.getEvent().isEpsilon())
				{
					continue loop;
				}
				hasMarkedDecessor |= arc.getToState().isAccepting();
			}

			// So, at least one outgoing and if "state" is marked, there must be a marked decessor
			if ((state.outgoingArcsIterator().hasNext()) && (!isMarked || hasMarkedDecessor))
			{
				int arcCount = 0;
				for (ArcIterator outIt = state.outgoingArcsIterator(); outIt.hasNext(); )
				{
					Arc outArc = outIt.nextArc();
					
					for (ArcIterator inIt = state.incomingArcsIterator(); inIt.hasNext(); )
					{
						Arc inArc = inIt.nextArc();
						Arc newArc = new Arc(inArc.getFromState(), outArc.getToState(), inArc.getEvent());
						
						aut.addArc(newArc);
						arcCount++;
					}
					
					// BUG! There is a problem here, since when using the State.setName method, all
					// StateSet:s that the state is involved in would need to be rebuilt since the 
					// hashCode of the state is changed... not gooooood...
					//   On the other hand... maybe we shouldn't give the state a new name at all? 
					// After all, we don't merge states, we just remove a state.
					if (!useShortNames && false)
					{
						State toState = outArc.getToState();
						toState.setName(state.getName() + SupremicaProperties.getStateSeparator() + 
										toState.getName());
					}
				}
				
				aut.removeState(state);
				//logger.error("Removed state " + state + ", added " + arcCount + " arcs.");
				countF++;
			}
			
			/*
			// All outgoing (at least one!) must be epsilon, i.e. there should be no events in the alphabet
			// of enabled events if we don't consider the epsilon closure...
			if ((state.outgoingArcsIterator().hasNext()) && (state.enabledEvents(false).size() == 0))
			{
				for (ArcIterator outIt = state.outgoingArcsIterator(); outIt.hasNext(); )
				{
					Arc outArc = outIt.nextArc();
					
					for (ArcIterator inIt = state.incomingArcsIterator(); inIt.hasNext(); )
					{
						Arc inArc = inIt.nextArc();
						Arc newArc = new Arc(inArc.getFromState(), outArc.getToState(), inArc.getEvent());

						aut.addArc(newArc);
					}					
				}
				
				aut.removeState(state);
				countF++;
			}
			*/
		}

		return countF;
	}

	public static void printTotal()
	{
		logger.warn("Totally: A: " + totalA + ", B: " + totalB + ", C: " + totalC + ", D: " + totalD + ", F: " + totalF + ", OE: " + totalOE + ", Arcs: " + totalArcs + ".");
	}
	public static void resetTotal()
	{
		totalA = 0;
		totalB = 0;
		totalC = 0;
		totalD = 0;
		totalF = 0;
		totalOE = 0;
		totalArcs = 0;
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
			StateSet closure = currState.epsilonClosure(true);
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
				arcLoop: for (ArcIterator arcIt = closureIt.nextState().outgoingArcsIterator(); arcIt.hasNext(); )
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
						continue arcLoop;
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
	 * Algorithm inspired by "Minimizing the Number of Transitions with Respect to Observation 
	 * Equivalence" by Jaana Eloranta. Removes all transitions that are redundant.
	 *
	 * @return the number of arcs that have been removed.
	 */
	private int removeRedundantTransitions(Automaton aut)
	{
		// Are there any silent-self-loops? They can be removed!
		// Note! These are not "redundant" by Jaana Elorantas definition, but must be
		// removed before removing her "redundant transitions" (see below).

		// Count the removed arcs
		int count = 0;

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
			count++;
		}

		// Put redundant arcs in list, remove after all have been found
		toBeRemoved.clear();
		for (ArcIterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();
			LabeledEvent event = arc.getEvent();

			// Using Elorantas notation... (s1, s2 and (later) s3)
			State s1 = arc.getFromState();
			State s2 = arc.getToState();

			// Is the criteria fulfilled? (I.e. does there exist a s3 such that either
			// (s1 -a-> s3 and s3 -tau-> s2) or (s1 -tau-> s3 and s3 -a-> s2) holds?)
			test: for (ArcIterator outIt = s1.outgoingArcsIterator(); outIt.hasNext(); )
			{
				Arc firstArc = outIt.nextArc();
				LabeledEvent firstEvent = firstArc.getEvent();

				if (firstEvent.isEpsilon() || firstEvent.equals(event))
				{
					State s3 = firstArc.getToState();

					for (ArcIterator inIt = s2.incomingArcsIterator(); inIt.hasNext(); )
					{
						if (stopRequested)
						{
							return -1;
						}
						
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
		/*
		// Put redundant arcs in list, remove after all have been found
		toBeRemoved.clear();
		int i=0;
		arcLoop: for (ArcIterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
		{
			logger.info("" + ++i);
			Arc arc = arcIt.nextArc();

			// Using Elorantas notation... (a, s1, s2 and (not exactly) s3)... but not her method...
			LabeledEvent a = arc.getEvent();
			State s1 = arc.getFromState();
			State s2 = arc.getToState();
				
			// So there does exist an arc (s1 -a-> s2)... now, check if there is ANOTHER way of 
			// getting to s2. I.e. (s1 -a-> s2) can be removed if (s1 -tau*.a.tau*-> s2) with at 
			// least one tau in either of the tau*-expressions. Remember that there at this point 
			// should be no tau-loops in this system!				

			// Yhe string tau*.a.tau* must begin with either a or tau.
			
			// First assume it starts with tau
			//StateSet s3set = s1.nextStates(tau, false);
			StateSet s3set = s1.epsilonClosure(false);
			assert(!s3set.contains(s1)); // There should be no loops!
			// Now if s2 can be reached from s3set using at least one a, the arc can be removed!
			StateSet set;
			if (a.isEpsilon())
			{
				set = s3set.epsilonClosure(false);
			}
			else
			{
				set = s3set.nextStates(a, true);
			}
			if (set.contains(s2))
			{
				toBeRemoved.add(arc);
				continue arcLoop;
			}
			// If a is epsilon, we will do the same thing all over again... so skip it!
			if (a.isEpsilon())
			{
				continue arcLoop;
			}
			// Now the same again, but assuming it starts with an a
			s3set = s1.nextStates(a, false);
			// Now if s2 can be reached using at least one tau, the arc can be removed!
			//if (s3set.nextStates(tau, true).contains(s2))
			if (s3set.epsilonClosure(false).contains(s2))
			{
				toBeRemoved.add(arc);
				continue arcLoop;
			}
		}
		*/
		while (toBeRemoved.size() > 0)
		{
			aut.removeArc((Arc) toBeRemoved.remove(0));
			count++;
		}

		return count;
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
	 * Sets the executionDialog of this AutomatonMinimizer. If executionDialog is null,
	 * the dialog is not updated.
	 */
	public void setExecutionDialog(ExecutionDialog executionDialog)
	{
		this.executionDialog = executionDialog;
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

/**
 * Class for characterizing states. For use with the "B-rule".
 */
class StateInfo
{
	private State state;
	private Alphabet enabledEvents;
	private Arclets incomingArcs;
	private boolean isInitial;
	private boolean isMarked;
	
	private int hash = 0;

	public StateInfo(State state)
	{
		this.state = state;
		enabledEvents = state.enabledEvents(true);
		StateSet backwardsClosure = state.backwardsEpsilonClosure(); 
		incomingArcs = new Arclets(backwardsClosure.incomingArcsIterator());
		isInitial = backwardsClosure.hasInitialState();
		isMarked = state.isAccepting();
	}	

	public void setState(State state)
	{
		this.state = state;
	}

	public State getState()
	{
		return state;
	}

	public boolean equalCharacterization(StateInfo other)
	{
		boolean result = true;
		result &= enabledEvents.equals(other.enabledEvents);
		result &= incomingArcs.equals(other.incomingArcs);
		result &= isInitial == other.isInitial;
		result &= isMarked == other.isMarked;
		
		return result;
	}

	public boolean equals(Object object)
	{
		StateInfo other = (StateInfo) object;

		return state.equals(other.state);
		//return state.getName().equals(other.state.getName());
	}

	public int hashCode()
	{
		if (hash != 0)
		{
			return hash;
		}

		hash = enabledEvents.hashCode();
		hash = hash * 31 + incomingArcs.hashCode();
		hash = hash * 23 + (isInitial ? 1231 : 1237);
		hash = hash * 17 + (isMarked ? 1231 : 1237);

		return hash;
	}

	public String toString()
	{
		String result = "";
		result += "State: " + state;
		result += "\n\tEnabled: " + enabledEvents;
		result += "\n\tArclets: " + incomingArcs;
		result += "\n\tInitial: " + isInitial;
		result += "\n\tMarked : " + isMarked;
		return result;
	}

	/**
	 * Class describing "half" arcs, i.e. only one state and the event.
	 */
	class Arclet
	{
		private State state;
		private LabeledEvent event; 

		public Arclet(Arc arc)
		{
			state = arc.getFromState();
			event = arc.getEvent();
		}

		public boolean equals(Object obj)
		{
			Arclet other = (Arclet) obj;

			return (state.equals(other.state) && event.equals(other.event));
		}

		public int hashCode()
		{
			return (17*state.hashCode())*event.hashCode();
		}

		public String toString()
		{
			return "<" + state.getName() + ", " + event.getLabel() + ">";
		}
	}

	class Arclets 
	{
		// Why can't I have a TreeSet here!?!?!?
		private TreeMap arclets = new TreeMap();
		//private TreeSet arclets = new TreeSet();

		public Arclets(ArcIterator arcIterator)
		{
			while (arcIterator.hasNext())
			{
				Arc arc = arcIterator.nextArc();

				// Ignore epsilon events
				if (!arc.getEvent().isEpsilon())
				{
					Arclet arclet = new Arclet(arc);
					arclets.put("" + arclet.hashCode(), arclet);
					//arclets.add(arclet);
				}
			}
		}

		public boolean equals(Object obj)
		{		
			return arclets.equals(((Arclets) obj).arclets);
		}

		public int hashCode()
		{
			return arclets.hashCode();
		}

		public String toString()
		{
			String result = "";

			//Iterator it = arclets.iterator();
			Iterator it = arclets.values().iterator();
			while (it.hasNext())
			{
				result += (Arclet) it.next();
				if (it.hasNext())
				{
					result += ", ";
				}
			}
	   		
			return result;
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
 * @author  ka
 * @since  November 28, 2001
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
