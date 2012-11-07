//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ProjectionAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

class ProjectionAbstractionProcedure extends TRSimplifierAbstractionProcedure
{
  //#########################################################################
  //# Constructor
  ProjectionAbstractionProcedure
    (final CompositionalSafetyVerifier verifier,
     final TransitionRelationSimplifier simplifier,
     final SubsetConstructionTRSimplifier subset)
  {
    super(verifier, simplifier);
    mSubsetConstructionTRSimplifier = subset;
  }


  //#########################################################################
  //# Simple Access
  @Override
  CompositionalSafetyVerifier getAnalyzer()
  {
    return (CompositionalSafetyVerifier) super.getAnalyzer();
  }


  //#########################################################################
  //# Overrides for TRSimplifierAbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps)
    throws AnalysisException
  {
    final CompositionalSafetyVerifier verifier = getAnalyzer();
    final TransitionRelationSimplifier simplifier = getSimplifier();
    try {
      EventProxy tau = null;
      for (final EventProxy event : local) {
        if (verifier.getPropertyStatus(event) ==
            CompositionalSafetyVerifier.NONPROPERTY) {
          tau = event;
          break;
        }
      }
      final EventEncoding eventEnc = createEventEncoding(aut, tau);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final int config = simplifier.getPreferredInputConfiguration();
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      for (final EventProxy event : local) {
        if (verifier.getPropertyStatus(event) ==
            CompositionalSafetyVerifier.FORBIDDEN) {
          final int e = eventEnc.getEventCode(event);
          if (e >= 0) {
            mSubsetConstructionTRSimplifier.setForbiddenEvent(e, true);
          }
        }
      }
      verifier.showDebugLog(rel);
      simplifier.setTransitionRelation(rel);
      simplifier.run();
      final ProductDESProxyFactory factory = getFactory();
      final StateEncoding outputStateEnc = new StateEncoding();
      final AutomatonProxy convertedAut =
        rel.createAutomaton(factory, eventEnc, outputStateEnc);
      final AbstractionStep step = createStep
        (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
      steps.add(step);
      return true;
    } finally {
      simplifier.reset();
    }
  }

  @Override
  ProjectionStep createStep(final AutomatonProxy input,
                            final StateEncoding inputStateEnc,
                            final AutomatonProxy output,
                            final StateEncoding outputStateEnc,
                            final EventProxy tau)
  {
    final CompositionalSafetyVerifier verifier = getAnalyzer();
    return new ProjectionStep(verifier, output, input, tau);
  }


  //#########################################################################
  //# Data Members
  private final SubsetConstructionTRSimplifier
    mSubsetConstructionTRSimplifier;

}