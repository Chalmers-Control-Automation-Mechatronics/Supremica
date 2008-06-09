/*****************************************************
*
* Converts supervisor described as an automaton to
* an SOP.
*
*****************************************************/

package org.supremica.external.avocades.specificationsynthesis;

import java.util.*;
import javax.swing.*;
import java.applet.*;
import java.lang.*;

import java.io.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;




public class Extractor
{

	Document relationsDoc; // Output xml document.
	Element relations = new Element("SOP");

	public class Restriction {
		public String opId;
		public String opName;
		public List restrictions;

		public Restriction() {}

		public Restriction(String id, String name, ArrayList r)
		{
			opId = id;
			opName = name;
			restrictions = (List) r.clone();
		}
	}

	public Extractor() {}

/*****************************************************
*
* att fixa
*
* Kanske inte behöver ha operationens namn och id först i varje entry i
* restrictions. Då måste vi ändra i getStates, removeStates och remove-listan
* i simplifyOneOp.
*
******************************************************/



/*****************************************************
*
* extract Restrictions
*
*
*
******************************************************/

public Document extractRestrictions(Document sup)
{
	relationsDoc = new Document(relations);
	Element root = sup.getRootElement();
	List automaton = root.getChildren("Automaton");
	Iterator autIter = automaton.iterator();
	Element theSupervisor = (Element) autIter.next();

	List eventsList = theSupervisor.getChildren("Events");
	Iterator eventsIter = eventsList.iterator();
	Element events = (Element) eventsIter.next();

	List eventList = events.getChildren("Event");
	int noOfOps = (int) (eventList.size() * 0.5);
	List restrList = new ArrayList();

	/* opMatch contains a matching between operation names and start-event IDs. In each place lies an arrayList containing two strings, the operation name in place 0 and the corresponding start-event ID in place 1. */
	List opMatch = new ArrayList();

	int i = 0;
	for(Iterator eventIter = eventList.iterator(); eventIter.hasNext(); )
	{
		Element event = (Element) eventIter.next();
		String eventLabel = event.getAttributeValue("label");
		if(eventLabel.substring(0,2).equals("st"))
		{
			//System.out.println("start " + eventLabel);

			String eventId = event.getAttributeValue("id");
			String opName = eventLabel.substring(2);
			List opAndEvent = new ArrayList();
			opAndEvent.add(0,opName);
			opAndEvent.add(1,eventId);
			opMatch.add(i, opAndEvent);
			i++;
		}
		else if(!eventLabel.substring(0,3).equals("fin"))
		{
			/*Felmeddelande*/
		}
	}

	/* For all operations, find the restrictions for their starting. */
	for(Iterator matchIter = opMatch.iterator(); matchIter.hasNext(); )
	{
		Restriction restr = new Restriction();
		ArrayList m = (ArrayList) matchIter.next();
		String eventId = (String) m.get(1);

		Object dummy = m.get(1);
		String opId = dummy.toString(); // Get id of the operation
		dummy = m.get(0);
		String opName = dummy.toString();
		restr.opId = opId;
		restr.opName = opName;
		//System.out.println("***** opId " + opId +" opName " + opName + "*****");

		/* Get the supervisor states where eventId is enabled, e.g. q0 and q2 */
		List enabledInSupStates = getSupervisorStates(theSupervisor, eventId);

		//System.out.println("Klar med getSupStates");
		/* Find the corresponding states in each operation model, e.g. q0 corresponds to O1 (id=2) being in init and O2 (id=3) being in init, and q2 corresponds to O1 (id=2) being in comp and O2 (id=3) being in exec*/
		//Restriction buildRestriction(enabledInSupStates, eventId, opMatch);
		List enabledInModelStates = getModelStates(enabledInSupStates, eventId, opMatch);

		//System.out.println("Klar med getModelStates");

		restr.restrictions = enabledInModelStates;
		//printList(enabledInModelStates);
		restrList.add(restr);
	}

	/*int j=1;
	for(Iterator e = restrList.iterator(); e.hasNext(); )
	{
		Restriction vv = (Restriction) e.next();
		System.out.println("******** Restriction " + j + " ********");
		System.out.println("******** Fore simplify ********");
		j++;
		System.out.println(vv.opName);
		printStringList(vv.restrictions);
		System.out.println("*******************************");
	}
	System.out.println();*/


	ArrayList simpleRestr = simplify((ArrayList) restrList);

	int k=1;
	for(Iterator f = restrList.iterator(); f.hasNext(); )
		{
			Restriction ff = (Restriction) f.next();
			System.out.println("******** Restriction " + k + " ********");
			System.out.println("******** Efter simplify *******");
			k++;
			System.out.println(ff.opName);
			printStringList(ff.restrictions);
			System.out.println("*******************************");
		}
		System.out.println();


	//buildRelatiosDoc( eventName, unsafeStates);
	return relationsDoc;
}

/*****************************************************
*
* getSupervisorStates
*
* Creates an ArrayList of the names of the supervisor states
* where eventId is enabled, e.g. O1i.O2c and O1i.O2i
*
******************************************************/

public ArrayList getSupervisorStates(Element sup, String eventId)
{
	//ArrayList enabledInStateIds = new ArrayList();
	ArrayList enabledInStates = new ArrayList();

	List statesList = sup.getChildren("States");
	Iterator statesIter = statesList.iterator();
	Element states = (Element) statesIter.next();

	List transitionsList = sup.getChildren("Transitions");
	Iterator transitionsIter = transitionsList.iterator();
	Element transitions = (Element) transitionsIter.next();

	List stateList = states.getChildren("State");
	List transList = transitions.getChildren("Transition");

	/* Find state ID where eventID is enabled. */
	for(Iterator transIter = transList.iterator(); transIter.hasNext(); )
	{
		Element trans = (Element) transIter.next();
		String evId = trans.getAttributeValue("event");
		if(evId.equals(eventId))
		{
			String source = trans.getAttributeValue("source"); // Id of supervisor state
			//System.out.println("found event " + evId + " present in state with id " + source);

			Iterator stateIter = stateList.iterator();
			boolean found = false;
			while( !found && stateIter.hasNext() )
			{

				Element state = (Element) stateIter.next();
				String stId = state.getAttributeValue("id");
				//System.out.println("Letar i statelistan. stId="+ stId);
				if(stId.equals(source))
				{
					found = true;

					String stName = state.getAttributeValue("name");
					//System.out.println("Hittat state id " + stName);
					enabledInStates.add(stName);
				}
			}
		}
	}

	//System.out.println("eventID " + eventId + " enabled in:");
//	printStringList(enabledInStates);
	return enabledInStates;
}

/*****************************************************
*
* getModelStates
*
* Creates an ArrayList of the combinations of states of all
* other operations that enables execution of "eventID".
*
* opMatch contains a matching between operation names and start-event IDs.
* In each place lies an arrayList containing two strings, the operation
* name in place 0 and the corresponding start-event ID in place 1.
*
* supStates contains the names of the states in which eventId is enabled
*
******************************************************/

public ArrayList getModelStates(List supStates, String eventId, List match)
{
	/* For each of the operations in the system, find out what state it has in each of the supervisor states listed in supStates. Create an entry that contains the operation id, and then an ordered list of its states. The first state corresponds to the first */

System.out.println("******** getModelStates *******");

System.out.println("Entry for "+ eventId);

	ArrayList modelStates = new ArrayList();
	char stateSeparator = '.';
	String stateIndicator = "_";

	int i = 0;
	// For all operations
	for(Iterator matchIter = match.iterator(); matchIter.hasNext(); )
	{
		ArrayList operationStates = new ArrayList();
		ArrayList m = (ArrayList) matchIter.next();
		Object dummy = m.get(1);
		String opId = dummy.toString(); // Get id of the operation
		dummy = m.get(0);
		String opName = dummy.toString();
		operationStates.add(0, opId); // Put id and name in restriction list
		operationStates.add(1, opName);
		//System.out.println("Finding states of operation "+ opName);

		/* For each of the states where eventId is enabled, extract the states of all operations */
		int place = 2;
		for(Iterator stateIter = supStates.iterator(); stateIter.hasNext(); )
		{
			String supStateName = (String) stateIter.next();
			//System.out.println("Enabled in sup state "+ supStateName);
			String opStateName;
			int index = supStateName.indexOf(opName);
			int indexSeparator = supStateName.indexOf(stateSeparator, index);
			//System.out.println("sep " + indexSeparator);

			if(indexSeparator > index)
			{
				opStateName = supStateName.substring(index, indexSeparator);
			}
			else
			{
				// Last state in string, take rest of string.
				opStateName = supStateName.substring(index);
			}

			//System.out.println("Corresponds to " + opStateName);
			operationStates.add(place, opStateName);
			place++;
		}
		modelStates.add(i, operationStates);
		i++;
		//printStringList(operationStates);
	}

//System.out.println("********* modelstates: ******");
//printList(modelStates);
	return modelStates;
}

/*****************************************************
*
* simplify
*
* Simplifies the restrictions.
*
******************************************************/
public ArrayList simplify(ArrayList operationList)
{
	ArrayList simpleRestr = new ArrayList();
	simpleRestr = (ArrayList) operationList.clone();

	/* Remove the restriction O_init from the entry for O. */
	for(Iterator restrIter = simpleRestr.iterator(); restrIter.hasNext(); )
	{
		Restriction restr = (Restriction) restrIter.next();
		String opName = restr.opName;
		String opId = restr.opId;
		ArrayList rList = (ArrayList) restr.restrictions;

		Iterator rIter = rList.iterator();
		boolean found = false;
		int placeRList = 0;
		while( rIter.hasNext() && !found )
		{
			ArrayList oneRestr = (ArrayList) rIter.next();
			String name = (String) oneRestr.get(1);
			if( name.equals(opName) )
			{
				// The restriction entry to be removed has been found
				found = true;
				rList.remove(placeRList);
			}
			placeRList++;
		}


	}

	/* Replace O_init->string OR O_exec->string OR O_comp->string with O_- -> string. */
	ArrayList temp = (ArrayList) simpleRestr.clone();
	simpleRestr.clear();
	for(Iterator restrIter = temp.iterator(); restrIter.hasNext(); )
	{
		Restriction restr = (Restriction) restrIter.next();
		String opName = restr.opName;
		String opId = restr.opId;
		ArrayList rList = (ArrayList) restr.restrictions;

		Restriction oneSimpleRestr = simplifyOneOperation(restr);
		simpleRestr.add(oneSimpleRestr);
		//System.out.println("************ Simplified ****** " + oneSimpleRestr.opName);
		//printStringList(oneSimpleRestr.restrictions);
	}

	return simpleRestr;
}

/*****************************************************
*
* simplifyOneOperation
*
* Simplifies the restrictions for one operation. Returns a simplified
* Restriction to put in the list of the restrictions for all operations.
*
******************************************************/
public Restriction simplifyOneOperation(Restriction restr)
{

	/* Replace unnecessary restrictions with a - (instead of init/exec/comp). For example, if there are three restrictions, where O2 is in O2_init in all of them, whereas O1 is in O1_init, O1_exec and O1_comp respectively, then this can be simplified to one restriction where O2 is in O2_init and O1 is in - (don't care). */

	String stateIndicator = "_";
	String firstState = stateIndicator.concat("init");
	String secondState = stateIndicator.concat("exec");
	String thirdState = stateIndicator.concat("comp");
	String dontCareState = stateIndicator.concat("-");

	System.out.println("\n********** SimplifyOneOperation ***********");
	String op = (String) restr.opName;
	System.out.println("Simplify restrictions for operation " + op);

	// Restrictions for starting one operation (call it Op)
	Restriction simpleRestr = new Restriction(restr.opId, restr.opName, (ArrayList) restr.restrictions);


	System.out.println("At start, restrictions are ");
	printList(simpleRestr.restrictions);
	System.out.println("----------------------");


	ArrayList restrictions = (ArrayList) simpleRestr.restrictions;
	int noOfRestr = restrictions.size();
	int i = 0;

	// Work with one restriction entry at a time, i.e. one operation at a time
	while(i < noOfRestr)
	{
		ArrayList otherStatesList = new ArrayList(); //Each entry contains a set
		ArrayList allStatesList = new ArrayList();   //Each entry contains a set
		String allStates = new String();
		String opName = new String();

		//Get the restr (operation) we are currently working with. List containing strings.
		ArrayList states = (ArrayList) restrictions.get(i);
		int stateNumber = -1;

		/* For all states of the operation, e.g. O1_init, O1_init, O1_comp, O1_init etc. build two lists. allStatesList that in each entry contains a string for each alternative state combination, and otherStatesList that in each entry contains a string representing the states of the other operations (e.g. all but O1). */
		for(Iterator stateIter = states.iterator(); stateIter.hasNext(); )
		{
			String stateName = (String) stateIter.next();
			if(stateNumber == 2)
			{
				opName = stateName.substring(0, stateName.indexOf(stateIndicator)); // e.g. O1
				System.out.println("*** Simplify entry for opName = " + opName + " ***");
			}
			stateNumber++;
			//System.out.println("SimplifyOneOperation stateNumber " + stateNumber);

			// Into the real states (leaving name and id)
			if(stateNumber > 1)
			{
				System.out.println("statename= " + stateName);

				//String allOtherStates, firstPart, lastPart;
				Set allOtherStates = new HashSet();
				Set temp = new HashSet();
				//String temp = new String();

				// e.g. O1_initO2_compO3_exec in each ArrayList-entry
				allOtherStates = getStates(stateNumber, restrictions);
				allStatesList.add(getStates(stateNumber, restrictions));


				//System.out.println("temp= " + temp);
				//firstPart = temp.substring(0, temp.indexOf(stateName));
				//System.out.println("firstPart= " + firstPart);
				//lastPart = temp.substring(temp.indexOf(stateName) + stateName.length());
				//System.out.println("lastPart= " + lastPart);


				//allOtherStates = firstPart.concat(lastPart);
				//allOtherStates.add(temp);
				//System.out.println("allOtherStates fore remove statename ");
				//printStringSet(allOtherStates);
				allOtherStates.remove(stateName);
			    //System.out.println("allOtherStates efter remove statename ");
			    //printStringSet(allOtherStates);
			    otherStatesList.add(allOtherStates);

			}
		}

		System.out.println("*** Before remove, allStatesList = ***");
		printSetList(allStatesList);
		/*System.out.println("otherStatesList = ");
		printSetList(otherStatesList);
		System.out.println("*********************************");*/

		int noOfRestrEntrys = states.size();
		String rem = "rem", keep = "keep";
		ArrayList treated = new ArrayList();
		ArrayList remove = new ArrayList();
		for(int ii=0; ii<noOfRestrEntrys; ii++)
		{
			remove.add(keep);
		}

		int indF, indS, indT;
		for(Iterator otherIter = otherStatesList.iterator(); otherIter.hasNext(); )
		{
			//System.out.println("Inside other-loop");
			HashSet other = (HashSet) otherIter.next();
			//String other = (String) otherIter.next();

			// Only check each restriction string once.
			if( !treated.contains(other) )
			{
				//Set first = new HashSet(other);
				HashSet first = new HashSet();
				HashSet second = new HashSet();
				HashSet third = new HashSet();

//System.out.println("hit0");
				// Funkar inte med clone eller constructorn
				for(Iterator oIter = other.iterator(); oIter.hasNext(); )
				{
					//System.out.println("hit");
					String n = (String) oIter.next();
					//System.out.println("n= " + n);
					first.add(n);
					second.add(n);
					third.add(n);
				}

				/*first = opName.concat(firstState).concat(other);
				second = opName.concat(secondState).concat(other);
				third = opName.concat(thirdState).concat(other);*/

				first.add(opName.concat(firstState));
				second.add(opName.concat(secondState));
				third.add(opName.concat(thirdState));

				// Check if there exist e.g. O1_init -> string, O1_exec -> string and O1_comp -> string
				indF = allStatesList.indexOf(first);
				indS = allStatesList.indexOf(second);
				indT = allStatesList.indexOf(third);

				System.out.println("first: ");
				printStringSet(first);
				System.out.println("indF = " + indF);
				System.out.println("indS = " + indS);
				System.out.println("indT = " + indT);
				//System.out.println("second = " + second + " indS = " + indS);
			//	System.out.println("third = " + third + " indT = " + indT);

				// If there exist e.g. O1_init -> string, O1_exec -> string and O1_comp -> string, simplify to O1_- -> string, where - meand don't care.
				if(indF > -1 && indS > -1 && indT > -1)
				{

					/*states.remove(indF+2);
					String adding = opName.concat(dontCareState).concat(other);
					System.out.println("Add dont care: " + adding);
					states.add(indF+2, adding);*/

					states.remove(indF+2);
					String adding = opName.concat(dontCareState);
					System.out.println("Add dont care: " + adding);
					states.add(indF+2, adding);

					remove.remove(indS+2);
					remove.add(indS+2, rem);
					remove.remove(indT+2);
					remove.add(indT+2, rem);

					/*System.out.println("*** states = ****");
					printStringList(states);
					System.out.println("*** restrictions = ****");*/

					restrictions.remove(i);
					restrictions.add(i, states);
					//printList(restrictions);

				}

				System.out.println("*** After remove, allStatesList = ****");
				printSetList(allStatesList);
				System.out.println("*********************************");
				treated.add(other);
			}
		}

		// Perform the removing. Replace the restriction list with a new one, where the unnecessary entrys in each operation restriction list have been removed.

		/*System.out.println("remove:");
		printStringList(remove);*/
		for(int k = remove.size()-1; k>1 ; k--)
		{
			if( remove.get(k) == rem )
			{
				restrictions = removeStates(k, restrictions);
			}
		}

		i++;
		simpleRestr.restrictions = (ArrayList) restrictions.clone();
	}

	return simpleRestr;
}

/*****************************************************
*
* getStates
*
* Get a Set containing the total restriction in place i,
* i.e. the states (Strings) of all operations.
*
* restrictions is the list of restrictions located within a
* Restriction instance.
*
******************************************************/
public HashSet getStates(int index, ArrayList restrictions)
{
	if(index<2)
	{
		System.err.println("getStates: Index must be at least 2");
		return null;
	}

	HashSet c = new HashSet();
	//String states = new String();
	//ArrayList states = new ArrayList();
	//int i = 0;
	for(Iterator rIter = restrictions.iterator(); rIter.hasNext(); )
	{
		ArrayList oneOperation = (ArrayList) rIter.next();
		String temp = (String) oneOperation.get(index);
		//states.add(i, temp);
		//System.out.println("getStates, temp = " + temp);
		//states = states.concat(temp);
		c.add(temp);
		//System.out.println("getStates, states = " + states);
		//i++;
	}

	return c;
}

/*****************************************************
*
* removeStates
*
*
******************************************************/
public ArrayList removeStates(int index, ArrayList restrictionList)
{
	if(index<2)
	{
		System.err.println("removeStates: Index must be at least 2");
		return null;
	}

	ArrayList newRestrList = new ArrayList();
	int i = 0;
	for(Iterator rIter = restrictionList.iterator(); rIter.hasNext(); )
	{
		ArrayList oneOperation = (ArrayList) rIter.next();
		oneOperation.remove(index);
		newRestrList.add(i, oneOperation);
		i++;
	}
	return newRestrList;
}

/*****************************************************
*
* buildRelationsDoc
*
* Build document containing the SOPs. Probably the machine sequences
* are needed here.
*
******************************************************/

public void buildRelationsDoc(String eventName, List unsafeStates)
{
	/* Om - finns i restriktionsuttrycket ar den operationen ointressant. */
	Element event_interlocking = new Element("Event_interlocking");
	event_interlocking.setAttribute("id", eventName);
	relations.addContent(event_interlocking);

	Element event = new Element("Event");
	event_interlocking.addContent(event);

	Element restriction = new Element("Restriction");
	event.addContent(restriction);

	Element or = new Element("Or");
	restriction.addContent(or);

	for(Iterator unsafeIter = unsafeStates.iterator(); unsafeIter.hasNext(); )
	{
		Set oneUnsafe = (HashSet) unsafeIter.next();

		Element and = new Element("And");
		or.addContent(and);
		System.out.println("HIT " + oneUnsafe.toString());
		for(Iterator oneUnsafeIter = oneUnsafe.iterator(); oneUnsafeIter.hasNext(); )
		{
			ArrayList oneState = (ArrayList) oneUnsafeIter.next();
			String stateName = (String) oneState.get(0);
			String stateId = (String) oneState.get(1);
			Element state = new Element("State");
			state.setAttribute("name", stateName);
			state.setAttribute("id", stateId);
			and.addContent(state);
		}
	}
}



/*****************************************************/

	public void printSetList(List v)
	{

		int i=1;
		for(Iterator e = v.iterator(); e.hasNext(); )
		{
			Set vv = (Set) e.next();
			System.out.println("Rad " + i);
			i++;
			for(Iterator ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}
/*****************************************************/

	public void printList(List v)
	{
		int i=1;
		for(Iterator e = v.iterator(); e.hasNext(); )
		{
			List vv = (List) e.next();
			System.out.println("Rad " + i);
			i++;
			for(Iterator ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}

/*****************************************************/

	public void printStringList(List v)
	{
		System.out.println();
		int i=1;
		for(Iterator e = v.iterator(); e.hasNext(); )
		{
			//System.out.println("Rad " + i);
			System.out.println(e.next());
			i++;
		}
		System.out.println();
	}

/*****************************************************/

	public void printStringSet(Set v)
	{
		for(Iterator ee = v.iterator(); ee.hasNext(); )
		{
			System.out.println(ee.next());
		}
		System.out.println();
	}
/*****************************************************/

}