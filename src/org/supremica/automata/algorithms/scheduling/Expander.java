/********************* Expanderjava **********************/
// Interface for expanding children
// Collection is a superinterface of 
// 		List, Set, SortedSet 
// Classes implementing the Collection interface include'
//		AbstractCollection, AbstractList, AbstractSet, ArrayList, 
//		HashSet, LinkedHashSet, LinkedList, TreeSet, Vector 

package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.automata.*;

public interface Expander
{
	public Collection expand(Element elem);	// get a collection of the elements that are children of elem
	public Element getInitialState();
	public Automata getSpecs();
}