
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
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.LabelTrace;
import org.supremica.log.*;

public class AutomatonExplorer
	extends JFrame
	implements AutomatonListener
{
	private Automaton theAutomaton;
	private BorderLayout layout = new BorderLayout();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private StateViewer stateViewer;
	private ExplorerController controller;
	private VisualProject theProject;

	public AutomatonExplorer(VisualProject theProject, Automaton theAutomaton)
		throws Exception
	{
		this.theProject = theProject;
		this.theAutomaton = theAutomaton;

		theAutomaton.getListeners().addListener(this);
		setBackground(Color.white);

		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(layout);

		// contentPane.add(toolBar, BorderLayout.NORTH);
		setTitle(theAutomaton.getName());
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

		State currState = theAutomaton.getInitialState();

		if (currState == null)
		{
			throw new Exception("No initial state");
		}

		stateViewer = new StateViewer(theAutomaton);

		contentPane.add(stateViewer, BorderLayout.CENTER);

		controller = new ExplorerController(theProject, stateViewer, theAutomaton);

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

	public void setState(State theState)
	{
		stateViewer.setCurrState(theState);
	}

	public void updated(Object o)
	{
		if (o == theAutomaton)
		{
			stateViewer.goToInitialState();
		}
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

class StateViewer
	extends JPanel
{
	private Automaton theAutomaton;
	private State currState;
	private EventList forwardEvents;
	private EventList backwardEvents;
	private ExplorerController controller;
	private StateDisplayer stateDisplayer;
	private JSplitPane eventSplitter;
	private JSplitPane stateEventSplitter;
	private LinkedList prevStates = new LinkedList();
	private LinkedList nextStates = new LinkedList();

	public StateViewer(Automaton theAutomaton)
	{
		setLayout(new BorderLayout());

		this.theAutomaton = theAutomaton;
		forwardEvents = new EventList(this, theAutomaton, true);
		backwardEvents = new EventList(this, theAutomaton, false);

		backwardEvents.setShowStateId(true);

		eventSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forwardEvents, backwardEvents);
		stateDisplayer = new StateDisplayer(this, theAutomaton);
		stateEventSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stateDisplayer, eventSplitter);

		add(stateEventSplitter, BorderLayout.CENTER);
	}

	public void initialize()
	{
		eventSplitter.setDividerLocation(0.5);
		stateEventSplitter.setDividerLocation(0.6);
	}

	public void setCurrState(State newState)
	{
		setCurrState(newState, false);
	}

	private void setCurrState(State newState, boolean isUndo)
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

	public State getCurrState()
	{
		return currState;
	}

	public void goToInitialState()
	{
		prevStates.clear();

		currState = null;

		setCurrState(theAutomaton.getInitialState(), false);
	}

	public void undoState()
	{
		if (prevStates.size() > 0)
		{
			State newState = (State) prevStates.removeLast();

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
			State newState = (State) nextStates.removeFirst();

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
		stateDisplayer.setCurrState(currState);
		forwardEvents.setCurrState(currState);
		backwardEvents.setCurrState(currState);
		controller.update();
	}

	public void setController(ExplorerController controller)
	{
		this.controller = controller;
	}
}

class EventList
	extends JPanel
{
	private boolean forward;
	private boolean showStateId = false;
	private Automaton theAutomaton;
	private State currState;
	private StateViewer stateViewer;
	private EventListModel eventsList;
	private JList theList;

	public EventList(StateViewer stateViewer, Automaton theAutomaton, boolean forward)
	{
		setLayout(new BorderLayout());

		this.stateViewer = stateViewer;
		this.theAutomaton = theAutomaton;
		this.forward = forward;
		eventsList = new EventListModel(theAutomaton, forward);
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
						State newState = eventsList.getStateAt(index);

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

	public void setCurrState(State currState)
	{
		this.currState = currState;

		theList.clearSelection();
		update();
	}

	public void update()
	{
		eventsList.setCurrState(currState);
	}

	private void updateStateViewer(State newState)
	{
		stateViewer.setCurrState(newState);
	}
}

class EventListModel
	extends AbstractListModel
{
	private State currState;
	private ArrayList currArcs = new ArrayList();
	private boolean forward;
	private Automaton theAutomaton;
	private Alphabet theAlphabet;
	private boolean showState = false;

	public EventListModel(Automaton theAutomaton, boolean forward)
	{
		this.forward = forward;
		this.theAutomaton = theAutomaton;
		this.theAlphabet = theAutomaton.getAlphabet();
	}

	public void setCurrState(State currState)
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
		Iterator arcIt;

		if (forward)
		{
			arcIt = currState.outgoingArcsIterator();
		}
		else
		{
			arcIt = currState.incomingArcsIterator();
		}

		currArcs.clear();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();

			currArcs.add(currArc);
		}

		fireContentsChanged(this, 0, currArcs.size() - 1);
	}

	public int getSize()
	{
		return currArcs.size();
	}

	public Object getElementAt(int index)
	{
		Arc currArc = (Arc) currArcs.get(index);
		String eventId = currArc.getEventId();
		org.supremica.automata.LabeledEvent currEvent;

		try
		{
			currEvent = theAlphabet.getEventWithId(eventId);
		}
		catch (Exception e)
		{
			System.err.println("Error: Could not find " + eventId + " in alphabet!\n");

			return null;
		}

		StringBuffer responseString = new StringBuffer();

		boolean terminateFont = false;
		if (nextStateAssociated(currArc))
		{
			responseString.append("<html><font color=RED>");
			terminateFont = true;
		}


		if (!currEvent.isControllable())
		{
			responseString.append("!");
		}

		responseString.append(currEvent.getLabel());

		if (showState)
		{
			State currState;

			if (forward)
			{
				currState = currArc.getToState();
			}
			else
			{
				currState = currArc.getFromState();
			}

			responseString.append(" [state name: " + currState.getName() + "]");
		}
		if (terminateFont)
		{
			responseString.append("</font></html>");
		}
		return responseString.toString();
	}

	private boolean nextStateAssociated(Arc currArc)
	{
		if (forward)
		{
			if (currState.getAssociatedState() == null)
			{
				return false;
			}
			State nextState = currArc.getToState();
			return nextState == currState.getAssociatedState();
		}
		else
		{
			State nextState = currArc.getFromState();
			if (nextState == null || nextState.getAssociatedState() == null)
			{
				return false;
			}
			return nextState.getAssociatedState() == currState;
		}

	}

	public State getStateAt(int index)
	{
		Arc currArc = (Arc) currArcs.get(index);
		State newState;

		if (forward)
		{
			newState = currArc.getToState();
		}
		else
		{
			newState = currArc.getFromState();
		}

		return newState;
	}
}

class StateDisplayer
	extends JPanel
{
	private StateViewer stateViewer;
	private Automaton theAutomaton;
	private JCheckBox isInitialBox = new JCheckBox("initial");
	private JCheckBox isAcceptingBox = new JCheckBox("accepting");
	private JCheckBox isForbiddenBox = new JCheckBox("forbidden");
	private JLabel stateCost = new JLabel();
	private JLabel stateId = new JLabel();
	private JLabel stateName = new JLabel();

	private void changeStateAccepting(boolean b)
	{
		stateViewer.getCurrState().setAccepting(b);
	}

	private void changeStateForbidden(boolean b)
	{
		stateViewer.getCurrState().setForbidden(b);
	}

	public StateDisplayer(StateViewer stateViewer, Automaton theAutomaton)
	{
		setLayout(new BorderLayout());

		this.stateViewer = stateViewer;
		this.theAutomaton = theAutomaton;

		JLabel header = new JLabel("Current state");

		// header.setOpaque(true);
		// header.setBackground(Color.yellow);
		add(header, BorderLayout.NORTH);

		Box statusBox = new Box(BoxLayout.Y_AXIS);

		isInitialBox.setEnabled(false);
		isInitialBox.setBackground(Color.white);
		statusBox.add(isInitialBox);
		isAcceptingBox.setEnabled(true);
		isAcceptingBox.setBackground(Color.white);
		isAcceptingBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				changeStateAccepting(((JCheckBox) e.getSource()).isSelected());
			}
		});
		statusBox.add(isAcceptingBox);
		isForbiddenBox.setEnabled(true);
		isForbiddenBox.setBackground(Color.white);
		isForbiddenBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				changeStateForbidden(((JCheckBox) e.getSource()).isSelected());
			}
		});
		statusBox.add(isForbiddenBox);
		statusBox.add(stateCost);
		statusBox.add(stateId);
		statusBox.add(stateName);

		JScrollPane boxScroller = new JScrollPane(statusBox);

		add(boxScroller, BorderLayout.CENTER);

		JViewport vp = boxScroller.getViewport();

		vp.setBackground(Color.white);
	}

	public void setCurrState(State currState)
	{
		isInitialBox.setSelected(currState.isInitial());
		isAcceptingBox.setSelected(currState.isAccepting());
		isForbiddenBox.setSelected(currState.isForbidden());
		stateCost.setText("cost: " + currState.getCost());
		stateId.setText("id: " + currState.getId());
		stateName.setText("name: " + currState.getName());
	}
}

