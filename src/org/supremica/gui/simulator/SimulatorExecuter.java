
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

public class SimulatorExecuter
	extends JFrame
	implements AutomatonListener
{
	private Automata theAutomata;
	private BorderLayout layout = new BorderLayout();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private SimulatorStateViewer stateViewer;
	private SimulatorExecuterController controller;
	private AutomataSynchronizerHelper helper;
	private AutomataOnlineSynchronizer onlineSynchronizer;

	public SimulatorExecuter(Automata theAutomata)
		throws Exception
	{
		this.theAutomata = theAutomata;

		SynchronizationOptions syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, false, SupremicaProperties.verboseMode(), false, true);

		helper = new AutomataSynchronizerHelper(theAutomata, syncOptions);

		// Build the initial state
		Automaton currAutomaton;
		State currInitialState;
		int[] initialState = new int[this.theAutomata.size() + 1];

		// + 1 status field
		Iterator autIt = this.theAutomata.iterator();

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
		theAutomata.getListeners().addListener(this);
		setBackground(Color.white);

		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(layout);

		// contentPane.add(toolBar, BorderLayout.NORTH);
		// / setTitle(theAutomaton.getName());
		setTitle("Animation Executer");
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

		// / stateViewer = new StateViewer(theAutomaton);
		stateViewer = new SimulatorStateViewer(helper);

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

		/*
		 *  Trams. FIXA!
		 *  if (o == theAutomata)
		 *  {
		 *  stateViewer.goToInitialState();
		 *  }
		 */
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
}

//DEL//class SimulatorStateViewer
//DEL//	extends JPanel
//DEL//{
//DEL//	private Automata theAutomata;
//DEL//	private AutomataSynchronizerHelper helper;
//DEL//	private int[] currState;
//DEL//	private SimulatorEventList forwardEvents;
//DEL//	private SimulatorEventList backwardEvents;
//DEL//	private SimulatorExecuterController controller;
//DEL//	private AutomataStateDisplayer stateDisplayer;
//DEL//	private JSplitPane eventSplitter;
//DEL//	private JSplitPane stateEventSplitter;
//DEL//	private LinkedList prevStates = new LinkedList();
//DEL//	private LinkedList nextStates = new LinkedList();
//DEL//
//DEL//	public SimulatorStateViewer(AutomataSynchronizerHelper helper)
//DEL//	{
//DEL//		setLayout(new BorderLayout());
//DEL//
//DEL//		theAutomata = helper.getAutomata();
//DEL//		this.helper = helper;
//DEL//		forwardEvents = new SimulatorEventList(this, helper, true);
//DEL//		backwardEvents = new SimulatorEventList(this, helper, false);
//DEL//
//DEL//		eventSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forwardEvents, backwardEvents);
//DEL//		stateDisplayer = new AutomataStateDisplayer(this, helper);
//DEL//		stateEventSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stateDisplayer, eventSplitter);
//DEL//
//DEL//		add(stateEventSplitter, BorderLayout.CENTER);
//DEL//	}
//DEL//
//DEL//	public void initialize()
//DEL//	{
//DEL//		eventSplitter.setDividerLocation(0.5);
//DEL//		stateEventSplitter.setDividerLocation(0.6);
//DEL//	}
//DEL//
//DEL//	public void setCurrState(int[] newState)
//DEL//	{
//DEL//		setCurrState(newState, false);
//DEL//	}
//DEL//
//DEL//	private void setCurrState(int[] newState, boolean isUndo)
//DEL//	{
//DEL//		if (!isUndo)
//DEL//		{
//DEL//			if (currState != null)
//DEL//			{
//DEL//				prevStates.addLast(currState);
//DEL//			}
//DEL//
//DEL//			nextStates.clear();
//DEL//		}
//DEL//
//DEL//		currState = newState;
//DEL//
//DEL//		update();
//DEL//	}
//DEL//
//DEL//	public void goToInitialState()
//DEL//	{
//DEL//		prevStates.clear();
//DEL//
//DEL//		currState = null;
//DEL//
//DEL//		helper.getCoExecuter().setCurrState(SimulatorExecuterHelper.getInitialState());
//DEL//		setCurrState(SimulatorExecuterHelper.getInitialState(), false);
//DEL//	}
//DEL//
//DEL//	public void undoState()
//DEL//	{
//DEL//		if (prevStates.size() > 0)
//DEL//		{
//DEL//			int[] newState = (int[]) prevStates.removeLast();
//DEL//
//DEL//			nextStates.addFirst(currState);
//DEL//			setCurrState(newState, true);
//DEL//		}
//DEL//	}
//DEL//
//DEL//	public boolean undoEnabled()
//DEL//	{
//DEL//		return prevStates.size() > 0;
//DEL//	}
//DEL//
//DEL//	public void redoState()
//DEL//	{
//DEL//		if (nextStates.size() > 0)
//DEL//		{
//DEL//			int[] newState = (int[]) nextStates.removeFirst();
//DEL//
//DEL//			prevStates.addLast(currState);
//DEL//			setCurrState(newState, true);
//DEL//		}
//DEL//	}
//DEL//
//DEL//	public boolean redoEnabled()
//DEL//	{
//DEL//		return nextStates.size() > 0;
//DEL//	}
//DEL//
//DEL//	public void update()
//DEL//	{
//DEL//
//DEL//		// The order of theese are changed, for states to be properly forbidden...
//DEL//		forwardEvents.setCurrState(currState);
//DEL//		backwardEvents.setCurrState(currState);
//DEL//		stateDisplayer.setCurrState(currState);
//DEL//		controller.update();
//DEL//	}
//DEL//
//DEL//	public void setController(SimulatorExecuterController controller)
//DEL//	{
//DEL//		this.controller = controller;
//DEL//	}
//DEL//}

