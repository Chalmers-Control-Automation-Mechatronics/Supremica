
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
package org.supremica.gui.automataExplorer;

import org.supremica.gui.*;
import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;


public class AutomataEventListModel
	extends AbstractListModel
{
	private int[] currState;

	// / private ArrayList currArcs = new ArrayList();
	private int[] events;
	private int eventAmount = 0;
	private boolean forward;
	private Automata theAutomata;
	private Alphabet theAlphabet;
	private boolean showState = false;
	private AutomataSynchronizerHelper helper;

	public AutomataEventListModel(AutomataSynchronizerHelper helper, boolean forward)
	{
		this.forward = forward;
		this.helper = helper;
		this.theAutomata = helper.getAutomata();
		this.theAlphabet = helper.getAutomaton().getAlphabet();
	}

	public void setCurrState(int[] currState)
	{
		this.currState = currState;

		update();
	}

	public void setShowStateId(boolean showState)
	{
		this.showState = showState;
	}

	public void update()
	{
		AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();

		if (forward)
		{
			events = onlineSynchronizer.getOutgoingEvents(currState);
		}
		else
		{
			events = onlineSynchronizer.getIncomingEvents(currState);
		}

		eventAmount = 0;

		while (events[eventAmount] != Integer.MAX_VALUE)
		{
			eventAmount++;
		}

		fireContentsChanged(this, 0, eventAmount - 1);

		/*
		 *  Iterator arcIt;
		 *  if (forward)
		 *  {
		 *  arcIt = currState.outgoingArcsIterator();
		 *  }
		 *  else
		 *  {
		 *  arcIt = currState.incomingArcsIterator();
		 *  }
		 *  currArcs.clear();
		 *  while (arcIt.hasNext())
		 *  {
		 *  Arc currArc = (Arc)arcIt.next();
		 *  currArcs.add(currArc);
		 *  }
		 *  fireContentsChanged(this, 0, currArcs.size() - 1);
		 */
	}

	public int getSize()
	{
		return eventAmount;
	}

	public Object getElementAt(int index)
	{
		org.supremica.automata.LabeledEvent currEvent;

		try
		{
			currEvent = theAlphabet.getEventWithIndex(events[index]);
		}
		catch (Exception e)
		{
			System.err.println("Error: Could not find event in alphabet!\n");

			return null;
		}

		StringBuffer responseString = new StringBuffer();

		if (!currEvent.isControllable())
		{
			responseString.append("!");
		}

		responseString.append(currEvent.getLabel());

		/*
		 *  if (showState)
		 *  {
		 *  int[] currState;
		 *  if (forward)
		 *  {
		 *  currState = currArc.getToState();
		 *  }
		 *  else
		 *  {
		 *  currState = currArc.getFromState();
		 *  }
		 *  responseString.append(" [state name: " + currState.getName() + "]");
		 *  }
		 */
		return responseString.toString();
	}

	public int[] getStateAt(int index)
	{
		AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();

		return onlineSynchronizer.doTransition(events[index]);

		/*
		 *  Arc currArc = (Arc)currArcs.get(index);
		 *  State newState;
		 *  if (forward)
		 *  {
		 *  newState = currArc.getToState();
		 *  }
		 *  else
		 *  {
		 *  newState = currArc.getFromState();
		 *  }
		 *  return newState;
		 */
	}
}