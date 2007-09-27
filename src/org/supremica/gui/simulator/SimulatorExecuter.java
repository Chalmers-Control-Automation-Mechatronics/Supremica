
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

import org.supremica.automata.AutomataIndexMap;
import org.supremica.gui.*;
import org.supremica.log.*;
import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.supremica.automata.Arc;
import org.supremica.automata.Project;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.animators.scenebeans.Animator;
import org.supremica.automata.execution.*;
import uk.ac.ic.doc.scenebeans.event.AnimationListener;
import uk.ac.ic.doc.scenebeans.event.AnimationEvent;
import uk.ac.ic.doc.scenebeans.animation.Animation;
import uk.ac.ic.doc.scenebeans.animation.CommandException;
import org.supremica.util.SupremicaException;

public class SimulatorExecuter
    extends JFrame
    implements AutomatonListener, AnimationListener
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(SimulatorExecuter.class);
    private BorderLayout layout = new BorderLayout();
    private JPanel contentPane;
    private JMenuBar menuBar = new JMenuBar();
    private SimulatorStateViewer stateViewer;
    private SimulatorExecuterController controller;
    private AutomataSynchronizerHelper helper;
    
    //private AutomataOnlineSynchronizer onlineSynchronizer;
    private AutomataSynchronizerExecuter onlineSynchronizer;
    private Actions actions;
    private Controls controls;
    private VisualProject project;
    private Animator animator;
    private Animation animation;
    private AnimationSignals animationSignals;
    private int[] currState;
    
    public SimulatorExecuter(VisualProject project, boolean useExternalExecuter)
    throws Exception
    {
        this.project = project;
        this.actions = project.getActions();
        this.controls = project.getControls();
        animator = project.getAnimator();
        
        if (animator == null)
        {
            String msg = "Could not open animator: " + project.getAnimationURL();
            
            logger.error(msg);
            
            throw new SupremicaException("Could not open animator: " + project.getAnimationURL());
        }
        
        animation = animator.getAnimation();        
        animation.addAnimationListener(this);        
        animationSignals = new AnimationSignals(animation);
        
        SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultVerificationOptions();
        helper = new AutomataSynchronizerHelper(project, syncOptions);
        AutomataIndexMap indexMap = helper.getIndexMap();
        
        // Build the initial state
        Automaton automaton;
        State state;
        int[] initialState = AutomataIndexFormHelper.createState(this.project.size());
        Iterator autIt = this.project.iterator();
        while (autIt.hasNext())
        {
            automaton = (Automaton) autIt.next();
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
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
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
        setIconImage(Supremica.cornerImage);
        stateViewer.initialize();
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
        menuFile.add(menuFileClose);
        menuBar.add(menuFile);
        menuFileClose.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
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
    
    public void updated(Object o)
    {}
    
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
    
    public void animationEvent(AnimationEvent ev)
    {
        
        //logger.info("AnimationEvent: " + ev.getName());
    }
    
    public int[] getCurrentState()
    {
        return currState;
    }
    
    public void registerSignalObserver(SignalObserver listener)
    {
        animationSignals.registerInterest(listener);
    }
    
    public boolean isTrue(Condition theCondition)
    {
        return animationSignals.isTrue(theCondition.getLabel());
    }
    
/*
                protected void updateSignals()
                {
                                theAnimationSignals.updateSignals();
                }
 */
    public boolean executeEvent(LabeledEvent event)
    {
        String label = event.getLabel();
        
        if (controls != null)
        {}
        
        if (actions != null)
        {
            if (actions.hasAction(label))
            {
                org.supremica.automata.execution.Action currAction = actions.getAction(label);
                
                for (Iterator cmdIt = currAction.commandIterator();
                cmdIt.hasNext(); )
                {
                    Command currCommand = (Command) cmdIt.next();
                    
                    try
                    {
                        animation.invokeCommand(currCommand.getLabel());
                    }
                    catch (CommandException ex)
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
