
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.CompositeState;
import org.supremica.automata.LabelTrace;
import org.supremica.automata.State;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.SupremicaException;

/**
 * This class is responsible for the "exploreStates"-window.
 */
public class AutomatonExplorer
    extends JFrame
    implements AutomatonListener
{
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(AutomatonExplorer.class);
    private final Automaton theAutomaton;
    private final BorderLayout layout = new BorderLayout();
    private final JPanel contentPane;
    private final JMenuBar menuBar = new JMenuBar();
    private final StateViewer stateViewer;
    private final ExplorerController controller;
    @SuppressWarnings("unused")
	private final VisualProject theProject;

    public AutomatonExplorer(final VisualProject theProject, final Automaton theAutomaton)
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

        final State currState = theAutomaton.getInitialState();
        if (currState == null)
        {
            throw new SupremicaException("No initial state");
        }

        stateViewer = new StateViewer(theAutomaton);

        contentPane.add(stateViewer, BorderLayout.CENTER);

        controller = new ExplorerController(theProject, stateViewer, theAutomaton);

        contentPane.add(controller, BorderLayout.SOUTH);
        stateViewer.setController(controller);
        stateViewer.goToInitialState();

        initMenubar();
    }

    public void initialize()
    {
        final List<Image> images = IconLoader.ICONLIST_APPLICATION;
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
        menuBar.add(menuFile);

        // File.Close
        final JMenuItem menuFileClose = new JMenuItem("Close");
        menuFile.add(menuFileClose);
        menuFileClose.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                setVisible(false);
                //dispose();
            }
        });

        // View
        final JMenu menuView = new JMenu();
        menuView.setText("View");
        menuView.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menuView);

        // View.EpsilonClosure
        final JCheckBoxMenuItem menuViewEpsilonClosure = new JCheckBoxMenuItem("Consider Epsilon Closure");
        menuViewEpsilonClosure.setSelected(stateViewer.getConsiderEpsilonClosure());
        menuView.add(menuViewEpsilonClosure);
        menuViewEpsilonClosure.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                stateViewer.setConsiderEpsilonClosure(!stateViewer.getConsiderEpsilonClosure());
                stateViewer.update();
            }
        });

        // View.OutgoingStateNames
        final JCheckBoxMenuItem menuViewOutgoingStateNames = new JCheckBoxMenuItem("Show Outgoing State Names");
        menuViewOutgoingStateNames.setSelected(stateViewer.getShowOutgoingStateNames());
        menuView.add(menuViewOutgoingStateNames);
        menuViewOutgoingStateNames.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                stateViewer.setShowOutgoingStateNames(!stateViewer.getShowOutgoingStateNames());
                stateViewer.update();
            }
        });

        // View.IncomingStateNames
        final JCheckBoxMenuItem menuViewIncomingStateNames = new JCheckBoxMenuItem("Show Incoming State Names");
        menuViewIncomingStateNames.setSelected(stateViewer.getShowIncomingStateNames());
        menuView.add(menuViewIncomingStateNames);
        menuViewIncomingStateNames.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                stateViewer.setShowIncomingStateNames(!stateViewer.getShowIncomingStateNames());
                stateViewer.update();
            }
        });
    }

    public void setState(final State theState)
    {
        stateViewer.setCurrState(theState);
    }

    @Override
    public void updated(final Object o)
    {
        if (o == theAutomaton)
        {
            stateViewer.goToInitialState();
        }
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

class StateViewer
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(StateViewer.class);
    private final Automaton theAutomaton;
    private State currState;
    private final EventList forwardEvents;
    private final EventList backwardEvents;
    private ExplorerController controller;
    private final StateDisplayer stateDisplayer;
    private final JSplitPane eventSplitter;
    private final JSplitPane stateEventSplitter;
    private final LinkedList<State> prevStates = new LinkedList<State>();
    private final LinkedList<State> nextStates = new LinkedList<State>();

    public StateViewer(final Automaton theAutomaton)
    {
        setLayout(new BorderLayout());

        this.theAutomaton = theAutomaton;
        forwardEvents = new EventList(this, theAutomaton, true);
        backwardEvents = new EventList(this, theAutomaton, false);

        forwardEvents.setShowStateName(false);
        backwardEvents.setShowStateName(true);

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

    public void setCurrState(final State newState)
    {
        setCurrState(newState, false);
    }

    private void setCurrState(final State newState, final boolean isUndo)
    {
        setCurrState(newState, isUndo, true);
    }

    public void setCurrState(final State newState, final boolean isUndo, final boolean forward)
    {
        if (!isUndo)
        {
            if (currState != null)
            {
                prevStates.addLast(currState);
            }

            nextStates.clear();

            if (forward)
            {
                updateCosts(currState, newState);
            }
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
            final State newState = prevStates.removeLast();

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
            final State newState = nextStates.removeFirst();

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

    public void setController(final ExplorerController controller)
    {
        this.controller = controller;
    }

    /**
     *  Performs any action only if the current automaton is composite (otherwise
     *  it is not necessary). Updates the current costs (see also CompositeState)
     *  if the current state is not initial. Otherwise, the method initializes the
     *  current and accumulated costs.
     */
    public void updateCosts(final State currState, final State newState)
    {
        if (currState instanceof CompositeState)
        {
            if (newState.isInitial())
            {
                ((CompositeState) newState).initCosts();
            }
            else
            {
                ((CompositeState) newState).updateCosts(currState);
            }
        }
    }

    public boolean getConsiderEpsilonClosure()
    {
        // Should be the same forward and backwards... should be static, that is... well, well.
        return forwardEvents.getConsiderEpsilonClosure();
    }
    public void setConsiderEpsilonClosure(final boolean bool)
    {
        forwardEvents.setConsiderEpsilonClosure(bool);
        backwardEvents.setConsiderEpsilonClosure(bool);
    }

    public boolean getShowOutgoingStateNames()
    {
        return forwardEvents.getShowStateName();
    }
    public void setShowOutgoingStateNames(final boolean bool)
    {
        forwardEvents.setShowStateName(bool);
    }

    public boolean getShowIncomingStateNames()
    {
        return backwardEvents.getShowStateName();
    }
    public void setShowIncomingStateNames(final boolean bool)
    {
        backwardEvents.setShowStateName(bool);
    }
}

class EventList
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private final boolean forward;
    @SuppressWarnings("unused")
	private final boolean showStateId = false;
    @SuppressWarnings("unused")
	private final Automaton theAutomaton;
    private State currState;
    private final StateViewer stateViewer;
    private final EventListModel eventsList;
    private final JList<Object> theList;

    public EventList(final StateViewer stateViewer, final Automaton theAutomaton, final boolean forward)
    {
        setLayout(new BorderLayout());

        this.stateViewer = stateViewer;
        this.theAutomaton = theAutomaton;
        this.forward = forward;
        eventsList = new EventListModel(theAutomaton, forward);
        theList = new JList<Object>(eventsList);

        final JScrollPane scrollPanel = new JScrollPane(theList);

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

        final JLabel jLabel = new JLabel(label);

        // jLabel.setOpaque(true);
        // jLabel.setBackground(Color.yellow);
        add(jLabel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
        theList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    final int index = theList.locationToIndex(e.getPoint());

                    if (index >= 0)
                    {
                        final State newState = eventsList.getStateAt(index);

                        updateStateViewer(newState);
                    }
                }
            }
        });
    }

    public boolean getShowStateName()
    {
        return eventsList.getShowStateName();
    }
    public void setShowStateName(final boolean showState)
    {
        eventsList.setShowStateName(showState);
    }

    public boolean getConsiderEpsilonClosure()
    {
        return eventsList.getConsiderEpsilonClosure();
    }
    public void setConsiderEpsilonClosure(final boolean bool)
    {
        eventsList.setConsiderEpsilonClosure(bool);
    }

    public void setCurrState(final State currState)
    {
        this.currState = currState;

        theList.clearSelection();
        update();
    }

    public void update()
    {
        eventsList.setCurrState(currState);
    }

    private void updateStateViewer(final State newState)
    {
        stateViewer.setCurrState(newState, false, forward);
    }
}