class ExplorerController
	extends JPanel
{
	private static Logger logger = LoggerFactory.createLogger(ExplorerController.class);
	private StateViewer stateViewer;
	private Automaton theAutomaton;
	private JButton undoButton;
	private JButton redoButton;
	private VisualProject theProject;

	public ExplorerController(VisualProject theProject, StateViewer stateViewer, Automaton theAutomaton)
	{
		setLayout(new BorderLayout());

		this.theProject = theProject;
		this.stateViewer = stateViewer;
		this.theAutomaton = theAutomaton;

		Box redoBox = new Box(BoxLayout.X_AXIS);

		ImageIcon forwardImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Forward24.gif"));
		ImageIcon backwardImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Back24.gif"));
		ImageIcon homeImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Home24.gif"));
		ImageIcon findImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/general/Find24.gif"));
		ImageIcon routeImg = new ImageIcon(ExplorerController.class.getResource("/icons/Route24.gif"));

		undoButton = new JButton(backwardImg);
		undoButton.setToolTipText("Back");
		redoButton = new JButton(forwardImg);
		redoButton.setToolTipText("Forward");
		JButton resetButton = new JButton(homeImg);
		resetButton.setToolTipText("Go to the initial state");
		JButton findButton = new JButton(findImg);
		findButton.setToolTipText("Search for a state");
		JButton routeButton = new JButton(routeImg);
		routeButton.setToolTipText("Find shortest path from the initial state to this state and mark the corresponding events in red.");

		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(undoButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(redoButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(resetButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(findButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(routeButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(Box.createHorizontalGlue());

		add(redoBox, BorderLayout.NORTH);

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
		findButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				find_actionPerformed(e);
			}
		});
		routeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				route_actionPerformed(e);
			}
		});
	}

	public void reset_actionPerformed(ActionEvent e)
	{
		stateViewer.goToInitialState();
	}

	public void undo_actionPerformed(ActionEvent e)
	{
		stateViewer.undoState();
	}

	public void redo_actionPerformed(ActionEvent e)
	{
		stateViewer.redoState();
	}

	public void find_actionPerformed(ActionEvent e)
	{
		Automata theAutomata = new Automata();
		theAutomata.addAutomaton(theAutomaton);
		FindStates find_states = new FindStates(theProject, theAutomata);

		try
		{
			find_states.execute();
		}
		catch (Exception ex)
		{

			logger.error(ex.toString());
		}
	}

	public void route_actionPerformed(ActionEvent e)
	{
		try
		{
			LabelTrace trace = theAutomaton.getTrace(stateViewer.getCurrState());
			logger.info("Trace to state " + stateViewer.getCurrState().getId() + ": " + trace);
		}
		catch (Exception ex)
		{
			logger.error("Error when performing route: " + ex.getMessage());
		}
		stateViewer.update();
	}

	public void update()
	{
		undoButton.setEnabled(stateViewer.undoEnabled());
		redoButton.setEnabled(stateViewer.redoEnabled());
	}
}
