package org.supremica.util.BDD;

import java.util.*;
import java.io.*;

public class EventSet
	extends Vector
{
	private int count = 0, total = 0;
	private boolean closed = false;

	// ------------------------------------------------ stuffs used BEFORE closing!
	private boolean in(String name)
	{
		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			Event ev = (Event) e.nextElement();

			if (ev.label.equals(name))
			{
				return true;
			}
		}

		return false;
	}

	public Event getEventByName(String name)
	{
		BDDAssert.bddAssert(!closed, "[EventSet.getEventByName]BAD FUNCTION CALL!");

		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			Event ev = (Event) e.nextElement();

			if (ev.name_id.equals(name))
			{
				return ev;
			}
		}

		return null;
	}

	public int getIdByName(String name)
	{
		BDDAssert.bddAssert(!closed, "[EventSet.getIdByName]BAD FUNCTION CALL!");

		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			Event ev = (Event) e.nextElement();

			if (ev.name_id.equals(name))
			{
				return ev.id;
			}
		}

		return Automaton.FAILED;
	}

	public void add(EventManager alphabet, String label, String id, boolean c, boolean p)
	{
		BDDAssert.bddAssert(!closed, "[EventSet.add] BAD FUNCTION CALL!");
		BDDAssert.bddAssert(!in(label), "Duplicate event: " + label);

		Event event = new Event();

		event.label = label;
		event.name_id = id;
		event.c = c;
		event.p = p;
		event.code = count++;
		event.id = alphabet.registerEvent(event);

		addElement(event);
	}

	// -------------------------------------------------------
	private Event[] events;

	public int getSize()
	{
		BDDAssert.bddAssert(closed, "[EventSet.getSize] BAD FUNCTION CALL!");

		return total;
	}

	public Event[] getEventVector()
	{
		BDDAssert.bddAssert(closed, "[EventSet.getEventVector] BAD FUNCTION CALL!");

		return events;
	}

	public Event getEvent(int index)
	{
		BDDAssert.bddAssert(closed, "[EventSet.getEvent] BAD FUNCTION CALL!");
		BDDAssert.bddAssert((index >= 0) && (index < total), "BAD event-index");

		return events[index];
	}

	void close(EventManager alphabet)
	{
		BDDAssert.bddAssert(!closed, "[EventSet.close] BAD FUNCTION CALL!");

		total = alphabet.getSize();
		events = new Event[total];

		for (int t = 0; t < total; t++)
		{
			events[t] = null;
		}

		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			Event ev = (Event) e.nextElement();

			events[ev.id] = ev;
		}

		removeAllElements();

		closed = true;
	}

	public void dump(PrintStream ps)
	{
		int uc, p;

		uc = p = 0;

		boolean first = true;

		ps.print("Events = { ");

		for (int i = 0; i < total; i++)
		{
			if (events[i] != null)
			{
				if (!first)
				{
					ps.print(", ");
				}
				else
				{
					first = false;
				}

				ps.print(events[i].label);

				// DEBUG
				if(!events[i].c) ps.print(":u");
				if(!events[i].p) ps.print(":p");

				if (!events[i].c)
				{
					uc++;
				}

				if (events[i].p)
				{
					p++;
				}
			}
		}

		ps.println(" };");

		// print uncontrollable events:
		if (uc > 0)
		{
			ps.print("Events_u = { ");

			first = true;

			for (int i = 0; i < total; i++)
			{
				if (events[i] != null)
				{
					if (!events[i].c)
					{
						if (!first)
						{
							ps.print(", ");
						}
						else
						{
							first = false;
						}

						ps.print(events[i].label);
					}
				}
			}

			ps.println(" };");
		}

		// print prioritized events:
		if (p == count)
		{
			ps.println("Events_p = Events;");
		}
		else if (p > 0)
		{
			ps.print("Events_p = { ");

			first = true;

			for (int i = 0; i < total; i++)
			{
				if (events[i] != null)
				{
					if (events[i].p)
					{
						if (!first)
						{
							ps.print(", ");
						}
						else
						{
							first = false;
						}

						ps.print(events[i].label);
					}
				}
			}

			ps.println(" };");
		}

		ps.println();
	}
}
