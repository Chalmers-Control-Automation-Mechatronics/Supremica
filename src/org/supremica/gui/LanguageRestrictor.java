/*********************** LanguageRestrictor.java ******************/
// This is the GUI interface for the restriction facility
// Collects the alphabet to consider as epsilons and calls Determinizer
// Does not alter the original automata, but generates new automata
// named as "restr(automaton)"

// Note, this is the first command to implement the Swing.Action interface
// Makes thinsg so much simpler!

package org.supremica.gui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Alphabet;
import org.supremica.automata.LabeledEvent;
import org.supremica.util.VPopupMenu;
import org.supremica.automata.algorithms.standard.Determinizer;

// To be able to show disabled tree nodes, we need a custom renderer
class EventNodeRenderer 
	extends DefaultTreeCellRenderer 
{
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                        							boolean leaf, int row, boolean hasFocus) 
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        /*
        SupremicaTreeNode eventnode = (SupremicaTreeNode)value;
        if(!eventnode.isEnabled())
        {
        	setIcon(getDisabledIcon()); // getDisabledIcxon is from JLabel
        }
		*/
        return this;
    }
}
// TODO:
// Need the following: selecting any child ov an EventSubTree, selects the entire subtree
// A disabled EventSubTree should not be selectable
// 
class ViewerPanel
	extends JPanel
{
	public ViewerPanel()
	{
	}
}

class EventsViewerPanel	// compare AlphabetsViewerPanel
	extends JPanel
{
	private JTree theTree = new JTree();
	private SupremicaTreeNode root = new SupremicaTreeNode();
	private JScrollPane scrollPanel = new JScrollPane(theTree);
	Automata automata = null; 

	EventsViewerPanel(Automata automata)
	{
		this.automata = automata;
		init();
		build(false);
	}
	
	private void init()
	{
		theTree.setCellRenderer(new EventNodeRenderer());
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 400));
		add(scrollPanel, BorderLayout.CENTER);		
	}
	// In this panel, the EventSubTrees are at level 1 (root is at 0) with the AlphabetViewerSubTrees at level 2
	// The AlphabetViewerSubTrees are only there to show what automata the event belongs to
	// Shold make an effort to cache the AlphabetViewerSubTrees and not calc and store the same subtree several times
	public void build(boolean showroot)
	{
		// Was a good idea, but subtrees cannot be shared, I guess this has to do with requiring a single parent
		// DefaultMutableTreeNode::add() says "Removes newChild from its parent and makes it a child of this node"
		// So, we have to create over and oper again -- bummer.
		// EventTreeCache eventTreeCache = new EventTreeCache();
		// AutomatonTreeCache automatonTreeCache = new AutomatonTreeCache();
		
		// This cache is only for storing whether we have already seen the event or not
		HashSet eventTreeCache = new HashSet();
		
		Iterator autit = automata.iterator();
		while(autit.hasNext())
		{
			Automaton aut = (Automaton)autit.next();
			
			// Iterate over the events, for each event not already encountered, calc its subtree
			// Then add as children all the automata containing this event
			Iterator eventit = aut.getAlphabet().iterator();
			while(eventit.hasNext())
			{
				LabeledEvent event = (LabeledEvent)eventit.next();
				if(!eventTreeCache.contains(event)) // if not already seen
				{
					EventSubTree eventsubtree = new EventSubTree(event);
					eventTreeCache.add(event);
					root.add(eventsubtree);
			
					// Now, look for automata containing this event
					Iterator autoit = automata.iterator();
					while(autoit.hasNext())
					{
						Automaton auto = (Automaton)autoit.next();
						if(auto.getAlphabet().contains(event))
						{
							// AlphabetViewerSubTree autonode = automatonTreeCache.lookup(auto);
							// if(autonode == null)
							// {
							// 	autonode = new AlphabetViewerSubTree(auto);
							// 	automatonTreeCache.store(auto, autonode);
							// }
							AlphabetViewerSubTree autonode = new AlphabetViewerSubTree(auto);
							eventsubtree.add(autonode);
						}
					}
				}
			}
		}
		
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		theTree.setModel(treeModel);
		theTree.setRootVisible(showroot);
		theTree.setShowsRootHandles(true);

		revalidate();
	}
	
	// Rebuild after having events added
	public void rebuild()
	{
		SupremicaTreeNode temp = root;	// save in case of exception
		try
		{
			root = new SupremicaTreeNode();
			build(true); // build an entirely new one
		}
		catch(Exception excp)
		{
			root = temp;
			excp.printStackTrace();
		}
	}
	

	// Go through the tree and unhide all nodes
	public void showUnion()
	{
		// for all the (immediate) children of the root, make them visible
		for (Enumeration enum = root.children(); enum.hasMoreElements(); ) 
		{
			EventSubTree node = (EventSubTree)enum.nextElement();
			node.setEnabled(true);
        }
        revalidate();
	}
	
	// Go through the tree and make hide those nodes that are not in all automata
	public void showIntersection()
	{
		// for all the (immediate) children of the root, make them visible
		for (Enumeration enum = root.children(); enum.hasMoreElements(); ) 
		{
			EventSubTree node = (EventSubTree)enum.nextElement();
			
			// If the number of children is not the same as the number of automata plus the number in EventSubTree
			// Then it has to be disabled/hidden
			if(node.getChildCount()-EventSubTree.numChildren() != automata.size())
			{
				node.setEnabled(false);
			}
        }
        revalidate();
	}
	
	public TreePath[] getSelectionPaths()
	{
		return theTree.getSelectionPaths();
	}
	
	/* These are the caches used during tree building
	class EventTreeCache
		extends HashMap		// HashMap<LabeledEvent, EventSubTree>
	{
		public EventSubTree lookup(LabeledEvent key)
		{
			return (EventSubTree)get(key);
		}
		public EventSubTree cache(LabeledEvent key, EventSubTree value)
		{
			return (EventSubTree)put(key, value);
		}
		
	}
	class AutomatonTreeCache
		extends HashMap	// HashMap<Automaton, AlphabetViewerSubTree>
	{
		public AlphabetViewerSubTree lookup(Automaton key)
		{
			return (AlphabetViewerSubTree)get(key);
		}
		public AlphabetViewerSubTree cache(Automaton key, AlphabetViewerSubTree value)
		{
			return (AlphabetViewerSubTree)put(key, value);
		}
	}
	*/
	
}

