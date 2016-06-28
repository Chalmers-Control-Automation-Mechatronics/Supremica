//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.simulator;

import gnu.trove.set.hash.THashSet;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.flexfact.LocalServer;
import net.sourceforge.waters.gui.flexfact.LocalSocket;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.util.PropositionIcon;
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
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.properties.Config;


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
    setCompiledDES(null);
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
  public void setState(final int time)
  {
    if (mCurrentTime != time) {
      mCurrentTime = time;
      loadSimulatorState();
      final SimulationChangeEvent simEvent =
        new SimulationChangeEvent(this, SimulationChangeEvent.STATE_CHANGED);
      fireSimulationChangeEvent(simEvent);
    }
  }

  public void setState(final AutomatonProxy aut, final StateProxy state)
  {
    if (!aut.getStates().contains(state)) {
      throw new IllegalArgumentException
        ("Automaton " + aut.getName() +
         " does not contain state " + state.getName() + "!");
    } else if (getCurrentState(aut) != state) {
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
      warnAboutMissingInitialStates(state);
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
    final List<SimulatorStep> steps = new ArrayList<SimulatorStep>();
    for (final SimulatorStep step : getEnabledSteps()) {
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
        ModuleContext.getEventKindToolTip(ekind, true) + ' ' + event.getName() +
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
  public void step(final List<SimulatorStep> possibleSteps)
  {
    final int size = possibleSteps.size();
    assert size > 0 : "Can't step with empty list of choices!";
    final SimulatorStep step;
    if (size == 1) {
      step = possibleSteps.get(0);
    } else {
      final JLabel[] labels = new JLabel[size];
      final SimulatorStep[] steps = new SimulatorStep[size];
      for (int looper = 0; looper < size; looper++) {
        final SimulatorStep possible = possibleSteps.get(looper);
        final JLabel label = new JLabel(possible.toString());
        final EventProxy event = possible.getEvent();
        final EventKind kind = event.getKind();
        final boolean observable = event.isObservable();
        final Icon icon = ModuleContext.getEventKindIcon(kind, observable);
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
  public void step(final SimulatorStep step)
  {

    final SimulatorState nextState = step.getNextSimulatorState();
    if(Config.INCLUDE_FLEXFACT.isTrue()) {
      String event = nextState.getEvent().getName();
      if(event.length() >= 4){
        int ind = event.lastIndexOf("_east");
        if(ind >= 0) {
          event = new StringBuilder(event).replace(ind, ind+5, "+").toString();
        }
        ind = event.lastIndexOf("_west");
        if(ind >= 0) {
          event = new StringBuilder(event).replace(ind, ind+5, "-").toString();
        }
        if(LocalServer.events.contains(event))
        {
          LocalSocket.SendEvent(event);
        }
      }
    }
    removeFutureSteps();
    addNewSimulatorState(nextState);
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

  public boolean isAutomatonEnabled(final AutomatonProxy aut)
  {
    return getAutomatonStatus(aut) != AutomatonStatus.DISABLED;
  }

  public void setAutomatonEnabled(final AutomatonProxy aut, final boolean enabled)
  {
    final StateProxy state = mCurrentState.getState(aut);
    final AutomatonStatus status;
    if (enabled) {
      final SimulatorState pred =
        mCurrentTime == 0 ? null : getHistoryState(mCurrentTime - 1);
      final EventProxy event = mCurrentState.getEvent();
      status = SimulatorState.findAutomatonStatus(pred, aut, event, state);
    } else {
      status = AutomatonStatus.DISABLED;
    }
    mCurrentState.setState(aut, state, status);
    removeFutureSteps();
    loadSimulatorState();
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

  public List<AutomatonProxy> getOrderedAutomata()
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
   * @return The icon of a state, taking into account propositions
   */
  Icon getMarkingIcon(final StateProxy state,
                      final AutomatonProxy automaton)
  {
    final PropositionIcon.ColorInfo info =
      getMarkingColorInfo(state, automaton);
    return info.getIcon();
  }

  /**
   * @param state
   *          The state to be drawn
   * @param automaton
   *          The automaton the state belongs to
   * @return The colour of a state, taking into account propositions.
   */
  PropositionIcon.ColorInfo getMarkingColorInfo
    (final StateProxy state, final AutomatonProxy automaton)
  {
    final Collection<EventProxy> props = state.getPropositions();
    if (props.isEmpty()) {
      if (hasNonForbiddenPropositions(automaton)) {
        return PropositionIcon.getUnmarkedColors();
      } else {
        return PropositionIcon.getNeutralColors();
      }
    } else {
      final Map<Object,SourceInfo> infomap =
        mModuleContainer.getSourceInfoMap();
      if (infomap == null) {
        return PropositionIcon.getNeutralColors();
      }
      final int size = props.size();
      final Set<Color> colorset;
      final List<Color> colorlist;
      if (hasNonForbiddenPropositions(automaton)) {
        colorset = new THashSet<Color>(size);
        colorlist = new ArrayList<Color>(size);
      } else {
        colorset = null;
        colorlist = null;
      }
      boolean forbidden = false;
      for (final EventProxy prop : props) {
        final SourceInfo info = infomap.get(prop);
        final EventDeclProxy decl = (EventDeclProxy) info.getSourceObject();
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo != null) {
          if (colorset != null) {
            for (final Color color : geo.getColorSet()) {
              if (colorset.add(color)) {
                colorlist.add(color);
              }
            }
          }
        } else if (decl.getName().equals
                     (EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          forbidden = true;
        } else if (colorset != null) {
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
  public StateProxy getCurrentState(final AutomatonProxy aut)
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

  public List<SimulatorStep> getEnabledSteps()
  {
    updateEventStatus();
    return mEnabledSteps;
  }


  //#########################################################################
  //# Interface net.sourceforge.subject.base.Observer
  /**
   * Invalidates the current compiled DES; it will be recompiled later when
   * the simulator tab is activated.
   */
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    final int kind = event.getKind();
    switch (kind) {
    case ModelChangeEvent.NAME_CHANGED:
    case ModelChangeEvent.STATE_CHANGED:
      if (!(event.getSource() instanceof ModuleProxy)) {
        setCompiledDES(null);
      }
      break;
    case ModelChangeEvent.GEOMETRY_CHANGED:
      break;
    default:
      setCompiledDES(null);;
      break;
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.RENDERING_PRIORITY;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Observer
  // This event is received when a new tab has been activated in a
  // module container, after recompiling the module. If the activated
  // tab was the simulator, it is now safe to get an updated compiled
  // DES from the module container. After storing the new simulation
  // state in the simulator, all registered views are notified to
  // update themselves.
  @Override
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
      final SimulationChangeEvent simEvent = new SimulationChangeEvent
        (this, SimulationChangeEvent.MODEL_CHANGED);
      fireSimulationChangeEvent(simEvent);
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

  private void warnAboutMissingInitialStates(final SimulatorState state)
  {
    if (state.getNumberOfDisabledAutomata() > 0) {
      final StringBuilder buffer =
        new StringBuilder("Could not determine initial state for ");
      boolean first = true;
      for (final AutomatonProxy aut : mOrderedAutomata) {
        if (state.getStatus(aut) == AutomatonStatus.DISABLED) {
          if (first) {
            first = false;
          } else {
            buffer.append(", ");
          }
          buffer.append(aut.getName());
        }
      }
      buffer.append('.');
      final IDE ide = mModuleContainer.getIDE();
      ide.warn(buffer.toString());
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
        if (isAutomatonEnabled(aut)) {
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
      final Map<EventProxy,List<StateProxy>> enabled =
        new HashMap<EventProxy,List<StateProxy>>(numGlobal);
      mEventStatusMap = new HashMap<EventProxy,EventEntry>(numGlobal);
      for (final EventProxy event : mOrderedEvents) {
        final Collection<AutomatonProxy> automata =
          getAutomataSensitiveToEvent(event);
        final int numAut = automata.size();
        final EventEntry entry = new EventEntry(numAut);
        mEventStatusMap.put(event, entry);
      }
      final ComponentKind[] order = new ComponentKind[]
        {ComponentKind.PLANT, ComponentKind.SPEC,
         ComponentKind.SUPERVISOR, ComponentKind.PROPERTY};
      for (final ComponentKind kind : order) {
        for (final AutomatonProxy aut : mOrderedAutomata) {
          if (aut.getKind() == kind) {
            final AutomatonStatus oldStatus = mCurrentState.getStatus(aut);
            if (oldStatus != AutomatonStatus.DISABLED) {
              AutomatonStatus newStatus = AutomatonStatus.IGNORED;
              final StateProxy source = mCurrentState.getState(aut);
              final Collection<EventProxy> local = aut.getEvents();
              for (final TransitionProxy trans : aut.getTransitions()) {
                if (trans.getSource() == source) {
                  final EventProxy event = trans.getEvent();
                  List<StateProxy> successors = enabled.get(event);
                  if (successors == null) {
                    successors = new LinkedList<StateProxy>();
                    enabled.put(event, successors);
                  }
                  final StateProxy target = trans.getTarget();
                  successors.add(target);
                }
              }
              for (final EventProxy event : local) {
                if (event.getKind() != EventKind.PROPOSITION) {
                  final EventEntry entry = mEventStatusMap.get(event);
                  final List<StateProxy> successors = enabled.get(event);
                  if (successors != null) {
                    entry.setSuccessors(aut, successors);
                  } else {
                    switch (kind) {
                    case PLANT:
                      entry.setStatus(aut, EventStatus.DISABLED);
                      break;
                    case SPEC:
                    case SUPERVISOR:
                      if (event.getKind() == EventKind.CONTROLLABLE ||
                          entry.getStatus() == EventStatus.DISABLED) {
                        entry.setStatus(aut, EventStatus.DISABLED);
                      } else {
                        entry.setStatus(aut, EventStatus.ERROR);
                        newStatus = AutomatonStatus.ERROR;
                      }
                      break;
                    case PROPERTY:
                      if (entry.getStatus() == EventStatus.DISABLED) {
                        entry.setStatus(aut, EventStatus.DISABLED);
                      } else {
                        entry.setStatus(aut, EventStatus.WARNING);
                        if (newStatus == AutomatonStatus.IGNORED) {
                          newStatus = AutomatonStatus.WARNING;
                        }
                      }
                      break;
                    default:
                      throw new IllegalStateException
                        ("Unknown component kind " + aut.getKind() + "!");
                    }
                  }
                }
              }
              enabled.clear();
              if (newStatus != AutomatonStatus.IGNORED) {
                mCurrentState.setState(aut, source, newStatus);
              } else if (oldStatus == AutomatonStatus.WARNING ||
                         oldStatus == AutomatonStatus.ERROR) {
                if (mCurrentTime > 0) {
                  final SimulatorState pred = getHistoryState(mCurrentTime - 1);
                  final EventProxy event = mCurrentState.getEvent();
                  newStatus =
                    SimulatorState.findAutomatonStatus(pred, aut, event, source);
                }
                mCurrentState.setState(aut, source, newStatus);
              }
            }
          }
        }
      }
      final int numAutomata = mOrderedAutomata.size();
      final Map<AutomatonProxy,StateProxy> source =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      for (final AutomatonProxy aut : mOrderedAutomata) {
        final StateProxy state = mCurrentState.getState(aut);
        if (state != null) {
          source.put(aut, state);
        }
      }
      final List<AutomatonProxy> nondet =
        new ArrayList<AutomatonProxy>(numAutomata);
      mEnabledSteps = new ArrayList<SimulatorStep>();
      for (final EventProxy event : mOrderedEvents) {
        final EventStatus status = getEventStatus(event);
        if (status.canBeFired()) {
          final List<AutomatonProxy> automata =
            getAutomataSensitiveToEvent(event);
          for (final AutomatonProxy aut : automata) {
            final EventEntry entry = mEventStatusMap.get(event);
            final List<StateProxy> successors = entry.getSuccessors(aut);
            if (successors != null && successors.size() > 1) {
              nondet.add(aut);
            }
          }
          final List<AutomatonProxy> ndcopy =
            nondet.isEmpty() ? null : new ArrayList<AutomatonProxy>(nondet);
          nondet.clear();
          final Map<AutomatonProxy,StateProxy> target =
            new HashMap<AutomatonProxy,StateProxy>(source);
          createSteps(event, target, automata, ndcopy, 0);
        }
      }
      for (final EventEntry entry : mEventStatusMap.values()) {
        entry.clearSuccessors();
      }
    }
  }

  private void createSteps(final EventProxy event,
                           final Map<AutomatonProxy,StateProxy> targetTuple,
                           final List<AutomatonProxy> automata,
                           final List<AutomatonProxy> nondet,
                           final int index)
  {
    if (index == automata.size()) {
      final Map<AutomatonProxy,StateProxy> targetCopy =
        new HashMap<AutomatonProxy,StateProxy>(targetTuple);
      final SimulatorState nextState =
        SimulatorState.createSuccessorState(mCurrentState, event, targetCopy);
      final SimulatorStep step = new SimulatorStep(nextState, nondet);
      mEnabledSteps.add(step);
    } else {
      final int next = index + 1;
      final AutomatonProxy aut = automata.get(index);
      final EventEntry entry = mEventStatusMap.get(event);
      final List<StateProxy> successors = entry.getSuccessors(aut);
      if (successors == null) {
        targetTuple.remove(aut);
        createSteps(event, targetTuple, automata, nondet, next);
      } else {
        for (final StateProxy target : successors) {
          targetTuple.put(aut, target);
          createSteps(event, targetTuple, automata, nondet, next);
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
  private static boolean hasNonForbiddenPropositions
    (final AutomatonProxy automaton)
  {
    for (final EventProxy event : automaton.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {
        final String name = event.getName();
        if (!name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          return true;
        }
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
      mSuccessorMap = new HashMap<AutomatonProxy,List<StateProxy>>(numAutomata);
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
      final StringBuilder buffer;
      switch (mStatus) {
      case DISABLED:
        return "currently disabled";
      case ENABLED:
        return "currently enabled";
      case WARNING:
        buffer = new StringBuilder("causes violation of property ");
        break;
      case ERROR:
        buffer =
          new StringBuilder("violates controllability of specification ");
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
        if (!mStatus.canBeFired()) {
          clearSuccessors();
        }
      }
    }

    private List<StateProxy> getSuccessors(final AutomatonProxy aut)
    {
      return mSuccessorMap.get(aut);
    }

    private void setSuccessors(final AutomatonProxy aut,
                               final List<StateProxy> successors)
    {
      if (mStatus.canBeFired()) {
        mSuccessorMap.put(aut, successors);
      }
    }

    private void clearSuccessors()
    {
      mSuccessorMap.clear();
    }

    //#########################################################################
    //# Data Members
    private EventStatus mStatus;
    private final Map<AutomatonProxy,EventStatus> mAutomataMap;
    private final Map<AutomatonProxy,List<StateProxy>> mSuccessorMap;
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
  private List<SimulatorStep> mEnabledSteps;

  private TraceProxy mTrace;
  private boolean mAllowLastStep;
  private boolean mTraceInvalidated;

}
