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

import java.util.*;

import com.objectspace.jgl.BinaryPredicate;
import com.objectspace.jgl.Range;
import com.objectspace.jgl.OrderedSetIterator;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.properties.SupremicaProperties;

import com.objectspace.jgl.MultiSet; // actually my own...

//
interface ElementInterface
	extends Comparable
{
	int getBound();
	void setBound(int b);
	int getCost();
	void setCost(int i);
	int[] getStateArray();
	int[] getTimeArray();
	void setTime(int index, int time);
	String toString();
	int compareState(final ElementInterface ef);
	int compareRemainingTime(final ElementInterface ef);
	int compareCost(final ElementInterface ef);
	int compareBound(final ElementInterface ef);
	
	ElementInterface getParent();
	void setParent(ElementInterface ef);
	
	OrderedSetIterator getStateIterator();
	OrderedSetIterator getBoundIterator();
	void setStateIterator(OrderedSetIterator it);
	void setBoundIterator(OrderedSetIterator it);
}
// this is what populates the Open and Closed lists
// Implements the Comparable interface and wraps the int[] "states"
class Element 
	implements ElementInterface
{
	private int f;		// the sum of g(n) and h(n)
	private int g;		// the price to get here, g(n)
	private int[] Tv;	// remaining processing time for each product in its current resource
	private int[] state;// logical state
	private int EB[];	// used for limiting the node expansion
	private ElementInterface parent;	// ptr to predecessor element

	private OrderedSetIterator state_it = null;
	private OrderedSetIterator bound_it = null;

	public Element(int[] s, int tvlen)
	{
		this.state = s;
		this.Tv = new int[tvlen];
	}
	
	public int getBound()
	{
		return f;
	}
	public void setBound(int b)
	{
		f = b;
	}
	
	public int getCost()
	{
		return g;
	}
	
	public void setCost(int c)
	{
		g = c;
	}
	
	public int[] getStateArray()
	{
		return state;
	}
	
	public int[] getTimeArray()
	{
		return Tv;
	}
	
	public void setTime(int index, int time)
	{
		Tv[index] = time;
	}
	
	public ElementInterface getParent()
	{
		return parent;
	}
	
	public void setParent(ElementInterface ef)
	{
		parent = ef;
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
		sbuf.append(" f = ");
		sbuf.append(f);
		sbuf.append(" Tv = [");
		for(int i = 0; i < Tv.length; ++i)
		{
			sbuf.append(Tv[i]);
			sbuf.append(".");
		}
		sbuf.append("]");
		
		return sbuf.toString();
	}
	
	// lexicographic compare
	public int compareState(final ElementInterface elem)
		throws ClassCastException
	{
		if(elem.getStateArray().length != this.state.length)
		{
			throw new ClassCastException("Non-equal state vector lengths");
		}
		
		for(int i = 0; i < this.state.length - AutomataIndexFormHelper.STATE_EXTRA_DATA; ++i)
		{
			if(elem.getStateArray()[i] != this.state[i])
			{
				return this.state[i] - elem.getStateArray()[i];
			}
		}
		return 0;
	}
	
	public int compareRemainingTime(final ElementInterface elem)
		throws ClassCastException
	{
		if(elem.getTimeArray().length != Tv.length)
		{
			throw new ClassCastException("Non-equal time vector lengths");
		}
		
		for(int i = 0; i < Tv.length; ++i)
		{
			if(elem.getTimeArray()[i] != Tv[i])
			{
				return elem.getTimeArray()[i] - Tv[i];
			}
		}
		return 0;
	}
	
	// Return <0 if this < elem
	// Return 0 if this == elem
	// Return >0 if this > elem
	public int compareCost(final ElementInterface elem)
	{
		return this.getCost() - elem.getCost();
	}
	
	public int compareBound(final ElementInterface elem)
	{
		return this.getBound() - elem.getBound();
	}
	
	//-- Note -- this *only* compares the logical state
	public int compareTo(final ElementInterface elem)
		throws ClassCastException
	{
		return compareState(elem);
	}
	
	public int compareTo(Object obj)
	{
		return compareTo((ElementInterface)obj);
	}

	public OrderedSetIterator getStateIterator()
	{
		return state_it;
	}
	
	public OrderedSetIterator getBoundIterator()
	{
		return bound_it;
	}
	
	public void setStateIterator(OrderedSetIterator it)
	{
		state_it = it;
	}
	
	public void setBoundIterator(OrderedSetIterator it)
	{
		bound_it = it;
	}

}
// We need several different ways to compare two nodes for equality, see Section 6.2
// DefaultComparator compares according to eq (53)
// This comparator should return for compare(elem1, elem2)
// -1 if elem1 is "better" than elem2 			(elem1 < elem2)
//  0 if elem1 and elem2 are equally "good"		(elem1 == elem2)
// +1 if elem2 is "better" than elem1			(elem1 > elem2)
// Note that compare is called only for elements with equal logical state
class DefaultComparator
	implements Comparator
{
		public int compare(Element e1, Element e2)
		{
			// Compare according to eq (53), g(n') - g(n) >= max(Tvn[i] - Tvn'[i])
			// How do we turn this into a smaller/larger-than integer?
			return e1.compareRemainingTime(e2);
		}
		
		public int compare(Object o1, Object o2)
		{
			return compare((Element)o1, (Element)o2);
		}
}

