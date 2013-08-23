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

package org.supremica.automata.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.gui.ExecutionDialog;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.IntArrayHashTable;
import org.supremica.util.IntArrayList;
import org.supremica.util.SupremicaException;

//import EDU.oswego.cs.dl.util.concurrent.Rendezvous;
/**
 * Contains information that is common to all synchronization threads.
 * <p/>
 * @author ka
 * @author Mohammad Reza Shoaei (EFA synchronization)
 * @since November 28, 2001
 */
public class AutomataSynchronizerHelper
{

  private static Logger logger = LoggerFactory.createLogger(
   AutomataSynchronizerHelper.class);
  private AutomataIndexForm theAutomataIndexForm;
  private final IntArrayHashTable theStates;
  private final IntArrayList statesToProcess;
  private int nbrOfStatesToProcess = 0;
  // Two locks are used to limit the access the statesToProcess
  private final Object gettingFromStatesToProcessLock = new Object();
  private final Object addingToStatesToProcessLock =
   gettingFromStatesToProcessLock;
  private final Object addStateLock = new Object();
  private final Automata theAutomata;
  private final Automaton theAutomaton;    // the result
  private boolean automataIsControllable = true;
  // Keeps information common to helpers.
  private final AutomataSynchronizerHelperStatistics helperStatistics;
  private SynchronizationOptions syncOptions = null;
  // Used by AutomataSynchronizerExecuter
  private final StateMemorizer stateMemorizer = new StateMemorizer();
  private boolean rememberUncontrollable = false;
  private boolean expandEventsUsingPriority = false;
  private final IntArrayList fromStateList = new IntArrayList();
  private final IntArrayList stateTrace = new IntArrayList();
  private boolean rememberTrace = false;
  private boolean coExecute = false;
  private AutomataSynchronizerExecuter coExecuter = null;

  /* Used by AutomataControllabillityCheck.
   * Causes the synchronization to stop as soon as an uncontrollable
   * state is found.
   */
  private boolean exhaustiveSearch = false;
  // For synchronizing without recalculating the AutomataIndexForm
  private boolean[] activeAutomata;
  // For counting states in executionDialog
  private ExecutionDialog executionDialog = null;
  // Stop execution after amount of state
  private int stopExecutionLimit = -1;
  //////////////////
  private final ModuleSubjectFactory mFactory = 
   ModuleSubjectFactory.getInstance();
  Set<EventProxy> mCurrentEvents;
  Set<EventProxy> mCurrentBlockedEvents;
  Map<State, SimpleNodeProxy> mCurrentNodeMap;
  Collection<EdgeProxy> mEdges;
  SimpleComponentProxy synchronizedComponent = null;
  private HashMap<Arc, EdgeSubject>[] arc2EdgeTable;
  private HashMap<String, Integer> autName2indexTable =
   new HashMap<String, Integer>();

  public AutomataSynchronizerHelper(final Automata theAutomata,
                                    final SynchronizationOptions syncOptions,
                                    boolean sups_as_plants)
  {
    this(theAutomata, syncOptions, null, null, sups_as_plants);
  }

  @SuppressWarnings("deprecation")
  public AutomataSynchronizerHelper(final Automata theAutomata,
                                    final SynchronizationOptions syncOptions,
                                    final HashMap<Arc, EdgeSubject>[] arc2EdgeTable,
                                    final HashMap<String, Integer> autName2indexTable,
                                    boolean sups_as_plants)
  {
    if (syncOptions.getEFAMode()) {
      this.arc2EdgeTable = arc2EdgeTable;
      this.autName2indexTable = autName2indexTable;
      mCurrentNodeMap = new HashMap<State, SimpleNodeProxy>();
      mEdges = new ArrayList<EdgeProxy>();
      mCurrentBlockedEvents = new HashSet<EventProxy>();
    }
    if (theAutomata == null) {
      throw new IllegalArgumentException("theAutomata must be non-null");
    }

    if (syncOptions == null) {
      throw new IllegalArgumentException("syncOptions must be non-null");
    }

    this.theAutomata = theAutomata;
    this.syncOptions = syncOptions;
    helperStatistics = new AutomataSynchronizerHelperStatistics();
    statesToProcess = new IntArrayList();
    nbrOfStatesToProcess = 0;
    theStates = new IntArrayHashTable(syncOptions.getInitialHashtableSize(),
                                      syncOptions.expandHashtable());
    theAutomaton = new Automaton("");

    // Calculate the automataIndexForm (a more efficient representation of an automata)
    try {
      theAutomataIndexForm = new AutomataIndexForm(theAutomata, theAutomaton,
                                                   sups_as_plants);
    } catch (final Exception e) {
      logger.error("Error while computing AutomataIndexForm");
      logger.debug(e.getStackTrace());

      throw new RuntimeException(e);
    }
  }

