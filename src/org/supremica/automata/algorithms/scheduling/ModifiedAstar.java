/******************** ModifiedAstar.java **************************
 * MFs implementation of Tobbes modified Astar search algo
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.properties.SupremicaProperties;

// this is what populates the Open and Closed lists
// Implements the Comparable interface and wraps the int[] "states"
class Element 
	implements Comparable
{
	int f;		// the sum of g(n) and h(n)
	int g;		// the price to get here, g(n)
	int[] Tv;	// remaining processing time for each product in its current resource
	int[] state;// logical state
	public int EB[];	// used for limiting the node expansion
	public Element p;	// ptr to predecessor element
	
	public Element(int[] s, int tvlen)
	{
		this.state = s;
		this.Tv = new int[tvlen];
	}
	
	int[] getArray()
	{
		return state;
	}
	
	// Must really fix this int[] state madness - why no useful class?
	public String toString()
	{
		StringBuffer sbuf = new StringBuffer("[");
		for(int i = 0; i < state.length; ++i)
		{
			sbuf.append(state[i]);
			sbuf.append(".");
		}
		sbuf.append("] g = ");
		sbuf.append(g);
		sbuf.append(" Tv = [");
		for(int i = 0; i < Tv.length; ++i)
		{
			sbuf.append(Tv[i]);
			sbuf.append(".");
		}
		sbuf.append("]");
		
		return sbuf.toString();
	}
	
	public int compareTo(final Element cs)
		throws ClassCastException
	{
		if(cs.getArray().length != state.length)
		{
			throw new ClassCastException("Non-equal lengths");
		}
		
		for(int i = 0; i < state.length; ++i)
		{
			if(cs.getArray()[i] != state[i])
			{
				return cs.getArray()[i] - state[i];
			}
		}
		return 0;
	}
	
	public int compareTo(Object obj)
	{
		return compareTo((Element)obj);
	}
	
}	
/*
// For each set of problems, you need a singleton instance of ElementUpdater
// This object keeps track of all the autoamat and their indices etc
// This is so that Element does not have to have a ref to the automata to carry around
// All problem-static data are ciollected in ElementUptdater
// ElementUpdater should be the only friend of Element, but alas, we're doin Java here...
class ElementUpdater
{
	// private Automata automata;
	private int numProds; // the numProds first automata are the products/specs
	private AutomataIndexForm automata;
	
	ElementUpdater(Automata automata, int numProds)
		throws Exception
	{
		this.automata = new AutomataIndexForm(automata, new Automaton());
		this.numProds = numProds;
	}
	
	// Take a step in this direction and crate a new updated Element
	Element newElement(Element e, int direction)
	{
		Element element = new Element();
		
		for(int j = 0; j < numProds; ++j)
		{
			if(j != direction)
			{
				element.Tv[j] = java.lang.Math.max(0, e.Tv[j] - e.Tv[direction]);
			}
		}
		element.g = e.g + element.Tv[direction];
//		element.state = step(e.state, direction);
//		element.Tv[direction] = cost(element, direction);
		return element;
	}
*/	
	/* From this composite state, take a step in this direction
	private CompositeState step(CompositeState state, int direction)
	{
		
	}
	*/
	/* Return the cost of the current state in this direction
	private int cost(Element element, int direction)
	{
		State state = automata.getState(direction, element.state.getStateAt(direction));
		return state.getCost();
		
	}
	*/
// }

class ElementComparator
	implements Comparator
{
	private boolean areEqual(Element e1, Element e2) // see page 65
	{
		return	e1.state == e2.state // equal logical state
				&&
				e1.g == e2.g		// equal cost to here
				&&
				e1.Tv == e2.Tv;		// equal remain processing time for each product
	}
	
	protected int compare(Element e1, Element e2) // see page 65
	{
		if(e1.g != e2.g) return e1.g - e2.g;	// should be e1.f == e2.f? (see Main.cpp (130))
		else return 0;
		
	}
	
	public int compare(Object obj1, Object obj2)
	{
		return compare((Element)obj1, (Element)obj2);
	}
}

