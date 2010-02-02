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
    mPreviousEvents = new ArrayList<Step>();
    mEnabledLastStep = new ArrayList<AutomatonProxy>();
    mPreviousAutomatonStates = new ArrayList<Map<AutomatonProxy, StateProxy>>();
    mPreviousEnabledLastStep = new ArrayList<ArrayList<AutomatonProxy>>();
    mBlockingEvents = new ArrayList<Pair<EventProxy, AutomatonProxy>>();
    mUncontrollableAutomata = new ArrayList<ArrayList<AutomatonProxy>>();
    currentTime = -1;
    mContainer = container;
    invalidated = false;
    final ModuleSubject module = container.getModule();
    module.addModelObserver(this);
    container.attach(this);
    final ProductDESProxy des = container.getCompiledDES();
    setCompiledDES(des);
    updateControllability(true);
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
      throw new UnsupportedOperationException("ERROR: Unknown event status");
    }
  }

  public ImageIcon getAutomatonActivityIcon(final AutomatonProxy aut)
  {
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

  public List<AutomatonProxy> isNonControllableAtTime(final int time)
  {
    if (time < -1 || time > mUncontrollableAutomata.size() - 2)
      return Collections.emptyList();
    else
      return Collections.unmodifiableList(mUncontrollableAutomata.get(time + 1));
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
      return mPreviousEnabledLastStep.get(currentTime - 1).contains(automaton);
  }
  public boolean changedNextStep(final AutomatonProxy aut)
  {
    if (currentTime == mPreviousEnabledLastStep.size() - 1)
      return false;
    else
      return mPreviousEnabledLastStep.get(currentTime + 1).contains(aut);
  }

  public ModuleContainer getContainer()
  {
    return mContainer;
  }

  public List<Step> getEventHistory()
  {
    return Collections.unmodifiableList(mPreviousEvents);
  }

  public List<Map<AutomatonProxy, StateProxy>> getAutomatonHistory()
  {
   return Collections.unmodifiableList(mPreviousAutomatonStates);
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
    return mPreviousEnabledLastStep.get(time);
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
          if (mPreviousEvents.get(currentTime) == null)
            return null;
          return mPreviousEvents.get(currentTime).getTransition(automaton,
                                                                startState,
                                                                mPreviousAutomatonStates.get(currentTime).get(automaton));
        }
      }
    }
    else
    {
      if (mPreviousEvents.get(currentTime) == null)
        return null;
      return mPreviousEvents.get(currentTime).getTransition(automaton,
                     mPreviousAutomatonStates.get(currentTime - 1).get(automaton),
                     mPreviousAutomatonStates.get(currentTime).get(automaton));
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
    if (!automaton.getStates().contains(state))
    {
      throw new IllegalArgumentException("ERROR: " + state.getName() + " does not belong to this automaton");
    }
    if (mAllAutomatons.containsKey(automaton))
      mAllAutomatons.put(automaton, state);
    else
      throw new IllegalArgumentException("ERROR: This automaton is not in this program");
    invalidated = true;
    findEventClassification();
    mPreviousAutomatonStates.add(new HashMap<AutomatonProxy, StateProxy>(mAllAutomatons));
    final ArrayList<AutomatonProxy> auto = new ArrayList<AutomatonProxy>();
    auto.add(automaton);
    mPreviousEnabledLastStep.add(auto);
    mPreviousEvents.add(null);
    currentTime++;
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  public void reset()
  {
    if (mTrace != null)
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
          for (final StateProxy state : automaton.getStates())
            if (state.isInitial())
              mAllAutomatons.put(automaton, state);
        findEventClassification();
      }
      mPreviousEvents = new ArrayList<Step>();
      mEnabledLastStep = new ArrayList<AutomatonProxy>();
      mPreviousAutomatonStates = new ArrayList<Map<AutomatonProxy, StateProxy>>();
      mPreviousEnabledLastStep = new ArrayList<ArrayList<AutomatonProxy>>();
      mUncontrollableAutomata = new ArrayList<ArrayList<AutomatonProxy>>();
      currentTime = -1;
      updateControllability(true);
      final SimulationChangeEvent simEvent = new SimulationChangeEvent
        (this, SimulationChangeEvent.STATE_CHANGED);
      fireSimulationChangeEvent(simEvent);
    }
  }

  public void run(final TraceProxy trace, final boolean allowLastStep)
  {
    mTrace = null;
    mAllowLastStep = false;
    reset();
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
          boolean isTheRightStep = true;
          if (step.getEvent() == tStep.getEvent()) // Check if the event name is right. If not, this is not the correct Step
          {
            for (final AutomatonProxy auto : this.mAllAutomatons.keySet())
            {
              if (step.getSource().get(auto) != null) // Check to see if the step is non-deterministic. If it is, then it is the correct Step
              {
                if (tStep.getStateMap().get(auto) == null) // If there is no non-deterministic information, fail always
                  throw new IllegalArgumentException("No non-deterministic information available");
                else if (step.getDest().get(auto) == tStep.getStateMap().get(auto)) // If the destinations match, then it is the right step
                  {
                    isTheRightStep = true;
                  }
                  else
                    isTheRightStep = false;
              }
              else
                isTheRightStep = true;
            }
          }
          else
          {
            isTheRightStep = false;
          }
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

  public void step(final Step step) throws NonDeterministicException
  {
    time = System.currentTimeMillis();
    if (step == null)
    {
      return;
    }
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
    else
    {
      invalidated = true;
      removeFutureEvents();
      mPreviousEvents.add(step);
      mEnabledLastStep = new ArrayList<AutomatonProxy>();
      final HashMap<AutomatonProxy, TransitionProxy> thisStateTransitionFire = new HashMap<AutomatonProxy, TransitionProxy>();
      for (final AutomatonProxy automata : mAllAutomatons.keySet())
      {
        boolean moved = false;
        final StateProxy oldLocation = mAllAutomatons.get(automata);
        if (step.getDest().get(automata) != null)
        {
          mAllAutomatons.put(automata, step.getDest().get(automata));
        }
        else
        {
          for (final TransitionProxy trans : automata.getTransitions())
          {
            if (trans.getEvent() == step.getEvent())
            {
              if (trans.getSource() == mAllAutomatons.get(automata) && !moved)
              {
                mAllAutomatons.put(automata, trans.getTarget());
                thisStateTransitionFire.put(automata, trans);
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
        }
      }
      mPreviousAutomatonStates.add(new HashMap<AutomatonProxy, StateProxy>(mAllAutomatons));
      mPreviousEnabledLastStep.add(mEnabledLastStep);
      currentTime++;
    }
    findEventClassification();
    updateControllability(true);
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
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
        if (currentTime == mPreviousEvents.size() - 1)
        {
          mContainer.getIDE().info(": Looping to start of control loop");
          while (currentTime != ((LoopTraceProxy)mTrace).getLoopIndex())
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
          if (automata.getKind() == ComponentKind.SPEC)
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
    if (addNewUncontrollable)
      mUncontrollableAutomata.add(uncontrollableAutomatonThisTime);
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

  @SuppressWarnings("unchecked")
  private void findEventClassification()
  {
    mEnabledEvents = new ArrayList<Step>();
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
    for (final AutomatonProxy aut : mAllAutomatons.keySet())
    {
      final ArrayList<TransitionProxy> eventsFirable = new ArrayList<TransitionProxy>();
      for (final TransitionProxy trans : aut.getTransitions())
      {
        if (trans.getSource() == mAllAutomatons.get(aut))
        {
          boolean isInEnabled = false;
          boolean isInInvalid = false;
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
            TransitionProxy old = null;
            for (final TransitionProxy eventFired : eventsFirable)
            {
              if (eventFired.getEvent() == trans.getEvent())
                old = eventFired;
            }
            if (old != null)
            {
              splitEvent(aut, old, trans);
            }
          }
          else if (!isInEnabled && !isInInvalid)
          {
            mEnabledEvents.add(new Step(trans.getEvent()));
          }
          eventsFirable.add(trans);
        }
      }
      for (final EventProxy event : aut.getEvents())
      {
        boolean fired = false;
        for (final TransitionProxy trans : eventsFirable)
        {
          if (trans.getEvent() == event)
            fired = true;
        }
        if (!fired)
        {
          for (final Step toBeRemoved : (ArrayList<Step>)mEnabledEvents.clone())
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
  }

  @SuppressWarnings("unchecked")
  private void splitEvent(final AutomatonProxy auto, final TransitionProxy old, final TransitionProxy trans)
  {
    if (old.getEvent() != trans.getEvent())
      throw new IllegalArgumentException();
    for (final Step step : (ArrayList<Step>)mEnabledEvents.clone())
    {
      if (step.getEvent() == old.getEvent())
      {
        if (!step.isSensitive(auto))
        {
          mEnabledEvents.remove(step);
          mEnabledEvents.add(step.addNewTransition(auto, old));
          mEnabledEvents.add(step.addNewTransition(auto, trans));
          return;
        }
        else
        {
          mEnabledEvents.add(step.addNewTransition(auto, trans));
          return;
        }
      }
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
      reset();
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

  @SuppressWarnings("unchecked")
  private void moveSafely(final boolean forward)
  {
    if (forward)
    {
      if (currentTime == mPreviousEvents.size() - 1)
      {
        System.out.println("No future Events");
        return;
      }
      else
        currentTime++;
    }
    else
    {
      if (currentTime == -1)
      {
        System.out.println("No previous Event");
        return;
      }
      else
        currentTime--;
    }
    if (currentTime == -1)
    {
      mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>();
      mEnabledEvents = new ArrayList<Step>();
      mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
      if (mCompiledDES != null)
      {
        for (final AutomatonProxy automaton : mCompiledDES.getAutomata())
          for (final StateProxy state : automaton.getStates())
            if (state.isInitial())
              mAllAutomatons.put(automaton, state);
        findEventClassification();
      }
      mEnabledLastStep = new ArrayList<AutomatonProxy>();
    }
    else
    {
      mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>(mPreviousAutomatonStates.get(currentTime));
      mEnabledLastStep = (ArrayList<AutomatonProxy>)mPreviousEnabledLastStep.get(currentTime).clone();
      findEventClassification();
    }
    updateControllability(false);
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
  }

  private void removeFutureEvents()
  {
    while (mPreviousEvents.size() != currentTime + 1)
    {
      mPreviousEvents.remove(currentTime + 1);
      mPreviousAutomatonStates.remove(currentTime + 1);
      mPreviousEnabledLastStep.remove(currentTime + 1);
      mUncontrollableAutomata.remove(currentTime + 1);
    }
  }

  //#########################################################################
  //# Data Members
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;

  private Map<AutomatonProxy,StateProxy> mAllAutomatons; // The Map object is the current state of the key
  private ArrayList<Step> mEnabledEvents;
  private HashMap<EventProxy, ArrayList<AutomatonProxy>> mInvalidEvents; //The Map object is the list of all the Automatons which are blocking the event
  private ArrayList<Step> mPreviousEvents;
  private int currentTime; // The index representing the current index for the current version history.
  private ArrayList<Map<AutomatonProxy, StateProxy>> mPreviousAutomatonStates;
  private ArrayList<ArrayList<AutomatonProxy>> mPreviousEnabledLastStep;
  private ArrayList<Pair<EventProxy, AutomatonProxy>> mBlockingEvents;
  private ArrayList<ArrayList<AutomatonProxy>> mUncontrollableAutomata;
  private final ModuleContainer mModuleContainer;
  private ArrayList<AutomatonProxy> mEnabledLastStep;
  private final ArrayList<SimulationObserver> mSimulationObservers;
  private ProductDESProxy mCompiledDES;
  private final ModuleContainer mContainer;
  private TraceProxy mTrace;
  private boolean mAllowLastStep;
  private boolean invalidated;

  long time = 0;

}