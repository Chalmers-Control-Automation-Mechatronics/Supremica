/********************** KnutsManipulator.java **********************/
package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.OrderedSetIterator;

class KnutsManipulator
	implements Manipulator
{
	public boolean manipulate(Element elem, OrderedSetIterator begin, OrderedSetIterator beyond, Structure struct)
	{
		boolean keepelem = true;
		for(OrderedSetIterator it = new OrderedSetIterator(begin); !it.equals(beyond); it.advance())
		{
			Element ef = (Element)it.get();
			if(elem.getBound() > ef.getBound())
			{
				return false;
			}
		}
		return true;
	}
} 
	