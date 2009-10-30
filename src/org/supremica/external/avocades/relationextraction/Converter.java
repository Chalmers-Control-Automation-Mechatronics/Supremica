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

	public Document convertToUnsafe(Document safeIL)
	{
		unsafeILDoc = new Document(unsafeIL);
		Element root = safeIL.getRootElement();

		List<?> eventILList = root.getChildren("Event_interlocking");

		/* Put all components involved in the interlocking into a HashSet.
		   Put all safe states into sets and the sets into an ArrayList. */
		for(Iterator<?> eventILIter = eventILList.iterator(); eventILIter.hasNext(); )
		{
			Set<String> involvedComponents = new HashSet<String>();
			List<Set<List<String>>> safeStates = new ArrayList<Set<List<String>>>();
			//System.out.println("\n--------- Ny eventIL -------");
			Element eventIL = (Element) eventILIter.next();
			Element event = eventIL.getChild("Event");
			Element restr = event.getChild("Restriction");
			Element or = restr.getChild("Or");

			List<?> andList = or.getChildren("And");
			for(Iterator<?> andIter = andList.iterator(); andIter.hasNext(); )
			{
				Element and = (Element) andIter.next();
				List<?> stateList = and.getChildren("State");
				Set<List<String>> theState = new HashSet<List<String>>();

				for(Iterator<?> stateIter = stateList.iterator(); stateIter.hasNext(); )
				{
					Element state = (Element) stateIter.next();
					String stateName = state.getAttributeValue("name");
					if(!involvedComponents.contains(stateName))
					{
						involvedComponents.add(stateName);
					}
					String stateId = state.getAttributeValue("id");
					List<String> theName = new ArrayList<String>();
					theName.add(stateName);
					List<String> theId = new ArrayList<String>();
					theId.add(stateId);
					List<String> oneState = new ArrayList<String>();
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
			ArrayList<Set> allPossibleStates = getPossibleStates(root, involvedComponents);
			//printSetList(allPossibleStates);
			//System.out.println("---------------------");
			/* Convert the event interlocking to an expression in unsafe states,
			and save it in the xml document. */
			String eventName = eventIL.getAttributeValue("id");
			List<Set> unsafeStates = safeToUnsafe(safeStates, allPossibleStates);

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

	@SuppressWarnings("unchecked")
  public ArrayList<Set> getPossibleStates(Element root, Set<String> involvedComponents)
	{
		List<?> machineList = root.getChildren("Machine");
		ArrayList<Set> allPossibleStates = new ArrayList<Set>();
		boolean first = true;

		/* Search in the machine list for the involved components */
		for(Iterator<?> machIter = machineList.iterator(); machIter.hasNext(); )
		{
			Element machine = (Element) machIter.next();
			List<?> componentList = machine.getChildren("Component");

			for(Iterator<?> compIter = componentList.iterator(); compIter.hasNext(); )
			{
				Element component = (Element) compIter.next();
				String compName = component.getAttributeValue("name");

				/* If the component is an incolved component, ... */
				if(involvedComponents.contains(compName) )
				{
					List<String> theName = new ArrayList<String>();
					theName.add(compName);
					List<?> stateList = component.getChildren("State");
					int n = stateList.size(); /* Number of possible states for the component */
					int lengthAP=0; /* Length of allPossibleStates */

					if(!first)
					{
						ArrayList<Set> clone = (ArrayList<Set>) allPossibleStates.clone();

						/* Prolong allPossibleStates, to make place for the states of the
						next component. */
						for (Iterator<Set> cloneIter = clone.iterator(); cloneIter.hasNext();)
						{
							Set<?> copy = (HashSet<?>) cloneIter.next();
							for(int j=lengthAP+1; j<lengthAP+n; j++)
							{
								allPossibleStates.add(j, copy);
							}
							lengthAP=lengthAP+n;
						}
					}
					int place=1;

					/* For all states of the component */
					for(Iterator<?> stateIter = stateList.iterator(); stateIter.hasNext();)
					{
						Element state = (Element) stateIter.next();
						String stateId = state.getAttributeValue("id");
						List<String> theId = new ArrayList<String>();
						theId.add(stateId);

						if(first)
						{
							List<String> onePossibleState = new ArrayList<String>();
							onePossibleState.add(compName);
							onePossibleState.add(stateId);
							Set<List<String>> theState = new HashSet<List<String>>();
							theState.add(onePossibleState);
							allPossibleStates.add(theState);
							//printSetList(allPossibleStates);
						}
						else
						{
							// Append new states to old ones
							for(int k=place-1; k<lengthAP; k=k+n)
							{
								HashSet<List<String>> theState = (HashSet<List<String>>) allPossibleStates.remove(k);
								HashSet<List<String>> theStateClone = (HashSet<List<String>>) theState.clone();
								List<String> onePossibleState = new ArrayList<String>();
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

	@SuppressWarnings("unchecked")
    public List<Set> safeToUnsafe(List<Set<List<String>>> safeStates, ArrayList<Set> allPossibleStates)
	{
		ArrayList<Set> unsafeStates = (ArrayList<Set>) allPossibleStates.clone();

		for(Iterator<Set<List<String>>> safeIter = safeStates.iterator(); safeIter.hasNext(); )
		{
			HashSet<?> safeState = (HashSet<?>) safeIter.next();

			for(Iterator<Set> unsafeIter = allPossibleStates.iterator(); unsafeIter.hasNext(); )
			{
				Set<?> unsafeState = (HashSet<?>) unsafeIter.next();
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

	public void buildUnsafeDoc(Element root, String eventName, List<Set> unsafeStates)
	{

		Element event_interlocking = new Element("Event_interlocking");
		event_interlocking.setAttribute("id", eventName);
		unsafeIL.addContent(event_interlocking);

		Element event = new Element("Event");
		event_interlocking.addContent(event);

		Element restriction = new Element("Restriction");
		event.addContent(restriction);

		Element or = new Element("Or");
		restriction.addContent(or);

		for(Iterator<Set> unsafeIter = unsafeStates.iterator(); unsafeIter.hasNext(); )
		{
			Set<?> oneUnsafe = (HashSet<?>) unsafeIter.next();

			Element and = new Element("And");
			or.addContent(and);
			System.out.println("HIT " + oneUnsafe.toString());
			for(Iterator<?> oneUnsafeIter = oneUnsafe.iterator(); oneUnsafeIter.hasNext(); )
			{
				ArrayList<?> oneState = (ArrayList<?>) oneUnsafeIter.next();
				String stateName = (String) oneState.get(0);
				String stateId = (String) oneState.get(1);
				Element state = new Element("State");
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

	public ArrayList<ArrayList<String>> getStates(Element interlocking) {

		ArrayList<ArrayList<String>> allStates = new ArrayList<ArrayList<String>>();

		Element event = interlocking.getChild("Event");
		Element restriction = event.getChild("Restriction");
		Element or = restriction.getChild("Or");

		/* Get all ands. Corresponds to the rows in the Volvo case. */
		List<?> andList = or.getChildren("And");
		for(Iterator<?> andIter = andList.iterator(); andIter.hasNext(); )
		{
			Element and = (Element) andIter.next();
			/* Get the states for the first and, e.g. A and B */
			List<?> stateList = and.getChildren("State");
			ArrayList<String> oneState = new ArrayList<String>();
			/* For each state (component might be a better word), get its name and id.
			Id corresponds to the unsafe state of the state */
			for(Iterator<?> stateIter = stateList.iterator(); stateIter.hasNext(); )
			{
				Element state = (Element) stateIter.next();

				String stateName = state.getAttributeValue("name");
				String stateId = state.getAttributeValue("id");
				String stateExpression = stateName + stateId;

				oneState.add(stateExpression);
			}
			allStates.add(oneState);
		}
		return allStates;
	}

/*****************************************************/

	public void printSetList(List<?> v)
	{

		int i=1;
		for(Iterator<?> e = v.iterator(); e.hasNext(); )
		{

			Set<?> vv = (Set<?>) e.next();
			System.out.println("Rad " + i);
			i++;
			for(Iterator<?> ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}
/*****************************************************/

	public void printList(List<?> v)
	{

		int i=1;
		for(Iterator<?> e = v.iterator(); e.hasNext(); )
		{

			List<?> vv = (List<?>) e.next();
			System.out.println("Rad " + i);
			i++;
			for(Iterator<?> ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}
}
