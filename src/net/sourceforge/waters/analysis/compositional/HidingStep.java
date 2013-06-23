//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   HidingStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * @author Robi Malik
 */

class HidingStep extends AbstractionStep
{

  //#######################################################################
  //# Constructors
  HidingStep(final AbstractCompositionalModelAnalyzer analyzer,
             final Collection<AutomatonProxy> originals,
             final AutomatonProxy composedAut,
             final Collection<EventProxy> localEvents,
             final EventProxy tau)
  {
    super(analyzer, composedAut, originals);
    mLocalEvents = localEvents;
    mHiddenEvent = tau;
    mStateMap = null;
  }

  HidingStep(final AbstractCompositionalModelAnalyzer analyzer,
             final AutomatonProxy composedAut,
             final Collection<EventProxy> localEvents,
             final EventProxy tau,
             final SynchronousProductStateMap stateMap)
  {
    super(analyzer, composedAut, stateMap.getInputAutomata());
    mLocalEvents = localEvents;
    mHiddenEvent = tau;
    mStateMap = stateMap;
  }

  HidingStep(final AbstractCompositionalModelAnalyzer analyzer,
             final AutomatonProxy composedAut,
             final AutomatonProxy originalAut,
             final Collection<EventProxy> localEvents,
             final EventProxy tau)
  {
    super(analyzer, composedAut, originalAut);
    mLocalEvents = localEvents;
    mHiddenEvent = tau;
    mStateMap = null;
  }


  //#######################################################################
  //# Simple Access
  EventProxy getTauEvent()
  {
    return mHiddenEvent;
  }

  Collection<EventProxy> getLocalEvents()
  {
    return mLocalEvents;
  }


  //#######################################################################
  //# Trace Computation
  @Override
  List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
    throws AnalysisAbortException, OverflowException
  {
    Map<AutomatonProxy,StateProxy> previousMapOrig = null;
    final ListIterator<TraceStepProxy> iter = steps.listIterator();
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final Map<AutomatonProxy,StateProxy> nextMapResult =
        step.getStateMap();
      final EventProxy event = step.getEvent();
      final TraceStepProxy convertedStep =
        findNextStep(previousMapOrig, nextMapResult, event);
      iter.set(convertedStep);
      previousMapOrig = convertedStep.getStateMap();
    }
    return steps;
  }

  TraceStepProxy findNextStep
    (final Map<AutomatonProxy,StateProxy> previousMapOrig,
     final Map<AutomatonProxy,StateProxy> nextMapResult,
     final EventProxy resultEvent)
    throws AnalysisAbortException, OverflowException
  {
    final Map<AutomatonProxy,StateProxy> nextMapOrig =
      new HashMap<AutomatonProxy,StateProxy>(nextMapResult);
    final AutomatonProxy resultAutomaton = getResultAutomaton();
    final StateProxy nextStateResult =
      nextMapOrig.remove(resultAutomaton);
    final Collection<AutomatonProxy> originalAutomata =
      getOriginalAutomata();
    for (final AutomatonProxy aut : originalAutomata) {
      checkAbort();
      final StateProxy nextStateOrig =
        getOriginalState(nextStateResult, aut);
      assert nextStateOrig != null;
      nextMapOrig.put(aut, nextStateOrig);
    }
    final EventProxy origEvent;
    if (resultEvent != null && resultEvent == mHiddenEvent) {
      origEvent = findEvent(previousMapOrig, nextMapOrig);
    } else {
      origEvent = resultEvent;
    }
    final ProductDESProxyFactory factory = getFactory();
    return factory.createTraceStepProxy(origEvent, nextMapOrig);
  }

  EventProxy findEvent(final Map<AutomatonProxy,StateProxy> sources,
                       final Map<AutomatonProxy,StateProxy> targets)
    throws AnalysisAbortException, OverflowException
  {
    final Collection<EventProxy> possible =
      new LinkedList<EventProxy>(mLocalEvents);
    for (final AutomatonProxy aut : getOriginalAutomata()) {
      if (possible.size() <= 1) {
        break;
      }
      final StateProxy source = sources.get(aut);
      final StateProxy target = targets.get(aut);
      final Collection<EventProxy> alphabet =
        new THashSet<EventProxy>(aut.getEvents());
      final int size = alphabet.size();
      final Collection<EventProxy> retained =
        new THashSet<EventProxy>(size);
      for (final TransitionProxy trans : aut.getTransitions()) {
        checkAbort();
        if (trans.getSource() == source && trans.getTarget() == target) {
          final EventProxy event = trans.getEvent();
          retained.add(event);
        }
      }
      final Iterator<EventProxy> iter = possible.iterator();
      while (iter.hasNext()) {
        checkAbort();
        final EventProxy event = iter.next();
        if (alphabet.contains(event)) {
          if (!retained.contains(event)) {
            iter.remove();
          }
        } else {
          if (source != target) {
            iter.remove();
          }
        }
      }
    }
    return possible.iterator().next();
  }

  StateProxy getOriginalState(final StateProxy convertedState,
                              final AutomatonProxy aut)
  {
    if (mStateMap == null) {
      return convertedState;
    } else {
      return mStateMap.getOriginalState(convertedState, aut);
    }
  }

  boolean isMatchingEvent(final EventProxy origEvent,
                          final EventProxy resultEvent)
  {
    if (resultEvent == mHiddenEvent) {
      return mLocalEvents.contains(origEvent);
    } else {
      return origEvent == resultEvent;
    }
  }


  //#######################################################################
  //# Data Members
  private final Collection<EventProxy> mLocalEvents;
  private final EventProxy mHiddenEvent;
  private final SynchronousProductStateMap mStateMap;
}
