/*****************************************************
*
* Converts supervisor described as an automaton to
* COPs.
*
*****************************************************/

package org.supremica.external.avocades.relationextraction;

import java.util.*;

import org.jdom.*;

import java.util.Hashtable;

/*-----------------------------------------------------------------------------
 *
 * Imported constants
 *
 *-----------------------------------------------------------------------------*/

import static org.supremica.external.avocades.AutomataNames.OPERATION_START_PREFIX;
import static org.supremica.external.avocades.AutomataNames.OPERATION_STOP_PREFIX;

import static org.supremica.external.avocades.AutomataNames.STATE_SEPARATOR;
import static org.supremica.external.avocades.AutomataNames.STATE_INDICATOR;

import static org.supremica.external.avocades.AutomataNames.INITIAL_STATE_POSTFIX;
import static org.supremica.external.avocades.AutomataNames.EXECUTION_STATE_POSTFIX;
import static org.supremica.external.avocades.AutomataNames.END_STATE_POSTFIX;
import static org.supremica.external.avocades.AutomataNames.DONT_CARE_STATE_POSTFIX;

public class Extractor
{
	private ArrayList<Document> COPList;
	@SuppressWarnings("unused")
	private Document relationsDoc; // Output xml document.
	@SuppressWarnings("unused")
	private final Element relations = new Element("COP");

	public class Restriction {
		public String opId;
		public String opName;
		public List<?> restrictions;

		public Restriction() {}

		public Restriction(final String id, final String name, final ArrayList<?> r)
		{
			opId = id;
			opName = name;
			restrictions = (List<?>) r.clone();
		}
	}

