/******************** ModifiedAstar.java **************************
 * MFs implementation of Tobbes modified Astar search algo
 * Basically this is a guided tree-search algorithm, like
 *
 *	list processed = 0;				// closed
 *	list waiting = initial_state; 	// open
 *
 *	while still waiting
 *	{
 *		choose an element from waiting	// the choice is guided by heuristics
 *		generate successors of this element
 *		if a successor is not already waiting or processed
 *			put it on waiting
 *		place the element on processed, remove it from waiting
 *	}
 */

package org.supremica.automata.algorithms.scheduling;

import java.io.*;
import java.util.*;

import com.objectspace.jgl.BinaryPredicate;
import com.objectspace.jgl.Range;
import com.objectspace.jgl.OrderedSetIterator;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.IO.AutomataToXml;
import org.supremica.properties.SupremicaProperties;
import org.supremica.util.ActionTimer;

import com.objectspace.jgl.MultiSet; // actually my own...

public class ModifiedAstar
{
	private static Logger logger = LoggerFactory.createLogger(ModifiedAstar.class);

	// Open and Closed are sorted sets of CompositeStates
	private Structure /* MultiSet */ open = new Structure();
	private Structure /* MultiSet */ closed = new Structure();	// store the managed states here

	// private Estimator estimator = null;
	private Calculator calculator = null;
	private Reopener reopener = null;
	private Automata automata = null;
	private Automata specAutomata = null;
	private Expander expander = null;

	private int debugNum = 0;
	private ActionTimer timer = new ActionTimer();

	// This is only needed for init() and walk1()
	private AutomataOnlineSynchronizer onlineSynchronizer = null;
	private int[] indexmap;

	public ModifiedAstar(Automata theAutomata, Calculator calculator, Manipulator manipulator, Expander expander, Reopener reopener)
		throws Exception
	{
		this.automata = theAutomata;
		this.expander = expander;
		// this.estimator = estimator;
		this.calculator = calculator;
		this.reopener = reopener;
		this.open = new Structure(manipulator);
		this.closed = new Structure(manipulator);
		this.specAutomata = new Automata(); // is filled in by initx()
		initx();
	}

	public ModifiedAstar(Automata theAutomata, Calculator calculator, Manipulator manipulator, Expander expander)
		throws Exception
	{
		this(theAutomata, calculator, manipulator, expander, new Reopener());
	}

	public ModifiedAstar(Automata theAutomata, Calculator calculator, Manipulator manipulator)
		throws Exception
	{
		this(theAutomata, calculator, manipulator, new DefaultExpander(theAutomata));
	}

	public ModifiedAstar(Automata theAutomata, Calculator calculator)
		throws Exception
	{
		this(theAutomata, calculator, new DefaultManipulator(), new DefaultExpander(theAutomata));
	}

	public ModifiedAstar(Automata theAutomata)
		throws Exception
	{
		this(theAutomata, new DefaultCalculator(theAutomata),  new DefaultManipulator(), new DefaultExpander(theAutomata));
	}

	private void init()
		throws Exception
	{
		automata.setIndicies();

		this.indexmap = new int[automata.size()];

		// Build the initial state
		int[] initialState = AutomataIndexFormHelper.createState(automata.size());
		int index = 0;
		for(Iterator autIt = automata.iterator(); autIt.hasNext(); )
		{
			Automaton automaton = (Automaton) autIt.next();
			State currInitialState = automaton.getInitialState();
			initialState[automaton.getIndex()] = currInitialState.getIndex();
			automaton.remapStateIndices();		// Rebuild the maps to have the indices match up - why the f***??
			if(automaton.getType() == AutomatonType.Specification)
			{
				specAutomata.addAutomaton(automaton);
				indexmap[automaton.getIndex()] = index++;	// count only the specs
			}
		}

		SynchronizationOptions syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, false, SupremicaProperties.verboseMode(), false, true);
		AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(automata, syncOptions);
		this.onlineSynchronizer = new AutomataOnlineSynchronizer(helper);

		onlineSynchronizer.initialize();
		onlineSynchronizer.setCurrState(initialState);
		helper.setCoExecuter(onlineSynchronizer);

		// Put the initial state on open
		Element ini = new ElementObject(initialState, specAutomata.size());
		open.add(ini);

