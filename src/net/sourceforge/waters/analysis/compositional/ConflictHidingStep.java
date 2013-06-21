//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ConflictHidingStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * A special abstraction step for compositional conflict checkers.
 * Includes support for counterexample expansion after synchronous
 * product computations, during which dump states of the input automata
 * are merged into a single dump state of the synchronous product that
 * is not further expanded.
 *
 * @author Robi Malik
 */

class ConflictHidingStep extends HidingStep
{

  //#########################################################################
  //# Constructor
  ConflictHidingStep(final AbstractCompositionalModelAnalyzer analyzer,
                     final AutomatonProxy composedAut,
                     final Collection<EventProxy> localEvents,
                     final EventProxy tau,
                     final SynchronousProductStateMap stateMap)
  {
    super(analyzer, composedAut, localEvents, tau, stateMap);
  }


  //#########################################################################
  //# Trace Computation
  @Override
  TraceStepProxy findNextStep
    (final Map<AutomatonProxy,StateProxy> previousMapOrig,
     final Map<AutomatonProxy,StateProxy> nextMapResult,
     final EventProxy resultEvent)
    throws AnalysisAbortException
  {
    final AutomatonProxy resultAutomaton = getResultAutomaton();
    final StateProxy nextStateResult = nextMapResult.get(resultAutomaton);
    for (final AutomatonProxy aut : getOriginalAutomata()) {
      if (getOriginalState(nextStateResult, aut) == null) {
        return findDumpStep(previousMapOrig, nextMapResult, resultEvent);
      }
    }
    return super.findNextStep(previousMapOrig, nextMapResult, resultEvent);
  }

  private TraceStepProxy findDumpStep
    (final Map<AutomatonProxy,StateProxy> previousMapOrig,
     final Map<AutomatonProxy,StateProxy> nextMapResult,
     final EventProxy resultEvent)
    throws AnalysisAbortException
  {
    final List<AutomatonProxy> originalAutomata =
      getOriginalAutomata();
    final int numAutomata = originalAutomata.size();

    final Map<EventProxy,DumpStateSearchData> searchMap;
    if (resultEvent == getTauEvent()) {
      final Collection<EventProxy> local = getLocalEvents();
      final int numLocal = local.size();
      searchMap =
        new HashMap<EventProxy,DumpStateSearchData>(numLocal);
      for (final EventProxy event : local) {
        final DumpStateSearchData data =
          new DumpStateSearchData(numAutomata);
        searchMap.put(event, data);
      }
    } else {
      final DumpStateSearchData data =
        new DumpStateSearchData(numAutomata);
      searchMap = Collections.singletonMap(resultEvent, data);
    }

    final EventProxy marking = getUsedDefaultMarking();
    for (final AutomatonProxy aut : originalAutomata) {
      checkAbort();
      final Collection<EventProxy> alphabet =
        new THashSet<EventProxy>(aut.getEvents());
      final Collection<StateProxy> states = aut.getStates();
      final StateProxy prev;
      if (previousMapOrig == null) {
        // Initial step ...
        assert resultEvent == null;
        prev = null;
        final DumpStateSearchData data = searchMap.get(null);
        for (final StateProxy state : states) {
          if (state.isInitial()) {
            data.addTargetState(state);
          }
        }
      } else {
        prev = previousMapOrig.get(aut);
      }
      final Collection<StateProxy> nonDumpStates;
      if (alphabet.contains(marking)) {
        final int numStates = states.size();
        nonDumpStates = new THashSet<StateProxy>(numStates);
      } else {
        nonDumpStates = null;
      }
      if (prev != null || nonDumpStates != null) {
        for (final TransitionProxy trans : aut.getTransitions()) {
          final StateProxy src = trans.getSource();
          if (nonDumpStates != null) {
            nonDumpStates.add(src);
          }
          if (src == prev) {
            final EventProxy event = trans.getEvent();
            final DumpStateSearchData data = searchMap.get(event);
            if (data != null) {
              final StateProxy target = trans.getTarget();
              data.addTargetState(target);
            }
          }
        }
      }
      final Iterator<Map.Entry<EventProxy,DumpStateSearchData>> iter =
        searchMap.entrySet().iterator();
      while (iter.hasNext()) {
        checkAbort();
        final Map.Entry<EventProxy,DumpStateSearchData> entry = iter.next();
        final EventProxy event = entry.getKey();
        final DumpStateSearchData data = entry.getValue();
        if (event != null && !alphabet.contains(event)) {
          final StateProxy state = previousMapOrig.get(aut);
          final boolean dump =
            nonDumpStates != null &&
            !nonDumpStates.contains(state) &&
            !state.getPropositions().contains(marking);
          data.setTarget(aut, state, dump);
        } else if (data.findTarget(aut, nonDumpStates, marking) == null) {
          iter.remove();
        }
      }
    }

    EventProxy event = null;
    DumpStateSearchData data = null;
    for (final Map.Entry<EventProxy,DumpStateSearchData> entry :
         searchMap.entrySet()) {
      data = entry.getValue();
      if (data.hasDumpState()) {
        event = entry.getKey();
        break;
      }
    }
    final AutomatonProxy resultAutomaton = getResultAutomaton();
    final Map<AutomatonProxy,StateProxy> nextMapOrig =
      new HashMap<AutomatonProxy,StateProxy>(nextMapResult);
    nextMapOrig.remove(resultAutomaton);
    final Map<AutomatonProxy,StateProxy> recordMap =
      data.getTargetStateMap();
    nextMapOrig.putAll(recordMap);
    final ProductDESProxyFactory factory = getFactory();
    return factory.createTraceStepProxy(event, nextMapOrig);
  }


  //#########################################################################
  //# Inner Class DumpStateSearchData
  private static class DumpStateSearchData
  {
    //#######################################################################
    //# Constructor
    private DumpStateSearchData(final int numAutomata)
    {
      mTargetStateMap = new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      mCurrentTargets = new ArrayList<StateProxy>();
      mHasDumpState = false;
    }

    //#######################################################################
    //# Simple Access
    private boolean hasDumpState()
    {
      return mHasDumpState;
    }

    private Map<AutomatonProxy,StateProxy> getTargetStateMap()
    {
      return mTargetStateMap;
    }

    //#######################################################################
    //# Searching
    private void addTargetState(final StateProxy target)
    {
      mCurrentTargets.add(target);
    }

    private StateProxy setTarget(final AutomatonProxy aut,
                                 final StateProxy state,
                                 final boolean dump)
    {
      mTargetStateMap.put(aut, state);
      mHasDumpState |= dump;
      return state;
    }
    private StateProxy findTarget(final AutomatonProxy aut,
                                  final Collection<StateProxy> nonDumpStates,
                                  final EventProxy marking)
    {
      StateProxy state = null;
      if (mCurrentTargets.isEmpty()) {
        // No successor with this event ...
        return null;
      } else if (nonDumpStates == null) {
        // Marking proposition not in automaton alphabet ...
        state = mCurrentTargets.get(0);
      } else {
        // Event and marking in alphabet --- try to find a dump state ...
        for (final StateProxy succ : mCurrentTargets) {
          if (!nonDumpStates.contains(succ) &&
              !succ.getPropositions().contains(marking)) {
            state = succ;
            mHasDumpState = true;
            break;
          } else if (state == null) {
            state = succ;
          }
        }
      }
      mCurrentTargets.clear();
      return setTarget(aut, state, false);
    }

    //#######################################################################
    //# Data Members
    private final Map<AutomatonProxy,StateProxy> mTargetStateMap;
    private final List<StateProxy> mCurrentTargets;
    private boolean mHasDumpState;
  }

}

