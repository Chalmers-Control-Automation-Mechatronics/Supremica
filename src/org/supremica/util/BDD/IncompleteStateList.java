package org.supremica.util.BDD;

import java.util.*;

public class IncompleteStateList
{
	private int count;
	private String[] automata_names;
	private Vector list;

	public IncompleteStateList(BDDAutomaton[] as, int size)
	{
		count = size;
		automata_names = new String[count];

		for (int i = 0; i < count; i++)
		{
			automata_names[i] = as[i].getName();
		}

		list = new Vector();
	}

	public void insert(String[] list_)
	{

		// make a copy
		String[] new_list = new String[count];

		for (int i = 0; i < count; i++)
		{
			new_list[i] = list_[i];
		}

		list.add(new_list);
	}

	public int getWidth()
	{
		return count;
	}

	public String[] getAutomatonNames()
	{
		return automata_names;
	}

	public Vector getList()
	{
		return list;
	}

	public boolean empty()
	{
		return list.isEmpty();
	}
}
