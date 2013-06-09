
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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import net.sourceforge.waters.gui.util.IconLoader;

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
        final Image image = IconLoader.ICON_APPLICATION.getImage();
        setIconImage(image);
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
