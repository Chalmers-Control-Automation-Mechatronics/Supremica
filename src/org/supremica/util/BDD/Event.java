package org.supremica.util.BDD;

public class Event
{
	public Event()
	{
		owners = use = 0;
	}

	String name_id, label;
	int id, code, use, owners;
	int bdd;
	boolean c, p;
    Automaton automaton;
}
