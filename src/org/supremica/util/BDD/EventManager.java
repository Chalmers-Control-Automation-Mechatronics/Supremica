package org.supremica.util.BDD;

import java.util.*;

public class EventManager
{
	private Vector eventv;
	private Event[] events;
	private int size;
	private boolean closed;

	public EventManager()
	{
		eventv = new Vector();
		size = 0;
		closed = false;
	}

	private Event addEvent(Event e)
	{
		e.id = size++;

		eventv.addElement(e);

		return e;
	}

	private Event getEvent(Event e1)
	{
		for (Enumeration e = eventv.elements(); e.hasMoreElements(); )
		{
			Event e2 = (Event) e.nextElement();

			if (e1.label.equals(e2.label))
			{
				return e2;
			}
		}

		return null;
	}

	public int registerEvent(Event e)
	{
		BDDAssert.bddAssert(!closed, "[EventManager.registerEvent] BAD function call");

		Event old = getEvent(e);    // already registred ?

		if (old == null)
		{    // nop, create a new one
			old = addEvent(e);
		}
		else
		{

			// already seen it, check parameter consistency:
			BDDAssert.bddAssert(e.c == old.c, "Controllability inconsistancy: " + e.label);
		}

		e.id = old.id;    // copy the id (possibly just created)

		return old.id;
	}

	// -----------------------------------------------------
	public void close()
	{
		BDDAssert.bddAssert(!closed, "[EventManager.registerEvent] BAD function call");

		events = new Event[size];

		for (int i = 0; i < size; i++)
		{
			events[i] = null;
		}

		for (Enumeration e = eventv.elements(); e.hasMoreElements(); )
		{
			Event ev = (Event) e.nextElement();

			if (events[ev.id] != null)
			{
				BDDAssert.fatal("Event collision: " + events[ev.id].label + " and " + ev.label + " --> " + ev.id);
			}

			events[ev.id] = ev;
		}

		eventv.removeAllElements();

		closed = true;
	}

	public int getSize()
	{
		return size;
	}

	public Event[] getEventVector()
	{
		BDDAssert.bddAssert(closed, "[EventManager.getEventVector] BAD function call");

		return events;
	}

	public Event getEvent(int index)
	{
		BDDAssert.bddAssert(closed, "[EventManager.getEvent] BAD function call");
		BDDAssert.bddAssert((index >= 0) && (index < size), "BAD state-index");

		return events[index];
	}

	public Event[] copyEvents()
	{
		BDDAssert.bddAssert(closed, "[EventManager.copyEvents] BAD function call");

		Event[] ret = new Event[size];

		for (int i = 0; i < size; i++)
		{
			ret[i] = events[i];
		}

		return ret;
	}
}
