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

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
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

public class ProjectionTraceExpander extends TRTraceExpander
{

  //#######################################################################
  //# Constructors
  protected ProjectionTraceExpander
    (final AbstractCompositionalModelVerifier verifier,
     final EventProxy tau,
     final AutomatonProxy resultAut,
     final AutomatonProxy originalAut)
    throws AnalysisException
  {
    super(verifier, tau, resultAut, originalAut);
  }


  //#######################################################################
  //# Invocation
  @Override
  public List<TraceStepProxy> convertTraceSteps
    (final List<TraceStepProxy> traceSteps)
    throws AnalysisAbortException, OverflowException
  {
    final TIntArrayList crucialEvents = getEventSteps(traceSteps);
    final SearchRecord endRecord = convertEventSteps(crucialEvents);
    final List<SearchRecord> convertedSteps = endRecord.getTrace();
    mergeTraceSteps(traceSteps, convertedSteps);
    return traceSteps;
  }


  //#######################################################################
  //# Auxiliary Methods
  private TIntArrayList getEventSteps(final List<TraceStepProxy> traceSteps)
  {
    final EventEncoding enc = getEventEncoding();
    final int len = traceSteps.size();
    final TIntArrayList eventSteps = new TIntArrayList(len);
    final Iterator<TraceStepProxy> iter = traceSteps.iterator();
    TraceStepProxy step = iter.next();
    while (iter.hasNext()) {
      step = iter.next();
      final EventProxy event = step.getEvent();
      final int eventID = enc.getEventCode(event);
      if (eventID <= 0) {
        // Step of another automaton only or tau --- skip.
      } else {
        // Step by a proper event ---
        eventSteps.add(eventID);
      }
    }
    return eventSteps;
  }

  private SearchRecord convertEventSteps(final TIntArrayList eventSteps)
    throws AnalysisAbortException, OverflowException
  {
    // 1. Collect initial states
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
    final Set<SearchRecord> visited = new THashSet<SearchRecord>();
    final int numStates = rel.getNumberOfStates();
    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        final SearchRecord record = new SearchRecord(state);
        if (eventSteps.isEmpty()) {
          return record;
        }
        visited.add(record);
        open.add(record);
      }
    }
    // 2. Breadth-first search
    final int tau = EventEncoding.TAU;
    final TransitionIterator iter =
      rel.createSuccessorsReadOnlyIterator();
    while (true) {
      checkAbort();
      final SearchRecord current = open.remove();
      final int state = current.getState();
      final int depth = current.getDepth();
      final int nextdepth = depth + 1;
      final int event = eventSteps.get(depth);
      iter.reset(state, event);
      while (iter.advance()) {
        final int target = iter.getCurrentTargetState();
        final SearchRecord next =
          new SearchRecord(target, nextdepth, event, current);
        if (nextdepth == eventSteps.size()) {
          return next;
        } else if (visited.add(next)) {
          open.add(next);
        }
      }
      iter.reset(state, tau);
      while (iter.advance()) {
        final int target = iter.getCurrentTargetState();
        final SearchRecord next =
          new SearchRecord(target, depth, tau, current);
        if (visited.add(next)) {
          open.add(next);
        }
      }
    }
  }

}








