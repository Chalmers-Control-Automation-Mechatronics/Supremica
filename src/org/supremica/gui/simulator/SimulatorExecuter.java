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

import org.supremica.gui.*;
import org.supremica.log.*;
import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Project;
import org.supremica.automata.Automata;
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
import uk.ac.ic.doc.scenebeans.animation.ResetActivityCommand;



public class SimulatorExecuter
	extends JFrame
	implements AutomatonListener, AnimationListener
{
	private static Logger logger = LoggerFactory.createLogger(SimulatorExecuter.class);

	private BorderLayout layout = new BorderLayout();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private SimulatorStateViewer stateViewer;
	private SimulatorExecuterController controller;
	private AutomataSynchronizerHelper helper;
	private AutomataOnlineSynchronizer onlineSynchronizer;
	private Actions theActions;
	private Controls theControls;
	private VisualProject theProject;
	private Animator theAnimator;
	private Animation theAnimation;
	private AnimationSignals theAnimationSignals;


	public SimulatorExecuter(VisualProject theProject)
		throws Exception
	{
		this.theProject = theProject;
		this.theActions = theProject.getActions();
		this.theControls = theProject.getControls();

		theAnimator = theProject.getAnimator();
		if (theAnimator == null)
		{
			String msg = "Could not open animator: " + theProject.getAnimationPath();
			logger.error(msg);
			throw new Exception("Could not open animator: " + theProject.getAnimationPath());
		}

		theAnimation = theAnimator.getAnimation();
		theAnimation.addAnimationListener(this);

		theAnimationSignals = new AnimationSignals(theAnimation);

		SynchronizationOptions syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, false, SupremicaProperties.verboseMode(), false, true);

		helper = new AutomataSynchronizerHelper(theProject, syncOptions);

		// Build the initial state
		Automaton currAutomaton;
		State currInitialState;
		int[] initialState = new int[this.theProject.size() + 1];

		// + 1 status field
		Iterator autIt = this.theProject.iterator();

		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();
			currInitialState = currAutomaton.getInitialState();
			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
		}

		SimulatorExecuterHelper.setInitialState(initialState);

		onlineSynchronizer = new AutomataOnlineSynchronizer(helper);

		onlineSynchronizer.initialize();
		onlineSynchronizer.setCurrState(initialState);
		helper.setCoExecuter(onlineSynchronizer);
		theProject.getListeners().addListener(this);
		setBackground(Color.white);

		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(layout);

		// contentPane.add(toolBar, BorderLayout.NORTH);
		// / setTitle(theAutomaton.getName());
		setTitle("Simulator");
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
				setVisible(false);
				dispose();
			}
		});
		initMenubar();

		stateViewer = new SimulatorStateViewer(this, helper, theAnimationSignals);

		contentPane.add(stateViewer, BorderLayout.CENTER);

		// / controller = new ExplorerController(stateViewer, theAutomaton);
		controller = new SimulatorExecuterController(stateViewer, helper);

		contentPane.add(controller, BorderLayout.SOUTH);
		stateViewer.setController(controller);
		stateViewer.goToInitialState();
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

	public void updated(Object o)
	{

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


	public void animationEvent(AnimationEvent ev)
	{
		//logger.info("AnimationEvent: " + ev.getName());
	}

	public void executeEvent(LabeledEvent event)
	{
		String label = event.getLabel();

		if (theControls != null)
		{

		}

		if (theActions != null)
		{
			if (theActions.hasAction(label))
			{
				org.supremica.automata.execution.Action currAction = theActions.getAction(label);
				for (Iterator cmdIt = currAction.commandIterator(); cmdIt.hasNext(); )
				{
					String currCommand = (String)cmdIt.next();
					try
					{
						theAnimation.invokeCommand(currCommand);
					}
					catch (CommandException ex)
					{
						logger.error("Exception while executing command: " + currCommand + "\nMessage: " + ex.getMessage());
					}
				}
			}
		}
	}

	public void resetAnimation()
	{
		//logger.info("Reset animation");
		//theAnimator.reset();
	}

	public Project getProject()
	{
		return theProject;
	}

}




