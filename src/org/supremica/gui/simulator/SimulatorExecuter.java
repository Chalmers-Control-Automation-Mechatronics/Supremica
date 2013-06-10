
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
package org.supremica.gui.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
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
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.execution.Actions;
import org.supremica.automata.execution.Command;
import org.supremica.automata.execution.Condition;
import org.supremica.automata.execution.Controls;
import org.supremica.gui.VisualProject;
import org.supremica.gui.animators.scenebeans.Animator;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.SupremicaException;

import uk.ac.ic.doc.scenebeans.animation.Animation;
import uk.ac.ic.doc.scenebeans.animation.CommandException;
import uk.ac.ic.doc.scenebeans.event.AnimationEvent;
import uk.ac.ic.doc.scenebeans.event.AnimationListener;

public class SimulatorExecuter
    extends JFrame
    implements AutomatonListener, AnimationListener
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(SimulatorExecuter.class);
    private final BorderLayout layout = new BorderLayout();
    private final JPanel contentPane;
    private final JMenuBar menuBar = new JMenuBar();
    private final SimulatorStateViewer stateViewer;
    private final SimulatorExecuterController controller;
    private final AutomataSynchronizerHelper helper;

    //private AutomataOnlineSynchronizer onlineSynchronizer;
    private final AutomataSynchronizerExecuter onlineSynchronizer;
    private final Actions actions;
    private final Controls controls;
    private final VisualProject project;
    private final Animator animator;
    private final Animation animation;
    private final AnimationSignals animationSignals;
    private int[] currState;

    public SimulatorExecuter(final VisualProject project, final boolean useExternalExecuter)
    throws Exception
    {
        this.project = project;
        this.actions = project.getActions();
        this.controls = project.getControls();
        animator = project.getAnimator();

        if (animator == null)
        {
            final String msg = "Could not open animator: " + project.getAnimationURL();

            logger.error(msg);

            throw new SupremicaException("Could not open animator: " + project.getAnimationURL());
        }

        animation = animator.getAnimation();
        animation.addAnimationListener(this);
        animationSignals = new AnimationSignals(animation);

        final SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultVerificationOptions();
        helper = new AutomataSynchronizerHelper(project, syncOptions, false);
        final AutomataIndexMap indexMap = helper.getIndexMap();

        // Build the initial state
        Automaton automaton;
        State state;
        final int[] initialState = AutomataIndexFormHelper.createState(this.project.size());
        final Iterator<Automaton> autIt = this.project.iterator();
        while (autIt.hasNext())
        {
            automaton = autIt.next();
            state = automaton.getInitialState();
            //initialState[automaton.getIndex()] = state.getIndex();
            initialState[indexMap.getAutomatonIndex(automaton)] = indexMap.getStateIndex(automaton, state);
        }
        // Set initial state in helper
        SimulatorExecuterHelper.setInitialState(initialState);

        //onlineSynchronizer = new AutomataOnlineSynchronizer(helper);
        onlineSynchronizer = new AutomataSynchronizerExecuter(helper);
        onlineSynchronizer.initialize();
        onlineSynchronizer.setCurrState(initialState);

        currState = initialState;

        helper.setCoExecuter(onlineSynchronizer);

        ////////////////
        // SET UP GUI //
        ////////////////

        //theProject.getListeners().addListener(this);
        setBackground(Color.white);

        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(layout);

        // contentPane.add(toolBar, BorderLayout.NORTH);
        // setTitle(theAutomaton.getName());
        setTitle("Supremica Simulator");
        setSize(400, 500);

        // Center the window
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = getSize();

        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }

        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }

        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent e)
            {
                close();
            }
        });
        initMenubar();

        stateViewer = new SimulatorStateViewer(this, helper, useExternalExecuter);
        contentPane.add(stateViewer, BorderLayout.CENTER);

        //controller = new ExplorerController(stateViewer, theAutomaton);
        controller = new SimulatorExecuterController(stateViewer, useExternalExecuter);

        contentPane.add(controller, BorderLayout.SOUTH);
        stateViewer.setController(controller);

        //stateViewer.goToInitialState();
        update();
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
                dispose();
            }
        });
    }

    private void close()
    {
        setVisible(false);

        if (stateViewer != null)
        {
            stateViewer.close();
        }

        dispose();
    }

    @Override
    public void updated(final Object o)
    {}

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

    @Override
    public void animationEvent(final AnimationEvent ev)
    {

        //logger.info("AnimationEvent: " + ev.getName());
    }

    public int[] getCurrentState()
    {
        return currState;
    }

    public void registerSignalObserver(final SignalObserver listener)
    {
        animationSignals.registerInterest(listener);
    }

    public boolean isTrue(final Condition theCondition)
    {
        return animationSignals.isTrue(theCondition.getLabel());
    }

/*
                protected void updateSignals()
                {
                                theAnimationSignals.updateSignals();
                }
 */
    public boolean executeEvent(final LabeledEvent event)
    {
        final String label = event.getLabel();

        if (controls != null)
        {}

        if (actions != null)
        {
            if (actions.hasAction(label))
            {
                final org.supremica.automata.execution.Action currAction = actions.getAction(label);

                for (final Iterator<Command> cmdIt = currAction.commandIterator();
                cmdIt.hasNext(); )
                {
                    final Command currCommand = cmdIt.next();

                    try
                    {
                        animation.invokeCommand(currCommand.getLabel());
                    }
                    catch (final CommandException ex)
                    {
                        logger.error("Exception while executing command: " + currCommand + "\nMessage: " + ex.getMessage());
                        logger.debug(ex.getStackTrace());
                    }
                }
            }
        }

        // Update the state here
        onlineSynchronizer.setCurrState(currState);

        if (onlineSynchronizer.isEnabled(event))
        {
            currState = onlineSynchronizer.doTransition(currState, event);

            // return onlineSynchronizer.doTransition(events[index]);
            update();
        }
        else
        {
            logger.error("The event " + event + " is not enabled");
        }

        return currState != null;
    }

    public void resetAnimation()
    {

        //logger.info("Reset animation");
        //theAnimator.reset();
    }

    public Project getProject()
    {
        return project;
    }

    public void update()
    {

        //theAnimationSignals.notifyObservers();
    }
}