// Implementations of the Manipulator interface determine whether to discard the new element or the old ones
// manpulator::manipulate should return true if the new element (elem) should be added by the Structure
// Why a specific class/interface for this?
// We're not sure exactly by what criteria to determine which element to keep.
// Manipulators let us easily experiment
interface Manipulator
{
	public boolean manipulate(ElementInterface elem, OrderedSetIterator begin, OrderedSetIterator beyond, Structure struct);
}
// The DefaultManipulator decides on a one-on-one basis
// It uses Structure.remove(OrderedSetIterator) to remove any existing elemnts that should be removed
class DefaultManipulator
	implements Manipulator
{
	Comparator comparator;
	
	public DefaultManipulator()
	{
		this.comparator = new DefaultComparator();
	}
	
	public boolean manipulate(ElementInterface elem, OrderedSetIterator begin, OrderedSetIterator beyond, Structure struct)
	{
		// Discard elem only if it is worse than *all* others, else keep it
		// Never remove any element that's already on the list
		boolean keepelem = true;
		for(OrderedSetIterator it = new OrderedSetIterator(begin); !it.equals(beyond); it.advance())
		{
			ElementInterface e1 = (ElementInterface)it.get();
			int result = comparator.compare(elem, e1);
			
			if(result == 0) // both are equally good, keep both
			{
				return true;
			}
			else if(result < 0)	// elem is better, discard e1
			{
				// struct.remove(it);
				return true;
			}
			else // result > 0, elem is worse, discard it
			{
				keepelem = false;
			}
		}
		return keepelem;
	}
}
//
class KnutsManipulator
	implements Manipulator
{
	public boolean manipulate(ElementInterface elem, OrderedSetIterator begin, OrderedSetIterator beyond, Structure struct)
	{
		boolean keepelem = true;
		for(OrderedSetIterator it = new OrderedSetIterator(begin); !it.equals(beyond); it.advance())
		{
			ElementInterface ef = (ElementInterface)it.get();
			if(elem.getBound() > ef.getBound())
			{
				return false;
			}
		}
		return true;
	}
} 
// This class implements a two-way sorted structure of Elements
// This is so since we need to sort both on the state and the time
// Note, we work with iterators
class Structure
{
	private static class StateComparator
		implements /* Comparator, */ BinaryPredicate
	{
		int compare(ElementInterface e1, ElementInterface e2)
		{
			return e1.compareTo(e2);
		}
		
		public int compare(Object obj1, Object obj2)
		{
			return compare((ElementInterface)obj1, (ElementInterface)obj2);
		}
		
		public boolean execute(Object obj1, Object obj2)
		{
			return compare(obj1, obj2) < 0;
		}
	 }
	
