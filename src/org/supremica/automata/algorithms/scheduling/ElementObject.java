/********************** ElementObject.java **************************/
// This is what populates the open and closed lists
package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.OrderedSetIterator;

import org.supremica.automata.*;

class ElementObject 
	implements Element
{
	private int f;		// the sum of g(n) and h(n)
	private int g;		// the price to get here, g(n)
	private int depth;
	private int[] Tv;	// remaining processing time for each product in its current resource
	private int[] state;// logical state
	private int EB[];	// used for limiting the node expansion
	private Element parent;	// ptr to predecessor element

	private OrderedSetIterator state_it = null;
	private OrderedSetIterator bound_it = null;

	public ElementObject(int[] s, int tvlen)
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

	public int getDepth()
	{
		return depth;
	}
	
	public void setDepth(int d)
	{
		depth = d;
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
	
	public Element getParent()
	{
		return parent;
	}
	
	public void setParent(Element ef)
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
		
		sbuf.append(timeArrayToString());
		return sbuf.toString();
	}
	public String timeArrayToString()
	{
		StringBuffer sbuf = new StringBuffer("Tv = [");
		
		for(int i = 0; i < Tv.length; ++i)
		{
			sbuf.append(Tv[i]);
			sbuf.append(".");
		}
		sbuf.append("]");
		
		return sbuf.toString();
	}
	
	// lexicographic compare
	public int compareState(final Element elem)
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
	
	public int compareRemainingTime(final Element elem)
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
	public int compareCost(final Element elem)
	{
		return this.getCost() - elem.getCost();
	}
	
	public int compareBound(final Element elem)
	{
		return this.getBound() - elem.getBound();
	}
	
	//-- Note -- this *only* compares the logical state
	public int compareTo(final Element elem)
		throws ClassCastException
	{
		return compareState(elem);
	}
	
	public int compareTo(Object obj)
	{
		return compareTo((Element)obj);
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
	