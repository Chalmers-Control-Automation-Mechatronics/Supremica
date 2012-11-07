//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   StandardConflictCheckerAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.certainconf.CertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

class StandardConflictCheckerAbstractionProcedure
  extends AbstractAbstractionProcedure
{
  //#######################################################################
  //# Constructor
  StandardConflictCheckerAbstractionProcedure
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

  //#######################################################################
  //# Overrides for AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps)
    throws AnalysisException
  {
    try {
      assert local.size() <= 1 : "At most one tau event supported!";
      final ProductDESProxyFactory factory = getFactory();
      final Iterator<EventProxy> iter = local.iterator();
      final EventProxy tau = iter.hasNext() ? iter.next() : null;
      final EventEncoding eventEnc = createEventEncoding(aut, tau);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final int config = mPreChain.getPreferredInputConfiguration();
      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
      analyzer.showDebugLog(rel);
      final int numStates = rel.getNumberOfStates();
      final int numTrans = rel.getNumberOfTransitions();
      final int numMarkings = rel.getNumberOfMarkings();
      AutomatonProxy lastAut = aut;
      StateEncoding lastStateEnc = inputStateEnc;
      List<int[]> partition = null;
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
            final List<int[]> ccPart =
              mLimitedCertainConflictsSimplifier.getResultPartition();
            partition = ChainTRSimplifier.mergePartitions(partition, ccPart);
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
          final List<int[]> postPart = mPostChain.getResultPartition();
          partition = ChainTRSimplifier.mergePartitions(partition, postPart);
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
  public void requestAbort()
  {
    mCompleteChain.requestAbort();
  }

  public boolean isAborting()
  {
    return mCompleteChain.isAborting();
  }


  //#########################################################################
  //# Auxiliary Methods
  protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                              final EventProxy tau)
  {
    final KindTranslator translator = getKindTranslator();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final Collection<EventProxy> filter =
      Collections.singletonList(defaultMarking);
    final EventEncoding enc =
      new EventEncoding(aut, translator, tau, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    final int defaultMarkingID = enc.getEventCode(defaultMarking);
    if (defaultMarkingID < 0) {
      enc.addEvent(defaultMarking, translator, true);
    }
    mCompleteChain.setDefaultMarkingID(defaultMarkingID);
    return enc;
  }

  private AbstractionStep createStep(final AutomatonProxy input,
                                     final StateEncoding inputStateEnc,
                                     final AutomatonProxy output,
                                     final StateEncoding outputStateEnc,
                                     final EventProxy tau,
                                     final List<int[]> partition,
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
