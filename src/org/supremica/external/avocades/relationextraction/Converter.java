/*****************************************************
*
* Converts interlocking described as safe states into interlocking
* described as unsafe states.
*
*****************************************************/

package org.supremica.external.avocades.relationextraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;


@SuppressWarnings("unchecked")
public class Converter
{

	Document unsafeILDoc; // Output xml document.
	Element unsafeIL = new Element("PA");

	public Converter() {}


/*****************************************************
*
* convertToUnsafe
*
* Convertes interlocking described as safe states into interlocking
* desribed as unsafe states. Stores the unsafe states into an xml-
* document.
*
******************************************************/

	public Document convertToUnsafe(final Document safeIL)
	{
		unsafeILDoc = new Document(unsafeIL);
		final Element root = safeIL.getRootElement();

		final List<?> eventILList = root.getChildren("Event_interlocking");

		/* Put all components involved in the interlocking into a HashSet.
		   Put all safe states into sets and the sets into an ArrayList. */
		for(final Iterator<?> eventILIter = eventILList.iterator(); eventILIter.hasNext(); )
		{
			final Set<String> involvedComponents = new HashSet<String>();
			final List<Set<List<String>>> safeStates = new ArrayList<Set<List<String>>>();
			//System.out.println("\n--------- Ny eventIL -------");
			final Element eventIL = (Element) eventILIter.next();
			final Element event = eventIL.getChild("Event");
			final Element restr = event.getChild("Restriction");
			final Element or = restr.getChild("Or");

			final List<?> andList = or.getChildren("And");
			for(final Iterator<?> andIter = andList.iterator(); andIter.hasNext(); )
			{
				final Element and = (Element) andIter.next();
				final List<?> stateList = and.getChildren("State");
				final Set<List<String>> theState = new HashSet<List<String>>();

				for(final Iterator<?> stateIter = stateList.iterator(); stateIter.hasNext(); )
				{
					final Element state = (Element) stateIter.next();
					final String stateName = state.getAttributeValue("name");
					if(!involvedComponents.contains(stateName))
					{
						involvedComponents.add(stateName);
					}
					final String stateId = state.getAttributeValue("id");
					final List<String> theName = new ArrayList<String>();
					theName.add(stateName);
					final List<String> theId = new ArrayList<String>();
					theId.add(stateId);
					final List<String> oneState = new ArrayList<String>();
					oneState.add(stateName);
					oneState.add(stateId);
					theState.add(oneState);
				}
				safeStates.add(theState);
			}
			//System.out.println("--------- safeStates -------");
			//printSetList(safeStates);
			//System.out.println("---------------------");
			//System.out.println("--------- allStates -------");
			final ArrayList<Set<?>> allPossibleStates = getPossibleStates(root, involvedComponents);
			//printSetList(allPossibleStates);
			//System.out.println("---------------------");
			/* Convert the event interlocking to an expression in unsafe states,
			and save it in the xml document. */
			final String eventName = eventIL.getAttributeValue("id");
			final List<Set<?>> unsafeStates = safeToUnsafe(safeStates, allPossibleStates);

			buildUnsafeDoc(root, eventName, unsafeStates);

		} // for(eventILIter)
		return unsafeILDoc;
	}

/*****************************************************
*
* getPossibleStates
*
* Creates a ArrayList of all combinations of states of all
* involved components.
*
******************************************************/

  public ArrayList<Set<?>> getPossibleStates(final Element root, final Set<String> involvedComponents)
	{
		final List<?> machineList = root.getChildren("Machine");
		final ArrayList<Set<?>> allPossibleStates = new ArrayList<Set<?>>();
		boolean first = true;

		/* Search in the machine list for the involved components */
		for(final Iterator<?> machIter = machineList.iterator(); machIter.hasNext(); )
		{
			final Element machine = (Element) machIter.next();
			final List<?> componentList = machine.getChildren("Component");

			for(final Iterator<?> compIter = componentList.iterator(); compIter.hasNext(); )
			{
				final Element component = (Element) compIter.next();
				final String compName = component.getAttributeValue("name");

				/* If the component is an incolved component, ... */
				if(involvedComponents.contains(compName) )
				{
					final List<String> theName = new ArrayList<String>();
					theName.add(compName);
					final List<?> stateList = component.getChildren("State");
					final int n = stateList.size(); /* Number of possible states for the component */
					int lengthAP=0; /* Length of allPossibleStates */

					if(!first)
					{
						final ArrayList<Set<?>> clone = (ArrayList<Set<?>>) allPossibleStates.clone();

						/* Prolong allPossibleStates, to make place for the states of the
						next component. */
						for (final Iterator<Set<?>> cloneIter = clone.iterator(); cloneIter.hasNext();)
						{
							final Set<?> copy = (HashSet<?>) cloneIter.next();
							for(int j=lengthAP+1; j<lengthAP+n; j++)
							{
								allPossibleStates.add(j, copy);
							}
							lengthAP=lengthAP+n;
						}
					}
					int place=1;

					/* For all states of the component */
					for(final Iterator<?> stateIter = stateList.iterator(); stateIter.hasNext();)
					{
						final Element state = (Element) stateIter.next();
						final String stateId = state.getAttributeValue("id");
						final List<String> theId = new ArrayList<String>();
						theId.add(stateId);

						if(first)
						{
							final List<String> onePossibleState = new ArrayList<String>();
							onePossibleState.add(compName);
							onePossibleState.add(stateId);
							final Set<List<String>> theState = new HashSet<List<String>>();
							theState.add(onePossibleState);
							allPossibleStates.add(theState);
							//printSetList(allPossibleStates);
						}
						else
						{
							// Append new states to old ones
							for(int k=place-1; k<lengthAP; k=k+n)
							{
								final HashSet<List<String>> theState = (HashSet<List<String>>) allPossibleStates.remove(k);
								final HashSet<List<String>> theStateClone = (HashSet<List<String>>) theState.clone();
								final List<String> onePossibleState = new ArrayList<String>();
								onePossibleState.add(compName);
								onePossibleState.add(stateId);
								theStateClone.add(onePossibleState);
								allPossibleStates.add(k, theStateClone);
							}
							place++;
						}
					}
					first = false;
				}
			}
		}
		return allPossibleStates;
	}

/*****************************************************
*
* safeToUnsafe
*
* Returns a list that is equal to
* allPossibleStates - safeStates
*
******************************************************/

