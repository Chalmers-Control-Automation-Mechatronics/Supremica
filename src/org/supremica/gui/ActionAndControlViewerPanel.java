//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.*;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.automata.execution.*;
import org.supremica.log.*;

public class ActionAndControlViewerPanel
	extends JPanel
	implements AutomataListener
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(ActionAndControlViewerPanel.class);
	private Project theProject;
	@SuppressWarnings("unused")
	private boolean updateNeeded = false;
	private JTree theTree = new JTree();
	private JScrollPane scrollPanel = new JScrollPane(theTree);

	public ActionAndControlViewerPanel(Project theProject)
		throws Exception
	{
		this.theProject = theProject;

		theProject.addListener(this);
		setLayout(new BorderLayout());

		// setPreferredSize(new Dimension(75, 400));
		add(scrollPanel, BorderLayout.CENTER);
		build();
	}

	public void initialize() {}

	public void build()
		throws Exception
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Execution");
		DefaultMutableTreeNode inputSignalsNode = new DefaultMutableTreeNode("InputSignals");

		root.add(inputSignalsNode);

		DefaultMutableTreeNode outputSignalsNode = new DefaultMutableTreeNode("OutputSignals");

		root.add(outputSignalsNode);

		DefaultMutableTreeNode actionsNode = new DefaultMutableTreeNode("Actions");

		root.add(actionsNode);

		DefaultMutableTreeNode controlsNode = new DefaultMutableTreeNode("Controls");

		root.add(controlsNode);

		DefaultMutableTreeNode timersNode = new DefaultMutableTreeNode("Timers");

		root.add(timersNode);

		for (Iterator<Signal> theIt = theProject.inputSignalsIterator();
				theIt.hasNext(); )
		{
			Signal currSignal = (Signal) theIt.next();
			DefaultMutableTreeNode currSignalNode = new DefaultMutableTreeNode(currSignal.getLabel());

			inputSignalsNode.add(currSignalNode);

			DefaultMutableTreeNode currPortNode = new DefaultMutableTreeNode("Port");

			currSignalNode.add(currPortNode);

			DefaultMutableTreeNode currPortNodeAttribute = new DefaultMutableTreeNode(new Integer(currSignal.getPort()));

			currPortNode.add(currPortNodeAttribute);
		}

		for (Iterator<Signal> theIt = theProject.outputSignalsIterator();
				theIt.hasNext(); )
		{
			Signal currSignal = (Signal) theIt.next();
			DefaultMutableTreeNode currSignalNode = new DefaultMutableTreeNode(currSignal.getLabel());

			outputSignalsNode.add(currSignalNode);

			DefaultMutableTreeNode currPortNode = new DefaultMutableTreeNode("Port");

			currSignalNode.add(currPortNode);

			DefaultMutableTreeNode currPortNodeAttribute = new DefaultMutableTreeNode(new Integer(currSignal.getPort()));

			currPortNode.add(currPortNodeAttribute);
		}

		Actions currActions = theProject.getActions();

		if (currActions != null)
		{
			for (Iterator<Action> actIt = currActions.iterator(); actIt.hasNext(); )
			{
				Action currAction = (Action) actIt.next();
				DefaultMutableTreeNode currActionNode = new DefaultMutableTreeNode(currAction.getLabel());

				actionsNode.add(currActionNode);

				for (Iterator<Command> cmdIt = currAction.commandIterator();
						cmdIt.hasNext(); )
				{
					Command currCommand = (Command) cmdIt.next();
					DefaultMutableTreeNode currCommandNode = new DefaultMutableTreeNode(currCommand);

					currActionNode.add(currCommandNode);
				}
			}
		}

		Controls currControls = theProject.getControls();

		if (currControls != null)
		{
			for (Iterator<Control> conIt = currControls.iterator(); conIt.hasNext(); )
			{
				Control currControl = (Control) conIt.next();
				DefaultMutableTreeNode currControlNode = new DefaultMutableTreeNode(currControl.getLabel());

				controlsNode.add(currControlNode);

				for (Iterator<Condition> condIt = currControl.conditionIterator();
						condIt.hasNext(); )
				{
					Condition currCondition = (Condition) condIt.next();
					DefaultMutableTreeNode currConditionNode = new DefaultMutableTreeNode(currCondition);

					currControlNode.add(currConditionNode);
				}
			}
		}

		for (Iterator<EventTimer> theIt = theProject.timerIterator(); theIt.hasNext(); )
		{
			EventTimer currTimer = (EventTimer) theIt.next();
			DefaultMutableTreeNode currTimerNode = new DefaultMutableTreeNode(currTimer.getName());

			timersNode.add(currTimerNode);

			// Start event
			DefaultMutableTreeNode currStartEventNode = new DefaultMutableTreeNode("Start event");

			currTimerNode.add(currStartEventNode);

			DefaultMutableTreeNode currStartEventNodeAttribute = new DefaultMutableTreeNode(currTimer.getStartEvent());

			currStartEventNode.add(currStartEventNodeAttribute);

			// Timeout event
			DefaultMutableTreeNode currTimeoutEventNode = new DefaultMutableTreeNode("Timeout event");

			currTimerNode.add(currTimeoutEventNode);

			DefaultMutableTreeNode currTimeoutEventNodeAttribute = new DefaultMutableTreeNode(currTimer.getTimeoutEvent());

			currTimeoutEventNode.add(currTimeoutEventNodeAttribute);

			// delay
			DefaultMutableTreeNode currDelayNode = new DefaultMutableTreeNode("Delay (ms)");

			currTimerNode.add(currDelayNode);

			DefaultMutableTreeNode currDelayNodeAttribute = new DefaultMutableTreeNode(new Integer(currTimer.getDelay()));

			currDelayNode.add(currDelayNodeAttribute);
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(root);

		theTree.setModel(treeModel);
		revalidate();
	}

	public void setVisible(boolean toVisible)
	{
		super.setVisible(toVisible);
	}

	void rebuild()
	{
		try
		{
			build();
		}
		catch (Exception ex)
		{
			logger.error("ActionAndControlViewerPanel::rebuild ", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	public void automatonAdded(Automata automata, Automaton automaton)
	{    // Do nothing
	}

	public void automatonRemoved(Automata automata, Automaton automaton)
	{    // Do nothing
	}

	public void automatonRenamed(Automata automata, Automaton automaton)
	{    // Do nothing
	}

	public void actionsOrControlsChanged(Automata automata)
	{
		rebuild();
	}

	public void updated(Object o)
	{
		rebuild();
	}
}