//DEL//class SimulatorEventList
//DEL//	extends JPanel
//DEL//{
//DEL//	private boolean forward;
//DEL//	private boolean showStateId = false;
//DEL//	private Automata theAutomata;
//DEL//	private int[] currState;
//DEL//	private SimulatorStateViewer stateViewer;
//DEL//	private SimulatorEventListModel eventsList;
//DEL//	private JList theList;
//DEL//
//DEL//	public SimulatorEventList(SimulatorStateViewer stateViewer, AutomataSynchronizerHelper helper, boolean forward)
//DEL//	{
//DEL//		setLayout(new BorderLayout());
//DEL//
//DEL//		this.stateViewer = stateViewer;
//DEL//		this.theAutomata = helper.getAutomata();
//DEL//		this.forward = forward;
//DEL//		eventsList = new SimulatorEventListModel(helper, forward);
//DEL//		theList = new JList(eventsList);
//DEL//
//DEL//		JScrollPane scrollPanel = new JScrollPane(theList);
//DEL//
//DEL//		theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//DEL//
//DEL//		String label;
//DEL//
//DEL//		if (forward)
//DEL//		{
//DEL//			label = "Outgoing events";
//DEL//		}
//DEL//		else
//DEL//		{
//DEL//			label = "Incoming events";
//DEL//		}
//DEL//
//DEL//		JLabel jLabel = new JLabel(label);
//DEL//
//DEL//		// jLabel.setOpaque(true);
//DEL//		// jLabel.setBackground(Color.yellow);
//DEL//		add(jLabel, BorderLayout.NORTH);
//DEL//		add(scrollPanel, BorderLayout.CENTER);
//DEL//		theList.addMouseListener(new MouseAdapter()
//DEL//		{
//DEL//			public void mouseClicked(MouseEvent e)
//DEL//			{
//DEL//				if (e.getClickCount() == 2)
//DEL//				{
//DEL//					int index = theList.locationToIndex(e.getPoint());
//DEL//
//DEL//					if (index >= 0)
//DEL//					{
//DEL//						int[] newState = eventsList.getStateAt(index);
//DEL//
//DEL//						updateStateViewer(newState);
//DEL//					}
//DEL//				}
//DEL//			}
//DEL//		});
//DEL//	}
//DEL//
//DEL//	public void setShowStateId(boolean showStateId)
//DEL//	{
//DEL//		eventsList.setShowStateId(showStateId);
//DEL//	}
//DEL//
//DEL//	public void setCurrState(int[] currState)
//DEL//	{
//DEL//		this.currState = currState;
//DEL//
//DEL//		theList.clearSelection();
//DEL//		update();
//DEL//	}
//DEL//
//DEL//	public void update()
//DEL//	{
//DEL//		eventsList.setCurrState(currState);
//DEL//	}
//DEL//
//DEL//	private void updateStateViewer(int[] newState)
//DEL//	{
//DEL//		stateViewer.setCurrState(newState);
//DEL//	}
//DEL//}

