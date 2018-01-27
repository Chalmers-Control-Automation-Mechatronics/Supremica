
/*********************** LanguageRestrictor.java ******************/

// This is the GUI interface for the restriction facility
// Collects the alphabet to consider as epsilons and calls Determinizer
// Does not alter the original automata, but generates new automata
// named as "restr(automaton)"
// Note, this is the first command to implement the Swing.Action interface
// Makes things so much simpler!
// TODO:
// Need a TreeSelectionListener that selects the event and all its children
// when it or one of its children is selected (or only allow level 1 nodes to be selected
package org.supremica.testcases.warehouse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import org.supremica.automata.Alphabet;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.Utility;
import org.supremica.gui.treeview.AlphabetSubTree;
import org.supremica.gui.treeview.EventSubTree;
import org.supremica.gui.treeview.SupremicaTreeCellRenderer;
import org.supremica.gui.treeview.SupremicaTreeNode;

// To be able to show disabled tree nodes, we need a custom renderer
class EventNodeRenderer
	extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1L;

	@Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		/**/
		final SupremicaTreeNode eventnode = (SupremicaTreeNode) value;

		if (!eventnode.isEnabled())
		{

			// setIcon(getDisabledIcon()); // getDisabledIcxon is from JLabel
			setEnabled(false);
		}

		/**/
		return this;
	}
}

// TODO:
// Need the following: selecting any child ov an EventSubTree, selects the entire subtree
// A disabled EventSubTree should not be selectable
//
class EventSelectionModel
	extends DefaultTreeSelectionModel
{

	private static final long serialVersionUID = 1L;
}

class ViewerPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;

	public ViewerPanel() {}
}

class EventsViewerPanel    // compare AlphabetsViewerPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final JTree theTree = new JTree();
	private SupremicaTreeNode root = new SupremicaTreeNode();
	private final JScrollPane scrollPanel = new JScrollPane(theTree);
	Alphabet alphabet = null;

	EventsViewerPanel(final Alphabet alphabet)
	{
		this.alphabet = alphabet;

		build(false);
		init();
	}

	private void init()
	{
		theTree.setCellRenderer(new SupremicaTreeCellRenderer());    // EventNodeRenderer());
		theTree.setSelectionModel(new EventSelectionModel());
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 400));
		add(scrollPanel, BorderLayout.CENTER);
	}

	// In this panel, the EventSubTrees are at level 1 (root is at 0) with the AlphabetViewerSubTrees at level 2
	// The AlphabetViewerSubTrees are only there to show what automata the event belongs to
	// Shold make an effort to cache the AlphabetViewerSubTrees and not calc and store the same subtree several times
	public void build(final boolean showroot)
	{

		// Was a good idea, but subtrees cannot be shared, I guess this has to do with requiring a single parent
		// DefaultMutableTreeNode::add() says "Removes newChild from its parent and makes it a child of this node"
		// So, we have to create over and oper again -- bummer.
		// EventTreeCache eventTreeCache = new EventTreeCache();
		// AutomatonTreeCache automatonTreeCache = new AutomatonTreeCache();
		// This cache is only for storing whether we have already seen the event or not
		final HashSet<LabeledEvent> eventTreeCache = new HashSet<LabeledEvent>();

		for (final Iterator<?> eventit = alphabet.iterator(); eventit.hasNext(); )
		{
			final LabeledEvent event = (LabeledEvent) eventit.next();

			if (!eventTreeCache.contains(event))    // if not already seen
			{
				final EventSubTree eventsubtree = new EventSubTree(event);

				eventTreeCache.add(event);
				root.add(eventsubtree);
			}
		}

/*
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
														// AlphabetViewerSubTree autonode = new AlphabetViewerSubTree(auto);
														AutomatonSubTree autonode = new AutomatonSubTree(auto, true, false);
														eventsubtree.add(autonode);
												}
										}
								}
						}
				}
*/
		final DefaultTreeModel treeModel = new DefaultTreeModel(root);

		theTree.setModel(treeModel);
		theTree.setRootVisible(showroot);
		theTree.setShowsRootHandles(true);
		revalidate();
	}

	// Rebuild after having events added
	public void rebuild()
	{
		final SupremicaTreeNode temp = root;    // save in case of exception

		try
		{
			root = new SupremicaTreeNode();

			build(true);    // build an entirely new one
		}
		catch (final Exception excp)
		{
			root = temp;

			excp.printStackTrace();
		}
	}

	// Go through the tree and unhide all nodes
	public void showUnion()
	{

		// for all the (immediate) children of the root, make them visible
		for (final Enumeration<?> e = root.children(); e.hasMoreElements(); )
		{
			final EventSubTree node = (EventSubTree) e.nextElement();

			node.setEnabled(true);
		}

		repaint();
	}

