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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import org.apache.log4j.Logger;


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
  TRAbstractionStepMonolithic(final String name,
                              final List<TRAbstractionStep> preds,
                              final TraceProxy trace)
  {
    super(name);
    mPredecessors = preds;
    mMonolithicTrace = trace;
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
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalAnalyzer analyzer)
    throws AnalysisException
  {
    final List<EventProxy> events = mMonolithicTrace.getEvents();
    trace.reset(events);
    final int numSteps = trace.getNumberOfSteps();

    final Map<String,TRAutomatonProxy> traceAutomataMap =
      new HashMap<>(mPredecessors.size());
    for (final AutomatonProxy aut : mMonolithicTrace.getAutomata()) {
      final String name = aut.getName();
      final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
      traceAutomataMap.put(name, tr);
    }

    for (final TRAbstractionStep pred : mPredecessors) {
      analyzer.checkAbort();
      final String name = pred.getName();
      final TRAutomatonProxy aut = traceAutomataMap.get(name);
      final EventEncoding enc = aut.getEventEncoding();
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final int[] states = new int[numSteps];
      int stepIndex = 0;
      int current = -1;
      for (final TraceStepProxy step : mMonolithicTrace.getTraceSteps()) {
        final StateProxy state = step.getStateMap().get(aut);
        if (state != null) {
          current = aut.getStateIndex(state);
        } else if (stepIndex == 0) {
          current = rel.getFirstInitialState();
          assert current >= 0 || trace instanceof SafetyTraceProxy :
            "No initial state found in trace for automaton " + name + "!";
        } else if (current >= 0){
          final EventProxy event = step.getEvent();
          final int e = enc.getEventCode(event);
          if (e >= 0) {
            final byte status = enc.getProperEventStatus(e);
            if (EventStatus.isUsedEvent(status)) {
              iter.reset(current, e);
              if (iter.advance()) {
                current = iter.getCurrentTargetState();
                assert !iter.advance() :
                  "Nondeterministic successor states for trace found " +
                  "in automaton " + name + "!";
              } else {
                current = -1;
                assert trace instanceof SafetyTraceProxy :
                  "No successor state found in trace for automaton " +
                  name + "!";
              }
            }
          }
        }
        states[stepIndex++] = current;
      }
      trace.addAutomaton(pred, states);
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public void report(final Logger logger)
  {
    logger.debug("Converting monolithic trace ...");
  }


  //#########################################################################
  //# Data Members
  private final List<TRAbstractionStep> mPredecessors;
  private final TraceProxy mMonolithicTrace;

}