//DEL//class SimulatorEventListModel
//DEL//	extends AbstractListModel
//DEL//{
//DEL//	private int[] currState;
//DEL//
//DEL//	// / private ArrayList currArcs = new ArrayList();
//DEL//	private int[] events;
//DEL//	private int eventAmount = 0;
//DEL//	private boolean forward;
//DEL//	private Automata theAutomata;
//DEL//	private Alphabet theAlphabet;
//DEL//	private boolean showState = false;
//DEL//	private AutomataSynchronizerHelper helper;
//DEL//
//DEL//	public SimulatorEventListModel(AutomataSynchronizerHelper helper, boolean forward)
//DEL//	{
//DEL//		this.forward = forward;
//DEL//		this.helper = helper;
//DEL//		this.theAutomata = helper.getAutomata();
//DEL//		this.theAlphabet = helper.getAutomaton().getAlphabet();
//DEL//	}
//DEL//
//DEL//	public void setCurrState(int[] currState)
//DEL//	{
//DEL//		this.currState = currState;
//DEL//
//DEL//		update();
//DEL//	}
//DEL//
//DEL//	public void setShowStateId(boolean showState)
//DEL//	{
//DEL//		this.showState = showState;
//DEL//	}
//DEL//
//DEL//	public void update()
//DEL//	{
//DEL//		AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();
//DEL//
//DEL//		if (forward)
//DEL//		{
//DEL//			events = onlineSynchronizer.getOutgoingEvents(currState);
//DEL//		}
//DEL//		else
//DEL//		{
//DEL//			events = onlineSynchronizer.getIncomingEvents(currState);
//DEL//		}
//DEL//
//DEL//		eventAmount = 0;
//DEL//
//DEL//		while (events[eventAmount] != Integer.MAX_VALUE)
//DEL//		{
//DEL//			eventAmount++;
//DEL//		}
//DEL//
//DEL//		fireContentsChanged(this, 0, eventAmount - 1);
//DEL//	}
//DEL//
//DEL//	public int getSize()
//DEL//	{
//DEL//		return eventAmount;
//DEL//	}
//DEL//
//DEL//	public Object getElementAt(int index)
//DEL//	{
//DEL//		org.supremica.automata.LabeledEvent currEvent;
//DEL//
//DEL//		try
//DEL//		{
//DEL//			currEvent = theAlphabet.getEventWithIndex(events[index]);
//DEL//		}
//DEL//		catch (Exception e)
//DEL//		{
//DEL//			System.err.println("Error: Could not find event in alphabet!\n");
//DEL//
//DEL//			return null;
//DEL//		}
//DEL//
//DEL//		StringBuffer responseString = new StringBuffer();
//DEL//
//DEL//		if (!currEvent.isControllable())
//DEL//		{
//DEL//			responseString.append("!");
//DEL//		}
//DEL//
//DEL//		responseString.append(currEvent.getLabel());
//DEL//
//DEL//		return responseString.toString();
//DEL//	}
//DEL//
//DEL//	public int[] getStateAt(int index)
//DEL//	{
//DEL//		AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();
//DEL//
//DEL//		return onlineSynchronizer.doTransition(events[index]);
//DEL//	}
//DEL//}


