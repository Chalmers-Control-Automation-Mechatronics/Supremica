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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.annotation.ListBufferTauLoopRemoval;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Simon Ware
 */

@SuppressWarnings("deprecation")
public class Canonize
{
  // #######################################################################
  // # Constructor
  public Canonize(final ListBufferTransitionRelation automaton,
                  final EventEncoding encoding, final int marking, final int alpha, final int cont)
  {
    mAutomaton = automaton;
    mEncoding = encoding;
    mAlpha = alpha;
    mMarking = marking;
    mCont = cont;
  }

  public ListBufferTransitionRelation run(final ProductDESProxyFactory factory)
    throws AnalysisException
  {
    final ListBufferTauLoopRemoval lbtr = new ListBufferTauLoopRemoval(mAutomaton);
    lbtr.run();
    final TIntHashSet alphas = new TIntHashSet();
    for (int s = 0; s < mAutomaton.getNumberOfStates(); s++) {
      if (mAutomaton.isMarked(s, mAlpha)) {alphas.add(s);}
    }
    Determinizer determinizer = new Determinizer(mAutomaton, mEncoding,
                                                 mMarking);
    determinizer.setNodeLimit(100000);
    determinizer.run(alphas.toArray());
    final ListBufferTransitionRelation lower = determinizer.getAutomaton();
    FindBlockingStates fbs = new FindBlockingStates(lower, mMarking);
    TIntHashSet blocking = fbs.getBlockingStates();
    int[] arrblocking = blocking.toArray();
    for (int i = 0; i < arrblocking.length; i++) {
      lower.removeIncomingTransitions(arrblocking[i]);
      lower.removeOutgoingTransitions(arrblocking[i]);
    }
    lower.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final List<int[]> partitions = null; //oetrs.getResultPartition();
    // this tells me what the alpha states in the original became when determinizing
    final TIntIntHashMap initialtoinitial = determinizer.getInitialToInitial();
    if (partitions != null) {
      // I need to update the initials now
      final TIntIntHashMap otherway = new TIntIntHashMap();
      initialtoinitial.forEachEntry(new TIntIntProcedure() {
        @Override
        public boolean execute(final int k, final int v)
        {
          otherway.put(v, k);
          return true;
        }
      });
      for (int s = 0; s < partitions.size(); s++) {
        final int[] arr = partitions.get(s);
        for (int i = 0; i < arr.length; i++) {
          final int state = arr[i];
          if (!otherway.containsKey(state)) {continue;}
          final int initial = otherway.get(state);
          initialtoinitial.put(initial, s);
        }
      }
    }
    fbs = new FindBlockingStates(mAutomaton, mAlpha);
    blocking = fbs.getBlockingStates();
    arrblocking = blocking.toArray();
    for (int i = 0; i < arrblocking.length; i++) {
      mAutomaton.removeIncomingTransitions(arrblocking[i]);
      mAutomaton.removeOutgoingTransitions(arrblocking[i]);
    }
    determinizer = new Determinizer(mAutomaton, mEncoding,
                                    mAlpha);
    determinizer.setNodeLimit(100000);
    determinizer.run();
    final ListBufferTransitionRelation upper = determinizer.getAutomaton();
    fbs = new FindBlockingStates(upper, mAlpha);
    blocking = fbs.getBlockingStates();
    arrblocking = blocking.toArray();
    for (int i = 0; i < arrblocking.length; i++) {
      upper.removeIncomingTransitions(arrblocking[i]);
      upper.removeOutgoingTransitions(arrblocking[i]);
    }
    final TObjectIntHashMap<TIntHashSet> statesets = determinizer.getSetStateMap();
    final THashMap<TIntHashSet, TIntArrayList> alphaset = new THashMap<TIntHashSet, TIntArrayList>();
    final LessMarkedFullCache lm = new LessMarkedFullCache(lower, mMarking, new TIntHashSet());
    final TIntArrayList nonAlphas = new TIntArrayList();
    lower.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    statesets.forEachEntry(new TObjectIntProcedure<TIntHashSet>(){
      @Override
      public boolean execute(final TIntHashSet set, final int state) {
        final int[] arr = set.toArray();
        boolean hasalpha = false;
        OUTER:
        for (int i = 0; i < arr.length; i++) {
          int s1 = arr[i]; if (s1 == -1) {continue;}
          if (!alphas.contains(s1)) {arr[i] = -1; continue;}
          else {hasalpha = true;}
          s1 = initialtoinitial.get(s1);
          INNER:
          for (int j = i + 1; j < arr.length; j++) {
            int s2 = arr[j]; if (s2 == -1) {continue;}
            if (!alphas.contains(s2)) {continue;}
            s2 = initialtoinitial.get(s2);
            if (s1 == s2) {
              arr[j] = -1;
              continue INNER;
            }
            final int lesser = lm.run(s1, s2);
            if (lesser == s1) {
              arr[j] = -1;
              continue INNER;
            }
            if (lesser == s2) {
              arr[i] = -1;
              continue OUTER;
            }
          }
        }
        for (int i = 0; i < arr.length; i++) {
          arr[i] = arr[i] != -1 ? initialtoinitial.get(arr[i]) : -1;
        }
        if (!hasalpha) {nonAlphas.add(state); return true;}
        final TIntHashSet alphas = new TIntHashSet(arr);
        alphas.remove(-1);
        TIntArrayList stateswith = alphaset.get(alphas);
        if (stateswith == null) {
          stateswith = new TIntArrayList();
          alphaset.put(alphas, stateswith);
        }
        stateswith.add(state);
        return true;
      }
    });
    // lm.outputstats();
    final List<int[]> partitions2 = new ArrayList<int[]>();
    if (!nonAlphas.isEmpty()) {partitions2.add(nonAlphas.toArray());}
    alphaset.forEachValue(new TObjectProcedure<TIntArrayList>() {
      @Override
      public boolean execute(final TIntArrayList set) {
        partitions2.add(set.toArray());
        return true;
      }
    });
    final int uppstates = upper.getNumberOfStates();
    upper.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final List<int[]> partitions3 = null; //oetrs.getResultPartition();
    if (partitions3 != null) {
      final int[] intmap = new int[uppstates];
      for (int p = 0; p < partitions3.size(); p++) {
        final int[] parr = partitions3.get(p);
        for (int i = 0; i < parr.length; i++) {
          final int s = parr[i];
          intmap[s] = p;
        }
      }
      alphaset.forEachValue(new TObjectProcedure<TIntArrayList>() {
        @Override
        public boolean execute(final TIntArrayList set) {
          for (int i = set.size() - 1; i >= 0; i--) {
            final int s = set.get(i);
            set.set(i, intmap[s]);
          }
          return true;
        }
      });
    }
    final int upperstates = upper.getNumberOfReachableStates();
    final int lowerstates = lower.getNumberOfReachableStates();
    final int states = upperstates + lowerstates + lowerstates;
    final ListBufferTransitionRelation canon =
      new ListBufferTransitionRelation("Canon:" + mAutomaton.getName(),
                                        mAutomaton.getKind(),
                                        mEncoding,
                                        states,
                                        ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    lower.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    upper.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    for (int s = 0; s < upperstates; s++) {
      if (upper.isInitial(s)) {canon.setInitial(s, true);}
      canon.setReachable(s, true);
      final TransitionIterator ti = upper.createSuccessorsReadOnlyIterator(s);
      while (ti.advance()) {
        canon.addTransition(s, ti.getCurrentEvent(), ti.getCurrentTargetState());
      }
    }
    for (int s = 0; s < lowerstates; s++) {
      final TransitionIterator ti = lower.createSuccessorsReadOnlyIterator(s);
      canon.setReachable(s + upperstates, true);
      canon.setInitial(s + upperstates, false);
      if (lower.isMarked(s, mMarking)) {
        canon.setMarked(s + upperstates, mMarking, true);
      }
      while (ti.advance()) {
        canon.addTransition(s + upperstates, ti.getCurrentEvent(),
                            ti.getCurrentTargetState() + upperstates);
      }
    }
    alphas.clear();
    alphaset.forEachEntry(new TObjectObjectProcedure<TIntHashSet, TIntArrayList>(){
      @Override
      public boolean execute(final TIntHashSet alphasfun, final TIntArrayList states) {
        final int[] arralp = alphasfun.toArray();
        final int[] arrsta = states.toArray();
        for (int i = 0; i < arrsta.length; i++) {
          for (int j = 0; j < arralp.length; j++) {
            final int alpha = arralp[j];
            alphas.add(alpha);
            canon.setReachable(alpha + upperstates + lowerstates, true);
            canon.addTransition(arrsta[i], mCont, //EventEncoding.TAU,
                                alpha + upperstates + lowerstates);
          }
        }
        return true;
      }
    });
    final int[] alphaarr = alphas.toArray();
    for (int i = 0; i < alphaarr.length; i++) {
      final int state = alphaarr[i];
      canon.setMarked(lowerstates + upperstates + state, mAlpha, true);
      canon.setReachable(lowerstates + upperstates + state, true);
      canon.addTransition(lowerstates + upperstates + state, mCont, //EventEncoding.TAU,
                          state + upperstates);
    }
    for (int s = 0; s < lowerstates; s++) {
      canon.setInitial(s + upperstates, false);
    }
    final ObservationEquivalenceTRSimplifier oetrs = new ObservationEquivalenceTRSimplifier(canon);
    oetrs.run();
    return canon;
  }

  private final ListBufferTransitionRelation mAutomaton;
  private final EventEncoding mEncoding;
  private final int mAlpha;
  private final int mMarking;
  private final int mCont;
}
