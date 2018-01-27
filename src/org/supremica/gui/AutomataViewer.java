//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.treeview.AutomatonSubTree;
import org.supremica.gui.treeview.SupremicaTreeCellRenderer;
import org.supremica.gui.treeview.SupremicaTreeNode;


class AutomataViewerPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private final Automata automata;
    private final JTree theTree = new JTree();
    private final JScrollPane scrollPanel = new JScrollPane(theTree);

    public AutomataViewerPanel(final Automata automata, final boolean showalpha, final boolean showstates)
    {
        this.automata = automata;

        setLayout(new BorderLayout());
        add(scrollPanel, BorderLayout.CENTER);
        build(showalpha, showstates);
        theTree.setCellRenderer(new SupremicaTreeCellRenderer());    // EventNodeRenderer());
    }

    public void build(final boolean showalpha, final boolean showstates)
    {
        final SupremicaTreeNode root = new SupremicaTreeNode();
        final Iterator<Automaton> autit = automata.iterator();

        while (autit.hasNext())
        {
            root.add(new AutomatonSubTree(autit.next(), showalpha, showstates));
        }

        final DefaultTreeModel treeModel = new DefaultTreeModel(root);

        theTree.setModel(treeModel);
        theTree.setRootVisible(false);
        theTree.setShowsRootHandles(true);

        // theTree.setExpanded(new TreePath(node));
        revalidate();
    }

        /*
        public void setVisible(boolean toVisible)
        {
                super.setVisible(toVisible);
        }
         */
}

public class AutomataViewer
    extends JFrame
{
    private static final long serialVersionUID = 1L;
    private final JPanel contentPane;
    private final JMenuBar menuBar = new JMenuBar();
    private final AutomataViewerPanel viewerPanel;

    public AutomataViewer(final Automata automata)
    {
        this(automata, true, true);
    }

    public AutomataViewer(final Automata automata, final boolean showalpha, final boolean showstates)
    {
        setTitle("Automata Viewer");
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent e)
            {
                setVisible(false);
            }
        });

                /* Center the window
                setSize(200, 500);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension frameSize = getSize();

                if (frameSize.height > screenSize.height)
                {
                                frameSize.height = screenSize.height;
                }

                if (frameSize.width > screenSize.width)
                {
                                frameSize.width = screenSize.width;
                }

                setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
                setIconImage(Supremica.cornerImage);*/
        Utility.setupFrame(this, 200, 500);
        initMenubar();

        this.viewerPanel = new AutomataViewerPanel(automata, showalpha, showstates);
        contentPane = (JPanel) getContentPane();

        contentPane.add(viewerPanel, BorderLayout.CENTER);
    }

    private void initMenubar()
    {
        setJMenuBar(menuBar);

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

        // File menu
        final JMenu menuFile = new JMenu();
        menuFile.setText("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(menuFileClose);
        menuBar.add(menuFile);

                /*
                // View.Union (default, therefore initially checked)
                JRadioButtonMenuItem viewMenuUnion = new JRadioButtonMenuItem("Union", true);
                viewMenuUnion.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent e) {}
                });

                // View.Intersection
                JRadioButtonMenuItem viewMenuIntersection = new JRadioButtonMenuItem("Intersection");
                viewMenuIntersection.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent e) {}
                });

                // Radio button functionality?
                ButtonGroup buttongroup = new ButtonGroup();
                buttongroup.add(viewMenuUnion);
                buttongroup.add(viewMenuIntersection);

                // View menu
                JMenu viewMenu = new JMenu("View");
                viewMenu.setMnemonic(KeyEvent.VK_V);
                viewMenu.add(viewMenuUnion);
                viewMenu.add(viewMenuIntersection);
                menuBar.add(viewMenu);
                 */
    }

    public void initialize()
    {}
}
