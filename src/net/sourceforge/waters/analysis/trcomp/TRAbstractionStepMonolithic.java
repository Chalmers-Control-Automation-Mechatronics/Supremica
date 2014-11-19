//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepMonolithic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * An abstraction step representing a subsystem that has failed monolithic
 * verification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepMonolithic
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepMonolithic(final List<TRAbstractionStep> preds,
                              final TraceProxy trace)
  {
    mPredecessors = preds;
    mMonolithicTrace = trace;
    for (final TRAbstractionStep pred : preds) {
      pred.setSuccessor(this);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return mPredecessors;
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
  {
    return null;
  }

  @Override
  public void expandTrace(final TRTraceProxy trace)
    throws AnalysisException
  {
    final List<EventProxy> events = mMonolithicTrace.getEvents();
    trace.reset(events);
    final int numSteps = trace.getNumberOfSteps();
    for (final TRAbstractionStep pred : mPredecessors) {
      final TRAutomatonProxy aut =
        pred.getOutputAutomaton(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final EventEncoding enc = aut.getEventEncoding();
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final int[] states = new int[numSteps];
      int stepIndex = 0;
      int current = -1;
      for (final TraceStepProxy step : mMonolithicTrace.getTraceSteps()) {
        final StateProxy state = step.getStateMap().get(aut);
        if (state != null) {
          current = aut.getStateIndex(state);
        } else if (stepIndex == 0) {
          current = findInitialState(rel);
        } else {
          final EventProxy event = step.getEvent();
          final int e = enc.getEventCode(event);
          if (e >= 0) {
            final byte status = enc.getProperEventStatus(e);
            if (EventStatus.isUsedEvent(status)) {
              current = findSuccessorState(rel, current, e);
            }
          }
        }
        states[stepIndex++] = current;
      }
      trace.addAutomaton(pred, states);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int findInitialState(final ListBufferTransitionRelation rel)
  {
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      if (rel.isInitial(s)) {
        return s;
      }
    }
    assert false : "Initial state for trace not found in automaton " +
                   rel.getName() + "!";
    return -1;
  }

  private int findSuccessorState(final ListBufferTransitionRelation rel,
                                 final int source,
                                 final int event)
  {
    final TransitionIterator iter =
      rel.createSuccessorsReadOnlyIterator(source, event);
    assert iter.advance() :
      "No successor state for trace not found in automaton " +
      rel.getName() + "!";
    final int target = iter.getCurrentTargetState();
    assert !iter.advance() :
      "Nondeterministic successor states for trace found in automaton " +
      rel.getName() + "!";
    return target;
  }


  //#########################################################################
  //# Data Members
  private final List<TRAbstractionStep> mPredecessors;
  private final TraceProxy mMonolithicTrace;

}
