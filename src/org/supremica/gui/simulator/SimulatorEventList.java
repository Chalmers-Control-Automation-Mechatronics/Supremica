
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

package org.supremica.gui.simulator;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Project;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class SimulatorEventList
	extends JPanel
{
	private boolean showStateId = false;
	private Automata theAutomata;
	private int[] currState;
	private SimulatorStateViewer stateViewer;
	private SimulatorEventListModel eventsList;
	private JList theList;
	private boolean allowEventSelection = false;

	public SimulatorEventList(SimulatorStateViewer stateViewer, AutomataSynchronizerHelper helper, Project theProject, AnimationSignals theSignals, boolean showDisabledEvents)
	{
		setLayout(new BorderLayout());

		this.stateViewer = stateViewer;
		this.theAutomata = helper.getAutomata();
		eventsList = new SimulatorEventListModel(helper, theProject, theSignals, showDisabledEvents);
		allowEventSelection = !showDisabledEvents;
		theList = new JList(eventsList);

		JScrollPane scrollPanel = new JScrollPane(theList);

		theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JLabel jLabel = null;

		if (showDisabledEvents)
		{
			jLabel = new JLabel("Outgoing events");
		}
		else
		{
			jLabel = new JLabel("Enabled events");
		}

		add(jLabel, BorderLayout.NORTH);
		add(scrollPanel, BorderLayout.CENTER);
		theList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (clickable())
				{
					if (e.getClickCount() == 2)
					{
						int index = theList.locationToIndex(e.getPoint());

						if (index >= 0)
						{
							// KA : These two commands should probably be executed without interruption
							// Try with a wrapper object
							LabeledEvent currEvent = eventsList.getEventAt(index);
							int[] newState = eventsList.getStateAt(index);

							executeEvent(currEvent);

							updateStateViewer(newState);
						}
					}
				}
			}
		});
	}

	public boolean clickable()
	{
		return allowEventSelection;
	}

	public void setShowStateId(boolean showStateId)
	{
		eventsList.setShowStateId(showStateId);
	}

	public void setCurrState(int[] currState)
	{
		this.currState = currState;

		theList.clearSelection();
		update();
	}

	public void update()
	{
		eventsList.setCurrState(currState);
	}

	private void updateStateViewer(int[] newState)
	{
		stateViewer.setCurrState(newState);
	}

	private void executeEvent(LabeledEvent event)
	{
		stateViewer.executeEvent(event);
	}

}
