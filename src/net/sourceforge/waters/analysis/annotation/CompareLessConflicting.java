//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.annotation
//# CLASS:   CompareLessConflicting
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.gnonblocking.FindBlockingStates;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * <P>An implementation of the conflict preorder algorithm.</P>
 *
 * <P><I>Reference:</I><BR>
 * Simon Ware, Robi Malik. A State-Based Characterisation of the Conflict
 * Preorder. Proc. 10th Workshop on the Foundations of Coordination Languages
 * and Software Architecture, FOCLASA 2011, 34-48, Aachen, Germany, 2011.</P>
 *
 * @author Simon Ware
 */

public class CompareLessConflicting
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a conflict preorder checker to compare two transition
   * relations. The conflict preorder checker compares two transition
   * relations defined using the same event encoding.
   * @param first   A transition relation that is to be checked whether it
   *                is less conflicting than the second argument.
   * @param second  A transition relation that is to be checked whether it
   *                is more conflicting than the first argument.
   * @param marking The event code of the marking proposition in both
   *                transition relations.
   */
  public CompareLessConflicting(final ListBufferTransitionRelation first,
                                final ListBufferTransitionRelation second,
                                final int marking)
  {
    mFirstRelation = first;
    mSecondRelation = second;
    mSetCache = new HashMap<TIntHashSet, TIntHashSet>();
    mTupleCache = new TObjectIntHashMap<Tuple>();
    mStates = new ArrayList<Tuple>();
    mFirstLC = new TIntHashSet();
    mExpanded = 0;
    mMarking = marking;
    FindBlockingStates fbs = new FindBlockingStates(second, mMarking);
    mSecondBlocking = fbs.getBlockingStates();
    fbs = new FindBlockingStates(first, mMarking);
    System.out.println(Arrays.toString(mSecondBlocking.toArray()));
    System.out.println("First: " + mFirstRelation.getNumberOfStates());
    System.out.println("Second: " + mSecondRelation.getNumberOfStates());
    mSuccessors = new ArrayList<TIntArrayList>();
    mPredeccessors = new ArrayList<TIntHashSet[]>();
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this checker to determine whether the given transition relations
   * are related through the conflict preorder.
   * @return <CODE>true</CODE> if the given first transition relation is
   *         less conflicting than the given second transition relation.
   * @see #CompareLessConflicting(ListBufferTransitionRelation, ListBufferTransitionRelation, int)
   *      CompareLessConflicting()
   */
  public boolean isLessConflicting()
  {
    final TIntHashSet first = new TIntHashSet();
    final TIntHashSet second = new TIntHashSet();
    for (int s = 0; s < mFirstRelation.getNumberOfStates(); s++) {
      if (mFirstRelation.isInitial(s)) {
        first.add(s);
        continue;
      }
    }
    for (int s = 0; s < mSecondRelation.getNumberOfStates(); s++) {
      if (mSecondRelation.isInitial(s)) {
        second.add(s);
        continue;
      }
    }
    return isLessConflicting(new Tuple(calculateTauReachable(first, mFirstRelation),
                                       calculateTauReachable(second, mSecondRelation)));
  }


  //#########################################################################
  //# Auxiliary Methods
  private TIntHashSet calculateTauReachable(final int state,
                                            final ListBufferTransitionRelation trans)
  {
    final TIntHashSet set = new TIntHashSet();
    set.add(state);
    return calculateTauReachable(set, trans);
  }

  private TIntHashSet calculateTauReachable(final TIntHashSet set,
                                            final ListBufferTransitionRelation trans)
  {
    final TIntHashSet taureach = new TIntHashSet(set.toArray());
    final TIntArrayList togo = new TIntArrayList(set.toArray());
    while (!togo.isEmpty()) {
      final int state = togo.remove(togo.size() - 1);
      final TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(state,
                                                                     EventEncoding.TAU);
      while (ti.advance()) {
        if (taureach.add(ti.getCurrentTargetState())) {
          togo.add(ti.getCurrentTargetState());
        }
      }
    }
    return taureach;
  }

  private TIntHashSet calculateSuccessor(TIntHashSet set, final int event,
                                         final ListBufferTransitionRelation trans)
  {
    set = calculateTauReachable(set, trans);// this shouldn't be needed
    final TIntHashSet succ = new TIntHashSet();
    final TIntIterator it = set.iterator();
    while (it.hasNext()) {
      final int s = it.next();
      if (s == -1) {return null;}
      if (event != trans.getNumberOfProperEvents()) {
        final TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(s, event);
        while (ti.advance()) {
          succ.add(ti.getCurrentTargetState());
        }
      } else {
        if (trans.isMarked(s, mMarking)) {
          succ.add(-1);
          return succ;
        }
      }
    }
    return calculateTauReachable(succ, trans);
  }

  private int getState(final Tuple tup)
  {
    if (!mTupleCache.containsKey(tup)) {
      final int state = mStates.size();
      mTupleCache.put(tup, state);
      mStates.add(tup);
      final int[] sucs = new int[mFirstRelation.getNumberOfProperEvents() + 1];
      for (int i = 0; i < sucs.length; i++) {
        sucs[i] = -1;
      }
      mSuccessors.add(new TIntArrayList(sucs));
      mPredeccessors.add(new TIntHashSet[mFirstRelation.getNumberOfProperEvents() + 1]);
      if (tup.firstset.contains(-1)) {
        mFirstLC.add(state);
      }
      final TIntIterator it = tup.secondset.iterator();
      while (it.hasNext()) {
        final int num = it.next();
        if (mSecondBlocking.contains(num)) {
          mFirstLC.add(state); break;
        }
      }
    }
    final int state = mTupleCache.get(tup);
    return state;
  }

  private void expandStates()
  {
    for (;mExpanded < mStates.size(); mExpanded++) {
      final int state = mExpanded;
      final Tuple tup = mStates.get(state);
      if (tup.firstset.contains(-1) || tup.secondset.contains(-1)) {
        continue;
      }
      //System.out.println(tup);
      final TIntIterator it = tup.firstset.iterator();
      while (it.hasNext()) {
        final TIntHashSet f = new TIntHashSet();
        f.add(it.next());
        getState(new Tuple(f, tup.secondset));
      }
      for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
        if (e == EventEncoding.TAU) {continue;}
        final TIntHashSet first = calculateSuccessor(tup.firstset, e, mFirstRelation);
        final TIntHashSet second = calculateSuccessor(tup.secondset, e, mSecondRelation);
        final int target = getState(new Tuple(first, second));
        mSuccessors.get(state).set(e, target);
        TIntHashSet preds = mPredeccessors.get(target)[e];
        if (preds == null) {
          preds = new TIntHashSet();
          final TIntHashSet[] predsarr = mPredeccessors.get(target);
          predsarr[e] = preds;
        }
        preds.add(state);
      }
      final TIntHashSet first = calculateSuccessor(tup.firstset, mMarking, mFirstRelation);
      final TIntHashSet second = calculateSuccessor(tup.secondset, mMarking, mSecondRelation);
      final int target = getState(new Tuple(first, second));
      mSuccessors.get(state).set(mMarking, target);
    }
  }

  private void calculateLCStates()
  {
    boolean modified = true;
    int LC = 0;
    while (modified) {
      System.out.println("LC: " + LC++ + " " + mFirstLC.size());
      modified = false;
      final TIntArrayList makelc = new TIntArrayList();
      final Set<Triple> MCTriples = new THashSet<Triple>();
      final List<Triple> tobeexpanded = new ArrayList<Triple>();
      for (int s = 0; s < mStates.size(); s++) {
        //System.out.println(mStates.get(s));
        if (!mFirstLC.contains(s)) {
          makelc.add(s);
          final Tuple state = mStates.get(s);
          final TIntHashSet moreset = state.secondset;
          if (moreset.contains(-1)) {
            //System.out.println("MC:" + Arrays.toString(state.firstset.toArray()) + " : " + Arrays.toString(moreset.toArray()));
            final Triple triple = new Triple(mStates.get(s), -1);
            MCTriples.add(triple);
            tobeexpanded.add(triple);
          }
        }
      }
      while (!tobeexpanded.isEmpty()) {
        final Triple triple = tobeexpanded.remove(tobeexpanded.size() - 1);
        for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
          if (e == EventEncoding.TAU) {continue;}
          final TIntHashSet preds = mPredeccessors.get(mTupleCache.get(triple.tuple))[e];
          if (preds == null) {continue;}
          final TIntIterator it = preds.iterator();
          while (it.hasNext()) {
            final int pred = it.next();
            if (mFirstLC.contains(pred)) {continue;}
            final Tuple predtuple = mStates.get(pred);
            final TIntHashSet moreset = predtuple.secondset;
            final TIntIterator itstates = moreset.iterator();
            while (itstates.hasNext()) {
              final int state = itstates.next();
              final TIntHashSet newset = new TIntHashSet(); newset.add(state);
              final TIntHashSet statesuccessors = calculateSuccessor(newset, e, mSecondRelation);
              if (statesuccessors.contains(triple.state)) {
                //System.out.println("MC:" + Arrays.toString(predtuple.firstset.toArray()) + " : " + Arrays.toString(moreset.toArray()) + ":" + state);
                final Triple add = new Triple(mStates.get(pred), state);
                if (MCTriples.add(add)) {tobeexpanded.add(add);}
              }
            }
          }
        }
      }
      System.out.println("MC: " + MCTriples.size());
      for (int i = 0; i < makelc.size(); i++) {
        final int state = makelc.get(i);
        final Tuple tup = mStates.get(state);
        final TIntIterator it2 = tup.secondset.iterator();
        while (it2.hasNext()) {
          final int propstate = it2.next();
          final Triple triple = new Triple(tup, propstate);
          if (!MCTriples.contains(triple)) {
            mFirstLC.add(state);
            modified = true;
          }
        }
      }
    }
  }

  private boolean isLessConflicting(final int s1, final int s2)
  {
    return isLessConflicting(new Tuple(calculateTauReachable(s1, mFirstRelation),
                                       calculateTauReachable(s2, mSecondRelation)));
  }

  private boolean isLessConflicting(final Tuple tuple)
  {
    long time = System.currentTimeMillis();
    final int initial = getState(tuple);
    // adds the certain conflict states to the calculation
    getState(new Tuple(new TIntHashSet(), tuple.secondset));
    expandStates();
    System.out.println("tuples: " + mStates.size());
    calculateLCStates();
    //System.out.println("LC:" + mFirstLC.size());
    final TIntHashSet explored = new TIntHashSet();
    final TIntArrayList toexplore = new TIntArrayList();
    explored.add(initial);
    toexplore.add(initial);
    while (!toexplore.isEmpty()) {
      //System.out.println(mStates.size());
      final int s = toexplore.remove(toexplore.size() -1);
      Tuple state = mStates.get(s);
      if (state.firstset.isEmpty()) {continue;}
      if (state.firstset.size() > 1) {
        final TIntIterator it = state.firstset.iterator();
        while (it.hasNext()) {
          final TIntHashSet set = new TIntHashSet();
          set.add(it.next());
          if (explored.add(getState(new Tuple(set, state.secondset)))) {
            toexplore.add(getState(new Tuple(set, state.secondset)));
          }
        }
        continue;
      }
      //calculateLCStates();
      ////System.out.println("LC:" + mFirstLC.size());
      //System.out.println(state);
      if (mFirstLC.contains(getState(new Tuple(new TIntHashSet(), state.secondset)))) {continue;}
      if (!mFirstLC.contains(s)) {
        final TIntArrayList states = new TIntArrayList();
        final TIntHashSet visited2 = new TIntHashSet();
        visited2.add(s);
        states.add(s);
        while (!states.isEmpty()) {
          final int snum = states.remove(0);
          state = mStates.get(snum);
          System.out.println("tuple: " + state);
          for (int e = 0; e < mSuccessors.get(snum).size(); e++) {
            if (e == EventEncoding.TAU) {continue;}
            if (mSuccessors.get(snum).get(e) != -1) {
              final Tuple target = mStates.get(mSuccessors.get(snum).get(e));
              final int tnum = mSuccessors.get(snum).get(e);
              System.out.println(state + " " + mFirstLC.contains(snum) + " -" + e + "-> " + target + " " + mFirstLC.contains(tnum));
              if (visited2.add(tnum) && !mFirstLC.contains(tnum)) {
                states.add(tnum);
              }
            }
          }
        }
        time -= System.currentTimeMillis();
        System.out.println("Time: " + time);
        return false;
      }
      final TIntArrayList succs = mSuccessors.get(s);
      for (int e = 0; e < succs.size(); e++) {
        if (e == EventEncoding.TAU) {continue;}
        final int suc = succs.get(e);
        if (suc == -1) {continue;}
        if (explored.add(suc)) {
          //System.out.println(state + " -" + e + "-> " + mStates.get(suc));
          toexplore.add(suc);
        }
      }
    }
    time -= System.currentTimeMillis();
    System.out.println("Time: " + time);
    return true;
  }

  private TIntHashSet getSet(final TIntHashSet set)
  {
    TIntHashSet tset = mSetCache.get(set);
    if (tset == null) {
      tset = set;
      mSetCache.put(set, set);
    }
    return tset;
  }


  //#########################################################################
  //# Inner Class Triple
  private static class Triple
  {
    public final Tuple tuple;
    public final int state;

    public Triple(final Tuple ptuple, final int s)
    {
      tuple = ptuple;
      state = s;
    }

    @Override
    public int hashCode()
    {
      return tuple.hashCode() * 13 + state;
    }

    @Override
    public boolean equals(final Object o)
    {
      final Triple other = (Triple)o;
      return state == other.state && tuple.equals(other.tuple);
    }
  }


  //#########################################################################
  //# Inner Class Tuple
  private class Tuple
  {
    public final TIntHashSet firstset;
    public final TIntHashSet secondset;

    public Tuple(final TIntHashSet first, final TIntHashSet second)
    {
      firstset = getSet(first);
      secondset = getSet(second);
      if (firstset == null || secondset == null) {
        throw new RuntimeException();
      }
    }

    @SuppressWarnings("unused")
    public Tuple(final TIntHashSet first, final TIntHashSet second, final boolean alreadycanon)
    {
      firstset = first;
      secondset = second;
    }

    @Override
    public int hashCode()
    {
      return firstset.hashCode() * 13 + secondset.hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
      final Tuple other = (Tuple)o;
      return firstset == other.firstset && secondset == other.secondset;
    }

    @Override
    public String toString()
    {
      return Arrays.toString(firstset.toArray()) + " : " + Arrays.toString(secondset.toArray());
    }
  }


  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    final String modname = args[0];
    final String lessconf = args[1];
    final String moreconf = args[2];
    final String markname = args[3];
    final String tauname = args[4];
    try {
      final ProductDESProxy model = getCompiledDES(new File(modname), null);
      AutomatonProxy lprox = null;
      AutomatonProxy mprox = null;
      EventProxy mproxy = null;
      EventProxy tauproxy = null;
      for (final AutomatonProxy aut : model.getAutomata()) {
        if (aut.getName().equals(lessconf)) {lprox = aut;}
        if (aut.getName().equals(moreconf)) {mprox = aut;}
      }
      for (final EventProxy ev : model.getEvents()) {
        if (ev.getName().equals(markname)) {mproxy = ev;}
        if (ev.getName().equals(tauname)) {tauproxy = ev;}
      }
      final EventEncoding ee = new EventEncoding(lprox,
                                ConflictKindTranslator.getInstance(), tauproxy);
      if (!lprox.getEvents().contains(mproxy)) {
        ee.addEvent(mproxy, ConflictKindTranslator.getInstance(), EventEncoding.STATUS_EXTRA_SELFLOOP);
      }
      final ListBufferTransitionRelation lessbuff =
        new ListBufferTransitionRelation(lprox, ee,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final ListBufferTransitionRelation morebuff =
        new ListBufferTransitionRelation(mprox, ee,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final int mint = ee.getEventCode(mproxy);
      final CompareLessConflicting clc = new CompareLessConflicting(lessbuff, morebuff, mint);
      System.out.println(lessbuff);
      System.out.println(morebuff);
      System.out.println("Is LC: " + clc.isLessConflicting());
    } catch(final Throwable t) {
      t.printStackTrace();
    }
  }

  public static ListBufferTransitionRelation
    mergeConflictEquivalent(final ListBufferTransitionRelation aut, final int marking)
  {
    final CompareLessConflicting clc = new CompareLessConflicting(aut, aut, marking);
    final TIntObjectHashMap<TIntArrayList> statetogroup = new TIntObjectHashMap<TIntArrayList>();
    final List<TIntArrayList> values = new ArrayList<TIntArrayList>();
    for (int s1 = 0; s1 < aut.getNumberOfStates(); s1++) {
      if (statetogroup.containsKey(s1)) {continue;}
      final TIntArrayList group = new TIntArrayList();
      group.add(s1);
      statetogroup.put(s1, group);
      values.add(group);
      for (int s2 = s1 + 1; s2 < aut.getNumberOfStates(); s2++) {
        if (statetogroup.containsKey(s2)) {continue;}
        if (clc.isLessConflicting(s1, s2) && clc.isLessConflicting(s2, s1)) {
          group.add(s2);
          statetogroup.put(s2, group);
        }
      }
      System.out.println("same:" + group.size());
    }
    final List<int[]> partitions = new ArrayList<int[]>();
    System.out.println(aut.getNumberOfStates() + "vs" + values.size());
    for (final TIntArrayList list : values) {
      partitions.add(list.toNativeArray());
    }
    aut.merge(partitions);
    return aut;
  }

  private static DocumentManager mDocumentManager = new DocumentManager();
  private static ProductDESProxyFactory mProductDESProxyFactory = ProductDESElementFactory.getInstance();

  private static ProductDESProxy getCompiledDES
    (final File filename,
     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final DocumentProxy doc = mDocumentManager.load(filename);
    if (doc instanceof ProductDESProxy) {
      return (ProductDESProxy) doc;
    } else if (doc instanceof ModuleProxy) {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
      return compiler.compile(bindings);
    } else {
      return null;
    }
  }


  static {
    final ModuleElementFactory mModuleFactory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    try {
      final JAXBModuleMarshaller modmarshaller =
        new JAXBModuleMarshaller(mModuleFactory, optable);
      mDocumentManager.registerUnmarshaller(modmarshaller);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mFirstRelation;
  private final ListBufferTransitionRelation mSecondRelation;
  private final Map<TIntHashSet,TIntHashSet> mSetCache;
  private final TObjectIntHashMap<Tuple> mTupleCache;
  private final List<Tuple> mStates;
  private final List<TIntArrayList> mSuccessors;
  private final List<TIntHashSet[]> mPredeccessors;
  private final TIntHashSet mFirstLC;
  private final TIntHashSet mSecondBlocking;
  private final int mMarking;
  private int mExpanded;

}