class EventListModel
    extends AbstractListModel<Object>
{
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(EventListModel.class);
    private State currState;
    //private ArrayList currArcs = new ArrayList();

    /** Sorts the arcs in a TreeMap with the same key for equivalent arcs (epsilon closure) */
    private final TreeMap<String,Arc> currArcs = new TreeMap<String,Arc>();
    private final boolean forward;
    @SuppressWarnings("unused")
	private final Automaton theAutomaton;
    @SuppressWarnings("unused")
	private final Alphabet theAlphabet;
    private boolean showState = false;
    private boolean considerEpsilonClosure = false;

    public EventListModel(final Automaton theAutomaton, final boolean forward)
    {
        this.forward = forward;
        this.theAutomaton = theAutomaton;
        this.theAlphabet = theAutomaton.getAlphabet();
    }

    public void setCurrState(final State currState)
    {
        this.currState = currState;

        update();
    }

    public boolean getShowStateName()
    {
        return showState;
    }
    public void setShowStateName(final boolean showState)
    {
        this.showState = showState;
    }

    public boolean getConsiderEpsilonClosure()
    {
        return considerEpsilonClosure;
    }
    public void setConsiderEpsilonClosure(final boolean bool)
    {
        this.considerEpsilonClosure = bool;
    }

    public void update()
    {
        Iterator<Arc> arcIt;

        if (forward)
        {
            if (considerEpsilonClosure)
            {
                arcIt = currState.epsilonClosure(true).outgoingArcsIterator();
            }
            else
            {
                arcIt = currState.outgoingArcsIterator();
            }
        }
        else
        {
            if (considerEpsilonClosure)
            {
                arcIt = currState.epsilonClosure(true).incomingArcsIterator();
            }
            else
            {
                arcIt = currState.incomingArcsIterator();
            }
        }

        currArcs.clear();

        while (arcIt.hasNext())
        {
            final Arc currArc = arcIt.next();

            //currArcs.add(currArc);
            if (forward)
            {
                // Sort on event and target state for forward
                currArcs.put(currArc.getEvent().getLabel() + " " +
                    currArc.getToState().getName(), currArc);
            }
            else
            {
                // Sort on event and start state for backward
                currArcs.put(currArc.getEvent().getLabel() + " " +
                    currArc.getFromState().getName(), currArc);
            }
        }

        fireContentsChanged(this, 0, currArcs.size() - 1);
    }

    @Override
    public int getSize()
    {
        return currArcs.size();
    }

    @Override
    public Object getElementAt(final int index)
    {
        //Arc currArc = (Arc) currArcs.get(index);
        final Arc currArc = (Arc) currArcs.values().toArray()[index];

        // String eventId = currArc.getEventId();
        final org.supremica.automata.LabeledEvent currEvent = currArc.getEvent();

        final StringBuilder responseString = new StringBuilder();
        boolean terminateFont = false;

        if (nextStateAssociated(currArc))
        {
            responseString.append("<html><font color=BLUE>");

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

    private boolean nextStateAssociated(final Arc currArc)
    {
        if (forward)
        {
            if (currState.getAssociatedState() == null)
            {
                return false;
            }

            final State nextState = currArc.getToState();

            return nextState == currState.getAssociatedState();
        }
        else
        {
            final State nextState = currArc.getFromState();

            if ((nextState == null) || (nextState.getAssociatedState() == null))
            {
                return false;
            }

            return nextState.getAssociatedState() == currState;
        }
    }

    public State getStateAt(final int index)
    {
        //Arc currArc = (Arc) currArcs.get(index);
        final Arc currArc = (Arc) currArcs.values().toArray()[index];
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
    private static final long serialVersionUID = 1L;
    private final StateViewer stateViewer;
    @SuppressWarnings("unused")
	private final Automaton theAutomaton;
    private final JCheckBox isInitialBox = new JCheckBox("initial");
    private final JCheckBox isAcceptingBox = new JCheckBox("accepting");
    //private JCheckBox isMutuallyAcceptingBox = new JCheckBox("mutually accepting");
    private final JCheckBox isForbiddenBox = new JCheckBox("forbidden");
    private final JLabel stateCost = new JLabel();
    private final JLabel stateId = new JLabel();
    private final JLabel stateName = new JLabel();
    private final JLabel currentCosts = new JLabel();

    private void changeStateAccepting(final boolean b)
    {
        stateViewer.getCurrState().setAccepting(b);
    }
/*
    private void changeStateMutuallyAccepting(boolean b)
    {
        stateViewer.getCurrState().setMutuallyAccepting(b);
    }
*/
    private void changeStateForbidden(final boolean b)
    {
        stateViewer.getCurrState().setForbidden(b);
    }

    public StateDisplayer(final StateViewer stateViewer, final Automaton theAutomaton)
    {
        setLayout(new BorderLayout());

        this.stateViewer = stateViewer;
        this.theAutomaton = theAutomaton;

        final JLabel header = new JLabel("Current state");

        // header.setOpaque(true);
        // header.setBackground(Color.yellow);
        add(header, BorderLayout.NORTH);

        final Box statusBox = new Box(BoxLayout.Y_AXIS);

        isInitialBox.setEnabled(false);
        isInitialBox.setBackground(Color.white);
        statusBox.add(isInitialBox);
        isAcceptingBox.setEnabled(true);
        isAcceptingBox.setBackground(Color.white);
        isAcceptingBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                changeStateAccepting(((JCheckBox) e.getSource()).isSelected());
            }
        });
        statusBox.add(isAcceptingBox);
        isForbiddenBox.setEnabled(true);
        isForbiddenBox.setBackground(Color.white);
        isForbiddenBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                changeStateForbidden(((JCheckBox) e.getSource()).isSelected());
            }
        });
        statusBox.add(isForbiddenBox);
        statusBox.add(stateCost);
        statusBox.add(stateId);
        statusBox.add(stateName);
        statusBox.add(currentCosts);

        final JScrollPane boxScroller = new JScrollPane(statusBox);

        add(boxScroller, BorderLayout.CENTER);

        final JViewport vp = boxScroller.getViewport();

        vp.setBackground(Color.white);
    }

    /**
     * This method sets the values of the graphical components building up the
     * stateDisplayer.
     */
    public void setCurrState(final State currState)
    {
        isInitialBox.setSelected(currState.isInitial());
        isAcceptingBox.setSelected(currState.isAccepting());
        //isMutuallyAcceptingBox.setSelected(currState.isMutuallyAccepting());
        isForbiddenBox.setSelected(currState.isForbidden());

        if (currState instanceof CompositeState)
        {
            final StringBuilder str = new StringBuilder();
            final double[] costs = ((CompositeState) currState).getCurrentCosts();

            for (int i = 0; i < costs.length - 1; i++)
            {
                str.append(costs[i] + "  ");
            }

            str.append(costs[costs.length - 1] + "");
            currentCosts.setText("composite costs: [" + str + "]");
            stateCost.setText("accumulated cost: " + ((CompositeState) currState).getAccumulatedCost());
        }
        else
        {
            stateCost.setText("cost: " + currState.getCost());
        }

        //              stateId.setText("id: " + currState.getId());
        stateName.setText("name: " + currState.getName());
    }
}

