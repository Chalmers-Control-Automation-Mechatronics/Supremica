
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
package org.supremica.gui;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.EventLabel;

public class AutomataExplorer
	extends JFrame
	implements AutomatonListener
{
	private Automata theAutomata;
	private BorderLayout layout = new BorderLayout();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private AutomataStateViewer stateViewer;
	private AutomataExplorerController controller;
	private AutomataSynchronizerHelper helper;
	private AutomataOnlineSynchronizer onlineSynchronizer;

	public AutomataExplorer(Automata theAutomata)
		throws Exception
	{
		this.theAutomata = theAutomata;

		SynchronizationOptions syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, false, SupremicaProperties.verboseMode());

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

		AutomataExplorerHelper.setInitialState(initialState);

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
		setTitle("AutomataExplorer");
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
}

class AutomataStateViewer
	extends JPanel
{
	private Automata theAutomata;
	private AutomataSynchronizerHelper helper;
	private int[] currState;
	private AutomataEventList forwardEvents;
	private AutomataEventList backwardEvents;
	private AutomataExplorerController controller;
	private AutomataStateDisplayer stateDisplayer;
	private JSplitPane eventSplitter;
	private JSplitPane stateEventSplitter;
	private LinkedList prevStates = new LinkedList();
	private LinkedList nextStates = new LinkedList();

	public AutomataStateViewer(AutomataSynchronizerHelper helper)
	{
		setLayout(new BorderLayout());

		theAutomata = helper.getAutomata();
		this.helper = helper;
		forwardEvents = new AutomataEventList(this, helper, true);
		backwardEvents = new AutomataEventList(this, helper, false);

		// / backwardEvents.setShowStateId(true); // Svårlöst? FIXA!!
		eventSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forwardEvents, backwardEvents);
		stateDisplayer = new AutomataStateDisplayer(this, helper);
		stateEventSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stateDisplayer, eventSplitter);

		add(stateEventSplitter, BorderLayout.CENTER);
	}

	public void initialize()
	{
		eventSplitter.setDividerLocation(0.5);
		stateEventSplitter.setDividerLocation(0.6);
	}

	public void setCurrState(int[] newState)
	{
		setCurrState(newState, false);
	}

	private void setCurrState(int[] newState, boolean isUndo)
	{
		if (!isUndo)
		{
			if (currState != null)
			{
				prevStates.addLast(currState);
			}

			nextStates.clear();
		}

		currState = newState;

		update();
	}

	public void goToInitialState()
	{
		prevStates.clear();

		currState = null;

		helper.getCoExecuter().setCurrState(AutomataExplorerHelper.getInitialState());
		setCurrState(AutomataExplorerHelper.getInitialState(), false);
	}

	public void undoState()
	{
		if (prevStates.size() > 0)
		{
			int[] newState = (int[]) prevStates.removeLast();

			nextStates.addFirst(currState);
			setCurrState(newState, true);
		}
	}

	public boolean undoEnabled()
	{
		return prevStates.size() > 0;
	}

	public void redoState()
	{
		if (nextStates.size() > 0)
		{
			int[] newState = (int[]) nextStates.removeFirst();

			prevStates.addLast(currState);
			setCurrState(newState, true);
		}
	}

	public boolean redoEnabled()
	{
		return nextStates.size() > 0;
	}

	public void update()
	{

		// The order of theese are changed, for states to be properly forbidden...
		forwardEvents.setCurrState(currState);
		backwardEvents.setCurrState(currState);
		stateDisplayer.setCurrState(currState);
		controller.update();
	}

	public void setController(AutomataExplorerController controller)
	{
		this.controller = controller;
	}
}

