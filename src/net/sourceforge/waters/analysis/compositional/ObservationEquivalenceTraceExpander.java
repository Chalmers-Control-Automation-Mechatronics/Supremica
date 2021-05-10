//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.tr.BFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * @author Robi Malik
 */

public class ObservationEquivalenceTraceExpander extends TRTraceExpander
{

  //#######################################################################
  //# Constructors
  protected ObservationEquivalenceTraceExpander
    (final AbstractCompositionalModelVerifier verifier,
     final EventProxy tau,
     final AutomatonProxy resultAut,
     final StateEncoding resultStateEnc,
     final AutomatonProxy originalAut,
     final TRPartition partition)
    throws AnalysisException
  {
    super(verifier, tau, resultAut, resultStateEnc, originalAut, partition);
  }

  protected ObservationEquivalenceTraceExpander
    (final AbstractCompositionalModelVerifier verifier,
     final EventProxy tau,
     final EventProxy preconditionMarking,
     final AutomatonProxy resultAut,
     final StateEncoding resultStateEnc,
     final AutomatonProxy originalAut,
     final StateEncoding originalStateEnc,
     final TRPartition partition,
     final boolean preconditionMarkingReduced)
    throws AnalysisException
  {
    super(verifier, tau, preconditionMarking,
          resultAut, resultStateEnc, originalAut, originalStateEnc,
          partition, preconditionMarkingReduced);
  }


  //#######################################################################
  //# Invocation
  @Override
  public List<TraceStepProxy> convertTraceSteps
    (final List<TraceStepProxy> traceSteps)
    throws AnalysisAbortException, OverflowException
  {
    final List<SearchRecord> crucialSteps = getCrucialSteps(traceSteps);
    final List<SearchRecord> convertedSteps =
      convertCrucialSteps(crucialSteps);
    mergeTraceSteps(traceSteps, convertedSteps);
    return traceSteps;
  }


  //#######################################################################
  //# Auxiliary Methods
  private List<SearchRecord> convertCrucialSteps
    (final List<SearchRecord> crucialSteps)
    throws AnalysisAbortException, OverflowException
  {
    final List<SearchRecord> foundSteps = new LinkedList<SearchRecord>();
    int state = -1;
    for (final SearchRecord crucialStep : crucialSteps) {
      SearchRecord found = convertCrucialStep(state, crucialStep);
      state = found.getState();
      // Append the found search records in reverse order to the result
      final int end = foundSteps.size();
      final ListIterator<SearchRecord> iter = foundSteps.listIterator(end);
      while (found.getPredecessor() != null) {
        iter.add(found);
        iter.previous();
        found = found.getPredecessor();
      }
    }
    return foundSteps;
  }

  /**
   * Finds a partial trace in the original automaton before observation
   * equivalence. This method computes a sequence of tau transitions, followed
   * by a transition with the given event, followed by another sequence of tau
   * transitions linking the source state to some state in the class of the
   * target state in the simplified automaton.
   * @param originalSource
   *         State number of the source state in the original automaton,
   *         or -1 to request a search starting from all initial states.
   * @param crucialStep
   *         Search record containing code of the event and state number of
   *         the target state in the simplified automaton (code of state
   *         class), with -1 requesting search for an alpha-marked state.
   * @return Search record describing the trace from source to
   *         target, in reverse order. The last entry in the list represents
   *         the first step after the source state, with its event and target
   *         state. The first step has a target state in the given target
   *         class. Events in the list can only be tau or the given event.
   */
  private SearchRecord convertCrucialStep(final int originalSource,
                                          final SearchRecord crucialStep)
    throws AnalysisAbortException, OverflowException
  {
    final int targetClass = crucialStep.getState();
    setupTarget(targetClass);
    // The crucial event may be tau, but only for the first or last step.
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int tau = EventEncoding.TAU;
    final int crucialEvent = crucialStep.getEvent();
    // There are two types of search records, representing the states
    // reached before or after execution of the crucial event, except
    // when the crucial event is tau. If the crucial event is tau, only
    // search states after the crucial event are considered, so a search
    // using only tau transitions is performed.
    final BFSSearchSpace<SearchRecord> searchSpace =
      new BFSSearchSpace<SearchRecord>();
    if (originalSource >= 0) {
      // Normal search starting from known state.
      final SearchRecord record;
      if (crucialEvent == tau) {
        record = new SearchRecord(originalSource, 1, -1, null);
        if (isTargetState(originalSource)) {
          return record;
        }
      } else {
        record = new SearchRecord(originalSource);
      }
      searchSpace.add(record);
    } else {
      // Start from initial state. The dummy record ensures that the first
      // real search record will later be included in the trace.
      final SearchRecord dummy = new SearchRecord(-1);
      final int numStates = rel.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        checkAbort();
        if (rel.isInitial(state)) {
          final SearchRecord record;
          if (crucialEvent == tau) {
            record = new SearchRecord(state, 1, -1, dummy);
            if (isTargetState(state)) {
              return record;
            }
          } else {
            record = new SearchRecord(state, 0, -1, dummy);
          }
          searchSpace.add(record);
        }
      }
    }
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final int defaultMarkingID = getDefaultMarkingID();
    SearchRecord deadlock = null;
    while (!searchSpace.isEmpty()) {
      checkAbort();
      final SearchRecord current = searchSpace.remove();
      final int source = current.getState();
      if (rel.isDeadlockState(source, defaultMarkingID)) {
        // If a deadlock state is encountered, remember it.
        // If we find nothing else, we will later use it as the result.
        if (deadlock == null) {
          deadlock = current;
        }
      } else {
        final int depth = current.getDepth();
        final boolean hasEvent = depth > 0;
        iter.reset(source, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final SearchRecord record =
            new SearchRecord(target, depth, tau, current);
          if (hasEvent && isTargetState(target)) {
            return record;
          } else {
            searchSpace.add(record);
          }
        }
        if (!hasEvent) {
          iter.reset(source, crucialEvent);
          while (iter.advance()) {
            final int target = iter.getCurrentTargetState();
            final SearchRecord record =
              new SearchRecord(target, 1, crucialEvent, current);
            if (isTargetState(target)) {
              return record;
            } else {
              searchSpace.add(record);
            }
          }
        }
      }
    }
    assert deadlock != null :
      "Trace expansion search exhausted, but no deadlock state found!";
    return deadlock;
  }

}
