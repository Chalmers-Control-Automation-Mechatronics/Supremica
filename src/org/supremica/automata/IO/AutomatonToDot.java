
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
import org.supremica.automata.Arc;
import org.supremica.automata.ArcSet;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class AutomatonToDot
	implements AutomataSerializer
{

	// We hope that this is the size of an A4 page (isn't 8.5" times 11" ??)
	private static final int DEFAULT_WIDTH = 7;
	private static final int DEFAULT_HEIGHT = 11;
	private Automaton aut;
	private boolean leftToRight = false;
	private boolean withLabel = true;
	private boolean withCircles = false;
	private boolean useStateColors = false;
	private boolean useArcColors = false;
	private boolean writeEventLabels = true;

	public AutomatonToDot(Automaton aut)
	{
		this.aut = aut;
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

	public void setWithEventLabels(boolean withLabel)
	{
		this.writeEventLabels = withLabel;
	}


	public void setWithCircles(boolean withCircles)
	{
		this.withCircles = withCircles;
	}

	public void setUseStateColors(boolean useStateColors)
	{
		this.useStateColors = useStateColors;
	}

	public void setUseArcColors(boolean useArcColors)
	{
		this.useArcColors = useArcColors;
	}

	protected String getStateColor(State s)
	{
		if (!useStateColors)
		{
			return "";
		}

		if (s.isAccepting() &&!s.isForbidden())
		{
			return ", color = green3";
		}

		if (s.isMutuallyAccepting() &&!s.isForbidden())
		{
			return ", color = yellow";
		}

		if (s.isForbidden())
		{
			return ", color = red1";
		}

		return "";
	}

	protected String getArcColor(boolean is_ctrl, boolean is_prio, boolean is_imm, boolean is_eps, boolean is_obs)
	{
		if (useArcColors)
		{
			if (is_ctrl)
			{
				return ", color = green3";
			}
			else
			{
				return ", color = red1";
			}
		}

		return "";
	}

	public void serialize(PrintWriter pw)
		throws Exception
	{
		aut.normalizeStateIdentities();

		Vector initialStates = new Vector();
		final String initPrefix = "__init_";
		String standardShape = null;
		String acceptingShape = null;
		String mutuallyAcceptingShape = null;
		String forbiddenShape = null;

		pw.println("digraph state_automaton {");
		pw.println("\tcenter = true;");

		// fix page size to this:
		pw.println("\tsize = \"" + DEFAULT_WIDTH + "," + DEFAULT_HEIGHT + "\";");

		if (leftToRight)
		{
			pw.println("\trankdir = LR;");
		}

		if (withCircles)
		{
			standardShape = "circle";
			acceptingShape = "doublecircle";
			mutuallyAcceptingShape = "doublecircle";
			forbiddenShape = "box";
		}
		else
		{
			standardShape = "plaintext";
			acceptingShape = "ellipse";
			mutuallyAcceptingShape = "ellipse";
			forbiddenShape = "box";
		}

		// The mutually accepting states are not shown if we aren't using colors...
		if (!useStateColors)
		{
			mutuallyAcceptingShape = "plaintext";
		}

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
				pw.println("\t{node [shape = plaintext, style=invis] \"" + initPrefix + state.getId() + "\"};");
			}

			if (state.isAccepting() &&!state.isForbidden())
			{
				pw.println("\t{node [shape = " + acceptingShape + "] \"" + state.getId() + "\"};");
			}

			if (state.isMutuallyAccepting() &&!state.isForbidden())
			{
				pw.println("\t{node [shape = " + mutuallyAcceptingShape + "] \"" + state.getId() + "\"};");
			}
			else if (state.isForbidden())
			{
				pw.println("\t{node [shape = " + forbiddenShape + "] \"" + state.getId() + "\"};");
			}
			else
			{
				pw.println("\t{node [shape = " + standardShape + "] \"" + state.getId() + "\"};");
			}
		}

		for (int i = 0; i < initialStates.size(); i++)
		{
			String stateId = ((State) initialStates.elementAt(i)).getId();

			// pw.println("\t\"" + initPrefix + stateId + "\" [label = \"\"]; ");
			// pw.println("\t\"" + initPrefix + stateId + "\" [height = \"0\"]; ");
			// pw.println("\t\"" + initPrefix + stateId + "\" [width = \"0\"]; ");
			pw.println("\t\"" + initPrefix + stateId + "\" -> \"" + stateId + "\";");
		}

		//Alphabet theAlphabet = aut.getAlphabet();
		for (Iterator states = aut.stateIterator(); states.hasNext(); )
		{
			State sourceState = (State) states.next();

			pw.print("\t\"" + sourceState.getId() + "\" [label = \"");

			if (withLabel)
			{
				pw.print(EncodingHelper.normalize(sourceState.getName(), false));
			}

			pw.println("\"" + getStateColor(sourceState) + "]; ");


			for (Iterator arcSets = sourceState.outgoingArcSetIterator();
					arcSets.hasNext(); )
			{
				boolean is_ctrl = true;
				boolean is_prio = false;
				boolean is_imm = false;
				boolean is_eps = false;
				boolean is_obs = false;
				ArcSet currArcSet = (ArcSet) arcSets.next();
				State fromState = currArcSet.getFromState();
				State toState = currArcSet.getToState();

				pw.print("\t\"" + fromState.getId() + "\" -> \"" + toState.getId());

				pw.print("\" [ label = \"");

				if (writeEventLabels)
				{
					for (Iterator arcIt = currArcSet.iterator(); arcIt.hasNext(); )
					{
						Arc currArc = (Arc) arcIt.next();
						LabeledEvent thisEvent = currArc.getEvent();

						if (!thisEvent.isControllable())
						{
							pw.print("!");

							is_ctrl = false;
						}

						if (!thisEvent.isPrioritized())
						{
							pw.print("?");

							is_prio = true;
						}

						if (thisEvent.isImmediate())
						{
							pw.print("#");

							is_imm = true;
						}

						if (thisEvent.isEpsilon())
						{
							pw.print("@");

							is_eps = true;
						}

						if (!thisEvent.isObservable())
						{
							pw.print("$");

							is_obs = true;
						}

						pw.print(EncodingHelper.normalize(thisEvent.getLabel(), false));

						if (arcIt.hasNext())
						{
							pw.print("\\n");
						}
					}
				}

				pw.println("\" " + getArcColor(is_ctrl, is_prio, is_imm, is_eps, is_obs) + "];");
			}
		}

		// An attemp to always start at the initial state.
		// The problem is that a rectangle is drawn around the initial state.
		// Ok, new versions of dot seems to be able to deal with this.
		for (Iterator stateIt = initialStates.iterator(); stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			pw.println("\t{ rank = min ;");
			pw.println("\t\t\"" + initPrefix + currState.getId() + "\";");
			//pw.println("\t\t\"" + currState.getId() + "\";");
			pw.println("\t}");
		}

		pw.println("}");
		pw.flush();
		pw.close();
	}

	public void serialize(String fileName)
		throws Exception
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}
}
