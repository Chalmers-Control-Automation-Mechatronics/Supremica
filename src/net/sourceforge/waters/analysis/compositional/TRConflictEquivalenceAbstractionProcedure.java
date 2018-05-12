//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;

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
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.OverflowException;
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
                                              final Collection<EventProxy> local,
                                              final Candidate candidate)
    throws OverflowException
  {
    final EventEncoding enc = super.createEventEncoding(events, local, candidate);
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
    final TRPartition partition = simplifier.getResultPartition();
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
