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
import org.supremica.automata.Project;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class SimulatorStateViewer
	extends JPanel
//	implements SignalObserver
{
	private Automata theAutomata;
	private Project theProject;
	private AutomataSynchronizerHelper helper;
//	private int[] currState;
	private SimulatorEventList forwardEvents;
//	private SimulatorEventList backwardEvents;
	private SimulatorExecuterController controller;
	private SimulatorStateDisplayer stateDisplayer;
//	private JSplitPane eventSplitter;
	private JSplitPane stateEventSplitter;
//	private LinkedList prevStates = new LinkedList();
//	private LinkedList nextStates = new LinkedList();
	private SimulatorExecuter simulator;
	private EventExecuter theExecuter;
	private boolean executerIsExternal;

	public SimulatorStateViewer(SimulatorExecuter simulator, AutomataSynchronizerHelper helper, boolean executerIsExternal)
	{
		setLayout(new BorderLayout());

		this.simulator = simulator;
		theProject = simulator.getProject();

		theAutomata = helper.getAutomata();
		this.helper = helper;
		forwardEvents = new SimulatorEventList(simulator, helper, theProject);
		//backwardEvents = new SimulatorEventList(this, helper, theProject, theAnimationSignals, true);

		//eventSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forwardEvents, backwardEvents);
		stateDisplayer = new SimulatorStateDisplayer(helper);
		stateEventSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stateDisplayer, forwardEvents);

		add(stateEventSplitter, BorderLayout.CENTER);
		//theAnimationSignals.registerInterest(this);

		this.executerIsExternal = executerIsExternal;
		theExecuter = executerIsExternal ?
			new ExternalEventExecuter(simulator, forwardEvents.getEventListModel())
			: new EventExecuter(simulator, forwardEvents.getEventListModel());
	}

	public void initialize()
	{
//		eventSplitter.setDividerLocation(0.5);
		stateEventSplitter.setDividerLocation(0.6);
		theExecuter.start();
	}

	/** we do not use the built in executer /Arash */
	public boolean isExternal()
	{
		return executerIsExternal;
	}

//	public void setCurrState(int[] newState)
//	{
//		setCurrState(newState, false);
//	}

//	private void setCurrState(int[] newState, boolean isUndo)
//	{
//		if (!isUndo)
//		{
//			if (currState != null)
//			{
//				prevStates.addLast(currState);
//			}
//
//			nextStates.clear();
//		}
//
//		currState = newState;
//
//		update();
//	}

//	public void goToInitialState()
//	{
//		prevStates.clear();
//
//		currState = null;
//
//		helper.getCoExecuter().setCurrState(SimulatorExecuterHelper.getInitialState());
//		setCurrState(SimulatorExecuterHelper.getInitialState(), false);
//		simulator.resetAnimation();
//	}
//
//	public void undoState()
//	{
//		if (prevStates.size() > 0)
//		{
//			int[] newState = (int[]) prevStates.removeLast();
//
//			nextStates.addFirst(currState);
//			setCurrState(newState, true);
//		}
//	}

//	public boolean undoEnabled()
//	{
//		return prevStates.size() > 0;
//	}
//
//	public void redoState()
//	{
//		if (nextStates.size() > 0)
//		{
//			int[] newState = (int[]) nextStates.removeFirst();
//
//			prevStates.addLast(currState);
//			setCurrState(newState, true);
//		}
//	}
//
//	public boolean redoEnabled()
//	{
//		return nextStates.size() > 0;
//	}

//	public void update()
//	{

		// The order of theese are changed, for states to be properly forbidden...
//		forwardEvents.setCurrState(currState);
//		backwardEvents.setCurrState(currState);
//		stateDisplayer.setCurrState(currState);
//		controller.update();
//	}

//	public void signalUpdated()
//	{
//		update();
//	}

//	public void executeEvent(LabeledEvent event, int[] newState)
//	{
//		simulator.executeEvent(event);
//		setCurrState(newState);
//	}

	public void setController(SimulatorExecuterController controller)
	{
		this.controller = controller;
	}

	public void executeControllableEvents(boolean doExecute)
	{
		theExecuter.executeControllableEvents(doExecute);
	}

	public void executeUncontrollableEvents(boolean doExecute)
	{
		theExecuter.executeUncontrollableEvents(doExecute);
	}

	public void close()
	{
		if (theExecuter != null)
		{
			theExecuter.requestStop();
		}
	}

//	public SimulatorExecuter getSimulatorExecuter()
//	{
//		return simulator;
//	}
}

