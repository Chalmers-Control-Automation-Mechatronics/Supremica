
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.supremica.automata.LabeledEvent;

public class Timers
{
	private final Map<String, EventTimer> labelToTimerMap = new TreeMap<String, EventTimer>();

	public Timers() {}

	public Timers(final Timers otherTimers)
	{
		for (final Iterator<EventTimer> actIt = otherTimers.iterator(); actIt.hasNext(); )
		{
			final EventTimer currTimer = actIt.next();
			final EventTimer newTimer = new EventTimer(currTimer);

			addTimer(newTimer);
		}
	}

	public void addTimers(final Timers otherTimers)
	{
		for (final Iterator<EventTimer> actIt = otherTimers.iterator(); actIt.hasNext(); )
		{
			final EventTimer currTimer = actIt.next();
			final EventTimer newTimer = new EventTimer(currTimer);

			addTimer(newTimer);
		}
	}

	public boolean addTimer(final EventTimer theTimer)
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

	public void removeTimer(final EventTimer theTimer)
	{
		labelToTimerMap.remove(theTimer.getName());
	}

	public boolean hasTimer(final String label)
	{
		return labelToTimerMap.containsKey(label);
	}

	public EventTimer getTimer(final String label)
	{
		return labelToTimerMap.get(label);
	}

	/**
	 * Returns the timer that has <code>event</code> as
	 * it's timeout event. An event can not
	 * be a timeout event to more than one timer.
	 * @param event The timeout event
	 */
	public EventTimer getTimerWithTimeoutEvent(final LabeledEvent event)
	{
		EventTimer timer;

		for (final Iterator<EventTimer> it = iterator(); it.hasNext(); )
		{
			timer = it.next();

			if (timer.getTimeoutEvent().equals(event.getLabel()))
			{
				return timer;
			}
		}

		return null;
	}

	public Iterator<EventTimer> iterator()
	{
		return labelToTimerMap.values().iterator();
	}

	/**
	 * Returns an iterator to the timers that has
	 * this event as start event. An event can be
	 * startevent to more than one timer.
	 */
	public Iterator<?> iteratorWithStartEvent(final LabeledEvent theEvent)
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

		for (final Iterator<EventTimer> theIt = iterator(); theIt.hasNext(); )
		{
			final EventTimer currTimer = theIt.next();

			currTimer.setSynchIndex(i++);
		}
	}

	class TimerIterator
		implements Iterator<Object>
	{
		private final Iterator<EventTimer> theIterator;
		@SuppressWarnings("unused")
		private final LabeledEvent theStartEvent;
		private final String label;
		private Object nextObject = null;
		@SuppressWarnings("unused")
		private boolean startEvent;

		public TimerIterator(final Iterator<EventTimer> theIterator, final LabeledEvent theStartEvent)
		{
			this.theIterator = theIterator;
			this.theStartEvent = theStartEvent;
			this.label = theStartEvent.getLabel();

			findNextObject();
		}

		@Override
    public boolean hasNext()
		{
			return nextObject != null;
		}

		@Override
    public Object next()
			throws NoSuchElementException
		{
			if (nextObject != null)
			{
				final Object oldObject = nextObject;

				findNextObject();

				return oldObject;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		@Override
    public void remove()
			throws UnsupportedOperationException, IllegalStateException
		{
			throw new UnsupportedOperationException();
		}

		private void findNextObject()
		{
			while (theIterator.hasNext())
			{
				final EventTimer currTimer = theIterator.next();

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
