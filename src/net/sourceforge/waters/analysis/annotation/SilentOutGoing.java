//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.procedure.TIntProcedure;

import java.util.Arrays;
import java.util.Set;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class SilentOutGoing
{
  private final TransitionRelation mTransitionRelation;
  private final int mTau;
  private final TIntHashSet[][] mPossibleSuccs;
  private final TIntHashSet[] mPossibleTauStates;
  private final Set<TIntHashSet>[] mAnns;

  public static int STATESREMOVED = 0;
  public static int TIME = 0;


  @SuppressWarnings("unused")
  private long key(int s1, final int s2)
  {
    long l = s1;
    s1 <<= 32;
    final long l2 = s2;
    l |= l2;
    return l;
  }

  public static void clearStats()
  {
    TIME = 0;
    STATESREMOVED = 0;
  }


  public static String stats()
  {
    return "SilentOutgoing: " +
            " States Removed" + STATESREMOVED + " TIME = " + TIME;
  }


  @SuppressWarnings("unchecked")
  public SilentOutGoing(final TransitionRelation transitionrelation, final int tau)
  {
    mTransitionRelation = transitionrelation;
    mPossibleSuccs =
      new TIntHashSet[mTransitionRelation.numberOfStates()]
                     [mTransitionRelation.numberOfEvents()];
    mTau = tau;
    mPossibleTauStates = new TIntHashSet[mTransitionRelation.numberOfStates()];
    mAnns = new Set[mTransitionRelation.numberOfStates()];
    for (int s = 0; s < mPossibleTauStates.length; s++) {
      mPossibleTauStates[s] = new TIntHashSet();
      mAnns[s] = new THashSet<TIntHashSet>();
    }
  }

  private void calculateAnns()
  {
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final TIntHashSet taus = mTransitionRelation.getSuccessors(s, mTau);
      if (taus == null || taus.isEmpty()) {
        mAnns[s].add(mTransitionRelation.getActiveEvents(s));
      } else {
        final int state = s;
        taus.forEach(new TIntProcedure() {
          public boolean execute(final int tausucc)
          {
            mAnns[state].add(mTransitionRelation.getActiveEvents(tausucc));
            return true;
          }
        });
      }
    }
  }

  private void calculatePossibleTauSuccs()
  {
    STATES:
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if (!mTransitionRelation.hasPredecessors(s)) {continue;}
      TIntHashSet possible = null;
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        if (e == mTau) {continue;}
        final TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
        if (succs == null) {continue;}
        final int[] succsarr = succs.toArray();
        for (int i = 0; i < succsarr.length; i++) {
          final int succ = succsarr[i];
          final TIntHashSet succpreds = mTransitionRelation.getPredecessors(succ, e);
          if (possible == null) {
            possible = new TIntHashSet(succpreds.toArray());
            possible.remove(s);
          } else {
            possible.retainAll(succpreds.toArray());
          }
          if (possible.isEmpty()) {continue STATES;}
        }
      }
      if (possible == null) {continue;}
      if (mTransitionRelation.isMarked(s)) {
        final int[] possiblearr = possible.toArray();
        for (int i = 0; i < possiblearr.length; i++) {
          final int poss = possiblearr[i];
          if (!mTransitionRelation.isMarked(poss)) {possible.remove(poss);}
        }
        if (possible.isEmpty()) {continue STATES;}
      }
      final int[] possiblearr = possible.toArray();
      CoversAnnoations:
      for (int i = 0; i < possiblearr.length; i++) {
        final int poss = possiblearr[i];
        Annotations:
        for (final TIntHashSet ann1 : mAnns[s]) {
          for (final TIntHashSet ann2 : mAnns[poss]) {
            if (ann1.containsAll(ann2.toArray())) {
              continue Annotations;
            }
          }
          possible.remove(poss); continue CoversAnnoations;
        }
      }
      if (possible.isEmpty()) {continue STATES;}
      for (int i = 0; i < possiblearr.length; i++) {
        final int poss = possiblearr[i];
        mPossibleTauStates[poss].add(s);
      }
    }
  }

  private boolean isCandidateState(final int s)
  {
    final TIntHashSet tausuccs = mTransitionRelation.getSuccessors(s, mTau);
    if (tausuccs == null) {return false;}
    final int[] tausarr = tausuccs.toArray();
    final TIntHashSet followonactive = new TIntHashSet();
    for (int i = 0; i < tausarr.length; i++) {
      final int tausucc = tausarr[i];
      followonactive.addAll(mTransitionRelation.getActiveEvents(tausucc).toArray());
    }
    followonactive.add(mTau);
    final TIntHashSet activeEvents = mTransitionRelation.getActiveEvents(s);
    /*for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      if (e == mTau) {continue;}
      TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
      if (succs == null) {continue;}
      if (succs.contains(s)) {return false;}
    }*/
    activeEvents.removeAll(followonactive.toArray());
    if (activeEvents.isEmpty()) {return true;}
    final int[] posstaus = mPossibleTauStates[s].toArray();
    for (int i = 0; i < posstaus.length; i++) {
      final int poss = posstaus[i];
      if (mTransitionRelation.getActiveEvents(poss).containsAll(activeEvents.toArray())) {
        return true;
      }
    }
    return false;
  }

  public void calculateCandidateTransitions(final int s)
  {
    final TIntHashSet taupreds = mTransitionRelation.getPredecessors(s, mTau);
    final int[] activeevents = mTransitionRelation.getActiveEvents(s).toArray();
    final int[] predsarr = taupreds.toArray();
    for (int i = 0; i < activeevents.length; i++) {
      final int e = activeevents[i];
      if (mTransitionRelation.isMarkingEvent(e)) {continue;}
      mPossibleSuccs[s][e] =
        new TIntHashSet(mTransitionRelation.getSuccessors(s, e).toArray());
    }
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      if (e == mTau) {continue;}
      if (mTransitionRelation.getPredecessors(s, e) != null &&
          !mTransitionRelation.getPredecessors(s, e).isEmpty()) {return;}
    }
    for (int i = 0; i < predsarr.length; i++) {
      final int pred = predsarr[i];
      for (int j = 0; j < activeevents.length; j++) {
        final int e = activeevents[j];
        if (mTransitionRelation.isMarkingEvent(e)) {continue;}
        mPossibleSuccs[s][e].retainAll(mTransitionRelation.getSuccessors(pred, e).toArray());
      }
    }
  }

  public void attemptToRemove(final int s)
  {
    final int[] tausuccs = mTransitionRelation.getSuccessors(s, mTau).toArray();
    Arrays.sort(tausuccs);
    final int[] activeevents = mTransitionRelation.getActiveEvents(s).toArray();
    final TIntHashSet[] tobeCovered = new TIntHashSet[activeevents.length];
    final TIntHashSet[][] tobeadded = new TIntHashSet[activeevents.length][tausuccs.length];
    boolean needextra = false;
    for (int j = 0; j < activeevents.length; j++) {
      final int e = activeevents[j];
      if (e == mTau) {continue;}
      if (mTransitionRelation.isMarkingEvent(e)) {continue;}
      tobeCovered[j] = new TIntHashSet(mTransitionRelation.getSuccessors(s, e).toArray());
      for (int i = 0; i < tausuccs.length; i++) {
        final int tausucc = tausuccs[i];
        tobeadded[j][i] = new TIntHashSet();
        final int[] arrtobecovered = tobeCovered[j].toArray();
        for (int k = 0; k < arrtobecovered.length; k++) {
          final int succ = arrtobecovered[k];
          final TIntHashSet psuccs = mPossibleSuccs[tausucc][e];
          if (psuccs == null) {continue;}
          if (psuccs.contains(succ)) {
            tobeadded[j][i].add(succ); tobeCovered[j].remove(succ);
          }
        }
      }
      if (tobeCovered[j].isEmpty()) {continue;}
      needextra = true;
      final int[] possarr = mPossibleTauStates[s].toArray();
      for (int i = 0; i < possarr.length; i++) {
        final int poss = possarr[i];
        final TIntHashSet posssuccs = mTransitionRelation.getSuccessors(poss, e);
        if (posssuccs == null ||
            !posssuccs.containsAll(tobeCovered[j].toArray())) {
          mPossibleTauStates[s].remove(poss);
        }
      }
      if (mPossibleTauStates[s].isEmpty()) {return;}
    }
    for (int j = 0; j < activeevents.length; j++) {
      final int e = activeevents[j];
      if (e == mTau) {continue;}
      if (mTransitionRelation.isMarkingEvent(e)) {continue;}
      for (int i = 0; i < tausuccs.length; i++) {
        final int tausucc = tausuccs[i];
        final int[] succs = tobeadded[j][i].toArray();
        for (int k = 0; k < succs.length; k++) {
          final int succ = succs[k];
          mTransitionRelation.addTransition(tausucc, e, succ);
        }
      }
    }
    for (int i = 0; i < tausuccs.length; i++) {
      final int tausucc = tausuccs[i];
      mTransitionRelation.addAllPredeccessors(s, tausucc);
    }
    if (needextra) {
      System.out.println("shouldn't");
      final int poss = mPossibleTauStates[s].toArray()[0];
      mTransitionRelation.addAllPredeccessors(s, poss);
    }
    mTransitionRelation.removeAllIncoming(s);
    mTransitionRelation.removeAllOutgoing(s);
    STATESREMOVED++;
  }

  public void run(final ProductDESProxyFactory factory)
  {
    TIME -= System.currentTimeMillis();
    System.out.println("start");
    calculateAnns();
    calculatePossibleTauSuccs();
    final TIntHashSet tausuccs = new TIntHashSet();
    final TIntHashSet candidates = new TIntHashSet();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      if (isCandidateState(state)) {
        candidates.add(state);
        tausuccs.addAll(mTransitionRelation.getSuccessors(state, mTau).toArray());
      }
    }
    System.out.println("candidates: " + candidates.size());
    tausuccs.forEach(new TIntProcedure() {
      public boolean execute(final int state)
      {
        calculateCandidateTransitions(state); return true;
      }
    });
    candidates.forEach(new TIntProcedure() {
      public boolean execute(final int state)
      {
        /*int rem = STATESREMOVED;
        AutomatonProxy bef = mTransitionRelation.getAutomaton(factory);*/
        attemptToRemove(state);
        /*if (STATESREMOVED != rem) {
          System.out.println("before");
          System.out.println(bef);
          System.out.println("after");
          System.out.println(mTransitionRelation.getAutomaton(factory));
        }*/
        return true;
      }
    });
    System.out.println("remmed: " + STATESREMOVED);
    TIME += System.currentTimeMillis();
  }
}
