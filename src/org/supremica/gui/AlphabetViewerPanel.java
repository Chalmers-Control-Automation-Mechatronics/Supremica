
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
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
import org.supremica.log.*;

class SupremicaTreeNode
	extends DefaultMutableTreeNode
{
	boolean enabled = true;
	
	public SupremicaTreeNode()
	{
		super();
	}
	
	public SupremicaTreeNode(Object obj)
	{
		super(obj);
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean b)
	{
		enabled = b;
	}
}

// An EventSubTree is a tree node with the event name as root and the event properties as children
class EventSubTree
	extends SupremicaTreeNode
{
	public EventSubTree(LabeledEvent event)
	{
		super(event);	// Note that this also caches the event for quick access

		SupremicaTreeNode currControllableNode = new SupremicaTreeNode("controllable: " + event.isControllable());
		add(currControllableNode);

		SupremicaTreeNode currPrioritizedNode = new SupremicaTreeNode("prioritized: " + event.isPrioritized());
		add(currPrioritizedNode);
	}
	
	// Change this to reflect the correct number of children
	public static int numChildren()
	{
		return 2;
	}
}
// An AlphabetViewerSubTree is a tree node with the automaton name as root and the events as children
class AlphabetViewerSubTree	
	extends SupremicaTreeNode
{
	public AlphabetViewerSubTree(Automaton automaton)
	{
		super(automaton.getName());

		Iterator eventIt = automaton.getAlphabet().iterator();
		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();
			add(new EventSubTree(currEvent));
		}	
	}
}
// I changed AlphabetViewer to accept Automata objects and to show the alphabets 
// of all selected Automaton in the same window. That's probably what you want if 
// you select more than one and request Alphabet viewing. Previously, one 
// AlphabetViewer was opened for each automaton.
public class AlphabetViewerPanel
	extends JPanel
	// implements AutomatonListener // to what are we to listen?
{
	private static Logger logger = LoggerFactory.createLogger(AlphabetViewerPanel.class);

	private Automata theAutomata;
	private Alphabet theAlphabet;
	private boolean showId = false;
	private boolean updateNeeded = false;
	private JTree theTree = new JTree();
	private JScrollPane scrollPanel = new JScrollPane(theTree);

	public AlphabetViewerPanel(Automata theAutomata)	// What's the reason for passing an automata here?
		throws Exception								// Alphabets cannot exist outside an automaton?
	{

		this.theAutomata = theAutomata;

		// theAutomaton.getListeners().addListener(this);	// What are we to listen to?

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
			catch (Exception e)
			{
				System.err.println("Error while updating AlphabetViewer");
			}
		}
	}
**/
	public void build()
	{
		SupremicaTreeNode root = new SupremicaTreeNode();
		
		Iterator autit = theAutomata.iterator();
		while(autit.hasNext())
		{
			root.add(new AlphabetViewerSubTree((Automaton)autit.next()));
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
*/	}
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
