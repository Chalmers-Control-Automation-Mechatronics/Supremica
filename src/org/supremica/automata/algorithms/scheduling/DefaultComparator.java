/********************** DefaultComparator.java ******************/
// We need several different ways to compare two nodes for equality, see Section 6.2
// DefaultComparator compares according to eq (53)
// This comparator should return for compare(elem1, elem2)
// -1 if elem1 is "better" than elem2 			(elem1 < elem2)
//  0 if elem1 and elem2 are equally "good"		(elem1 == elem2)
// +1 if elem2 is "better" than elem1			(elem1 > elem2)
// Note that compare is called only for elements with equal logical state

package org.supremica.automata.algorithms.scheduling;

import java.util.*;

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
	