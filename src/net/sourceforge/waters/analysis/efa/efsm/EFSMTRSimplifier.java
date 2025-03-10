//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
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
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>An abstraction procedure based on a transition relation simplifier
 * ({@link ChainTRSimplifier}) used by the EFSM conflict checker
 * ({@link EFSMConflictChecker}).</P>
 *
 * @author Robi Malik
 */

class EFSMTRSimplifier extends AbstractEFSMAlgorithm
{

  //#########################################################################
  //# Factory Methods
  public static EFSMTRSimplifier createObservationEquivalenceProcedure
      (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final int limit,
       final CompilerOperatorTable op)
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
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    return new EFSMTRSimplifier(chain, op);
  }

  public static EFSMTRSimplifier
    createGeneralisedNonblockingProcedure
      (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final int limit,
       final CompilerOperatorTable op)
  {
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
    return new EFSMTRSimplifier(chain, op);
  }


  public static EFSMTRSimplifier
    createStandardNonblockingProcedure
      (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
       final int limit,
       final CompilerOperatorTable op)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
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
    chain.add(incomingEquivalenceSimplifier);
    final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
      new LimitedCertainConflictsTRSimplifier();
    chain.add(certainConflictsRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
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
    return new EFSMTRSimplifier(chain, op);
  }


  //#########################################################################
  //# Constructor
  /**
   * Creates a new conflict equivalence abstraction procedure.
   * @param simplifier    The transition relation simplifier implementing
   *                      the abstraction.
   */
  private EFSMTRSimplifier(final TransitionRelationSimplifier simplifier,
                           final CompilerOperatorTable op)
  {
    mSimplifier = simplifier;
    mOperatorTable = op;
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
  //# Overrides for net.sourceforge.waters.analysis.efa.efsm.AbstractEFSMAlgorithm
  @Override
  List<ConstraintList> getSelfloopedUpdates()
  {
    return mSelfloopedUpdates;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort(final AbortRequester sender)
  {
    super.requestAbort(sender);
    mSimplifier.requestAbort(sender);
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mSimplifier.resetAbort();
  }


  //#########################################################################
  //# Invocation
  public EFSMTransitionRelation run(final EFSMTransitionRelation efsmTR,
                                    final EFSMVariableContext context)
    throws AnalysisException
  {
    try {
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        logger.debug("Simplifying: {} ...", efsmTR.getName());
        logger.debug("{} states",
                     efsmTR.getTransitionRelation().getNumberOfStates());
      }
      final long start = System.currentTimeMillis();
      ListBufferTransitionRelation rel = efsmTR.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int numTrans = rel.getNumberOfTransitions();
      final int numMarkings = rel.getNumberOfMarkings(false);
      mSimplifier.setTransitionRelation(rel);
      final int prop = rel.isPropositionUsed(0) ? 0 : -1;
      mSimplifier.setDefaultMarkingID(prop);
      mSelfloopedUpdates = new ArrayList<ConstraintList>();
      if (mSimplifier.run()) {
        rel = mSimplifier.getTransitionRelation();
        final int newNumReachableStates = rel.getNumberOfReachableStates();
        final int newNumStates = rel.getNumberOfStates();
        if (newNumReachableStates == numStates &&
            rel.getNumberOfTransitions() == numTrans &&
            rel.getNumberOfMarkings(false) == numMarkings) {
          return null;
        }
        final int newProp = rel.isPropositionUsed(0) ? 0 : -1;
        final EFSMEventEncoding eventEncoding = efsmTR.getEventEncoding();
        final int numEvents = eventEncoding.size();
        int newNumEvents = 1;
        final boolean[] usedEvents = new boolean[numEvents];
        usedEvents[0] = true;
        final TransitionIterator iter =
          rel.createAllTransitionsReadOnlyIterator();
        while (iter.advance()) {
          final int currentEvent = iter.getCurrentEvent();
          if (!usedEvents[currentEvent]) {
            usedEvents[currentEvent] = true;
            newNumEvents++;
          }
        }
        final EFSMEventEncoding newEventEncoding;
        if (newNumEvents == numEvents) {
          newEventEncoding = eventEncoding;
        } else {
          newEventEncoding = new EFSMEventEncoding(newNumEvents);
          for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
            if (usedEvents[e]) {
              final ConstraintList update = eventEncoding.getUpdate(e);
              newEventEncoding.createEventId(update);
            } else if ((rel.getProperEventStatus(e) &
                        EventStatus.STATUS_UNUSED) != 0){
              final ConstraintList update = eventEncoding.getUpdate(e);
              mSelfloopedUpdates.add(update);
            }
          }
        }
        final int[] stateEncoding;
        if (newNumReachableStates == numStates) {
          stateEncoding = null;
        } else {
          stateEncoding = new int[newNumStates];
          Arrays.fill(stateEncoding, -1);
          int newCode = 0;
          for (int s=0; s < newNumStates; s++) {
            if (rel.isReachable(s)) {
              stateEncoding[s] = newCode;
              newCode++;
            }
          }
        }
        checkAbort();
        final ListBufferTransitionRelation newRel;
        if (newNumReachableStates == numStates && newNumEvents == numEvents) {
          newRel = rel;
        } else {
          final int newNumProps = newProp < 0 ? 0 : 1;
          newRel = new ListBufferTransitionRelation(rel.getName(),
                                                    rel.getKind(),
                                                    newNumEvents,
                                                    newNumProps,
                                                    newNumReachableStates,
                                                    rel.getConfiguration());
          for (int s = 0; s < newNumStates; s++) {
            final int newCode = stateEncoding == null ? s : stateEncoding[s];
            if (newCode >= 0) {
              if (rel.isInitial(s)) {
                newRel.setInitial(newCode, true);
              }
              if (rel.getNumberOfPropositions() > 0) {
                if (rel.isMarked(s, 0)) {
                  newRel.setMarked(newCode, 0, true);
                }
              }
            }
          }
          iter.reset();
          while (iter.advance()) {
            final int source = iter.getCurrentSourceState();
            final int newSource =
              stateEncoding == null ? source : stateEncoding[source];
            final int target = iter.getCurrentTargetState();
            final int newTarget =
              stateEncoding == null ? target : stateEncoding[target];
            final int event = iter.getCurrentEvent();
            final int newEvent;
            if (newNumEvents == numEvents) {
              newEvent = event;
            } else {
              final ConstraintList update = eventEncoding.getUpdate(event);
              newEvent = newEventEncoding.getEventId(update);
            }
            newRel.addTransition(newSource, newEvent, newTarget);
            checkAbort();
          }
        }
        Collection<EFSMVariable> newVariables;
        if (newNumEvents == numEvents) {
          newVariables = efsmTR.getVariables();
        } else {
          newVariables = new THashSet<EFSMVariable>(efsmTR.getVariables().size());
          final EFSMVariableCollector variableCollector =
            new EFSMVariableCollector (mOperatorTable, context);
          variableCollector.collectAllVariables(newEventEncoding, newVariables);
        }
        final EFSMTransitionRelation newEFSMTR =
          new EFSMTransitionRelation(newRel, newEventEncoding, newVariables);
        if (logger.isDebugEnabled()) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          final String msg = String.format("%d states, %.3f seconds",
                                           newRel.getNumberOfStates(),
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


  //#########################################################################
  //# Logging
  @Override
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return LogManager.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;
  private final CompilerOperatorTable mOperatorTable;
  private List<ConstraintList> mSelfloopedUpdates;

}
