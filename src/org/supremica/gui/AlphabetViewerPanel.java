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
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.treeview.*;

// I changed AlphabetViewer to accept Automata objects and to show the alphabets 
// of all selected Automaton in the same window. That's probably what you want if 
// you select more than one and request Alphabet viewing. Previously, one 
// AlphabetViewer was opened for each automaton.
public class AlphabetViewerPanel
	extends JPanel

// implements AutomatonListener // to what are we to listen?
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(AlphabetViewerPanel.class);
	private Automata theAutomata;
	@SuppressWarnings("unused")
	private Alphabet theAlphabet;
	@SuppressWarnings("unused")
	private boolean showId = false;
	@SuppressWarnings("unused")
	private boolean updateNeeded = false;
	private JTree theTree = new JTree();
	private JScrollPane scrollPanel = new JScrollPane(theTree);

	public AlphabetViewerPanel(Automata theAutomata)    // What's the reason for passing an automata here?
		throws Exception    // Alphabets cannot exist outside an automaton?
	{
		this.theAutomata = theAutomata;

		// theAutomaton.getListeners().addListener(this);       // What are we to listen to?
		// theAlphabet = theAutomaton.getAlphabet();
		setLayout(new BorderLayout());

		// setPreferredSize(new Dimension(75, 400));
		add(scrollPanel, BorderLayout.CENTER);
		build();
	}

/** AutomataListener stuff
		public void initialize() {}

		public void updated(Object o)
		{
				if (o == theAutomaton)
				{
						update();
				}
		}

		public void stateAdded(Automaton aut, State q)
		{
				updated(aut);
		}

		public void stateRemoved(Automaton aut, State q)
		{
				updated(aut);
		}

		public void arcAdded(Automaton aut, Arc a)
		{
				updated(aut);
		}

		public void arcRemoved(Automaton aut, Arc a)
		{
				updated(aut);
		}

		public void attributeChanged(Automaton aut)
		{
				updated(aut);
		}

		public void automatonRenamed(Automaton aut, String oldName)
		{
				updated(aut);
		}

		public void update()
		{
				if (!isVisible())
				{
						updateNeeded = true;
				}
				else
				{
						try
						{
								build();

								updateNeeded = false;
						}
						catch (Exception ex)
						{
								logger.error("Error while updating AlphabetViewer", ex);
								logger.debug(ex.getStackTrace());
						}
				}
		}
**/
	public void build()
	{
		SupremicaTreeNode root = new SupremicaTreeNode();    // Really AutomataSubTree(theAutomata, showalpha, nostates)?
		Iterator<Automaton> autit = theAutomata.iterator();

		while (autit.hasNext())
		{
			root.add(new AutomatonSubTree((Automaton) autit.next(), true, false));
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(root);

		theTree.setModel(treeModel);
		theTree.setRootVisible(false);
		theTree.setShowsRootHandles(true);

		// theTree.setExpanded(new TreePath(node));             
		revalidate();
	}

/* We use AlphabetViewerSubTree instead **
		public void build()
				throws Exception
		{
				DefaultMutableTreeNode root = new DefaultMutableTreeNode(theAutomaton.getName());
				int nbrOfEvents = 0;
				Iterator eventIt = theAlphabet.iterator();

				while (eventIt.hasNext())
				{
						org.supremica.automata.LabeledEvent currEvent = (org.supremica.automata.LabeledEvent) eventIt.next();
						DefaultMutableTreeNode currEventNode = new DefaultMutableTreeNode(currEvent.getLabel());

						root.add(currEventNode);

						DefaultMutableTreeNode currControllableNode = new DefaultMutableTreeNode("controllable: " + currEvent.isControllable());

						currEventNode.add(currControllableNode);

						DefaultMutableTreeNode currPrioritizedNode = new DefaultMutableTreeNode("prioritized: " + currEvent.isPrioritized());

						currEventNode.add(currPrioritizedNode);

						if (showId)
						{
								DefaultMutableTreeNode currIdNode = new DefaultMutableTreeNode("id: " + currEvent.getId());

								currEventNode.add(currIdNode);
						}
				}

				DefaultTreeModel treeModel = new DefaultTreeModel(root);

				theTree.setModel(treeModel);
				revalidate();
		}
**/
	public void setVisible(boolean toVisible)
	{
		super.setVisible(toVisible);

/*
				if (updateNeeded)
				{
						update();
				}
*/
	}
}

/*
 *  class Renderer
 *  extends DefaultTreeCellRenderer
 *  {
 *  public Renderer()
 *  {
 *  super();
 *  }
 *
 *  }
 *
 *  class Editor
 *  extends DefaultTreeEditor
 *  {
 *  public Editor()
 *  {
 *  super();
 *  }
 *
 *  }
 */