class RestrictEventsViewerPanel
	extends JPanel
{
	private JTree tree = null;
	private JScrollPane scrollpane = null;
	private Automaton automaton = new Automaton("Restrict Events");
	private AlphabetViewerSubTree root = null;
	
	public RestrictEventsViewerPanel()
	{
		this.root = new AlphabetViewerSubTree(automaton);
		this.tree = new JTree(root);
		this.scrollpane = new JScrollPane(tree);

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 400));
		add(scrollpane, BorderLayout.CENTER);		
	}

	// add this event to the first and only! automaton
	public void add(LabeledEvent event)
	{
		try
		{
			Alphabet alpha = automaton.getAlphabet();
			if(!alpha.containsEventWithLabel(event.getLabel()))
			{
				alpha.addEvent(event);
			}
		}
		catch(Exception excp)
		{
			excp.printStackTrace();
		}
	}
	// 
	public void remove(LabeledEvent event)
	{
		try
		{
			Alphabet alpha = automaton.getAlphabet();
			alpha.removeEvent(event);
		}
		catch(Exception excp)
		{
			excp.printStackTrace();
		}
		
	}
	
	// If the alphabet has changed, you have to rebuild
	public void rebuild()
	{
		this.root = new AlphabetViewerSubTree(automaton);
		((DefaultTreeModel)tree.getModel()).setRoot(root);
		revalidate();
	}

	public TreePath[] getSelectionPaths()
	{
		return tree.getSelectionPaths();
	}
	
	public Alphabet getAlphabet()
	{
		return automaton.getAlphabet();
	}
}

