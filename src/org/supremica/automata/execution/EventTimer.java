
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

public class EventTimer
{
	private String startEvent = null;
	private String timeoutEvent = null;
	private String name = null;
	private int delay = -1;
	private int synchIndex = -1;

	public EventTimer(String name, String startEvent, String timeoutEvent, int delay)
		throws IllegalArgumentException
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Name must be non null");
		}

		if (startEvent == null)
		{
			throw new IllegalArgumentException("startEvent must be non null");
		}

		if (timeoutEvent == null)
		{
			throw new IllegalArgumentException("timeoutEvent must be non null");
		}

		if (delay < 0)
		{
			throw new IllegalArgumentException("Delay must be non negative");
		}

		this.name = name;
		this.startEvent = startEvent;
		this.timeoutEvent = timeoutEvent;
		this.delay = delay;
	}

	public EventTimer(EventTimer otherEventTimer)
	{
		this.startEvent = otherEventTimer.startEvent;
		this.timeoutEvent = otherEventTimer.timeoutEvent;
		this.name = otherEventTimer.name;
		this.delay = otherEventTimer.delay;
	}

	public String getName()
	{
		if (name == null)
		{
			return "";
		}

		return name;
	}

	public void setName(String name)
		throws IllegalArgumentException
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Name must be non null");
		}

		this.name = name;
	}

	public String getStartEvent()
	{
		if (startEvent == null)
		{
			return "";
		}

		return startEvent;
	}

	public void setStartEvent(String startEvent)
		throws IllegalArgumentException
	{
		if (startEvent == null)
		{
			throw new IllegalArgumentException("startEvent must be non null");
		}

		this.startEvent = startEvent;
	}

	public String getTimeoutEvent()
	{
		if (timeoutEvent == null)
		{
			return "";
		}

		return timeoutEvent;
	}

	public void setTimeoutEvent(String timeoutEvent)
		throws IllegalArgumentException
	{
		if (timeoutEvent == null)
		{
			throw new IllegalArgumentException("timeoutEvent must be non null");
		}

		this.timeoutEvent = timeoutEvent;
	}

	public int getDelay()
	{
		return delay;
	}

	public void setDelay(int delay)
		throws IllegalArgumentException
	{
		if (delay < 0)
		{
			throw new IllegalArgumentException("Delay must be non negative");
		}

		this.delay = delay;
	}

	public int getSynchIndex()
	{
		return synchIndex;
	}

	void setSynchIndex(int synchIndex)
	{
		this.synchIndex = synchIndex;
	}
}
