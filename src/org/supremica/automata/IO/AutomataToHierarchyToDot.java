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
package org.supremica.automata.IO;

import java.io.*;
import java.util.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.ArcSet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class AutomataToHierarchyToDot
	implements AutomataSerializer
{
	private Automata theAutomata;
	private boolean leftToRight = false;
	private boolean withLabel = true;
	private boolean withCircles = false;
	private boolean useColors = false;

	public AutomataToHierarchyToDot(Automata aut)
	{
		this.theAutomata = aut;
	}

	public boolean isLeftToRight()
	{
		return leftToRight;
	}

	public void setLeftToRight(boolean leftToRight)
	{
		this.leftToRight = leftToRight;
	}

	public void setWithLabels(boolean withLabel)
	{
		this.withLabel = withLabel;
	}

	public void setWithCircles(boolean withCircles)
	{
		this.withCircles = withCircles;
	}

	public void setUseColors(boolean useColors)
	{
		this.useColors = useColors;
	}

	private String getColor(Automaton aut)
	{
		if (!useColors)
		{
			return "";
		}

		if (aut.isInterface())
		{
			return ", color = yellow";
		}

		if (aut.isUndefined())
		{
			return ", color = pink";
		}

		if (aut.isPlant())
		{
			return ", color = red";
		}

		if (aut.isSupervisor() || aut.isSpecification())
		{
			return ", color = green";
		}

		// What on G*d's green earth was that?
		return ", color = white";
	}

	private String getShape(Automaton aut)
	{
		if (withCircles)
		{
			return "";
		}

		if (aut.isInterface())
		{
			return ", shape = diamond";
		}

		if (aut.isPlant())
		{
			return ", shape = box";
		}

		if (aut.isSupervisor() || aut.isSpecification())
		{
			return ", shape = ellipse";
		}

		if (aut.isUndefined())
		{
			return ", shape = egg";
		}

		// What the f**k was that? 
		return "";
	}

	public void serialize(PrintWriter pw)
		throws Exception
	{
		pw.println("graph hierarchy {");
		// pw.println("\tcenter = true;");

		// Left to right or top to bottom?
		if (leftToRight)
		{
			pw.println("\trankdir = LR;");
		}

		// Circles?
		if (withCircles)
		{
			pw.println("\tnode [shape = circle];");
		}
		else
		{
			//pw.println("\tnode [shape = plaintext];");
			//pw.println("\tnode [shape = ellipse];");
		}

		// Filled?
		if (useColors)
		{
			pw.println("\tnode [style = filled];");
		}
		
		// The automata are nodes in the graph		
		//for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		for (int i=0; i<theAutomata.size(); i++)
		{
			Automaton currAutomaton = theAutomata.getAutomatonAt(i);						
			pw.print("\t\"" + currAutomaton.getName() + "\" [label = \"");			
			if (withLabel)
			{
				pw.print(EncodingHelper.normalize(currAutomaton.getName()));
			}			
			pw.println("\"" + getColor(currAutomaton) + getShape(currAutomaton) + "]; ");

			// The arcs in the graph represent common events in the respective alphabets
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			//for (Iterator otherIt = theAutomata.iterator(); otherIt.hasNext(); )
			for (int j=i+1; j<theAutomata.size(); j++)
			{
				Automaton otherAutomaton = theAutomata.getAutomatonAt(j);
				Alphabet otherAlphabet = otherAutomaton.getAlphabet();
				
				int weight = currAlphabet.nbrOfCommonEvents(otherAlphabet);
				if (weight > 0)
				{
					//pw.print("\t\"" + currAutomaton.getName() + "\" -- \"" + otherAutomaton.getName() + "\";");
					pw.print("\t\"" + currAutomaton.getName() + "\" -- \"" + otherAutomaton.getName() + "\" ");
					if (weight == 1)
						pw.print("[style = dashed, ");
					if (weight > 1)
						pw.print("[style = solid, ");
					pw.println("label = " + weight + "];");
				}
			}
		}

		pw.println("}");
		pw.flush();
		pw.close();

		/* // The way it's done in AutomatonViewer
		Vector initialStates = new Vector();
		final String initPrefix = "__init_";

		pw.println("digraph state_automaton {");
		pw.println("\tcenter = true;");


		if (!aut.hasInitialState())
		{
			pw.println("\t noState [shape = plaintext, label = \"No initial state\" ]");
			pw.println("}");
			pw.flush();
			pw.close();

			return;
		}

		for (Iterator states = aut.stateIterator(); states.hasNext(); )
		{
			State state = (State) states.next();

			if (state.isInitial())
			{
				initialStates.addElement(state);
				pw.println("\tnode [shape = plaintext] \"" + initPrefix + state.getId() + "\";");
			}

			if (state.isAccepting() &&!state.isForbidden())
			{
				if (withCircles)
				{
					pw.println("\tnode [shape = doublecircle] \"" + state.getId() + "\";");
				}
				else
				{
					pw.println("\tnode [shape = ellipse] \"" + state.getId() + "\";");
				}
			}

			if (state.isForbidden())
			{
				pw.println("\tnode [shape = box] \"" + state.getId() + "\";");
			}
		}

		if (withCircles)
		{
			pw.println("\tnode [shape = circle];");
		}
		else
		{
			pw.println("\tnode [shape = plaintext];");
		}

		for (int i = 0; i < initialStates.size(); i++)
		{
			String stateId = ((State) initialStates.elementAt(i)).getId();

			pw.println("\t\"" + initPrefix + stateId + "\" [label = \"\"]; ");
			pw.println("\t\"" + initPrefix + stateId + "\" [height = \"0\"]; ");
			pw.println("\t\"" + initPrefix + stateId + "\" [width = \"0\"]; ");
			pw.println("\t\"" + initPrefix + stateId + "\" -> \"" + stateId + "\";");
		}

		//Alphabet theAlphabet = aut.getAlphabet();

		for (Iterator states = aut.stateIterator(); states.hasNext(); )
		{
			State sourceState = (State) states.next();

			pw.print("\t\"" + sourceState.getId() + "\" [label = \"");

			if (withLabel)
			{
				pw.print(EncodingHelper.normalize(sourceState.getName()));
			}

			pw.println("\"" + getColor(sourceState) + "]; ");

			for (Iterator arcSets = sourceState.outgoingArcSetIterator(); arcSets.hasNext(); )
			{
				ArcSet currArcSet = (ArcSet) arcSets.next();
				State fromState = currArcSet.getFromState();
				State toState = currArcSet.getToState();

				pw.print("\t\"" + fromState.getId() + "\" -> \"" + toState.getId());
				pw.print("\" [ label = \"");

				for (Iterator arcIt = currArcSet.iterator(); arcIt.hasNext(); )
				{
					Arc currArc = (Arc) arcIt.next();
					LabeledEvent thisEvent = currArc.getEvent(); // theAlphabet.getEventWithId(currArc.getEventId());

					if (!thisEvent.isControllable())
					{
						pw.print("!");
					}

					if (!thisEvent.isPrioritized())
					{
						pw.print("?");
					}

					if (thisEvent.isImmediate())
					{
						pw.print("#");
					}

					pw.print(EncodingHelper.normalize(thisEvent.getLabel()));

					if (arcIt.hasNext())
					{
						pw.print("\\n");
					}
				}

				pw.println("\" ];");
			}
		}

		// An attemp to always start at the initial state.
		// The problem is that a rectangle is drawn around the initial state.
		Iterator stateIt = initialStates.iterator();
		while(stateIt.hasNext())
		{
		 	State currState = (State)stateIt.next();
		 	pw.println("\t{ rank = min ;");
		 	pw.println("\t\t" + initPrefix + currState.getId() + ";");
		 	pw.println("\t\t" + currState.getId() + ";");
		 	pw.println("\t}");
		}

		pw.println("}");
		pw.flush();
		pw.close();
		*/
	}

	public void serialize(String fileName)
		throws Exception
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}
}
