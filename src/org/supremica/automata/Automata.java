
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
import org.supremica.log.*;
import org.supremica.automata.algorithms.*;

/**
 * An ordered set of Automaton-objects.
 * @see Automaton
 */
public class Automata
	implements AutomatonListener
{
	private static Logger logger = LoggerFactory.createLogger(Automata.class);
	private ArrayList theAutomata;
	private HashMap nameMap;
	private String name = null;
	private AutomataListeners listeners = null;
	private String owner = null;
	private String hash = null;

	public Automata()
	{
		theAutomata = new ArrayList();
		nameMap = new HashMap();
	}

	/**
	 * Copy constructor that also makes a copy of all the automata
	 * contained in oldAutomata. Calling this is equal to calling
	 * Automata(oldAutomata, false)
	 **/
	public Automata(Automata oldAutomata)
	{
		this(oldAutomata, false);
	}

	/**
	 * Does not make a new copy of the contained automata unless shallowCopy is false
	 **/
	public Automata(Automata oldAutomata, boolean shallowCopy)
	{
		this();
		if (shallowCopy)
		{
			shallowAutomataCopy(oldAutomata);
		}
		else
		{
			deepAutomataCopy(oldAutomata);
		}
	}

	private void deepAutomataCopy(Automata oldAutomata)
	{
		for (Iterator automataIterator = oldAutomata.iterator(); automataIterator.hasNext(); )
		{
			addAutomaton(new Automaton((Automaton) automataIterator.next()));
		}
	}

	private void shallowAutomataCopy(Automata oldAutomata)
	{
		for (Iterator automataIterator = oldAutomata.iterator(); automataIterator.hasNext(); )
		{
			addAutomaton((Automaton) automataIterator.next());
		}
	}

	public void addAutomaton(Automaton aut)
	{
		theAutomata.add(aut);
		nameMap.put(aut.getName(), aut);
		aut.addListener(this);
		notifyListeners(AutomataListeners.MODE_AUTOMATON_ADDED, aut);

		// logger.debug("Automata.addAutomaton: " + aut.getName());
	}

	public void addAutomata(Automata automata)
	{
		for (Iterator autIt = automata.iterator(); autIt.hasNext(); )
		{
			addAutomaton((Automaton) autIt.next());
		}
	}

	public void removeAutomaton(Automaton aut)
	{
		theAutomata.remove(aut);
		nameMap.remove(aut.getName());
		notifyListeners(AutomataListeners.MODE_AUTOMATON_REMOVED, aut);
	}

	public void removeAutomaton(String name)
	{
		Automaton currAutomaton = getAutomaton(name);

		if (currAutomaton != null)
		{
			removeAutomaton(currAutomaton);
		}
	}

	public void renameAutomaton(Automaton aut, String newName)
	{
		aut.setName(newName);
	}


	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setHash(String hash)
	{
		this.hash = hash;
	}

	public String getHash()
	{
		return hash;
	}

	public String computeHash()
	{
		long checksum = checksum();
		long ownerhash = owner.hashCode();
		long extrahash = 0x314C;
		long totalhash = checksum * ownerhash * extrahash;
		String newHash = Long.toHexString(totalhash);

		return newHash;
	}

	/**
	 * Set a synchronization index in all states and events.
	 */
	public void setSynchronizationIndicies()
		throws Exception
	{
		AutomataSynchronizerHelper syncHelper = new AutomataSynchronizerHelper(this, new SynchronizationOptions());
	}

	/**
	 * If you want the synchronization indicies to be valid then you have to call setSynchronizationIndicies
	 * before calling this method.
	 */
	/*
	private Alphabet getUnionAlphabet()
		throws Exception
	{
		return getUnionAlphabet(true, true);
	}

	private Alphabet getUnionAlphabet(boolean requireConsistentControllability, boolean requireConsistentImmediate)
		throws Exception
	{
		return AlphabetHelpers.getUnionAlphabet(this, requireConsistentControllability, requireConsistentImmediate);
	}
	*/

	public Iterator iterator()
	{
		return theAutomata.iterator();
	}


	public Iterator plantIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Plant);
	}

	public Iterator specificationIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Specification);
	}

	public Iterator supervisorIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Supervisor);
	}

	public Iterator interfaceIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Interface);
	}

	/**
	 * Returns true if all automata have initial states
	 */
	public boolean isDeterministic()
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(!automaton.isDeterministic())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if all automata have initial states
	 */
	public boolean hasInitialState()
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(!automaton.hasInitialState())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if each automaton has at least one accepting state
	 * Note, this is no guarantee that the composition will have an accepting state.
	 */
	public boolean hasAcceptingState()
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(!automaton.hasAcceptingState())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if all automata have all events prioritized
	 */
	public boolean isAllEventsPrioritized()
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(!automaton.isAllEventsPrioritized())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the controllability is consistent through all the automata.
	 */
	public boolean isEventControllabilityConsistent()
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(!automaton.isAllEventsPrioritized())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if any automaton has a self loop
	 */
	public boolean hasSelfLoop()
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(automaton.hasSelfLoop())
			{
				return true;
			}
		}
		return false;
	}

	public int size()
	{
		return theAutomata.size();
	}

	public int nbrOfAutomata()
	{
		return size();
	}

	/**
	 * Use nbrOfAutomata instead.
	 */

	public int getNbrOfAutomata()
	{
		return nbrOfAutomata();
	}

	public boolean containsAutomaton(String name)
	{
		return nameMap.containsKey(name);
	}

	public boolean containsAutomaton(Automaton otherAutomaton)
	{
		Automaton thisAutomaton = (Automaton)nameMap.get(otherAutomaton.getName());
		if (thisAutomaton == null)
		{
			return false;
		}
		return thisAutomaton == otherAutomaton;
	}


	/**
	 * Set the synchronization indicies. The returned alphabet is the union alphabet
	 * and contains the synchronization index of all the events in this automata.
	 */
	public Alphabet setIndicies()
	{
		Alphabet theAlphabet;
		try
		{
			theAlphabet = AlphabetHelpers.getUnionAlphabet(this, false, false);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
		theAlphabet.setIndicies();
		int i = 0;
		for (Iterator autIt = iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			currAutomaton.setIndicies(i++, theAlphabet);
		}
		return theAlphabet;

	}

	public Automaton getAutomaton(String name)
	{
		return (Automaton) nameMap.get(name);
	}

	public Automaton getAutomatonAt(int i)
	{
		return (Automaton) theAutomata.get(i);
	}

	public Automaton getFirstAutomaton()
	{
		return getAutomatonAt(0);
	}

	public int getAutomatonIndex(Automaton theAutomaton)
	{
		for (int i = 0; i < theAutomata.size(); i++)
		{
			Automaton currAutomaton = getAutomatonAt(i);
			if (currAutomaton == theAutomaton)
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Compares two automata objects for equality. This test is supposed
	 * to a quick test, therefor we do not check for language equality
	 * in this automaton. Note, that the automata must be in the same order
	 * for this method to return true.
	 */
	public boolean equalAutomata(Automata other)
	{
		if (nbrOfAutomata() != other.nbrOfAutomata())
		{
			return false;
		}
		if (!getName().equals(other.getName()))
		{
			return false;
		}
		for (Iterator thisAutIt = iterator(), otherAutIt = other.iterator(); thisAutIt.hasNext() || otherAutIt.hasNext(); )
		{
			//System.err.println("automata i");
			if (!thisAutIt.hasNext())
			{
				//System.err.println("automata i this");
				return false;
			}
			if (!otherAutIt.hasNext())
			{
				//System.err.println("automata i other");
				return false;
			}
			Automaton thisAutomaton = (Automaton)thisAutIt.next();
			Automaton otherAutomaton = (Automaton)otherAutIt.next();
			if (!thisAutomaton.equalAutomaton(otherAutomaton))
			{
				//System.err.println("unequal automaton");
				return false;
			}
		}

		//System.err.println("equal automaton");
		return true;
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
	{
		this.name = name;
	}

	public void clear()
	{
		ArrayList theAutomataCopy = new ArrayList(theAutomata);
		for (Iterator autIt = theAutomataCopy.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			removeAutomaton(currAutomaton.getName());
		}
		theAutomata.clear();
		nameMap.clear();
	}

	public String getUniqueAutomatonName()
	{
		return getUniqueAutomatonName("Untitled");
	}

	public String getUniqueAutomatonName(String prefix)
	{
		if (prefix == null)
		{
			return getUniqueAutomatonName();
		}

		if (!containsAutomaton(prefix))
		{
			return prefix;
		}

		int index = 1;
		String newName;

		do
		{
			newName = prefix + "(" + index++ + ")";
		}
		while (containsAutomaton(newName));

		return newName;
	}

	public void stateAdded(Automaton aut, State q)
	{ // Do nothing
	}

	public void stateRemoved(Automaton aut, State q)
	{ // Do nothing
	}

	public void arcAdded(Automaton aut, Arc a)
	{ // Do nothing
	}

	public void arcRemoved(Automaton aut, Arc a)
	{ // Do nothing
	}

	public void attributeChanged(Automaton aut)
	{ // Do nothing
	}

	public void automatonRenamed(Automaton aut, String oldName)
	{
		nameMap.remove(oldName);
		nameMap.put(aut.getName(), aut);
		notifyListeners(AutomataListeners.MODE_AUTOMATON_RENAMED, aut);
	}

	public void updated(Object obj)
	{
		notifyListeners();
	}

	public AutomataListeners getListeners()
	{
		if (listeners == null)
		{
			listeners = new AutomataListeners(this);
		}

		return listeners;
	}

	public void addListener(AutomataListener listener)
	{
		AutomataListeners listeners = getListeners();

		listeners.addListener(listener);
	}

	public long checksum()
	{    // Ad-hoc checksum algorithm
		long checksum = 53562951413L;

		for (Iterator aIt = iterator(); aIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) aIt.next();

			checksum = checksum + currAutomaton.checksum();
		}

		return checksum;
	}

	void notifyListeners()
	{
		if (listeners != null)
		{
			listeners.notifyListeners();
		}
	}

	void notifyListeners(int mode, Automaton a)
	{

		// logger.debug("Automata.notifyListeners Start");
		if (listeners != null)
		{

			// logger.debug("Automata.notifyListeners");
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


	class AutomatonTypeIterator
		implements Iterator
	{
		private Iterator autIt;
		private AutomatonType theType;
		private Automaton theAutomaton = null;

		public AutomatonTypeIterator(AutomatonType theType)
		{
			this.autIt = theAutomata.iterator();
			this.theType = theType;
			findNext();
		}

		public boolean hasNext()
		{
			return theAutomaton != null;
		}

		public Object next()
		{
			Automaton returnAutomaton = theAutomaton;
			findNext();
			return returnAutomaton;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		private void findNext()
		{
			while (autIt.hasNext())
			{
				theAutomaton = (Automaton)autIt.next();
				if (theAutomaton.getType() == theType)
				{
					return;
				}
			}
			theAutomaton = null;
		}
	}
}
