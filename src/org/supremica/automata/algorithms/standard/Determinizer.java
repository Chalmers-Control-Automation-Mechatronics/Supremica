/********************** Determinizer,java ******************/
// Makes a determinization through the subset construction method
// See vanNoord (2000), Fig 1
// There are two ways of calling Determinizer depending on how the epsilons should be specified
// With no 'epsilons' alphabet, each event is queried whether it isEpsilon or not
// With an 'epsilons' alphabet, the events of 'epsilons' are considered to be...well...epsilons

package org.supremica.automata.algorithms.standard;

import org.supremica.log.*;

import java.util.HashSet;
import java.util.Iterator;

import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Automaton;
import org.supremica.automata.StateSet;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Arc;

// Would this have any public use?
class SetOfStateSets
{
	private HashSet theSet = null;
	
	// Private constructor for cloning
	private SetOfStateSets(HashSet hashset)
	{
		this.theSet = new HashSet(hashset);
	}
	
	// Create an empty set
	public SetOfStateSets()
	{
		theSet = new HashSet();
	}

	// Shallow copy (should it be deep?)
	public SetOfStateSets(SetOfStateSets ss)
	{
		this(ss.theSet);
	}

	public static SetOfStateSets union(SetOfStateSets s1, SetOfStateSets s2)
	{
		SetOfStateSets ss = new SetOfStateSets(s1);
		ss.union(s2);
		return ss;
	}

	public static SetOfStateSets intersect(SetOfStateSets s1, SetOfStateSets s2)
	{
		SetOfStateSets ss = new SetOfStateSets(s1);
		ss.intersect(s2);
		return ss;
	}

	// Make me the union of myself and s2
	public void union(SetOfStateSets s2)
	{
		theSet.addAll(s2.theSet);
	}

	// Make me the intersection of myself and s2
	public void intersect(SetOfStateSets s2)
	{
		theSet.retainAll(s2.theSet);
	}

	public boolean add(StateSet states)
	{
		return theSet.add(states);
	}

	public void clear()
	{
		theSet.clear();
	}

	// Shallow copy (is that what we mean by clone, really?)
	public Object clone()
	{
		return new SetOfStateSets(((HashSet)theSet.clone()));
	}

	public boolean contains(StateSet states)
	{
		return theSet.contains(states);
	}

	public boolean isEmpty()
	{
		return theSet.isEmpty();
	}

	public Iterator iterator()
	{
		return theSet.iterator();
	}

	public boolean remove(StateSet states)
	{
		return theSet.remove(states);
	}

	public int size()
	{
		return theSet.size();
	}
	
