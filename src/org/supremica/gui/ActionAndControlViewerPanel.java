
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
package org.supremica.gui;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
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
	private static Logger logger = LoggerFactory.createLogger(ActionAndControlViewerPanel.class);

	private Project theProject;
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
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Exceution");

		DefaultMutableTreeNode actionsNode = new DefaultMutableTreeNode("Actions");
		root.add(actionsNode);

		DefaultMutableTreeNode controlsNode = new DefaultMutableTreeNode("Controls");
		root.add(controlsNode);

		Actions currActions = theProject.getActions();
		if (currActions != null)
		{
			for (Iterator actIt = currActions.iterator(); actIt.hasNext();)
			{
				Action currAction = (Action)actIt.next();
				DefaultMutableTreeNode currActionNode = new DefaultMutableTreeNode(currAction.getLabel());
				actionsNode.add(currActionNode);
				for (Iterator cmdIt = currAction.commandIterator(); cmdIt.hasNext();)
				{
					String currCommand = (String)cmdIt.next();
					DefaultMutableTreeNode currCommandNode = new DefaultMutableTreeNode(currCommand);
					currActionNode.add(currCommandNode);
				}
			}

		}

		Controls currControls = theProject.getControls();
		if (currControls != null)
		{
			for (Iterator conIt = currControls.iterator(); conIt.hasNext();)
			{
				Control currControl = (Control)conIt.next();
				DefaultMutableTreeNode currControlNode = new DefaultMutableTreeNode(currControl.getLabel());
				controlsNode.add(currControlNode);
				for (Iterator condIt = currControl.conditionIterator(); condIt.hasNext();)
				{
					String currCondition = (String)condIt.next();
					DefaultMutableTreeNode currConditionNode = new DefaultMutableTreeNode(currCondition);
					currControlNode.add(currConditionNode);
				}
			}
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
	{ // Do nothing
	}

	public void automatonRemoved(Automata automata, Automaton automaton)
	{ // Do nothing
	}

	public void automatonRenamed(Automata automata, Automaton automaton)
	{ // Do nothing
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