/*
		// Go through the tree and make hide those nodes that are not in all automata
		public void showIntersection()
		{
				// for all the (immediate) children of the root, make them visible
				for (Enumeration e = root.children(); e.hasMoreElements(); )
				{
						EventSubTree node = (EventSubTree)e.nextElement();

						// If the number of children is not the same as the number of automata plus the number in EventSubTree
						// Then it has to be disabled/hidden/unselectable
						if(node.getChildCount() - node.numDirectLeafs() != automata.size())
						{
								node.setEnabled(false);
						}
		}
		repaint();
		}
*/
	public TreePath[] getSelectionPaths()
	{
		return theTree.getSelectionPaths();
	}

	/* These are the caches used during tree building
	class EventTreeCache
			extends HashMap         // HashMap<LabeledEvent, EventSubTree>
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
			extends HashMap // HashMap<Automaton, AlphabetViewerSubTree>
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

/**
 * THIS IS ALMOST A COPY OF THE LanguageRestrictor.java!!!!!!
 */
class RestrictEventsViewerPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JTree tree = null;
	private JScrollPane scrollpane = null;

	// private AutomatonSubTree root = null;
	private SupremicaTreeNode root = null;
	boolean erase = true;

	// private Automaton automaton = new Automaton("Select These Events");
	private final Alphabet alpha = new Alphabet();

	public RestrictEventsViewerPanel()
	{
		this.root = new AlphabetSubTree(alpha);

		root.setUserObject("Select These Events");

		this.tree = new JTree(root);
		this.scrollpane = new JScrollPane(tree);

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 400));
		add(scrollpane, BorderLayout.CENTER);
		tree.setCellRenderer(new SupremicaTreeCellRenderer());
	}

	// add this event to the first and only! automaton
	public void add(final LabeledEvent event)
	{
		try
		{

			// Alphabet alpha = automaton.getAlphabet();
			if (!alpha.contains(event.getLabel()))
			{
				alpha.addEvent(event);
			}
		}
		catch (final Exception excp)
		{
			excp.printStackTrace();
		}
	}

	public void remove(final LabeledEvent event)
	{
		try
		{

			// Alphabet alpha = automaton.getAlphabet();
			alpha.removeEvent(event);
		}
		catch (final Exception excp)
		{
			excp.printStackTrace();
		}
	}

	// If the alphabet has changed, you have to rebuild
	public void rebuild()
	{

		//this.root = new AutomatonSubTree(automaton, true, false);
		final SupremicaTreeNode newRoot = new AlphabetSubTree(alpha);

		newRoot.setUserObject(root.getUserObject());

		this.root = newRoot;

		((DefaultTreeModel) tree.getModel()).setRoot(root);
		revalidate();
	}

	public void eraseThese()
	{

		// automaton.setName("Select These Events");
		root.setUserObject("Select These Events");

		erase = true;

		tree.repaint();
	}

	public void keepThese()
	{

		// automaton.setName("Do Not Select These Events");
		root.setUserObject("Do Not Select These Events");

		erase = false;

		tree.repaint();
	}

	public boolean toErase()
	{
		return erase;
	}

	public TreePath[] getSelectionPaths()
	{
		return tree.getSelectionPaths();
	}

	public Alphabet getAlphabet()
	{

		// return automaton.getAlphabet();
		return alpha;
	}
}

