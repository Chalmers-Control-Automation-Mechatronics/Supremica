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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class RemoveAnnotations
{
  private final TransitionRelation mTransitionRelation;
  private final Set<TIntHashSet> mStates;
  public static int REMMEDALLANNOTATIONS = 0;
  public static int ANNOTATIONSREMOVED = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    REMMEDALLANNOTATIONS = 0;
    ANNOTATIONSREMOVED = 0;
    TIME = 0;

  }

  public static String stats()
  {
    return "REMOVEANNOTATIONS: ALLANNSREMOVED = " + REMMEDALLANNOTATIONS + " Annotations Removed = " + ANNOTATIONSREMOVED +
            " TIME = " + TIME;
  }

  public RemoveAnnotations(final TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mStates = new THashSet<TIntHashSet>();
  }

  public void addCoveredAnnotations(final Set<TIntHashSet> coverers,
                                    final Set<TIntHashSet> tobecovered,
                                    final Set<TIntHashSet> covered)
  {
    final Iterator<TIntHashSet> it = tobecovered.iterator();
    MainLoop:
    while (it.hasNext()) {
      final TIntHashSet tocov = it.next();
      final int[] tocovarr = tocov.toArray();
      for (final TIntHashSet coverer : coverers) {
        if (coverer.size() >= tocov.size()) {
          coverer.containsAll(tocovarr);
          it.remove();
          covered.add(tocov);
          continue MainLoop;
        }
      }
    }
  }

  public void run()
  {
    System.out.println("begin rem anns");
    final List<TIntHashSet> todo = new ArrayList<TIntHashSet>();
    final TIntHashSet initial = new TIntHashSet();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if (mTransitionRelation.isInitial(s)) {initial.add(s);}
    }
    todo.add(initial);
    mStates.add(initial);
    while (!todo.isEmpty()) {
      final TIntHashSet tistates = todo.remove(todo.size() - 1);
      final int[] states = tistates.toArray();
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet successor = new TIntHashSet();
        for (int i = 0; i < states.length; i++) {
          final int state = states[i];
          final TIntHashSet succs = mTransitionRelation.getSuccessors(state, e);
          if (succs == null) {continue;}
          successor.addAll(succs.toArray());
        }
        if (successor.isEmpty()) {continue;}
        if (mStates.add(successor)) {
          todo.add(successor);
          if (mStates.size() > 20000) {
            return;
          }
        }
      }
    }
    System.out.println("rem anns");
    final List<Set<TIntHashSet>> occurswith =
      new ArrayList<Set<TIntHashSet>>(mTransitionRelation.numberOfStates());
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if (mTransitionRelation.getAnnotations2(s)
                             .contains(mTransitionRelation.getActiveEvents(s))) {
        occurswith.add(new THashSet<TIntHashSet>());
      } else {
        occurswith.add(null);
      }
    }
    for (final TIntHashSet sub : mStates) {
      final int[] subarray = sub.toArray();
      for (int i = 0; i < subarray.length; i++) {
        final int state = subarray[i];
        if (sub.size() == 1) {
          occurswith.set(state, null);
        } else {
          if (occurswith.get(state) != null) {occurswith.get(state).add(sub);}
        }
      }
    }
    MainLoop:
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if (!mTransitionRelation.hasPredecessors(s)) {continue;}
      if (occurswith.get(s) == null || occurswith.get(s).isEmpty()) {continue;}
      Set<TIntHashSet> coverann = mTransitionRelation.getAnnotation(s);
      if (coverann == null) {continue;}
      for (final TIntHashSet ostates : occurswith.get(s)) {
        final Set<TIntHashSet> tcoverann = new THashSet<TIntHashSet>();
        final int[] statesarr = ostates.toArray();
        for (int i = 0 ; i < statesarr.length; i++) {
          final int state = statesarr[i];
          if (state == s) {continue;}
          final Set<TIntHashSet> oanns = mTransitionRelation.getAnnotations2(state);
          addCoveredAnnotations(oanns, coverann, tcoverann);
        }
        coverann = tcoverann;
        if (coverann.isEmpty()) {continue MainLoop;}
      }
      System.out.println("remmed :" + coverann.size());
      ANNOTATIONSREMOVED += coverann.size();
      if (mTransitionRelation.getAnnotation(s).size() == coverann.size()) {
        REMMEDALLANNOTATIONS++;
      }
      mTransitionRelation.removeAnnotations(s, coverann);
    }
  }
}
