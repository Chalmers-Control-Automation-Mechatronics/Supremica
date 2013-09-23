//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFASimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.BlockedEventsRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.HidingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OmegaRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.efa.efsm.EFSMConflictChecker;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;

import org.apache.log4j.Logger;


/**
 * <P>An abstraction procedure based on a transition relation simplifier
 * ({@link ChainTRSimplifier}) used by the EFSM conflict checker
 * ({@link EFSMConflictChecker}).</P>
 *
 * @author Robi Malik
 */

class UnifiedEFASimplifier extends AbstractEFAAlgorithm
{

  //#########################################################################
  //# Factory Methods
  public static UnifiedEFASimplifier createObservationEquivalenceProcedure
      (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final int limit)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final HidingTRSimplifier hiding = new HidingTRSimplifier();
    chain.add(hiding);
    final BlockedEventsRemovalTRSimplifier blockedRemover =
      new BlockedEventsRemovalTRSimplifier();
    chain.add(blockedRemover);
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
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    return new UnifiedEFASimplifier(chain);
  }

  public static UnifiedEFASimplifier
    createGeneralisedNonblockingProcedure
      (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final int limit)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final HidingTRSimplifier hiding = new HidingTRSimplifier();
    chain.add(hiding);
    final BlockedEventsRemovalTRSimplifier blockedRemover =
      new BlockedEventsRemovalTRSimplifier();
    chain.add(blockedRemover);
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final MarkingRemovalTRSimplifier alphaRemover =
      new MarkingRemovalTRSimplifier();
    chain.add(alphaRemover);
    final OmegaRemovalTRSimplifier omegaRemover =
      new OmegaRemovalTRSimplifier();
    chain.add(omegaRemover);
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
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
      new NonAlphaDeterminisationTRSimplifier();
    nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
    nonAlphaDeterminiser.setTransitionLimit(limit);
    chain.add(nonAlphaDeterminiser);
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    chain.add(saturator);
    return new UnifiedEFASimplifier(chain);
  }


  public static UnifiedEFASimplifier
    createStandardNonblockingProcedure
      (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final int limit)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final HidingTRSimplifier hiding = new HidingTRSimplifier();
    chain.add(hiding);
    final BlockedEventsRemovalTRSimplifier blockedRemover =
      new BlockedEventsRemovalTRSimplifier();
    chain.add(blockedRemover);
    final TauLoopRemovalTRSimplifier tauLoopRemover =
      new TauLoopRemovalTRSimplifier();
    tauLoopRemover.setDumpStateAware(true);
    chain.add(tauLoopRemover);
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    chain.add(markingRemover);
    final TransitionRemovalTRSimplifier transitionRemover =
      new TransitionRemovalTRSimplifier();
    transitionRemover.setTransitionLimit(limit);
    chain.add(transitionRemover);
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    silentInRemover.setDumpStateAware(true);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setDumpStateAware(true);
    chain.add(silentOutRemover);
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    incomingEquivalenceSimplifier.setTransitionLimit(limit);
    chain.add(incomingEquivalenceSimplifier);
    final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
      new LimitedCertainConflictsTRSimplifier();
    chain.add(certainConflictsRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setUsingSpecialEvents(true);
    bisimulator.setDumpStateAware(true);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    chain.add(saturator);
    return new UnifiedEFASimplifier(chain);
  }


  //#########################################################################
  //# Constructor
  /**
   * Creates a new conflict equivalence abstraction procedure.
   * @param simplifier    The transition relation simplifier implementing
   *                      the abstraction.
   */
  private UnifiedEFASimplifier(final TransitionRelationSimplifier simplifier)
  {
    mSimplifier = simplifier;
  }


  //#########################################################################
  //# Simple Access
  TransitionRelationSimplifier getSimplifier()
  {
    return mSimplifier;
  }


  /**
   * Stores statistics in the given list. This method is used to
   * collect detailed statistics for each individual simplifier invoked by
   * this simplifier.
   * @param  list           Statistics records are added to the end of this
   *                        list, in order of invocation of the simplifiers.
   */
  public void collectStatistics(final List<TRSimplifierStatistics> list)
  {
    mSimplifier.collectStatistics(list);
  }



  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mSimplifier.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mSimplifier.resetAbort();
  }


  //#########################################################################
  //# Invocation
  public UnifiedEFATransitionRelation run(final UnifiedEFATransitionRelation tr)
    throws AnalysisException
  {
    try {
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        logger.debug("Simplifying: " + tr.getName() + " ...");
        logger.debug(rel.getNumberOfReachableStates() + " states, " +
                     rel.getNumberOfTransitions() + " transitions");
      }
      final long start = System.currentTimeMillis();
      final ListBufferTransitionRelation oldRel = tr.getTransitionRelation();
      final int config = mSimplifier.getPreferredInputConfiguration();
      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(oldRel, config);
      final int numStates = rel.getNumberOfStates();
      final int numTrans = rel.getNumberOfTransitions();
      final int numMarkings = rel.getNumberOfMarkings();
      mSimplifier.setTransitionRelation(rel);
      final int prop =
        rel.isUsedProposition(UnifiedEFAEventEncoding.OMEGA) ? 0 : -1;
      mSimplifier.setDefaultMarkingID(prop);
      if (mSimplifier.run()) {
        rel = mSimplifier.getTransitionRelation();
        final int newNumReachableStates = rel.getNumberOfReachableStates();
        if (newNumReachableStates == numStates &&
            rel.getNumberOfTransitions() == numTrans &&
            rel.getNumberOfMarkings() == numMarkings &&
            !isAlphabetChanged(oldRel, rel)) {
          return null;
        }
        final UnifiedEFATransitionRelation newEFSMTR =
          new UnifiedEFATransitionRelation(rel, tr.getEventEncoding());
        if (logger.isDebugEnabled()) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          final String msg = String.format("%d states, %.3f seconds",
                                           newNumReachableStates,
                                           difftime);
          logger.debug(msg);
        }
        return newEFSMTR;
      } else {
        if (logger.isDebugEnabled()) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          final String msg = String.format("No change, %.3f seconds", difftime);
          logger.debug(msg);
        }
        return null;
      }
    } finally {
      mSimplifier.reset();
    }
  }

  private boolean isAlphabetChanged(final ListBufferTransitionRelation rel1,
                                    final ListBufferTransitionRelation rel2)
  {
    if (rel1.getNumberOfProperEvents() != rel2.getNumberOfProperEvents()) {
      return true;
    } else {
      for (int e = EventEncoding.TAU; e < rel1.getNumberOfProperEvents(); e++) {
        final byte status1 = rel1.getProperEventStatus(e);
        final byte status2 = rel2.getProperEventStatus(e);
        if (status1 != status2) {
          return true;
        }
      }
      return false;
    }
  }

  //#########################################################################
  //# Logging
  @Override
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;

}
