package net.sourceforge.waters.gui.simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.IconLoader;
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
import org.supremica.gui.ide.ModuleContainer;


public class Simulation implements ModelObserver, Observer
{
  //#########################################################################
  //# Constructors
  public Simulation(final ModuleContainer container)
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, optable);
    mSimulationObservers = new ArrayList<SimulationObserver>();
    mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>();
    mEnabledEvents = new ArrayList<Step>();
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
    mModuleContainer = container;
    mWarningProperties = new HashMap<Step, AutomatonProxy>();
    mEnabledLastStep = new ArrayList<AutomatonProxy>();
    previousStates = new ArrayList<SimulatorState>();
    mBlockingEvents = new ArrayList<Pair<EventProxy, AutomatonProxy>>();
    mDisabledProperties = new ArrayList<AutomatonProxy>();
    currentTime = 0;
    mContainer = container;
    invalidated = false;
    final ModuleSubject module = container.getModule();
    module.addModelObserver(this);
    container.attach(this);
    final ProductDESProxy des = container.getCompiledDES();
    setCompiledDES(des);
    updateControllability(true);
    findEventClassification();
    addNewSimulatorState();
  }

  // ########################################################################
  // # SimulationState Access Methods

  public Collection<? extends Step> getEnabledEvents()
  {
    return mEnabledEvents;
  }
  public Step getCurrentEvent()
  {
    return mCurrentEvent;
  }
  public Collection<? extends Pair<EventProxy,AutomatonProxy>> getAllBlocking()
  {
     return mBlockingEvents;
  }
  public Collection<? extends AutomatonProxy> getAutomatonActivity()
  {
    return mEnabledLastStep;
  }
  public Collection<? extends AutomatonProxy> getDisabledProperties()
  {
    return mDisabledProperties;
  }
  public Map<? extends Step,? extends AutomatonProxy> getWarningProperties()
  {
    return mWarningProperties;
  }

  private void addNewSimulatorState()
  {
    previousStates.add(new SimulatorState(this));
  }

  private void loadSimulatorState()
  {
    final SimulatorState stateToLoad = previousStates.get(currentTime);
    mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>(stateToLoad.mCurrentStates);
    mEnabledEvents = new ArrayList<Step>(stateToLoad.mEnabledEvents);
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>>(stateToLoad.mInvalidEvents);
    mCurrentEvent = stateToLoad.mCurrentEvent;
    mBlockingEvents = new ArrayList<Pair<EventProxy, AutomatonProxy>>(stateToLoad.mBlockingEvents);
    mEnabledLastStep = new ArrayList<AutomatonProxy>(stateToLoad.mEnabledLastStep);
    mDisabledProperties = new ArrayList<AutomatonProxy>(stateToLoad.mDisabledProperties);
    mWarningProperties = new HashMap<Step, AutomatonProxy>(stateToLoad.mWarningProperties);
    updateControllability(false);
  }

  //#########################################################################
  //# Simple Access
  SimpleExpressionCompiler getSimpleExpressionCompiler()
  {
    return mSimpleExpressionCompiler;
  }

  public List<Step> getValidTransitions()
  {
    return Collections.unmodifiableList(mEnabledEvents);
  }

  public Map<EventProxy,ArrayList<AutomatonProxy>> getInvalidEvents()
  {
    return Collections.unmodifiableMap(mInvalidEvents);
  }

  public Map<AutomatonProxy, StateProxy> getCurrentStates()
  {
    return Collections.unmodifiableMap(mAllAutomatons);
  }

  public List<EventProxy> getAllEvents()
  {
    if (mCompiledDES == null) {
      return Collections.emptyList();
    } else {
      final Collection<EventProxy> events = mCompiledDES.getEvents();
      final ArrayList<EventProxy> output = new ArrayList<EventProxy>();
      for (final EventProxy event : events)
        if (event.getKind() != EventKind.PROPOSITION)
          output.add(event);
      Collections.sort(output);
      return output;
    }
  }

  public String getBlockingTextual()
  {
    String output = "";
    output += "The automaton which are blocking the states are:\r\n";
    for (final EventProxy event : mInvalidEvents.keySet())
    {
      output += event.getName() + ":[";
      for (final AutomatonProxy proxy : mInvalidEvents.get(event))
      {
        output += proxy.getName() + ",";
      }
      output += "]\r\n";
    }
    return output;
  }

  public ImageIcon getEventActivityIcon(final EventProxy event)
  {
    for (final Step blockingStep : mWarningProperties.keySet())
    {
      if (event == blockingStep.getEvent())
        return IconLoader.ICON_YELLOWWARNING;
    }
    for (final Step validStep : mEnabledEvents)
    {
      if (validStep.getEvent() == event)
        return IconLoader.ICON_TICK;
    }
    if (mInvalidEvents.containsKey(event))
    {
      updateControllability(false);
      if (mBlockingEvents != null)
      {
        for (final Pair<EventProxy, AutomatonProxy> blockingEvent : mBlockingEvents)
        {
          if (blockingEvent.getFirst() == event)
            return IconLoader.ICON_WARNING;
        }
      }
      return IconLoader.ICON_CROSS;
    }
    else
    {
      throw new UnsupportedOperationException("ERROR: Unknown event status. Event name: "
                                              + event.getName() + " enabled/invalid size:"
                                              + mEnabledEvents.size() + " / " + mInvalidEvents.size());
    }
  }

  public ImageIcon getAutomatonActivityIcon(final AutomatonProxy aut)
  {
    if (mDisabledProperties.contains(aut))
      return IconLoader.ICON_CROSS;
    if (mWarningProperties.containsValue(aut))
      return IconLoader.ICON_YELLOWWARNING;
    updateControllability(false);
    if (mBlockingEvents != null && aut.getKind() == ComponentKind.SPEC)
    {
      for (final Pair<EventProxy, AutomatonProxy> blockingEvent : mBlockingEvents)
      {
        if (blockingEvent.getSecond() == aut)
            return IconLoader.ICON_WARNING;
      }
    }
    if (mEnabledLastStep.contains(aut))
      return IconLoader.ICON_TICK;
    else
      return new ImageIcon();
  }

  public Set<AutomatonProxy> isNonControllableAtTime(final int time)
  {
    if (time < 0 || time > previousStates.size() - 1)
      return Collections.emptySet();
    else
    {
      final Set<AutomatonProxy> output = new HashSet<AutomatonProxy>();
      for (final Pair<EventProxy, AutomatonProxy> blocking: previousStates.get(time).mBlockingEvents)
      {
        output.add(blocking.getSecond());
      }
      return output;
    }
  }

  /**
   * A check to see if the event is a blocking event (All automaton which are blocked by this event are specifications)
   * @param event The event to test
   * @return null if the event is a non-blocking event, all specifications which are blocking it otherwise
   */
  public ArrayList<AutomatonProxy> getNonControllable(final EventProxy event)
  {
    if (mBlockingEvents == null)
      return null;
    final ArrayList<AutomatonProxy> output = new ArrayList<AutomatonProxy>();
    for (final Pair<EventProxy, AutomatonProxy> blocker : mBlockingEvents)
    {
      if (blocker.getFirst() == event)
        output.add(blocker.getSecond());
    }
    return output;
  }

  /**
   * Returns the automaton which are blocked by this event
   * @param event
   * @return All automaton which are blocked by this event, or an empty arraylist, if the event is valid.
   */
  public List<AutomatonProxy> getBlocking(final EventProxy event)
  {
    if (mInvalidEvents.containsKey(event))
    {
      return Collections.unmodifiableList(mInvalidEvents.get(event));
    }
    else
    {
      return Collections.emptyList();
    }
  }

  public Icon getMarkingIcon(final StateProxy state,
                             final AutomatonProxy automaton,
                             final boolean drawAsEditor)
  {
    final PropositionIcon.ColorInfo info = getMarkingColorInfo(state, automaton, drawAsEditor);
    return info.getIcon();
  }

  public PropositionIcon.ColorInfo getMarkingColorInfo
    (final StateProxy state, final AutomatonProxy automaton, final boolean drawAsEditor)
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

  public boolean changedLastStep(final AutomatonProxy automaton)
  {
    return mEnabledLastStep.contains(automaton);
  }
  public boolean changedSecondLastStep(final AutomatonProxy automaton)
  {
    if (currentTime < 1)
      return false;
    else
      return previousStates.get(currentTime - 1).mEnabledLastStep.contains(automaton);
  }
  public boolean changedNextStep(final AutomatonProxy aut)
  {
    if (currentTime == previousStates.size() - 1)
      return false;
    else
      return previousStates.get(currentTime + 1).mEnabledLastStep.contains(aut);
  }

  public ModuleContainer getContainer()
  {
    return mContainer;
  }

  public List<Step> getEventHistory()
  {
    final ArrayList<Step> output = new ArrayList<Step>();
    for (final SimulatorState state : previousStates)
    {
      output.add(state.mCurrentEvent);
    }
    return output;
  }

  public List<Map<AutomatonProxy, StateProxy>> getAutomatonHistory()
  {
    final ArrayList<Map<AutomatonProxy, StateProxy>> output = new ArrayList<Map<AutomatonProxy, StateProxy>>();
    for (final SimulatorState state : previousStates)
    {
      output.add(state.mCurrentStates);
    }
    return output;
  }

  public ArrayList<AutomatonProxy> getAutomata()
  {
    final ArrayList<AutomatonProxy> output = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy automaton : mAllAutomatons.keySet())
      output.add(automaton);
    Collections.sort(output);
    return output;
  }

  public ArrayList<AutomatonProxy> getAutomatonActivityAtTime(final int time)
  {
    return previousStates.get(time).mEnabledLastStep;
  }

  ProductDESProxy getCompiledDES()
  {
    return mCompiledDES;
  }

  public int getCurrentTime()
  {
    return currentTime;
  }

  public TransitionProxy getPreviousTransition(final AutomatonProxy automaton)
  {
    if (currentTime == -1)
      return null;
    else if (currentTime == 0)
    {
      for (final StateProxy startState : automaton.getStates())
      {
        if (startState.isInitial())
        {
          if (mCurrentEvent == null)
            return null;
          return mCurrentEvent.getTransition(automaton,
                                             startState,
                                             mAllAutomatons.get(automaton));
        }
      }
    }
    else
    {
      if (mCurrentEvent == null)
        return null;
      return mCurrentEvent.getTransition(automaton,
                     previousStates.get(currentTime - 1).mCurrentStates.get(automaton),
                     mAllAutomatons.get(automaton));
    }
    return null;
  }

  public AutomatonProxy getAutomatonFromName(final String automatonFind)
  {
    for (final AutomatonProxy automaton : mAllAutomatons.keySet())
    {
      if (automaton.getName().compareTo(automatonFind) == 0)
        return automaton;
    }
    return null;
  }

  public void setTrace(final TraceProxy trace)
  {
    mTrace = trace;
    invalidated = false;
  }
  public TraceProxy getTrace()
  {
    if (invalidated)
      return null;
    else
      return mTrace;
  }

  public void setState(final AutomatonProxy automaton, final StateProxy state)
  {
    mWarningProperties = new HashMap<Step, AutomatonProxy>();
    if (!automaton.getStates().contains(state))
    {
      throw new IllegalArgumentException("ERROR: " + state.getName() + " does not belong to this automaton");
    }
    if (mAllAutomatons.containsKey(automaton))
      mAllAutomatons.put(automaton, state);
    else
      throw new IllegalArgumentException("ERROR: This automaton is not in this program");
    invalidated = true;
    removeFutureEvents();
    updateControllability(true);
    findEventClassification();
    final ArrayList<AutomatonProxy> auto = new ArrayList<AutomatonProxy>();
    auto.add(automaton);
    mCurrentEvent = null;
    currentTime++;
    addNewSimulatorState();
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  public void reset(final boolean destroyTrace)
  {
    mWarningProperties = new HashMap<Step, AutomatonProxy>();
    if (mTrace != null && !destroyTrace)
    {
      run(mTrace, mAllowLastStep);
    }
    else
    {
      mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>();
      mEnabledEvents = new ArrayList<Step>();
      mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
      if (mCompiledDES != null)
      {
        for (final AutomatonProxy automaton : mCompiledDES.getAutomata())
        {
          for (final StateProxy state : automaton.getStates())
          {
            if (state.isInitial())
            {
              mAllAutomatons.put(automaton, state);
            }
          }
        }
        findEventClassification();
      }
      previousStates = new ArrayList<SimulatorState>();
      mEnabledLastStep = new ArrayList<AutomatonProxy>();
      mCurrentEvent = null;
      mDisabledProperties = new ArrayList<AutomatonProxy>();
      currentTime = 0;
      updateControllability(true);
      addNewSimulatorState();
      final SimulationChangeEvent simEvent = new SimulationChangeEvent
        (this, SimulationChangeEvent.STATE_CHANGED);
      fireSimulationChangeEvent(simEvent);
    }
  }

  public void run(final TraceProxy trace, final boolean allowLastStep)
  {
    mTrace = null;
    mAllowLastStep = false;
    reset(false);
    mAllowLastStep = allowLastStep;
    mTrace = trace;
    Step locatedStep = null;
    boolean firstStep = true;
    final TraceStepProxy lastStep = trace.getTraceSteps().get(trace.getTraceSteps().size() - 1);
    for (final TraceStepProxy tStep : trace.getTraceSteps()) // Travel through each trace step
    {
      locatedStep = null;
      if (!firstStep && (tStep != lastStep || allowLastStep))
      {
        for (final Step step : mEnabledEvents) // Look for all possible Steps in the simulation
        {
          final boolean isTheRightStep = compareTraceStep(step, tStep);
          if (isTheRightStep)
            locatedStep = step; // We have found the next instruction for the trace
        }
        if (locatedStep != null)
        {
          try {
            step(locatedStep); // So fire it, and continue to the next one.
          } catch (final NonDeterministicException exception) {
            // Do nothing
          }
        }
        else
          throw new IllegalArgumentException("No valid step could be found, trace was: " + trace.getTraceSteps());
      }
      else
        firstStep = false;
    }
    invalidated = false;
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  private boolean compareTraceStep(final Step step, final TraceStepProxy tStep)
  {
    if (step.getEvent() == tStep.getEvent()) // Check if the event name is right. If not, this is not the correct Step
    {
      for (final AutomatonProxy auto : this.mAllAutomatons.keySet())
      {
        if (step.getSource().get(auto) != null) // Check to see if the step is non-deterministic. If it isn't, then it is the correct Step
        {
          if (tStep.getStateMap().get(auto) == null) // If there is no non-deterministic information, fail always
            throw new IllegalArgumentException("No non-deterministic information available. Trace is:" + mTrace.getTraceSteps());
          else if (step.getDest().get(auto) == tStep.getStateMap().get(auto)) // If the destinations match, then it is the right step
            {
              return true;
            }
            else
              return false;
        }
        else
          return true;
      }
    }
    else
    {
      return false;
    }
    return false;
  }


  public void step(final Step step) throws NonDeterministicException
  {
    mWarningProperties = new HashMap<Step, AutomatonProxy>();
    time = System.currentTimeMillis();
    if (step == null)
    {
      return;
    }
    checkInvalid(step);
    invalidated = true;
    removeFutureEvents();
    mCurrentEvent = step;
    mEnabledLastStep = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy automata : mAllAutomatons.keySet())
    {
      if (!mDisabledProperties.contains(automata))
      {
        final StateProxy oldLocation = mAllAutomatons.get(automata);
        if (step.getDest().get(automata) != null)
        {
          mAllAutomatons.put(automata, step.getDest().get(automata));
        }
        else
        {
          moveDeterministicTransition(automata, step, oldLocation);
        }
      }
    }
    currentTime++;
    findEventClassification();
    updateControllability(true);
    addNewSimulatorState();
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  private void moveDeterministicTransition(final AutomatonProxy automata,
                                                 final Step step,
                                                 final StateProxy oldLocation) throws NonDeterministicException
  {
    boolean moved = false;
    for (final TransitionProxy trans : automata.getTransitions())
    {
      if (trans.getEvent() == step.getEvent())
      {
        if (trans.getSource() == mAllAutomatons.get(automata) && !moved)
        {
          mAllAutomatons.put(automata, trans.getTarget());
          moved = true;
          mEnabledLastStep.add(automata);
        }
        else if (trans.getSource() == oldLocation && moved)
        {
          throw new NonDeterministicException("The automaton " + automata.getName() +
                                              " has two options. The event fired was " + step.toString());
        }
      }
    }
    if (!moved && automata.getKind() == ComponentKind.PROPERTY && automata.getEvents().contains(step.getEvent()))
    {
      if (SHOW_DISABLED_PROPERTY_ERROR_MESSAGE)
        mContainer.getIDE().error("Property "
                                + automata.getName()
                                + " has been disabled, as the event "
                                + step.getEvent().getName()
                                + " could not be fired");
      mDisabledProperties.add(automata);
    }
  }


  private void checkInvalid(final Step step)
  {
    if (isInInvalidEvent(step.getEvent()))
    {
      String errorMessage = "ERROR: The event " + step.toString() +
        " cannot be compiled as the following automata are blocking it:";
      for (final AutomatonProxy automata : mInvalidEvents.get(step.getEvent()))
        errorMessage += "\r\n" + automata.getName();
      throw new IllegalArgumentException(errorMessage);
    }
    else if (!isInValidEvent(step))
    {
      final String errorMessage = "ERROR: The event " + step.toString() +
        " cannot be completed as it is not inside any automata";
      throw new IllegalArgumentException(errorMessage);
    }
  }


  public void stepBack()
  {
    moveSafely(false);
  }
  public void replayStep()
  {
    if (mTrace != null)
    {
      if (mTrace instanceof LoopTraceProxy && !invalidated)
      {
        if (currentTime == previousStates.size() - 1)
        {
          mContainer.getIDE().info(": Looping to start of control loop");
          while (currentTime != ((LoopTraceProxy)mTrace).getLoopIndex() + 1)
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

  //#########################################################################
  //# Class Object
  public String toString()
  {
    String output = "";
    for (final AutomatonProxy auto : mAllAutomatons.keySet()){
      output += auto.getName() + " -> " + mAllAutomatons.get(auto) + "||";
    }
    return output;
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


  //#########################################################################
  //# Auxiliary Functions
  private void updateControllability(final boolean addNewUncontrollable)
  {
    final ArrayList<Pair<EventProxy, AutomatonProxy>> output = new ArrayList<Pair<EventProxy, AutomatonProxy>>();
    final ArrayList<AutomatonProxy> uncontrollableAutomatonThisTime = new ArrayList<AutomatonProxy>();
    for (final EventProxy event : mInvalidEvents.keySet())
    {
      if (event.getKind() == EventKind.UNCONTROLLABLE)
      {
        boolean blockingPlant = false;
        Pair<EventProxy, AutomatonProxy> blockingSpec = null;
        for (final AutomatonProxy automata : mInvalidEvents.get(event))
        {
          if (automata.getKind() == ComponentKind.SPEC || automata.getKind() == ComponentKind.SUPERVISOR)
          {
            blockingSpec =  new Pair<EventProxy, AutomatonProxy> (event, automata);
          }
          else if (automata.getKind() == ComponentKind.PLANT)
          {
            blockingPlant = true;
          }
        }
        if (!blockingPlant && blockingSpec != null)
        {
          output.add(blockingSpec);
          uncontrollableAutomatonThisTime.add(blockingSpec.getSecond());
        }
      }
    }
    if (output.size() == 0)
      mBlockingEvents = null;
    else
      mBlockingEvents = output;
    for (final AutomatonProxy auto : mAllAutomatons.keySet())
    {
      if (auto.getKind() == ComponentKind.PROPERTY && !mDisabledProperties.contains(auto))
      {
        for (final Step step : mEnabledEvents)
        {
          if (auto.getEvents().contains(step.getEvent()))
          {
            boolean found = false;
            for (final TransitionProxy trans : auto.getTransitions())
            {
              if (trans.getSource() == mAllAutomatons.get(auto) && trans.getEvent() == step.getEvent())
                found = true;
            }
            if (!found)
              mWarningProperties.put(step, auto);
          }
        }
      }
    }
  }

  private ArrayList<Pair<EventProxy, AutomatonProxy>> testForControlability()
  {
    return mBlockingEvents;
  }

  private boolean isInValidEvent (final Step step)
  {
    for (final Step validStep : mEnabledEvents)
    {
      if (step == validStep)
        return true;
    }
    return false;
  }

  private boolean isInInvalidEvent(final EventProxy event)
  {
    for (final EventProxy invalidEvent : mInvalidEvents.keySet())
    {
      if (invalidEvent == event)
        return true;
    }
    return false;
  }

  private void findEventClassification()
  {
    mEnabledEvents = new ArrayList<Step>();
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
    for (final AutomatonProxy aut : mAllAutomatons.keySet())
    {
      final ArrayList<TransitionProxy> eventsFirable = new ArrayList<TransitionProxy>();
      final ArrayList<TransitionProxy> newEventsFirable = new ArrayList<TransitionProxy>();
      final ArrayList<TransitionProxy> automatonsActiveEvents = new ArrayList<TransitionProxy>();
      for (final TransitionProxy trans : aut.getTransitions())
      {
        if (trans.getSource() == mAllAutomatons.get(aut))
        {
          boolean isInEnabled = false;
          boolean isInInvalid = false;
          automatonsActiveEvents.add(trans);
          for (final Step step: mEnabledEvents)
          {
            if (step.getEvent() == trans.getEvent())
              isInEnabled = true;
          }
          for (final EventProxy invalidEvent : mInvalidEvents.keySet())
          {
            if (invalidEvent == trans.getEvent())
              isInInvalid = true;
          }
          if (isInEnabled)
          {
            eventsFirable.add(trans);
          }
          else if (!isInEnabled && !isInInvalid)
          {
            newEventsFirable.add(trans);
          }
        }
      }
      processOldEvents(eventsFirable, aut);
      processNewEvents(newEventsFirable, aut);
      if (aut.getKind() != ComponentKind.PROPERTY)
        removeIgnoredEvents(automatonsActiveEvents, aut);
    }
    Collections.sort(mEnabledEvents);
    updateControllability(false);
    if (mBlockingEvents != null)
    {
      final ArrayList<Pair<EventProxy, AutomatonProxy>> invalidEvent = testForControlability();
      for (final Pair<EventProxy, AutomatonProxy> invalidPair : invalidEvent)
      {
        mModuleContainer.getIDE().error(": The event " + invalidPair.getFirst().getName()
            + " is not controllable, inside the automaton " + invalidPair.getSecond().getName()
            + ". Current state is: " + mAllAutomatons.get(invalidPair.getSecond()).getName());
      }
    }
    if (mEnabledEvents.size() == 0)
    {
      checkForBlockingError();
    }
  }

  private void checkForBlockingError()
  {
    for (final AutomatonProxy auto : mAllAutomatons.keySet())
    {
      if (mAllAutomatons.get(auto).getPropositions().size() == 0)
      {
        for (final StateProxy state : auto.getStates())
        {
          if (state.getPropositions().size() != 0)
          {
            mModuleContainer.getIDE().error(": The automaton " + auto.getName() + " is blocking");
          }
        }
      }
      else
      {
        boolean isAccepting = false;
        for (final EventProxy prop : mAllAutomatons.get(auto).getPropositions())
        {
          if (prop.getName().compareTo(":accepting") == 0)
            isAccepting = true;
        }
        if (!isAccepting)
        {
          mModuleContainer.getIDE().error(": The automaton " + auto.getName() + " is blocking");
        }
      }
    }
  }


  private void removeIgnoredEvents(final ArrayList<TransitionProxy> automatonsActiveEvents, final AutomatonProxy aut)
  {
    for (final EventProxy event : aut.getEvents())
    {
      boolean fired = false;
      for (final TransitionProxy trans : automatonsActiveEvents)
      {
        if (trans.getEvent() == event)
          fired = true;
      }
      if (!fired)
      {
        for (final Step toBeRemoved : new ArrayList<Step>(mEnabledEvents))
        {
          if (toBeRemoved.getEvent() == event)
          {
            mEnabledEvents.remove(toBeRemoved);
          }
        }
        addNewInvalidEvent(event, aut);
      }
    }
  }


  private void processNewEvents(final ArrayList<TransitionProxy> newEventsFirable,
                                final AutomatonProxy aut)
  {
    while (newEventsFirable.size() != 0)
    {
      final TransitionProxy firable = newEventsFirable.get(0);
      final ArrayList<TransitionProxy> targetTrans = new ArrayList<TransitionProxy>();
      for (final TransitionProxy trans : newEventsFirable)
      {
        if (firable.getEvent() == trans.getEvent())
          targetTrans.add(trans);
      }
      if (targetTrans.size() != 1)
      {
        for (final TransitionProxy trans : targetTrans)
        {
          final HashMap<AutomatonProxy, StateProxy> source = new HashMap<AutomatonProxy, StateProxy>();
          final HashMap<AutomatonProxy, StateProxy> dest = new HashMap<AutomatonProxy, StateProxy>();
          source.put(aut, trans.getSource());
          dest.put(aut, trans.getTarget());
          mEnabledEvents.add(new Step(trans.getEvent(),source, dest));
        }
      }
      else
      {
        mEnabledEvents.add(new Step(firable.getEvent()));
      }
      for (final TransitionProxy trans : targetTrans)
        newEventsFirable.remove(trans);
    }
  }


  private void processOldEvents(final ArrayList<TransitionProxy> eventsFirable, final AutomatonProxy aut)
  {
    while (eventsFirable.size() != 0)
    {
      final TransitionProxy firable = eventsFirable.get(0);
      final ArrayList<TransitionProxy> targetTrans = new ArrayList<TransitionProxy>();
      for (final TransitionProxy trans : eventsFirable)
      {
        if (firable.getEvent() == trans.getEvent())
          targetTrans.add(trans);
      }
      if (targetTrans.size() != 1)
      {
        for (final Step step : Collections.unmodifiableList(mEnabledEvents))
        {
          if (step.getEvent() == firable.getEvent())
          {
            mEnabledEvents.remove(step);
            for (final TransitionProxy trans : targetTrans)
            {
              mEnabledEvents.add(step.addNewTransition(aut, trans));
            }
          }
        }
      }
      for (final TransitionProxy trans : targetTrans)
        eventsFirable.remove(trans);
    }
  }


  private void addNewInvalidEvent(final EventProxy event, final AutomatonProxy automaton)
  {
    boolean isInValidEvent = false;
    for (final Step step : mEnabledEvents)
    {
      if (step.getEvent() == event)
        isInValidEvent = true;
    }
    if (isInInvalidEvent(event))
    {
      final ArrayList<AutomatonProxy> got = mInvalidEvents.get(event);
      got.add(automaton);
      mInvalidEvents.put(event, got);
    }
    else if (isInValidEvent)
    {
      for (final Step step : mEnabledEvents)
      {
        if (step.getEvent() == event)
          mEnabledEvents.remove(step);
      }
      final ArrayList<AutomatonProxy> failAutomaton = new ArrayList<AutomatonProxy>();
      failAutomaton.add(automaton);
      mInvalidEvents.put(event, failAutomaton);
    }
    else
    {
      final ArrayList<AutomatonProxy> failAutomaton = new ArrayList<AutomatonProxy>();
      failAutomaton.add(automaton);
      mInvalidEvents.put(event, failAutomaton);
    }
  }

  private boolean hasPropositions(final AutomatonProxy automaton)
  {
    for (final EventProxy event : automaton.getEvents())
    {
      if (event.getKind() == EventKind.PROPOSITION)
        return true;
    }
    return false;
  }

  /**
   * Stores a new compiled DES. Recomputes all data associated with it
   * and notifies registered views of the change.
   */
  private void setCompiledDES(final ProductDESProxy des)
  {
    if (des != mCompiledDES) {
      mCompiledDES = des;
      reset(true);
      final SimulationChangeEvent event = new SimulationChangeEvent
          (this, SimulationChangeEvent.MODEL_CHANGED);
      fireSimulationChangeEvent(event);
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

  private void fireSimulationChangeEvent(final SimulationChangeEvent event)
  {
    final ArrayList<SimulationObserver> temp =
      new ArrayList<SimulationObserver>(mSimulationObservers);
    for (final SimulationObserver observer : temp) {
      observer.simulationChanged(event);
    }
  }

  private void moveSafely(final boolean forward)
  {
    mWarningProperties = new HashMap<Step, AutomatonProxy>();
    moveTime(forward);
    loadSimulatorState();
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  private void moveTime(final boolean forward)
  {
    if (forward)
    {
      if (currentTime == previousStates.size() - 1)
      {
        mContainer.getIDE().error("No future Events");
        return;
      }
      else
        currentTime++;
    }
    else
    {
      if (currentTime == 0)
      {
        mContainer.getIDE().error("No previous Event");
        return;
      }
      else
        currentTime--;
    }
  }


  private void removeFutureEvents()
  {
    while (previousStates.size() != currentTime + 1)
    {
      previousStates.remove(currentTime + 1);
    }
  }

  //#########################################################################
  //# Data Members
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;

  private Map<AutomatonProxy,StateProxy> mAllAutomatons; // The Map object is the current state of the key
  private ArrayList<Step> mEnabledEvents;
  private HashMap<EventProxy, ArrayList<AutomatonProxy>> mInvalidEvents; //The Map object is the list of all the Automatons which are blocking the event
  private Step mCurrentEvent;
  private int currentTime; // The index representing the current index for the current version history.
  private ArrayList<Pair<EventProxy, AutomatonProxy>> mBlockingEvents;
  private final ModuleContainer mModuleContainer;
  private ArrayList<AutomatonProxy> mEnabledLastStep;
  private final ArrayList<SimulationObserver> mSimulationObservers;
  private ProductDESProxy mCompiledDES;
  private final ModuleContainer mContainer;
  private TraceProxy mTrace;
  private boolean mAllowLastStep;
  private boolean invalidated;
  private HashMap<Step, AutomatonProxy> mWarningProperties;
  private ArrayList<SimulatorState> previousStates;
  private ArrayList<AutomatonProxy> mDisabledProperties;
  long time = 0;

  // #######################################################################
  // # Class Constants
  private static final boolean SHOW_DISABLED_PROPERTY_ERROR_MESSAGE = false;
}