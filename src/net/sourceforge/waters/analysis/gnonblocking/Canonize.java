//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   Canonize
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.THashMap;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;
import gnu.trove.TObjectObjectProcedure;
import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.annotation.ListBufferTauLoopRemoval;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
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
    throws OverflowException, AnalysisException
  {
    final ListBufferTauLoopRemoval lbtr = new ListBufferTauLoopRemoval(mAutomaton);
    lbtr.run();
//    System.out.println("automaton: " + mAutomaton.createAutomaton(factory, mEncoding));
    //System.out.println(mAutomaton.getNumberOfReachableStates());
    //System.out.println("canon");
    final TIntHashSet alphas = new TIntHashSet();
    for (int s = 0; s < mAutomaton.getNumberOfStates(); s++) {
      //System.out.println(mAutomaton.isMarked(s, mAlpha));
      if (mAutomaton.isMarked(s, mAlpha)) {alphas.add(s);}
    }
    //System.out.println("alphas:" + Arrays.toString(alphas.toArray()));
    Determinizer determinizer = new Determinizer(mAutomaton, mEncoding,
                                                 mMarking);
    determinizer.setNodeLimit(100000);
    determinizer.run(alphas.toArray());
    //System.out.println("det");
    final ListBufferTransitionRelation lower = determinizer.getAutomaton();
    FindBlockingStates fbs = new FindBlockingStates(lower, mMarking);
    TIntHashSet blocking = fbs.getBlockingStates();
    int[] arrblocking = blocking.toArray();
    for (int i = 0; i < arrblocking.length; i++) {
      //System.out.println("blocking: " + i);
      lower.removeIncomingTransitions(arrblocking[i]);
      lower.removeOutgoingTransitions(arrblocking[i]);
    }
    //System.out.println(lower);
    lower.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    //System.out.println(lower);
    //System.out.println(lower.getNumberOfReachableStates());
    /*ListBufferTransitionRelation clower = new ListBufferTransitionRelation(lower, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    ObservationEquivalenceTRSimplifier oetrsl = new ObservationEquivalenceTRSimplifier(clower);
    //System.out.println(lower);
    oetrsl.run();
    System.out.println("lower: " + lower.getNumberOfReachableStates() + ", " + clower.getNumberOfReachableStates());*/
    //oetrs.applyResultPartition();
    //System.out.println("lower:" + lower.createAutomaton(factory, mEncoding));
    final List<int[]> partitions = null; //oetrs.getResultPartition();
    // this tells me what the alpha states in the original became when determinizing
    final TIntIntHashMap initialtoinitial = determinizer.getInitialToInitial();
    if (partitions != null) {
      // I need to update the initials now
      final TIntIntHashMap otherway = new TIntIntHashMap();
      initialtoinitial.forEachEntry(new TIntIntProcedure() {
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
          //System.out.println(state + "; " + initial);
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
    //System.out.println("aut: " + mAutomaton.createAutomaton(factory, mEncoding));
    determinizer = new Determinizer(mAutomaton, mEncoding,
                                    mAlpha);
    determinizer.setNodeLimit(100000);
    determinizer.run();
    final ListBufferTransitionRelation upper = determinizer.getAutomaton();
    //System.out.println("upper: " + upper.createAutomaton(factory, mEncoding));
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
    System.out.println("subsumption");
    statesets.forEachEntry(new TObjectIntProcedure<TIntHashSet>(){
      public boolean execute(final TIntHashSet set, final int state) {
        final int[] arr = set.toArray(); //System.out.println(Arrays.toString(alphas.toArray()));
        //System.out.println(Arrays.toString(arr));
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
            //System.out.println("\ts1: " + s1 + "\ts2:" + s2 + "arr: " + Arrays.toString(arr));
            if (s1 == s2) {
              System.out.println("same");
              arr[j] = -1;
              continue INNER;
            }
            final int lesser = lm.run(s1, s2);
            //System.out.println("\ts1: " + s1 + "\ts2:" + s2 + "\tlesser:" + lesser);
            if (lesser == s1) {
              //System.out.println("SUBSUMED");
              arr[j] = -1;
              continue INNER;
            }
            if (lesser == s2) {
              //System.out.println("SUBSUMED");
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
          //System.out.println("add alphaset");
          stateswith = new TIntArrayList();
          alphaset.put(alphas, stateswith);
        }
        stateswith.add(state);
        return true;
      }
    });
    lm.outputstats();
    System.out.println("finished subsumption");
    final List<int[]> partitions2 = new ArrayList<int[]>();
    if (!nonAlphas.isEmpty()) {partitions2.add(nonAlphas.toNativeArray());}
    alphaset.forEachValue(new TObjectProcedure<TIntArrayList>() {
      public boolean execute(final TIntArrayList set) {
        partitions2.add(set.toNativeArray());
        //System.out.println(Arrays.toString(set.toNativeArray()));
        return true;
      }
    });
    //System.out.println(Arrays.toString(nonAlphas.toNativeArray()));
    //System.out.println(partitions2.size() + ":" + upper.getNumberOfReachableStates());
    final int uppstates = upper.getNumberOfStates();
    upper.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    //ListBufferTransitionRelation cupper = new ListBufferTransitionRelation(upper, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    //oetrs = new ObservationEquivalenceTRSimplifier(cupper);
    //System.out.println(lower);
    //oetrs.run();
    //System.out.println("upper: " + upper.getNumberOfReachableStates() + ", " + cupper.getNumberOfReachableStates());
    final List<int[]> partitions3 = null; //oetrs.getResultPartition();
    //oetrs.applyResultPartition();
    //System.out.println("upper: " + upper.createAutomaton(factory, mEncoding));
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
    //System.out.println("lower to glue");
    //System.out.println(alphaset.size());
    alphaset.forEachEntry(new TObjectObjectProcedure<TIntHashSet, TIntArrayList>(){
      public boolean execute(final TIntHashSet alphasfun, final TIntArrayList states) {
        //System.out.println("alphas:" + Arrays.toString(alphasfun.toArray()));
        //System.out.println("states:" + Arrays.toString(states.toNativeArray()));
        final int[] arralp = alphasfun.toArray();
        final int[] arrsta = states.toNativeArray();
        for (int i = 0; i < arrsta.length; i++) {
          for (int j = 0; j < arralp.length; j++) {
            final int alpha = arralp[j];
            alphas.add(alpha);
            //alpha = initialtoinitial.get(alpha);
            //System.out.println(arrsta[i] + " " + (alpha + upperstates + lowerstates));
            canon.setReachable(alpha + upperstates + lowerstates, true);
            canon.addTransition(arrsta[i], mCont, //EventEncoding.TAU,
                                alpha + upperstates + lowerstates);
          }
        }
        return true;
      }
    });
    int[] alphaarr = alphas.toArray();
    //Systemc.out.println("glue to upper");
    for (int i = 0; i < alphaarr.length; i++) {
      final int state = alphaarr[i];
      //System.out.println("state: " + state + "lowerstates: " + lowerstates);
      canon.setMarked(lowerstates + upperstates + state, mAlpha, true);
      canon.setReachable(lowerstates + upperstates + state, true);
      //System.out.println((lowerstates + upperstates + state) + " " + (state + upperstates));
      canon.addTransition(lowerstates + upperstates + state, mCont, //EventEncoding.TAU,
                          state + upperstates);
    }
    for (int s = 0; s < lowerstates; s++) {
      canon.setInitial(s + upperstates, false);
    }
    //System.out.println("end canon");
    //System.out.println("canon before: " + canon.createAutomaton(factory, mEncoding));
    final ObservationEquivalenceTRSimplifier oetrs = new ObservationEquivalenceTRSimplifier(canon);
    oetrs.run();
    //System.out.println("canon: " + canon.createAutomaton(factory, mEncoding));
    //if (canon.getName().equals("Canon:{Canon:{Input1,Output},WS2}")) {System.exit(4);}
    return canon;
  }

  private final ListBufferTransitionRelation mAutomaton;
  private final EventEncoding mEncoding;
  private final int mAlpha;
  private final int mMarking;
  private final int mCont;
}
