/*************************** Structure.java ********************/
// This class implements a two-way sorted structure of Elements
// This is so since we need to sort both on the state and the time
// Note, we work with iterators

package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.BinaryPredicate;
import com.objectspace.jgl.Range;
import com.objectspace.jgl.OrderedSetIterator;

import com.objectspace.jgl.MultiSet;

class Structure
{
	private static class StateComparator
		implements /* Comparator, */ BinaryPredicate
	{
		int compare(Element e1, Element e2)
		{
			return e1.compareTo(e2);
		}
		
		public int compare(Object obj1, Object obj2)
		{
			return compare((Element)obj1, (Element)obj2);
		}
		
		public boolean execute(Object obj1, Object obj2)
		{
			return compare(obj1, obj2) < 0;
		}
	 }
	
	private static class BoundComparator
		implements /* Comparator, */ BinaryPredicate
	{
		int compare(Element e1, Element e2)
		{
			return e1.compareBound(e2);
		}
		
		public int compare(Object o1, Object o2)
		{
			return compare((Element)o1, (Element)o2);
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
	public void add(Element elem)
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
	
	public void addElement(Element elem)
	{
		OrderedSetIterator state_it = stateSet.insert(elem);
		OrderedSetIterator bound_it = boundSet.insert(elem);
		elem.setStateIterator(state_it);
		elem.setBoundIterator(bound_it);
	} 
	
	public Range equalRange(Element elem)
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
	
	public boolean contains(Element elem)	// 'contains' is determined by stateSet
	{
		return stateSet.contains(elem);
	}
	
	// Remove the particular element pointed to by this iterator
	public void remove(OrderedSetIterator osi)
	{
		Element elem = (Element)osi.get();
		stateSet.remove(elem.getStateIterator());
		boundSet.remove(elem.getBoundIterator());		
	}
	public void remove(Element elem)
	{
		stateSet.remove(elem.getStateIterator());
		boundSet.remove(elem.getBoundIterator());		
	}
	
	public void clear()
	{
		stateSet.clear();
		boundSet.clear();
	}
	
	public String toString()
	{
		return boundSet.toString() + "\n\t" + stateSet.toString();
	}
}
	