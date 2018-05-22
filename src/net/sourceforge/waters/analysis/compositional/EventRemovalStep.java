//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * An abstraction step that consists of removing some events from the
 * model. Event removal is implemented by replacing automata with simplified
 * copies that use the same state objects, so trace expansion can be
 * achieved by replacing only the automata in a trace.
 *
 * @author Robi Malik
 */

class EventRemovalStep extends AbstractionStep
{

  //#######################################################################
  //# Constructor
  /**
   * Creates a new event removal step.
   * @param  analyzer  The model analyser that owns this abstraction step.
   * @param  results   List of automata after event removal.
   * @param  originals List of automata before removal, with indexes
   *                   matching those of results. The automaton at position
   *                   <I>i</I> in originals is replaced by the automaton
   *                   at the same position in results.
   * @param  stateMap  If states have been replaced during event removal,
   *                   this map contains mappings from old to new states.
   * @param  failing   Collection of failing events or <CODE>null</CODE>.
   *                   Failing events need special (slow) treatment in
   *                   trace expansion.
   */
  EventRemovalStep(final AbstractCompositionalModelAnalyzer analyzer,
                   final List<AutomatonProxy> results,
                   final List<AutomatonProxy> originals,
                   final Map<StateProxy,StateProxy> stateMap,
                   final Collection<EventProxy> failing)
  {
    super(analyzer, results, originals);
    mStateMap = stateMap;
    if (failing == null || failing.isEmpty()){
      mFailingEvents = null;
    } else {
      mFailingEvents = failing;
    }
  }


  //#######################################################################
  //# Simple Access
  public Map<StateProxy,StateProxy> getStateMap()
  {
    return mStateMap;
  }

  public void addAutomatonPair(final AutomatonProxy result,
                               final AutomatonProxy original,
                               final Map<StateProxy,StateProxy> stateMap)
  {
    addAutomatonPair(result, original);
    mStateMap.putAll(stateMap);
  }


  //#######################################################################
  //# Trace Computation
  @Override
  List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
  {
    final List<AutomatonProxy> originals = getOriginalAutomata();
    final List<AutomatonProxy> results = getResultAutomata();
    final int numAutomata = results.size();
    final Map<AutomatonProxy,AutomatonProxy> autMap =
      new HashMap<>(numAutomata);
    final Map<StateProxy,StateProxy> stateMap = new HashMap<>();
    final Iterator<AutomatonProxy> originalIter = originals.iterator();
    final Iterator<AutomatonProxy> resultIter = results.iterator();
    while (resultIter.hasNext()) {
      final AutomatonProxy original = originalIter.next();
      final AutomatonProxy result = resultIter.next();
      autMap.put(result, original);
      addToStateMap(original, result, stateMap);
    }
    final ProductDESProxyFactory factory = getFactory();
    TraceStepProxy previousStep = null;
    final ListIterator<TraceStepProxy> iter = steps.listIterator();
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final EventProxy event = step.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final int size = stepMap.size();
      final Map<AutomatonProxy,StateProxy> newStepMap =
        new HashMap<AutomatonProxy,StateProxy>(size);
      for (final Map.Entry<AutomatonProxy,StateProxy> entry :
           stepMap.entrySet()) {
        final AutomatonProxy aut = entry.getKey();
        AutomatonProxy newAut = autMap.get(aut);
        if (newAut == null) {
          newAut = aut;
        }
        StateProxy target = entry.getValue();
        final StateProxy altTarget = stateMap.get(target);
        if (altTarget != null) {
          target = altTarget;
        }
        if (mFailingEvents != null && event != null &&
            mFailingEvents.contains(event) && previousStep != null) {
          final StateProxy source = previousStep.getStateMap().get(newAut);
          target = getAlternativeSuccessor(newAut, source, event, target);
        }
        newStepMap.put(newAut, target);
      }
      final TraceStepProxy newStep =
        factory.createTraceStepProxy(event, newStepMap);
      iter.set(newStep);
      previousStep = newStep;
    }
    return steps;
  }


  //#######################################################################
  //# Auxiliary Methods
  private void addToStateMap(final AutomatonProxy original,
                             final AutomatonProxy result,
                             final Map<StateProxy,StateProxy> stateMap)
  {
    if (result != original) {
      for (final StateProxy originalState : original.getStates()) {
        final StateProxy resultState = mStateMap.get(originalState);
        if (resultState == null) {
          stateMap.put(originalState, originalState);
        } else {
          stateMap.put(resultState, originalState);
        }
      }
    }
  }

  private static StateProxy getAlternativeSuccessor(final AutomatonProxy aut,
                                                    final StateProxy source,
                                                    final EventProxy event,
                                                    final StateProxy target)
  {
    if (aut.getEvents().contains(event)) {
      StateProxy result = null;
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (trans.getSource() == source && trans.getEvent() == event) {
          if (trans.getTarget() == target) {
            return target;
          } else if (result == null) {
            result = trans.getTarget();
          }
        }
      }
      return result;
    } else {
      return target;
    }
  }


  //#######################################################################
  //# Data Members
  private final Map<StateProxy,StateProxy> mStateMap;
  private final Collection<EventProxy> mFailingEvents;

}
