//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   TRConflictEquivalenceAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
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
import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


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

class EFSMTRSimplifier
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
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    chain.add(markingRemover);
    final TauLoopRemovalTRSimplifier tauLoopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(tauLoopRemover);
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    chain.add(silentOutRemover);
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    incomingEquivalenceSimplifier.setTransitionLimit(limit);
    chain.add(incomingEquivalenceSimplifier);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
    (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
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

  List<ConstraintList> getSelfloopedUpdates()
  {
    return mSelfloopedUpdates;
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
  //# Invocation
  public EFSMTransitionRelation run(final EFSMTransitionRelation efsmTR,
                                    final EFSMVariableContext context)
    throws AnalysisException
  {
    try {
      System.err.println("Simplifying: " + efsmTR.getName() + " ...");
      System.err.println(efsmTR.getTransitionRelation().getNumberOfStates() + " states");
      final long start = System.currentTimeMillis();
      ListBufferTransitionRelation rel = efsmTR.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int numTrans = rel.getNumberOfTransitions();
      final int numMarkings = rel.getNumberOfMarkings();
      mSimplifier.setTransitionRelation(rel);
      mSelfloopedUpdates = new ArrayList<ConstraintList>();
      if (mSimplifier.run()) {
        rel = mSimplifier.getTransitionRelation();
        final int newNumReachableStates = rel.getNumberOfReachableStates();
        final int newNumStates = rel.getNumberOfStates();
        if (newNumReachableStates == numStates &&
            rel.getNumberOfTransitions() == numTrans &&
            rel.getNumberOfMarkings() == numMarkings) {
          return null;
        }
        final EFSMEventEncoding eventEncoding = efsmTR.getEventEncoding();
        final int numEvents = eventEncoding.size();
        int newNumEvents = 1;
        final boolean[] usedEvents = new boolean[numEvents];
        usedEvents[0] = true;
        final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
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
          for (int i=EventEncoding.NONTAU; i < numEvents; i++) {
            if (usedEvents[i]) {
              final ConstraintList update = eventEncoding.getUpdate(i);
              newEventEncoding.createEventId(update);
            } else if ((rel.getProperEventStatus(i) &
                        EventEncoding.STATUS_UNUSED) != 0){
              final ConstraintList update = eventEncoding.getUpdate(i);
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
        final ListBufferTransitionRelation newRel;
        if (newNumReachableStates == numStates && newNumEvents == numEvents) {
          newRel = rel;
        } else {
          newRel = new ListBufferTransitionRelation(rel.getName(),
                                                    rel.getKind(),
                                                    newNumEvents,
                                                    rel.getNumberOfPropositions(),
                                                    newNumReachableStates,
                                                    rel.getConfiguration());
          for (int s=0; s < newNumStates; s++) {
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
            final int newSource = stateEncoding == null ? source : stateEncoding[source];
            final int target = iter.getCurrentTargetState();
            final int newTarget = stateEncoding == null ? target : stateEncoding[target];
            final int event = iter.getCurrentEvent();
            final int newEvent;
            if (newNumEvents == numEvents) {
              newEvent = event;
            } else {
              final ConstraintList update = eventEncoding.getUpdate(event);
              newEvent = newEventEncoding.getEventId(update);
            }
            newRel.addTransition(newSource, newEvent, newTarget);
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
        final long stop = System.currentTimeMillis();
        final float difftime = 0.001f * (stop - start);
        @SuppressWarnings("resource")
        final Formatter formatter = new Formatter(System.err);
        formatter.format("%d states, %.3f seconds\n",
                         newRel.getNumberOfStates(), difftime);
        return newEFSMTR;
      } else {
        final long stop = System.currentTimeMillis();
        final float difftime = 0.001f * (stop - start);
        @SuppressWarnings("resource")
        final Formatter formatter = new Formatter(System.err);
        formatter.format("No change, %.3f seconds\n", difftime);
        return null;
      }
    } finally {
      mSimplifier.reset();
    }
  }


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;
  private final CompilerOperatorTable mOperatorTable;
  private List<ConstraintList> mSelfloopedUpdates;

}
