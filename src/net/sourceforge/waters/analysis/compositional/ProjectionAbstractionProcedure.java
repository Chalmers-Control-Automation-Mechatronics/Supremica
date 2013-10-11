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
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstraction procedure to compute the natural projection.
 * The abstraction sequence consists of tau-loop removal, followed
 * by subset construction, a second step of tau-loop removal, and
 * finally the deterministic automata minimisation algorithm.
 *
 * @author Robi Malik
 */

class ProjectionAbstractionProcedure extends TRAbstractionProcedure
{

  //#########################################################################
  //# Constructor
  public static ProjectionAbstractionProcedure
    createProjectionAbstractionProcedure
      (final CompositionalSafetyVerifier verifier)
  {
    final int slimit = verifier.getInternalStateLimit();
    final int tlimit = verifier.getInternalTransitionLimit();
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover1 =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover1);
    final SubsetConstructionTRSimplifier subset =
      new SubsetConstructionTRSimplifier();
    chain.add(subset);
    subset.setStateLimit(slimit);
    subset.setTransitionLimit(tlimit);
    final TransitionRelationSimplifier loopRemover2 =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover2);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE);
    bisimulator.setTransitionLimit(tlimit);
    chain.add(bisimulator);
    return new ProjectionAbstractionProcedure(verifier, chain, subset);
  }


  //#########################################################################
  //# Constructor
  private ProjectionAbstractionProcedure
    (final CompositionalSafetyVerifier verifier,
     final TransitionRelationSimplifier simplifier,
     final SubsetConstructionTRSimplifier subset)
  {
    super(verifier, simplifier, false);
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
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps,
                     final Candidate candidate)
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
      final Collection<EventProxy> taus = Collections.singletonList(tau);
      final EventEncoding eventEnc = createEventEncoding(aut, taus, candidate);
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


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.TRAbstractionProcedure
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