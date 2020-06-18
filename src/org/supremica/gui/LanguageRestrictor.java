//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.algorithms.standard.Determinizer;
import org.supremica.gui.treeview.AlphabetSubTree;
import org.supremica.gui.treeview.AutomatonSubTree;
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

    public ViewerPanel()
    {}
}

class EventsViewerPanel    // compare AlphabetViewerPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private final JTree theTree = new JTree();
    private SupremicaTreeNode root = new SupremicaTreeNode();
    private final JScrollPane scrollPanel = new JScrollPane(theTree);

    private Automata automata = null;
    private final Alphabet alphabetSubset;

    EventsViewerPanel(final Automata automata, final Alphabet alphabetSubset)
    {
        this.automata = automata;
        this.alphabetSubset = alphabetSubset;

        build(false);
        init();
    }

    EventsViewerPanel(final Automata automata)
    {
        this(automata, automata.getUnionAlphabet());
    }

    private void init()
    {
        theTree.setCellRenderer(new SupremicaTreeCellRenderer());    // EventNodeRenderer());
        theTree.setSelectionModel(new EventSelectionModel());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 400));
        add(scrollPanel, BorderLayout.CENTER);
    }

    // In this panel, the EventSubTrees are at level 1 (root is
    // at 0) with the AlphabetViewerSubTrees at level 2
    // The AlphabetViewerSubTrees are only there to show what
    // automata the event belongs to should make an effort to
    // cache the AlphabetViewerSubTrees and not calc and store
    // the same subtree several times
    public void build(final boolean showroot)
    {
        // Was a good idea, but subtrees cannot be shared, I
        // guess this has to do with requiring a single parent
        // DefaultMutableTreeNode::add() says "Removes newChild
        // from its parent and makes it a child of this node"
        // So, we have to create over and oper again -- bummer.
        // EventTreeCache eventTreeCache = new EventTreeCache();
        // AutomatonTreeCache automatonTreeCache = new AutomatonTreeCache();
        // This cache is only for storing whether we have already seen the event or not
        final HashSet<LabeledEvent> eventTreeCache = new HashSet<LabeledEvent>();

        // Loop over automata and add events to dialog
        for (final Automaton aut: automata)
        {
            // Iterate over the events, for each event not already encountered, calc its subtree
            // Then add as children all the automata containing this event
            for (final LabeledEvent event: aut.getAlphabet())
            {
                if (alphabetSubset.contains(event) && !eventTreeCache.contains(event))    // if not already seen
                {
                    final EventSubTree eventsubtree = new EventSubTree(event);

                    eventTreeCache.add(event);
                    root.add(eventsubtree);

                    // Now, look for automata containing this event
                    final Iterator<?> autoit = automata.iterator();

                    while (autoit.hasNext())
                    {
                        final Automaton auto = (Automaton) autoit.next();

                        if (auto.getAlphabet().contains(event))
                        {

                            // AlphabetViewerSubTree autonode = new AlphabetViewerSubTree(auto);
                            final AutomatonSubTree autonode = new AutomatonSubTree(auto, true, false);

                            eventsubtree.add(autonode);
                        }
                    }
                }
            }
        }

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

    // Go through the tree and make hide those nodes that are not in all automata
    public void showIntersection()
    {
        // for all the (immediate) children of the root, make them visible
        for (final Enumeration<?> e = root.children(); e.hasMoreElements(); )
        {
            final EventSubTree node = (EventSubTree) e.nextElement();

            // If the number of children is not the same as the number of automata plus the number in EventSubTree
            // Then it has to be disabled/hidden/unselectable
            if (node.getChildCount() - node.numDirectLeafs() != automata.size())
            {
                node.setEnabled(false);
            }
        }

        repaint();
    }

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

class RestrictEventsViewerPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private JTree tree = null;
    private JScrollPane scrollpane = null;

    // private AutomatonSubTree root = null;
    private SupremicaTreeNode root = null;
    boolean erase = true;

    // private Automaton automaton = new Automaton("Erase These Events");
    private final Alphabet alpha = new Alphabet();

    public RestrictEventsViewerPanel()
    {
        // this.root = new AutomatonSubTree(automaton, true, false);
        // this.root = new SupremicaTreeNode("Erase These Events");
        this.root = new AlphabetSubTree(alpha);

        this.root.setUserObject("Erase These Events");

        this.tree = new JTree(root);

        //root.add(new AutomatonSubTree(new Automaton(), true, false));
        // root.add(new AlphabetSubTree(alpha));
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
        // automaton.setName("Erase These Events");
        root.setUserObject("Erase These Events");

        erase = true;

        tree.repaint();
    }

    public void keepThese()
    {
        // automaton.setName("Keep These Events");
        root.setUserObject("Keep These Events");

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

class LanguageRestrictorDialog
    extends JFrame
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(LanguageRestrictorDialog.class);
    protected Automata automata;
    @SuppressWarnings("unused")
	private final boolean doit = false;
    protected EventsViewerPanel sourceEvents;
    protected RestrictEventsViewerPanel restrictEvents;
    protected JButton okButton;
    private final Alphabet othersAlphabet;

    /**
     * @param othersAlphabet the union alphabet of the REST of the automata (not including the ones in automata).
     */
    public LanguageRestrictorDialog(final Automata automata, final Alphabet othersAlphabet)
    {
        super("Language Restrictor");

        this.automata = automata;
        this.sourceEvents = new EventsViewerPanel(automata);
        this.restrictEvents = new RestrictEventsViewerPanel();
        this.othersAlphabet = othersAlphabet;

        initMenubar();

        final JPanel okcancelpanel = new JPanel();    // default is flowlayout

        okButton = new OkButton();
        okcancelpanel.add(okButton);
        okcancelpanel.add(new CancelButton());

        //JPanel movebuttonpanel = new JPanel(new BorderLayout());
        final JPanel movebuttonpanel = new JPanel();
        movebuttonpanel.setLayout(new BoxLayout(movebuttonpanel, BoxLayout.Y_AXIS));

        //movebuttonpanel.add(new MoveButton(), BorderLayout.NORTH);
        //movebuttonpanel.add(new ChooseLocalButton(), BorderLayout.CENTER);
        //movebuttonpanel.add(new RemoveButton(), BorderLayout.SOUTH);
        movebuttonpanel.add(new MoveButton());
        movebuttonpanel.add(new ChooseLocalButton());
        movebuttonpanel.add(new RemoveButton());

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

    protected void shutWindow()
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
            setAlignmentX(JButton.CENTER_ALIGNMENT);

            setToolTipText("Do the restriction");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    doRestrict();
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
            setAlignmentX(JButton.CENTER_ALIGNMENT);

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
            setAlignmentX(JButton.CENTER_ALIGNMENT);

            setToolTipText("Add selected events to restriction alphabet");
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

    private class ChooseLocalButton
        extends JButton
    {
        private static final long serialVersionUID = 1L;

        public ChooseLocalButton()
        {
            super(">[LOCAL]>");
            setAlignmentX(JButton.CENTER_ALIGNMENT);

            setToolTipText("Selected all 'local' events");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    chooseLocal();
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
            setAlignmentX(JButton.CENTER_ALIGNMENT);
            setAlignmentY(JButton.BOTTOM_ALIGNMENT);

            setToolTipText("Remove selected events from restriction alphabet");
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

    /**
     * Select all local events, i.e. events used by no other automata.
     */
    private void chooseLocal()
    {
        // Clear selection
        final Alphabet selection = restrictEvents.getAlphabet();
        for (final LabeledEvent event: selection)
        {
            restrictEvents.remove(event);
        }
        // Select the local events
        for (final Automaton aut: automata)
        {
            final Alphabet localAlphabet = new Alphabet(aut.getAlphabet());
            final Alphabet local = localAlphabet.minus(othersAlphabet);

            for (final Automaton otherAut: automata)
            {
                if (aut != otherAut)
                {
                    local.minus(otherAut.getAlphabet());
                }
            }

            for (final LabeledEvent event: local)
            {
                if (event.isObservable())
                {
                    restrictEvents.add(event);
                }
            }
        }
        restrictEvents.eraseThese();
        restrictEvents.rebuild();
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

    // Return the the restrictEvents
    public Alphabet getRestrictionAlphabet()
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

        // View.Intersection
        final JRadioButtonMenuItem viewMenuIntersection = new JRadioButtonMenuItem("Intersection");

        viewMenuIntersection.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                sourceEvents.showIntersection();
            }
        });

        final ButtonGroup buttongroup = new ButtonGroup();

        buttongroup.add(viewMenuUnion);
        buttongroup.add(viewMenuIntersection);
        viewMenu.add(viewMenuUnion);
        viewMenu.add(viewMenuIntersection);

        // For the moment, until we get the rendering etc fixed
        // viewMenuUnion.setEnabled(false);
        // viewMenuIntersection.setEnabled(false);

        // Restrict
        final JMenu restrictMenu = new JMenu("Restrict");
        restrictMenu.setMnemonic(KeyEvent.VK_R);
        // Restrict.Erase These Events (default, therefore initially checked)
        final JRadioButtonMenuItem restrictMenuErase = new JRadioButtonMenuItem("Erase These Events", true);
        restrictMenuErase.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                restrictEvents.eraseThese();
            }
        });
        // Restrict.Keep These Events
        final JRadioButtonMenuItem restrictMenuKeep = new JRadioButtonMenuItem("Keep These Events");
        restrictMenuKeep.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                restrictEvents.keepThese();
            }
        });

        final ButtonGroup restrictGroup = new ButtonGroup();
        restrictGroup.add(restrictMenuErase);
        restrictGroup.add(restrictMenuKeep);
        restrictMenu.add(restrictMenuErase);
        restrictMenu.add(restrictMenuKeep);
        restrictMenuErase.setEnabled(true);
        restrictMenuKeep.setEnabled(true);

        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(viewMenu);
        menuBar.add(restrictMenu);
        setJMenuBar(menuBar);
    }

    protected void doRestrict()
    {
        // Get the restriction alphabet
        final Alphabet alpha = restrictEvents.getAlphabet();
        final Automata newautomata = new Automata();
        final Iterator<?> autit = automata.iterator();

        while (autit.hasNext())
        {
            final Automaton automaton = (Automaton) autit.next();
            final Determinizer detm = new Determinizer(automaton, alpha, restrictEvents.toErase());

            detm.execute();

            final Automaton newautomaton = detm.getNewAutomaton();

            if (restrictEvents.toErase())
            {
                newautomaton.setComment(automaton.getName() + "\\" +
                    AlphabetHelpers.intersect(alpha, automaton.getAlphabet()));
            }
            else
            {
                newautomaton.setComment(automaton.getName() + "\\" +
                    AlphabetHelpers.minus(automaton.getAlphabet(), alpha));
            }

            newautomata.addAutomaton(newautomaton);
        }

        // Shut the window!!
        shutWindow();

        try
        {
            ActionMan.gui.addAutomata(newautomata);
        }
        catch (final Exception ex)
        {
            logger.debug("LanguageRestriction::doRestrict() -- ", ex);
            logger.debug(ex.getStackTrace());
        }
    }
}

public class LanguageRestrictor
    extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    public LanguageRestrictor()
    {
        putValue(NAME, "Language Restriction");
        putValue(SHORT_DESCRIPTION, "Restrict the language to a subset of the alphabet");
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
        // Get the selected automata
        final Automata automata = ActionMan.getGui().getSelectedAutomata();
        // Throw up the dialog, let the user select the alphabet
        new LanguageRestrictorDialog(automata, ActionMan.getGui().getUnselectedAutomata().getUnionAlphabet());
    }
}
