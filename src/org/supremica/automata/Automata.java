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
import org.supremica.gui.Gui;
import javax.swing.JOptionPane;

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
	private String comment = null;
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
	 * Construct an Automata object with a single automaton.
	 **/
	public Automata(Automaton theAutomaton)
	{
		this();
		addAutomaton(theAutomaton);
	}

	/**
	 * Does not make a new copy of the contained automata unless shallowCopy is false
	 */
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

	public void removeAutomata(Automata theAutomata)
	{
		for (Iterator automataIterator = theAutomata.iterator(); automataIterator.hasNext(); )
		{
			Automaton a = (Automaton)automataIterator.next();
			removeAutomaton((Automaton) a);
		}

	}

	public void removeAutomaton(String name)
	{
		Automaton currAutomaton = getAutomaton(name);

		if (currAutomaton != null)
		{
			removeAutomaton(currAutomaton);
		}
	}

	// Moves automaton one step up or down in the ArrayList
	public void moveAutomaton(Automaton aut, boolean directionIsUp)
	{
		int firstAutomatonIndex = theAutomata.indexOf(aut);
		int secondAutomatonIndex;
		if (directionIsUp)
			secondAutomatonIndex = firstAutomatonIndex - 1;
		else
			secondAutomatonIndex = firstAutomatonIndex + 1;

		Automaton firstAutomaton = aut;
		Automaton secondAutomaton = (Automaton) theAutomata.get(secondAutomatonIndex);
		theAutomata.set(firstAutomatonIndex, secondAutomaton);
		theAutomata.set(secondAutomatonIndex, firstAutomaton);
		notifyListeners();
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

	public Iterator iterator()
	{
		return theAutomata.iterator();
	}

	/**
	 * Iterates backwars through the automata... necessary
	 * in the automataMove_actionPerformed in ActionMan when
	 * moving down
	 *
	 *@see ActionMan
	 */
	public Iterator backwardsIterator()
	{
		ArrayList backwardList = new ArrayList();
		Iterator forwardIterator = iterator();
		while (forwardIterator.hasNext())
		{
			backwardList.add(0, forwardIterator.next());
		}
		return backwardList.iterator();
	}

	public Iterator plantIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Plant);
	}

	/**
	 * Returns a new automata object with all plant
	 * in this automata. Note that this reuses the references
	 * to the plant automata.
	 */
	public Automata getPlantAutomata()
	{
		Automata newAutomata = new Automata();
		for (Iterator theIt = plantIterator(); theIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)theIt.next();
			newAutomata.addAutomaton(currAutomaton);
		}
		return newAutomata;
	}

	public Iterator specificationIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Specification);
	}

	/**
	 * Returns a new automata object with all specifications
	 * in this automata. Note that this reuses the references
	 * to the specification automata.
	 */
	public Automata getSpecificationAutomata()
	{
		Automata newAutomata = new Automata();
		for (Iterator theIt = specificationIterator(); theIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)theIt.next();
			newAutomata.addAutomaton(currAutomaton);
		}
		return newAutomata;
	}

	public Iterator supervisorIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Supervisor);
	}

	/**
	 * Returns a new automata object with all specifications
	 * in this automata. Note that this reuses the references
	 * to the specification automata.
	 */
	public Automata getSupervisorAutomata()
	{
		Automata newAutomata = new Automata();
		for (Iterator theIt = supervisorIterator(); theIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)theIt.next();
			newAutomata.addAutomaton(currAutomaton);
		}
		return newAutomata;
	}

	/**
	 * Returns a new automata object with all specification and supervisor automata
	 * in this automata. Note that this reuses the references
	 * to the plant automata.
	 */
	public Automata getSpecificationSupervisorAutomata()
	{
		Automata newAutomata = new Automata();
		for (Iterator theIt = specificationIterator(); theIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)theIt.next();
			newAutomata.addAutomaton(currAutomaton);
		}
		for (Iterator theIt = supervisorIterator(); theIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)theIt.next();
			newAutomata.addAutomaton(currAutomaton);
		}
		return newAutomata;
	}

	public Iterator interfaceIterator()
	{
		return new AutomatonTypeIterator(AutomatonType.Interface);
	}

	/**
	 * Returns true if all automata are deterministic
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
	 * Of course, this is no guarantee that the composition will have 
	 * an accepting state.
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
	 * Returns name of first automaton found that has no accepting states. Returns null
	 * if all automata have at least one accepting state.
	 */
	/*
	public String hasAcceptingState()
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(!automaton.hasAcceptingState())
			{
				return automaton.getName();
			}
		}
		return null;
	}
	*/

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
	 * True if all automata are plants
	 */
	public boolean isAllAutomataPlants()
	{
		for (Iterator autIt = iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (currAutomaton.getType() != AutomatonType.Plant)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * True if all automata are supervisors
	 */
	public boolean isAllAutomataSupervisors()
	{
		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (currAutomaton.getType() != AutomatonType.Supervisor)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * True if all automata are specifications
	 */
	public boolean isAllAutomataSpecifications()
	{
		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (currAutomaton.getType() != AutomatonType.Specification)
			{
				return false;
			}
		}

		return true;
	}



	/**
	 * True if NONE of the automata are plants.
	 */
	public boolean isNoAutomataPlants()
	{
		for (Iterator autIt = iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (currAutomaton.getType() == AutomatonType.Plant)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * True if no automaton is  either specification OR supervisor.
	 * This is good to for early termination in algorithms :)
	 */
	public boolean isNoAutomataSpecificationsOrSupervisors()
	{
		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (currAutomaton.getType() == AutomatonType.Specification
			 	|| currAutomaton.getType() == AutomatonType.Supervisor)
			{
				return false;
			}
		}

		return true;
	}



	/**
	 * Returns true if at least one automaton has the event as prioritized.
	 * Returns false if the event is not included in any alphabet or
	 * all automata has this event as as non-prioritized.
	 */
	public boolean isPrioritizedInAtleastOneAutomaton(LabeledEvent theEvent)
	{
		for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
		{
			Automaton automaton = (Automaton) automataIterator.next();

			if(automaton.isEventPrioritized(theEvent.getLabel()))
			{
				return true;
			}
		}
		return false;
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
			// Why "this, false, false"?!? Shouldn't it be "this, true, true"?  /hugo
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

	/**
	 * Returns the union alphabet of all represented automata.
	 */
	public Alphabet getUnionAlphabet()
	{
		try
		{
			return AlphabetHelpers.getUnionAlphabet(this);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
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

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		if (name == null)
		{
			return "";
		}
		return name;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getComment()
	{
		if (comment == null)
		{
			return "";
		}
		return comment;
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
	{   // Ad-hoc checksum algorithm
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

	// Useful for debugging (among other things)
	public String toDebugString()
	{
		StringBuffer sbuf = new StringBuffer();

		for(Iterator it = iterator(); it.hasNext(); )
		{
			Automaton automaton = (Automaton)it.next();
			sbuf.append(automaton.toString());
			sbuf.append("\n");
		}
		return sbuf.toString();
	}

	public String toString()
	{
		StringBuffer sbuf = new StringBuffer();

		if (size() > 0)
		{
			for(Iterator it = iterator(); it.hasNext(); )
			{
				Automaton automaton = (Automaton)it.next();
				sbuf.append(automaton.toString() + ", ");
			}
			sbuf.delete(sbuf.length()-2, sbuf.length());
		}

		return sbuf.toString();
	}

	// Useful for debugging (among other things) - writes Java code
	public String toCode()
	{
		StringBuffer sbuf = new StringBuffer();
		for(Iterator it = iterator(); it.hasNext(); )
		{
			Automaton automaton = (Automaton)it.next();
			sbuf.append(automaton.toCode());
			sbuf.append("\n");
		}
		sbuf.append("Automata automata = new Automata();\n");
		for(Iterator it = iterator(); it.hasNext(); )
		{
			Automaton automaton = (Automaton)it.next();
			sbuf.append("automata.addAutomaton(" + automaton.getName() + ");");
			sbuf.append("\n");
		}

		return sbuf.toString();
	}

	public String stateToString(int[] arrstate)
	{
		StringBuffer sbuf = new StringBuffer();
		int i = 0;
		for(Iterator it = iterator(); it.hasNext(); )
		{
			Automaton automaton = (Automaton)it.next();
			State state = automaton.getStateWithIndex(arrstate[i]);
			sbuf.append(state.getName() + ".");
			++i;
		}

		return sbuf.toString();
	}

	/**
	 * Examines automata size and, optionally, if all automata
	 * has initial states and/or a defined type.
	 *
	 * @param gui If gui != null, a JOptionPane shows the results and guides the user.
	 * @param minSize Minimum size of the automata.
	 * @param mustHaveInitial Test requires automata to have initial states.
	 * @param mustHaveType Test requires that the automata are not of undefined type.
	 *
	 * This method was originally in gui.ActionMan (to handle the gui-stuff conveniently).
	 */
	public boolean sanityCheck(Gui gui, int minSize, boolean mustHaveInitial, 
											   boolean mustHaveType)
	{
		if (mustHaveInitial)
		{
			// All automata must have initial states.
			// There is another method for this, Automata.hasInitialState(),
			// but it doesn't tell which automaton breaks the test...
			Iterator autIt = iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();

				// Does this automaton have an initial state?
				if (!currAutomaton.hasInitialState())
				{
					if (gui != null)
					{
						String message = "The automaton \"" + currAutomaton.getName() + 
							"\" does not have an initial state.\n" +
							"Skip this automaton or Cancel the whole operation?";
						Object[] options = { "Skip", "Cancel" };
						int cont = JOptionPane.showOptionDialog(gui.getComponent(), message, "Alert", 
																JOptionPane.OK_CANCEL_OPTION, 
																JOptionPane.WARNING_MESSAGE, null, 
																options, options[1]);
						
						if(cont == JOptionPane.OK_OPTION)
						{   // Skip
							// Unselect the automaton
							gui.unselectAutomaton(getAutomatonIndex(currAutomaton));
							// Skip this automaton (remove it from this)
							autIt.remove();
						}
						else // JOptionPane.CANCEL_OPTION
						{   // Cancel
							// This is iNsanE!
							return false;
						}
					}
					else
					{
						// This is iNsaNe!
						return false;
					}
				}
			}
		}

		if (mustHaveType)
		{
			// All automata must have a defined type, i.e. must not be of type "Undefined".
			Iterator autIt = iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();

				// Is this Automaton's type AutomatonType.Undefined?
				if(currAutomaton.getType() == AutomatonType.Undefined)
				{
					if (gui != null)
					{
						String message = "The automaton \"" + currAutomaton.getName() +
							"\" is of type \"Undefined\".\n" +
							"Skip this automaton or Cancel the whole operation?";
						Object[] options = { "Skip", "Cancel" };
						int cont = JOptionPane.showOptionDialog(gui.getComponent(), message, "Alert", 
																JOptionPane.OK_CANCEL_OPTION, 
																JOptionPane.WARNING_MESSAGE, null, 
																options, options[1]);
						
						if(cont == JOptionPane.OK_OPTION)
						{   // Skip
							// Unselect the automaton
							gui.unselectAutomaton(getAutomatonIndex(currAutomaton));
							// Skip this automaton (remove it from this)
							autIt.remove();
						}
						else // JOptionPane.CANCEL_OPTION
						{   // Cancel
							// This is iNsaNe!
							return false;
						}
					}
					else
					{
						// This is iNsaNe!
						return false;
					}
				}
			}
		}

		// Make sure the automata has the right size!
		if (minSize > 0 && size() < minSize)
		{
			if (gui != null)
			{
				String size;
				if (minSize == 1)
					size = "one automaton";
				else if (minSize == 2)
					size = "two automata";
				else
					size = minSize + " automata";
				JOptionPane.showMessageDialog(gui.getFrame(), "At least " +
											  size + " must be selected!",
											  "Alert", JOptionPane.ERROR_MESSAGE);
			}
			// This is inSaNe!
			return false;
		}

		// Sane!
		return true;
	}
}
