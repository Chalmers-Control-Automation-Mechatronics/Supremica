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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;

// I changed AlphabetViewer to accept Automata objects and to show the alphabets
// of all selected Automaton in the same window. That's probably what you want if
// you select more than one and request Alphabet viewing. Previously, one
// AlphabetViewer was opened for each automaton.
public class AlphabetViewer
    extends JFrame
{
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JMenuBar menuBar = new JMenuBar();
    //private AlphabetViewerPanel alphabetPanel;
    private EventsViewerPanel alphabetPanel;

    /**
     * Shows the events in the intersection of alphabetSubset and the union alphabet of <code>automata</code> 
     * and shows which of the automata in <code>automata</code> share those events.
     */
    public AlphabetViewer(Automata automata, Alphabet alphabetSubset)
    throws Exception
    {
        //this.alphabetPanel = new AlphabetViewerPanel(theAutomata);
        this.alphabetPanel = new EventsViewerPanel(automata, alphabetSubset);
        contentPane = (JPanel) getContentPane();

        // contentPane.setLayout(new BorderLayout());
        // contentPane.add(toolBar, BorderLayout.NORTH);
        setTitle("Alphabet Viewer");    // : " + theAutomaton.getName());

        // setSize(200, 500);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                setVisible(false);

                //dispose();
            }
        });

                /* Center the window
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
        contentPane.add(alphabetPanel, BorderLayout.CENTER);
    }

    private void initMenubar()
    {
        setJMenuBar(menuBar);

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
        menuBar.add(menuFile);

        // View
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        // View.Union (default, therefore initially checked)
        JRadioButtonMenuItem viewMenuUnion = new JRadioButtonMenuItem("Union", true);
        viewMenuUnion.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                alphabetPanel.showUnion();
            }
        });

        // View.Intersection
        JRadioButtonMenuItem viewMenuIntersection = new JRadioButtonMenuItem("Intersection");
        viewMenuIntersection.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                alphabetPanel.showIntersection();
            }
        });

        ButtonGroup buttongroup = new ButtonGroup();

        buttongroup.add(viewMenuUnion);
        buttongroup.add(viewMenuIntersection);
        viewMenu.add(viewMenuUnion);
        viewMenu.add(viewMenuIntersection);
        menuBar.add(viewMenu);
    }

    public void initialize()
    {}
}
