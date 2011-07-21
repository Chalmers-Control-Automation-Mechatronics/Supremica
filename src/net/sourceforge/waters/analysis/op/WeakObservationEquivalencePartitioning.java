//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   WeakObservationEquivalencePartitioning
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TLongHashSet;

import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * @author Robi Malik
 */

public class WeakObservationEquivalencePartitioning
{

  //#########################################################################
  //# Constructor
  WeakObservationEquivalencePartitioning(final ListBufferTransitionRelation rel,
                                         final List<int[]> partition,
                                         final long propositionMask,
                                         final int limit)
  {
    mTransitionRelation = rel;
    mPartition = partition;
    mPropositionMask = propositionMask;
    mTransitionLimit = limit;
  }

  //#########################################################################
  //# Invocation
  void applyPartition() throws AnalysisException
  {
    mTransitionRelation.reconfigure
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    establishClassMap();
    collectTauVictims();
    if (mTauVictims.isEmpty()) {
      mTransitionRelation.merge(mPartition);
    } else {
      collectionAdditionalTransitions();
      mTauClosure = null;
      mClassMap = null;
      mTransitionRelation.merge(mPartition);
      deleteTauVictims();
      mTauVictims = null;
      mAdditionalTransitions.addOutgoingTransitions(mTransitionRelation);
    }
  }