		logger.debug("Initial state: " + ini.toString());
	}
	// using the expander
	private void initx()
	{
		automata.setIndicies();

		specAutomata = expander.getSpecs();
		Element ini = expander.getInitialState();
		open.add(ini);
		logger.debug("Initial state: " + ini.toString());

	}

	// Check if this state is marked by all spec automata
	boolean isMarked(Element state)
	{
		for(Iterator autIt = specAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int stateIndex = state.getStateArray()[currAutomaton.getIndex()];
			// Now we need to find the state with this index in currAutomaton
			State s = currAutomaton.getStateWithIndex(stateIndex);
			if(!s.isAccepting())
			{
				return false;
			}
		}

		logger.debug(state.toString() + " is marked.");
		return true;
	}

	// This one walks the system like:
	//
	//	foreach spec automaton Ai
	//		foreach event e enbled in the local state of Ai
	//			if e is globally enabled
	//				move in direction Ai
	//			end if
	//		end foreach
	//	end foreach

	// Potentially, there can be a lot of locally enabled events that are not globally enabled.
	public Element walk1()
		throws Exception
	{
		// When we get here, initx() has already run, we need to undo what's been done, that is, clear open
		open.clear();
		init();

		timer.start();

		while(!open.isEmpty())
		{
			// Here we should intelligently select the state to manipulate
			// Intelligently means lowest bound, this is taken care of by Structure itself
			Capsule state = new Capsule(open.first());
			debugNum++;

			/* System.out.println("\n(" + debugNum + ") Glob State: " + state.toString());*/

			if(isMarked(state))
			{
				timer.stop();
				return state; // we've reached the goal, are done
			}

			// From this state, move in all directions one step
			// First we need to find the events enabled in automaton i
			for(Iterator autIt = specAutomata.iterator(); autIt.hasNext(); )
			{
				onlineSynchronizer.setCurrState(state.getStateArray());	// we operate from this state
				Automaton currAutomaton = (Automaton) autIt.next();
				// We only look through specAutomata
				// if(currAutomaton.isSpecification())	// only do this for specs, plants/resources are not "directions"
				{
					int stateIndex = state.getStateArray()[currAutomaton.getIndex()];
					// Now we need to find the state with this index in currAutomaton
					State s = currAutomaton.getStateWithIndex(stateIndex);

					/* System.out.println("Part State: " + currAutomaton.getName() + "[" + stateIndex + ":" + s.getIndex() + "]::" + s.toString());
					*/
					// Now, let us iterate over all events (locally) enabled in this state
					EventIterator evit = currAutomaton.outgoingEventsIterator(s);
					while(evit.hasNext())
					{
						LabeledEvent event = evit.nextEvent();

						/* System.out.print("Event: " + currAutomaton.getName() + "::" + event.toString()); */

						if(onlineSynchronizer.isEnabled(event))	// if the event is globally enabled
						{
							/* System.out.println(" is globally enabled"); */

							// Move in direction Ai
							Element nextState = new ElementObject(onlineSynchronizer.doTransition(event), specAutomata.size());
							move(state, nextState, currAutomaton);

							// attach ptr back to n
							nextState.setParent(state);

							// If we've not already seen it (same logical state)...
							boolean onopen = open.contains(nextState);
							boolean onclosed = closed.contains(nextState);
							if(!onopen && !onclosed)
							{
								// ...calc the estimate
								nextState.setBound(calculator.calculate(nextState)); // nextState.getCost() + estimator.h(nextState));
								// ...put it on the list
								open.add(nextState);
							}
							else // already on open or closed
							{
								// direct ptr along path yielding the lowest g(n')
								if(nextState.getParent() != state)
								{
									// needed ptr adjustment
									nextState.setParent(state);

									if(onclosed)	// reopen
									{
										reopener.reopen(nextState, open, closed);
									}
								}
							}
						}

						/* System.out.println();*/
					}
				}
			}
			// done with this state, remove it from open
			open.remove(state);
 			// and put it on closed
			closed.add(state);
			// Note! Must be done in that order, adding alters the stored iterators
			// If you add before removing the iterators wont be pointing right
			/* System.out.println("Open: " + open.toString()); */
		}
		timer.stop();
		return null;
	}


	// This one walks the system like:
	//
	//	foreach globally enabled event e
	//		foreach spec automaton Ai
	//			if e belongs to Ai.alpha
	//				move in direction Ai
	//				break foreach
	//			end if
	//		end foreach
	//	end foreach
	public void walk2()
	{
		int debugNum = 1;

		while(!open.isEmpty())
		{
			Element state = new Capsule(open.first());

			/* System.out.println("\n(" + debugNum++ + ") Glob State: " + state.toString());*/

			// foreach event enabled in this state
			int[] events = onlineSynchronizer.getOutgoingEvents(state.getStateArray());
			for(int i = 0; i < events.length; ++i)
			{
				// What do we do now?
			}
		}
	}

	// Here's the one that uses the expander element, just to see how it works out
	public Element walk3()
	{
		timer.start();

		while(!open.isEmpty())
		{
			// Here we should intelligently select the state to manipulate
			// Intelligently means lowest bound, this is taken care of by Structure itself
			Element state = (Element)open.first().get(); // new Capsule(open.first());
			debugNum++;

			if(isMarked(state))
			{
				timer.stop();
				return state; // we've reached the goal, are done
			}

			Collection children = expander.expand(state);

			for(Iterator it = children.iterator(); it.hasNext(); )
			{
				Element nextState = (Element)it.next();

				// If we've not already seen it (same logical state)...
				boolean onopen = open.contains(nextState);
				boolean onclosed = closed.contains(nextState);
				if(!onopen && !onclosed)
				{
					// ...calc the estimate
					nextState.setBound(calculator.calculate(nextState)); // nextState.getCost() + estimator.h(nextState));
					// ...put it on the list
					open.add(nextState);
				}
				else // already on open or closed
				{
					// direct ptr along path yielding the lowest g(n')
					if(nextState.getParent() != state)
					{
						// needed ptr adjustment
						nextState.setParent(state);

						if(onclosed)	// reopen
						{
							reopener.reopen(nextState, open, closed);
						}
					}
				}
			}

			// done with this state, remove it from open
			open.remove(state);
 			// and put it on closed
			closed.add(state);
			// Note! Must be done in that order, adding alters the stored iterators
			// If you add before removing the iterators wont be pointing right

		}
		timer.stop();
		return null;	// never reached a (globally) marked state!!
	}

	// Take a step in the direction of aut
	// g and Tv are updated as (see p.47)
	private void move(Element state, Element nxtstate, Automaton aut)
	{
		int direction = indexmap[aut.getIndex()]; // specAutomata.getAutomatonIndex(aut); // aut.getIndex();
		for(Iterator autit = specAutomata.iterator(); autit.hasNext(); )
		{
			Automaton automaton = (Automaton)autit.next();
			int autidx = indexmap[automaton.getIndex()]; // specAutomata.getAutomatonIndex(automaton); // automaton.getIndex();
			if(autidx != direction)
			{
				/* System.out.println("autidx :" + autidx + " direction: " + direction + " Tv.length: " + nxtstate.getTimeArray().length + ", " + state.getTimeArray().length);
				*/
				nxtstate.getTimeArray()[autidx] = Math.max(0, state.getTimeArray()[autidx] - state.getTimeArray()[direction]);
				/* System.out.println("Tv[" + autidx + "] = max(0, " + state.getTimeArray()[autidx] + " - " + state.getTimeArray()[direction] + ")");
				*/
			}
		}
		nxtstate.setCost(state.getCost() + state.getTimeArray()[direction]);
		nxtstate.setTime(direction, aut.getStateWithIndex(nxtstate.getStateArray()[direction]).getCost());
	}
	// Generate the trace for this element
	// Note, it's written backwards (should we bother?)
	public String trace(Element elem)
	{
		StringBuffer sbuf = new StringBuffer(debugNum + " nodes searched in " + timer.toString() + ".\n");

		boolean doit = true;
		while(doit)
		{
			sbuf.append(automata.stateToString(elem.getStateArray()) + "g = " + elem.getCost() + " f = " + elem.getBound() + " " + elem.timeArrayToString());
			sbuf.append("\n<<---");

			if(elem.getParent() != null)
			{
				elem = elem.getParent();
			}
			else
			{
				doit = false;
			}
		}
		return sbuf.toString();
	}

	// Generate an automaton from the trace
	State makeNewState(Element elem)
	{
		return new State(automata.stateToString(elem.getStateArray()) + "g = " + elem.getCost() + " f = " + elem.getBound() + " " + elem.timeArrayToString());
	}
	LabeledEvent getEvent(int[] fromState, int[] toState)
	{
		int automatonIndex = -1;
		int fromStateIndex = -1;
		int toStateIndex = -1;
		// Find the index of the differing position (there's only one!)
		for(int i = 0; i < fromState.length; ++i)
		{
			if(fromState[i] != toState[i])
			{
				fromStateIndex = fromState[i];
				toStateIndex = toState[i];
				automatonIndex = i;
				break;
			}
		}
		// automatinIndex indexes an automaton, find the transition from fromStateIndex to toStateIndex
		Automaton automaton = automata.getAutomatonAt(automatonIndex);
		State from = automaton.getStateWithIndex(fromStateIndex);
		State to = automaton.getStateWithIndex(toStateIndex);
		for(ArcIterator ait = from.outgoingArcsIterator(); ait.hasNext(); )
		{
			Arc arc = ait.nextArc();
			if(arc.getToState() == to)
			{
				return arc.getEvent();
			}
		}
		return null;
	}
	public Automaton getAutomaton(Element elem)
	{
		String theTrace = trace(elem);
		logger.info(theTrace);

		Automaton automaton = new Automaton();
		automaton.setComment("Schedule");

		State state = makeNewState(elem);
		int[] stateArray = elem.getStateArray();
		state.setAccepting(true); // the final state is accepting
		automaton.addState(state);

		while(elem.getParent() != null)
		{
			elem = elem.getParent();
			State prevState = makeNewState(elem);
			automaton.addState(prevState);

			LabeledEvent event = new LabeledEvent(getEvent(elem.getStateArray(), stateArray));
			if(event == null) // something's terribly wrong!
			{
				throw new RuntimeException("No such event!");
			}
			automaton.getAlphabet().addEvent(event, false);
			automaton.addArc(new Arc(prevState, state, event));

			state = prevState;
			stateArray = elem.getStateArray();
		}

		automaton.setInitialState(state); // now this is the initial state
		return automaton;
	}

	public long getElapsedTime()
	{
		return timer.elapsedTime();
	}
	// Read in problems of the format
	//	<num machines>
	//	<num products>
	//	<first product first machine>
	//	<first product time in first machine>
	//	<first product second machine>
	//	<first product time in second machine>
	//	...
	//	<second product first machine>
	//	<second product time in second machine>
	//	...
	// The machines are numbered 0 to n-1
	public static Automata makeProblem(InputStream istream)
		throws IOException
	{
		// From the manuals:
		// Deprecated. As of JDK version 1.1, the preferred way to tokenize an input stream is to convert
		// it into a character stream, for example:
		// Reader r = new BufferedReader(new InputStreamReader(is));
		// StreamTokenizer st = new StreamTokenizer(r);
		StreamTokenizer toker = new StreamTokenizer(new BufferedReader(new InputStreamReader(istream)));
		return makeProblem(toker);
	}
	public static Automata makeProblem(Reader reader)
		throws IOException
	{
		StreamTokenizer toker = new StreamTokenizer(new BufferedReader(reader));
		return makeProblem(toker);
	}

	public static Automata makeProblem(StreamTokenizer toker)
		throws IOException
	{
		toker.eolIsSignificant(false);	// newline is whitespace
		toker.slashStarComments(true);	// recognize and ignore c-comments
		toker.slashSlashComments(true);	// recognize and ignore c++comments
		toker.parseNumbers();			// we only expect ints

		Automata automata = new Automata();

		// get num machines
		int numMachines = 0;
		if(toker.nextToken() == StreamTokenizer.TT_NUMBER)
 		{
 			numMachines = (int)toker.nval;
 		}
		State[] minits = new State[numMachines];
		Automaton[] mauto = new Automaton[numMachines];
 		for(int m = 0; m < numMachines; ++m)
 		{
 			minits[m] = new State("m" + m + "0");
 			mauto[m] = new Automaton("M" + m);
 			mauto[m].addState(minits[m]);
 			mauto[m].setInitialState(minits[m]);
 			mauto[m].setType(AutomatonType.Plant);

 			automata.addAutomaton(mauto[m]);
 		}

 		// get num products
 		int numProducts = 0;
		if(toker.nextToken() == StreamTokenizer.TT_NUMBER)
 		{
 			numProducts = (int)toker.nval;
 		}
		State[] pstates = new State[numProducts];
		Automaton[] pauto = new Automaton[numProducts];
		for(int p = 0; p < numProducts; ++p)
		{
			pstates[p] = new State("p" + p + "0");
			pstates[p].setCost(0);

			pauto[p] = new Automaton("P" + p);
			pauto[p].addState(pstates[p]);
			pauto[p].setInitialState(pstates[p]);
			pauto[p].setType(AutomatonType.Specification);

			// State finale = new State("x" + p);
			// finale.setAccepting(true);
			// finale.setCost(0);
			// pauto[p].addState(finale);

			automata.addAutomaton(pauto[p]);
		}

		// next follows (should follow) numProducts of numMachines times 2 entries
		for(int p = 0; p < numProducts; ++p)
		{
			int num = 0;
			for(int m = 0; m < numMachines; ++m)
			{
				if(toker.nextToken() == StreamTokenizer.TT_NUMBER)
				{
					int mach = (int)toker.nval;

					if(toker.nextToken() == StreamTokenizer.TT_NUMBER)
					{
						int time = (int)toker.nval;
						// so we got the machine and the time for the next op
						// make a transition in p
						State nxtp = new State("p" + p + "m" + mach);
						nxtp.setCost(time);
						LabeledEvent evp = new LabeledEvent("e" + p + num);
						pauto[p].getAlphabet().addEvent(evp);
						pauto[p].addState(nxtp);
						pauto[p].addArc(new Arc(pstates[p], nxtp, evp));
						pstates[p] = nxtp;
						// and a trasnition in m -- whatabout multiple entries?
						State nxtm = new State("p" + p);
						LabeledEvent evm = new LabeledEvent("e" + p + num);
						mauto[mach].addState(nxtm);
						mauto[mach].getAlphabet().addEvent(evm);
						mauto[mach].addArc(new Arc(minits[mach], nxtm, evm));

						num++;

						// The event out of there is same as the event into the next one
						evm = new LabeledEvent("e" + p + num);
						mauto[mach].getAlphabet().addEvent(evm);
						mauto[mach].addArc(new Arc(nxtm, minits[mach], evm));
					}
				}
				// throw SomeFuckingException
			}
			// final transition into the goal state
			LabeledEvent evp = new LabeledEvent("e" + p + num);
			pauto[p].getAlphabet().addEvent(evp);
			State finale = new State("x" + p);
			finale.setAccepting(true);
			finale.setCost(0);
			pauto[p].addState(finale);
			pauto[p].addArc(new Arc(pstates[p], finale, evp));
		}

		return automata;

	}
	public static void main(String args[])
	{
		/* See Alorithm 1, Fig 5.16, p.47 in Tobbes lic
		Automaton p1 = new Automaton("P1");		{			p1.setType(AutomatonType.Specification);
		State q10 = new State("p1_0");	q10.setCost(0);		p1.addState(q10);	p1.setInitialState(q10);
		State q11 = new State("p1M1");	q11.setCost(3);		p1.addState(q11);
		State q12 = new State("p1M2");	q12.setCost(4);		p1.addState(q12);
		State q13 = new State("p1_3");	q13.setCost(0);		p1.addState(q13);	q13.setAccepting(true);
		LabeledEvent e11 = new LabeledEvent("p1 in i m1");			p1.getAlphabet().addEvent(e11);
		LabeledEvent e12 = new LabeledEvent("p1 ut ur m1 in i m2");	p1.getAlphabet().addEvent(e12);
		LabeledEvent e13 = new LabeledEvent("p1 ut ur m2");			p1.getAlphabet().addEvent(e13);
		p1.addArc(new Arc(q10, q11, e11));
		p1.addArc(new Arc(q11, q12, e12));
		p1.addArc(new Arc(q12, q13, e13));		}

		Automaton p2 = new Automaton("P2");		{			p2.setType(AutomatonType.Specification);
		State q20 = new State("p2_0");	q20.setCost(0);		p2.addState(q20);	p2.setInitialState(q20);
		State q21 = new State("p2M1");	q21.setCost(1);		p2.addState(q21);
		State q22 = new State("p2M2");	q22.setCost(2);		p2.addState(q22);
		State q23 = new State("p2_3");	q23.setCost(0);		p2.addState(q23);	q23.setAccepting(true);
		LabeledEvent e21 = new LabeledEvent("p2 in i m1");			p2.getAlphabet().addEvent(e21);
		LabeledEvent e22 = new LabeledEvent("p2 ut ur m1 in i m2");	p2.getAlphabet().addEvent(e22);
		LabeledEvent e23 = new LabeledEvent("p2 ut ur m2");			p2.getAlphabet().addEvent(e23);
		p2.addArc(new Arc(q20, q21, e21));
		p2.addArc(new Arc(q21, q22, e22));
		p2.addArc(new Arc(q22, q23, e23));		}

		Automaton m1 = new Automaton("M1");	{	m1.setType(AutomatonType.Plant);
		State m10 = new State("m10");			m1.addState(m10);	m1.setInitialState(m10);	m10.setAccepting(true);
		State m11 = new State("m11");			m1.addState(m11);								m11.setAccepting(true);
		LabeledEvent em11 = new LabeledEvent("p1 in i m1");			m1.getAlphabet().addEvent(em11);
		LabeledEvent em12 = new LabeledEvent("p2 in i m1");			m1.getAlphabet().addEvent(em12);
		LabeledEvent em13 = new LabeledEvent("p1 ut ur m1 in i m2");m1.getAlphabet().addEvent(em13);
		LabeledEvent em14 = new LabeledEvent("p2 ut ur m1 in i m2");m1.getAlphabet().addEvent(em14);
		m1.addArc(new Arc(m10, m11, em11));
		m1.addArc(new Arc(m10, m11, em12));
		m1.addArc(new Arc(m11, m10, em13));
		m1.addArc(new Arc(m11, m10, em14));		}

		Automaton m2 = new Automaton("M2");	{	m2.setType(AutomatonType.Plant);
		State m20 = new State("m20");			m2.addState(m20);	m2.setInitialState(m20);	m20.setAccepting(true);
		State m21 = new State("m21");			m2.addState(m21);								m21.setAccepting(true);
		LabeledEvent em21 = new LabeledEvent("p1 ut ur m1 in i m2");m2.getAlphabet().addEvent(em21);
		LabeledEvent em22 = new LabeledEvent("p2 ut ur m1 in i m2");m2.getAlphabet().addEvent(em22);
		LabeledEvent em23 = new LabeledEvent("p1 ut ur m2");		m2.getAlphabet().addEvent(em23);
		LabeledEvent em24 = new LabeledEvent("p2 ut ur m2");		m2.getAlphabet().addEvent(em24);
		m2.addArc(new Arc(m20, m21, em21));
		m2.addArc(new Arc(m20, m21, em22));
		m2.addArc(new Arc(m21, m20, em23));
		m2.addArc(new Arc(m21, m20, em24));	}

		Automata automata = new Automata();
		automata.addAutomaton(p1);
		automata.addAutomaton(p2);
		automata.addAutomaton(m1);
		automata.addAutomaton(m2); */

		/* This one guarantees that only the route shown on p 47 is followed
		// Note that without route we will not get the shown g and Tv results
		Automaton route = new Automaton("route");		route.setType(AutomatonType.Plant);
		State r0 = new State("r0");			route.addState(r0);	route.setInitialState(r0);
		State r1 = new State("r1");			route.addState(r1);
		State r2 = new State("r2");			route.addState(r2);
		State r3 = new State("r3");			route.addState(r3);
		State r4 = new State("r4");			route.addState(r4);
		State r5 = new State("r5");			route.addState(r5);
		State r6 = new State("r6");			route.addState(r6);	r6.setAccepting(true);
		LabeledEvent er1 = new LabeledEvent("p2 in i m1");			route.getAlphabet().addEvent(er1);
		LabeledEvent er2 = new LabeledEvent("p2 ut ur m1 in i m2");	route.getAlphabet().addEvent(er2);
		LabeledEvent er3 = new LabeledEvent("p1 in i m1");			route.getAlphabet().addEvent(er3);
		LabeledEvent er4 = new LabeledEvent("p2 ut ur m2");			route.getAlphabet().addEvent(er4);
		LabeledEvent er5 = new LabeledEvent("p1 ut ur m1 in i m2");	route.getAlphabet().addEvent(er5);
		LabeledEvent er6 = new LabeledEvent("p1 ut ur m2");			route.getAlphabet().addEvent(er6);
		route.addArc(new Arc(r0, r1, er1));
		route.addArc(new Arc(r1, r2, er2));
		route.addArc(new Arc(r2, r3, er3));
		route.addArc(new Arc(r3, r4, er4));
		route.addArc(new Arc(r4, r5, er5));
		route.addArc(new Arc(r5, r6, er6));

		// automata.addAutomaton(route);
		*/

		/* This is the example on page 66
		Automaton p1 = new Automaton("P1");	{				p1.setType(AutomatonType.Specification);
		State q10 = new State("p1_0");	q10.setCost(0);		p1.addState(q10);	p1.setInitialState(q10);
		State q11 = new State("p1M1");	q11.setCost(1);		p1.addState(q11);
		State q12 = new State("p1M2");	q12.setCost(2);		p1.addState(q12);
		State q13 = new State("p1_3");	q13.setCost(0);		p1.addState(q13);	q13.setAccepting(true);
		LabeledEvent e11 = new LabeledEvent("alfa 1");		p1.getAlphabet().addEvent(e11);
		LabeledEvent e12 = new LabeledEvent("alfa 4");		p1.getAlphabet().addEvent(e12);
		LabeledEvent e13 = new LabeledEvent("alfa 7");		p1.getAlphabet().addEvent(e13);
		p1.addArc(new Arc(q10, q11, e11));
		p1.addArc(new Arc(q11, q12, e12));
		p1.addArc(new Arc(q12, q13, e13));	}

		Automaton p2 = new Automaton("P2");	{				p2.setType(AutomatonType.Specification);
		State q20 = new State("p2_0");	q20.setCost(0);		p2.addState(q20);	p2.setInitialState(q20);
		State q21 = new State("p2M1");	q21.setCost(2);		p2.addState(q21);
		State q22 = new State("p2M3");	q22.setCost(1);		p2.addState(q22);
		State q23 = new State("p2_3");	q23.setCost(0);		p2.addState(q23);	q23.setAccepting(true);
		LabeledEvent e21 = new LabeledEvent("alfa 2");	p2.getAlphabet().addEvent(e21);
		LabeledEvent e22 = new LabeledEvent("alfa 5");	p2.getAlphabet().addEvent(e22);
		LabeledEvent e23 = new LabeledEvent("alfa 8");	p2.getAlphabet().addEvent(e23);
		p2.addArc(new Arc(q20, q21, e21));
		p2.addArc(new Arc(q21, q22, e22));
		p2.addArc(new Arc(q22, q23, e23));	}

		Automaton p3 = new Automaton("P3");	{				p3.setType(AutomatonType.Specification);
		State q30 = new State("p3_0");	q30.setCost(0);		p3.addState(q30);	p3.setInitialState(q30);
		State q31 = new State("p3M4");	q31.setCost(3);		p3.addState(q31);
		State q32 = new State("p3M5");	q32.setCost(1);		p3.addState(q32);
		State q33 = new State("p3_3");	q33.setCost(0);		p3.addState(q33);	q33.setAccepting(true);
		LabeledEvent e31 = new LabeledEvent("alfa 3");	p3.getAlphabet().addEvent(e31);
		LabeledEvent e32 = new LabeledEvent("alfa 6");	p3.getAlphabet().addEvent(e32);
		LabeledEvent e33 = new LabeledEvent("alfa 9");	p3.getAlphabet().addEvent(e33);
		p3.addArc(new Arc(q30, q31, e31));
		p3.addArc(new Arc(q31, q32, e32));
		p3.addArc(new Arc(q32, q33, e33));	}

		Automaton m1 = new Automaton("M1");	{	m1.setType(AutomatonType.Plant);
		State m10 = new State("m10");			m1.addState(m10);	m1.setInitialState(m10);	m10.setAccepting(true);
		State m11 = new State("m11");			m1.addState(m11);								m11.setAccepting(true);
		LabeledEvent em11 = new LabeledEvent("alfa 1");		m1.getAlphabet().addEvent(em11);
		LabeledEvent em12 = new LabeledEvent("alfa 2");		m1.getAlphabet().addEvent(em12);
		LabeledEvent em13 = new LabeledEvent("alfa 4");		m1.getAlphabet().addEvent(em13);
		LabeledEvent em14 = new LabeledEvent("alfa 5");		m1.getAlphabet().addEvent(em14);
		m1.addArc(new Arc(m10, m11, em11));
		m1.addArc(new Arc(m10, m11, em12));
		m1.addArc(new Arc(m11, m10, em13));
		m1.addArc(new Arc(m11, m10, em14));	}

		Automaton m2 = new Automaton("M2");	{	m2.setType(AutomatonType.Plant);
		State m20 = new State("m20");			m2.addState(m20);	m2.setInitialState(m20);	m20.setAccepting(true);
		State m21 = new State("m21");			m2.addState(m21);								m21.setAccepting(true);
		LabeledEvent em21 = new LabeledEvent("alfa 4");		m2.getAlphabet().addEvent(em21);
		LabeledEvent em22 = new LabeledEvent("alfa 7");		m2.getAlphabet().addEvent(em22);
		m2.addArc(new Arc(m20, m21, em21));
		m2.addArc(new Arc(m21, m20, em22));	}

		Automaton m3 = new Automaton("M3");	{	m3.setType(AutomatonType.Plant);
		State m30 = new State("m30");			m3.addState(m30);	m3.setInitialState(m30);	m30.setAccepting(true);
		State m31 = new State("m31");			m3.addState(m31);								m31.setAccepting(true);
		LabeledEvent em31 = new LabeledEvent("alfa 5");		m3.getAlphabet().addEvent(em31);
		LabeledEvent em32 = new LabeledEvent("alfa 8");		m3.getAlphabet().addEvent(em32);
		m3.addArc(new Arc(m30, m31, em31));
		m3.addArc(new Arc(m31, m30, em32));	}

		Automaton m4 = new Automaton("m4");	{	m4.setType(AutomatonType.Plant);
		State m40 = new State("m40");			m4.addState(m40);	m4.setInitialState(m40);	m40.setAccepting(true);
		State m41 = new State("m41");			m4.addState(m41);								m41.setAccepting(true);
		LabeledEvent em41 = new LabeledEvent("alfa 3");		m4.getAlphabet().addEvent(em41);
		LabeledEvent em42 = new LabeledEvent("alfa 6");		m4.getAlphabet().addEvent(em42);
		m4.addArc(new Arc(m40, m41, em41));
		m4.addArc(new Arc(m41, m40, em42));	}

		Automaton m5 = new Automaton("m5");	{	m5.setType(AutomatonType.Plant);
		State m50 = new State("m50");			m5.addState(m50);	m5.setInitialState(m50);	m50.setAccepting(true);
		State m51 = new State("m51");			m5.addState(m51);								m51.setAccepting(true);
		LabeledEvent em51 = new LabeledEvent("alfa 6");		m5.getAlphabet().addEvent(em51);
		LabeledEvent em52 = new LabeledEvent("alfa 9");		m5.getAlphabet().addEvent(em52);
		m5.addArc(new Arc(m50, m51, em51));
		m5.addArc(new Arc(m51, m50, em52));	}

		Automata automata = new Automata();
		automata.addAutomaton(p1);
		automata.addAutomaton(p2);
		automata.addAutomaton(p3);
		automata.addAutomaton(m1);
		automata.addAutomaton(m2);
		automata.addAutomaton(m3);
		automata.addAutomaton(m4);
		automata.addAutomaton(m5); */

		// This is FisherThompson 6x6 read in from file and generetade by toCode()
		Automaton M0 = new Automaton("M0");		{			M0.setType(AutomatonType.Plant);
		State m00 = new State("m00");	m00.setCost(-1);	M0.addState(m00);	M0.setInitialState(m00);
		State p0 = new State("p0");	p0.setCost(-1);	M0.addState(p0);
		State p1 = new State("p1");	p1.setCost(-1);	M0.addState(p1);
		State p2 = new State("p2");	p2.setCost(-1);	M0.addState(p2);
		State p3 = new State("p3");	p3.setCost(-1);	M0.addState(p3);
		State p4 = new State("p4");	p4.setCost(-1);	M0.addState(p4);
		State p5 = new State("p5");	p5.setCost(-1);	M0.addState(p5);
		LabeledEvent e15 = new LabeledEvent("e15");	M0.getAlphabet().addEvent(e15);
		LabeledEvent e14 = new LabeledEvent("e14");	M0.getAlphabet().addEvent(e14);
		LabeledEvent e24 = new LabeledEvent("e24");	M0.getAlphabet().addEvent(e24);
		LabeledEvent e02 = new LabeledEvent("e02");	M0.getAlphabet().addEvent(e02);
		LabeledEvent e45 = new LabeledEvent("e45");	M0.getAlphabet().addEvent(e45);
		LabeledEvent e23 = new LabeledEvent("e23");	M0.getAlphabet().addEvent(e23);
		LabeledEvent e01 = new LabeledEvent("e01");	M0.getAlphabet().addEvent(e01);
		LabeledEvent e44 = new LabeledEvent("e44");	M0.getAlphabet().addEvent(e44);
		LabeledEvent e54 = new LabeledEvent("e54");	M0.getAlphabet().addEvent(e54);
		LabeledEvent e32 = new LabeledEvent("e32");	M0.getAlphabet().addEvent(e32);
		LabeledEvent e53 = new LabeledEvent("e53");	M0.getAlphabet().addEvent(e53);
		LabeledEvent e31 = new LabeledEvent("e31");	M0.getAlphabet().addEvent(e31);
		M0.addArc(new Arc(m00, p0, e01));
		M0.addArc(new Arc(p0, m00, e02));
		M0.addArc(new Arc(m00, p1, e14));
		M0.addArc(new Arc(p1, m00, e15));
		M0.addArc(new Arc(m00, p2, e23));
		M0.addArc(new Arc(p2, m00, e24));
		M0.addArc(new Arc(m00, p3, e31));
		M0.addArc(new Arc(p3, m00, e32));
		M0.addArc(new Arc(m00, p4, e44));
		M0.addArc(new Arc(p4, m00, e45));
		M0.addArc(new Arc(m00, p5, e53));
		M0.addArc(new Arc(p5, m00, e54));
		}

		Automaton M1 = new Automaton("M1");		{			M1.setType(AutomatonType.Plant);
		State m10 = new State("m10");	m10.setCost(-1);	M1.addState(m10);	M1.setInitialState(m10);
		State p0 = new State("p0");	p0.setCost(-1);	M1.addState(p0);
		State p1 = new State("p1");	p1.setCost(-1);	M1.addState(p1);
		State p2 = new State("p2");	p2.setCost(-1);	M1.addState(p2);
		State p3 = new State("p3");	p3.setCost(-1);	M1.addState(p3);
		State p4 = new State("p4");	p4.setCost(-1);	M1.addState(p4);
		State p5 = new State("p5");	p5.setCost(-1);	M1.addState(p5);
		LabeledEvent e25 = new LabeledEvent("e25");	M1.getAlphabet().addEvent(e25);
		LabeledEvent e03 = new LabeledEvent("e03");	M1.getAlphabet().addEvent(e03);
		LabeledEvent e24 = new LabeledEvent("e24");	M1.getAlphabet().addEvent(e24);
		LabeledEvent e02 = new LabeledEvent("e02");	M1.getAlphabet().addEvent(e02);
		LabeledEvent e11 = new LabeledEvent("e11");	M1.getAlphabet().addEvent(e11);
		LabeledEvent e10 = new LabeledEvent("e10");	M1.getAlphabet().addEvent(e10);
		LabeledEvent e42 = new LabeledEvent("e42");	M1.getAlphabet().addEvent(e42);
		LabeledEvent e31 = new LabeledEvent("e31");	M1.getAlphabet().addEvent(e31);
		LabeledEvent e41 = new LabeledEvent("e41");	M1.getAlphabet().addEvent(e41);
		LabeledEvent e30 = new LabeledEvent("e30");	M1.getAlphabet().addEvent(e30);
		LabeledEvent e51 = new LabeledEvent("e51");	M1.getAlphabet().addEvent(e51);
		LabeledEvent e50 = new LabeledEvent("e50");	M1.getAlphabet().addEvent(e50);
		M1.addArc(new Arc(m10, p0, e02));
		M1.addArc(new Arc(p0, m10, e03));
		M1.addArc(new Arc(m10, p1, e10));
		M1.addArc(new Arc(p1, m10, e11));
		M1.addArc(new Arc(m10, p2, e24));
		M1.addArc(new Arc(p2, m10, e25));
		M1.addArc(new Arc(m10, p3, e30));
		M1.addArc(new Arc(p3, m10, e31));
		M1.addArc(new Arc(m10, p4, e41));
		M1.addArc(new Arc(p4, m10, e42));
		M1.addArc(new Arc(m10, p5, e50));
		M1.addArc(new Arc(p5, m10, e51));
		}

		Automaton M2 = new Automaton("M2");		{			M2.setType(AutomatonType.Plant);
		State m20 = new State("m20");	m20.setCost(-1);	M2.addState(m20);	M2.setInitialState(m20);
		State p0 = new State("p0");	p0.setCost(-1);	M2.addState(p0);
		State p1 = new State("p1");	p1.setCost(-1);	M2.addState(p1);
		State p2 = new State("p2");	p2.setCost(-1);	M2.addState(p2);
		State p3 = new State("p3");	p3.setCost(-1);	M2.addState(p3);
		State p4 = new State("p4");	p4.setCost(-1);	M2.addState(p4);
		State p5 = new State("p5");	p5.setCost(-1);	M2.addState(p5);
		LabeledEvent e56 = new LabeledEvent("e56");	M2.getAlphabet().addEvent(e56);
		LabeledEvent e12 = new LabeledEvent("e12");	M2.getAlphabet().addEvent(e12);
		LabeledEvent e01 = new LabeledEvent("e01");	M2.getAlphabet().addEvent(e01);
		LabeledEvent e55 = new LabeledEvent("e55");	M2.getAlphabet().addEvent(e55);
		LabeledEvent e33 = new LabeledEvent("e33");	M2.getAlphabet().addEvent(e33);
		LabeledEvent e11 = new LabeledEvent("e11");	M2.getAlphabet().addEvent(e11);
		LabeledEvent e00 = new LabeledEvent("e00");	M2.getAlphabet().addEvent(e00);
		LabeledEvent e32 = new LabeledEvent("e32");	M2.getAlphabet().addEvent(e32);
		LabeledEvent e21 = new LabeledEvent("e21");	M2.getAlphabet().addEvent(e21);
		LabeledEvent e20 = new LabeledEvent("e20");	M2.getAlphabet().addEvent(e20);
		LabeledEvent e41 = new LabeledEvent("e41");	M2.getAlphabet().addEvent(e41);
		LabeledEvent e40 = new LabeledEvent("e40");	M2.getAlphabet().addEvent(e40);
		M2.addArc(new Arc(m20, p0, e00));
		M2.addArc(new Arc(p0, m20, e01));
		M2.addArc(new Arc(m20, p1, e11));
		M2.addArc(new Arc(p1, m20, e12));
		M2.addArc(new Arc(m20, p2, e20));
		M2.addArc(new Arc(p2, m20, e21));
		M2.addArc(new Arc(m20, p3, e32));
		M2.addArc(new Arc(p3, m20, e33));
		M2.addArc(new Arc(m20, p4, e40));
		M2.addArc(new Arc(p4, m20, e41));
		M2.addArc(new Arc(m20, p5, e55));
		M2.addArc(new Arc(p5, m20, e56));
		}

		Automaton M3 = new Automaton("M3");		{			M3.setType(AutomatonType.Plant);
		State m30 = new State("m30");	m30.setCost(-1);	M3.addState(m30);	M3.setInitialState(m30);
		State p0 = new State("p0");	p0.setCost(-1);	M3.addState(p0);
		State p1 = new State("p1");	p1.setCost(-1);	M3.addState(p1);
		State p2 = new State("p2");	p2.setCost(-1);	M3.addState(p2);
		State p3 = new State("p3");	p3.setCost(-1);	M3.addState(p3);
		State p4 = new State("p4");	p4.setCost(-1);	M3.addState(p4);
		State p5 = new State("p5");	p5.setCost(-1);	M3.addState(p5);
		LabeledEvent e16 = new LabeledEvent("e16");	M3.getAlphabet().addEvent(e16);
		LabeledEvent e15 = new LabeledEvent("e15");	M3.getAlphabet().addEvent(e15);
		LabeledEvent e04 = new LabeledEvent("e04");	M3.getAlphabet().addEvent(e04);
		LabeledEvent e03 = new LabeledEvent("e03");	M3.getAlphabet().addEvent(e03);
		LabeledEvent e46 = new LabeledEvent("e46");	M3.getAlphabet().addEvent(e46);
		LabeledEvent e45 = new LabeledEvent("e45");	M3.getAlphabet().addEvent(e45);
		LabeledEvent e34 = new LabeledEvent("e34");	M3.getAlphabet().addEvent(e34);
		LabeledEvent e33 = new LabeledEvent("e33");	M3.getAlphabet().addEvent(e33);
		LabeledEvent e22 = new LabeledEvent("e22");	M3.getAlphabet().addEvent(e22);
		LabeledEvent e21 = new LabeledEvent("e21");	M3.getAlphabet().addEvent(e21);
		LabeledEvent e52 = new LabeledEvent("e52");	M3.getAlphabet().addEvent(e52);
		LabeledEvent e51 = new LabeledEvent("e51");	M3.getAlphabet().addEvent(e51);
		M3.addArc(new Arc(m30, p0, e03));
		M3.addArc(new Arc(p0, m30, e04));
		M3.addArc(new Arc(m30, p1, e15));
		M3.addArc(new Arc(p1, m30, e16));
		M3.addArc(new Arc(m30, p2, e21));
		M3.addArc(new Arc(p2, m30, e22));
		M3.addArc(new Arc(m30, p3, e33));
		M3.addArc(new Arc(p3, m30, e34));
		M3.addArc(new Arc(m30, p4, e45));
		M3.addArc(new Arc(p4, m30, e46));
		M3.addArc(new Arc(m30, p5, e51));
		M3.addArc(new Arc(p5, m30, e52));
		}

		Automaton M4 = new Automaton("M4");		{			M4.setType(AutomatonType.Plant);
		State m40 = new State("m40");	m40.setCost(-1);	M4.addState(m40);	M4.setInitialState(m40);
		State p0 = new State("p0");	p0.setCost(-1);	M4.addState(p0);
		State p1 = new State("p1");	p1.setCost(-1);	M4.addState(p1);
		State p2 = new State("p2");	p2.setCost(-1);	M4.addState(p2);
		State p3 = new State("p3");	p3.setCost(-1);	M4.addState(p3);
		State p4 = new State("p4");	p4.setCost(-1);	M4.addState(p4);
		State p5 = new State("p5");	p5.setCost(-1);	M4.addState(p5);
		LabeledEvent e06 = new LabeledEvent("e06");	M4.getAlphabet().addEvent(e06);
		LabeledEvent e05 = new LabeledEvent("e05");	M4.getAlphabet().addEvent(e05);
		LabeledEvent e26 = new LabeledEvent("e26");	M4.getAlphabet().addEvent(e26);
		LabeledEvent e25 = new LabeledEvent("e25");	M4.getAlphabet().addEvent(e25);
		LabeledEvent e35 = new LabeledEvent("e35");	M4.getAlphabet().addEvent(e35);
		LabeledEvent e13 = new LabeledEvent("e13");	M4.getAlphabet().addEvent(e13);
		LabeledEvent e34 = new LabeledEvent("e34");	M4.getAlphabet().addEvent(e34);
		LabeledEvent e12 = new LabeledEvent("e12");	M4.getAlphabet().addEvent(e12);
		LabeledEvent e55 = new LabeledEvent("e55");	M4.getAlphabet().addEvent(e55);
		LabeledEvent e54 = new LabeledEvent("e54");	M4.getAlphabet().addEvent(e54);
		LabeledEvent e43 = new LabeledEvent("e43");	M4.getAlphabet().addEvent(e43);
		LabeledEvent e42 = new LabeledEvent("e42");	M4.getAlphabet().addEvent(e42);
		M4.addArc(new Arc(m40, p0, e05));
		M4.addArc(new Arc(p0, m40, e06));
		M4.addArc(new Arc(m40, p1, e12));
		M4.addArc(new Arc(p1, m40, e13));
		M4.addArc(new Arc(m40, p2, e25));
		M4.addArc(new Arc(p2, m40, e26));
		M4.addArc(new Arc(m40, p3, e34));
		M4.addArc(new Arc(p3, m40, e35));
		M4.addArc(new Arc(m40, p4, e42));
		M4.addArc(new Arc(p4, m40, e43));
		M4.addArc(new Arc(m40, p5, e54));
		M4.addArc(new Arc(p5, m40, e55));
		}

		Automaton M5 = new Automaton("M5");		{			M5.setType(AutomatonType.Plant);
		State m50 = new State("m50");	m50.setCost(-1);	M5.addState(m50);	M5.setInitialState(m50);
		State p0 = new State("p0");	p0.setCost(-1);	M5.addState(p0);
		State p1 = new State("p1");	p1.setCost(-1);	M5.addState(p1);
		State p2 = new State("p2");	p2.setCost(-1);	M5.addState(p2);
		State p3 = new State("p3");	p3.setCost(-1);	M5.addState(p3);
		State p4 = new State("p4");	p4.setCost(-1);	M5.addState(p4);
		State p5 = new State("p5");	p5.setCost(-1);	M5.addState(p5);
		LabeledEvent e05 = new LabeledEvent("e05");	M5.getAlphabet().addEvent(e05);
		LabeledEvent e04 = new LabeledEvent("e04");	M5.getAlphabet().addEvent(e04);
		LabeledEvent e36 = new LabeledEvent("e36");	M5.getAlphabet().addEvent(e36);
		LabeledEvent e14 = new LabeledEvent("e14");	M5.getAlphabet().addEvent(e14);
		LabeledEvent e35 = new LabeledEvent("e35");	M5.getAlphabet().addEvent(e35);
		LabeledEvent e13 = new LabeledEvent("e13");	M5.getAlphabet().addEvent(e13);
		LabeledEvent e23 = new LabeledEvent("e23");	M5.getAlphabet().addEvent(e23);
		LabeledEvent e44 = new LabeledEvent("e44");	M5.getAlphabet().addEvent(e44);
		LabeledEvent e22 = new LabeledEvent("e22");	M5.getAlphabet().addEvent(e22);
		LabeledEvent e43 = new LabeledEvent("e43");	M5.getAlphabet().addEvent(e43);
		LabeledEvent e53 = new LabeledEvent("e53");	M5.getAlphabet().addEvent(e53);
		LabeledEvent e52 = new LabeledEvent("e52");	M5.getAlphabet().addEvent(e52);
		M5.addArc(new Arc(m50, p0, e04));
		M5.addArc(new Arc(p0, m50, e05));
		M5.addArc(new Arc(m50, p1, e13));
		M5.addArc(new Arc(p1, m50, e14));
		M5.addArc(new Arc(m50, p2, e22));
		M5.addArc(new Arc(p2, m50, e23));
		M5.addArc(new Arc(m50, p3, e35));
		M5.addArc(new Arc(p3, m50, e36));
		M5.addArc(new Arc(m50, p4, e43));
		M5.addArc(new Arc(p4, m50, e44));
		M5.addArc(new Arc(m50, p5, e52));
		M5.addArc(new Arc(p5, m50, e53));
		}

		Automaton P0 = new Automaton("P0");		{			P0.setType(AutomatonType.Specification);
		State p00 = new State("p00");	p00.setCost(0);	P0.addState(p00);	P0.setInitialState(p00);
		State p0m0 = new State("p0m0");	p0m0.setCost(3);	P0.addState(p0m0);
		State p0m1 = new State("p0m1");	p0m1.setCost(6);	P0.addState(p0m1);
		State p0m2 = new State("p0m2");	p0m2.setCost(1);	P0.addState(p0m2);
		State p0m3 = new State("p0m3");	p0m3.setCost(7);	P0.addState(p0m3);
		State p0m4 = new State("p0m4");	p0m4.setCost(6);	P0.addState(p0m4);
		State p0m5 = new State("p0m5");	p0m5.setCost(3);	P0.addState(p0m5);
		State x0 = new State("x0");	x0.setCost(0);	P0.addState(x0);	x0.setAccepting(true);
		LabeledEvent e06 = new LabeledEvent("e06");	P0.getAlphabet().addEvent(e06);
		LabeledEvent e05 = new LabeledEvent("e05");	P0.getAlphabet().addEvent(e05);
		LabeledEvent e04 = new LabeledEvent("e04");	P0.getAlphabet().addEvent(e04);
		LabeledEvent e03 = new LabeledEvent("e03");	P0.getAlphabet().addEvent(e03);
		LabeledEvent e02 = new LabeledEvent("e02");	P0.getAlphabet().addEvent(e02);
		LabeledEvent e01 = new LabeledEvent("e01");	P0.getAlphabet().addEvent(e01);
		LabeledEvent e00 = new LabeledEvent("e00");	P0.getAlphabet().addEvent(e00);
		P0.addArc(new Arc(p00, p0m2, e00));
		P0.addArc(new Arc(p0m2, p0m0, e01));
		P0.addArc(new Arc(p0m0, p0m1, e02));
		P0.addArc(new Arc(p0m1, p0m3, e03));
		P0.addArc(new Arc(p0m3, p0m5, e04));
		P0.addArc(new Arc(p0m5, p0m4, e05));
		P0.addArc(new Arc(p0m4, x0, e06));
		}

		Automaton P1 = new Automaton("P1");		{			P1.setType(AutomatonType.Specification);
		State p10 = new State("p10");	p10.setCost(0);	P1.addState(p10);	P1.setInitialState(p10);
		State p1m0 = new State("p1m0");	p1m0.setCost(10);	P1.addState(p1m0);
		State p1m1 = new State("p1m1");	p1m1.setCost(8);	P1.addState(p1m1);
		State p1m2 = new State("p1m2");	p1m2.setCost(5);	P1.addState(p1m2);
		State p1m3 = new State("p1m3");	p1m3.setCost(4);	P1.addState(p1m3);
		State p1m4 = new State("p1m4");	p1m4.setCost(10);	P1.addState(p1m4);
		State p1m5 = new State("p1m5");	p1m5.setCost(10);	P1.addState(p1m5);
		State x1 = new State("x1");	x1.setCost(0);	P1.addState(x1);	x1.setAccepting(true);
		LabeledEvent e16 = new LabeledEvent("e16");	P1.getAlphabet().addEvent(e16);
		LabeledEvent e15 = new LabeledEvent("e15");	P1.getAlphabet().addEvent(e15);
		LabeledEvent e14 = new LabeledEvent("e14");	P1.getAlphabet().addEvent(e14);
		LabeledEvent e13 = new LabeledEvent("e13");	P1.getAlphabet().addEvent(e13);
		LabeledEvent e12 = new LabeledEvent("e12");	P1.getAlphabet().addEvent(e12);
		LabeledEvent e11 = new LabeledEvent("e11");	P1.getAlphabet().addEvent(e11);
		LabeledEvent e10 = new LabeledEvent("e10");	P1.getAlphabet().addEvent(e10);
		P1.addArc(new Arc(p10, p1m1, e10));
		P1.addArc(new Arc(p1m1, p1m2, e11));
		P1.addArc(new Arc(p1m2, p1m4, e12));
		P1.addArc(new Arc(p1m4, p1m5, e13));
		P1.addArc(new Arc(p1m5, p1m0, e14));
		P1.addArc(new Arc(p1m0, p1m3, e15));
		P1.addArc(new Arc(p1m3, x1, e16));
		}

		Automaton P2 = new Automaton("P2");		{			P2.setType(AutomatonType.Specification);
		State p20 = new State("p20");	p20.setCost(0);	P2.addState(p20);	P2.setInitialState(p20);
		State p2m0 = new State("p2m0");	p2m0.setCost(9);	P2.addState(p2m0);
		State p2m1 = new State("p2m1");	p2m1.setCost(1);	P2.addState(p2m1);
		State p2m2 = new State("p2m2");	p2m2.setCost(5);	P2.addState(p2m2);
		State p2m3 = new State("p2m3");	p2m3.setCost(4);	P2.addState(p2m3);
		State p2m4 = new State("p2m4");	p2m4.setCost(7);	P2.addState(p2m4);
		State p2m5 = new State("p2m5");	p2m5.setCost(8);	P2.addState(p2m5);
		State x2 = new State("x2");	x2.setCost(0);	P2.addState(x2);	x2.setAccepting(true);
		LabeledEvent e26 = new LabeledEvent("e26");	P2.getAlphabet().addEvent(e26);
		LabeledEvent e25 = new LabeledEvent("e25");	P2.getAlphabet().addEvent(e25);
		LabeledEvent e24 = new LabeledEvent("e24");	P2.getAlphabet().addEvent(e24);
		LabeledEvent e23 = new LabeledEvent("e23");	P2.getAlphabet().addEvent(e23);
		LabeledEvent e22 = new LabeledEvent("e22");	P2.getAlphabet().addEvent(e22);
		LabeledEvent e21 = new LabeledEvent("e21");	P2.getAlphabet().addEvent(e21);
		LabeledEvent e20 = new LabeledEvent("e20");	P2.getAlphabet().addEvent(e20);
		P2.addArc(new Arc(p20, p2m2, e20));
		P2.addArc(new Arc(p2m2, p2m3, e21));
		P2.addArc(new Arc(p2m3, p2m5, e22));
		P2.addArc(new Arc(p2m5, p2m0, e23));
		P2.addArc(new Arc(p2m0, p2m1, e24));
		P2.addArc(new Arc(p2m1, p2m4, e25));
		P2.addArc(new Arc(p2m4, x2, e26));
		}

		Automaton P3 = new Automaton("P3");		{			P3.setType(AutomatonType.Specification);
		State p30 = new State("p30");	p30.setCost(0);	P3.addState(p30);	P3.setInitialState(p30);
		State p3m0 = new State("p3m0");	p3m0.setCost(5);	P3.addState(p3m0);
		State p3m1 = new State("p3m1");	p3m1.setCost(5);	P3.addState(p3m1);
		State p3m2 = new State("p3m2");	p3m2.setCost(5);	P3.addState(p3m2);
		State p3m3 = new State("p3m3");	p3m3.setCost(3);	P3.addState(p3m3);
		State p3m4 = new State("p3m4");	p3m4.setCost(8);	P3.addState(p3m4);
		State p3m5 = new State("p3m5");	p3m5.setCost(9);	P3.addState(p3m5);
		State x3 = new State("x3");	x3.setCost(0);	P3.addState(x3);	x3.setAccepting(true);
		LabeledEvent e36 = new LabeledEvent("e36");	P3.getAlphabet().addEvent(e36);
		LabeledEvent e35 = new LabeledEvent("e35");	P3.getAlphabet().addEvent(e35);
		LabeledEvent e34 = new LabeledEvent("e34");	P3.getAlphabet().addEvent(e34);
		LabeledEvent e33 = new LabeledEvent("e33");	P3.getAlphabet().addEvent(e33);
		LabeledEvent e32 = new LabeledEvent("e32");	P3.getAlphabet().addEvent(e32);
		LabeledEvent e31 = new LabeledEvent("e31");	P3.getAlphabet().addEvent(e31);
		LabeledEvent e30 = new LabeledEvent("e30");	P3.getAlphabet().addEvent(e30);
		P3.addArc(new Arc(p30, p3m1, e30));
		P3.addArc(new Arc(p3m1, p3m0, e31));
		P3.addArc(new Arc(p3m0, p3m2, e32));
		P3.addArc(new Arc(p3m2, p3m3, e33));
		P3.addArc(new Arc(p3m3, p3m4, e34));
		P3.addArc(new Arc(p3m4, p3m5, e35));
		P3.addArc(new Arc(p3m5, x3, e36));
		}

		Automaton P4 = new Automaton("P4");		{			P4.setType(AutomatonType.Specification);
		State p40 = new State("p40");	p40.setCost(0);	P4.addState(p40);	P4.setInitialState(p40);
		State p4m0 = new State("p4m0");	p4m0.setCost(3);	P4.addState(p4m0);
		State p4m1 = new State("p4m1");	p4m1.setCost(3);	P4.addState(p4m1);
		State p4m2 = new State("p4m2");	p4m2.setCost(9);	P4.addState(p4m2);
		State p4m3 = new State("p4m3");	p4m3.setCost(1);	P4.addState(p4m3);
		State p4m4 = new State("p4m4");	p4m4.setCost(5);	P4.addState(p4m4);
		State p4m5 = new State("p4m5");	p4m5.setCost(4);	P4.addState(p4m5);
		State x4 = new State("x4");	x4.setCost(0);	P4.addState(x4);	x4.setAccepting(true);
		LabeledEvent e46 = new LabeledEvent("e46");	P4.getAlphabet().addEvent(e46);
		LabeledEvent e45 = new LabeledEvent("e45");	P4.getAlphabet().addEvent(e45);
		LabeledEvent e44 = new LabeledEvent("e44");	P4.getAlphabet().addEvent(e44);
		LabeledEvent e43 = new LabeledEvent("e43");	P4.getAlphabet().addEvent(e43);
		LabeledEvent e42 = new LabeledEvent("e42");	P4.getAlphabet().addEvent(e42);
		LabeledEvent e41 = new LabeledEvent("e41");	P4.getAlphabet().addEvent(e41);
		LabeledEvent e40 = new LabeledEvent("e40");	P4.getAlphabet().addEvent(e40);
		P4.addArc(new Arc(p40, p4m2, e40));
		P4.addArc(new Arc(p4m2, p4m1, e41));
		P4.addArc(new Arc(p4m1, p4m4, e42));
		P4.addArc(new Arc(p4m4, p4m5, e43));
		P4.addArc(new Arc(p4m5, p4m0, e44));
		P4.addArc(new Arc(p4m0, p4m3, e45));
		P4.addArc(new Arc(p4m3, x4, e46));
		}

		Automaton P5 = new Automaton("P5");		{			P5.setType(AutomatonType.Specification);
		State p50 = new State("p50");	p50.setCost(0);	P5.addState(p50);	P5.setInitialState(p50);
		State p5m0 = new State("p5m0");	p5m0.setCost(10);	P5.addState(p5m0);
		State p5m1 = new State("p5m1");	p5m1.setCost(3);	P5.addState(p5m1);
		State p5m2 = new State("p5m2");	p5m2.setCost(1);	P5.addState(p5m2);
		State p5m3 = new State("p5m3");	p5m3.setCost(3);	P5.addState(p5m3);
		State p5m4 = new State("p5m4");	p5m4.setCost(4);	P5.addState(p5m4);
		State p5m5 = new State("p5m5");	p5m5.setCost(9);	P5.addState(p5m5);
		State x5 = new State("x5");	x5.setCost(0);	P5.addState(x5);	x5.setAccepting(true);
		LabeledEvent e56 = new LabeledEvent("e56");	P5.getAlphabet().addEvent(e56);
		LabeledEvent e55 = new LabeledEvent("e55");	P5.getAlphabet().addEvent(e55);
		LabeledEvent e54 = new LabeledEvent("e54");	P5.getAlphabet().addEvent(e54);
		LabeledEvent e53 = new LabeledEvent("e53");	P5.getAlphabet().addEvent(e53);
		LabeledEvent e52 = new LabeledEvent("e52");	P5.getAlphabet().addEvent(e52);
		LabeledEvent e51 = new LabeledEvent("e51");	P5.getAlphabet().addEvent(e51);
		LabeledEvent e50 = new LabeledEvent("e50");	P5.getAlphabet().addEvent(e50);
		P5.addArc(new Arc(p50, p5m1, e50));
		P5.addArc(new Arc(p5m1, p5m3, e51));
		P5.addArc(new Arc(p5m3, p5m5, e52));
		P5.addArc(new Arc(p5m5, p5m0, e53));
		P5.addArc(new Arc(p5m0, p5m4, e54));
		P5.addArc(new Arc(p5m4, p5m2, e55));
		P5.addArc(new Arc(p5m2, x5, e56));
		}

		Automata automata = new Automata();
		automata.addAutomaton(M0);
		automata.addAutomaton(M1);
		automata.addAutomaton(M2);
		automata.addAutomaton(M3);
		automata.addAutomaton(M4);
		automata.addAutomaton(M5);
		automata.addAutomaton(P0);
		automata.addAutomaton(P1);
		automata.addAutomaton(P2);
		automata.addAutomaton(P3);
		automata.addAutomaton(P4);
		automata.addAutomaton(P5);

		try
		{

		//	Automata auto = makeProblem(new FileReader("D:\\Temp\\FisherThompson6x6.txt"));
		//	System.out.println(auto.toCode());
//			AutomataToXml toXML = new AutomataToXml(automata);
//			toXML.writeCost(true);
//			toXML.serialize("D:\\Temp\\FisherThompson6x6.xml");
//
//			if(true) return;
//
//			// ModifiedAstar mastar = new ModifiedAstar(auto);
//			ModifiedAstar mastar = new ModifiedAstar(automata);
//		//	ModifiedAstar mastar = new ModifiedAstar(automata, new Estimator(automata), new KnutsManipulator());
//			Element elem = mastar.walk1();

//			System.out.println("Search complete(" + mastar.debugNum + "):");
//
//			System.out.println(mastar.trace(elem));

			SimpleEstimator se = new SimpleEstimator(automata);
			System.out.println("calc(P1) = " + se.calc(P2, P2.getInitialState()));
		}
		catch(Exception excp)
		{
			System.out.println("Exception: " + excp);
			excp.printStackTrace();
		}

	}

}