  /**
   * Constructs new helper but keeps the same AutomataIndexForm-, Automata-,
   * HelperData and Automaton-Objects.
   * <p/>
   * @param orgHelper The old helper to collect information from
   */
  public AutomataSynchronizerHelper(final AutomataSynchronizerHelper orgHelper)
   throws Exception
  {
    theAutomata = orgHelper.getAutomata();
    theAutomaton = orgHelper.getAutomaton();
    theAutomataIndexForm = orgHelper.getAutomataIndexForm();
    syncOptions = orgHelper.getSynchronizationOptions();
    helperStatistics = orgHelper.getHelperData();
    executionDialog = orgHelper.getExecutionDialog();
    statesToProcess = new IntArrayList();
    nbrOfStatesToProcess = 0;
    theStates = new IntArrayHashTable(syncOptions.getInitialHashtableSize(),
                                      syncOptions.expandHashtable());
  }

  public void clear()
  {
    theStates.clear();

    automataIsControllable = true;
    coExecute = false;
    coExecuter = null;
    rememberTrace = false;
    exhaustiveSearch = false;
    rememberUncontrollable = false;
    expandEventsUsingPriority = false;
  }

  /**
   * Initializes the helper for a new run. Generates a new initial state and
   * adds it to the queue.
   */
  public void initialize()
   throws Exception
  {
    // The helper (or rather theStates) should be clear before executing this method
    if (theStates.size() > 0) {
      throw new Exception(
       "AutomataSynchronizerHelper not cleared properly before reinitialization.");
    }

    // Build the initial state  (including 2 status fields)
    final int[] initialState = AutomataIndexFormHelper.createState(theAutomata
     .size());

    final Iterator<Automaton> autIt = theAutomata.iterator();
    while (autIt.hasNext()) {
      final Automaton currAutomaton = autIt.next();
      final State currInitialState = currAutomaton.getInitialState();
      initialState[theAutomataIndexForm.getAutomataIndexMap().getAutomatonIndex(
       currAutomaton)] = theAutomataIndexForm.getAutomataIndexMap()
       .getStateIndex(currAutomaton, currInitialState);
    }

    // Add state to stack
    addState(initialState);
  }

  public AutomataIndexMap getIndexMap()
  {
    return theAutomataIndexForm.getIndexMap();
  }

  public SynchronizationOptions getSynchronizationOptions()
  {
    return syncOptions;
  }

  public Automaton getAutomaton()
  {
    return theAutomaton;
  }
  /*
   public Rendezvous getExecuterRendezvous()
   {
   return executerRendezvous;
   }
   */

  public Alphabet getUnionAlphabet()
  {
    return theAutomaton.getAlphabet();
  }

  public int getNbrOfEvents()
  {
    return getUnionAlphabet().size();
  }

  public Automata getAutomata()
  {
    return theAutomata;
  }

  public AutomataIndexForm getAutomataIndexForm()
  {
    return theAutomataIndexForm;
  }

  public AutomataSynchronizerHelperStatistics getHelperData()
  {
    return helperStatistics;
  }

  public int getNbrOfStatesToProcess()
  {
    return nbrOfStatesToProcess;
  }

  /**
   * @return a state if there are more states to process, null otherwise.
   */
  public int[] getStateToProcess()
  {
    synchronized (gettingFromStatesToProcessLock) {
      if ((nbrOfStatesToProcess == 0) || (stopExecutionLimit == 0)) {
        return null;
      }

      if (stopExecutionLimit > 0) {
        stopExecutionLimit--;
      }

      nbrOfStatesToProcess--;

      if (rememberTrace) {
        if (fromStateList.size() > 0) {
          while (!(Arrays.equals(fromStateList.getLast(), stateTrace.getLast()))) {
            stateTrace.removeLast();
          }

          if (stateTrace.size() == 0) {
            logger.error("Error when recording trace.");
          }

          fromStateList.removeLast();
          stateTrace.addLast(statesToProcess.getLast());
        }

        if (stateTrace.size() == 0) {
          stateTrace.addLast(statesToProcess.getLast());
        }

        // Depth first search
        return statesToProcess.removeLast();
      } else {
        if (coExecute) {

          // Depth first search
          return statesToProcess.removeLast();
        } else {

          // Width first search
          return statesToProcess.removeFirst();
        }
      }
    }
  }