class Estimator
{
	// Assumptions about automata:
	//	* plants are the resources
	//	* specs are the product routes
	private Automata automata;
	
	public Estimator(Automata automata)	// Here we should precalculate the estimates
	{
		this.automata = automata;
	}
	
	public Automata getAutomata()		// Return the stored automata
	{
		return automata;
	}
	
	public int h()			// For this composite state, return an estimate
	{
		return 0;	// 0 is always less than the exact "estimate" h*, so should always give the optimal
	}
}

class TwoProductRelaxation 
	extends Estimator
{
	
	public TwoProductRelaxation(Automata automata)	// Here we should precalculate the estimates
	{
		super(automata);
		// calc the two-product relaxation estimates
	}
}

class OneMachineRelaxation
	extends Estimator
{
	public OneMachineRelaxation(Automata automata)	// Here we should precalculate the estimates
	{
		super(automata);
		// calc the one-machine relaxation estimates
	}
}

public class ModifiedAstar
{
	private static Logger logger = LoggerFactory.createLogger(ModifiedAstar.class);

	// Open and Closed are sorted sets of CompositeStates
	private TreeSet open = null;
	private TreeSet closed = null;
	// private Automata theAutomata;
	private Automata specAutomata;
	private AutomataOnlineSynchronizer onlineSynchronizer;
	