class SelectOperatorEventsDialog
	extends JFrame
{
	private static final long serialVersionUID = 1L;
	private final Alphabet alphabet;
	@SuppressWarnings("unused")
	private final boolean doit = false;
	private final EventsViewerPanel sourceEvents;
	private final RestrictEventsViewerPanel restrictEvents;
	private boolean selectOperatorEvents = false;
	private boolean selectUnobservableEvents = false;

	private void shutWindow()
	{
		setVisible(false);
		dispose();
	}

	private class OkButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public OkButton()
		{
			super("Ok");

			setToolTipText("Update alphabets");
			addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)
				{
					doRestrict();
					shutWindow();
				}
			});
		}
	}

	private class CancelButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public CancelButton()
		{
			super("Close");

			setToolTipText("Close this window");
			addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)
				{
					shutWindow();
				}
			});
		}
	}

	private class MoveButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public MoveButton()
		{
			super(">>");

			setToolTipText("Copy selected events to alphabet");
			addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)
				{
					moveEvents();
				}
			});
		}
	}

	private class RemoveButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public RemoveButton()
		{
			super("<<");

			setToolTipText("Copy selected events to alphabet");
			addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)
				{
					removeEvents();
				}
			});
		}
	}

	// Move the selected events from sourceEvents to restrictEvents
	private void moveEvents()
	{
		final TreePath[] paths = sourceEvents.getSelectionPaths();

		if (paths != null)    // gotta have something selected
		{
			for (int i = 0; i < paths.length; ++i)
			{
				final TreePath path = paths[i];

				// The second element is the one we're interested in - component 0, the root, can never be selected.
				final SupremicaTreeNode node = (SupremicaTreeNode) path.getPathComponent(1);
				final LabeledEvent event = (LabeledEvent) node.getUserObject();

				restrictEvents.add(event);
			}

			restrictEvents.rebuild();
		}
	}

	// Remove the selected events from restrictEvents (not from sourceEvents, of course)
	private void removeEvents()
	{
		final TreePath[] paths = restrictEvents.getSelectionPaths();

		if (paths != null)    // gotta have something selected
		{
			for (int i = 0; i < paths.length; ++i)
			{
				final TreePath path = paths[i];

				// The second element is the one we're interested in - component 0, the root, can never be selected
				final SupremicaTreeNode node = (SupremicaTreeNode) path.getPathComponent(1);
				final LabeledEvent event = (LabeledEvent) node.getUserObject();

				restrictEvents.remove(event);
			}

			restrictEvents.rebuild();
		}
	}

/*
		// Return the the restrictEvents
		public Alphabet getRestrictionAlphabet()
		{
				return restrictEvents.getAlphabet();
		}
*/

	// Return the the restrictEvents
	public Alphabet getSelectedEvents()
	{
		return restrictEvents.getAlphabet();
	}

	// Almost identical to AlphabetViewer menubar
	private void initMenubar()
	{

		// File
		final JMenu menuFile = new JMenu();

		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		// File.Close
		final JMenuItem menuFileClose = new JMenuItem();

		menuFileClose.setText("Close");
		menuFileClose.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				setVisible(false);

				//dispose();
			}
		});
		menuFile.add(menuFileClose);

		// View
		final JMenu viewMenu = new JMenu("View");

		viewMenu.setMnemonic(KeyEvent.VK_V);

		// View.Union (default, therefore initially checked)
		final JRadioButtonMenuItem viewMenuUnion = new JRadioButtonMenuItem("Union", true);

		viewMenuUnion.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				sourceEvents.showUnion();
			}
		});

/*
				// View.Intersection
				JRadioButtonMenuItem viewMenuIntersection = new JRadioButtonMenuItem("Intersection");
				viewMenuIntersection.addActionListener(new ActionListener()
				{
						public void actionPerformed(ActionEvent e)
						{
								sourceEvents.showIntersection();
						}
				});
*/
		final ButtonGroup buttongroup = new ButtonGroup();

		buttongroup.add(viewMenuUnion);

//              buttongroup.add(viewMenuIntersection);
		viewMenu.add(viewMenuUnion);

//              viewMenu.add(viewMenuIntersection);
		// For the moment, until we get the rendering etc fixed
		// viewMenuUnion.setEnabled(false);
		// viewMenuIntersection.setEnabled(false);

/*
				// Restrict
				JMenu restrictMenu = new JMenu("Select");
				restrictMenu.setMnemonic(KeyEvent.VK_R);

				// Restrict.Erase These Events (default, therefore initially checked)
				JRadioButtonMenuItem restrictMenuErase = new JRadioButtonMenuItem("Select These Events", true);
				restrictMenuErase.addActionListener(new ActionListener()
				{
						public void actionPerformed(ActionEvent e)
						{
								restrictEvents.eraseThese();
						}
				});

				// Restrict.Keep These Events
				JRadioButtonMenuItem restrictMenuKeep = new JRadioButtonMenuItem("Keep These Events");
				restrictMenuKeep.addActionListener(new ActionListener()
				{
						public void actionPerformed(ActionEvent e)
						{
								restrictEvents.keepThese();
						}
				});
*/

/*
								ButtonGroup restrgroup = new ButtonGroup();
				restrgroup.add(restrictMenuErase);
				restrgroup.add(restrictMenuKeep);

				restrictMenu.add(restrictMenuErase);
				restrictMenu.add(restrictMenuKeep);
				restrictMenuErase.setEnabled(true);
				restrictMenuKeep.setEnabled(true);
*/
		final JMenuBar menuBar = new JMenuBar();

		menuBar.add(menuFile);

