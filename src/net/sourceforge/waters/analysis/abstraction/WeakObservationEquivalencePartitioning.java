//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * @author Robi Malik
 */

public class WeakObservationEquivalencePartitioning
{

  //#########################################################################
  //# Constructor
  WeakObservationEquivalencePartitioning(final ListBufferTransitionRelation rel,
                                         final TRPartition partition,
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
    final int numClasses = mPartition.getNumberOfClasses();
    mClassMap = new int[numStates];
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.getStates(c);
      if (clazz != null) {
        for (final int state : clazz) {
          mClassMap[state] = c;
        }
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
    final int numClasses = mPartition.getNumberOfClasses();
    mTauVictims = new TLongHashSet();
    mTauClosure =
      mTransitionRelation.createSuccessorsTauClosure(mTransitionLimit);
    final TransitionIterator tauClosureIter = mTauClosure.createIterator();
    final TIntIntHashMap succCounts = new TIntIntHashMap();
    final TIntHashSet visitedClasses = new TIntHashSet();
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.getStates(c);
      final int csize = clazz == null ? 0 : clazz.length;
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
  throws OverflowException
  {
    final int numEvents = mTransitionRelation.getNumberOfProperEvents();
    mAdditionalTransitions = new PreTransitionBuffer(numEvents);
    final int tau = EventEncoding.TAU;
    final TIntStack tauStack = new TIntArrayStack();
    final TransitionIterator tauEventIter =
      mTransitionRelation.createAnyReadOnlyIterator();
    final TIntHashSet tauSuccessors = new TIntHashSet();
    final TIntHashSet succClasses = new TIntHashSet();
    final TIntStack eventStack = new TIntArrayStack();
    final TransitionIterator eventIter =
      mTransitionRelation.createAnyReadOnlyIterator();
    final TIntHashSet eventSuccessors = new TIntHashSet();
    final TransitionIterator tauStarIter = mTauClosure.createIterator();
    final TransitionIterator closureIter =
      mTauClosure.createFullEventClosureIterator(-1);
    final int numClasses = mPartition.getNumberOfClasses();
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.getStates(c);
      if (clazz == null) {
        continue;
      }
      long markings = 0;
      for (final int root : clazz) {
        tauSuccessors.add(root);
        tauStack.push(root);
        while (tauStack.size() > 0) {
          final int src = tauStack.pop();
          final int srccls = mClassMap[src];
          succClasses.add(srccls);
          markings |= mTransitionRelation.getAllMarkings(src);
          eventIter.reset(src, EventEncoding.TAU);
          while (eventIter.advance()) {
            final int target = eventIter.getCurrentTargetState();
            final int targetcls = mClassMap[target];
            if (!isTauVictim(srccls, targetcls) &&
                tauSuccessors.add(target)) {
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
          if (!succClasses.contains(targetcls)) {
            if (init) {
              mTransitionRelation.setInitial(target, true);
            }
            final long missing =
              mTransitionRelation.getAllMarkings(target) & markings;
            mTransitionRelation.addMarkings(root, missing);
          }
        }
      }
      succClasses.clear();

      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        for (final int root : clazz) {
          tauSuccessors.add(root);
          tauStack.push(root);
          while (tauStack.size() > 0) {
            final int tausucc1 = tauStack.pop();
            final int taucls1 = mClassMap[tausucc1];
            eventIter.reset(tausucc1, EventEncoding.TAU);
            while (eventIter.advance()) {
              final int tautarget1 = eventIter.getCurrentTargetState();
              final int tautargetcls1 = mClassMap[tautarget1];
              if (!isTauVictim(taucls1, tautargetcls1) &&
                  tauSuccessors.add(tautarget1)) {
                tauStack.push(tautarget1);
              }
            }
            eventIter.reset(tausucc1, event);
            while (eventIter.advance()) {
              final int etarget = eventIter.getCurrentTargetState();
              if (eventSuccessors.add(etarget)) {
                eventStack.push(etarget);
                int ecls = mClassMap[etarget];
                succClasses.add(ecls);
                do {
                  final int estate = eventStack.pop();
                  ecls = mClassMap[estate];
                  tauEventIter.reset(estate, tau);
                  while (tauEventIter.advance()) {
                    final int tausucc2 = tauEventIter.getCurrentTargetState();
                    final int taucls2 = mClassMap[tausucc2];
                    if (!isTauVictim(ecls, taucls2) &&
                        eventSuccessors.add(tausucc2)) {
                      eventStack.push(tausucc2);
                      succClasses.add(taucls2);
                    }
                  }
                } while (eventStack.size() > 0);
              }
            }
          }
          tauSuccessors.clear();
          eventSuccessors.clear();
        }
        for (final int root : clazz) {
          closureIter.reset(root, event);
          while (closureIter.advance()) {
            final int target = closureIter.getCurrentTargetState();
            final int tclass = mClassMap[target];
            if (succClasses.add(tclass)) {
              mAdditionalTransitions.addTransition(c, event, tclass);
            }
          }
        }
        succClasses.clear();
      }
    }
  }

  private void deleteTauVictims()
  {
    final int tau = EventEncoding.TAU;
    final int numClasses = mPartition.getNumberOfClasses();
    final TransitionIterator iter =
      mTransitionRelation.createSuccessorsModifyingIterator();
    for (int c = 0; c < numClasses; c++) {
      final int[] clazz = mPartition.getStates(c);
      if (clazz != null && clazz.length > 1) {
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
  private final TRPartition mPartition;
  private final long mPropositionMask;
  private final int mTransitionLimit;

  private int[] mClassMap;
  private TauClosure mTauClosure;
  private TLongHashSet mTauVictims;
  private PreTransitionBuffer mAdditionalTransitions;

}









