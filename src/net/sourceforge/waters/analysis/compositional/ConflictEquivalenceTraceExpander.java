//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ConflictEquivalenceTraceExpander
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
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
     final List<int[]> partition)
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
     final List<int[]> partition,
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
    final Set<SearchRecord> visited = new THashSet<SearchRecord>();
    final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
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
        visited.add(record);
        open.add(record);
      }
    }
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    while (true) {
      checkAbort();
      final SearchRecord current = open.remove();
      final int source = current.getState();
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
        } else if (visited.add(record)) {
          open.add(record);
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
          } else if (visited.add(record)) {
            open.add(record);
          }
        }
      }
    }
  }

}