class AutomataEventList
	extends JPanel
{
	private boolean forward;
	private boolean showStateId = false;
	private Automata theAutomata;
	private int[] currState;
	private AutomataStateViewer stateViewer;
	private AutomataEventListModel eventsList;
	private JList theList;

	public AutomataEventList(AutomataStateViewer stateViewer, AutomataSynchronizerHelper helper, boolean forward)
	{
		setLayout(new BorderLayout());

		this.stateViewer = stateViewer;
		this.theAutomata = helper.getAutomata();
		this.forward = forward;
		eventsList = new AutomataEventListModel(helper, forward);
		theList = new JList(eventsList);

		JScrollPane scrollPanel = new JScrollPane(theList);

		theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		String label;

		if (forward)
		{
			label = "Outgoing events";
		}
		else
		{
			label = "Incoming events";
		}

		JLabel jLabel = new JLabel(label);

		// jLabel.setOpaque(true);
		// jLabel.setBackground(Color.yellow);
		add(jLabel, BorderLayout.NORTH);
		add(scrollPanel, BorderLayout.CENTER);
		theList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int index = theList.locationToIndex(e.getPoint());

					if (index >= 0)
					{
						int[] newState = eventsList.getStateAt(index);

						updateStateViewer(newState);
					}
				}
			}
		});
	}

	public void setShowStateId(boolean showStateId)
	{
		eventsList.setShowStateId(showStateId);
	}

	public void setCurrState(int[] currState)
	{
		this.currState = currState;

		theList.clearSelection();
		update();
	}

	public void update()
	{
		eventsList.setCurrState(currState);
	}

	private void updateStateViewer(int[] newState)
	{
		stateViewer.setCurrState(newState);
	}
}

class AutomataEventListModel
	extends AbstractListModel
{
	private int[] currState;

	// / private ArrayList currArcs = new ArrayList();
	private int[] events;
	private int eventAmount = 0;
	private boolean forward;
	private Automata theAutomata;
	private Alphabet theAlphabet;
	private boolean showState = false;
	private AutomataSynchronizerHelper helper;

	public AutomataEventListModel(AutomataSynchronizerHelper helper, boolean forward)
	{
		this.forward = forward;
		this.helper = helper;
		this.theAutomata = helper.getAutomata();
		this.theAlphabet = helper.getAutomaton().getAlphabet();
	}

	public void setCurrState(int[] currState)
	{
		this.currState = currState;

		update();
	}

	public void setShowStateId(boolean showState)
	{
		this.showState = showState;
	}

	public void update()
	{
		AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();

		if (forward)
		{
			events = onlineSynchronizer.getOutgoingEvents(currState);
		}
		else
		{
			events = onlineSynchronizer.getIncomingEvents(currState);
		}

		eventAmount = 0;

		while (events[eventAmount] != Integer.MAX_VALUE)
		{
			eventAmount++;
		}

		fireContentsChanged(this, 0, eventAmount - 1);

		/*
		 *  Iterator arcIt;
		 *  if (forward)
		 *  {
		 *  arcIt = currState.outgoingArcsIterator();
		 *  }
		 *  else
		 *  {
		 *  arcIt = currState.incomingArcsIterator();
		 *  }
		 *  currArcs.clear();
		 *  while (arcIt.hasNext())
		 *  {
		 *  Arc currArc = (Arc)arcIt.next();
		 *  currArcs.add(currArc);
		 *  }
		 *  fireContentsChanged(this, 0, currArcs.size() - 1);
		 */
	}

	public int getSize()
	{
		return eventAmount;
	}

	public Object getElementAt(int index)
	{
		org.supremica.automata.EventLabel currEvent;

		try
		{
			currEvent = theAlphabet.getEventWithIndex(events[index]);
		}
		catch (Exception e)
		{
			System.err.println("Error: Could not find event in alphabet!\n");

			return null;
		}

		StringBuffer responseString = new StringBuffer();

		if (!currEvent.isControllable())
		{
			responseString.append("!");
		}

		responseString.append(currEvent.getLabel());

		/*
		 *  if (showState)
		 *  {
		 *  int[] currState;
		 *  if (forward)
		 *  {
		 *  currState = currArc.getToState();
		 *  }
		 *  else
		 *  {
		 *  currState = currArc.getFromState();
		 *  }
		 *  responseString.append(" [state name: " + currState.getName() + "]");
		 *  }
		 */
		return responseString.toString();
	}

	public int[] getStateAt(int index)
	{
		AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();

		return onlineSynchronizer.doTransition(events[index]);

		/*
		 *  Arc currArc = (Arc)currArcs.get(index);
		 *  State newState;
		 *  if (forward)
		 *  {
		 *  newState = currArc.getToState();
		 *  }
		 *  else
		 *  {
		 *  newState = currArc.getFromState();
		 *  }
		 *  return newState;
		 */
	}
}

