
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.execution;

import java.util.*;
import org.supremica.automata.LabeledEvent;

public class Timers
{
	private Map labelToTimerMap = new TreeMap();

	public Timers()
	{
	}

	public Timers(Timers otherTimers)
	{
		for (Iterator actIt = otherTimers.iterator(); actIt.hasNext(); )
		{
			EventTimer currTimer = (EventTimer) actIt.next();
			EventTimer newTimer = new EventTimer(currTimer);

			addTimer(newTimer);
		}
	}

	public void addTimers(Timers otherTimers)
	{
		for (Iterator actIt = otherTimers.iterator(); actIt.hasNext(); )
		{
			EventTimer currTimer = (EventTimer) actIt.next();
			EventTimer newTimer = new EventTimer(currTimer);

			addTimer(newTimer);
		}
	}

	public boolean addTimer(EventTimer theTimer)
	{
		if (theTimer == null)
		{
			return false;
		}
		if (labelToTimerMap.containsKey(theTimer.getName()))
		{
			return false;
		}
		labelToTimerMap.put(theTimer.getName(), theTimer);
		return true;
	}

	public void removeTimer(EventTimer theTimer)
	{
		labelToTimerMap.remove(theTimer.getName());
	}

	public boolean hasTimer(String label)
	{
		return labelToTimerMap.containsKey(label);
	}

	public EventTimer getTimer(String label)
	{
		return (EventTimer) labelToTimerMap.get(label);
	}
	
	/**
	 * Returns the timer that has <code>event</code> as
	 * it's timeout event. An event can not 
	 * be a timeout event to more than one timer. 
	 * @param event The timeout event
	 * @return
	 */
	public EventTimer getTimerWithTimeoutEvent(LabeledEvent event)
	{
		EventTimer timer;
		for (Iterator it = iterator(); it.hasNext();)
		{
			timer = (EventTimer) it.next(); 
			if (timer.getTimeoutEvent().equals(event.getLabel()))
				return timer;
		}
		return null;
	}

	public Iterator iterator()
	{
		return labelToTimerMap.values().iterator();
	}

	/**
	 * Returns an iterator to the timers that has
	 * this event as start event. An event can be 
	 * startevent to more than one timer.  
	 * @param theEvent
	 * @return
	 */
	public Iterator iteratorWithStartEvent(LabeledEvent theEvent)
	{
		return new TimerIterator(iterator(), theEvent);
	}

	public int size()
	{
		return labelToTimerMap.size();
	}

	public void clear()
	{
		labelToTimerMap.clear();
	}

	public void setIndices()
	{
		int i = 0;
		for (Iterator theIt = iterator(); theIt.hasNext();)
		{
			EventTimer currTimer = (EventTimer)theIt.next();
			currTimer.setSynchIndex(i++);
		}
	}

	class TimerIterator
		implements Iterator
	{
		private final Iterator theIterator;
		private LabeledEvent theStartEvent;
		private String label;
		private Object nextObject = null;
		private boolean startEvent;

		public TimerIterator(Iterator theIterator, LabeledEvent theStartEvent)
		{
			this.theIterator = theIterator;
			this.theStartEvent = theStartEvent;
			this.label = theStartEvent.getLabel();
			findNextObject();
		}

		public boolean hasNext()
		{
			return nextObject != null;
		}

		public Object next()
			throws NoSuchElementException
		{
			if (nextObject != null)
			{
				Object oldObject = nextObject;
				findNextObject();
				return oldObject;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		public void remove()
			throws UnsupportedOperationException, IllegalStateException
		{
			throw new UnsupportedOperationException();
		}

		private void findNextObject()
		{
			while (theIterator.hasNext())
			{
				EventTimer currTimer = (EventTimer)theIterator.next();
				if (label.equals(currTimer.getStartEvent()))
				{
					nextObject = currTimer;
					return;
				}
			}
			nextObject = null;
		}
	}

}
