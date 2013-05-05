//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   TRConflictEquivalenceAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.CoreachabilityTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OmegaRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>An abstraction procedure based on a transition relation simplifier
 * ({@link ChainTRSimplifier}) used by a compositional conflict checker
 * ({@link CompositionalConflictChecker}).</P>
 *
 * <P>This class supports both standard and generalised nonblocking.
 * The two cases are distinguished by calling the method {@link
 * #getUsedPreconditionMarking()}, which return a non-null event
 * when a proper generalised nonblocking check is carried out.</P>
 *
 * @author Robi Malik
 */

class TRConflictEquivalenceAbstractionProcedure
  extends TRAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  public static TRConflictEquivalenceAbstractionProcedure
    createObservationEquivalenceProcedure
      (final AbstractCompositionalModelAnalyzer analyzer,
       final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    final int limit = analyzer.getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    return
      new TRConflictEquivalenceAbstractionProcedure(analyzer, chain, false);
  }

  public static TRConflictEquivalenceAbstractionProcedure
    createGeneralisedNonblockingProcedure
      (final AbstractCompositionalModelAnalyzer analyzer,
       final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final EventProxy preconditionMarking =
      analyzer.getConfiguredPreconditionMarking();
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final MarkingRemovalTRSimplifier alphaRemover =
      new MarkingRemovalTRSimplifier();
    chain.add(alphaRemover);
    final OmegaRemovalTRSimplifier omegaRemover =
      new OmegaRemovalTRSimplifier();
    chain.add(omegaRemover);
    if (preconditionMarking != null) {
      final CoreachabilityTRSimplifier nonCoreachableRemover =
        new CoreachabilityTRSimplifier();
      chain.add(nonCoreachableRemover);
    }
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    chain.add(silentOutRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final int limit = analyzer.getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
      new NonAlphaDeterminisationTRSimplifier();
    nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
    nonAlphaDeterminiser.setTransitionLimit(limit);
    chain.add(nonAlphaDeterminiser);
    if (preconditionMarking != null) {
      final AlphaDeterminisationTRSimplifier alphaDeterminiser =
        new AlphaDeterminisationTRSimplifier();
      alphaDeterminiser.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      alphaDeterminiser.setTransitionLimit(limit);
      chain.add(alphaDeterminiser);
    }
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    chain.add(saturator);
    return
      new TRConflictEquivalenceAbstractionProcedure(analyzer, chain, true);
  }


  //#########################################################################
  //# Constructor
  /**
   * Creates a new conflict equivalence abstraction procedure.
   * @param analyzer      The model analyser running the main analysis
   *                      operation.
   * @param simplifier    The transition relation simplifier implementing
   *                      the abstraction.
   * @param forceMarkings Whether or not all event encodings should be forced
   *                      to include the default marking and, in case of
   *                      generalised nonblocking, precondition marking.
   */
  TRConflictEquivalenceAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier simplifier,
     final boolean forceMarkings)
  {
    super(analyzer, simplifier, forceMarkings);
  }


  //#########################################################################
  //# Simple Access
  @Override
  ChainTRSimplifier getSimplifier()
  {
    return (ChainTRSimplifier) super.getSimplifier();
  }


  //#########################################################################
  //# Overrides for TRSimplifierAbstractionProcedure
  @Override
  protected EventEncoding createEventEncoding(final Collection<EventProxy> events,
                                              final EventProxy tau,
                                              final Candidate candidate)
  {
    final EventEncoding enc = super.createEventEncoding(events, tau, candidate);
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    if (preconditionMarking == null) {
      mPreconditionMarkingID = -1;
    } else {
      mPreconditionMarkingID = enc.getEventCode(preconditionMarking);
    }
    return enc;
  }

  @Override
  MergeStep createStep(final AutomatonProxy input,
                       final StateEncoding inputStateEnc,
                       final AutomatonProxy output,
                       final StateEncoding outputStateEnc,
                       final EventProxy tau)
  {
    final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
    final ChainTRSimplifier simplifier = getSimplifier();
    final List<int[]> partition = simplifier.getResultPartition();
    final boolean reduced =
      mPreconditionMarkingID >= 0 &&
      simplifier.isReducedMarking(mPreconditionMarkingID);
    if (simplifier.isObservationEquivalentAbstraction()) {
      return new ObservationEquivalenceStep(analyzer, output, input, tau,
                                            inputStateEnc, partition,
                                            reduced, outputStateEnc);
    } else {
      return new ConflictEquivalenceStep(analyzer, output, input, tau,
                                         inputStateEnc, partition,
                                         reduced, outputStateEnc);
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * The precondition marking ID used for the current event encoding.
   */
  private int mPreconditionMarkingID;

}