	// Return an arbitrary element. Note, assumes that at least one exists
	public StateSet get()
	{
		return (StateSet)iterator().next();
	}	
}
// Here we determine whether the passed event is epsilon or not
// Depending on the way we want to interpret epsilon, instantiate diferent objects
interface EpsilonTester
{
	public boolean isThisEpsilon(LabeledEvent event);
}
//
class DefaultEpsilonTester
	implements EpsilonTester
{
	public boolean isThisEpsilon(LabeledEvent event)
	{
		return event.isEpsilon();
	}
}
//
class AlphaEpsilonTester
	implements EpsilonTester
{
	Alphabet epsilons;
	
	public AlphaEpsilonTester(Alphabet epsilons)
	{
		this.epsilons = epsilons;
	}
	public boolean isThisEpsilon(LabeledEvent event)
	{
		return epsilons.contains(event);
	}
}
//
public class Determinizer
// extends InterruptableAlgorithm
{
	private static Logger logger = LoggerFactory.createLogger(Determinizer.class);
	
	private Automaton automaton;
	private Automaton newAutomaton = null;
	// private Alphabet epsilons = null;
	private EpsilonTester epsilonTester = null;
	private SetOfStateSets openStateSets = new SetOfStateSets();
	private SetOfStateSets closedStateSets = new SetOfStateSets();
	
	private StateSet openStates = new StateSet();
	private StateSet closedStates = new StateSet();

	// Debug stuff
	private int tabs = 0; // keeps track on num tabs to insert, for formatting output
	private void printTabs()
	{
	//	for(int i = 0; i < tabs; ++i)
	//		System.out.print("\t");
	}
	private void debugPrint(String str, boolean enter)
	{
	/*	if(enter)
			++tabs;
		
		printTabs();
		System.out.println(str);

		if(!enter)
			--tabs;
	*/
		logger.debug(str);
	}
	// end debug stuff
	
	public Determinizer(Automaton automaton)
	{
		this.automaton = automaton;
		this.epsilonTester = new DefaultEpsilonTester();
		this.newAutomaton = createNewAutomaton();
	}

	public Determinizer(Automaton automaton, Alphabet epsilons)
	{
		this.automaton = automaton;
		this.epsilonTester = new AlphaEpsilonTester(epsilons);
		this.newAutomaton = createNewAutomaton();
	}
	
	public void execute()
	{
		logger.debug("Executing Determinizer(" + automaton.getName() + ")");

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
		
		StateSet initset = epsilonClosure(automaton.getInitialState()); // This should be the one and only initial state!
		add(initset);
		State init = initset.createNewState();
		newAutomaton.addState(init);
		newAutomaton.setInitialState(init);
		
		while(!openStateSets.isEmpty())
		{
			StateSet Q1 = openStateSets.get(); // This is vanNoords T
			State T = newAutomaton.addStateChecked(Q1.createNewState());
			
			// for each event not to be disregarded, calc the closure, create the arc
			/* Alphabet */ Iterator it = automaton.getAlphabet().iterator();
			while(it.hasNext())
			{
				LabeledEvent e = (LabeledEvent)it.next();
				if(!epsilonTester.isThisEpsilon(e))
				{
					// From this set, via this event, calc the reached state
					StateSet Q2 = eventClosure(Q1, e);	// this is vanNoords U
					StateSet Q2c = epsilonClosure(Q2);
					if(!Q2c.isEmpty())
					{
						add(Q2c);
						State U = newAutomaton.addStateChecked(Q2c.createNewState());
						newAutomaton.addArc(new Arc(T, U, e));
					}
				}
			}
			
			openStateSets.remove(Q1);
			closedStateSets.add(Q1); // "mark" this set as done
		}	
	}
	
	public Automaton getNewAutomaton()
	{
		return newAutomaton;
	}
	
	private Automaton createNewAutomaton()
	{
		Automaton newautomaton = new Automaton();
		// Add all events except the epsilons
		Iterator alphait = automaton.getAlphabet().iterator();
		while(alphait.hasNext())
		{
			LabeledEvent event = (LabeledEvent)alphait.next();
			if(!epsilonTester.isThisEpsilon(event))
			{
				try // we know it can't throw, still we have to do this
				{
					newautomaton.getAlphabet().addEvent(event);
				}
				catch(Exception excp)
				{
					logger.error(excp);
				}
			}
		}
		return newautomaton;
	}
	
	private void add(StateSet states)
	{
		if(!openStateSets.contains(states) && !closedStateSets.contains(states))
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
		
		/* State */ Iterator stateit = states.iterator();
		// System.out.println("(evCSsE) stateit created");
		
		while(stateit.hasNext())
		{
			// System.out.println("stateit hasNext(), yes");
			State state = (State)stateit.next();
			
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
		/* LabeledEvent */ Iterator eventit = alpha.iterator();
		while(eventit.hasNext())
		{
			stateset.union(eventTransition(state, (LabeledEvent)eventit.next()));
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
		/* Arc */ Iterator it = state.outgoingArcsIterator();
		while(it.hasNext())
		{
			Arc arc = (Arc)it.next();
			try
			{
				if(automaton.getEvent(arc).isEqual(event))	// Can getEvent _really_ throw here?
				{
					stateset.add(arc.getToState());
					// printTabs();
					// System.out.println("(eTSE) Added state " + arc.getToState().getName() + ", stateset = " + stateset.toString());
				}
			}
			// Note this is a (ugly?) workaround, since I know that getEvent _cannot_ throw above
			// I just got the arc from a state of the automaton. Thus, getEvent must be able to 
			// perform correctly, else the automaton is broken and we are lost anyway.
			catch(Exception excp)
			{
				throw new RuntimeException(excp);
			}
		}
		debugPrint("(evTSE) return " + stateset.toString() + ")", false);
		return stateset;
	}
	
	
	
	// Calc the epsilon closure for this set of states
	// For each state, calc its closure and return teh union of the lot
	private StateSet epsilonClosure(StateSet states)
	{
		debugPrint("(eCSS) epsilonClosure(" + states.toString() + ")", true);
		
		StateSet closure = new StateSet();
		/* StateSet. */ Iterator stateit = states.iterator();
		while(stateit.hasNext())
		{
			closure.union(epsilonClosure((State)stateit.next()));
		}
		debugPrint("(eCSS) return closure" + closure.toString() + ")", false);
		return closure;
	}
	
	// Calc the epsilon closure for this particular state
	private StateSet epsilonClosure(State state)
	{
		debugPrint("(eCS) epsilonClosure(" + state.getName() + ")", true);
		
		if(state.getStateClass() != null) // if already calculated and cached, return it
		{
			StateSet closure = (StateSet)state.getStateClass();
			debugPrint("(eCS) return cached closure" + closure.toString() + ")", false);
			return (StateSet)state.getStateClass();
		}
		
		StateSet closure = new StateSet();
		StateSet tempset = new StateSet();
		// The original state is always included in the closure
		closure.add(state);
		// And we begin calculating from that state
		tempset.add(state);
		
		// No states are ever removed, so we can use the size to determine when fixpoint reached
		int nextsize = closure.size(); 	// we use the size to determine fixpoint
		int prevsize = 0;				// no states are removed, so same size means we're finished
		
		if(prevsize != nextsize)
		{
			do
			{	prevsize = nextsize;
				tempset = epsilonTransition(tempset); // call eTSS
				closure.union(tempset);
				nextsize = closure.size();

			} while(prevsize != nextsize);			
		}
		
		state.setStateClass(closure); // cache the result
		
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

		StateSet stateset = new StateSet(); // empty set
		// Iterate over the states and create the union of their respective closures
		/* StateSet. */ Iterator it = states.iterator();	// Should be typesafe iterator
		while(it.hasNext())
		{
			stateset.union(epsilonTransition((State)it.next()));
		}
		
		debugPrint("(eTSS) return " + stateset.toString() + ")", false);
		return stateset;
	}
	
	// calc the one-step epsilon transitions for this specific state
	private StateSet epsilonTransition(State state)
	{
		debugPrint("(eTS) epsilonTransition(" + state.getName() + ")", true);

		StateSet stateset = new StateSet();
		/* Arc */ Iterator it = state.outgoingArcsIterator();
		while(it.hasNext())
		{
			try
			{
				Arc arc = (Arc)it.next();
				if(epsilonTester.isThisEpsilon(automaton.getEvent(arc)))	// automaton.getEvent(arc).isEpsilon())
				{
					stateset.add(arc.getToState());
				}
			}
			// Note this is a (ugly?) workaround, since I know that getEvent _cannot_ throw above
			// I just got the arc from a state of the automaton. Thus, getEvent must be able to 
			// perform correctly, else the automaton is broken and we are lost anyway.
			catch(Exception excp)
			{
				excp.printStackTrace();
				throw new RuntimeException(excp);
			}
		}

		debugPrint("(eTS) return " + stateset.toString() + ")", false);
		return stateset;
	}

	// Walk the state of the automaton, and null out its stateclass (helps the GC)
	public void cleanUp()
	{
		/* State */ Iterator stateit = automaton.stateIterator();
		while(stateit.hasNext())
		{
			State state = (State)stateit.next();
			state.setStateClass(null);
		}
	}
	
	// Walk the state of the automaton, and null out its stateclass (helps the GC)
	public void cleanUpDebug()
	{
		/* State */ Iterator stateit = automaton.stateIterator();
		while(stateit.hasNext())
		{
			State state = (State)stateit.next();
			// System.out.print(state.getName());
			
			StateSet stateset = (StateSet)state.getStateClass();
			if(stateset != null)
			{
				System.out.println(": " + stateset.toString());
			}
			else
			{
				System.out.println(": <null state class>");
			} 
			state.setStateClass(null);
		}
	}
	// To test stuff, no GUI
	public static void main(String[] args)
	{
		logger.setLogToConsole(true);
		
		Automaton automaton = new Automaton("Determinizer test");
		
		State q0 = new State("q0"); automaton.addState(q0); automaton.setInitialState(q0);
		State q1 = new State("q1"); automaton.addState(q1);
		State q2 = new State("q2"); automaton.addState(q2);
		State q3 = new State("q3"); automaton.addState(q3);
		State q4 = new State("q4"); automaton.addState(q4);
		State q5 = new State("q5"); automaton.addState(q5); q5.setAccepting(true);
		
		LabeledEvent a = new LabeledEvent("a"); automaton.getAlphabet().addEvent(a, false);
		LabeledEvent b = new LabeledEvent("b"); automaton.getAlphabet().addEvent(b, false); b.setEpsilon(true);
		LabeledEvent c = new LabeledEvent("c"); automaton.getAlphabet().addEvent(c, false); c.setEpsilon(true);
		LabeledEvent d = new LabeledEvent("d"); automaton.getAlphabet().addEvent(d, false);

		Alphabet epsilons = new Alphabet();
		epsilons.addEvent(b, false);
		epsilons.addEvent(c, false);
		
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
				
*/		Determinizer detm = new Determinizer(automaton);
		detm.execute();
		detm.cleanUpDebug();

		org.supremica.automata.algorithms.AutomatonToDsx todsx = new org.supremica.automata.algorithms.AutomatonToDsx(detm.getNewAutomaton());
		try
		{
			todsx.serialize(new java.io.PrintWriter(System.out));
		}
		catch(Exception excp)
		{
			logger.error(excp);
		}


	}
}