	public Extractor() {}




/*****************************************************
*
* extract Restrictions
*
*
*
******************************************************/

@SuppressWarnings("unchecked")
public ArrayList<Document> extractRestrictions(final Document sup, final ArrayList<Document> ROPs)
{
	COPList = new ArrayList<Document>(ROPs);

	final Element root = sup.getRootElement();
	final List<Element> automaton = root.getChildren("Automaton");
	final Iterator<Element> autIter = automaton.iterator();
	final Element theSupervisor = autIter.next();

	final List<Element> eventsList = theSupervisor.getChildren("Events");
	final Iterator<Element> eventsIter = eventsList.iterator();
	final Element events = eventsIter.next();

	final List<Element> eventList = events.getChildren("Event");
	final List<Restriction> restrList = new ArrayList<Restriction>();

	/* opMatch contains a matching between operation names and start-event IDs.
	In each place lies an arrayList containing two strings, the operation name in
	place 0 and the corresponding start-event ID in place 1. */

	/* Key is operation name who gives the operation id */
	final Hashtable<String, String> opMatch = new Hashtable<String, String>();

	int i = 0;
	for(final Iterator<Element> eventIter = eventList.iterator(); eventIter.hasNext(); )
	{
		final Element event = eventIter.next();
		final String eventLabel = event.getAttributeValue("label");
		if(eventLabel.length() > OPERATION_START_PREFIX.length())
		{
			if( eventLabel.substring(0, OPERATION_START_PREFIX.length()).
					equals(OPERATION_START_PREFIX) )
			{
				final String eventId = event.getAttributeValue("id");
				String opName = eventLabel.substring(OPERATION_START_PREFIX.length());

				if( opName.contains("_") ){
					//EFA fix
					opName = opName.substring( 0, opName.indexOf("_") );
					//End EFA fix
					opMatch.put(opName, eventId);
				} else {
					opMatch.put(opName, eventId);
				}

				i++;
			}
			else if( !eventLabel.substring(0,OPERATION_STOP_PREFIX.length()).
					equals(OPERATION_STOP_PREFIX) )
			{
				;//do nothing
			}
		}
	}

	/* For all operations, find the restrictions for their starting. */
	for(final Iterator<String> matchIter = opMatch.keySet().iterator(); matchIter.hasNext(); )
	{
		final Restriction restr = new Restriction();

		restr.opName = matchIter.next();
		restr.opId = opMatch.get( restr.opName ); // Get id of the operation

		/* Get the supervisor states where eventId is enabled, e.g. q0 and q2 */
		final List<String> enabledInSupStates = getSupervisorStates(theSupervisor, restr.opId);

		final List<?> enabledInModelStates = getModelStates(enabledInSupStates,
				                                   restr.opId,
				                                   opMatch);

		restr.restrictions = enabledInModelStates;
		//printList(enabledInModelStates);
		restrList.add(restr);
	}

	final ArrayList<Restriction> simpleRestr = simplify((ArrayList<Restriction>) restrList);

	buildCOPDocs(simpleRestr, ROPs);
	return COPList;
}

/*****************************************************
*
* getSupervisorStates
*
* Creates an ArrayList of the names of the supervisor states
* where eventId is enabled, e.g. O1i.O2c and O1i.O2i
*
******************************************************/

@SuppressWarnings("unchecked")
public ArrayList<String> getSupervisorStates(final Element sup, final String eventId)
{
	//ArrayList enabledInStateIds = new ArrayList();
	final ArrayList<String> enabledInStates = new ArrayList<String>();

	final List<Element> statesList = sup.getChildren("States");
	final Iterator<Element> statesIter = statesList.iterator();
	final Element states = statesIter.next();

	final List<Element> transitionsList = sup.getChildren("Transitions");
	final Iterator<Element> transitionsIter = transitionsList.iterator();
	final Element transitions = transitionsIter.next();

	final List<Element> stateList = states.getChildren("State");
	final List<Element> transList = transitions.getChildren("Transition");

	/* Find state ID where eventID is enabled. */
	for(final Iterator<Element> transIter = transList.iterator(); transIter.hasNext(); )
	{
		final Element trans = transIter.next();
		final String evId = trans.getAttributeValue("event");
		if(evId.equals(eventId))
		{
			final String source = trans.getAttributeValue("source"); // Id of supervisor state

			final Iterator<Element> stateIter = stateList.iterator();
			boolean found = false;
			while( !found && stateIter.hasNext() )
			{

				final Element state = stateIter.next();
				final String stId = state.getAttributeValue("id");

				if(stId.equals(source))
				{
					found = true;
					final String stName = state.getAttributeValue("name");
					enabledInStates.add(stName);
				}
			}
		}
	}

	//printStringList(enabledInStates);
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

public ArrayList<?> getModelStates(final List<String> supStates, final String eventId, final Hashtable<String, String> match)
{
	/* For each of the operations in the system, find out what state it has in
	each of the supervisor states listed in supStates. Create an entry that
	contains the operation id, and then an ordered list of its states. The first
	state corresponds to the first */

	final ArrayList<ArrayList<String>> modelStates = new ArrayList<ArrayList<String>>();

	int i = 0;
	// For all operations
	for(final Iterator<String> matchIter = match.keySet().iterator(); matchIter.hasNext(); )
	{
		final ArrayList<String> operationStates = new ArrayList<String>();

		final String opName = matchIter.next();
		final String opId = match.get( opName ); // Get id of the operation

		operationStates.add(0, opId); // Put id and name in restriction list
		operationStates.add(1, opName);

		/* For each of the states where eventId is enabled, extract the states of all operations */
		int place = 2;
		for(final Iterator<String> stateIter = supStates.iterator(); stateIter.hasNext(); )
		{
			final String supStateName = stateIter.next();

			String opStateName;
			final int index = supStateName.indexOf(opName + STATE_INDICATOR);
			final int indexSeparator = supStateName.indexOf(STATE_SEPARATOR, index);

			if ( indexSeparator > index )
			{
				opStateName = supStateName.substring( index, indexSeparator );
			}
			else
			{
				// Last state in string, take rest of string.
				opStateName = supStateName.substring( index );
			}

			//EFA fix by David
			if ( opStateName.contains(opName + INITIAL_STATE_POSTFIX) )
			{
				opStateName = opName + INITIAL_STATE_POSTFIX;
			}
			else if ( opStateName.contains(opName + EXECUTION_STATE_POSTFIX) )
			{
				opStateName = opName + EXECUTION_STATE_POSTFIX;
			}
			else if ( opStateName.contains(opName + END_STATE_POSTFIX) )
			{
				opStateName = opName + END_STATE_POSTFIX;
			}else{
				//Something is wrong
				System.out.println( opStateName );
			}
			//End EFA fix

			operationStates.add(place, opStateName);
			place++;
		}
		modelStates.add(i, operationStates);
		i++;
		//printStringList(operationStates);
	}

	return modelStates;
}

/*****************************************************
*
* simplify
*
* Simplifies the restrictions.
*
* 1. Find predecessors.
* 2. Remove all but last predecessor.
* 3. If O2[O1\da], remove O2_init from O1.
* Loop:
* 4. Remove IEC-entries, i.e. if three restrictions are equal,
*    except that Ox_init, Ox_exec and Ox_comp in the three, respectively,
*    replace the three with one, where Ox_-.
* 5. If Ox[Oy\da], replace [Ox_comp & Oy_comp] with [Ox_comp].
* 6. If Ox[Oy\da], replace [Ox_exec & Oy_comp] with [Ox_exec].
* 7. If Ox[Oy\da], replace [Ox_init & Oy_init] with [Oy_init].
* 8. If Ox[Oy\da], replace [Ox_init & Oy_exec] with [Oy_exec].
* 9. Remove IEC-entries, i.e. if three restrictions can be made equal,
*    if predecessors are set to comp and/or successors are set to init,
*    except that Ox_init, Ox_exec and Ox_comp in the three, respectively,
*    replace the three with one, where Ox_-.
* End_loop;
*
******************************************************/
@SuppressWarnings("unchecked")
public ArrayList<Restriction> simplify(final ArrayList<Restriction> operationList)
{

	ArrayList<Restriction> simpleRestr = new ArrayList<Restriction>();
	simpleRestr = (ArrayList<Restriction>) operationList.clone();


	/* Remove the restriction O_init from the entry for O. */
	for(final Iterator<Restriction> restrIter = simpleRestr.iterator(); restrIter.hasNext(); )
	{
		final Restriction restr = restrIter.next();
		final String opName = restr.opName;
		final ArrayList<?> rList = (ArrayList<?>) restr.restrictions;

		final Iterator<?> rIter = rList.iterator();
		boolean found = false;
		int placeRList = 0;
		while( rIter.hasNext() && !found )
		{
			final ArrayList<?> oneRestr = (ArrayList<?>) rIter.next();
			final String name = (String) oneRestr.get(1);
			if( name.equals(opName) )
			{
				// The restriction entry to be removed has been found
				found = true;
				rList.remove(placeRList);
			}
			placeRList++;
		}
	}

	ArrayList<Restriction> simpleRestrOld = new ArrayList<Restriction>();
	ArrayList<Restriction> simpleRestrOld2 = new ArrayList<Restriction>();
	ArrayList<Restriction> simpleRestrOld3 = new ArrayList<Restriction>();


	/* Step 1: Find predecessors. */
	final ArrayList<ArrayList<String>> predSucc = new ArrayList<ArrayList<String>>();
	for(final Iterator<Restriction> restrIter = simpleRestr.iterator(); restrIter.hasNext(); )
	{
		final Restriction restr = restrIter.next();
		final ArrayList<ArrayList<String>> ps = findPredecessors(restr);
		predSucc.addAll(ps);
	}

	/* Step 3: If Oi is a predecessor to Oj, for all restrictions for Oi, remove the
	   entries Oj_init. */
	simpleRestr = removeSuccessors(simpleRestr, predSucc);
	simpleRestr = removeEqualAlternatives(simpleRestr);
	simpleRestr = removeDontCareOperations(simpleRestr);

	do {
		simpleRestrOld = copyRestrictions(simpleRestr);

		int i0=0;
		do {
			simpleRestrOld2 = copyRestrictions(simpleRestr);

			// Step 4: Replace O_init->string OR O_exec->string OR O_comp->string with O_- -> string.
			simpleRestr = findReplaceIEC(simpleRestr);
			simpleRestr = removeEqualAlternatives(simpleRestr);
			simpleRestr = removeDontCareOperations(simpleRestr);
			i0++;
		} while( compareRestrictions(simpleRestrOld2, simpleRestr) == -1);

		/* Step 5: If Oi -> Oj -> Ok, Ok[Oi_comp AND Oj_comp] is reduced to Ok[Oj_comp] */
		simpleRestr = removeCompSequences(simpleRestr, predSucc);

		int i1=0;
		do {
			simpleRestrOld3 = copyRestrictions(simpleRestr);

			// Step 3: Replace O_init->string OR O_exec->string OR O_comp->string with O_- -> string.
			simpleRestr = findReplaceIEC(simpleRestr);
			simpleRestr = removeEqualAlternatives(simpleRestr);
			simpleRestr = removeDontCareOperations(simpleRestr);
			i1++;
		} while( compareRestrictions(simpleRestrOld3, simpleRestr) == -1);
		/* Step 6: If Oi -> Oj, Ok[Oi_comp AND Oj_exec] is reduced to Ok[Oj_exec] */
		simpleRestr = removeCompExecSequences(simpleRestr, predSucc);

		/* This removal is important to not get lots of unnecessary iec-structures... */
		int i2=0;
		do {
			simpleRestrOld3 = copyRestrictions(simpleRestr);
			simpleRestr = findReplaceIEC(simpleRestr);
			simpleRestr = removeEqualAlternatives(simpleRestr);
			simpleRestr = removeDontCareOperations(simpleRestr);
			i2++;
		} while( compareRestrictions(simpleRestrOld3, simpleRestr) == -1);

		/* Step 7: If Oi -> Oj, Ok[Oi_init AND Oj_init] is reduced to Ok[Oi_init] */
		simpleRestr = removeInitInitSequences(simpleRestr, predSucc);

		int i3=0;
		do {
			simpleRestrOld3 = copyRestrictions(simpleRestr);
			simpleRestr = findReplaceIEC(simpleRestr);
			simpleRestr = removeEqualAlternatives(simpleRestr);
			simpleRestr = removeDontCareOperations(simpleRestr);
			i3++;
		} while( compareRestrictions(simpleRestrOld3, simpleRestr) == -1);

		/* Step 8: If Oi -> Oj, Ok[Oi_exec AND Oj_init] is reduced to Ok[Oi_exec] */
		simpleRestr = removeExecInitSequences(simpleRestr, predSucc);

		int i4=0;
		do {
			simpleRestrOld3 = copyRestrictions(simpleRestr);
			simpleRestr = findReplaceIEC(simpleRestr);
			simpleRestr = removeEqualAlternatives(simpleRestr);
			simpleRestr = removeDontCareOperations(simpleRestr);
			i4++;
		} while( compareRestrictions(simpleRestrOld3, simpleRestr) == -1);


		/* Step 9: Remove alternative restrictions that are equal */
		simpleRestr = removeEqualAlternatives(simpleRestr);
		simpleRestr = removeDontCareOperations(simpleRestr);


	} while( compareRestrictions(simpleRestrOld,simpleRestr) == -1);


	simpleRestr = removeDontCareStates(simpleRestr);
	simpleRestr = removeDontCareOperations(simpleRestr);

	simpleRestr = replaceIECpredSucc(simpleRestr, predSucc);
	simpleRestr = removeDontCareOperations(simpleRestr);


	return simpleRestr;
}

/*****************************************************
*
* ReplaceIECpredSucc
*
* Replace structures where Ox is in init, exec and comp in
* restrictions R1, R2 and R3 respectively, and the rest of the
* restrictions either are equal or can be made equal if
* predecessors are set to comp (when Ox equals exec or comp)
* and/or successors are set to init (when Ox equals init or exec).
*
******************************************************/
public ArrayList<Restriction> replaceIECpredSucc(final ArrayList<Restriction> restr, final ArrayList<ArrayList<String>> ps)
{

	for(final Iterator<Restriction> restrIter = restr.iterator(); restrIter.hasNext(); )
	{
		final Restriction operation = restrIter.next();
		ArrayList<?> rList = (ArrayList<?>) operation.restrictions;

		boolean changed = true;

		while(changed)
		{
			changed = false;
			// As long as at least one change is done in the restriction list
			final Iterator<?> rIter = rList.iterator();
			while( rIter.hasNext() )
			{
				final ArrayList<?> states = (ArrayList<?>) rIter.next();
				int noOfStates = states.size();
				String op = new String();
				final HashSet<String> preds = new HashSet<String>();
				final HashSet<String> succs = new HashSet<String>();
				String allOpStatesFirst = new String();
				String allOpStatesSecond = new String();
				String allOpStatesThird = new String();

				int i1=0;

				while( i1<noOfStates)
				{
					final String state = (String) states.get(i1);

					if(i1==1)
					{
						op = state;
					}

					if(state.equals(op.concat(END_STATE_POSTFIX)))
					{
						allOpStatesThird = getRestrString(rList, i1);
						allOpStatesThird = allOpStatesThird.replaceAll(op.concat(END_STATE_POSTFIX), "");

						int i2=0;
						while(i2<noOfStates)
						{
							final String state2 = (String) states.get(i2);
							if(state2.equals(op.concat(EXECUTION_STATE_POSTFIX)))
							{
								allOpStatesSecond = getRestrString(rList, i2);
								allOpStatesSecond = allOpStatesSecond.replaceAll(op.concat(EXECUTION_STATE_POSTFIX), "");

								int i3=0;
								while(i3<noOfStates)
								{
									final String state3 = (String) states.get(i3);
									if(state3.equals(op.concat(INITIAL_STATE_POSTFIX)))
									{
										allOpStatesFirst = getRestrString(rList, i3);
										allOpStatesFirst = allOpStatesFirst.replaceAll(op.concat(INITIAL_STATE_POSTFIX), "");

										// All strings found

										final HashSet<String> thePreds = new HashSet<String>();
										final HashSet<String> theSuccs = new HashSet<String>();
										final HashSet<?> tempPreds = getPredecessors(op, ps);
										final HashSet<?> tempSuccs = getSuccessors(op, ps);
										final HashSet<String> rOps = getRestrictionOps(rList);

										for( final Iterator<String> rOpsIter = rOps.iterator(); rOpsIter.hasNext(); )
										{
											final Object temp = rOpsIter.next();

											if(tempPreds.contains(temp))
											{
												preds.add((String) temp);
											}
											else if(tempSuccs.contains(temp))
											{
												succs.add((String) temp);
											}
										}

										final ArrayList<String> predsSuccs = new ArrayList<String>(preds);
										predsSuccs.addAll(succs);

										final boolean equal = findPredSucc(allOpStatesFirst, allOpStatesSecond, allOpStatesThird, predsSuccs, thePreds, theSuccs, preds.size(), succs.size());

										if(equal)
										{

											changed = true;
											rList = replaceState(rList, op, op.concat(DONT_CARE_STATE_POSTFIX), i1);
											if(thePreds.size() > 0)
											{
												for(final Iterator<String> predIter = thePreds.iterator(); predIter.hasNext(); )
												{

													final String thePred = predIter.next();

													rList = replaceState(rList, thePred, thePred.concat(END_STATE_POSTFIX), i1);
												}
											}
											if(theSuccs.size() > 0)
											{
												for(final Iterator<String> succIter = theSuccs.iterator(); succIter.hasNext(); )
												{
													final String theSucc = succIter.next();

													rList = replaceState(rList, theSucc, theSucc.concat(INITIAL_STATE_POSTFIX), i1);
												}
											}
											if(i2>i3)
											{
												rList = removeStates(i2, rList);
												rList = removeStates(i3, rList);
											}
											else
											{
												rList = removeStates(i3, rList);
												rList = removeStates(i2, rList);
											}
											noOfStates = states.size();

										}
									}
									i3++;
								}
							}
							i2++;
						}
					}
					i1++;
				}
			}
		}
	}
	return restr;
}


/*****************************************************
*
* findPredSucc
*
* Check if the three strings can be equal.
* Returns true if equal, false otherwise. Indicates the
* predecessor(s) that need to be set to comp, and the successor(s)
* that needs to be set to init.
*
******************************************************/
public boolean findPredSucc
(
	final String si, final String se, final String sc,
	final ArrayList<String> predsSuccs,
	final HashSet<String> thePreds,
	final HashSet<String> theSuccs,
	final int noPreds, final int noSuccs
)
{
	boolean ans = false;

	if(si.equals(se) && si.equals(sc))
	{
		return true;
	}
	else if(predsSuccs.size() == 0)
	{
		// Not equal, and no more preds/succs to try with
		return false;
	}
	else
	{
		int i = 0;
		int noPredsSuccs = predsSuccs.size();
		while( noPredsSuccs > 0 && !ans )
		{
			// Study all other preds, i.e. remPreds.remove(pred);
			final String ps = predsSuccs.get(0);

			final ArrayList<String> remPS = new ArrayList<String>(predsSuccs);
			remPS.remove(0);
			int noPredsNew = noPreds, noSuccsNew = noSuccs;

			String si2 = new String();
			String se2 = new String();
			String sc2 = new String();

			// If ps is a predecessor
			if(i<noPreds)
			{
				noPredsNew--;
				thePreds.add(ps);
				si2 = si;

				se2 = se.replaceAll( ps.concat(DONT_CARE_STATE_POSTFIX),
						             ps.concat(END_STATE_POSTFIX));

				sc2 = sc.replaceAll( ps.concat(DONT_CARE_STATE_POSTFIX),
						             ps.concat(END_STATE_POSTFIX));
			}
			else // ps is a successor
			{
				noSuccsNew--;
				theSuccs.add(ps);
				sc2 = sc;
				se2 = se.replaceAll( ps.concat(DONT_CARE_STATE_POSTFIX),
						             ps.concat(INITIAL_STATE_POSTFIX));

				si2 = si.replaceAll( ps.concat(DONT_CARE_STATE_POSTFIX),
						             ps.concat(INITIAL_STATE_POSTFIX));

			}

			ans = findPredSucc(si2, se2, sc2, remPS, thePreds,
					                                 theSuccs,
					                                 noPredsNew,
					                                 noSuccsNew);

			if(!ans && i<noPreds)
			{
				thePreds.remove(ps);
			}
			else if(!ans && i>=noPreds)
			{
				theSuccs.remove(ps);
			}
			predsSuccs.remove(0);
			noPredsSuccs--;
			i++;
		}
		return ans;
	}
}


/*****************************************************
*
* getRestrictionOps
*
* Returns the operations that participate in the
* restrictions specified by rList.
*
******************************************************/
HashSet<String> getRestrictionOps(final ArrayList<?> rList)
{
	final HashSet<String> h = new HashSet<String>();

	for( final Iterator<?> rIter = rList.iterator(); rIter.hasNext(); )
	{
		final ArrayList<?> states = (ArrayList<?>) rIter.next();
		final String opName = (String) states.get(1);
		h.add(opName);
	}

	return h;
}
/*****************************************************
*
* findReplaceIEC
*
*
*
******************************************************/
@SuppressWarnings("unchecked")
public ArrayList<Restriction> findReplaceIEC(final ArrayList<Restriction> r)
{
	final ArrayList<Restriction> temp = (ArrayList<Restriction>) r.clone();
	final ArrayList<Restriction> simpleRestr = new ArrayList<Restriction>();
	for(final Iterator<Restriction> restrIter = temp.iterator(); restrIter.hasNext(); )
	{
		final Restriction restr = restrIter.next();
		final Restriction oneSimpleRestr = replaceIEC(restr);
		simpleRestr.add(oneSimpleRestr);

	}
	return simpleRestr;
}

/*****************************************************
*
* replaceIEC
*
* Finds IEC-structures and removes them. The difference between
* this function and replaceIECpredSucc is that this function does not
* try to change predecessors/successors to find removable IEC-structures.
*
******************************************************/
@SuppressWarnings("unchecked")
public Restriction replaceIEC(final Restriction restr)
{
	/*
	 * Replace unnecessary restrictions with a - (instead of init/exec/comp).
	 * For example, if there are three restrictions, where O2 is in O2_init
	 * in all of them, whereas O1 is in O1_init, O1_exec and O1_comp
	 * respectively, then this can be simplified to one restriction
	 * where O2 is in O2_init and O1 is in - (don't care).
	 */

	// Restrictions for starting one operation (call it Op)
	final Restriction simpleRestr = new Restriction(restr.opId, restr.opName, (ArrayList<?>) restr.restrictions);

	ArrayList<ArrayList<String>> restrictions = (ArrayList<ArrayList<String>>) simpleRestr.restrictions;
	final int noOfRestr = restrictions.size();
	int i = 0;

	// Work with one restriction entry at a time, i.e. one operation at a time
	/* Build lists, where each entry contains the states of all (other) operations,
	   i.e each entry contains one restriction. */
	while(i < noOfRestr)
	{
		final ArrayList<Set<?>> otherStatesList = new ArrayList<Set<?>>(); //Each entry contains a set
		final ArrayList<HashSet<String>> allStatesList = new ArrayList<HashSet<String>>();   //Each entry contains a set
		String opName = new String();

		//Get the restr (operation) we are currently working with. List containing strings.
		final ArrayList<String> states = (ArrayList<String>) restrictions.get(i);
		int stateNumber = -1;

		/* For all states of the operation, e.g. O1_init, O1_init, O1_comp,
		 * O1_init etc. build two lists. allStatesList that in each entry
		 * contains a string for each alternative state combination, and
		 * otherStatesList that in each entry contains a string representing
		 * the states of the other operations (e.g. all but O1).
		 */
		for(final Iterator<String> stateIter = states.iterator(); stateIter.hasNext(); )
		{
			final String stateName = stateIter.next();

			if(stateNumber == 2)
			{
				opName = stateName.substring(0, stateName.indexOf(STATE_INDICATOR));
				// e.g. O1
			}
			stateNumber++;

			// Into the real states (leaving name and id)
			if(stateNumber > 1)
			{
				Set<String> allOtherStates = new HashSet<String>();
				// e.g. O1_initO2_compO3_exec in each ArrayList-entry
				allOtherStates = getStates(stateNumber, restrictions);
				allStatesList.add(getStates(stateNumber, restrictions));

				allOtherStates.remove(stateName);

			    otherStatesList.add(allOtherStates);

			}
		}


		final int noOfRestrEntrys = states.size();
		final String rem = "rem";
		final String keep = "keep";
		final ArrayList<HashSet<?>> treated = new ArrayList<HashSet<?>>();
		final ArrayList<String> remove = new ArrayList<String>();
		for(int ii=0; ii<noOfRestrEntrys; ii++)
		{
			remove.add(keep);
		}


		int indF, indS, indT;
		/* For each entry in other-list */
		for(final Iterator<Set<?>> otherIter = otherStatesList.iterator(); otherIter.hasNext(); )
		{
			final HashSet<?> other = (HashSet<?>) otherIter.next();

			// Only check each restriction string once.
			if( !treated.contains(other) )
			{
				final HashSet<String> first = new HashSet<String>();
				final HashSet<String> second = new HashSet<String>();
				final HashSet<String> third = new HashSet<String>();

				/* Build a set containing all other operation�s states */
				for(final Iterator<?> oIter = other.iterator(); oIter.hasNext(); )
				{
					final String n = (String) oIter.next();
					first.add(n);
					second.add(n);
					third.add(n);
				}


				first.add(opName.concat(INITIAL_STATE_POSTFIX));
				second.add(opName.concat(EXECUTION_STATE_POSTFIX));
				third.add(opName.concat(END_STATE_POSTFIX));

				// Check if there exist e.g. O1_init -> string, O1_exec -> string and O1_comp -> string
				indF = allStatesList.indexOf(first);
				indS = allStatesList.indexOf(second);
				indT = allStatesList.indexOf(third);


				/* If there exist
				 * 	e.g.
				 * 		O1_init -> string,
				 * 		O1_exec -> string and O1_comp -> string,
				 * 		simplify to O1_- -> string,
				 * 	where - meand don't care.
				 */
				if(indF > -1 && indS > -1 && indT > -1)
				{

					states.remove(indF+2);
					final String adding = opName.concat(DONT_CARE_STATE_POSTFIX);
					states.add(indF+2, adding);

					remove.remove(indS+2);
					remove.add(indS+2, rem);
					remove.remove(indT+2);
					remove.add(indT+2, rem);

					restrictions.remove(i);
					restrictions.add(i, states);
				}
				treated.add(other);
			}
		}

		// Perform the removing. Replace the restriction list with a new one, where the unnecessary entrys in each operation restriction list have been removed.
		for(int k = remove.size()-1; k>1 ; k--)
		{
			if( remove.get(k) == rem )
			{
				restrictions = (ArrayList<ArrayList<String>>) removeStates(k, restrictions);
			}
		}

		i++;
		simpleRestr.restrictions = (ArrayList<?>) restrictions.clone();
	}

	return simpleRestr;
}


/*****************************************************
*
* findPredecessors
*
* If Oi.comp occurs in all alternative restrictions for Oj, then Oi is a
* predecessor to Oj.
*
******************************************************/
public ArrayList<ArrayList<String>> findPredecessors(final Restriction restr)
{
	final String op = (String) restr.opName;

	final ArrayList<?> theRestr = (ArrayList<?>) restr.restrictions;
	final ArrayList<ArrayList<String>> predSucc = new ArrayList<ArrayList<String>>();

	for(final Iterator<?> restrIter = theRestr.iterator(); restrIter.hasNext(); )
	{
		final ArrayList<?> states = (ArrayList<?>) restrIter.next();
		int i = 0;
		boolean pred = true;
		final String opName = (String) states.get(1);

		final Iterator<?> statesIter = states.iterator();
		while(statesIter.hasNext() && pred)
		{
			final String state = (String) statesIter.next();
			if(i>1)
			{
				if(!state.equals(opName.concat(END_STATE_POSTFIX)))
				{
					pred = false;
				}
			}
			i++;
		}
		if(pred)
		{
			final ArrayList<String> temp = new ArrayList<String>();
			temp.add(0, opName);
			temp.add(1, op);
			predSucc.add(temp);
		}

	}

	return predSucc;
}



/*****************************************************
*
* removeEqualAlternatives
*
*
*
******************************************************/
public ArrayList<Restriction> removeEqualAlternatives(final ArrayList<Restriction> restr)
{
	// For each operation
	for(final Iterator<Restriction> opIter = restr.iterator(); opIter.hasNext(); )
	{
		final Restriction operation = opIter.next();

		// All restrictions for the operation
		ArrayList<?> rList = (ArrayList<?>) operation.restrictions;

		if(rList.size() > 0)
		{
			final ArrayList<?> firstStates = (ArrayList<?>) rList.get(0);
			int noOfStates = firstStates.size();
			int j, i=2;
			while( i < noOfStates-1 )
			{
				final String restrString = getRestrString(rList, i);

				j=i+1;
				while( j < noOfStates )
				{
					final String compareRestr = getRestrString(rList, j);

					if( restrString.equals(compareRestr) )
					{
						rList = removeRestrString(rList, j);
						j--;
						noOfStates--;
					}
					j++;
				}
				i++;
			}//end while
		}
	}
	return restr;
}

/*****************************************************
*
* getRestrString
*
* Get the restriction corresponding to place i
*
******************************************************/
public String getRestrString(final ArrayList<?> l, final int i)
{
	String r = new String();
	for(final Iterator<?> lIter = l.iterator(); lIter.hasNext(); )
	{
		final ArrayList<?> states = (ArrayList<?>) lIter.next();
		final String state = (String) states.get(i);
		r = r.concat(state);
	}
	return r;
}

/*****************************************************
*
* removeRestrString
*
* Remove the restriction corresponding to place i
*
******************************************************/
public ArrayList<?> removeRestrString(final ArrayList<?> l, final int i)
{
	for(final Iterator<?> lIter = l.iterator(); lIter.hasNext(); )
	{
		final ArrayList<?> states = (ArrayList<?>) lIter.next();
		states.remove(i);
	}
	return l;
}

/*****************************************************
*
* replaceState
*
*
*
******************************************************/
@SuppressWarnings("unchecked")
public ArrayList<?> replaceState(final ArrayList<?> l, final String op, final String s, final int i)
{
	final Iterator<?> lIter = l.iterator();
	boolean ready = false;
	while(lIter.hasNext() && !ready )
	{
		final ArrayList<String> states = (ArrayList<String>) lIter.next();
		final String opName = states.get(1);
		if(op.equals(opName))
		{
			states.set(i,s);
			ready = true;
		}
	}
	return l;
}

/*****************************************************
*
* removeCompSequences
*
*
* If Oi -> Oj, Ok[Oi_comp AND Oj_comp] is reduced to Ok[Oj_comp]
*
* Vet inte varf�r jag t�nkte s� h�r f�rst... Ej l�ngre impl. s�. Troligen fick jag
* problem d� jag inte hade psIEC-checken.
* (BUT, remove only if Oi_comp is not a predecessor to Ok, or if Oj is a predecessor.)
*
******************************************************/

public ArrayList<Restriction> removeCompSequences(final ArrayList<Restriction> restr, final ArrayList<ArrayList<String>> ps)
{
	// For each predecessor-successor pair
	for(final Iterator<ArrayList<String>> psIter = ps.iterator(); psIter.hasNext(); )
	{
		final ArrayList<?> predSucc = psIter.next();
		if(predSucc.size() > 0)
		{
			final String pred = (String) predSucc.get(0);
			final String succ = (String) predSucc.get(1);

			for(final Iterator<Restriction> rIter = restr.iterator(); rIter.hasNext(); )
			{
				final Restriction operation = rIter.next();
				ArrayList<?> rList = (ArrayList<?>) operation.restrictions;

				if(rList.size()>0)
				{
					final String op = operation.opName;


					final ArrayList<String> testPair = new ArrayList<String>();
					testPair.add(0,pred);
					testPair.add(1,op);

					final ArrayList<String> testPair2 = new ArrayList<String>();
					testPair2.add(0,succ);
					testPair2.add(1,op);

					final ArrayList<?> firstStates = (ArrayList<?>) rList.get(0);
					final int noOfStates = firstStates.size();
					int i=2;

					while( i < noOfStates )
					{
						final String restrString = getRestrString(rList, i);

						// If the restriction contains both the pred and the succ in comp-state
						if( restrString.indexOf(pred.concat(END_STATE_POSTFIX)) > -1 &&
							restrString.indexOf(succ.concat(END_STATE_POSTFIX)) > -1 )
						{
							rList = replaceState(rList, pred, pred.concat(DONT_CARE_STATE_POSTFIX), i);

						}
						i++;
					}

				}
			}//end for
		}
	}//end for

	return restr;
}

/*****************************************************
*
* removeCompExecSequences
*
*
* If Oi -> Oj, Ok[Oi_comp AND Oj_exec] is reduced to Ok[Oj_exec]
* BUT, remove only if Oi_comp is not a predecessor (Oj cannot be a predecessor).
*
******************************************************/

public ArrayList<Restriction> removeCompExecSequences(final ArrayList<Restriction> restr, final ArrayList<ArrayList<String>> ps)
{
	// For each predecessor-successor pair
	for(final Iterator<ArrayList<String>> psIter = ps.iterator(); psIter.hasNext(); )
	{
		final ArrayList<?> predSucc = psIter.next();

		final String pred = (String) predSucc.get(0);
		final String succ = (String) predSucc.get(1);

		for(final Iterator<Restriction> rIter = restr.iterator(); rIter.hasNext(); )
		{
			final Restriction operation = rIter.next();
			ArrayList<?> rList = (ArrayList<?>) operation.restrictions;
			final String op = operation.opName;

			final ArrayList<String> testPair = new ArrayList<String>();
			testPair.add(0,pred);
			testPair.add(1,op);

			// Pred is not a predecessor to op, allowed to be removed
			if(ps.indexOf(testPair) == -1)
			{

				if(rList.size() > 0)
				{
					final ArrayList<?> firstStates = (ArrayList<?>) rList.get(0);
					final int noOfStates = firstStates.size();
					int i=2;

					while( i < noOfStates )
					{

						final String restrString = getRestrString(rList, i);

						// If the restriction contains both the pred and the succ in comp-state
						if( restrString.indexOf(pred.concat(END_STATE_POSTFIX)) > -1 &&
						    restrString.indexOf(succ.concat(EXECUTION_STATE_POSTFIX)) > -1 )
						{
							rList = replaceState(rList, pred, pred.concat(DONT_CARE_STATE_POSTFIX), i);
						}
						i++;
					}
				}
			}
		}//end for
	}

	return restr;
}

/*****************************************************
*
* removeInitInitSequences
*
*
* If Oi -> Oj, Ok[O_init AND Oj_init] is reduced to Ok[Oi_init]
*
******************************************************/

public ArrayList<Restriction> removeInitInitSequences(final ArrayList<Restriction> restr, final ArrayList<ArrayList<String>> ps)
{
	// For each predecessor-successor pair
	for(final Iterator<ArrayList<String>> psIter = ps.iterator(); psIter.hasNext(); )
	{
		final ArrayList<?> predSucc = psIter.next();

		if(predSucc.size() > 0)
		{
			final String pred = (String) predSucc.get(0);
			final String succ = (String) predSucc.get(1);


			for(final Iterator<Restriction> rIter = restr.iterator(); rIter.hasNext(); )
			{
				final Restriction operation = rIter.next();
				ArrayList<?> rList = (ArrayList<?>) operation.restrictions;
				if(rList.size() > 0)
				{
					final ArrayList<?> firstStates = (ArrayList<?>) rList.get(0);
					final int noOfStates = firstStates.size();
					int i=2;

					while( i < noOfStates )
					{

						final String restrString = getRestrString(rList, i);

						// If the restriction contains both the pred and the succ in comp-state
						if( restrString.indexOf(pred.concat(INITIAL_STATE_POSTFIX)) > -1 &&
							restrString.indexOf(succ.concat(INITIAL_STATE_POSTFIX)) > -1 )
						{

							rList = replaceState(rList, succ, succ.concat(DONT_CARE_STATE_POSTFIX), i);
						}
						i++;
					}
				}
			}//end for
		}
	}//end for

	return restr;
}


/*****************************************************
*
* removeExecInitSequences
*
*
* If Oi -> Oj, Ok[O_exec AND Oj_init] is reduced to Ok[Oi_exec]
*
******************************************************/

public ArrayList<Restriction> removeExecInitSequences(final ArrayList<Restriction> restr, final ArrayList<ArrayList<String>> ps)
{
	// For each predecessor-successor pair
	for(final Iterator<ArrayList<String>> psIter = ps.iterator(); psIter.hasNext(); )
	{
		final ArrayList<?> predSucc = psIter.next();

		if(predSucc.size() > 0)
		{
			final String pred = (String) predSucc.get(0);
			final String succ = (String) predSucc.get(1);


			for(final Iterator<Restriction> rIter = restr.iterator(); rIter.hasNext(); )
			{
				final Restriction operation = rIter.next();
				ArrayList<?> rList = (ArrayList<?>) operation.restrictions;

				if(rList.size() > 0)
				{
					final ArrayList<?> firstStates = (ArrayList<?>) rList.get(0);
					final int noOfStates = firstStates.size();
					int i=2;

					while( i < noOfStates )
					{

						final String restrString = getRestrString(rList, i);
						// If the restriction contains both the pred and the succ in comp-state
						if( restrString.indexOf(pred.concat(EXECUTION_STATE_POSTFIX)) > -1 &&
							restrString.indexOf(succ.concat(INITIAL_STATE_POSTFIX)) > -1)
						{
							rList = replaceState(rList, succ, succ.concat(DONT_CARE_STATE_POSTFIX), i);
						}
						i++;
					}//end while
				}
			}//end for
		}
	}//end for
	return restr;
}



/*****************************************************
*
* removeSuccessors
*
* If Oi.comp occurs in all alternative restrictions for Oj, then Oi is a
* predecessor to Oj. Replace all entries Oj_init in the restrictions for
* Oi with Oj_-.
*
******************************************************/
public ArrayList<Restriction> removeSuccessors(final ArrayList<Restriction> restr , final ArrayList<ArrayList<String>> ps)
{

	// For each operation
	for(final Iterator<Restriction> opIter = restr.iterator(); opIter.hasNext(); )
	{
		final Restriction operation = opIter.next();

		final String opName = operation.opName;
		final ArrayList<?> rList = (ArrayList<?>) operation.restrictions;

		final ArrayList<ArrayList<?>> remove = new ArrayList<ArrayList<?>>();

		// For each restriction of the operation
		for(final Iterator<?> rIter = rList.iterator(); rIter.hasNext(); )
		{
			final ArrayList<?> states = (ArrayList<?>) rIter.next();

			final String rOpName = (String) states.get(1);
			final ArrayList<String> temp = new ArrayList<String>();
			temp.add(0, opName);
			temp.add(1, rOpName);

			// If the restriction operation is a successor to the studied operation
			if(ps.contains(temp))
			{
				// Mark the entry for removal
				remove.add(states);
			}
		}

		for(final Iterator<ArrayList<?>> remIter = remove.iterator(); remIter.hasNext(); )
		{
			final ArrayList<?> remStates = remIter.next();
			rList.remove(remStates);
		}
	}
	return restr;
}


/*****************************************************
*
* removeSequences
*
* Save only the "last" predecessor
* i.e. O2[O1_comp AND O3_comp] = O2[O3_comp] if O1->O3
*
******************************************************/

public ArrayList<?> removeSequences(final ArrayList<?> restr, final ArrayList<ArrayList<String>> ps)
{
	// For each operation
	for(final Iterator<?> restrIter = restr.iterator(); restrIter.hasNext(); )
	{
		final Restriction operation = (Restriction) restrIter.next();
		final String opName = operation.opName;

		// Get all predecessors
		final HashSet<?> preds = getPredecessors(opName, ps);

		// For all predecessors
		for(final Iterator<?> pIter = preds.iterator(); pIter.hasNext(); )
		{
			final String predName = (String) pIter.next();

			// Get all their predecessors
			final HashSet<?> pPreds = getPredecessors(predName, ps);
			final ArrayList<?> rList = (ArrayList<?>) operation.restrictions;

			// Remove the pPred-entries from the restriction list for opName
			int i=0;
			int noOfOps = rList.size();
			while( i<noOfOps )
			{
				final ArrayList<?> states = (ArrayList<?>) rList.get(i);
				if(pPreds.contains(states.get(1)))
				{
					rList.remove(i);
					i--;
					noOfOps--;
				}
				i++;
			}
		}
	}
	return restr;
}

/*****************************************************
*
* getPredecessors
*
******************************************************/
public HashSet<?> getPredecessors(final String opName, final ArrayList<ArrayList<String>> ps)
{
	final HashSet<String> predecessors = new HashSet<String>();
	for(final Iterator<ArrayList<String>> psIter = ps.iterator(); psIter.hasNext(); )
	{
		final ArrayList<String> pair = psIter.next();
		if(pair.get(1).equals(opName))
		{
			predecessors.add(pair.get(0));
		}
	}
	return predecessors;
}

/*****************************************************
*
* getSuccessors
*
******************************************************/
public HashSet<String> getSuccessors(final String opName, final ArrayList<ArrayList<String>> ps)
{
	final HashSet<String> successors = new HashSet<String>();
	for(final Iterator<ArrayList<String>> psIter = ps.iterator(); psIter.hasNext(); )
	{
		final ArrayList<String> pair = psIter.next();
		if(pair.get(0).equals(opName))
		{
			successors.add(pair.get(1));
		}
	}
	return successors;
}

/*****************************************************
*
* removeDontCareOperations
*
******************************************************/
public ArrayList<Restriction> removeDontCareOperations(final ArrayList<Restriction> restr)
{

	for(final Iterator<Restriction> rIter = restr.iterator(); rIter.hasNext(); )
	{
		final Restriction operation = rIter.next();
		final ArrayList<?> rList = (ArrayList<?>) operation.restrictions;

		final ArrayList<ArrayList<?>> remove = new ArrayList<ArrayList<?>>();

		for(final Iterator<?> rListIter = rList.iterator(); rListIter.hasNext(); )
		{
			final ArrayList<?> states = (ArrayList<?>) rListIter.next();
			final String opName = (String) states.get(1);

			// Check if there are only O_-
			final Iterator<?> sIter = states.iterator();

			boolean allDontCare = true;
			int i = 0;
			while( sIter.hasNext() && allDontCare )
			{

				final String state = (String) sIter.next();
				if(!state.equals(opName.concat(DONT_CARE_STATE_POSTFIX)) && i>1)
				{
					allDontCare = false;
				}

				i++;
			}

			if(allDontCare)
			{
				// Mark the entry for removal
				remove.add(states);
			}
		}

		for(final Iterator<ArrayList<?>> remIter = remove.iterator(); remIter.hasNext(); )
		{
			final ArrayList<?> remStates = remIter.next();
			rList.remove(remStates);
		}
	}
	return restr;
}


/*****************************************************
*
* removeDontCareStates
*
******************************************************/
public ArrayList<Restriction> removeDontCareStates(final ArrayList<Restriction> restr)
{

	for(final Iterator<Restriction> rIter = restr.iterator(); rIter.hasNext(); )
	{
		final Restriction operation = rIter.next();
		ArrayList<?> rList = (ArrayList<?>) operation.restrictions;
		String allOpStates = new String();
		String allOpStates2 = new String();
		String allButOp = new String();
		String allButOp2 = new String();

		// For each of the restriction entries
		for(final Iterator<?> rListIter = rList.iterator(); rListIter.hasNext(); )
		{
			final ArrayList<?> states = (ArrayList<?>) rListIter.next();
			int noOfStates = states.size();
			final String opName = (String) states.get(1);

			int j=0;
			while( j < noOfStates )
			{
				final String state = (String) states.get(j);
				if(state.equals(opName.concat(DONT_CARE_STATE_POSTFIX)) )
				{
					allOpStates = getRestrString(rList, j);
					allButOp = allOpStates.replaceAll(state, "");

					int jj=0;
					while(jj < noOfStates)
					{
						if(jj != j)
						{
							final String state2 = (String) states.get(jj);

							allOpStates2 = getRestrString(rList, jj);
							allButOp2 = allOpStates2.replaceAll(state2, "");
						}
						jj++;
					}

					if(allButOp.equals(allButOp2))
					{

						// Remove state
						rList = removeRestrString(rList, j);
						noOfStates--;
						j--;
					}
				}
				j++;
			}
		}
	}
	return restr;
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
public HashSet<String> getStates(final int index, final ArrayList<?> restrictions)
{
	if(index<2)
	{
		System.err.println("getStates: Index must be at least 2");
		return null;
	}

	final HashSet<String> c = new HashSet<String>();

	for(final Iterator<?> rIter = restrictions.iterator(); rIter.hasNext(); )
	{
		final ArrayList<?> oneOperation = (ArrayList<?>) rIter.next();
		final String temp = (String) oneOperation.get(index);
		c.add(temp);
	}

	return c;
}

/*****************************************************
*
* removeStates
*
*
******************************************************/
public ArrayList<?> removeStates(final int index, final ArrayList<?> restrictionList)
{
	if(index<2)
	{
		System.err.println("removeStates: Index must be at least 2");
		return null;
	}

	final ArrayList<ArrayList<?>> newRestrList = new ArrayList<ArrayList<?>>();
	int i = 0;
	for(final Iterator<?> rIter = restrictionList.iterator(); rIter.hasNext(); )
	{
		final ArrayList<?> oneOperation = (ArrayList<?>) rIter.next();
		oneOperation.remove(index);
		newRestrList.add(i, oneOperation);
		i++;
	}
	return newRestrList;
}

/*****************************************************
*
* copyRestrictions
*
*
*
******************************************************/
public ArrayList<Restriction> copyRestrictions(final ArrayList<Restriction> r)
{
	final ArrayList<Restriction> newR = new ArrayList<Restriction>();
	for(final Iterator<Restriction> rIter = r.iterator(); rIter.hasNext(); )
	{
		final Restriction op = rIter.next();
		final String opName = op.opName;
		final String opId = op.opId;


		final ArrayList<?> rList = (ArrayList<?>) op.restrictions;

		final ArrayList<ArrayList<String>> newRList = new ArrayList<ArrayList<String>>();
		for(final Iterator<?> rListIter = rList.iterator(); rListIter.hasNext(); )
		{
			final ArrayList<?> states = (ArrayList<?>) rListIter.next();
			final ArrayList<String> newStates = new ArrayList<String>();
			for(final Iterator<?> sIter = states.iterator(); sIter.hasNext(); )
			{
				final String state = (String) sIter.next();
				final String newState = new String(state);
				newStates.add(newState);
			}
			newRList.add(newStates);
		}
		final Restriction newOp = new Restriction(opId, opName, newRList);
		newR.add(newOp);
	}

	return newR;
}

/*****************************************************
*
* compareRestrictions
*
*
*
******************************************************/
public int compareRestrictions(final ArrayList<Restriction> r1, final ArrayList<Restriction> r2)
{
	if(!(r1.size() == r2.size()))
	{
		//System.out.println("Not equal length 1");
		return -1;
	}
	else
	{
		for(int i = 0; i<r1.size(); i++)
		{
			final Restriction op1 = r1.get(i);
			final Restriction op2 = r2.get(i);

			final String opName1 = op1.opName;
			final String opName2 = op2.opName;

			final String opId1 = op1.opId;
			final String opId2 = op2.opId;

			if(!opName1.equals(opName2))
			{
				//System.out.println("Not equal names");
				return -1;
			}
			if(!opId1.equals(opId2))
			{
				//System.out.println("Not equal ids");
				return -1;
			}

			final ArrayList<?> rList1 = (ArrayList<?>) op1.restrictions;
			final ArrayList<?> rList2 = (ArrayList<?>) op2.restrictions;
			if(!(rList1.size() == rList2.size()))
			{
				//System.out.println("Not equal sizes");
				return -1;
			}
			else
			{
				for(int j = 0; j<rList1.size(); j++)
				{
					final ArrayList<?> states1 = (ArrayList<?>) rList1.get(j);
					final ArrayList<?> states2 = (ArrayList<?>) rList2.get(j);

					if(!states1.equals(states2))
					{
						//System.out.println("Not equal states");
						return -1;
					}
				}
			}
		}
	}
	return 1;

}

/*****************************************************
*
* getOpMachMatching
*
* Builds two lists, machs contains the machine names, ops
* contains their operations.
*
******************************************************/
public ArrayList<ArrayList<String>> getOpMachMatching(final ArrayList<Document> ROPList)
{
	int i = 0;
	final ArrayList<ArrayList<String>> opsMachs = new ArrayList<ArrayList<String>>();
	for( final Iterator<Document> ROPiter = ROPList.iterator(); ROPiter.hasNext(); )
	{
		final ArrayList<String> temp = new ArrayList<String>();
		final Document rop = ROPiter.next();
		final Element root = rop.getRootElement();
		final Element mach = root.getChild("Machine");
		final String machName = mach.getText();
		temp.add(machName);

		final Element relation = root.getChild("Relation");
		temp.addAll( getOperationsFromActivities( getActivitiesFromRelation( relation ) ) );

		opsMachs.add(i, temp);
		i++;
	}
	return opsMachs;
}

/**
 * Returns all activities in a relation.
 * @param relation
 * @return
 */
@SuppressWarnings("unchecked")
public List<Element> getActivitiesFromRelation(final Element relation){

	final ArrayList<Element> actList = new ArrayList<Element>();

	//Sanity check
	if (null == relation){
		return actList;
	}

	//Add all Operations from activities
	List<Element> tmpElementList = relation.getChildren("Activity");
	actList.addAll( tmpElementList );

	//Add all Operations from Relations recursivly
	tmpElementList = relation.getChildren("Relation");
	for ( final Iterator<Element> relIter = tmpElementList.iterator(); relIter.hasNext(); ){
		actList.addAll( getActivitiesFromRelation( relIter.next()) );
	}

	return actList;
}

/**
 *
 * Create a list of operation names from a list of activities.
 *
 * @param activities - list of activities
 * @return A string list with operation names from the activity list
 */
public List<String> getOperationsFromActivities(final List<Element> activities){

	final ArrayList<String> actList = new ArrayList<String>();

	//Sanity check
	if (null == activities || 0 == activities.size()){
		return actList;
	}


	for( final Iterator<Element> actIter = activities.iterator(); actIter.hasNext(); )
	{
		final Element act = actIter.next();
		final Element operation = act.getChild("Operation");
		final String opName = operation.getText();
		actList.add(opName);
	}

	return actList;
}

/*****************************************************
*
* getRestriction
*
*
*
******************************************************/
public ArrayList<?> getRestriction(final String opName, final ArrayList<Restriction> restr, String found)
{
	for( final Iterator<Restriction> rIter = restr.iterator(); rIter.hasNext(); )
	{
		final Restriction r = rIter.next();
		if(opName.equals(r.opName))
		{
			final ArrayList<?> rr = (ArrayList<?>) r.restrictions;
			found = "nf";
			return rr;
		}
	}
	System.out.println("Cannot find a restriction for " + opName);
	found = "f";
	return new ArrayList<Object>();
}


/*****************************************************
*
* getMach
*
*
******************************************************/
public String getMachine(final String opName, final ArrayList<ArrayList<String>> opsMachs)
{
	int i = 0;
	for( final Iterator<ArrayList<String>> opIter = opsMachs.iterator(); opIter.hasNext(); )
	{
		final ArrayList<?> omList = opIter.next();
		if(omList.contains(opName))
		{
			final String mach = (String) omList.get(0);
			return mach;
		}
		i++;
	}
	System.out.println("Cannot find a machine that matches " + opName);
	return new String();
}

/*****************************************************
*
* buildCOPDocs
*
* Build documents containing the COPs.
*
******************************************************/

@SuppressWarnings("unchecked")
public void buildCOPDocs(final ArrayList<Restriction> restr, final ArrayList<Document> ROPs)
{
	final ArrayList<ArrayList<String>> opsMachs = getOpMachMatching(ROPs);
	for( final Iterator<Document> COPiter = COPList.iterator(); COPiter.hasNext(); )
	{

		final Document rop = COPiter.next();
		final Element root = rop.getRootElement();
		final Element rel = root.getChild("Relation");
		final List<?> activities = getActivitiesFromRelation( rel );

		for( final Iterator<?> actIter = activities.iterator(); actIter.hasNext(); )
		{
			final Element act = (Element) actIter.next();
			final Element operation = act.getChild("Operation");
			final String opName = operation.getText();
			act.removeChildren("Precondition");
			act.removeChildren("Operation");

			final String found = new String();
			final ArrayList<?> rList = getRestriction(opName, restr, found);

			if(found.equals("nf"))
			{
				act.removeChild("Operation");
			}
			// If a restriction exists
			else if(rList.size() > 0)
			{
				final ArrayList<ArrayList<String>> preConds = new ArrayList<ArrayList<String>>();

				// Transform restriction into a number of preconditions

				// Creates the "root"-precondition
				final Element precondOR = new Element("Precondition");
				precondOR.setAttribute("Operator", "or");

				// A restriction contains one row for each operation, i.e. it is actually the columns that consitute a precondition. Build ArrayList "preConds" that contain one entry for each column.
				int rowNo = 0;
				for( final Iterator<?> rIter = rList.iterator(); rIter.hasNext(); )
				{
					// All states of the operation
					final ArrayList<?> states = (ArrayList<?>) rIter.next();

					int i = 0;
					// Iterate within each "entry" (restriction) consisting of 1, O1, O1_-, O1_comp etc.
					for( final Iterator<?> sIter = states.iterator(); sIter.hasNext(); )
					{
						final String state = (String) sIter.next();

						if ( i > 1 )
						{
							ArrayList<String> theEntry = new ArrayList<String>();

							if(rowNo==0) // preConds is empty
							{
								theEntry.add(state);
							}
							else if(rowNo==1)
							{

								final ArrayList<String> oldState = preConds.get(i-2);
								oldState.add(state);
								theEntry = (ArrayList<String>) oldState.clone();
							}
							else if(rowNo>1)
							{
								theEntry = preConds.get(i-2);
								theEntry.add(state);
							}

							if ( preConds.size() > i - 2 )
							{
								preConds.set(i-2, theEntry);
							}
							else
							{
								preConds.add(i-2, theEntry);
							}
						}
						i++;
					}
					rowNo++;
				}


				// Create the COP xml-document:

				// For all alternative preconditions (e.g. (O1_comp AND O2_init) OR (O1_init))
				for( final Iterator<ArrayList<String>> pIter = preConds.iterator(); pIter.hasNext(); )
				{
					final ArrayList<?> states = pIter.next();
					final Element precondAND = new Element("Precondition");
					precondAND.setAttribute("Operator", "and");

					// For all AND-conditions (e.g. O1_comp AND O2_init)
					for( final Iterator<?> sIter = states.iterator(); sIter.hasNext(); )
					{
						final String state = (String) sIter.next();
						if( state.endsWith(INITIAL_STATE_POSTFIX)   ||
						    state.endsWith(EXECUTION_STATE_POSTFIX) ||
						    state.endsWith(END_STATE_POSTFIX) )
						{
							final Element predecessor = new Element("Predecessor");
							final int index = state.indexOf(STATE_INDICATOR);
							final String predName = state.substring(0,index);
							final String machName = getMachine(predName, opsMachs);
							final String stateName = state.substring(index+1);

							final Element machine = new Element("Machine");
							machine.setText(machName);
							final Element predOperation = new Element("Operation");
							predOperation.setText(predName);
							final Element predState = new Element("State");
							predState.setText(stateName);

							predecessor.addContent(machine);
							predecessor.addContent(predOperation);
							predecessor.addContent(predState);
							precondAND.addContent(predecessor);

						}
					}

					precondOR.addContent(precondAND);

				}

				act.addContent(precondOR);
			}

			final Element theOperation = new Element("Operation");
			theOperation.setText(opName);
			act.addContent(1,theOperation);
		}
	}
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

/*****************************************************/

	public void printStringList(final List<?> v)
	{
		System.out.println();
		int i=1;
		for(final Iterator<?> e = v.iterator(); e.hasNext(); )
		{
			//System.out.println("Rad " + i);
			System.out.println(e.next());
			i++;
		}
		System.out.println();
	}

/*****************************************************/

	public void printStringSet(final Set<?> v)
	{
		for(final Iterator<?> ee = v.iterator(); ee.hasNext(); )
		{
			System.out.println(ee.next());
		}
		System.out.println();
	}
/*****************************************************/

	public void printRestrictionList(final ArrayList<?> r)
	{
		int k=1;
		for(final Iterator<?> f = r.iterator(); f.hasNext(); )
		{
			final Restriction ff = (Restriction) f.next();
			System.out.println("******** Restriction " + k + " ********");
			k++;
			System.out.println(ff.opName);
			printStringList(ff.restrictions);
			System.out.println("*******************************");
		}
		System.out.println();
	}

/*****************************************************/

	public void printRestrictionSubList(final ArrayList<?> r, final int a, final int b)
	{

		int k=1;
		int aa=0, bb=0;
		final Iterator<?> f = r.iterator();
		while( f.hasNext() && bb<b)
		{
			final Restriction ff = (Restriction) f.next();
			if(aa>=a && bb<b){

				System.out.println("******** Restriction " + k + " ********");
				k++;
				System.out.println(ff.opName);
				printStringList(ff.restrictions);
				System.out.println("*******************************");
			}
			aa++;
			bb++;
		}
		System.out.println();
	}

/*****************************************************/


}