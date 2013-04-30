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

