
/********************** DefaultManipulator.java *********************/

// The DefaultManipulator decides on a one-on-one basis
// It uses Structure.remove(OrderedSetIterator) to remove any existing elemnts that should be removed
package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import com.objectspace.jgl.OrderedSetIterator;

class DefaultManipulator
	implements Manipulator
{
	Comparator comparator;

	public DefaultManipulator()
	{
		this.comparator = new DefaultComparator();
	}

	public boolean manipulate(Element elem, OrderedSetIterator begin, OrderedSetIterator beyond, Structure struct)
	{

		// Discard elem only if it is worse than *all* others, else keep it
		// Never remove any element that's already on the list
		boolean keepelem = true;

		for (OrderedSetIterator it = new OrderedSetIterator(begin);
				!it.equals(beyond); it.advance())
		{
			Element e1 = (Element) it.get();
			int result = comparator.compare(elem, e1);

			if (result == 0)    // both are equally good, keep both
			{
				return true;
			}
			else if (result < 0)    // elem is better, discard e1
			{

				// struct.remove(it);
				return true;
			}
			else    // result > 0, elem is worse, discard it
			{
				keepelem = false;
			}
		}

		return keepelem;
	}
}