//DEL//class SimulatorExecuterController
//DEL//	extends JPanel
//DEL//{
//DEL//	private SimulatorStateViewer stateViewer;
//DEL//	private Automata theAutomata;
//DEL//	private JButton undoButton;
//DEL//	private JButton redoButton;
//DEL//
//DEL//	public SimulatorExecuterController(SimulatorStateViewer stateViewer, AutomataSynchronizerHelper synchHelper)
//DEL//	{
//DEL//		setLayout(new BorderLayout());
//DEL//
//DEL//		this.stateViewer = stateViewer;
//DEL//		this.theAutomata = synchHelper.getAutomata();
//DEL//
//DEL//		Box redoBox = new Box(BoxLayout.X_AXIS);
//DEL//
//DEL//		ImageIcon forwardImg = new ImageIcon(SimulatorExecuterController.class.getResource("/toolbarButtonGraphics/navigation/Forward24.gif"));
//DEL//		ImageIcon backwardImg = new ImageIcon(SimulatorExecuterController.class.getResource("/toolbarButtonGraphics/navigation/Back24.gif"));
//DEL//		ImageIcon homeImg = new ImageIcon(SimulatorExecuterController.class.getResource("/toolbarButtonGraphics/navigation/Home24.gif"));
//DEL//
//DEL//		undoButton = new JButton(backwardImg);
//DEL//		undoButton.setToolTipText("Back");
//DEL//		redoButton = new JButton(forwardImg);
//DEL//		redoButton.setToolTipText("Forward");
//DEL//		JButton resetButton = new JButton(homeImg);
//DEL//		resetButton.setToolTipText("Go to the initial state");
//DEL//
//DEL//		redoBox.add(Box.createHorizontalGlue());
//DEL//		redoBox.add(Box.createHorizontalGlue());
//DEL//		redoBox.add(undoButton);
//DEL//		redoBox.add(Box.createHorizontalGlue());
//DEL//		redoBox.add(redoButton);
//DEL//		redoBox.add(Box.createHorizontalGlue());
//DEL//		redoBox.add(resetButton);
//DEL//		redoBox.add(Box.createHorizontalGlue());
//DEL//		redoBox.add(Box.createHorizontalGlue());
//DEL//
//DEL//		add(redoBox, BorderLayout.NORTH);
//DEL//
//DEL//		undoButton.addActionListener(new ActionListener()
//DEL//		{
//DEL//			public void actionPerformed(ActionEvent e)
//DEL//			{
//DEL//				undo_actionPerformed(e);
//DEL//			}
//DEL//		});
//DEL//		redoButton.addActionListener(new ActionListener()
//DEL//		{
//DEL//			public void actionPerformed(ActionEvent e)
//DEL//			{
//DEL//				redo_actionPerformed(e);
//DEL//			}
//DEL//		});
//DEL//		resetButton.addActionListener(new ActionListener()
//DEL//		{
//DEL//			public void actionPerformed(ActionEvent e)
//DEL//			{
//DEL//				reset_actionPerformed(e);
//DEL//			}
//DEL//		});
//DEL//	}
//DEL//
//DEL//	public void reset_actionPerformed(ActionEvent e)
//DEL//	{
//DEL//		stateViewer.goToInitialState();
//DEL//
//DEL//		// stateViewer.initialize();
//DEL//	}
//DEL//
//DEL//	public void undo_actionPerformed(ActionEvent e)
//DEL//	{
//DEL//		stateViewer.undoState();
//DEL//	}
//DEL//
//DEL//	public void redo_actionPerformed(ActionEvent e)
//DEL//	{
//DEL//		stateViewer.redoState();
//DEL//	}
//DEL//
//DEL//	public void update()
//DEL//	{
//DEL//		undoButton.setEnabled(stateViewer.undoEnabled());
//DEL//		redoButton.setEnabled(stateViewer.redoEnabled());
//DEL//	}
//DEL//}

//DEL//class SimulatorExecuterHelper
//DEL//{
//DEL//	private static int[] initialState;
//DEL//
//DEL//	public static void setInitialState(int[] state)
//DEL//	{
//DEL//		initialState = state;
//DEL//	}
//DEL//
//DEL//	public static int[] getInitialState()
//DEL//	{
//DEL//
//DEL//		// return (int[]) initialState.clone();
//DEL//		return initialState;
//DEL//	}
//DEL//}

