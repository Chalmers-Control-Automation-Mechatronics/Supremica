
/*********************** Capsule.java ************************/

// Wraps an iterator to ElemenObjects. 
// Cannot now really remeber why, but there was something to test....
// Or maybe something about removing the correct (that is, pointed to) object.
package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.OrderedSetIterator;

class Capsule
	implements Element
{
	private OrderedSetIterator it;

	public Capsule(OrderedSetIterator it)
	{
		this.it = it;
	}

	public int getBound()
	{
		return ((Element) it.get()).getBound();
	}

	public void setBound(int i)
	{
		((Element) it.get()).setBound(i);
	}

	public int getCost()
	{
		return ((Element) it.get()).getCost();
	}

	public void setCost(int i)
	{
		((Element) it.get()).setCost(i);
	}

	public int getDepth()
	{
		return ((Element) it.get()).getDepth();
	}

	public void setDepth(int i)
	{
		((Element) it.get()).setDepth(i);
	}

	public int[] getStateArray()
	{
		return ((Element) it.get()).getStateArray();
	}

	public int[] getTimeArray()
	{
		return ((Element) it.get()).getTimeArray();
	}

	public void setTime(int index, int time)
	{
		((Element) it.get()).setTime(index, time);
	}

	public String toString()
	{
		return ((Element) it.get()).toString();
	}

	public String timeArrayToString()
	{
		return ((Element) it.get()).timeArrayToString();
	}

	public int compareState(final Element ef)
	{
		return ((Element) it.get()).compareState(ef);
	}

	public int compareRemainingTime(final Element ef)
	{
		return ((Element) it.get()).compareRemainingTime(ef);
	}

	public int compareCost(final Element ef)
	{
		return ((Element) it.get()).compareCost(ef);
	}

	public int compareBound(final Element ef)
	{
		return ((Element) it.get()).compareBound(ef);
	}

	public Element getParent()
	{
		return ((Element) it.get()).getParent();
	}

	public void setParent(Element ef)
	{
		((Element) it.get()).setParent(ef);
	}

	public int compareTo(final Element elem)
	{
		return ((Element) it.get()).compareTo(elem);
	}

	public int compareTo(final Object obj)
	{
		return ((Element) it.get()).compareTo(obj);
	}

	public OrderedSetIterator getStateIterator()
	{
		return ((Element) it.get()).getStateIterator();
	}

	public OrderedSetIterator getBoundIterator()
	{
		return ((Element) it.get()).getBoundIterator();
	}

	public void setStateIterator(OrderedSetIterator itx)
	{
		((Element) it.get()).setStateIterator(itx);
	}

	public void setBoundIterator(OrderedSetIterator itx)
	{
		((Element) it.get()).setBoundIterator(itx);
	}
}