	private static class BoundComparator
		implements /* Comparator, */ BinaryPredicate
	{
		int compare(ElementInterface e1, ElementInterface e2)
		{
			return e1.compareBound(e2);
		}
		
		public int compare(Object o1, Object o2)
		{
			return compare((ElementInterface)o1, (ElementInterface)o2);
		}
		public boolean execute(Object obj1, Object obj2)
		{
			return compare(obj1, obj2) < 0;
		}
	}
	
	private MultiSet stateSet = new MultiSet(new StateComparator());
	private MultiSet boundSet = new MultiSet(new BoundComparator());
	private Manipulator manipulator;
	
	public Structure()
	{
		this.manipulator = new DefaultManipulator();
	}
	
	public Structure(Manipulator manipulator)
	{
		this.manipulator = manipulator;
	}
	
	// Add to both lists
	// First check if the same logical state has already been seen
	// If so, decide which to keep, note, we may need to keep both!
	public void add(ElementInterface elem)
	{
		Range range = stateSet.equalRange(elem);
		final OrderedSetIterator begin = (OrderedSetIterator)range.begin;
		final OrderedSetIterator end = (OrderedSetIterator)range.end;
		if(!begin.atEnd())
		{	
			// something like it already exists
			// determine what to keep and what not to keep
			if(manipulator.manipulate(elem, begin, end, this) == false)
			{
				return;	// discard elem
			}
			// else, add elem (below)
		}
		// else it did not already exist - add it
		addElement(elem);
	}
	
	public void addElement(ElementInterface elem)
	{
		OrderedSetIterator state_it = stateSet.insert(elem);
		OrderedSetIterator bound_it = boundSet.insert(elem);
		elem.setStateIterator(state_it);
		elem.setBoundIterator(bound_it);
	} 
	
	public Range equalRange(ElementInterface elem)
	{
		return stateSet.equalRange(elem);
	}
	
	public boolean isEmpty()
	{
		return stateSet.isEmpty();
	}
	
	public OrderedSetIterator first()	// 'first' means first on boundSet
	{
		return boundSet.begin();
	}
	
	public boolean contains(ElementInterface elem)	// 'contains' is determined by stateSet
	{
		return stateSet.contains(elem);
	}
	
	// Remove the particular element pointed to by this iterator
	public void remove(OrderedSetIterator osi)
	{
		ElementInterface elem = (ElementInterface)osi.get();
		stateSet.remove(elem.getStateIterator());
		boundSet.remove(elem.getBoundIterator());		
	}
	public void remove(ElementInterface elem)
	{
		stateSet.remove(elem.getStateIterator());
		boundSet.remove(elem.getBoundIterator());		
	}
	
