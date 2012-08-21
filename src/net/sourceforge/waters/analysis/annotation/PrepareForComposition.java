package net.sourceforge.waters.analysis.annotation;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class PrepareForComposition
{
  private final TransitionRelation mTransitionRelation;
  private final EventProxy mMarked;
  private final Set<EventProxy> mSelfloops;
  private final Map<Set<Set<EventProxy>>, EventProxy> mAnnToEvent;

  public static int STATESADDED = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    STATESADDED = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "UnAnnotateGraph: STATESADDED = " + STATESADDED +
            " TIME = " + TIME;
  }

  public EventProxy getAnnotationEvent(final Set<TIntHashSet> annotations) {
    if (annotations == null || annotations.isEmpty()) {return null;}
    final Set<Set<EventProxy>> res = new THashSet<Set<EventProxy>>();
    for (final TIntHashSet ann : annotations) {
      final Set<EventProxy> set = new THashSet<EventProxy>(mSelfloops);
      ann.forEach(new TIntProcedure() {
        public boolean execute(final int e) {
          final EventProxy event = mTransitionRelation.getEvent(e);
          assert(event != null);
          set.add(event);
          return true;
        }
      });
      res.add(set);
    }
    EventProxy event = mAnnToEvent.get(res);
    if (event == null) {
      event = new AnnotationEvent(res, mAnnToEvent.size() + "");
      mAnnToEvent.put(res, event);
    }
    //System.out.println("annotation:" + res);
    return event;
  }

  public PrepareForComposition(final TransitionRelation transitionrelation,
                               final EventProxy marked, final Set<EventProxy> selfloops)
  {
    mTransitionRelation = transitionrelation;
    mMarked = marked;
    mSelfloops = selfloops;
    mAnnToEvent = new THashMap<Set<Set<EventProxy>>, EventProxy>();
  }

  public AutomatonProxy run(final ProductDESProxyFactory factory)
  {
    TIME -= System.currentTimeMillis();
    final boolean containsmarked = mTransitionRelation.getEvents().contains(mMarked);
    final Map<TIntHashSet, TIntArrayList> statesWithAnnotation =
      new THashMap<TIntHashSet, TIntArrayList>();
    final Map<TIntHashSet, TIntArrayList> statesWithActiveEvents =
      new THashMap<TIntHashSet, TIntArrayList>();
    final List<TransitionProxy> newTransitions = new ArrayList<TransitionProxy>();
    final List<StateProxy> nextStates = new ArrayList<StateProxy>();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      Set<TIntHashSet> annotations = mTransitionRelation.getAnnotation(s);
      if (annotations != null) {
        annotations.remove(mTransitionRelation.getActiveEvents(s));
        annotations = annotations.isEmpty() ? null : annotations;
      }
      if (annotations == null) {
        final TIntHashSet active = mTransitionRelation.getActiveEvents(s);
        TIntArrayList withActiveEvents = statesWithActiveEvents.get(active);
        if (withActiveEvents == null) {
          withActiveEvents = new TIntArrayList();
          statesWithActiveEvents.put(active, withActiveEvents);
        }
        withActiveEvents.add(s);
      } else {
        //System.out.println("has annotation size:" + annotations.size());
        for (final TIntHashSet ann : annotations) {
          if (ann.equals(mTransitionRelation.getActiveEvents(s))) {
            continue;
          }
          TIntArrayList states = statesWithAnnotation.get(ann);
          if (states == null) {
            states = new TIntArrayList();
            statesWithAnnotation.put(ann, states);
          }
          states.add(s);
        }
      }
    }
    final TIntObjectHashMap<TIntArrayList> newStates =
      new TIntObjectHashMap<TIntArrayList>();
    // slightly confusing the source refers to state in new automaton
    // the successor refers to original
    final List<int[]> newtransitionsI = new ArrayList<int[]>();
    int statenum = 0;
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final TIntArrayList states = new TIntArrayList();
      states.add(statenum);
      newStates.put(s, states);
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
        if (succs == null) {continue;}
        final int[] array = succs.toArray();
        for (int ti = 0; ti < array.length; ti++) {
          final int t = array[ti];
          newtransitionsI.add(new int[]{s, e, t});
        }
      }
      final Set<EventProxy> used = new THashSet<EventProxy>();
      if (mTransitionRelation.isMarked(s)) {used.add(mMarked);}
      Set<TIntHashSet> anns = mTransitionRelation.getAnnotation(s);
      anns = anns == null ? Collections.singleton(mTransitionRelation.getActiveEvents(s))
                          : anns;
      used.add(getAnnotationEvent(anns));
      final boolean isInitial = mTransitionRelation.isInitial(s);
      if (!containsmarked) {
        used.remove(mMarked);
      }
      final StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
      nextStates.add(sp);
      statenum++;
      assert(statenum == nextStates.size());
    }
    for (final TIntHashSet ann : statesWithAnnotation.keySet()) {
      final TIntHashSet states = new TIntHashSet(statesWithAnnotation.get(ann).toNativeArray());
      final TIntArrayList withActiveEvents = statesWithActiveEvents.get(ann);
      if (withActiveEvents != null) {
        final int[] arr = states.toArray();
        final int[] eventarr = ann.toArray();
        STATES:
        for (int i = 0; i < arr.length; i++) {
          final int state = arr[i];
          WITHACTIVE:
          for (int j = 0; j < withActiveEvents.size(); j++) {
            final int acstate = withActiveEvents.get(j);
            for (int k = 0; k < eventarr.length; k++) {
              final int event = eventarr[k];
              if (mTransitionRelation.isMarkingEvent(event)) {continue;}
              final TIntHashSet succs1 = mTransitionRelation.getSuccessors(state, event);
              final TIntHashSet succs2 = mTransitionRelation.getSuccessors(acstate, event);
              if (!succs1.containsAll(succs2.toArray())) {
                continue WITHACTIVE;
              }
            }
            final TIntArrayList sharedstates = newStates.get(state);
            sharedstates.add(acstate);
            states.remove(state);
            continue STATES;
          }
        }
      }
      while (!states.isEmpty()) {
        final TIntObjectHashMap<TIntArrayList> indexToSuccessor =
          new TIntObjectHashMap<TIntArrayList>();
        TIntArrayList tocheck = new TIntArrayList(states.toArray());
        final TIntIterator it = ann.iterator();
        final Collection<EventProxy> used = new THashSet<EventProxy>();
        while (it.hasNext()) {
          final int event = it.next();
          if (mTransitionRelation.isMarkingEvent(event)) {
            used.add(mMarked);
            continue;
          }
          for (int its = 0; its < tocheck.size(); its++) {
            final int s = tocheck.get(its);
            final TIntHashSet succs = mTransitionRelation.getSuccessors(s, event);
            if (succs == null) {
              System.out.println("state = " + s);
            }
            final int[] array = succs.toArray();
            for (int suci = 0; suci < array.length; suci++) {
              final int suc = array[suci];
              TIntArrayList indexs = indexToSuccessor.get(suc);
              if (indexs == null) {
                indexs = new TIntArrayList();
                indexToSuccessor.put(suc, indexs);
              }
              indexs.add(s);
            }
          }

          TIntArrayList biggestlist = null;
          int bestsuc = -1;
          final TIntObjectIterator<TIntArrayList> it2 = indexToSuccessor.iterator();
          while (it2.hasNext()) {
            it2.advance();
            final TIntArrayList list = it2.value();
            if (biggestlist == null || biggestlist.size() < list.size()) {
              biggestlist = list;
              bestsuc = it2.key();
            }
          }
          if (biggestlist == null) {
            System.out.println("number of things:" + indexToSuccessor.size());
          }
          newtransitionsI.add(new int[]{statenum, event, bestsuc});
          /*System.out.println("statenum:" + statenum);
          System.out.println("numstates:" + mTransitionRelation.numberOfStates());
          System.out.println("event: " + mTransitionRelation.getEvent(event));
          System.out.println("tocheck.size()" + tocheck.size() + "biggestlist.size()" + biggestlist.size());*/
          tocheck = biggestlist;
          for (int i = 0; i < biggestlist.size(); i++) {
            final int state = biggestlist.get(i);
            if (!mTransitionRelation.getSuccessors(state, event).contains(bestsuc)) {
              System.out.println("doesn't contain successor");
              System.exit(2);
            }
          }
          indexToSuccessor.clear();
        }
        final boolean isInitial = false;
        for (int its = tocheck.size() - 1; its >= 0; its--) {
          final int state = tocheck.get(its);
          final TIntArrayList sharedstates = newStates.get(state);
          //isInitial = mTransitionRelation.isInitial(state) ? true : isInitial;
          sharedstates.add(statenum);
          states.remove(state);
        }
        if (!containsmarked) {
          used.remove(mMarked);
        }
        final StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
        //System.out.println("marking:" + used);
        nextStates.add(sp);
        statenum++;
        STATESADDED++;
        assert(statenum == nextStates.size());
      }
    }
    for (final int[] t : newtransitionsI) {
      final int s = t[0];
      final int e = t[1];
      final int ot = t[2];
      final StateProxy source = nextStates.get(s);
      final EventProxy event = mTransitionRelation.getEvent(e);
      /*for (int ti = 0; ti < targets.size(); ti++) {
        int ta = targets.get(ti);
        StateProxy target = nextStates.get(ta);
        newTransitions.add(factory.createTransitionProxy(source, event, target));
      }*/
      final StateProxy target = nextStates.get(ot);
      newTransitions.add(factory.createTransitionProxy(source, event, target));
    }
    for (int i = 0; i < newStates.size(); i++) {
      final StateProxy source = nextStates.get(i);
      final TIntArrayList taus = newStates.get(i);
      //System.out.println("Taus:" + taus.size());
      for (int j = 0; j < taus.size(); j++) {
        final int state = taus.get(j);
        if (i == state) {continue;}
        //System.out.println("tau:" + state);
        final StateProxy target = nextStates.get(state);
        for (final EventProxy event : mSelfloops) {
          newTransitions.add(factory.createTransitionProxy(source, event, target));
        }
      }
    }
    for (final StateProxy s : nextStates) {
      for (final EventProxy e : mSelfloops) {
        newTransitions.add(factory.createTransitionProxy(s, e, s));
      }
    }
    final Collection<EventProxy> eventsset = mTransitionRelation.getEvents();
    eventsset.addAll(mSelfloops);
    TIME += System.currentTimeMillis();
    return factory.createAutomatonProxy(mTransitionRelation.getName(),
                                        ComponentKind.PLANT,
                                        eventsset,
                                        nextStates, newTransitions);
  }
}