	public ModifiedAstar(Automata theAutomata)
		throws Exception
	{
		this.open = new TreeSet();
		this.closed = new TreeSet();	// store the managed states here
		// this.theAutomata = theAutomata;
		this.specAutomata = new Automata(); // is filled in below
		
		// Let's see if this is the way to do it, modeled after AutomataExplorer

		SynchronizationOptions syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, false, SupremicaProperties.verboseMode(), false, true);
		AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, syncOptions);

		// Build the initial state
		int[] initialState = AutomataIndexFormHelper.createState(theAutomata.size());

		for(Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton automaton = (Automaton) autIt.next();
			State currInitialState = automaton.getInitialState();
			initialState[automaton.getIndex()] = currInitialState.getIndex();
			automaton.remapStateIndices();		// Rebuild the maps to have the indices match up - why the f***??
			if(automaton.getType() == AutomatonType.Specification)
			{
				specAutomata.addAutomaton(automaton);
			}
		}

		this.onlineSynchronizer = new AutomataOnlineSynchronizer(helper);

		onlineSynchronizer.initialize();
		onlineSynchronizer.setCurrState(initialState);
		helper.setCoExecuter(onlineSynchronizer);

		// Put the initial state on open
		Element ini = new Element(initialState, specAutomata.size());
		open.add(ini);	

		/**/ System.out.println("Initial state: " + ini.toString());
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
	void walk1()
		throws Exception
	{
		int debugNum = 1;
		
		while(!open.isEmpty())
		{
			Element state = (Element)open.first();
			
			/**/ System.out.println("\n(" + debugNum++ + ") Glob State: " + state.toString());
			
			// From this state, move in all directions one step
			// First we need to find the events enabled in automaton i
			for(Iterator autIt = specAutomata.iterator(); autIt.hasNext(); )
			{
				onlineSynchronizer.setCurrState(state.getArray());	// we operate from this state
				Automaton currAutomaton = (Automaton) autIt.next();
				// We only look through specAutomata
				// if(currAutomaton.isSpecification())	// only do this for specs, plants/resources are not "directions"
				{
					int stateIndex = state.getArray()[currAutomaton.getIndex()];
					// Now we need to find the state with this index in currAutomaton
					State s = currAutomaton.getStateWithIndex(stateIndex);
					
					/**/ System.out.println("Part State: " + currAutomaton.getName() + "[" + stateIndex + ":" + s.getIndex() + "]::" + s.toString());
					
					// Now, let us iterate over all events (locally) enabled in this state
					EventIterator evit = currAutomaton.outgoingEventsIterator(s);
					while(evit.hasNext())
					{
						LabeledEvent event = evit.nextEvent();
						
						/* System.out.print("Event: " + currAutomaton.getName() + "::" + event.toString());
	*/
						if(onlineSynchronizer.isEnabled(event))	// if the event is globally enabled
						{
							/* System.out.println(" is globally enabled");
							*/
							// Move in direction Ai
							Element nextState = new Element(onlineSynchronizer.doTransition(event), specAutomata.size());
							// Here we should update g and Tv (and whatnot)
							move(state, nextState, currAutomaton);
							//
							
							// If we've not already seen it...
							if(!open.contains(nextState) && !closed.contains(nextState))
							{
								// ...put it on the list
								open.add(nextState);
								/* And show it to the public
								System.out.println();
								System.out.print(state.toString() + " - " + event.toString() + " -> " + nextState.toString());
								*/
							}
						}
						
						/**/ System.out.println();
					}
				}
			}
			// done with this state, put it on closed
			closed.add(state);
			// remove it from open
			open.remove(state);
		}
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
			Element state = (Element)open.first();
			
			/**/ System.out.println("\n(" + debugNum++ + ") Glob State: " + state.toString());
			
			// foreach event enabled in this state
			int[] events = onlineSynchronizer.getOutgoingEvents(state.getArray());
			for(int i = 0; i < events.length; ++i)
			{
				// What do we do now?
			}
		}		
	}

	// g and Tv are updated as (see p.47) 
	private void move(Element state, Element nxtstate, Automaton aut)
	{
		int direction = aut.getIndex();
		for(Iterator autit = specAutomata.iterator(); autit.hasNext(); )
		{
			Automaton automaton = (Automaton)autit.next();
			int autidx = automaton.getIndex();
			if(autidx != direction)
			{
				nxtstate.Tv[autidx] = Math.max(0, state.Tv[autidx] - state.Tv[direction]);
				/**/ System.out.println("Tv[" + autidx + "] = max(0, " + state.Tv[autidx] + " - " + state.Tv[direction] + ")");
			}
		}
		nxtstate.g = state.g + state.Tv[direction];
		nxtstate.Tv[direction] = aut.getStateWithIndex(nxtstate.getArray()[direction]).getCost();
	}
	
	public static void main(String args[])
	{
		// See Alorithm 1, Fig 5.16, p.47 in Tobbes lic
		Automaton p1 = new Automaton("P1");					p1.setType(AutomatonType.Specification);
		State q10 = new State("p1_0");	q10.setCost(0);		p1.addState(q10);	p1.setInitialState(q10);
		State q11 = new State("p1M1");	q11.setCost(3);		p1.addState(q11);
		State q12 = new State("p1M2");	q12.setCost(4);		p1.addState(q12);
		State q13 = new State("p1_3");	q13.setCost(0);		p1.addState(q13);	
		LabeledEvent e11 = new LabeledEvent("p1 in i m1");			p1.getAlphabet().addEvent(e11);
		LabeledEvent e12 = new LabeledEvent("p1 ut ur m1 in i m2");	p1.getAlphabet().addEvent(e12);
		LabeledEvent e13 = new LabeledEvent("p1 ut ur m2");			p1.getAlphabet().addEvent(e13);
		p1.addArc(new Arc(q10, q11, e11));
		p1.addArc(new Arc(q11, q12, e12));
		p1.addArc(new Arc(q12, q13, e13));

		Automaton p2 = new Automaton("P2");					p2.setType(AutomatonType.Specification);
		State q20 = new State("p2_0");	q20.setCost(0);		p2.addState(q20);	p2.setInitialState(q20);
		State q21 = new State("p2M1");	q21.setCost(1);		p2.addState(q21);
		State q22 = new State("p2M2");	q22.setCost(2);		p2.addState(q22);
		State q23 = new State("p2_3");	q23.setCost(0);		p2.addState(q23);
		LabeledEvent e21 = new LabeledEvent("p2 in i m1");			p2.getAlphabet().addEvent(e21);
		LabeledEvent e22 = new LabeledEvent("p2 ut ur m1 in i m2");	p2.getAlphabet().addEvent(e22);
		LabeledEvent e23 = new LabeledEvent("p2 ut ur m2");			p2.getAlphabet().addEvent(e23);
		p2.addArc(new Arc(q20, q21, e21));
		p2.addArc(new Arc(q21, q22, e22));
		p2.addArc(new Arc(q22, q23, e23));

		Automaton m1 = new Automaton("M1");		m1.setType(AutomatonType.Plant);
		State m10 = new State("m10");			m1.addState(m10);	m1.setInitialState(m10);
		State m11 = new State("m11");			m1.addState(m11);
		LabeledEvent em11 = new LabeledEvent("p1 in i m1");			m1.getAlphabet().addEvent(em11);
		LabeledEvent em12 = new LabeledEvent("p2 in i m1");			m1.getAlphabet().addEvent(em12);
		LabeledEvent em13 = new LabeledEvent("p1 ut ur m1 in i m2");m1.getAlphabet().addEvent(em13);
		LabeledEvent em14 = new LabeledEvent("p2 ut ur m1 in i m2");m1.getAlphabet().addEvent(em14);
		m1.addArc(new Arc(m10, m11, em11));
		m1.addArc(new Arc(m10, m11, em12));
		m1.addArc(new Arc(m11, m10, em13));
		m1.addArc(new Arc(m11, m10, em14));

		Automaton m2 = new Automaton("M2");		m2.setType(AutomatonType.Plant);
		State m20 = new State("m20");			m2.addState(m20);	m2.setInitialState(m20);
		State m21 = new State("m21");			m2.addState(m21);
		LabeledEvent em21 = new LabeledEvent("p1 ut ur m1 in i m2");m2.getAlphabet().addEvent(em21);
		LabeledEvent em22 = new LabeledEvent("p2 ut ur m1 in i m2");m2.getAlphabet().addEvent(em22);
		LabeledEvent em23 = new LabeledEvent("p1 ut ur m2");		m2.getAlphabet().addEvent(em23);
		LabeledEvent em24 = new LabeledEvent("p2 ut ur m2");		m2.getAlphabet().addEvent(em24);
		m2.addArc(new Arc(m20, m21, em21));
		m2.addArc(new Arc(m20, m21, em22));
		m2.addArc(new Arc(m21, m20, em23));
		m2.addArc(new Arc(m21, m20, em24));
		
		Automata automata = new Automata();
		automata.addAutomaton(p1);
		automata.addAutomaton(p2);
		automata.addAutomaton(m1);
		automata.addAutomaton(m2);
		
		// This one guarantees that only the route shown on p 47 is followed
		// Note that without route we will not get the shown g and Tv results 
		Automaton route = new Automaton("route");		route.setType(AutomatonType.Plant);
		State r0 = new State("r0");			route.addState(r0);	route.setInitialState(r0);
		State r1 = new State("r1");			route.addState(r1);
		State r2 = new State("r2");			route.addState(r2);
		State r3 = new State("r3");			route.addState(r3);
		State r4 = new State("r4");			route.addState(r4);
		State r5 = new State("r5");			route.addState(r5);
		State r6 = new State("r6");			route.addState(r6);
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
		
		automata.addAutomaton(route);
		
		try
		{
			ModifiedAstar mastar = new ModifiedAstar(automata);
			mastar.walk1();
		}
		catch(Exception excp)
		{
			System.out.println("Exception: " + excp);
			excp.printStackTrace();
		}
		
	}
	
}