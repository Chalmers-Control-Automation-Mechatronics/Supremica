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

package org.supremica.automata;

import java.util.*;
import org.supremica.gui.*;
import org.apache.log4j.*;

/**
 * A collection of automata.
 */
public class Automata
{
	private static Category thisCategory = LogDisplay.createCategory(Automata.class.getName());

	private ArrayList theAutomata; // Efficiency reasons
 	private HashMap nameMap;
 	private String name = null;
	private AutomataListeners listeners = null;

	public Automata()
	{
		theAutomata = new ArrayList();
  		nameMap = new HashMap();
	}

	public Automata(Automata oldAutomata)
	{
		this();
		Iterator automataIterator = oldAutomata.iterator();
		while (automataIterator.hasNext())
			// addAutomaton((Automaton) automataIterator.next());
			addAutomaton(new Automaton((Automaton) automataIterator.next()));
	}

	public void addAutomaton(Automaton aut)
	{
		theAutomata.add(aut);
  		nameMap.put(aut.getName(), aut);
		notifyListeners(AutomataListeners.MODE_AUTOMATON_ADDED, aut);
	}

	public void addAutomata(Automata automata)
	{
		Iterator automataIterator = automata.iterator();
		while (automataIterator.hasNext())
			addAutomaton((Automaton) automataIterator.next());
	}

	public void removeAutomaton(Automaton aut)
	{
		theAutomata.remove(aut);
		nameMap.remove(aut.getName());
		notifyListeners(AutomataListeners.MODE_AUTOMATON_REMOVED, aut);
	}

	public void renameAutomaton(Automaton aut, String newName)
	{
		nameMap.remove(aut.getName());
		aut.setName(newName);
		nameMap.put(aut.getName(), aut);
		notifyListeners(AutomataListeners.MODE_AUTOMATON_RENAMED, aut);
	}

	public Alphabet createUnionAlphabet()
		throws Exception
	{
  		// Compute the new alphabet
		EventsSet theAlphabets = new EventsSet();
		Iterator autIt = iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			theAlphabets.add(currAlphabet);
		}
		Alphabet theAlphabet = null;
		try
  		{
			theAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "a");
		}
  		catch (Exception e)
    	{
 			thisCategory.error("Error while generating union alphabet: " + e);
        	throw e;
     	}
     	return theAlphabet;
	}

	public Iterator iterator()
	{
		return theAutomata.iterator();
	}

	public int size()
	{
		return theAutomata.size();
	}

 	public Automaton getAutomatonAt(int i)
	{
		return (Automaton)theAutomata.get(i);
	}

 	public boolean containsAutomaton(String name)
  	{
		return nameMap.containsKey(name);
	}

 	public Automaton getAutomaton(String name)
  	{
		return (Automaton)nameMap.get(name);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public AutomataListeners getListeners()
	{
		if (listeners == null)
		{
			listeners = new AutomataListeners(this);
		}
		return listeners;
	}

	private void notifyListeners()
	{
		if (listeners != null)
		{
			listeners.notifyListeners();
		}
	}
	private void notifyListeners(int mode, Automaton a)
	{
		if (listeners != null)
		{
			listeners.notifyListeners(mode, a);
		}
	}

	public void beginTransaction()
	{
		if (listeners != null)
		{
			listeners.beginTransaction();
		}
	}

	public void endTransaction()
	{
		if (listeners != null)
		{
			listeners.endTransaction();
		}
	}
}
