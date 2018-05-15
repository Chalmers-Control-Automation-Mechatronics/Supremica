//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.AutomataListener;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.execution.Action;
import org.supremica.automata.execution.Actions;
import org.supremica.automata.execution.Command;
import org.supremica.automata.execution.Condition;
import org.supremica.automata.execution.Control;
import org.supremica.automata.execution.Controls;
import org.supremica.automata.execution.EventTimer;
import org.supremica.automata.execution.Signal;


public class ActionAndControlViewerPanel
	extends JPanel
	implements AutomataListener
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(ActionAndControlViewerPanel.class);
	private final Project theProject;
	@SuppressWarnings("unused")
	private final boolean updateNeeded = false;
	private final JTree theTree = new JTree();
	private final JScrollPane scrollPanel = new JScrollPane(theTree);

	public ActionAndControlViewerPanel(final Project theProject)
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
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Execution");
		final DefaultMutableTreeNode inputSignalsNode = new DefaultMutableTreeNode("InputSignals");

		root.add(inputSignalsNode);

		final DefaultMutableTreeNode outputSignalsNode = new DefaultMutableTreeNode("OutputSignals");

		root.add(outputSignalsNode);

		final DefaultMutableTreeNode actionsNode = new DefaultMutableTreeNode("Actions");

		root.add(actionsNode);

		final DefaultMutableTreeNode controlsNode = new DefaultMutableTreeNode("Controls");

		root.add(controlsNode);

		final DefaultMutableTreeNode timersNode = new DefaultMutableTreeNode("Timers");

		root.add(timersNode);

		for (final Iterator<Signal> theIt = theProject.inputSignalsIterator();
				theIt.hasNext(); )
		{
			final Signal currSignal = theIt.next();
			final DefaultMutableTreeNode currSignalNode = new DefaultMutableTreeNode(currSignal.getLabel());

			inputSignalsNode.add(currSignalNode);

			final DefaultMutableTreeNode currPortNode = new DefaultMutableTreeNode("Port");

			currSignalNode.add(currPortNode);

			final DefaultMutableTreeNode currPortNodeAttribute = new DefaultMutableTreeNode(new Integer(currSignal.getPort()));

			currPortNode.add(currPortNodeAttribute);
		}

		for (final Iterator<Signal> theIt = theProject.outputSignalsIterator();
				theIt.hasNext(); )
		{
			final Signal currSignal = theIt.next();
			final DefaultMutableTreeNode currSignalNode = new DefaultMutableTreeNode(currSignal.getLabel());

			outputSignalsNode.add(currSignalNode);

			final DefaultMutableTreeNode currPortNode = new DefaultMutableTreeNode("Port");

			currSignalNode.add(currPortNode);

			final DefaultMutableTreeNode currPortNodeAttribute = new DefaultMutableTreeNode(new Integer(currSignal.getPort()));

			currPortNode.add(currPortNodeAttribute);
		}

		final Actions currActions = theProject.getActions();

		if (currActions != null)
		{
			for (final Iterator<Action> actIt = currActions.iterator(); actIt.hasNext(); )
			{
				final Action currAction = actIt.next();
				final DefaultMutableTreeNode currActionNode = new DefaultMutableTreeNode(currAction.getLabel());

				actionsNode.add(currActionNode);

				for (final Iterator<Command> cmdIt = currAction.commandIterator();
						cmdIt.hasNext(); )
				{
					final Command currCommand = cmdIt.next();
					final DefaultMutableTreeNode currCommandNode = new DefaultMutableTreeNode(currCommand);

					currActionNode.add(currCommandNode);
				}
			}
		}

		final Controls currControls = theProject.getControls();

		if (currControls != null)
		{
			for (final Iterator<Control> conIt = currControls.iterator(); conIt.hasNext(); )
			{
				final Control currControl = conIt.next();
				final DefaultMutableTreeNode currControlNode = new DefaultMutableTreeNode(currControl.getLabel());

				controlsNode.add(currControlNode);

				for (final Iterator<Condition> condIt = currControl.conditionIterator();
						condIt.hasNext(); )
				{
					final Condition currCondition = condIt.next();
					final DefaultMutableTreeNode currConditionNode = new DefaultMutableTreeNode(currCondition);

					currControlNode.add(currConditionNode);
				}
			}
		}

		for (final Iterator<EventTimer> theIt = theProject.timerIterator(); theIt.hasNext(); )
		{
			final EventTimer currTimer = theIt.next();
			final DefaultMutableTreeNode currTimerNode = new DefaultMutableTreeNode(currTimer.getName());

			timersNode.add(currTimerNode);

			// Start event
			final DefaultMutableTreeNode currStartEventNode = new DefaultMutableTreeNode("Start event");

			currTimerNode.add(currStartEventNode);

			final DefaultMutableTreeNode currStartEventNodeAttribute = new DefaultMutableTreeNode(currTimer.getStartEvent());

			currStartEventNode.add(currStartEventNodeAttribute);

			// Timeout event
			final DefaultMutableTreeNode currTimeoutEventNode = new DefaultMutableTreeNode("Timeout event");

			currTimerNode.add(currTimeoutEventNode);

			final DefaultMutableTreeNode currTimeoutEventNodeAttribute = new DefaultMutableTreeNode(currTimer.getTimeoutEvent());

			currTimeoutEventNode.add(currTimeoutEventNodeAttribute);

			// delay
			final DefaultMutableTreeNode currDelayNode = new DefaultMutableTreeNode("Delay (ms)");

			currTimerNode.add(currDelayNode);

			final DefaultMutableTreeNode currDelayNodeAttribute = new DefaultMutableTreeNode(new Integer(currTimer.getDelay()));

			currDelayNode.add(currDelayNodeAttribute);
		}

		final DefaultTreeModel treeModel = new DefaultTreeModel(root);

		theTree.setModel(treeModel);
		revalidate();
	}

	@Override
  public void setVisible(final boolean toVisible)
	{
		super.setVisible(toVisible);
	}

	void rebuild()
	{
		try
		{
			build();
		}
		catch (final Exception ex)
		{
			logger.error("ActionAndControlViewerPanel::rebuild ", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	@Override
  public void automatonAdded(final Automata automata, final Automaton automaton)
	{    // Do nothing
	}

	@Override
  public void automatonRemoved(final Automata automata, final Automaton automaton)
	{    // Do nothing
	}

	@Override
  public void automatonRenamed(final Automata automata, final Automaton automaton)
	{    // Do nothing
	}

	@Override
  public void actionsOrControlsChanged(final Automata automata)
	{
		rebuild();
	}

	@Override
  public void updated(final Object o)
	{
		rebuild();
	}
}
