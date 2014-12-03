//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   TRAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */
abstract class TRAbstractionProcedure
  extends AbstractAbstractionProcedure
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new abstraction procedure.
   * @param analyzer      The model analyser running the main analysis
   *                      operation.
   * @param simplifier    The transition relation simplifier implementing
   *                      the abstraction.
   * @param forceMarkings Whether or not all event encodings should be forced
   *                      to include the default marking and, in case of
   *                      generalised nonblocking, precondition marking.
   */
  TRAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final TransitionRelationSimplifier simplifier,
     final boolean forceMarkings)
  {
    super(analyzer);
    mSimplifier = simplifier;
    mForceMarkings = forceMarkings;
  }


  //#########################################################################
  //# Overrides for AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps,
                     final Candidate candidate)
    throws AnalysisException
  {
    try {
      final Iterator<EventProxy> iter = local.iterator();
      final EventProxy tau = iter.hasNext() ? iter.next() : null;
      final EventEncoding eventEnc = createEventEncoding(aut, local, candidate);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final int config = mSimplifier.getPreferredInputConfiguration();
      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc, inputStateEnc, config);
      getAnalyzer().showDebugLog(rel);
      final int numStates = rel.getNumberOfStates();
      final int numTrans = aut.getTransitions().size();
      final int numMarkings = rel.getNumberOfMarkings(false);
      mSimplifier.setTransitionRelation(rel);
      if (mSimplifier.run()) {
        rel = mSimplifier.getTransitionRelation();
        if (rel.getNumberOfReachableStates() == numStates &&
            rel.getNumberOfTransitions() == numTrans &&
            rel.getNumberOfMarkings(false) == numMarkings) {
          return false;
        }
        rel.removeRedundantPropositions();
        final ProductDESProxyFactory factory = getFactory();
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy convertedAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        final AbstractionStep step = createStep
          (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
        steps.add(step);
        return true;
      } else {
        return false;
      }
    } finally {
      mSimplifier.reset();
    }
  }

  @Override
  public void storeStatistics()
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addSimplifierStatistics(mSimplifier);
  }

  @Override
  public void resetStatistics()
  {
    mSimplifier.createStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mSimplifier.requestAbort();
  }

  @Override
  public boolean isAborting()
  {
    return mSimplifier.isAborting();
  }

  @Override
  public void resetAbort()
  {
    mSimplifier.resetAbort();
  }


  //#########################################################################
  //# Simple Access
  TransitionRelationSimplifier getSimplifier()
  {
    return mSimplifier;
  }


  //#########################################################################
  //# Overrides for AbstractAbstractionProcedure
  @Override
  protected EventEncoding createEventEncoding(final Collection<EventProxy> events,
                                              final Collection<EventProxy> local,
                                              final Candidate candidate)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final EventEncoding enc =
      super.createEventEncoding(events, local, candidate);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    int defaultMarkingID = -1;
    if (defaultMarking != null) {
      defaultMarkingID = enc.getEventCode(defaultMarking);
      if (defaultMarkingID < 0 && mForceMarkings) {
        defaultMarkingID =
          enc.addEvent(defaultMarking, translator, EventStatus.STATUS_UNUSED);
      }
    }
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    int preconditionMarkingID = -1;
    if (preconditionMarking != null) {
      preconditionMarkingID = enc.getEventCode(preconditionMarking);
      if (preconditionMarkingID < 0 && mForceMarkings) {
        preconditionMarkingID =
          enc.addEvent(preconditionMarking, translator,
                       EventStatus.STATUS_UNUSED);
      }
    }
    mSimplifier.setPropositions(preconditionMarkingID, defaultMarkingID);
    return enc;
  }


  //#########################################################################
  //# Auxiliary Methods
  abstract AbstractionStep createStep
    (final AutomatonProxy input,
     final StateEncoding inputStateEnc,
     final AutomatonProxy output,
     final StateEncoding outputStateEnc,
     final EventProxy tau);


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;
  /**
   * Whether or not all event encodings should be forced to include the
   * default marking and, in case of generalised nonblocking, precondition
   * marking.
   */
  private final boolean mForceMarkings;

}