  public void addComment(final String comment)
  {
    theAutomaton.setComment(comment);
  }

  public void setExecutionDialog(final ExecutionDialog executionDialog)
  {
    this.executionDialog = executionDialog;
  }

  public ExecutionDialog getExecutionDialog()
  {
    return executionDialog;
  }

  /**
   * If the toState does not exist then make a copy of this state and add it to
   * the set of states and to the set of states waiting for processing. If it
   * exists then find it. Insert the arc.
   * <p/>
   * @param fromState The feature to be added to the State attribute
   * @param toState   The feature to be added to the State attribute
   * <p/>
   * @exception Exception Description of the Exception
   */
  public void addState(final int[] fromState, final int[] toState)
   throws Exception
  {
    //logger.debug("addState state: " +AutomataIndexFormHelper.dumpState(fromState));

    if (rememberTrace) {
      fromStateList.addLast(fromState);
    }

    if (true) // What? /Hugo.
    {
      final int prevStateIndex = theStates.getIndex(fromState);

      if (prevStateIndex >= 0) {
        AutomataIndexFormHelper.setPrevStateIndex(toState, prevStateIndex);
      } else {
        AutomataIndexFormHelper.setPrevStateIndex(toState,
                                                  AutomataIndexFormHelper.STATE_NO_PREVSTATE);
      }
    }

    addState(toState);
  }

  // Add this state to theStates
  public void addState(final int[] state)
   throws SupremicaException
  {
    int[] newState = null;

    synchronized (addStateLock) {
      newState = theStates.add(state);
    }

    if (newState != null) {
      if (rememberTrace && (stateTrace.size() == 0)) {
        // Add initial state
        stateTrace.add(newState);
      }

      addStatus(newState);
      addStateToProcess(newState);

      helperStatistics.nbrOfAddedStates++;
    } else if (rememberTrace && (fromStateList.size() != 0)) {
      fromStateList.removeLast();
    }

    helperStatistics.nbrOfCheckedStates++;
    if ((executionDialog != null) && 
        (helperStatistics.nbrOfCheckedStates % 2000 == 0)) 
    {
      executionDialog.setValue((int) helperStatistics.nbrOfAddedStates);
    }
  }

  /**
   * Add a state to the queue of states waiting for being processed. This is
   * only called by the addInitialState and addState methods.
   * <p/>
   * @param state The feature to be added to the StateToProcess attribute
   */
  public void addStateToProcess(final int[] state)
  {
    synchronized (addingToStatesToProcessLock) {
      statesToProcess.addLast(state);

      nbrOfStatesToProcess++;
    }
  }

