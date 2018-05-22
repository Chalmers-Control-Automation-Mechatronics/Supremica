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

import java.util.LinkedList;
import java.util.List;

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

public class ConflictEquivalenceTraceExpander extends TRTraceExpander
{

  //#######################################################################
  //# Constructors
  protected ConflictEquivalenceTraceExpander
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

  protected ConflictEquivalenceTraceExpander
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
    int len = crucialSteps.size();
    SearchRecord last = crucialSteps.get(len - 1);
    if (last.getState() < 0) {
      len--;
      last = crucialSteps.get(len - 1);
    }
    final int targetClass = last.getState();
    setupTarget(targetClass);
    final SearchRecord[] crucialArray = new SearchRecord[len];
    int index = 0;
    for (final SearchRecord crucialStep : crucialSteps) {
      if (index >= len) {
        break;
      }
      crucialArray[index++] = crucialStep;
    }
    SearchRecord found = convertCrucialSteps(crucialArray);
    // Append the found search records in reverse order to the result
    final List<SearchRecord> foundSteps = new LinkedList<SearchRecord>();
    while (found.getPredecessor() != null) {
      foundSteps.add(0, found);
      found = found.getPredecessor();
    }
    return foundSteps;
  }

  private SearchRecord convertCrucialSteps(final SearchRecord[] crucialSteps)
    throws AnalysisAbortException, OverflowException
  {
    final int tau = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final BFSSearchSpace<SearchRecord> searchSpace =
      new BFSSearchSpace<SearchRecord>();
    final boolean firstEnd =
      crucialSteps.length == 1 && crucialSteps[0].getEvent() == tau;
    // The dummy record ensures that the first
    // real search record will later be included in the trace.
    final SearchRecord dummy = new SearchRecord(-1);
    final int numStates = rel.getNumberOfStates();
    for (int state = 0; state < numStates; state++) {
      checkAbort();
      if (rel.isInitial(state)) {
        final SearchRecord record;
        if (!firstEnd) {
          record = new SearchRecord(state, 0, -1, dummy);
        } else if (!isTargetState(state)) {
          record = new SearchRecord(state, 1, -1, dummy);
        } else {
          record = new SearchRecord(state, 2, -1, dummy);
          if (isTraceEndState(state)) {
            return record;
          }
        }
        searchSpace.add(record);
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
        iter.reset(source, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          int nextDepth = depth;
          if (nextDepth == crucialSteps.length && isTargetState(target)) {
            nextDepth++;
          }
          final SearchRecord record =
            new SearchRecord(target, nextDepth, tau, current);
          if (nextDepth > crucialSteps.length && isTraceEndState(target)) {
            return record;
          } else {
            searchSpace.add(record);
          }
        }
        if (depth < crucialSteps.length) {
          final int event = crucialSteps[depth].getEvent();
          iter.reset(source, event);
          while (iter.advance()) {
            final int target = iter.getCurrentTargetState();
            int nextDepth = depth + 1;
            if (nextDepth == crucialSteps.length && isTargetState(target)) {
              nextDepth++;
            }
            final SearchRecord record =
              new SearchRecord(target, nextDepth, event, current);
            if (nextDepth > crucialSteps.length && isTraceEndState(target)) {
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
