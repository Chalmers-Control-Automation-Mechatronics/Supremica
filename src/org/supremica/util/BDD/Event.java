package org.supremica.util.BDD;

public class Event
{
	public Event()
	{
		owners = use = 0;
	}

	// package accessable
	String name_id, label;
	int id, code, use, owners;
	int bdd;
	boolean c, p;
	Automaton automaton;

	// for the rest of us:
	public String getName()
	{
		return name_id;
	}

	public boolean isControllable()
	{
		return c;
	}

	public boolean isPrioritized()
	{
		return p;
	}

	public int getBDD()
	{
		return bdd;
	}
}