class LanguageRestrictorDialog
	extends JFrame
{
	private Automata automata;
	private boolean doit = false;
	private EventsViewerPanel sourceEvents;
	private RestrictEventsViewerPanel restrictEvents;
	
	private void shutWindow() { setVisible(false); dispose(); }
	
	private class OkButton
		extends JButton
	{
		public OkButton()
		{
			super("Ok");
			setToolTipText("Do teh restriction");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doit = true;
					System.out.println(restrictEvents.getAlphabet().toString());

				}
			});
		}
	}
	
	private class CancelButton
		extends JButton
	{
		public CancelButton()
		{
			super("Close");
			setToolTipText("Close this window");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					shutWindow();
				}
			});
		}
		/*  For some reason, this is not the way to do it
		public void actionPerformed(ActionEvent event)
		{
			shutWindow();
		}
		*/
	}
	
	private class MoveButton
		extends JButton
	{
		public MoveButton()
		{
			super(">>");
			setToolTipText("Copy selected events to restriction alphabet");

			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					moveEvents();
				}
			});
		}
	}
	
	private class RemoveButton
		extends JButton
	{
		public RemoveButton()
		{
			super("<<");
			setToolTipText("Copy selected events to restriction alphabet");

			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					removeEvents();
				}
			});
		}
	}
	
	// Move the selected events from sourceEvents to restrictEvents
	private void moveEvents()
	{
		TreePath[] paths = sourceEvents.getSelectionPaths();
		
		for(int i = 0; i < paths.length; ++i)
		{
			TreePath path = paths[i];
			// The second element is the one we're interested in. Get its event object
			SupremicaTreeNode node = (SupremicaTreeNode)path.getPathComponent(1);
			LabeledEvent event = (LabeledEvent)node.getUserObject();
			
			restrictEvents.add(event);
		}
		
		restrictEvents.rebuild();
	}
	
	// Remove the selected events from restrictEvents to sourceEvents
	private void removeEvents()
	{
		TreePath[] paths = restrictEvents.getSelectionPaths();
		
		for(int i = 0; i < paths.length; ++i)
		{
			TreePath path = paths[i];
			// The second element is the one we're interested in
			SupremicaTreeNode node = (SupremicaTreeNode)path.getPathComponent(1);
			LabeledEvent event = (LabeledEvent)node.getUserObject();
			restrictEvents.remove(event);
		}
		
		restrictEvents.rebuild();
		
	}
	
	// Retunr the the restrictEvents
	public Alphabet getRestrictionAlphabet()
	{
		return restrictEvents.getAlphabet();
	}
	
	// Almost identical to AlphabetViewer menubar
	private void initMenubar()
	{
		// File
		JMenu menuFile = new JMenu();
		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		// File.Close
		JMenuItem menuFileClose = new JMenuItem();
		menuFileClose.setText("Close");
		menuFileClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				//dispose();
			}
		});
		
		menuFile.add(menuFileClose);
		
		// View
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		
		// View.Union (default, therefore initially checked)
		JRadioButtonMenuItem viewMenuUnion = new JRadioButtonMenuItem("Union", true);
		viewMenuUnion.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sourceEvents.showUnion();
			}
		});
		
		// View.Intersection
		JRadioButtonMenuItem viewMenuIntersection = new JRadioButtonMenuItem("Intersection");
		viewMenuIntersection.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sourceEvents.showIntersection();
			}
		});
		ButtonGroup buttongroup = new ButtonGroup();
		buttongroup.add(viewMenuUnion);
		buttongroup.add(viewMenuIntersection);
		
		viewMenu.add(viewMenuUnion);
		viewMenu.add(viewMenuIntersection);

		// For the moment, until we get the rendering etc fixed
		viewMenuUnion.setEnabled(false);
		viewMenuIntersection.setEnabled(false);
		
		JMenuBar menuBar = new JMenuBar();		
		menuBar.add(menuFile);
		menuBar.add(viewMenu);
		setJMenuBar(menuBar);
	}
	
	public LanguageRestrictorDialog(Automata automata)
	{
		this.automata = automata;
		this.sourceEvents = new EventsViewerPanel(automata);
		this.restrictEvents = new RestrictEventsViewerPanel();
		
		initMenubar();
		
		JPanel okcancelpanel = new JPanel(); // default is flowlayout
		okcancelpanel.add(new OkButton());
		okcancelpanel.add(new CancelButton());
		
		JPanel movebuttonpanel = new JPanel(new BorderLayout());
		movebuttonpanel.add(new MoveButton(), BorderLayout.NORTH);
		movebuttonpanel.add(new RemoveButton(), BorderLayout.SOUTH);
		
		JPanel buttonpanel = new JPanel(new BorderLayout());
		buttonpanel.add(movebuttonpanel, BorderLayout.CENTER);
		buttonpanel.add(okcancelpanel, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(sourceEvents, BorderLayout.WEST);
		panel.add(restrictEvents, BorderLayout.EAST);
		panel.add(buttonpanel, BorderLayout.CENTER);
		
		getContentPane().add(panel);

		Utility.setupFrame(this, 600, 600);
		pack();
		show();
	}
}

public class LanguageRestrictor
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(LanguageRestrictor.class);

	public LanguageRestrictor()
	{
		putValue(NAME, "Language Restriction");
		putValue(SHORT_DESCRIPTION, "Restrict the language to a subset of the alphabet");
		
	}
	
	public void actionPerformed(ActionEvent event)
	{
		// Get the selected automata
		Automata automata = ActionMan.gui.getSelectedAutomata();
		// Throw up the dialog, let the user select the alphabet

		LanguageRestrictorDialog dlg = new LanguageRestrictorDialog(automata);
		Alphabet alpha = dlg.getRestrictionAlphabet();
	}
}