	public String toString()
	{
		return boundSet.toString() + "\n\t" + stateSet.toString();
	}
}
// Note: "if n' required ptr adjustment and was found on closed, reopen it" (p.49)
// When an element should be reopened, what do we do?
// By default, reopen *all* elements that look like n'/elem
class Reopener
{
	static void reopen(ElementInterface elem, Structure open, Structure closed)
	{
		// We *know* elem is on closed
		final Range range = closed.equalRange(elem);
		
		for(OrderedSetIterator it = (OrderedSetIterator)range.begin; it.equals(range.end); it.advance())
		{
			ElementInterface e = (ElementInterface)it.get();
			// remove this one from closed, add it to open
			closed.remove(it);
			open.addElement(e);
		}
	}
}
//
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
	
	public int h(ElementInterface state)			// For this composite state, return an estimate
	{
		return 0;	// 0 is always less than the exact "estimate" h*, so should always give the optimal
	}
}
// Knuts estimator tries to use max(Tv[i]) as estimate
class KnutsEstimator
	extends Estimator
{
	public KnutsEstimator(Automata automata)
	{
		super(automata);
	}
	
	public int h(ElementInterface state)			// For this composite state, return an estimate
	{
		int[] arr = state.getTimeArray();
		int max = 0;
		for(int i = 0; i < arr.length; ++i)
		{
			if(arr[i] > max)
			{
				max = arr[i];
			}
		}
		return max;
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
//
class OneMachineRelaxation
	extends Estimator
{
	public OneMachineRelaxation(Automata automata)	// Here we should precalculate the estimates
	{
		super(automata);
		// calc the one-machine relaxation estimates
	}
}
//
class Capsule
	implements ElementInterface
{
	private OrderedSetIterator it;

	public Capsule(OrderedSetIterator it)
	{
		this.it = it;
	}
	
	public int getBound() { return ((ElementInterface)it.get()).getBound(); }
	public void setBound(int i) { ((ElementInterface)it.get()).setBound(i); }
	public int getCost() { return ((ElementInterface)it.get()).getCost(); }
	public void setCost(int i) { ((ElementInterface)it.get()).setCost(i); }
	
	public int[] getStateArray() { return ((ElementInterface)it.get()).getStateArray(); }
	public int[] getTimeArray() { return ((ElementInterface)it.get()).getTimeArray(); }
	public void setTime(int index, int time) { ((ElementInterface)it.get()).setTime(index, time); }
	
	public String toString() { return ((ElementInterface)it.get()).toString(); }
	
	public int compareState(final ElementInterface ef) { return ((ElementInterface)it.get()).compareState(ef); }
	public int compareRemainingTime(final ElementInterface ef) { return ((ElementInterface)it.get()).compareRemainingTime(ef); }
	public int compareCost(final ElementInterface ef) { return ((ElementInterface)it.get()).compareCost(ef); }
	public int compareBound(final ElementInterface ef) { return ((ElementInterface)it.get()).compareBound(ef); }
	
	public ElementInterface getParent() { return ((ElementInterface)it.get()).getParent(); }
	public void setParent(ElementInterface ef) { ((ElementInterface)it.get()).setParent(ef); }

	public int compareTo(final ElementInterface elem) { return ((ElementInterface)it.get()).compareTo(elem); }
	public int compareTo(final Object obj) { return ((ElementInterface)it.get()).compareTo(obj); }

	public OrderedSetIterator getStateIterator() { return ((ElementInterface)it.get()).getStateIterator(); }
	public OrderedSetIterator getBoundIterator() { return ((ElementInterface)it.get()).getBoundIterator(); }
	public void setStateIterator(OrderedSetIterator itx)  { ((ElementInterface)it.get()).setStateIterator(itx); }
	public void setBoundIterator(OrderedSetIterator itx)  { ((ElementInterface)it.get()).setBoundIterator(itx); }

}

public class ModifiedAstar
{
	private static Logger logger = LoggerFactory.createLogger(ModifiedAstar.class);

	// Open and Closed are sorted sets of CompositeStates
	private Structure /* MultiSet */ open = new Structure();
	private Structure /* MultiSet */ closed = new Structure();	// store the managed states here
	// private Automata theAutomata;
	private Estimator estimator;
	private Reopener reopener;
	private Automata specAutomata;
	private AutomataOnlineSynchronizer onlineSynchronizer;
	
	public ModifiedAstar(Automata theAutomata, Estimator estimator, Manipulator manipulator)
		throws Exception
	{
		this.estimator = estimator;
		this.specAutomata = new Automata(); // is filled in below
		this.open = new Structure(manipulator);
		this.closed = new Structure(manipulator);
		init(theAutomata);
	}

	public ModifiedAstar(Automata theAutomata, Estimator estimator)
		throws Exception
	{
		this.estimator = estimator;
		this.specAutomata = new Automata(); // is filled in below
		init(theAutomata);
	}
	
	public ModifiedAstar(Automata theAutomata)
		throws Exception
	{
		this.estimator = new Estimator(theAutomata);
		this.specAutomata = new Automata(); // is filled in below
		init(theAutomata);

	}
	private void init(Automata theAutomata)
		throws Exception
	{	
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
	
	// Check if this state is marked by all spec automata
	boolean isMarked(ElementInterface state)
	{
		for(Iterator autIt = specAutomata.iterator(); autIt.hasNext(); )
		{
			onlineSynchronizer.setCurrState(state.getStateArray());	// we operate from this state
			Automaton currAutomaton = (Automaton) autIt.next();
			// We only look through specAutomata
			if(currAutomaton.isSpecification())	// only do this for specs, plants/resources are not "directions"
			{
				int stateIndex = state.getStateArray()[currAutomaton.getIndex()];
				// Now we need to find the state with this index in currAutomaton
				State s = currAutomaton.getStateWithIndex(stateIndex);
				if(!s.isAccepting())
				{
					System.out.println(state.toString() + " is not marked");
					return false;
				}
			}
		}
		System.out.println(state.toString() + " is marked, yes!");
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
	public ElementInterface walk1()
		throws Exception
	{
		int debugNum = 1;
		
		while(!open.isEmpty())
		{
			// Here we should intelligently select the state to manipulate
			// Intelligently means lowest bound
			// ElementInterface state = (ElementInterface)open.first();
			Capsule state = new Capsule(open.first());
			
			/**/ System.out.println("\n(" + debugNum++ + ") Glob State: " + state.toString());
			
			if(isMarked(state))
			{
				return state; // we've reached the goal, are done
			}
			
			// From this state, move in all directions one step
			// First we need to find the events enabled in automaton i
			for(Iterator autIt = specAutomata.iterator(); autIt.hasNext(); )
			{
				onlineSynchronizer.setCurrState(state.getStateArray());	// we operate from this state
				Automaton currAutomaton = (Automaton) autIt.next();
				// We only look through specAutomata
				if(currAutomaton.isSpecification())	// only do this for specs, plants/resources are not "directions"
				{
					int stateIndex = state.getStateArray()[currAutomaton.getIndex()];
					// Now we need to find the state with this index in currAutomaton
					State s = currAutomaton.getStateWithIndex(stateIndex);
					
					/**/ System.out.println("Part State: " + currAutomaton.getName() + "[" + stateIndex + ":" + s.getIndex() + "]::" + s.toString());
					
					// Now, let us iterate over all events (locally) enabled in this state
					EventIterator evit = currAutomaton.outgoingEventsIterator(s);
					while(evit.hasNext())
					{
						LabeledEvent event = evit.nextEvent();
						
						System.out.print("Event: " + currAutomaton.getName() + "::" + event.toString());

						if(onlineSynchronizer.isEnabled(event))	// if the event is globally enabled
						{
							System.out.println(" is globally enabled");
							
							// Move in direction Ai
							ElementInterface nextState = new Element(onlineSynchronizer.doTransition(event), specAutomata.size());
							move(state, nextState, currAutomaton);
							
							// attach ptr back to n
							nextState.setParent(state);
							
							// If we've not already seen it (same logical state)...
							boolean onopen = open.contains(nextState);
							boolean onclosed = closed.contains(nextState);
							if(!onopen && !onclosed)
							{
								// ...calc the estimate
								nextState.setBound(nextState.getCost() + estimator.h(nextState));
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
						
						/**/ System.out.println();
					}
				}
			}
			// done with this state, remove it from open
			open.remove(state);
 			// and put it on closed
			closed.add(state);
			// Note! Must be done in that order, adding alters the stored iterators
			// If you add before removing the iterators wont be pointing right
			System.out.println("Open: " + open.toString());
		}
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
			ElementInterface state = new Capsule(open.first());
			
			/**/ System.out.println("\n(" + debugNum++ + ") Glob State: " + state.toString());
			
			// foreach event enabled in this state
			int[] events = onlineSynchronizer.getOutgoingEvents(state.getStateArray());
			for(int i = 0; i < events.length; ++i)
			{
				// What do we do now?
			}
		}		
	}

	// Take a step in the direction of aut
	// g and Tv are updated as (see p.47) 
	private void move(ElementInterface state, ElementInterface nxtstate, Automaton aut)
	{
		int direction = aut.getIndex();
		for(Iterator autit = specAutomata.iterator(); autit.hasNext(); )
		{
			Automaton automaton = (Automaton)autit.next();
			int autidx = automaton.getIndex();
			if(autidx != direction)
			{
				nxtstate.getTimeArray()[autidx] = Math.max(0, state.getTimeArray()[autidx] - state.getTimeArray()[direction]);
				/**/ System.out.println("Tv[" + autidx + "] = max(0, " + state.getTimeArray()[autidx] + " - " + state.getTimeArray()[direction] + ")");
			}
		}
		nxtstate.setCost(state.getCost() + state.getTimeArray()[direction]);
		nxtstate.setTime(direction, aut.getStateWithIndex(nxtstate.getStateArray()[direction]).getCost());
	}
	
	public static void main(String args[])
	{
		/* See Alorithm 1, Fig 5.16, p.47 in Tobbes lic
		Automaton p1 = new Automaton("P1");					p1.setType(AutomatonType.Specification);
		State q10 = new State("p1_0");	q10.setCost(0);		p1.addState(q10);	p1.setInitialState(q10);
		State q11 = new State("p1M1");	q11.setCost(3);		p1.addState(q11);
		State q12 = new State("p1M2");	q12.setCost(4);		p1.addState(q12);
		State q13 = new State("p1_3");	q13.setCost(0);		p1.addState(q13);	q13.setAccepting(true);	
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
		State q23 = new State("p2_3");	q23.setCost(0);		p2.addState(q23);	q23.setAccepting(true);
		LabeledEvent e21 = new LabeledEvent("p2 in i m1");			p2.getAlphabet().addEvent(e21);
		LabeledEvent e22 = new LabeledEvent("p2 ut ur m1 in i m2");	p2.getAlphabet().addEvent(e22);
		LabeledEvent e23 = new LabeledEvent("p2 ut ur m2");			p2.getAlphabet().addEvent(e23);
		p2.addArc(new Arc(q20, q21, e21));
		p2.addArc(new Arc(q21, q22, e22));
		p2.addArc(new Arc(q22, q23, e23));

		Automaton m1 = new Automaton("M1");		m1.setType(AutomatonType.Plant);
		State m10 = new State("m10");			m1.addState(m10);	m1.setInitialState(m10);	m10.setAccepting(true);
		State m11 = new State("m11");			m1.addState(m11);								m11.setAccepting(true);
		LabeledEvent em11 = new LabeledEvent("p1 in i m1");			m1.getAlphabet().addEvent(em11);
		LabeledEvent em12 = new LabeledEvent("p2 in i m1");			m1.getAlphabet().addEvent(em12);
		LabeledEvent em13 = new LabeledEvent("p1 ut ur m1 in i m2");m1.getAlphabet().addEvent(em13);
		LabeledEvent em14 = new LabeledEvent("p2 ut ur m1 in i m2");m1.getAlphabet().addEvent(em14);
		m1.addArc(new Arc(m10, m11, em11));
		m1.addArc(new Arc(m10, m11, em12));
		m1.addArc(new Arc(m11, m10, em13));
		m1.addArc(new Arc(m11, m10, em14));

		Automaton m2 = new Automaton("M2");		m2.setType(AutomatonType.Plant);
		State m20 = new State("m20");			m2.addState(m20);	m2.setInitialState(m20);	m20.setAccepting(true);
		State m21 = new State("m21");			m2.addState(m21);								m21.setAccepting(true);
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
		
		// This is the example on page 66
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
		automata.addAutomaton(m5);

		try
		{
			ModifiedAstar mastar = new ModifiedAstar(automata);
		// 	ModifiedAstar mastar = new ModifiedAstar(automata, new Estimator(automata), new KnutsManipulator());
			ElementInterface elem = mastar.walk1();
			
			System.out.println("Search complete:");
			// Track ptrs backward
			boolean doit = true;
			while(doit)
			{
				System.out.println(elem.toString());
				System.out.println("<<---");
				
				if(elem.getParent() != null)
				{
					elem = elem.getParent();
				}
				else
				{
					doit = false;
				}
			}
			
			
		}
		catch(Exception excp)
		{
			System.out.println("Exception: " + excp);
			excp.printStackTrace();
		}
		
	}
	
}