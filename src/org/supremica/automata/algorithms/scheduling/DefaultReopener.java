
/********************* DefaultReopener.java ******************/

// By default, reopen *all* elements that look like n'/elem
package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.Range;
import com.objectspace.jgl.OrderedSetIterator;

class DefaultReopener
{
	static void reopen(Element elem, Structure open, Structure closed)
	{

		// We *know* elem is on closed
		final Range range = closed.equalRange(elem);

		for (OrderedSetIterator it = (OrderedSetIterator) range.begin;
				it.equals(range.end); it.advance())
		{
			Element e = (Element) it.get();

			// remove this one from closed, add it to open
			closed.remove(it);
			open.addElement(e);
		}
	}
}
