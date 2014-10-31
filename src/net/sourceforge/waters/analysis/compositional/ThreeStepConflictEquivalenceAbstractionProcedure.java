//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ThreeStepConflictEquivalenceAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.certainconf.CertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A specialised abstraction procedure used by the compositional conflict
 * check algorithm. This abstraction procedure splits the abstraction
 * process into three stages. Abstraction steps before and after certain
 * conflicts are separated from certain conflicts computation to facilitate
 * counterexample expansion.
 *
 * @author Robi Malik
 */

class ThreeStepConflictEquivalenceAbstractionProcedure
  extends AbstractAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  public static ThreeStepConflictEquivalenceAbstractionProcedure
    createNBAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer,
       final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final boolean useLimitedCertainConflicts,
       final boolean useProperCertainConflicts)
  {
    final ChainTRSimplifier preChain = new ChainTRSimplifier();
    final ChainTRSimplifier postChain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setDumpStateAware(true);
    preChain.add(loopRemover);
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    preChain.add(markingRemover);
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    silentInRemover.setDumpStateAware(true);
    preChain.add(silentInRemover);
    final int limit = analyzer.getInternalTransitionLimit();
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setDumpStateAware(true);
    preChain.add(silentOutRemover);
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    preChain.add(incomingEquivalenceSimplifier);
    final LimitedCertainConflictsTRSimplifier limitedCertainConflictsRemover;
    if (useLimitedCertainConflicts) {
      limitedCertainConflictsRemover =
        new LimitedCertainConflictsTRSimplifier();
    } else {
      limitedCertainConflictsRemover = null;
    }
    final CertainConflictsTRSimplifier certainConflictsRemover;
    if (useProperCertainConflicts) {
      certainConflictsRemover = new CertainConflictsTRSimplifier();
    } else {
      certainConflictsRemover = null;
    }
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(limit);
    bisimulator.setDumpStateAware(true);
    postChain.add(bisimulator);
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    postChain.add(saturator);
    return new ThreeStepConflictEquivalenceAbstractionProcedure
      (analyzer, preChain, limitedCertainConflictsRemover,
       certainConflictsRemover, postChain);
  }

  public static ThreeStepConflictEquivalenceAbstractionProcedure
    createNBAAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer,
       final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final int limit = analyzer.getInternalTransitionLimit();
    final ChainTRSimplifier preChain = new ChainTRSimplifier();
    final ChainTRSimplifier postChain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setDumpStateAware(true);
    preChain.add(loopRemover);
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    preChain.add(markingRemover);
    final TransitionRemovalTRSimplifier transitionRemover =
      new TransitionRemovalTRSimplifier();
    transitionRemover.setTransitionLimit(limit);
    preChain.add(transitionRemover);
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    silentInRemover.setDumpStateAware(true);
    preChain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setDumpStateAware(true);
    preChain.add(silentOutRemover);
    final LimitedCertainConflictsTRSimplifier limitedCertainConflictsRemover =
      new LimitedCertainConflictsTRSimplifier();
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(limit);
    bisimulator.setDumpStateAware(true);
    postChain.add(bisimulator);
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    postChain.add(incomingEquivalenceSimplifier);
    final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
      new NonAlphaDeterminisationTRSimplifier();
    nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
    nonAlphaDeterminiser.setTransitionLimit(limit);
    nonAlphaDeterminiser.setDumpStateAware(true);
    postChain.add(nonAlphaDeterminiser);
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    postChain.add(saturator);
    return new ThreeStepConflictEquivalenceAbstractionProcedure
      (analyzer, preChain, limitedCertainConflictsRemover, null, postChain);
  }


  //#########################################################################
  //# Constructor
  private ThreeStepConflictEquivalenceAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier preChain,
     final LimitedCertainConflictsTRSimplifier limitedCCSimplifier,
     final CertainConflictsTRSimplifier ccSimplifier,
     final ChainTRSimplifier postChain)
  {
    super(analyzer);
    mPreChain = preChain;
    mLimitedCertainConflictsSimplifier = limitedCCSimplifier;
    mCertainConflictsSimplifier = ccSimplifier;
    mPostChain = postChain;
    mCompleteChain = new ChainTRSimplifier();
    mCompleteChain.add(preChain);
    if (limitedCCSimplifier != null) {
      mCompleteChain.add(limitedCCSimplifier);
    }
    if (ccSimplifier != null) {
      mCompleteChain.add(ccSimplifier);
    }
    mCompleteChain.add(postChain);
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
    try {
      assert local.size() <= 1 : "At most one tau event supported!";
      final ProductDESProxyFactory factory = getFactory();
      final Iterator<EventProxy> iter = local.iterator();
      final EventProxy tau = iter.hasNext() ? iter.next() : null;
      final EventEncoding eventEnc = createEventEncoding(aut, local, candidate);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final int config = mPreChain.getPreferredInputConfiguration();
      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
      analyzer.showDebugLog(rel);
      final int numStates = rel.getNumberOfStates();
      final int numTrans = aut.getTransitions().size();
      final int numMarkings = rel.getNumberOfMarkings();
      AutomatonProxy lastAut = aut;
      StateEncoding lastStateEnc = inputStateEnc;
      TRPartition partition = null;
      boolean oeq = true;
      boolean reduced = false;
      AbstractionStep preStep = null;
      mPreChain.setTransitionRelation(rel);
      if (mPreChain.run()) {
        rel = mPreChain.getTransitionRelation();
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy outputAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        partition = mPreChain.getResultPartition();
        oeq = mPreChain.isObservationEquivalentAbstraction();
        preStep = createStep(aut, inputStateEnc,
                             outputAut, outputStateEnc, tau,
                             partition, oeq, false);
        lastAut = outputAut;
        lastStateEnc = outputStateEnc;
      }
      boolean maybeBlocking = true;
      AbstractionStep lccStep = null;
      if (mLimitedCertainConflictsSimplifier != null) {
        mLimitedCertainConflictsSimplifier.setTransitionRelation(rel);
        if (mLimitedCertainConflictsSimplifier.run()) {
          rel = mLimitedCertainConflictsSimplifier.getTransitionRelation();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy outputAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          if (mLimitedCertainConflictsSimplifier.hasCertainConflictTransitions()) {
            lccStep = new LimitedCertainConflictsStep
              (analyzer, mLimitedCertainConflictsSimplifier, outputAut,
               lastAut, tau, lastStateEnc, outputStateEnc);
          } else {
            final TRPartition ccPart =
              mLimitedCertainConflictsSimplifier.getResultPartition();
            partition = TRPartition.combine(partition, ccPart);
            preStep = createStep(aut, inputStateEnc,
                                 outputAut, outputStateEnc, tau,
                                 partition, oeq, false);
          }
          lastAut = outputAut;
          lastStateEnc = outputStateEnc;
        }
        maybeBlocking =
          mLimitedCertainConflictsSimplifier.getMaxLevel() >= 0;
      }
      AbstractionStep ccStep = null;
      if (maybeBlocking && mCertainConflictsSimplifier != null) {
        mCertainConflictsSimplifier.setTransitionRelation(rel);
        if (mCertainConflictsSimplifier.run()) {
          rel = mCertainConflictsSimplifier.getTransitionRelation();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy outputAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          ccStep = new LimitedCertainConflictsStep
            (analyzer, mCertainConflictsSimplifier, outputAut, lastAut,
             tau, lastStateEnc, outputStateEnc);
          lastAut = outputAut;
          lastStateEnc = outputStateEnc;
        }
      }
      mPostChain.setTransitionRelation(rel);
      if (mPostChain.run()) {
        rel = mPostChain.getTransitionRelation();
        if (rel.getNumberOfReachableStates() == numStates &&
            rel.getNumberOfTransitions() == numTrans &&
            rel.getNumberOfMarkings() == numMarkings) {
          return false;
        } else if (lccStep == null && ccStep == null) {
          lastAut = aut;
          lastStateEnc = inputStateEnc;
          final TRPartition postPart = mPostChain.getResultPartition();
          partition = TRPartition.combine(partition, postPart);
          oeq &= mPostChain.isObservationEquivalentAbstraction();
        } else {
          recordStep(steps, preStep);
          recordStep(steps, lccStep);
          recordStep(steps, ccStep);
          partition = mPostChain.getResultPartition();
          oeq = mPostChain.isObservationEquivalentAbstraction();
          reduced = false;
        }
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy outputAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        final AbstractionStep postStep =
          createStep(lastAut, lastStateEnc, outputAut, outputStateEnc,
                     tau, partition, oeq, reduced);
        recordStep(steps, postStep);
      } else {
        recordStep(steps, preStep);
        recordStep(steps, lccStep);
        recordStep(steps, ccStep);
      }
      return !steps.isEmpty();
    } finally {
      mCompleteChain.reset();
    }
  }

  @Override
  public void storeStatistics()
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.setSimplifierStatistics(mCompleteChain);
  }

  @Override
  public void resetStatistics()
  {
    mCompleteChain.createStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mCompleteChain.requestAbort();
  }

  @Override
  public boolean isAborting()
  {
    return mCompleteChain.isAborting();
  }

  @Override
  public void resetAbort()
  {
    mCompleteChain.resetAbort();
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                              final Collection<EventProxy> local,
                                              final Candidate candidate)
    throws OverflowException
  {
    final EventEncoding enc = super.createEventEncoding(aut, local, candidate);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultMarkingID = enc.getEventCode(defaultMarking);
    if (defaultMarkingID < 0) {
      final KindTranslator translator = getKindTranslator();
      enc.addEvent(defaultMarking, translator, EventStatus.STATUS_UNUSED);
    }
    mCompleteChain.setDefaultMarkingID(defaultMarkingID);
    return enc;
  }

  private AbstractionStep createStep(final AutomatonProxy input,
                                     final StateEncoding inputStateEnc,
                                     final AutomatonProxy output,
                                     final StateEncoding outputStateEnc,
                                     final EventProxy tau,
                                     final TRPartition partition,
                                     final boolean oeq,
                                     final boolean reduced)
  {
    final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
    if (oeq) {
      return new ObservationEquivalenceStep(analyzer, output, input, tau,
                                            inputStateEnc, partition,
                                            reduced, outputStateEnc);
    } else {
      return new ConflictEquivalenceStep(analyzer, output, input, tau,
                                         inputStateEnc, partition,
                                         reduced, outputStateEnc);
    }
  }

  private void recordStep(final List<AbstractionStep> steps,
                          final AbstractionStep step)
  {
    if (step != null) {
      steps.add(step);
    }
  }


  //#########################################################################
  //# Data Members
  private final ChainTRSimplifier mPreChain;
  private final LimitedCertainConflictsTRSimplifier
    mLimitedCertainConflictsSimplifier;
  private final CertainConflictsTRSimplifier
    mCertainConflictsSimplifier;
  private final ChainTRSimplifier mPostChain;
  private final ChainTRSimplifier mCompleteChain;

}
