
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.io.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.*;
import org.supremica.automata.execution.*;

public class ProjectToSP
	implements AutomataSerializer
{
	private Project project;
	private boolean canonical = false;
	private boolean includeCost = false;
	private boolean debugMode = false;
	private boolean includeLayout = true;
	private boolean includeExecution = true;
	private final static int majorFileVersion = 0;
	private final static int minorFileVersion = 10;

	// mappings between state/event and id
	private Map stateIdMap = new HashMap();
	private Map eventIdMap = new HashMap();

	public ProjectToSP(Project project)
	{
		this.project = project;
	}

	public void setIncludeLayout(boolean includeLayout)
	{
		this.includeLayout = includeLayout;
	}

	public void setIncludeExecution(boolean includeExecution)
	{
		this.includeExecution = includeExecution;
	}

	public void serialize(PrintWriter pw)
	{
		pw.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		pw.print("<SupremicaProject");

		if (project.getName() != null)
		{
			pw.print(" name=\"" + EncodingHelper.normalize(project.getName()) + "\" ");
		}

		pw.print(" major=\"" + majorFileVersion + "\" ");
		pw.print(" minor=\"" + minorFileVersion + "\" ");

		if (SupremicaProperties.generalUseSecurity())
		{
			pw.print(" owner=\"" + project.getOwner() + "\"");
			pw.print(" hash=\"" + project.getHash() + "\"");
		}

		pw.println(">");

		for (Iterator projectIt = project.iterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<Automaton name=\"" + aut.getName() + "\" type=\"" + aut.getType().toString() + "\">");

			// Print all events
			pw.println("\t<Events>");

			int eventId = 0;	// need to make up ids

			for (Iterator eventIt = aut.eventIterator(); eventIt.hasNext(); )
			{
				LabeledEvent event = (LabeledEvent) eventIt.next();
				eventIdMap.put(event, new Integer(eventId));
				pw.print("\t\t<Event id=\"" + eventId + "\" label=\"" + EncodingHelper.normalize(event.getLabel()) + "\"");
				eventId++;
				//--
				// pw.print("\t\t<Event id=\"" + EncodingHelper.normalize(event.getId()) + "\" label=\"" + EncodingHelper.normalize(event.getLabel()) + "\"");
				//--
				if (!event.isControllable())
				{
					pw.print(" controllable=\"false\"");
				}

				if (!event.isPrioritized())
				{
					pw.print(" prioritized=\"false\"");
				}

				if (event.isImmediate())
				{
					pw.print(" immediate=\"true\"");
				}

				if (debugMode)
				{
					pw.print(" synchIndex=" + event.getSynchIndex());
				}

				pw.println("/>");
			}

			pw.println("\t</Events>");

			// Print all states
			pw.println("\t<States>");

			int stateId = 0; // we need to make up ids

			for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
			{
				State state = (State) stateIt.next();
				stateIdMap.put(state, new Integer(stateId)); // The arc must be able to find it fast
				pw.print("\t\t<State id=\"" + stateId + "\""); // no longer need to normalize
				stateId++;
				//--
				// pw.print("\t\t<State id=\"" + EncodingHelper.normalize(state.getId()) + "\"");
				//--
				pw.print(" name=\"" + EncodingHelper.normalize(state.getName()) + "\""); // always print the name
				//--
				// if (!state.getId().equals(state.getName()))
				// {
				// 	pw.print(" name=\"" + EncodingHelper.normalize(state.getName()) + "\"");
				// }
				//--
				if (state.isInitial())
				{
					pw.print(" initial=\"true\"");
				}

				if (state.isAccepting())
				{
					pw.print(" accepting=\"true\"");
				}

				if (state.isForbidden())
				{
					pw.print(" forbidden=\"true\"");
				}

				if (includeCost)
				{
					int value = state.getCost();

					if (value != State.UNDEF_COST)
					{
						pw.print(" cost=\"" + value + "\"");
					}
				}

				if (debugMode)
				{
					pw.print(" synchIndex=" + state.getIndex());
				}

				pw.println("/>");
			}

			pw.println("\t</States>");

			// Print all transitions
			pw.println("\t<Transitions>");

			for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
			{
				State sourceState = (State) stateIt.next();
				Object sourceId = stateIdMap.get(sourceState);

				for (Iterator outgoingArcsIt = sourceState.outgoingArcsIterator(); outgoingArcsIt.hasNext(); )
				{
					Arc arc = (Arc) outgoingArcsIt.next();
					State destState = arc.getToState();
					Object destId = stateIdMap.get(destState);
					LabeledEvent event = arc.getEvent();
					Object eventID = eventIdMap.get(event);
					pw.print("\t\t<Transition source=\"" + sourceId);
					pw.print("\" dest=\"" + destId);
					pw.println("\" event=\"" + eventID + "\"/>");
					//--
					// pw.print("\t\t<Transition source=\"" + EncodingHelper.normalize(sourceState.getId()));
					// pw.print("\" dest=\"" + EncodingHelper.normalize(destState.getId()));
					// pw.println("\" event=\"" + EncodingHelper.normalize(arc.getEventId()) + "\"/>");
					//--
				}
			}

			pw.println("\t</Transitions>");

			// Print layout
			if (includeLayout && aut.hasLayout())
			{
				pw.println("\t<Layout>");

				// Print State Layout
				pw.println("\t\t<StatesLayout>");

				for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
				{
					State state = (State) stateIt.next();

					// pw.print("\t\t\t<StateLayout id=\"" + EncodingHelper.normalize(state.getId()) + "\"");
					pw.print("\t\t\t<StateLayout id=\"" + stateIdMap.get(state) + "\"");
					pw.print(" x=\"" + state.getX() + "\"");
					pw.print(" y=\"" + state.getY() + "\"");
					pw.println("/>");
				}

				pw.println("\t\t</StatesLayout>");

				// Print Transition Layout
				pw.println("\t\t<TransitionsLayout>");

				for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
				{
					State sourceState = (State) stateIt.next();

					/*
	for (Iterator arcSets = sourceState.outgoingArcSetIterator(); arcSets.hasNext(); )
	{
					ArcSet currArcSet = (ArcSet) arcSets.next();
					State fromState = currArcSet.getFromState();
					State toState = currArcSet.getToState();
					pw.println("\t\t\t<ArcSet from=\"" + fromState.getId() + "\" to=\"" + toState.getId() + "\">");

					for (Iterator arcIt = currArcSet.iterator(); arcIt.hasNext(); )
					{
									Arc currArc = (Arc) arcIt.next();
									LabeledEvent thisEvent = theAlphabet.getEventWithId(currArc.getEventId());

									pw.println("\t\t\t\t" + "<Event>" + thisEvent.getLabel() < "</Event>");
					}

					pw.println("\t\t\t</ArcSet>");
	}
					*/
					pw.println("\t\t</TransitionsLayout>");
				}

				pw.println("\t</Layout>");
			}

			pw.println("</Automaton>");
		}

		if (includeExecution)
		{
			pw.println("<Execution>");
			pw.println("\t<Actions>");

			Actions theActions = project.getActions();

			if (theActions != null)
			{
				for (Iterator actionIt = theActions.iterator(); actionIt.hasNext(); )
				{
					Action currAction = (Action) actionIt.next();

					pw.println("\t\t<Action label=\"" + EncodingHelper.normalize(currAction.getLabel()) + "\">");

					for (Iterator cmdIt = currAction.commandIterator(); cmdIt.hasNext(); )
					{
						String currCommand = (String) cmdIt.next();

						pw.println("\t\t\t<Command command=\"" + EncodingHelper.normalize(currCommand) + "\"/>");
					}

					pw.println("\t\t</Action>");
				}
			}

			pw.println("\t</Actions>");
			pw.println("\t<Controls>");

			Controls theControls = project.getControls();

			if (theControls != null)
			{
				for (Iterator controlIt = theControls.iterator(); controlIt.hasNext(); )
				{
					Control currControl = (Control) controlIt.next();

					pw.println("\t\t<Control label=\"" + EncodingHelper.normalize(currControl.getLabel()) + "\">");

					for (Iterator condIt = currControl.conditionIterator(); condIt.hasNext(); )
					{
						String currCondition = (String) condIt.next();

						pw.println("\t\t\t<Condition condition=\"" + EncodingHelper.normalize(currCondition) + "\"/>");
					}
					/*
					if (currControl.getCondition() != null && !currControl.getCondition().equals(""))
					{
						pw.println("\t\t\t<Condition condition=\"" + EncodingHelper.normalize(currControl.getCondition()) + "\"/>");
					}
					*/
					pw.println("\t\t</Control>");
				}
			}

			pw.println("\t</Controls>");

			if (project.hasAnimation())
			{
				pw.println("\t<Animation path=\"" + EncodingHelper.normalize(project.getAnimationURL().toString()) + "\"/>");
			}

			pw.println("</Execution>");
		}

		pw.println("</SupremicaProject>");
		pw.flush();
	}

	public void serialize(File theFile)
		throws IOException
	{
		serialize(theFile.getAbsolutePath());
	}

	public void serialize(String fileName)
		throws IOException
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}

	void printIntArray(PrintWriter pw, int[] theArray)
	{
		for (int i = 0; i < theArray.length; i++)
		{
			if (i == 0)
			{
				pw.print(theArray[i]);
			}
			else
			{
				pw.print(" " + theArray[i]);
			}
		}
	}
}
