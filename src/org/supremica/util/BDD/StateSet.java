package org.supremica.util.BDD;

import java.util.*;
import java.io.*;

public class StateSet
	extends Vector
{
	int count = 0;
	boolean closed = false;

	// ------------------------------------------------ stuffs used BEFORE closing!
	private boolean in(String name)
	{
		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			State s = (State) e.nextElement();

			if (s.name.equals(name))
			{
				Options.out.println("Found state " + s.name + "/" + s.name_id + " " + s.id + "/" + s.code);

				return true;
			}
		}

		return false;
	}


	public State getByName(String name)
	    throws BDDException
	{
		BDDAssert.bddAssert(!closed, "[StateSet.getIdByName]BAD FUNCTION CALL!");

		State by_id = null;

		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			State s = (State) e.nextElement();

			if (s.name_id.equals(name))
			{
				return s;
			}

			if(s.name.equals(name))
			{
				by_id = s;
			}
		}



		if(by_id != null)
		{
			System.err.println("[StateSet.getByName] BAD MODEL, uses name " + by_id.name + " instead of " + by_id.name_id);
			return by_id;
		}


		// DEBUG:
		System.out.println("Failed to find " + name + " dumping complete state set for this automaton:");
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			State s = (State) e.nextElement();
			System.out.println("State " + s.name + ", id = " + s.name_id + ", id = " + s.id + ", code = " + s.code);
		}

		return null;
	}


	public int getIdByName(String name)
	    throws BDDException
	{
		State s = getByName(name);
		return ( s  != null) ? s.id : Automaton.FAILED;
	}


	public void add(String name, String id, boolean i, boolean m, boolean x)
	    throws BDDException
	{
		BDDAssert.bddAssert(!closed, "[StateSet.add] BAD FUNCTION CALL!");

		// BDDAssert.bddAssert(!in(name), "Duplicate state: " + name);
		if (in(name))
		{
			Options.out.println("Duplicate state: " + name + "/" + id + " " + (count + 1));

			int c = 0;

			for (Enumeration e = elements(); e.hasMoreElements(); )
			{
				State s = (State) e.nextElement();

				Options.out.println("state " + (c++) + " " + s.name + "/" + s.name_id + " " + s.id + "/" + s.code);
			}

			System.exit(20);
		}

		State state = new State();

		state.name = name;
		state.name_id = id;
		state.i = i;
		state.m = m;
		state.f = x;
		state.id = count++;
		state.code = state.id;

		addElement(state);

		// if(count == 1) Options.out.println();
		// Options.out.print("\r " + count + "   ");
	}

	// -------------------------------------------- AFTER CLOSING
	private State[] states;

	public int getSize()
	{
		return count;
	}

	public State[] getStateVector()
	{
		BDDAssert.internalCheck(closed, "[StateSet.getStateVector] BAD FUNCTION CALL!");

		return states;
	}

	public State getState(int index)
	{
		BDDAssert.internalCheck((index >= 0) && (index < count), "BAD state-index");

		return states[index];
	}

	void close()
	    throws BDDException
	{
		BDDAssert.bddAssert(!closed, "[StateSet.close] BAD FUNCTION CALL!");

		states = new State[count];

		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			State ss = (State) e.nextElement();

			states[ss.id] = ss;
		}

		removeAllElements();

		closed = true;
	}

	public void dump(PrintStream ps)
	{
		int m, i_, f;

		m = i_ = f = 0;

		ps.print("States = { ");

		for (int i = 0; i < count; i++)
		{
			if (i != 0)
			{
				ps.print(", ");
			}

			ps.print(states[i].name);

			if (states[i].i)
			{
				i_++;
			}

			if (states[i].m)
			{
				m++;
			}

			if (states[i].f)
			{
				f++;
			}
		}

		ps.println(" };");

		// print the initial state(s)
		if (i_ > 0)
		{
			ps.print("States_i = { ");

			boolean first = true;

			for (int i = 0; i < count; i++)
			{
				if (states[i].i)
				{
					if (!first)
					{
						ps.print(", ");
					}
					else
					{
						first = false;
					}

					ps.print(states[i].name);
				}
			}

			ps.println(" };");
		}

		// print marked states:
		if (m > 1)
		{
			ps.print("States_m = { ");

			boolean first = true;

			for (int i = 0; i < count; i++)
			{
				if (states[i].m)
				{
					if (!first)
					{
						ps.print(", ");
					}
					else
					{
						first = false;
					}

					ps.print(states[i].name);
				}
			}

			ps.println(" };");
		}

		// print forbidden states:
		if (f > 1)
		{
			ps.print("States_x = { ");

			boolean first = true;

			for (int i = 0; i < count; i++)
			{
				if (states[i].f)
				{
					if (!first)
					{
						ps.print(", ");
					}
					else
					{
						first = false;
					}

					ps.print(states[i].name);
				}
			}

			ps.println(" };");
		}

		ps.println();
	}
}
