//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.abstraction.ActiveEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.AlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.GNBCoreachabilityTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.abstraction.OmegaRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SelfloopSubsumptionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.TauEliminationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;

/**
 *
 * @author Benjamin Wheeler
 */
public class ChainBuilder
{

  public static ChainTRSimplifier createObservationEquivalenceChain
  (final Equivalence equivalence)
  {
    return createObservationEquivalenceChain(equivalence,
                                             null, null);
  }

  public static ChainTRSimplifier createObservationEquivalenceChain
  (final Equivalence equivalence,
   final TRSimplificationListener listener,
   final TRSimplificationListener specialEventsListener)
  {
    final ChainTRSimplifier chain = startAbstractionChain(specialEventsListener);
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setSimplificationListener(listener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    bisimulator.setSimplificationListener(listener);
    chain.add(bisimulator);

    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_Equivalence);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_TransitionRemovalMode);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_MarkingMode);

    chain.blacklistOption(TRSimplifierFactory.OPTION_AbstractMarking_DefaultMarkingID);
    chain.blacklistOption(TRSimplifierFactory.OPTION_AbstractMarking_PreconditionMarkingID);

    return chain;
  }

  public static ChainTRSimplifier startAbstractionChain(final TRSimplificationListener specialEventsListener)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final SpecialEventsTRSimplifier special = new SpecialEventsTRSimplifier();
    special.setSimplificationListener(specialEventsListener);
    chain.add(special);
    return chain;
  }

  public static ChainTRSimplifier createConflictEquivalenceChain
  (final Equivalence equivalence,
   final boolean certainConflicts,
   final boolean earlyTransitionRemoval,
   final boolean selfloopSubsumption,
   final boolean nonAlphaDeterminisation) throws AnalysisConfigurationException
  {
    return createConflictEquivalenceChain(equivalence,
                                          certainConflicts,
                                          earlyTransitionRemoval,
                                          selfloopSubsumption,
                                          nonAlphaDeterminisation,
                                          null, null, null, null);
  }

  public static ChainTRSimplifier createConflictEquivalenceChain
  (final Equivalence equivalence,
   final boolean certainConflicts,
   final boolean earlyTransitionRemoval,
   final boolean selfloopSubsumption,
   final boolean nonAlphaDeterminisation,
   final TRSimplificationListener specialEventsListener,
   final TRSimplificationListener markingListener,
   final TRSimplificationListener partitioningListener,
   final TRSimplificationListener certainConflictsListener)
  throws AnalysisConfigurationException
  {
    final ChainTRSimplifier chain = startAbstractionChain(specialEventsListener);
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setDumpStateAware(true);
    loopRemover.setSimplificationListener(partitioningListener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier.TransitionRemoval trMode;
    if (earlyTransitionRemoval) {
      final TransitionRemovalTRSimplifier transitionRemover =
        new TransitionRemovalTRSimplifier();
      transitionRemover.setSimplificationListener(partitioningListener);
      chain.add(transitionRemover);
      trMode = ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER;
    } else {
      trMode = ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL;
    }
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    markingRemover.setSimplificationListener(markingListener);
    chain.add(markingRemover);
    if (selfloopSubsumption) {
      final SelfloopSubsumptionTRSimplifier selfloopRemover =
        new SelfloopSubsumptionTRSimplifier();
      selfloopRemover.setSimplificationListener(partitioningListener);
      chain.add(selfloopRemover);
    }
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    silentInRemover.setDumpStateAware(true);
    silentInRemover.setSimplificationListener(partitioningListener);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setDumpStateAware(true);
    silentOutRemover.setSimplificationListener(partitioningListener);
    chain.add(silentOutRemover);
    if (certainConflicts) {
      final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
        new LimitedCertainConflictsTRSimplifier();
      certainConflictsRemover.setSimplificationListener(certainConflictsListener);
      chain.add(certainConflictsRemover);
    }
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode(trMode);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setDumpStateAware(true);
    bisimulator.setSimplificationListener(partitioningListener);
    chain.add(bisimulator);
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    incomingEquivalenceSimplifier.setSimplificationListener(partitioningListener);
    chain.add(incomingEquivalenceSimplifier);
    if (selfloopSubsumption) {
      final ActiveEventsTRSimplifier activeEventsSimplifier =
        new ActiveEventsTRSimplifier();
      activeEventsSimplifier.setSimplificationListener(partitioningListener);
      chain.add(activeEventsSimplifier);
    }
    if (nonAlphaDeterminisation) {
      final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
        new NonAlphaDeterminisationTRSimplifier();
      nonAlphaDeterminiser.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      nonAlphaDeterminiser.setDumpStateAware(true);
      nonAlphaDeterminiser.setSimplificationListener(partitioningListener);
      chain.add(nonAlphaDeterminiser);
    }
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    saturator.setSimplificationListener(markingListener);
    chain.add(saturator);

    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_Equivalence);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_TransitionRemovalMode);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_MarkingMode);
    chain.blacklistOption(TRSimplifierFactory.OPTION_TransitionRelationSimplifier_DumpStateAware);


    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_PropositionMask);
    chain.blacklistOption(TRSimplifierFactory.OPTION_AbstractMarking_PreconditionMarkingID);

    return chain;
  }

  public static ChainTRSimplifier createGeneralisedNonblockingChain
  (final Equivalence equivalence,
   final boolean earlyTransitionRemoval, final boolean hasConfiguredPreconditionMarking)
  {
    return createGeneralisedNonblockingChain(equivalence, earlyTransitionRemoval,
                                             hasConfiguredPreconditionMarking,
                                             null, null, null, null);
  }

  public static ChainTRSimplifier createGeneralisedNonblockingChain
  (final Equivalence equivalence,
   final boolean earlyTransitionRemoval,
   final boolean hasConfiguredPreconditionMarking,
   final TRSimplificationListener specialEventsListener,
   final TRSimplificationListener markingListener,
   final TRSimplificationListener omegaRemovalListener,
   final TRSimplificationListener partitioningListener)
  {
    final ChainTRSimplifier chain = startAbstractionChain(specialEventsListener);
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setSimplificationListener(partitioningListener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier.TransitionRemoval trMode;
    if (earlyTransitionRemoval) {
      final TransitionRemovalTRSimplifier transitionRemover =
        new TransitionRemovalTRSimplifier();
      transitionRemover.setSimplificationListener(partitioningListener);
      chain.add(transitionRemover);
      trMode = ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER;
    } else {
      trMode = ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL;
    }
    final MarkingRemovalTRSimplifier alphaRemover =
      new MarkingRemovalTRSimplifier();
    alphaRemover.setSimplificationListener(markingListener);
    chain.add(alphaRemover);
    final OmegaRemovalTRSimplifier omegaRemover =
      new OmegaRemovalTRSimplifier();
    omegaRemover.setSimplificationListener(omegaRemovalListener);
    chain.add(omegaRemover);
    if (hasConfiguredPreconditionMarking) {
      final GNBCoreachabilityTRSimplifier nonCoreachableRemover =
        new GNBCoreachabilityTRSimplifier();
      nonCoreachableRemover.setSimplificationListener(partitioningListener);
      chain.add(nonCoreachableRemover);
    }
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    silentInRemover.setSimplificationListener(partitioningListener);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setSimplificationListener(partitioningListener);
    chain.add(silentOutRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode(trMode);
    bisimulator.setSimplificationListener(partitioningListener);
    chain.add(bisimulator);
    final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
      new NonAlphaDeterminisationTRSimplifier();
    nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
    nonAlphaDeterminiser.setSimplificationListener(partitioningListener);
    chain.add(nonAlphaDeterminiser);
    if (hasConfiguredPreconditionMarking) {
      final AlphaDeterminisationTRSimplifier alphaDeterminiser =
        new AlphaDeterminisationTRSimplifier();
      alphaDeterminiser.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      alphaDeterminiser.setSimplificationListener(partitioningListener);
      chain.add(alphaDeterminiser);
    }
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    saturator.setSimplificationListener(markingListener);
    chain.add(saturator);

    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_Equivalence);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_TransitionRemovalMode);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_MarkingMode);

    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_PropositionMask);

    return chain;
  }

  public static ChainTRSimplifier createProjectionChain()
  {
    final ChainTRSimplifier startedAbstractionChain = startAbstractionChain(null);
    return createProjectionChain(startedAbstractionChain, 0, 0, false, null);
  }

  public static ChainTRSimplifier createProjectionChain
    (final ChainTRSimplifier startedAbstractionChain,
     final int stateLimit,
     final int transitionLimit,
     final boolean selfloopOnlyEventsUsed,
     final TRSimplificationListener listener)
  {
    final ChainTRSimplifier chain = startedAbstractionChain;
    final TransitionRelationSimplifier tauEliminator =
      new TauEliminationTRSimplifier();
    tauEliminator.setSimplificationListener(listener);
    chain.add(tauEliminator);
    final SubsetConstructionTRSimplifier subset =
      new SubsetConstructionTRSimplifier();
    chain.add(subset);
    subset.setStateLimit(stateLimit);
    subset.setTransitionLimit(transitionLimit);
    subset.setSimplificationListener(listener);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    final ObservationEquivalenceTRSimplifier.Equivalence eq =
      selfloopOnlyEventsUsed ?
      ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION :
      ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE;
    bisimulator.setEquivalence(eq);
    bisimulator.setTransitionLimit(transitionLimit);
    bisimulator.setSimplificationListener(listener);
    chain.add(bisimulator);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);

    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_Equivalence);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_TransitionRemovalMode);
    chain.blacklistOption(TRSimplifierFactory.OPTION_ObservationEquivalence_MarkingMode);
    chain.blacklistOption(TRSimplifierFactory.OPTION_AbstractMarking_DefaultMarkingID);
    chain.blacklistOption(TRSimplifierFactory.OPTION_AbstractMarking_PreconditionMarkingID);

    return chain;
  }



  static final int DEFAULT_MARKING = 0;
  static final int PRECONDITION_MARKING = 1;

}