class AutomataStateDisplayer
	extends JPanel
{
	private AutomataStateViewer stateViewer;
	private Automata theAutomata;
	private JCheckBox isInitialBox = new JCheckBox("initial");
	private JCheckBox isAcceptingBox = new JCheckBox("accepting");
	private JCheckBox isForbiddenBox = new JCheckBox("forbidden");
	private JLabel stateCost = new JLabel();
	private JLabel stateId = new JLabel();
	private JLabel stateName = new JLabel();
	private AutomataSynchronizerHelper helper;

	public AutomataStateDisplayer(AutomataStateViewer stateViewer, AutomataSynchronizerHelper helper)
	{
		setLayout(new BorderLayout());

		this.stateViewer = stateViewer;
		this.theAutomata = helper.getAutomata();
		this.helper = helper;

		// New!
		JLabel header = new JLabel("Current composite state");

		// header.setOpaque(true);
		// header.setBackground(Color.yellow);
		add(header, BorderLayout.NORTH);

		Box statusBox = new Box(BoxLayout.Y_AXIS);

		isInitialBox.setEnabled(false);
		isInitialBox.setBackground(Color.white);
		statusBox.add(isInitialBox);
		isAcceptingBox.setEnabled(false);
		isAcceptingBox.setBackground(Color.white);
		statusBox.add(isAcceptingBox);
		isForbiddenBox.setEnabled(false);
		isForbiddenBox.setBackground(Color.white);
		statusBox.add(isForbiddenBox);
		statusBox.add(stateCost);
		statusBox.add(stateId);
		statusBox.add(stateName);

		JScrollPane boxScroller = new JScrollPane(statusBox);

		add(boxScroller, BorderLayout.CENTER);

		JViewport vp = boxScroller.getViewport();

		vp.setBackground(Color.white);
	}

	public void setCurrState(int[] currState)
	{
		helper.addStatus(currState);

		if (!helper.getCoExecuter().isControllable())
		{
			helper.setForbidden(currState, true);
		}

		// isInitialBox.setSelected(currState.isInitial());
		// isAcceptingBox.setSelected(currState.isAccepting());
		// isForbiddenBox.setSelected(currState.isForbidden());
		// stateCost.setText("cost: " + currState.getCost());
		// stateId.setText("id: " + currState.getId());
		// stateName.setText("name: " + currState.getName());
		isInitialBox.setSelected(AutomataIndexFormHelper.isInitial(currState));
		isAcceptingBox.setSelected(AutomataIndexFormHelper.isAccepting(currState));
		isForbiddenBox.setSelected(AutomataIndexFormHelper.isForbidden(currState));

		// stateCost.setText("cost: Gratis?");
		// stateId.setText("id: Hemligt?");
		// stateName.setText("name: Hemligt?");
	}
}

class AutomataExplorerController
	extends JPanel
{
	private AutomataStateViewer stateViewer;
	private Automata theAutomata;
	private JButton undoButton;
	private JButton redoButton;

	public AutomataExplorerController(AutomataStateViewer stateViewer, AutomataSynchronizerHelper synchHelper)
	{
		setLayout(new BorderLayout());

		this.stateViewer = stateViewer;
		this.theAutomata = synchHelper.getAutomata();

		Box redoBox = new Box(BoxLayout.X_AXIS);

		undoButton = new JButton("Undo");
		redoButton = new JButton("Redo");

		redoBox.add(undoButton);
		redoBox.add(redoButton);
		add(redoBox, BorderLayout.NORTH);

		JButton resetButton = new JButton("Reset");

		add(resetButton, BorderLayout.CENTER);
		undoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				undo_actionPerformed(e);
			}
		});
		redoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				redo_actionPerformed(e);
			}
		});
		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				reset_actionPerformed(e);
			}
		});
	}

	public void reset_actionPerformed(ActionEvent e)
	{
		stateViewer.goToInitialState();

		// stateViewer.initialize();
	}

	public void undo_actionPerformed(ActionEvent e)
	{
		stateViewer.undoState();
	}

	public void redo_actionPerformed(ActionEvent e)
	{
		stateViewer.redoState();
	}

	public void update()
	{
		undoButton.setEnabled(stateViewer.undoEnabled());
		redoButton.setEnabled(stateViewer.redoEnabled());
	}
}

class AutomataExplorerHelper
{
	private static int[] initialState;

	public static void setInitialState(int[] state)
	{
		initialState = state;
	}

	public static int[] getInitialState()
	{

		// return (int[]) initialState.clone();
		return initialState;
	}
}

/*
 *  class Transition
 *  {
 *  private int[] fromState;
 *  private int event;
 *  private int[] toState;
 *
 *  public Transition(int[] fromState, int event, int[] toState)
 *  {
 *  this.fromState = fromState;
 *  this.event = event;
 *  this.toState = toState;
 *  }
 *  }
 */