  //#########################################################################
  //# Algorithm
  private void establishClassMap()
  {
    final int numStates = mTransitionRelation.getNumberOfStates();
    final int numClasses = mPartition.size();
    mClassMap = new int[numStates];
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.get(c);
      for (final int state : clazz) {
        mClassMap[state] = c;
      }
    }
  }

  /**
   * Collects tau-victims. A tau-victim is a tau-transition that would be
   * present in a standard automaton quotient, but which would only be correct
   * when partitioning for observation equivalence.
   */
  private void collectTauVictims()
  {
    final int numClasses = mPartition.size();
    mTauVictims = new TLongHashSet();
    mTauClosure =
      mTransitionRelation.createSuccessorsTauClosure(mTransitionLimit);
    final TransitionIterator tauClosureIter = mTauClosure.createIterator();
    final TIntIntHashMap succCounts = new TIntIntHashMap();
    final TIntHashSet visitedClasses = new TIntHashSet();
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.get(c);
      final int csize = clazz.length;
      if (csize > 1) {
        visitedClasses.add(c);
        tauClosureIter.resetState(clazz[0]);
        tauClosureIter.advance();
        while (tauClosureIter.advance()) {
          final int tausucc = tauClosureIter.getCurrentTargetState();
          final int taucls = mClassMap[tausucc];
          if (visitedClasses.add(taucls)) {
            succCounts.put(taucls, 1);
          }
        }
        visitedClasses.clear();
        for (int s = 1; s < csize; s++) {
          visitedClasses.add(c);
          tauClosureIter.resetState(clazz[s]);
          tauClosureIter.advance();
          while (tauClosureIter.advance()) {
            final int tausucc = tauClosureIter.getCurrentTargetState();
            final int taucls = mClassMap[tausucc];
            if (visitedClasses.add(taucls)) {
              succCounts.adjustOrPutValue(taucls, 1, 1);
            }
          }
          visitedClasses.clear();
        }
        final TIntIntIterator countIter = succCounts.iterator();
        while (countIter.hasNext()) {
          countIter.advance();
          final int taucls = countIter.key();
          if (countIter.value() != csize) {
            addTauVictim(c, taucls);
          }
        }
        succCounts.clear();
      }
    }
  }

  private void collectionAdditionalTransitions()
  {
    final int numEvents = mTransitionRelation.getNumberOfProperEvents();
    mAdditionalTransitions = new PreTransitionBuffer(numEvents);
    final int tau = EventEncoding.TAU;
    final TIntStack tauStack = new TIntStack();
    final TransitionIterator tauEventIter =
      mTransitionRelation.createAnyReadOnlyIterator();
    final TIntHashSet tauSuccessors = new TIntHashSet();
    final TIntHashSet tauClasses = new TIntHashSet();
    final TIntStack eventStack = new TIntStack();
    final TransitionIterator eventIter =
      mTransitionRelation.createAnyReadOnlyIterator();
    final TIntHashSet eventSuccessors = new TIntHashSet();
    final TransitionIterator tauStarIter = mTauClosure.createIterator();
    final TransitionIterator closureIter =
      mTauClosure.createFullEventClosureIterator(-1);
    final TIntHashSet eventSuccessorClasses = new TIntHashSet();
    final int numClasses = mPartition.size();
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.get(c);
      long markings = 0;
      for (final int root : clazz) {
        tauSuccessors.add(root);
        tauStack.push(root);
        while (tauStack.size() > 0) {
          final int src = tauStack.pop();
          final int srccls = mClassMap[src];
          tauClasses.add(srccls);
          markings |= mTransitionRelation.getAllMarkings(src);
          eventIter.reset(src, EventEncoding.TAU);
          while (eventIter.advance()) {
            final int target = eventIter.getCurrentTargetState();
            final int targetcls = mClassMap[target];
            if (!isTauVictim(srccls, targetcls) && tauSuccessors.add(target)) {
              tauStack.push(target);
            }
          }
        }
        tauSuccessors.clear();
      }
      markings = ~markings & mPropositionMask;
      for (final int root : clazz) {
        final boolean init = mTransitionRelation.isInitial(root);
        tauStarIter.resetState(root);
        tauStarIter.advance();
        while (tauStarIter.advance()) {
          final int target = tauStarIter.getCurrentTargetState();
          final int targetcls = mClassMap[target];
          if (!tauClasses.contains(targetcls)) {
            if (init) {
              mTransitionRelation.setInitial(target, true);
            }
            final long missing =
              mTransitionRelation.getAllMarkings(target) & markings;
            mTransitionRelation.addMarkings(root, missing);
          }
        }
      }
      tauClasses.clear();
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        for (final int root : clazz) {
          final boolean init = mTransitionRelation.isInitial(root);
          long nonTauVictimsMarkings =
            mTransitionRelation.getAllMarkings(root) & mPropositionMask;
          long tauVictimsMarkings = 0;
          tauStarIter.resetState(root);
          tauStarIter.advance();
          while (tauStarIter.advance()) {
            final int tausucc = tauStarIter.getCurrentTargetState();
            final int taucls = mClassMap[tausucc];
            markings =
              mTransitionRelation.getAllMarkings(tausucc) & mPropositionMask;
            if (isTauVictim(c, taucls)) {
              if (init) {
                mTransitionRelation.setInitial(tausucc, true);
              }
              tauVictimsMarkings =
                mTransitionRelation.mergeMarkings(tauVictimsMarkings, markings);
            } else {
              nonTauVictimsMarkings =
                mTransitionRelation.mergeMarkings(nonTauVictimsMarkings,
                                                  markings);
            }
          }
          tauVictimsMarkings &= ~nonTauVictimsMarkings;
          mTransitionRelation.addMarkings(root, tauVictimsMarkings);
          if (tauSuccessors.add(root)) {
            tauStack.push(root);
            do {
              final int taustate = tauStack.pop();
              eventIter.reset(taustate, event);
              while (eventIter.advance()) {
                final int etarget = eventIter.getCurrentTargetState();
                if (eventSuccessors.add(etarget)) {
                  eventStack.push(etarget);
                  final int eclass = mClassMap[etarget];
                  eventSuccessorClasses.add(eclass);
                  do {
                    final int state = eventStack.pop();
                    tauEventIter.reset(state, tau);
                    while (tauEventIter.advance()) {
                      final int tausucc = tauEventIter.getCurrentTargetState();
                      final int tauclass = mClassMap[tausucc];
                      if (!isTauVictim(eclass, tauclass) &&
                          eventSuccessors.add(tausucc)) {
                        eventStack.push(tausucc);
                        eventSuccessorClasses.add(tauclass);
                      }
                    }
                  } while (eventStack.size() > 0);
                }
              }
              tauEventIter.reset(taustate, tau);
              while (tauEventIter.advance()) {
                final int target = tauEventIter.getCurrentTargetState();
                final int tclass = mClassMap[target];
                if (!isTauVictim(c, tclass) && tauSuccessors.add(target)) {
                  tauStack.push(target);
                }
              }
            } while (tauStack.size() > 0);
          }
        }
        tauSuccessors.clear();
        eventSuccessors.clear();
        for (final int root : clazz) {
          closureIter.reset(root, event);
          while (closureIter.advance()) {
            final int target = closureIter.getCurrentTargetState();
            final int tclass = mClassMap[target];
            if (eventSuccessorClasses.add(tclass)) {
              mAdditionalTransitions.addTransition(c, event, tclass);
            }
          }
        }
        eventSuccessorClasses.clear();
      }
    }
  }

  private void deleteTauVictims()
  {
    final int tau = EventEncoding.TAU;
    final int numClasses = mPartition.size();
    final TransitionIterator iter =
      mTransitionRelation.createSuccessorsModifyingIterator();
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.get(c);
      if (clazz.length > 1) {
        iter.reset(c, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          if (isTauVictim(c, target)) {
            iter.remove();
          }
        }
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean addTauVictim(final int source, final int target)
  {
    final long key = source | (long) target << 32;
    return mTauVictims.add(key);
  }

  private boolean isTauVictim(final int source, final int target)
  {
    final long key = source | (long) target << 32;
    return mTauVictims.contains(key);
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final List<int[]> mPartition;
  private final long mPropositionMask;
  private final int mTransitionLimit;

  private int[] mClassMap;
  private TauClosure mTauClosure;
  private TLongHashSet mTauVictims;
  private PreTransitionBuffer mAdditionalTransitions;

}
