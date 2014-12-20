//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepPreconditionSaturation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.BFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StatusGroupTransitionIterator;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.log4j.Logger;


/**
 * An abstraction step representing a marking saturation step during
 * generalised nonblocking verification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepPreconditionSaturation
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepPreconditionSaturation
    (final TRAbstractionStep pred,
     final EventEncoding eventEncoding,
     final TransitionRelationSimplifier simplifier)
  {
    super(pred.getName());
    mPredecessor = pred;
    mEventEncoding = eventEncoding;
    mSimplifier = simplifier;
  }


  //#########################################################################
  //# Simple Access
  TRAbstractionStep getPredecessor()
  {
    return mPredecessor;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return Collections.singletonList(mPredecessor);
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
    throws AnalysisException
  {
    mSimplifier.setPropositions(TRCompositionalConflictChecker.PRECONDITION_MARKING,
                                TRCompositionalConflictChecker.DEFAULT_MARKING);
    mSimplifier.setPreferredOutputConfiguration(preferredConfig);
    final int inputConfig = mSimplifier.getPreferredInputConfiguration();
    final TRAutomatonProxy aut =
      mPredecessor.getOutputAutomaton(inputConfig);
    // We are going to destructively change this automaton,
    // so we need to clear the copy cached on the predecessor.
    mPredecessor.clearOutputAutomaton();
    reportRebuilding();
    final Logger logger = getLogger();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    rel.logSizes(logger);
    mSimplifier.setTransitionRelation(rel);
    mSimplifier.run();
    return aut;
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalAnalyzer analyzer)
    throws AnalysisException
  {
    trace.replaceAutomaton(this, mPredecessor);
    final TRAutomatonProxy aut = mPredecessor.getOutputAutomaton
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int oldNumSteps = trace.getNumberOfSteps();
    final int startState = trace.getState(mPredecessor, oldNumSteps - 1);
    if (!rel.isMarked(startState,
                      TRCompositionalConflictChecker.PRECONDITION_MARKING)) {
      final TRTraceSearchRecord found =
        findTraceToPreconditionMarking(rel, startState);
      final ProductDESProxy des = analyzer.getModel();
      final TRTraceProxy extension = createTraceExtension(found, des);
      trace.append(extension);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private TRTraceSearchRecord findTraceToPreconditionMarking
    (final ListBufferTransitionRelation rel, final int startState)
  {
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TransitionIterator inner = rel.createSuccessorsReadOnlyIterator();
    final TransitionIterator iter = new StatusGroupTransitionIterator
      (inner, mEventEncoding, EventStatus.STATUS_LOCAL);
    final BFSSearchSpace<TRTraceSearchRecord> searchSpace =
      new BFSSearchSpace<>(rel.getNumberOfStates());
    final TRTraceSearchRecord startRecord = new TRTraceSearchRecord(startState);
    searchSpace.add(startRecord);
    while (!searchSpace.isEmpty()) {
      final TRTraceSearchRecord current = searchSpace.poll();
      final int source = current.getState();
      iter.resetState(source);
      while (iter.advance()) {
        final int target = iter.getCurrentTargetState();
        final int event = iter.getCurrentEvent();
        final TRTraceSearchRecord next =
          new TRTraceSearchRecord(target, current, event, true);
        if (rel.isMarked(target,
                         TRCompositionalConflictChecker.PRECONDITION_MARKING)) {
          return next;
        }
        searchSpace.add(next);
      }
    }
    assert false : "Failed to extend trace to precondition-marked state!";
    return null;
  }

  private TRTraceProxy createTraceExtension(final TRTraceSearchRecord found,
                                            final ProductDESProxy des)
  {
    final List<TRTraceSearchRecord> searchRecordTrace =
      found.getSearchRecordTrace();
    final int numSteps = searchRecordTrace.size();
    final List<EventProxy> events = new ArrayList<>(numSteps - 1);
    final int[] states = new int[numSteps];
    int s = 0;
    for (final TRTraceSearchRecord record : searchRecordTrace) {
      if (s > 0) {
        final int e = record.getEvent();
        final EventProxy event = mEventEncoding.getProperEvent(e);
        events.add(event);
      }
      states[s++] = record.getState();
    }
    final TRTraceProxy extension = new TRConflictTraceProxy(des, events);
    extension.addAutomaton(mPredecessor, states);
    return extension;
  }


  //#########################################################################
  //# Debugging
  @Override
  public void reportExpansion()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Expanding precondition saturation of " + getName() + " ...");
    }
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final EventEncoding mEventEncoding;
  private final TransitionRelationSimplifier mSimplifier;

}