  public void addStatus(final int[] state)
  {
    final int[][] stateStatusTable = theAutomataIndexForm.getStateStatusTable();
    int tmpStatus = stateStatusTable[0][state[0]];
    boolean forbidden = AutomataIndexFormHelper.isForbidden(tmpStatus);
    int currStatus;

    for (int i = 1; i < state.length - AutomataIndexFormHelper.STATE_EXTRA_DATA;
         i++) {
      if ((activeAutomata == null) || (activeAutomata[i] == true)) {
        //logger.info("i: " + 1 + " state[i]: " + state[i]);
        //logger.info("stateStatTab: " + stateStatusTable[i][state[i]]);
        currStatus = stateStatusTable[i][state[i]];
        tmpStatus &= currStatus;

        // works for everything except forbidden
        forbidden |= AutomataIndexFormHelper.isForbidden(currStatus);
      }
    }

    if (forbidden) {
      tmpStatus |= (1 << 2);
    }

    state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END] =
     tmpStatus;
  }

  public void setForbidden(final int[] state, final boolean forbidden)
  {
    int currStatus = state[state.length
     - AutomataIndexFormHelper.STATE_STATUS_FROM_END];

    if (forbidden) {
      currStatus |= (1 << 2);
    } else {
      currStatus &= ~(1 << 2);
    }

    state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END] =
     currStatus;

    helperStatistics.nbrOfForbiddenStates++;
  }

  public void setDeadlocked(final int[] state, final boolean deadlocked)
  {

    /*
     if (logger.isDebugEnabled())
     {
     logger.debug("Deadlocked state:\n" +
     AutomataIndexFormHelper.dumpVerboseState(state, theAutomataIndexForm));
     logger.debug(displayTrace(state));
     }
     */
    int currStatus = state[state.length
     - AutomataIndexFormHelper.STATE_STATUS_FROM_END];

    if (deadlocked) {
      currStatus |= (1 << 6);

      helperStatistics.nbrOfDeadlockedStates++;
    } else {
      currStatus &= ~(1 << 6);
    }

    state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END] =
     currStatus;
  }

  public int[][] getStateTable()
  {
    return theStates.getTable();
  }

  public int getStateTableSize()
  {
    return theStates.size();
  }

  public Iterator<?> getStateIterator()
  {
    return theStates.iterator();
  }

  public long getNumberOfAddedStates()
  {
    return helperStatistics.nbrOfAddedStates;
  }

  public State[][] getIndexFormStateTable()
  {
    return theAutomataIndexForm.getStateTable();
  }

  public int getStateIndex(final int[] state)
  {
    return theStates.getIndex(state);
  }

  public String toString()
  {
    return theStates.toString();
  }

  /**
   * Used for getting the synchronization result to the worker-class.
   * <p/>
   * @param isControllable The new automataIsControllable value
   * <p/>
   * @see AutomataSynchronizerExecuter
   */
  public void setAutomataIsControllable(final boolean isControllable)
  {
    automataIsControllable = isControllable;
  }

  // automataIsControllable is set to false by AutomataSynchronizerhelper, AutomataSynchronizerExecuter
  // when an uncontrollable state is found.
  public boolean getAutomataIsControllable()
  {
    return automataIsControllable;
  }

  public StateMemorizer getStateMemorizer()
  {
    return stateMemorizer;
  }

  public boolean isGoalState(final int[] state)
  {
    return stateMemorizer.contains(state);
  }

  public void setRememberUncontrollable(final boolean remember)
  {
    rememberUncontrollable = remember;
  }

  public boolean getRememberUncontrollable()
  {
    return rememberUncontrollable;
  }

  public void setExhaustiveSearch(final boolean exhaustive)
  {
    exhaustiveSearch = exhaustive;
  }

  public boolean getExhaustiveSearch()
  {
    return exhaustiveSearch;
  }

  public void setExpandEventsUsingPriority(final boolean use)
  {
    expandEventsUsingPriority = use;
  }

  public boolean getExpandEventsUsingPriority()
  {
    return expandEventsUsingPriority;
  }

  // Returns array with priorities, 0 is the highest priority, larger numbers - lower priority
  public int[] getEventPriority()
  {
    final Alphabet unionAlphabet = theAutomaton.getAlphabet();
    final int[] eventPriority = new int[unionAlphabet.size()];
    int index = 0;

    for (final Iterator<LabeledEvent> eventIterator = unionAlphabet.iterator();
         eventIterator.hasNext();) {
      final LabeledEvent currEvent = eventIterator.next();

      if (currEvent.getExpansionPriority() < 0) {
        // The events are already ordered after synchIndex!
        // eventPriority[currEvent.getSynchIndex()] = 10;
        eventPriority[index++] = 10;
      } else {

        // The events are already ordered after synchIndex!
        // eventPriority[currEvent.getSynchIndex()] = currEvent.getExpansionPriority();
        eventPriority[index++] = currEvent.getExpansionPriority();
      }
    }

    return eventPriority;
  }

  public void setRememberTrace(final boolean rememberTrace)
   throws Exception
  {
    if (theStates.size() > 0) {
      throw new Exception(
       "Error in AutomataSynchronizerHelper. Helper must be cleared before calling setRememberTrace().");
    }

    this.rememberTrace = rememberTrace;
  }

  /**
   * Logs the amount of states examined during the execution and some other
   * stuff.
   */
  public void printStatistics()
  {
    // Did we do anything?
    if (helperStatistics.getNumberOfCheckedStates() != 0) {
      logger.info(helperStatistics);
    }
  }

  public String getStatisticsLineLatex()
  {
    return helperStatistics.getStatisticsLineLaTeX();
  }

  /**
   * Displays the event-trace leading to the uncontrollable state.
   */
  public void displayTrace()
   throws Exception
  {
    // We have to have an executer for finding the transitions
    clear();

    //AutomataOnlineSynchronizer executer = new AutomataOnlineSynchronizer(this);
    final AutomataSynchronizerExecuter executer =
     new AutomataSynchronizerExecuter(this);

    executer.initialize();

    // This version does not remove shortcuts, add this later. FIXA!
    final StringBuffer trace = new StringBuffer();
    int[] prevState = null;

    for (final Iterator<?> traceIt = stateTrace.iterator(); traceIt.hasNext();) {
      final int[] nextState = (int[]) traceIt.next();

      if (prevState != null) {
        final int currEventIndex = executer.findTransition(prevState, nextState);

        trace.append(" ");
        //trace.append(unionAlphabet.getEventWithIndex(currEventIndex).getLabel());
        trace.append(theAutomataIndexForm.getAutomataIndexMap().getEventAt(
         currEventIndex));
      }

      prevState = nextState;
    }

    logger.info("The trace leading to the uncontrollable state is:" + trace
     .toString() + ".");
  }

  /*
   public void displayTrace(int[] currState)
   {
   Alphabet unionAlphabet = theAutomaton.getAlphabet();

   // AutomataOnlineSynchronizer executer = new AutomataOnlineSynchronizer(this);
   AutomataSynchronizerExecuter executer = new AutomataSynchronizerExecuter(this);

   executer.initialize();

   int prevStateIndex = AutomataIndexFormHelper.getPrevStateIndex(currState);
   if (prevStateIndex != AutomataIndexFormHelper.STATE_NO_PREVSTATE)
   {
   int[] prevState = theStates.get(prevStateIndex);
   if (prevState != null)
   {
   displayTrace(prevState);
   int currEventIndex = executer.findTransition(prevState, currState);
   if (currEventIndex >= 0)
   {
   logger.info(unionAlphabet.getEventWithIndex(currEventIndex).getLabel());
   }
   else
   {
   logger.error("Could not find an event between prevState and currState");
   logger.error("Current state, index: " + theStates.getIndex(currState));
   logger.error(AutomataIndexFormHelper.dumpVerboseState(currState, theAutomataIndexForm));
   logger.error("Previous state, index: " + theStates.getIndex(prevState));
   logger.error(AutomataIndexFormHelper.dumpVerboseState(prevState, theAutomataIndexForm));
   }
   }
   }
   }
   */
  /**
   * Returns a string with events from the initial state to currState "a" -> "b"
   * -> "c"
   */
  public String displayTrace(final int[] currState)
  {
    // AutomataOnlineSynchronizer executer = new AutomataOnlineSynchronizer(this);
    final AutomataSynchronizerExecuter executer =
     new AutomataSynchronizerExecuter(this);

    executer.initialize();

    final int prevStateIndex = AutomataIndexFormHelper.getPrevStateIndex(
     currState);

    if (prevStateIndex != AutomataIndexFormHelper.STATE_NO_PREVSTATE) {
      final int[] prevState = theStates.get(prevStateIndex);

      if (prevState != null) {
        final String prevString = displayTrace(prevState);
        final int currEventIndex = executer.findTransition(prevState, currState);

        if (currEventIndex >= 0) {
          if (prevString.equals("")) {
            //return prevString + unionAlphabet.getEventWithIndex(currEventIndex);
            final LabeledEvent event = getIndexMap().getEventAt(currEventIndex);
            return prevString + event;
          } else {
            //return prevString + " -> " + unionAlphabet.getEventWithIndex(currEventIndex);
            final LabeledEvent event = getIndexMap().getEventAt(currEventIndex);
            return prevString + " -> " + event;
          }

          // logger.info(unionAlphabet.getEventWithIndex(currEventIndex).getLabel());
        } else {
          logger.error("Error in AutomataSynchronizerHelper");
          logger.error(
           "Could not find an event between prevState and currState\n");
          logger.error("Current state, index: " + theStates.getIndex(currState));
          logger.error(AutomataIndexFormHelper.dumpVerboseState(currState,
                                                                theAutomataIndexForm));
          logger
           .error("Previous state, index: " + theStates.getIndex(prevState));
          logger.error(AutomataIndexFormHelper.dumpVerboseState(prevState,
                                                                theAutomataIndexForm));

          return "";
        }
      } else {
        return "";
      }
    } else {
      return "";
    }
  }

  public void setCoExecute(final boolean coExecute)
  {
    this.coExecute = coExecute;
  }

  public boolean getCoExecute()
  {
    return coExecute;
  }

  //public void setCoExecuter(AutomataOnlineSynchronizer coExecuter)
  public void setCoExecuter(final AutomataSynchronizerExecuter coExecuter)
  {
    this.coExecuter = coExecuter;
  }

  //public AutomataOnlineSynchronizer getCoExecuter()
  public AutomataSynchronizerExecuter getCoExecuter()
  {
    return coExecuter;
  }

  public void printUncontrollableStates()
   throws Exception
  {
    final int[] automataIndices = new int[theAutomata.size()];

    for (int i = 0; i < theAutomata.size(); i++) {
      automataIndices[i] = i;
    }

    printUncontrollableStates(automataIndices);
  }

  public void printUncontrollableStates(final int[] automataIndices)
   throws Exception
  {
    int problemPlant;
    int problemEvent;
    Automaton problemAutomaton;
    int[] currState = new int[automataIndices.length];
    final State[][] stateTable = getIndexFormStateTable();
    final AutomataIndexMap indexMap = theAutomataIndexForm.getIndexMap();

    for (final Iterator<?> stateHolderIterator = stateMemorizer.iterator(
     automataIndices);
         stateHolderIterator.hasNext();) {
      final StateHolder stateHolder = (StateHolder) stateHolderIterator.next();

      currState = stateHolder.getArray();
      problemPlant = stateHolder.getProblemPlant();
      problemEvent = stateHolder.getProblemEvent();
      problemAutomaton = indexMap.getAutomatonAt(problemPlant);

      final StringBuffer state = new StringBuffer();
      boolean firstEntry = true;

      for (int i = 0; i < currState.length; i++) {
        // Only print states that are not initial if we are looking at a full state
        if (!stateTable[automataIndices[i]][currState[i]].isInitial()
         || (automataIndices.length < theAutomata.size())) {
          if (firstEntry) {
            firstEntry = false;
          } else {
            state.append(", ");
          }

          state.append(indexMap.getAutomatonAt(automataIndices[i]).getName());
          state.append(": ");
          state.append(stateTable[automataIndices[i]][currState[i]].getName());
        }
      }

      //String reason = "the event " + theAutomata.getAlphabet().getEventWithIndex(problemEvent) +
      final String reason = "the event " + theAutomataIndexForm
       .getAutomataIndexMap().getEventAt(problemEvent) + " is enabled in "
       + problemAutomaton;

      // Log the message
      if (!state.toString().equals("")) {
        logger.info("The state " + state + " is uncontrollable since " + reason
         + ".");
      } else {
        logger.info("The initial state is uncontrollable since " + reason + ".");
      }
    }
  }

  public boolean isAllAutomataPlants()
  {
    return theAutomata.isAllAutomataPlants();
  }

  public boolean isAllAutomataSupervisors()
  {
    return theAutomata.isAllAutomataSupervisors();
  }

  public boolean isAllAutomataSpecifications()
  {
    return theAutomata.isAllAutomataSpecifications();
  }

  public void selectAutomata(final int[] automataIndices)
  {
    if (activeAutomata == null) {
      activeAutomata = new boolean[theAutomata.size()];
    } else {
      for (int i = 0; i < activeAutomata.length; i++) {
        activeAutomata[i] = false;
      }
    }

    for (int i = 0; i < automataIndices.length; i++) {
      activeAutomata[automataIndices[i]] = true;
    }
  }

  public void stopExecutionAfter(final int stopExecutionLimit)
  {
    this.stopExecutionLimit = stopExecutionLimit;
  }

  /**
   * Redefines the controllableEventsTable so that all events are considered
   * uncontrollable. Used in the AutomataVerifier when performing language
   * inclusion verifications.
   */
  public void considerAllEventsUncontrollable()
  {
    final boolean[] controllableEventsTable = theAutomataIndexForm
     .getControllableEventsTable();

    for (int i = 0; i < controllableEventsTable.length; i++) {
      controllableEventsTable[i] = false;
    }
  }

  /**
   * Inverts the values of the controllableEventsTable.
   */
  public void invertControllability()
  {
    final boolean[] controllableEventsTable = theAutomataIndexForm
     .getControllableEventsTable();
    for (int i = 0; i < controllableEventsTable.length; i++) {
      controllableEventsTable[i] = !controllableEventsTable[i];
    }
  }

  //Functions declraed for extended automata
  public EdgeSubject getEdge(final String automaton, final String fromState,
                             final String toState, final String event)
  {
    final int automatonIndex = autName2indexTable.get(automaton);

    final HashMap<Arc, EdgeSubject>[] map = arc2EdgeTable;
    for (final Arc arc : map[automatonIndex].keySet()) {
      if (arc.getSource().getName().equals(fromState)
       && arc.getTarget().getName().equals(toState)
       && arc.getEvent().getName().equals(event)) {
        return map[automatonIndex].get(arc);
      }
    }

    return null;
  }

  public SimpleNodeProxy importNode(final State state)
  {
    final String name = state.getName();
    final boolean initial = state.isInitial();
    final Collection<SimpleIdentifierProxy> idents =
     new TreeSet<SimpleIdentifierProxy>();
    final Collection<EventProxy> props = state.getPropositions();
    for (final EventProxy prop : props) {
//          checkEvent(prop);
      final SimpleIdentifierProxy ident = importEvent(prop);
      idents.add(ident);
    }
    final PlainEventListProxy list = mFactory.createPlainEventListProxy(idents);
    return mFactory.createSimpleNodeProxy(name, list, null,
                                          initial, null, null, null);
  }

  public SimpleIdentifierProxy importEvent(final EventProxy event)
  {
    final String name = event.getName();
    return mFactory.createSimpleIdentifierProxy(name);
  }

  public EdgeSubject importEdge(final State fromState, final State toState,
                                final Set<EventProxy> events,
                                final GuardActionBlockProxy guardAction)
  {
    final NodeProxy fromNode = mCurrentNodeMap.get(fromState);
    final NodeProxy toNode = mCurrentNodeMap.get(toState);

    final int numevents = events.size();
    final Collection<SimpleIdentifierProxy> labels =
     new ArrayList<SimpleIdentifierProxy>(numevents);
    for (final EventProxy event : events) {
      final SimpleIdentifierProxy label = importEvent(event);
      labels.add(label);
    }
    final LabelBlockProxy labelblock = mFactory.createLabelBlockProxy(labels,
                                                                      null);
    final EdgeProxy edge = mFactory
     .createEdgeProxy(fromNode, toNode, labelblock, guardAction, null, null,
                      null);

    return (EdgeSubject) edge;
  }

  @SuppressWarnings("unused")
  private void checkEvent(final EventProxy event)
  {
    if (mCurrentEvents.contains(event)) {
      mCurrentBlockedEvents.remove(event);
    } else {
      throw new ItemNotFoundException("Automaton '"
       + "'the synchronized automaton'" + "' does not contain the event named '"
       + event.getName() + "'!");
    }
  }

  public void createExtendedAutomaton()
  {
    createExtendedAutomaton("");
  }

  public void createExtendedAutomaton(String name)
  {
    LabelBlockProxy blockedblock = null;
    final int numblocked = mCurrentBlockedEvents.size();
    if(numblocked > 0){
      final Collection<SimpleIdentifierProxy> blockedlabels =
       new ArrayList<SimpleIdentifierProxy>(numblocked);
      for (final EventProxy event : mCurrentBlockedEvents) {
        final SimpleIdentifierProxy label = importEvent(event);
        blockedlabels.add(label);
      }
      blockedblock = mFactory.createLabelBlockProxy(blockedlabels, null);
    }
    final Collection<SimpleNodeProxy> nodes = mCurrentNodeMap.values();
    final boolean deterministic = true;
    final GraphProxy graph =
     mFactory.createGraphProxy(deterministic, blockedblock,
                               nodes, mEdges);
    name = name.isEmpty() ? "Synchronized Automaton" : name;
    final SimpleIdentifierProxy ident = 
     mFactory.createSimpleIdentifierProxy(name);

    synchronizedComponent = mFactory.createSimpleComponentProxy(ident,
                                                                ComponentKind.PLANT,
                                                                graph);
  }

  public SimpleComponentProxy getSynchronizedComponent()
  {
    return synchronizedComponent;
  }
}
