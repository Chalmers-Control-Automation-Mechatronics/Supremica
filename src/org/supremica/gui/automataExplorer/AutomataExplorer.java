//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.automataExplorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.gui.Utility;

public class AutomataExplorer
    extends JFrame
    implements AutomatonListener
{
    private static final long serialVersionUID = 1L;
    private final Automata theAutomata;
    private final BorderLayout layout = new BorderLayout();
    private final JPanel contentPane;
    private final JMenuBar menuBar = new JMenuBar();
    private final AutomataStateViewer stateViewer;
    private final AutomataExplorerController controller;
    private final AutomataSynchronizerHelper helper;

    private final AutomataSynchronizerExecuter onlineSynchronizer;
    private final AutomataIndexMap indexMap;

    public AutomataExplorer(final Automata theAutomata)
    throws Exception
    {
        this.theAutomata = theAutomata;

        // Get current options
        final SynchronizationOptions syncOptions = new SynchronizationOptions();
        syncOptions.setBuildAutomaton(false);
        syncOptions.setRequireConsistentControllability(false);

        // Get helper
        helper = new AutomataSynchronizerHelper(theAutomata, syncOptions, false);

        indexMap = new AutomataIndexMap(theAutomata);

        // Build the initial state
        Automaton currAutomaton;
        State currInitialState;
        final int[] initialState = AutomataIndexFormHelper.createState(this.theAutomata.size());

        // + 1 status field
        final Iterator<Automaton> autIt = this.theAutomata.iterator();

        while (autIt.hasNext())
        {
            currAutomaton = autIt.next();
            currInitialState = currAutomaton.getInitialState();
//			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
            initialState[indexMap.getAutomatonIndex(currAutomaton)] = indexMap.getStateIndex(currAutomaton, currInitialState);
        }

        AutomataExplorerHelper.setInitialState(initialState);

        //onlineSynchronizer = new AutomataOnlineSynchronizer(helper);
        onlineSynchronizer = new AutomataSynchronizerExecuter(helper);
        onlineSynchronizer.initialize();
        onlineSynchronizer.setCurrState(initialState);
        helper.setCoExecuter(onlineSynchronizer);
        theAutomata.getListeners().addListener(this);
        setBackground(Color.white);

        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(layout);

        // contentPane.add(toolBar, BorderLayout.NORTH);
        setTitle("AutomataExplorer");
        Utility.setupFrame(this, 400, 500);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent e)
            {
                setVisible(false);

                //dispose();
            }
        });
        initMenubar();

        // / stateViewer = new StateViewer(theAutomaton);
        stateViewer = new AutomataStateViewer(helper);
        contentPane.add(stateViewer, BorderLayout.CENTER);

        // / controller = new ExplorerController(stateViewer, theAutomaton);
        controller = new AutomataExplorerController(stateViewer, helper);

        contentPane.add(controller, BorderLayout.SOUTH);
        stateViewer.setController(controller);
        stateViewer.goToInitialState();
    }

    public void initialize()
    {
        final List<Image> images = IconAndFontLoader.ICONLIST_APPLICATION;
        setIconImages(images);
        stateViewer.initialize();
    }

    private void initMenubar()
    {
        setJMenuBar(menuBar);

        // File
        final JMenu menuFile = new JMenu();

        menuFile.setText("File");
        menuFile.setMnemonic(KeyEvent.VK_F);

        // File.Close
        final JMenuItem menuFileClose = new JMenuItem();

        menuFileClose.setText("Close");
        menuFile.add(menuFileClose);
        menuBar.add(menuFile);
        menuFileClose.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                setVisible(false);

                //dispose();
            }
        });
    }

    @Override
    public void updated(final Object o)
    {
    }

    @Override
    public void stateAdded(final Automaton aut, final State q)
    {
        updated(aut);
    }

    @Override
    public void stateRemoved(final Automaton aut, final State q)
    {
        updated(aut);
    }

    @Override
    public void arcAdded(final Automaton aut, final Arc a)
    {
        updated(aut);
    }

    @Override
    public void arcRemoved(final Automaton aut, final Arc a)
    {
        updated(aut);
    }

    @Override
    public void attributeChanged(final Automaton aut)
    {
        updated(aut);
    }

    @Override
    public void automatonRenamed(final Automaton aut, final String oldName)
    {
        updated(aut);
    }
}