    public List<Set<?>> safeToUnsafe(final List<Set<List<String>>> safeStates,
                                     final ArrayList<Set<?>> allPossibleStates)
	{
		final ArrayList<Set<?>> unsafeStates = (ArrayList<Set<?>>) allPossibleStates.clone();

		for(final Iterator<Set<List<String>>> safeIter = safeStates.iterator(); safeIter.hasNext(); )
		{
			final HashSet<?> safeState = (HashSet<?>) safeIter.next();

			for(final Iterator<Set<?>> unsafeIter = allPossibleStates.iterator(); unsafeIter.hasNext(); )
			{
				final Set<?> unsafeState = (HashSet<?>) unsafeIter.next();
				if(unsafeState.containsAll(safeState))
				{
					unsafeStates.remove(unsafeState);
				}
			}
		}
		return unsafeStates;
	}


/*****************************************************
*
* buildSafeDoc
*
* Adds an xml-description of the unsafe interlocking for "eventName"
* to the xml-document referenced by "root".
*
******************************************************/

	public void buildUnsafeDoc(final Element root,
	                           final String eventName,
	                           final List<Set<?>> unsafeStates)
	{

		final Element event_interlocking = new Element("Event_interlocking");
		event_interlocking.setAttribute("id", eventName);
		unsafeIL.addContent(event_interlocking);

		final Element event = new Element("Event");
		event_interlocking.addContent(event);

		final Element restriction = new Element("Restriction");
		event.addContent(restriction);

		final Element or = new Element("Or");
		restriction.addContent(or);

		for(final Iterator<Set<?>> unsafeIter = unsafeStates.iterator(); unsafeIter.hasNext(); )
		{
			final Set<?> oneUnsafe = (HashSet<?>) unsafeIter.next();

			final Element and = new Element("And");
			or.addContent(and);
			System.out.println("HIT " + oneUnsafe.toString());
			for(final Iterator<?> oneUnsafeIter = oneUnsafe.iterator(); oneUnsafeIter.hasNext(); )
			{
				final ArrayList<?> oneState = (ArrayList<?>) oneUnsafeIter.next();
				final String stateName = (String) oneState.get(0);
				final String stateId = (String) oneState.get(1);
				final Element state = new Element("State");
				state.setAttribute("name", stateName);
				state.setAttribute("id", stateId);
				and.addContent(state);
			}
		}
	}
/*****************************************************
*
* getStates
*
* Given the interlocking, find the corresponding states.
* Returns an ArrayList containing all states.
*
******************************************************/

	public ArrayList<ArrayList<String>> getStates(final Element interlocking) {

		final ArrayList<ArrayList<String>> allStates = new ArrayList<ArrayList<String>>();

		final Element event = interlocking.getChild("Event");
		final Element restriction = event.getChild("Restriction");
		final Element or = restriction.getChild("Or");

		/* Get all ands. Corresponds to the rows in the Volvo case. */
		final List<?> andList = or.getChildren("And");
		for(final Iterator<?> andIter = andList.iterator(); andIter.hasNext(); )
		{
			final Element and = (Element) andIter.next();
			/* Get the states for the first and, e.g. A and B */
			final List<?> stateList = and.getChildren("State");
			final ArrayList<String> oneState = new ArrayList<String>();
			/* For each state (component might be a better word), get its name and id.
			Id corresponds to the unsafe state of the state */
			for(final Iterator<?> stateIter = stateList.iterator(); stateIter.hasNext(); )
			{
				final Element state = (Element) stateIter.next();

				final String stateName = state.getAttributeValue("name");
				final String stateId = state.getAttributeValue("id");
				final String stateExpression = stateName + stateId;

				oneState.add(stateExpression);
			}
			allStates.add(oneState);
		}
		return allStates;
	}

/*****************************************************/

	public void printSetList(final List<?> v)
	{

		int i=1;
		for(final Iterator<?> e = v.iterator(); e.hasNext(); )
		{

			final Set<?> vv = (Set<?>) e.next();
			System.out.println("Rad " + i);
			i++;
			for(final Iterator<?> ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}
/*****************************************************/

	public void printList(final List<?> v)
	{

		int i=1;
		for(final Iterator<?> e = v.iterator(); e.hasNext(); )
		{

			final List<?> vv = (List<?>) e.next();
			System.out.println("Rad " + i);
			i++;
			for(final Iterator<?> ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}
}