class ExplorerController
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(ExplorerController.class);
    private final StateViewer stateViewer;
    private final Automaton theAutomaton;
    private final JButton undoButton;
    private final JButton redoButton;
    private final VisualProject theProject;

    public ExplorerController(final VisualProject theProject, final StateViewer stateViewer, final Automaton theAutomaton)
    {
        setLayout(new BorderLayout());

        this.theProject = theProject;
        this.stateViewer = stateViewer;
        this.theAutomaton = theAutomaton;

        final Box redoBox = new Box(BoxLayout.X_AXIS);
        final ImageIcon forwardImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Forward24.gif"));
        final ImageIcon backwardImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Back24.gif"));
        final ImageIcon homeImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Home24.gif"));
        final ImageIcon findImg = new ImageIcon(ExplorerController.class.getResource("/toolbarButtonGraphics/general/Find24.gif"));
        final ImageIcon routeImg = new ImageIcon(ExplorerController.class.getResource("/icons/Route24.gif"));

        undoButton = new JButton(backwardImg);

        undoButton.setToolTipText("Back");

        redoButton = new JButton(forwardImg);

        redoButton.setToolTipText("Forward");

        final JButton resetButton = new JButton(homeImg);

        resetButton.setToolTipText("Go to the initial state");

        final JButton findButton = new JButton(findImg);

        findButton.setToolTipText("Search for a state");

        final JButton routeButton = new JButton(routeImg);

        routeButton.setToolTipText("Find shortest path from the initial state to this state and mark the corresponding events in blue.");
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
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                undo_actionPerformed(e);
            }
        });
        redoButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                redo_actionPerformed(e);
            }
        });
        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                reset_actionPerformed(e);
            }
        });
        findButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                find_actionPerformed(e);
            }
        });
        routeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                route_actionPerformed(e);
            }
        });
    }

    public void reset_actionPerformed(final ActionEvent e)
    {
        stateViewer.goToInitialState();
    }

    public void undo_actionPerformed(final ActionEvent e)
    {
        stateViewer.undoState();
    }

    public void redo_actionPerformed(final ActionEvent e)
    {
        stateViewer.redoState();
    }

    public void find_actionPerformed(final ActionEvent e)
    {
        final Automata theAutomata = new Automata();

        theAutomata.addAutomaton(theAutomaton);

        try
        {
            ActionMan.findStates.execute(theProject, theAutomata);
        }
        catch (final Exception ex)
        {
            logger.error(ex.toString());
            logger.debug(ex.getStackTrace());
        }
    }

    public void route_actionPerformed(final ActionEvent e)
    {
        try
        {
            final LabelTrace trace = theAutomaton.getTrace(stateViewer.getCurrState());

//                      logger.info("Trace to state " + stateViewer.getCurrState().getId() + ": " + trace);
            logger.info("Trace to state " + stateViewer.getCurrState().getName() + ": " + trace);
        }
        catch (final Exception ex)
        {
            logger.error("Error when performing route: ", ex);
            logger.debug(ex.getStackTrace());
        }

        stateViewer.update();
    }

    public void update()
    {
        undoButton.setEnabled(stateViewer.undoEnabled());
        redoButton.setEnabled(stateViewer.redoEnabled());
    }
}
