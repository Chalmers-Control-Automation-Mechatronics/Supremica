
/********************** Determinizer,java ******************/

// Makes a determinization through the subset construction method
// See vanNoord (2000), Fig 1
// There are two ways of calling Determinizer depending on how the epsilons should be specified
// With no 'epsilons' alphabet, each event is queried whether it isEpsilon or not
// With an 'epsilons' alphabet, the events of 'epsilons' are considered to be...well...epsilons
package org.supremica.automata.algorithms.standard;

import org.supremica.log.*;
import java.util.Iterator;
import org.supremica.automata.*;

//
public class Determinizer

// extends InterruptableAlgorithm
{
	private static Logger logger = LoggerFactory.createLogger(Determinizer.class);
	private Automaton automaton;
	private Automaton newAutomaton = null;

	// private Alphabet epsilons = null;
	private EpsilonTester epsilonTester = null;
	private StateSets openStateSets = new StateSets();
	private StateSets closedStateSets = new StateSets();
	private StateSet openStates = new StateSet();
	private StateSet closedStates = new StateSet();
	private boolean checkControlInconsistencies = false;
	private boolean resolveControlInconsistencies = false;
	private boolean isControlInconsistent = false;

	// Debug stuff
	private int tabs = 0;    // keeps track on num tabs to insert, for formatting output

	/**
	 * For this automaton, determinize with respect to the events marked as isEpsilon()
	 */
	public Determinizer(Automaton automaton)
	{
		this.automaton = automaton;
		this.epsilonTester = new DefaultEpsilonTester();
		this.newAutomaton = createNewAutomaton();
	}

	/**
	 * For this automaton, determinize with respect to the given events
	 * If contains == true, the events are considered to be epsilons
	 * If contains == false, the events are considered as non-epsilons (and all other epsilons)
	 */
	public Determinizer(Automaton automaton, Alphabet events, boolean contains)
	{
		this.automaton = automaton;
		this.epsilonTester = new AlphaEpsilonTester(events, contains);
		this.newAutomaton = createNewAutomaton();
	}

	/**
	 * Determinize using a supplied epsilonTester for deciding which (and when...) events are
	 * considered to be epsilons.
	 */
	public Determinizer(Automaton automaton, EpsilonTester epsilonTester)
	{
		this.automaton = automaton;
		this.epsilonTester = epsilonTester;
		this.newAutomaton = createNewAutomaton();
	}

	public void checkControlInconsistencies(boolean checkControlInconsistencies)
	{
		this.checkControlInconsistencies = checkControlInconsistencies;
	}

	public void resolveControlInconsistencies(boolean resolveControlInconsistencies)
	{
		this.resolveControlInconsistencies = resolveControlInconsistencies;
	}

	public boolean isControlInconsistent()
		throws IllegalStateException
	{
		if (checkControlInconsistencies || resolveControlInconsistencies)
		{
			return isControlInconsistent;
		}
		else
		{
			throw new IllegalStateException("Only allowed to be called when checkControlInconsistencies or resolvedControlInconsistencies are set to true");
		}
	}

	public void execute()
	{
		// Clear the caches - is this necessary?
		for (Iterator state_it = automaton.stateIterator();
				state_it.hasNext(); )
		{
			State state = (State) state_it.next();

			state.setStateSet(null);
		}

		logger.debug("Executing Determinizer(" + automaton.getName() + ") " + epsilonTester.showWhatYouGot());

		/* This is how it goes - a variant of van Noords

		let Qinit = epsilonClosure(initialState)
		put Qinit on openStates

		while openStates is not empty
		{
			get Q1 from openStates

			for all non-epsilon events e
			{
			    let Q2 = eventClosure(Q1, e) [the state set Q2 reached from Q1 via e, one-step]
				let Q2c = epsilonClosure(Q2)
				if(Q2c != empty)
				{
					add Q2c to openStates
					add arc <Q1, e, Q2c>
				}
			}

			put Q1 on closedStates
		}

		and that's how it goes */

		// This should be the one and only initial state!
		StateSet initset = epsilonClosure(automaton.getInitialState());

		// put it on openStateSets (if not already seen)
		add(initset);

		// Create initial state
		State init = initset.getSingleStateRepresentation();
		newAutomaton.addState(init);
		newAutomaton.setInitialState(init);

		// Loop as long as there are StateSet:s in openStateSets
		while (!openStateSets.isEmpty())
		{
			// Note that the getSingleStateRepresentation call ALWAYS returns a new representation
			// i.e. (if you look at the implementation ni StateSet) it never reuses the same
			// representation, the local singleStateRepresentation is never null!! (I think.)

			StateSet Q1 = openStateSets.get();    // This is vanNoords T
			State T = newAutomaton.addStateChecked(Q1.getSingleStateRepresentation());

			// for each event not to be disregarded, calc the closure, create the arc
			/* Alphabet */
			Iterator it = automaton.getAlphabet().iterator();
			while (it.hasNext())
			{
				LabeledEvent e = (LabeledEvent) it.next();

				if (!epsilonTester.isThisEpsilon(e))
				{
					// From this set, via this event, calc the reached state
					StateSet Q2 = eventClosure(Q1, e);    // this is vanNoords U
					StateSet Q2c = epsilonClosure(Q2);

					if (!Q2c.isEmpty())
					{
						if (checkControlInconsistencies)
						{
							Alphabet inconsistentEvents = automaton.getControlInconsistentEvents(Q2c);

							if (inconsistentEvents.size() > 0)
							{
								logger.debug("In states " + Q2c.toString() + " the following events are inconsistent " + inconsistentEvents.toString());

								isControlInconsistent = true;
							}
						}

						if (resolveControlInconsistencies)
						{
							Alphabet inconsistentEvents = automaton.resolveControlInconsistencies(Q2c);

							if (inconsistentEvents.size() > 0)
							{
								logger.debug("In states " + Q2c.toString() + " the following events are inconsistent (resolved) " + inconsistentEvents.toString());

								isControlInconsistent = true;
							}
						}

						add(Q2c);

						State U = newAutomaton.addStateChecked(Q2c.getSingleStateRepresentation());

						newAutomaton.addArc(new Arc(T, U, e));
					}
				}
			}

			openStateSets.remove(Q1);
			closedStateSets.add(Q1);    // "mark" this set as done
		}

		// Give the new automaton an appropriate comment
		if (automaton.getName() != "")
		{
			newAutomaton.setComment("detm(" + automaton.getName() + ")");
		}
		else
		{
			newAutomaton.setComment("detm(" + automaton.getComment() + ")");
		}
	}

	public Automaton getNewAutomaton()
	{
		return newAutomaton;
	}

	private Automaton createNewAutomaton()
	{
		Automaton aut = new Automaton();
		aut.setType(automaton.getType());

		// Add all events except the epsilons
		Iterator alphait = automaton.getAlphabet().iterator();
		while (alphait.hasNext())
		{
			LabeledEvent event = (LabeledEvent) alphait.next();

			if (!epsilonTester.isThisEpsilon(event))
			{
				try    // we know it can't throw, still we have to do this
				{
					aut.getAlphabet().addEvent(event);
				}
				catch (Exception excp)
				{
					logger.error(excp);
					logger.debug(excp.getStackTrace());
				}
			}
		}

		return aut;
	}

	private void add(StateSet states)
	{
		if (!openStateSets.contains(states) &&!closedStateSets.contains(states))
		{
			// printTabs();
			// System.out.println("adding " + states.toString());
			openStateSets.add(states);

			// never mind marking and forbidden for now
		}
	}

	// Calc the (one-step) event "closure" for this event from these states
	// Note that the original states are not necessarily included
	private StateSet eventClosure(StateSet states, LabeledEvent event)
	{
		debugPrint("(evCSsE) eventClosure(" + states.toString() + ", " + event.getLabel() + ")", true);

		StateSet closure = new StateSet();

		// System.out.println("(evCSsE) closure instantiated");
		/* State */
		Iterator stateit = states.iterator();

		// System.out.println("(evCSsE) stateit created");
		while (stateit.hasNext())
		{
			// System.out.println("stateit hasNext(), yes");
			State state = (State) stateit.next();

			// System.out.println("(evCSsE) Calling eventTransition(" + state.getName() + ", " + event.getLabel() + ")");
			StateSet ss = eventTransition(state, event);

			// System.out.println("(evCSsE) Performing the union");
			closure.union(ss);
		}

		debugPrint("(evCSsE) return closure " + closure.toString() + ")", false);

		return closure;
	}

	// For this state, calc the states reached in one step on the given events
	// Note that the original state is _not_necessarily_ included (no implicit selfloops, that is)
	private StateSet eventTransition(State state, Alphabet alpha)
	{
		debugPrint("(evTSA) eventTransition(" + state.getName() + ", " + alpha.toString() + ")", true);

		StateSet stateset = new StateSet();
		/* LabeledEvent */
		Iterator eventit = alpha.iterator();

		while (eventit.hasNext())
		{
			stateset.union(eventTransition(state, (LabeledEvent) eventit.next()));
		}

		debugPrint("(evTSA) return " + stateset.toString() + ")", false);

		return stateset;
	}

	// For this specific state, calc the states (might be more than one!) reached via this event
	// Note that the original state is _not_necessarily_ included (no implicit selfloops, that is)
	private StateSet eventTransition(State state, LabeledEvent event)
	{
		debugPrint("(evTSE) eventTransition(" + state.getName() + ", " + event.getLabel() + ")", true);

		StateSet stateset = new StateSet();

		for (Iterator<Arc> it = state.outgoingArcsIterator(); it.hasNext(); )
		{
			Arc arc = it.next();

			if (arc.getEvent().equals(event))
			{
				stateset.add(arc.getToState());

				// printTabs();
				// System.out.println("(eTSE) Added state " + arc.getToState().getName() + ", stateset = " + stateset.toString());
			}
		}

		debugPrint("(evTSE) return " + stateset.toString() + ")", false);

		return stateset;
	}

	// Calc the epsilon closure for this set of states
	// For each state, calc its closure and return the union of the lot
	private StateSet epsilonClosure(StateSet states)
	{
		debugPrint("(eCSS) epsilonClosure(" + states.toString() + ")", true);

		StateSet closure = new StateSet();

		/* StateSet. */
		for (Iterator<State> stateit = states.iterator(); stateit.hasNext(); )
		{
			closure.union(epsilonClosure(stateit.next()));
		}

		debugPrint("(eCSS) return closure" + closure.toString() + ")", false);

		return closure;
	}

	/**
	 * Calc the epsilon closure for this particular state
	 */
	public StateSet epsilonClosure(State state)
	{
		debugPrint("(eCS) epsilonClosure(" + state.getName() + ")", true);

		if (state.getStateSet() != null)    // if already calculated and cached, return it
		{
			StateSet closure = (StateSet) state.getStateSet();

			debugPrint("(eCS) return cached closure" + closure.toString() + ")", false);

			return (StateSet) state.getStateSet();
		}

		StateSet closure = new StateSet();
		StateSet tempset = new StateSet();

		// The original state is always included in the closure
		closure.add(state);

		// And we begin calculating from that state
		tempset.add(state);

		// No states are ever removed, so we can use the size to determine when fixpoint reached
		int nextsize = closure.size();    // we use the size to determine fixpoint
		int prevsize = 0;    // no states are removed, so same size means we're finished
		if (prevsize != nextsize)
		{
			do
			{
				prevsize = nextsize;
				tempset = epsilonTransition(tempset);    // call eTSS

				closure.union(tempset);

				nextsize = closure.size();
			}
			while (prevsize != nextsize);
		}

		state.setStateSet(closure);    // cache the result
		debugPrint("(eCS) return closure" + closure.toString() + ")", false);

		return closure;
	}

	// Calc the one-step epsilon transitions for this set of states
	// Note that the original states are _not_necessarily_ included,
	// That is, there are no implicit epilon moves to the same state,
	// this has to be taken care of above this level
	private StateSet epsilonTransition(StateSet states)
	{
		debugPrint("(eTSS) epsilonTransition(" + states.toString() + ")", true);

		StateSet stateset = new StateSet();    // empty set

		// Iterate over the states and create the union of their respective closures
		for (Iterator<State> it = states.iterator(); it.hasNext(); )
		{
			stateset.union(epsilonTransition(it.next()));
		}

		debugPrint("(eTSS) return " + stateset.toString() + ")", false);

		return stateset;
	}

	// calc the one-step epsilon transitions for this specific state
	private StateSet epsilonTransition(State state)
	{
		debugPrint("(eTS) epsilonTransition(" + state.getName() + ")", true);

		StateSet stateset = new StateSet();

		for (Iterator<Arc> it = state.outgoingArcsIterator(); it.hasNext(); )
		{
			try
			{
				Arc arc = it.next();

				if (epsilonTester.isThisEpsilon(arc.getEvent()))    // automaton.getEvent(arc).isEpsilon())
				{
					stateset.add(arc.getToState());
				}
			}
			// Note this is a (ugly?) workaround, since I know that getEvent _cannot_ throw above
			// I just got the arc from a state of the automaton. Thus, getEvent must be able to
			// perform correctly, else the automaton is broken and we are lost anyway.
			catch (Exception excp)
			{
				logger.debug(excp.getStackTrace());

				throw new RuntimeException(excp);
			}
		}

		debugPrint("(eTS) return " + stateset.toString() + ")", false);

		return stateset;
	}

	// Walk the state of the automaton, and null out its stateclass (helps the GC)
	public void cleanUp()
	{
		for (Iterator<State> stateit = automaton.stateIterator();
				stateit.hasNext(); )
		{
			State state = stateit.next();

			state.setStateSet(null);
		}
	}

	// Walk the state of the automaton, and null out its stateclass (helps the GC)
	public void cleanUpDebug()
	{
		/* State */
		for (Iterator stateit = automaton.stateIterator(); stateit.hasNext(); )
		{
			State state = (State) stateit.next();

			// System.out.print(state.getName());
			StateSet stateset = (StateSet) state.getStateSet();

			if (stateset != null)
			{
				System.out.println(": " + stateset.toString());
			}
			else
			{
				System.out.println(": <null state class>");
			}

			state.setStateSet(null);
		}
	}

	// To test stuff, no GUI
	public static void main(String[] args)
	{
		logger.setLogToConsole(true);

		Automaton automaton = new Automaton("Determinizer test");
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
		q5.setAccepting(true);

		LabeledEvent a = new LabeledEvent("a");

		automaton.getAlphabet().addEvent(a);

		LabeledEvent b = new LabeledEvent("b");

		automaton.getAlphabet().addEvent(b);
		b.setEpsilon(true);

		LabeledEvent c = new LabeledEvent("c");

		automaton.getAlphabet().addEvent(c);
		c.setEpsilon(true);

		LabeledEvent d = new LabeledEvent("d");

		automaton.getAlphabet().addEvent(d);

		Alphabet epsilons = new Alphabet();

		epsilons.addEvent(b);
		epsilons.addEvent(c);
		automaton.addArc(new Arc(q0, q1, a));
		automaton.addArc(new Arc(q0, q1, b));
		automaton.addArc(new Arc(q1, q2, b));
		automaton.addArc(new Arc(q1, q3, c));
		automaton.addArc(new Arc(q2, q4, d));
		automaton.addArc(new Arc(q3, q5, d));

/*
				automaton.addArc(new Arc(q0, q0, a));
				automaton.addArc(new Arc(q0, q1, c));
				automaton.addArc(new Arc(q1, q2, a));

*/
		Determinizer detm = new Determinizer(automaton);

		detm.execute();
		detm.cleanUpDebug();

		org.supremica.automata.IO.AutomatonToDsx todsx = new org.supremica.automata.IO.AutomatonToDsx(detm.getNewAutomaton());

		try
		{
			todsx.serialize(new java.io.PrintWriter(System.out));
		}
		catch (Exception excp)
		{
			logger.error(excp);
			logger.debug(excp.getStackTrace());
		}
	}

	private void printTabs()
	{

		//      for(int i = 0; i < tabs; ++i)
		//              System.out.print("\t");
	}

	private void debugPrint(String str, boolean enter)
	{

		/*      if(enter)
						++tabs;

				printTabs();
				System.out.println(str);

				if(!enter)
						--tabs;
		*/

		//logger.debug(str);
	}

	// end debug stuff
}
