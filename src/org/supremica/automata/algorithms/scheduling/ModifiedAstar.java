/******************** ModifiedAstar.java **************************
 * MFs implementation of Tobbes modified Astar search algo
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.properties.SupremicaProperties;

class Element // this is what populates the Open and Closed lists
{
	public int f;		// the sum of g(n) and h(n)
	public int g;		// the price to get here, g(n)
	// public Vector Tv;	// vector of remaining processing time for each product in its current resource
	public int Tv[]; 
	// public Vector Qv;	// vector representing the current logical product state
	public int EB[];	// used for limiting the node expansion
	public Element p;	// ptr to predecessor element
	
	public CompositeState state;	// the logical state, called Qv on page 50

	Element(int numProds)
	{
		this.Tv = new int[numProds];
		this.EB = new int[numProds];
	}
}

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
		Element element = new Element(numProds);
		
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
}

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

	private Estimator estimator = null;
	
	// Open and Closed are sorted sets of Elements, sorted on smallest f.
	private Set open = null;
	private Set closed = null;
	
	public ModifiedAstar(Estimator estimator, Comparator comparator)
	{
		this.estimator = estimator;
		this.open = new TreeSet(comparator);
		this.closed = new TreeSet(comparator);
	}
	
	// This one's for testing, it calcs the g() values and puts'em on the open list
	// Each state (except the initial and final ones) have a cost.
	// g is updated as (se p.47) 
	static void walk(Automata theAutomata)
		throws Exception
	{
		// Let's see if this is the way to do it, modeled after AutomataExplorer

		SynchronizationOptions syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, false, SupremicaProperties.verboseMode(), false, true);
		AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, syncOptions);

		// Build the initial state
		int[] initialState = AutomataIndexFormHelper.createState(theAutomata.size());

		Iterator autIt = theAutomata.iterator();
		Automaton currAutomaton;
		State currInitialState;
		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();
			currInitialState = currAutomaton.getInitialState();
			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
		}

//		AutomataExplorerHelper.setInitialState(initialState);

		AutomataOnlineSynchronizer onlineSynchronizer = new AutomataOnlineSynchronizer(helper);

		onlineSynchronizer.initialize();
		onlineSynchronizer.setCurrState(initialState);
		helper.setCoExecuter(onlineSynchronizer);

		// Initialization done, now what...
		
		// Put the initial state on a list
		TreeSet open = new TreeSet();
		open.add(initialState);
		
		TreeSet closed = new TreeSet(); // store the managed states here
		
		while(!open.isEmpty())
		{
			int[] state = (int[])open.first();	// get the first element on this list
			
			// From this state, move in all directions one step
			// First we need to find the events enabled in automaton i
			for(autIt = theAutomata.iterator(); autIt.hasNext(); )
			{
				currAutomaton = (Automaton) autIt.next();
				int stateIndex = state[currAutomaton.getIndex()];
				// Now we need to find the state with this index in currAutomaton
				State s = currAutomaton.getStateWithIndex(stateIndex);
				// Now, let us iterate over all events enabled in this state
				EventIterator evit = currAutomaton.outgoingEventsIterator(s);
				while(evit.hasNext())
				{
					LabeledEvent event = evit.nextEvent();
					int[] nextState = onlineSynchronizer.doTransition(event);
					// If we've not already seen it...
					if(!open.contains(nextState) && !closed.contains(nextState))
					{
						// ...put it on the list
						open.add(nextState);
						// And show it to the public
						logger.debug(state.toString() + " - " + event.toString() + " -> " + nextState.toString());
					}
				}
			}
			// done with this state, put it on closed
			closed.add(state);
		}
	}
	
	public static void main()
	{
		Automaton p1 = new Automaton("P1");
		State q10 = new State("p1_0");	q10.setCost(0);	p1.addState(q10);
		State q11 = new State("p1M1");	q11.setCost(3);	p1.addState(q11);
		State q12 = new State("p1M2");	q12.setCost(4);	p1.addState(q12);
		State q13 = new State("p1_3");	q13.setCost(0);	p1.addState(q13);	
		
		Automaton p2 = new Automaton("P2");
		State q20 = new State("p2_0");	q20.setCost(0);	p2.addState(q10);
		State q21 = new State("p2M1");	q21.setCost(1);	p2.addState(q11);
		State q22 = new State("p2M2");	q22.setCost(2);	p2.addState(q12);
		State q23 = new State("p2_3");	q23.setCost(0);	p2.addState(q13);
		
		Automaton m1 = new Automaton("M1");
		State m10
		
		Automaton m2 = new Automaton("M2");
		
		
		Automata automata = new Automata();
		automata.addAutomaton(p1);
		automata.addAutomaton(p2);
		automata.addAutomaton(m1);
		automata.addAutomaton(m2);
		
		try
		{
			walk(automata);
		}
		catch(Exception excp)
		{
			System.out.println("Exception: " + excp);
		}
		
	}
	
}