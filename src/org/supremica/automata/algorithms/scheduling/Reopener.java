/************************ Reopener.java ******************/
// Note: "if n' required ptr adjustment and was found on closed, reopen it" (p.49)
// When an element should be reopened, what do we do?


package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.Range;
import com.objectspace.jgl.OrderedSetIterator;

class Reopener
{
	static void reopen(Element elem, Structure open, Structure closed)
	{
		// We *know* elem is on closed
		final Range range = closed.equalRange(elem);
		
		for(OrderedSetIterator it = (OrderedSetIterator)range.begin; it.equals(range.end); it.advance())
		{
			Element e = (Element)it.get();
			// remove this one from closed, add it to open
			closed.remove(it);
			open.addElement(e);
		}
	}
}