//              menuBar.add(viewMenu);
//              menuBar.add(restrictMenu);
		setJMenuBar(menuBar);
	}

	public SelectOperatorEventsDialog(final Alphabet alphabet, final String name, final boolean selectOperatorEvents)
	{
		super(name);

		if (selectOperatorEvents)
		{
			this.selectOperatorEvents = true;
			this.selectUnobservableEvents = false;
		}
		else
		{
			this.selectOperatorEvents = false;
			this.selectUnobservableEvents = true;
		}

		this.alphabet = alphabet;
		this.sourceEvents = new EventsViewerPanel(alphabet);
		this.restrictEvents = new RestrictEventsViewerPanel();

		initMenubar();

		final JPanel okcancelpanel = new JPanel();    // default is flowlayout

		okcancelpanel.add(new OkButton());
		okcancelpanel.add(new CancelButton());

		final JPanel movebuttonpanel = new JPanel(new BorderLayout());

		movebuttonpanel.add(new MoveButton(), BorderLayout.NORTH);
		movebuttonpanel.add(new RemoveButton(), BorderLayout.SOUTH);

		final JPanel buttonpanel = new JPanel(new BorderLayout());

		buttonpanel.add(movebuttonpanel, BorderLayout.CENTER);
		buttonpanel.add(okcancelpanel, BorderLayout.SOUTH);

		final JPanel panel = new JPanel(new BorderLayout());

		panel.add(sourceEvents, BorderLayout.WEST);
		panel.add(restrictEvents, BorderLayout.EAST);
		panel.add(buttonpanel, BorderLayout.CENTER);
		getContentPane().add(panel);
		Utility.setupFrame(this, 600, 600);
		pack();
		setVisible(true);
	}

	private void doRestrict()
	{

		// Get the restriction alphabet
		final Alphabet operatorEvents = restrictEvents.getAlphabet();

		if (selectOperatorEvents)
		{
			for (final Iterator<LabeledEvent> evIt = alphabet.iterator(); evIt.hasNext(); )
			{
				final LabeledEvent currEvent = evIt.next();

				currEvent.setOperatorIncrease(operatorEvents.contains(currEvent.getLabel()));
			}
		}
		else if (selectUnobservableEvents)
		{
			for (final Iterator<LabeledEvent> evIt = alphabet.iterator(); evIt.hasNext(); )
			{
				final LabeledEvent currEvent = evIt.next();

				currEvent.setObservable(!operatorEvents.contains(currEvent.getLabel()));
			}
		}

/*
				// Get the restriction alphabet
				Alphabet alpha = restrictEvents.getAlphabet();

				Automata newautomata = new Automata();

				Iterator autit = automata.iterator();
				while(autit.hasNext())
				{
						Automaton automaton = (Automaton)autit.next();
						Determinizer detm = new Determinizer(automaton, alpha, restrictEvents.toErase());
						detm.execute();
						Automaton newautomaton = detm.getNewAutomaton();
						newautomaton.setComment(automaton.getName() + "\\" + alpha.toString());
						newautomata.addAutomaton(newautomaton);
				}

				try
				{
						ActionMan.gui.addAutomata(newautomata);
				}
				catch(Exception ex)
				{
						logger.debug("LanguageRestriction::doRestrict() -- ", ex);
						logger.debug(ex.getStackTrace());
				}
*/
	}
}

public class SelectEventsWindow
	extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	private Alphabet theAlphabet = null;
	private SelectOperatorEventsDialog dlg = null;
	private final String name;
	private boolean selectOperator = false;

	public SelectEventsWindow(final Alphabet theAlphabet, final String name, final String description, final boolean selectOperator)
	{
		putValue(NAME, name);
		putValue(SHORT_DESCRIPTION, description);

		this.theAlphabet = theAlphabet;
		this.name = name;
		this.selectOperator = selectOperator;
	}

	@Override
  public void actionPerformed(final ActionEvent event)
	{

		// Get the selected automata
		//Automata automata = ActionMan.getGui().getSelectedAutomata();
		// Throw up the dialog, let the user select the alphabet
		if (dlg == null)
		{
			dlg = new SelectOperatorEventsDialog(theAlphabet, name, selectOperator);
		}

		dlg.setVisible(true);
	}

	public Alphabet getSelectedEvents()
	{
		return dlg.getSelectedEvents();
	}
}
