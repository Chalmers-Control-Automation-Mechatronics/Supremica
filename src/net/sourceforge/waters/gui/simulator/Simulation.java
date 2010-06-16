package net.sourceforge.waters.gui.simulator;

import gnu.trove.THashSet;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class Simulation implements ModelObserver, Observer
{

  //#########################################################################
  //# Constructor
  Simulation(final ModuleContainer container)
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

    mModuleContainer = container;
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, optable);
    mSimulationObservers = new ArrayList<SimulationObserver>();
    mToolTipVisitor = new ToolTipVisitor(this);
    mStateHistory = new ArrayList<SimulatorState>();

    final ModuleSubject module = container.getModule();
    module.addModelObserver(this);
    container.attach(this);
    final ProductDESProxy des = container.getCompiledDES();
    setCompiledDES(des);

    /*
    mTransitionsToEvents = new TransitionEventMap(des);
    updateControllability(true);
    findEventClassification();
    addNewSimulatorState();
    */
  }


  //#########################################################################
  //# Simple Access
  public ModuleContainer getModuleContainer()
  {
    return mModuleContainer;
  }

  public SimpleExpressionCompiler getSimpleExpressionCompiler()
  {
    return mSimpleExpressionCompiler;
  }

  public int getCurrentTime()
  {
    return mCurrentTime;
  }

  public int getHistorySize()
  {
    return mStateHistory.size();
  }

  public void setTrace(final TraceProxy trace)
  {
    mTrace = trace;
    mTraceInvalidated = false;
  }

  public TraceProxy getTrace()
  {
    if (mTraceInvalidated) {
      return null;
    } else {
      return mTrace;
    }
  }

  public ToolTipVisitor getToolTipVisitor()
  {
    return mToolTipVisitor;
  }


  //###################################################################################
  //# Control
  public void setState(final AutomatonProxy aut, final StateProxy state)
  {
    if (!aut.getStates().contains(state)) {
      throw new IllegalArgumentException
        ("Automaton " + aut.getName() +
         " does not contain state " + state.getName() + "!");
    }
    final SimulatorState newState = new SimulatorState(null, mCurrentState);
    newState.setState(aut, state, AutomatonStatus.OK);
    // This state changes the simulation so it is different from the trace
    mTraceInvalidated = true;
    removeFutureSteps();
    addNewSimulatorState(newState);
    mCurrentTime++;
    loadSimulatorState();
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  /**
   * Resets the simulation to either its initial state, or if it has a trace,
   * restores the trace. Note that this method is used to initialise the
   * simulation as well, so simply setting the current time to 0 and calling
   * loadSimulatorState() will not work, as it will never be initialised.
   * @param destroyTrace
   *          If this is <STRONG>true</STRONG> then it will ALWAYS return to
   *          its initial state and it will remove the trace
   */
  public void resetState(final boolean destroyTrace)
  {
    if (mTrace != null && !destroyTrace) {
      executeTrace(mTrace, mAllowLastStep);
    } else {
      mTrace = null; // If we are to destroy the trace
      mTraceInvalidated = true;
      updateAutomata();
      final SimulatorState state = new SimulatorState(mOrderedAutomata);
      mStateHistory.clear();
      mStateHistory.add(state);
      mCurrentTime = 0;
      loadSimulatorState();
    }
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  /**
   * Executes an event. This method checks whether the given event is
   * enabled, and if so determines its possible nondeterministic successor
   * states. If more than one successor state is found, a dialog is popped
   * up, so the user can choose one possibility. The chosen step or
   * the only possible step is then executed. If the event is not enabled,
   * an error message is printed in the IDE's log panel.
   * @param event The event to be executed.
   */
  public void step(final EventProxy event)
  {
    final List<Step> steps = new ArrayList<Step>();
    for (final Step step: getEnabledSteps()) {
      if (step.getEvent() == event) {
        steps.add(step);
      }
    }
    if (steps.isEmpty()) {
      AutomatonProxy disabling = null;
      for (final AutomatonProxy aut : getAutomataSensitiveToEvent(event)) {
        final EventStatus status = getEventStatus(event, aut);
        if (!status.canBeFired()) {
          disabling = aut;
          break;
        }
      }
      assert disabling != null :
        "Can't find automaton disabling event " + event.getName() + "!";
      final EventKind ekind = event.getKind();
      final ComponentKind akind = disabling.getKind();
      final String msg =
        ModuleContext.getEventKindToolTip(ekind) + ' ' + event.getName() +
        " is disabled by " + ModuleContext.getComponentKindToolTip(akind) +
        ' ' + disabling.getName() + '.';
      final IDE ide = mModuleContainer.getIDE();
      ide.error(msg);
    } else {
      step(steps);
    }
  }

  /**
   * Executes a step from a list of choices.
   * @param possibleSteps Non-empty list of steps to be offered to the user.
   *                      If the list contains more than one item, a dialog
   *                      is popped up.
   */
  public void step(final List<Step> possibleSteps)
  {
    final int size = possibleSteps.size();
    assert size > 0 : "Can't step with empty list of choices!";
    final Step step;
    if (size == 1) {
      step = possibleSteps.get(0);
    } else {
      final JLabel[] labels = new JLabel[size];
      final Step[] steps = new Step[size];
      for (int looper = 0; looper < size; looper++) {
        final Step possible = possibleSteps.get(looper);
        final JLabel label = new JLabel(possible.toString());
        final EventProxy event = possible.getEvent();
        final EventKind kind = event.getKind();
        final Icon icon = ModuleContext.getEventKindIcon(kind);
        label.setIcon(icon);
        labels[looper] = label;
        steps[looper] = possible;
      }
      final IDE ide = mModuleContainer.getIDE();
      final EventChooserDialog dialog =
        new EventChooserDialog(ide, labels, steps);
      dialog.setVisible(true);
      if (dialog.wasCancelled()) {
        return;
      }
      step = dialog.getSelectedStep();
      if (step == null) {
        return;
      }
    }
    step(step);
  }

  /**
   * Moves the simulation forward one instruction, by firing a step.
   * @param step The instruction to be followed
   */
  public void step(final Step step)
  {
    final SimulatorState newState =
      SimulatorState.createSuccessorState(mCurrentState, step);
    removeFutureSteps();
    addNewSimulatorState(newState);
    mCurrentTime++;
    loadSimulatorState();
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  /**
   * Moves the simulation state back one step, without changing the trace
   */
  public void stepBack()
  {
    moveSafely(false);
  }
  /**
   * Moves the simulation state forward one step, without changing the trace.
   * If the trace is a loopTrace, if that trace hasn't been invalidated by the
   * step() and setState(...) methods, and if the current state is the final state
   * it will move the simulation state to the beginning of the loop instead.
   */
  public void replayStep()
  {
    if (mTrace != null)
    {
      if (mTrace instanceof LoopTraceProxy && !mTraceInvalidated)
      {
        if (mCurrentTime == mStateHistory.size() - 1)
        {
          mModuleContainer.getIDE().info(": Looping to start of control loop");
          while (mCurrentTime != ((LoopTraceProxy)mTrace).getLoopIndex() + 1)
          {
            stepBack();
          }
        }
        else
          moveSafely(true);
      }
      else
        moveSafely(true);
    }
    else
      moveSafely(true);
  }

  public void switchToTraceMode(final TraceProxy trace)
  {
    final boolean allowLast = !(trace instanceof SafetyTraceProxy);
    executeTrace(trace, allowLast);
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }


  //#########################################################################
  //# Accessing the Product DES
  ProductDESProxy getCompiledDES()
  {
    return mCompiledDES;
  }

  List<AutomatonProxy> getOrderedAutomata()
  {
    updateAutomata();
    return mOrderedAutomata;
  }

  public AutomatonProxy getAutomatonFromName(final String name)
  {
    updateAutomata();
    return mAutomataMap.get(name);
  }

  List<EventProxy> getOrderedEvents()
  {
    updateEvents();
    return mOrderedEvents;
  }

  public List<AutomatonProxy> getAutomataSensitiveToEvent
    (final EventProxy event)
  {
    updateAutomataSensitiveToEvent();
    final List<AutomatonProxy> list = mAutomataSensitiveToEvent.get(event);
    if (list == null) {
      return Collections.emptyList();
    } else {
      return list;
    }
  }

  /**
   * @param state
   *          The state to be drawn
   * @param automaton
   *          The automaton the state belongs to
   * @param drawAsEditor
   *          <STRONG>true</STRONG> if a state inside an automaton with no
   *          accepting states should be drawn with a white inner,
   *          <STRONG>false</STRONG> if it should be drawn with a grey (IE.
   *          Accepting) inner
   * @return The icon of a state, taking into account propositions
   */
  Icon getMarkingIcon(final StateProxy state,
                      final AutomatonProxy automaton,
                      final boolean drawAsEditor)
  {
    final PropositionIcon.ColorInfo info = getMarkingColorInfo(state, automaton, drawAsEditor);
    return info.getIcon();
  }

  /**
   * @param state
   *          The state to be drawn
   * @param automaton
   *          The automaton the state belongs to
   * @param drawAsEditor
   *          <STRONG>true</STRONG> if a state inside an automaton with no
   *          accepting states should be drawn with a white inner,
   *          <STRONG>false</STRONG> if it should be drawn with a grey (IE.
   *          Accepting) inner
   * @return The colour of a state, taking into account propositions.
   */
  PropositionIcon.ColorInfo getMarkingColorInfo
    (final StateProxy state, final AutomatonProxy automaton,
     final boolean drawAsEditor)
  {
    if (!hasPropositions(automaton) && drawAsEditor) {
      return PropositionIcon.getUnmarkedColors();
    }
    else if (!hasPropositions(automaton) && !drawAsEditor)
    {
      return PropositionIcon.getDefaultMarkedColors();
    }
    final Collection<EventProxy> props = state.getPropositions();
    if (props.isEmpty() && drawAsEditor) {
      return PropositionIcon.getUnmarkedColors();
    }
    else {
      final Map<Proxy,SourceInfo> infomap = mModuleContainer.getSourceInfoMap();
      if (infomap == null)
        return new PropositionIcon.ColorInfo(new ArrayList<Color>(), false);
      final int size = props.size();
      final Set<Color> colorset = new HashSet<Color>(size);
      final List<Color> colorlist = new ArrayList<Color>(size);
      boolean forbidden = false;
      for (final EventProxy prop : props) {
        final SourceInfo info = infomap.get(prop);
        final EventDeclProxy decl = (EventDeclProxy) info.getSourceObject();
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo != null) {
          for (final Color color : geo.getColorSet()) {
            if (colorset.add(color)) {
              colorlist.add(color);
            }
          }
        } else if (decl.getName().equals
            (EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          forbidden = true;
        } else {
          if (colorset.add(EditorColor.DEFAULTMARKINGCOLOR)) {
            colorlist.add(EditorColor.DEFAULTMARKINGCOLOR);
          }
        }
      }
      return new PropositionIcon.ColorInfo(colorlist, forbidden);
    }
  }


  //#########################################################################
  //# Accessing the State
  SimulatorState getCurrentState()
  {
    return mCurrentState;
  }

  SimulatorState getHistoryState(final int time)
  {
    return mStateHistory.get(time);
  }

  /**
   * Gets the current state of the given automaton in the simulation.
   */
  StateProxy getCurrentState(final AutomatonProxy aut)
  {
    return mCurrentState.getState(aut);
  }

  AutomatonStatus getAutomatonStatus(final AutomatonProxy aut)
  {
    return mCurrentState.getStatus(aut);
  }

  /**
   * This method is used for displaying automata in the AutomatonTable.
   * Similar, but different code is fired in the EventJTree for it's representation of images
   * of automata
   * @param aut The automaton which is to be drawn
   * @return An image. A tick if that automaton was enabled in the last step
   * a cross if that automaton is disabled
   * a yellow flag if that automaton is a property which can be disabled if the correct event is fired
   * a red flag, if that automaton is causing a controllability problem
   * no image, if none of the above are true.
   */
  ImageIcon getAutomatonActivityIcon(final AutomatonProxy aut)
  {
    final AutomatonStatus status = getAutomatonStatus(aut);
    return status.getIcon();
  }


  public EventStatus getEventStatus(final EventProxy event)
  {
    final EventEntry entry = mEventStatusMap.get(event);
    return entry.getStatus();
  }

  public EventStatus getEventStatus(final EventProxy event,
                                    final AutomatonProxy aut)
  {
    final EventEntry entry = mEventStatusMap.get(event);
    return entry.getStatus(aut);
  }

  public String getEventStatusText(final EventProxy event)
  {
    final EventEntry entry = mEventStatusMap.get(event);
    return entry.getStatusText();
  }

  /**
   * @param event The event which is to be drawn
   * @return An image. A tick if the event can be fired by at least one possible step,
   * a cross if that event is blocked by some automata,
   * a yellow flag if firing that event will disable an event, or
   * a red flag, if that event is causing a controllability problem
   */
  ImageIcon getEventActivityIcon(final EventProxy event)
  {
    final EventStatus status = getEventStatus(event);
    return status.getIcon();
  }

  public List<Step> getEnabledSteps()
  {
    updateEventStatus();
    return mEnabledSteps;
  }

  TransitionProxy getPreviousTransition(final AutomatonProxy aut)
  {
    if (mCurrentTime == 0) {
      return null;
    } else {
      // TODO Add cache
      final EventProxy event = mCurrentState.getEvent();
      if (!aut.getEvents().contains(event)) {
        return null;
      }
      final SimulatorState prev = mStateHistory.get(mCurrentTime - 1);
      final StateProxy source = prev.getState(aut);
      final StateProxy target = mCurrentState.getState(aut);
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (trans.getSource() == source &&
            trans.getEvent() == event &&
            trans.getTarget() == target) {
          return trans;
        }
      }
      throw new IllegalArgumentException
        ("Previous transition not found for automaton" + aut.getName() + "!");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  // This event is received when the user has made changes to the
  // model in the editor. The handler invalidates the current
  // compiled DES; it will be recompiled later when the simulator
  // tab is activated.
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() != ModelChangeEvent.GEOMETRY_CHANGED) {
      setCompiledDES(null);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Observer
  // This event is received when a new tab has been activated in a
  // module container, after recompiling the module. If the activated
  // tab was the simulator, it is now safe to get an updated compiled
  // DES from the module container. After storing the new simulation
  // state in the simulator, all registered views are notified to
  // update themselves.
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH &&
        mModuleContainer.getActivePanel() instanceof SimulatorPanel) {
      final ProductDESProxy newdes = mModuleContainer.getCompiledDES();
      setCompiledDES(newdes);
    }
  }

  public void attach(final SimulationObserver observer)
  {
    if (!mSimulationObservers.contains(observer)) {
      mSimulationObservers.add(observer);
    }
  }

  public void detach(final SimulationObserver observer)
  {
    mSimulationObservers.remove(observer);
  }


  //#########################################################################
  //# Updating the DES
  /**
   * Stores a new compiled DES. Recomputes all data associated with it
   * and notifies registered views of the change.
   */
  private void setCompiledDES(final ProductDESProxy des)
  {
    if (des != mCompiledDES) {
      mCompiledDES = des;
      mOrderedEvents = null;
      mOrderedAutomata = null;
      mAutomataMap = null;
      mAutomataSensitiveToEvent = null;
      resetState(true);
    }
  }

  private void updateAutomata()
  {
    if (mOrderedAutomata == null) {
      if (mCompiledDES == null) {
        mOrderedAutomata = Collections.emptyList();
      } else {
        final Collection<AutomatonProxy> automata = mCompiledDES.getAutomata();
        final int numAutomata = automata.size();
        mOrderedAutomata = new ArrayList<AutomatonProxy>(numAutomata);
        mAutomataMap = new HashMap<String,AutomatonProxy>(numAutomata);
        for (final AutomatonProxy aut : automata) {
          mOrderedAutomata.add(aut);
          final String name = aut.getName();
          mAutomataMap.put(name, aut);
        }
        Collections.sort(mOrderedAutomata);
      }
    }
  }

  private void updateEvents()
  {
    if (mOrderedEvents == null) {
      if (mCompiledDES == null) {
        mOrderedEvents = Collections.emptyList();
      } else {
        final Collection<EventProxy> events = mCompiledDES.getEvents();
        final int numEvents = events.size();
        mOrderedEvents = new ArrayList<EventProxy>(numEvents);
        for (final EventProxy event : events) {
          if (event.getKind() != EventKind.PROPOSITION) {
            mOrderedEvents.add(event);
          }
        }
        Collections.sort(mOrderedEvents);
      }
    }
  }

  private void updateAutomataSensitiveToEvent()
  {
    if (mAutomataSensitiveToEvent == null && mCompiledDES != null) {
      updateAutomata();
      updateEvents();
      final int numEvents = mOrderedEvents.size();
      mAutomataSensitiveToEvent =
        new HashMap<EventProxy,List<AutomatonProxy>>(numEvents);
      for (final AutomatonProxy aut : mOrderedAutomata) {
        for (final EventProxy event : aut.getEvents()) {
          if (event.getKind() != EventKind.PROPOSITION) {
            List<AutomatonProxy> list = mAutomataSensitiveToEvent.get(event);
            if (list == null) {
              list = new ArrayList<AutomatonProxy>();
              mAutomataSensitiveToEvent.put(event, list);
            }
            list.add(aut);
          }
        }
      }
    }
  }


  //#########################################################################
  //# Updating the state.
  /**
   * Adds a new simulation state to the end of the current history.
   * Thus, it is only used for step methods.
   */
  private void addNewSimulatorState(final SimulatorState state)
  {
    mStateHistory.add(state);
  }

  /**
   * This method takes the current time of the simulation, and changes the
   * current states of the simulation to match the current time. It is used for
   * stepping back and forward through the simulation without changing it.
   */
  private void loadSimulatorState()
  {
    mCurrentState = mStateHistory.get(mCurrentTime);
    mEnabledSteps = null;
    mEventStatusMap = null;
  }

  /**
   * Re-evaluates the lists of enabled events ({@link #mEnabledSteps}) and the
   * event status map ({@link #mEventStatusMap}). This method assumes that the
   * current state ({@link #mCurrentState}) is correctly set, but it adds
   * warning and error status to {@link #mCurrentState} if the need is
   * discovered.
   */
  private void updateEventStatus()
  {
    if (mEnabledSteps == null) {
      updateEvents();
      final int numGlobal = mOrderedEvents.size();
      final Set<EventProxy> enabled = new THashSet<EventProxy>(numGlobal);
      mEventStatusMap = new HashMap<EventProxy,EventEntry>(numGlobal);
      for (final EventProxy event : mOrderedEvents) {
        final Collection<AutomatonProxy> automata =
          getAutomataSensitiveToEvent(event);
        final int numAut = automata.size();
        final EventEntry entry = new EventEntry(numAut);
        mEventStatusMap.put(event, entry);
      }
      final ComponentKind[] order = new ComponentKind[]
        {ComponentKind.PLANT, ComponentKind.SPEC, ComponentKind.PROPERTY};
      for (final ComponentKind kind : order) {
        for (final AutomatonProxy aut : mOrderedAutomata) {
          if (aut.getKind() == kind) {
            final AutomatonStatus status = mCurrentState.getStatus(aut);
            if (status != AutomatonStatus.DISABLED) {
              final StateProxy source = mCurrentState.getState(aut);
              final Collection<EventProxy> local = aut.getEvents();
              for (final TransitionProxy trans : aut.getTransitions()) {
                if (trans.getSource() == source) {
                  final EventProxy event = trans.getEvent();
                  enabled.add(event);
                }
              }
              for (final EventProxy event : local) {
                if (event.getKind() != EventKind.PROPOSITION &&
                    !enabled.contains(event)) {
                  final EventEntry entry = mEventStatusMap.get(event);
                  switch (aut.getKind()) {
                  case PLANT:
                    entry.setStatus(aut, EventStatus.DISABLED);
                    break;
                  case SPEC:
                    if (event.getKind() == EventKind.CONTROLLABLE ||
                        entry.getStatus() == EventStatus.DISABLED) {
                      entry.setStatus(aut, EventStatus.DISABLED);
                    } else {
                      entry.setStatus(aut, EventStatus.ERROR);
                      mCurrentState.addStatus(aut, AutomatonStatus.ERROR);
                    }
                    break;
                  case PROPERTY:
                    if (entry.getStatus() == EventStatus.DISABLED) {
                      entry.setStatus(aut, EventStatus.DISABLED);
                    } else {
                      entry.setStatus(aut, EventStatus.WARNING);
                      mCurrentState.addStatus(aut, AutomatonStatus.WARNING);
                    }
                    break;
                  default:
                    throw new IllegalStateException
                    ("Unknown component kind " + aut.getKind() + "!");
                  }
                }
              }
              enabled.clear();
            }
          }
        }
      }
      final int numAutomata = mOrderedAutomata.size();
      final Map<AutomatonProxy,StateProxy> source =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      final Map<AutomatonProxy,StateProxy> target =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      for (final AutomatonProxy aut : mOrderedAutomata) {
        final StateProxy state = mCurrentState.getState(aut);
        source.put(aut, state);
        target.put(aut, state);
      }
      mEnabledSteps = new ArrayList<Step>();
      for (final EventProxy event : mOrderedEvents) {
        if (getEventStatus(event) == EventStatus.ENABLED) {
          final List<AutomatonProxy> automata =
            getAutomataSensitiveToEvent(event);
          createSteps(event, source, target, automata, 0);
        }
      }
    }
  }

  private void createSteps(final EventProxy event,
                           final Map<AutomatonProxy,StateProxy> sourceTuple,
                           final Map<AutomatonProxy,StateProxy> targetTuple,
                           final List<AutomatonProxy> automata,
                           final int index)
  {
    if (index == automata.size()) {
      final Map<AutomatonProxy,StateProxy> targetCopy =
        new HashMap<AutomatonProxy,StateProxy>(targetTuple);
      final Step step = new Step(event, sourceTuple, targetCopy);
      mEnabledSteps.add(step);
    } else {
      final int next = index + 1;
      final AutomatonProxy aut = automata.get(index);
      final StateProxy source = sourceTuple.get(aut);
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (trans.getSource() == source && trans.getEvent() == event) {
          final StateProxy target = trans.getTarget();
          targetTuple.put(aut, target);
          createSteps(event, sourceTuple, targetTuple, automata, next);
        }
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Resets the simulation, and then loads the trace
   * @param trace The trace to load
   * @param allowLastStep <STRONG>true</STRONG> if the last step in the TraceProxy is to be fired, <STRONG>false</STRONG>
   * if the simulation is to be stopped at the second-to-last step instead.
   */
  private void executeTrace(final TraceProxy trace,
                            final boolean allowLastStep)
  {
    mTrace = trace;
    mTraceInvalidated = false;
    mAllowLastStep = allowLastStep;
    mStateHistory.clear();
    mCurrentTime = -1;
    SimulatorState state = null;
    final List<TraceStepProxy> list = trace.getTraceSteps();
    final Iterator<TraceStepProxy> iter = list.iterator();
    while (iter.hasNext()) {
      final TraceStepProxy traceStep = iter.next();
      if (iter.hasNext() || mAllowLastStep) {
        final EventProxy event = traceStep.getEvent();
        if (state == null) {
          state = SimulatorState.createInitialState
            (mOrderedAutomata, traceStep);
        } else {
          state = SimulatorState.createSuccessorState(state, event, traceStep);
        }
        mStateHistory.add(state);
        mCurrentTime++;
      }
    }
    loadSimulatorState();
  }

  /**
   * Moves the simulation state forward one step, or backward one step, without
   * changing the trace.
   * @param forward
   *          Moves the simulation forward if <STRONG>true</STRONG>, backwards
   *          otherwise.
   */
  private void moveSafely(final boolean forward)
  {
    if (moveTime(forward)) {
      loadSimulatorState();
      final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
      fireSimulationChangeEvent(simEvent);
    }
  }

  /**
   * Increments or decrements the current time counter. If it is the last step
   * and is moving forward or if it is the first step and it is moving backward,
   * then it will print an error message instead.
   */
  private boolean moveTime(final boolean forward)
  {
    if (forward) {
      if (mCurrentTime == mStateHistory.size() - 1) {
        final IDE ide = mModuleContainer.getIDE();
        ide.error("No future events in simulation history!");
        return false;
      } else {
        mCurrentTime++;
        return true;
      }
    } else {
      if (mCurrentTime == 0) {
        final IDE ide = mModuleContainer.getIDE();
        ide.error("No previous event in simulation history!");
        return false;
      } else {
        mCurrentTime--;
        return true;
      }
    }
  }

  /**
   * Removes all future information from the simulation. This is used if the
   * simulation is doing something that changes the trace while the current time
   * is not the last time reached in the simulation. This invalidates all
   * later states.
   */
  private void removeFutureSteps()
  {
    int index = mStateHistory.size() - 1;
    while (index > mCurrentTime) {
      mStateHistory.remove(index--);
    }
  }

  private void fireSimulationChangeEvent(final SimulationChangeEvent event)
  {
    updateEventStatus();
    final ArrayList<SimulationObserver> temp =
      new ArrayList<SimulationObserver>(mSimulationObservers);
    for (final SimulationObserver observer : temp) {
      observer.simulationChanged(event);
    }
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static boolean hasPropositions(final AutomatonProxy automaton)
  {
    for (final EventProxy event : automaton.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {
        return true;
      }
    }
    return false;
  }


  //#########################################################################
  //# Inner Class EventEntry
  private static class EventEntry
  {
    //#########################################################################
    //# Constructor
    private EventEntry(final int numAutomata)
    {
      mStatus = EventStatus.ENABLED;
      mAutomataMap = new HashMap<AutomatonProxy,EventStatus>(numAutomata);
    }

    //#########################################################################
    //# Simple Access
    private EventStatus getStatus()
    {
      return mStatus;
    }

    private EventStatus getStatus(final AutomatonProxy aut)
    {
      final EventStatus status = mAutomataMap.get(aut);
      if (status == null) {
        return EventStatus.ENABLED;
      } else {
        return status;
      }
    }

    private String getStatusText()
    {
      final StringBuffer buffer;
      switch (mStatus) {
      case DISABLED:
        return "currently disabled";
      case ENABLED:
        return "currently enabled";
      case WARNING:
        buffer = new StringBuffer("causes violation of property ");
        break;
      case ERROR:
        buffer =
          new StringBuffer("violates controllability of specification ");
        break;
      default:
        throw new IllegalStateException
          ("Unknown event status " + mStatus + "!");
      }
      boolean first = true;
      for (final Map.Entry<AutomatonProxy,EventStatus> entry :
           mAutomataMap.entrySet()) {
        if (entry.getValue() == mStatus) {
          if (first) {
            first = false;
          } else {
            buffer.append(", ");
          }
          final AutomatonProxy aut = entry.getKey();
          final String name = aut.getName();
          buffer.append(name);
        }
      }
      return buffer.toString();
    }

    private void setStatus(final AutomatonProxy aut, final EventStatus status)
    {
      final EventStatus old = mAutomataMap.put(aut, status);
      assert old == null : "Status override not supported!";
      if (mStatus == EventStatus.ENABLED || status.compareTo(mStatus) > 0) {
        mStatus = status;
      }
    }

    //#########################################################################
    //# Data Members
    private EventStatus mStatus;
    private final Map<AutomatonProxy,EventStatus> mAutomataMap;
  }



  //#########################################################################
  //# Data Members

  // Variables remaining unchanged throughout simulator lifetime:
  private final ModuleContainer mModuleContainer;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final List<SimulationObserver> mSimulationObservers;
  private final ToolTipVisitor mToolTipVisitor;

  // Variables recalculated when the product DES is recompiled:
  private ProductDESProxy mCompiledDES;
  private List<EventProxy> mOrderedEvents;
  private List<AutomatonProxy> mOrderedAutomata;
  private Map<String,AutomatonProxy> mAutomataMap;
  private Map<EventProxy, List<AutomatonProxy>> mAutomataSensitiveToEvent;

  // Variables recalculated after each step:
  /**
   * History of steps executed so far.
   */
  private final List<SimulatorState> mStateHistory;
  /**
   *  The current index in the state history.
   */
  private int mCurrentTime;
  /**
   * The current state of the simulation. This is equal to the history
   * entry in {@link #mStateHistory} at index {@link #mCurrentTime}.
   * It contains the previous event and the current state and status
   * of each automaton.
   */
  private SimulatorState mCurrentState;
  /**
   * The current status of each event. The map shows for each event
   * whether it is enabled, disabled, etc. in the current state.
   * For disabled events, the entry object also shows the automata
   * that disable it.
   */
  private Map<EventProxy,EventEntry> mEventStatusMap;
  /**
   * List of currently enabled simulation steps. Each step contains
   * information about an event and its associated nondeterministic
   * successor states.
   */
  private List<Step> mEnabledSteps;

  private TraceProxy mTrace;
  private boolean mAllowLastStep;
  private boolean mTraceInvalidated;

}