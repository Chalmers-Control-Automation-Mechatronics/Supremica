package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import java.util.ArrayList;
import java.util.Collection;
import gnu.trove.TIntArrayList;
import java.util.Set;
import gnu.trove.THashSet;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Map;
import gnu.trove.THashMap;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import java.util.List;
import gnu.trove.TIntObjectHashMap;
import net.sourceforge.waters.model.des.EventProxy;
import java.util.Collections;
import gnu.trove.TIntObjectIterator;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

public class UnAnnotateGraph2
{
  private final TransitionRelation mTransitionRelation;
  private final EventProxy mMarked;
  
  public UnAnnotateGraph2(TransitionRelation transitionrelation, EventProxy marked)
  {
    mTransitionRelation = transitionrelation;
    mMarked = marked;
  }
  
  public AutomatonProxy run(ProductDESProxyFactory factory)
  {
    boolean containsmarked = mTransitionRelation.getEvents().contains(mMarked);
    Map<TIntHashSet, TIntArrayList> statesWithAnnotation = 
      new THashMap<TIntHashSet, TIntArrayList>();
    List<TransitionProxy> newTransitions = new ArrayList<TransitionProxy>();
    List<StateProxy> nextStates = new ArrayList<StateProxy>();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      Set<TIntHashSet> annotations = mTransitionRelation.getAnnotation(s);
      if (annotations == null) {continue;}
      //System.out.println("has annotation size:" + annotations.size());
      for (TIntHashSet ann : annotations) {
        TIntArrayList states = statesWithAnnotation.get(ann);
        if (states == null) {
          states = new TIntArrayList();
          statesWithAnnotation.put(ann, states);
        }
        states.add(s);
      }
    }
    TIntObjectHashMap<TIntArrayList> newStates =
      new TIntObjectHashMap<TIntArrayList>();
    Collection<EventProxy> markedcol = Collections.singleton(mMarked);
    Collection<EventProxy> notmarked = Collections.emptySet();
    List<int[]> newtransitionsI = new ArrayList<int[]>();
    int statenum = 0;
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      TIntArrayList states = new TIntArrayList();
      states.add(statenum);
      newStates.put(s, states);
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
        if (succs == null) {continue;}
        int[] array = succs.toArray();
        for (int ti = 0; ti < array.length; ti++) {
          int t = array[ti];
          newtransitionsI.add(new int[]{s, e, t});
        }
      }
      Collection<EventProxy> used = mTransitionRelation.isMarked(s) ?
                                    markedcol : notmarked;
      boolean isInitial = mTransitionRelation.isInitial(s);
      if (!containsmarked) {
        used = notmarked;
      }
      StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
      nextStates.add(sp);
      statenum++;
      assert(statenum == nextStates.size());
    }
    for (TIntHashSet ann : statesWithAnnotation.keySet()) {
      int[] states = statesWithAnnotation.get(ann).toNativeArray();
      for (int i = 0; i < states.length; i++) {
        int state = states[i];
        Collection<EventProxy> used = notmarked;
        TIntIterator it = ann.iterator();
        while (it.hasNext()) {
          int event = it.next();
          if (mTransitionRelation.isMarkingEvent(event)) {
            used = markedcol;
            continue;
          }
          TIntHashSet succs = mTransitionRelation.getSuccessors(state, event);
          int[] array = succs.toArray();
          for (int suci = 0; suci < array.length; suci++) {
            int suc = array[suci];
            newtransitionsI.add(new int[]{statenum, event, suc});
          }
        }
        TIntArrayList sharedstates = newStates.get(state);
        sharedstates.add(statenum);
        if (!containsmarked) {
          used = notmarked;
        }
        StateProxy sp = new AnnotatedMemStateProxy(statenum, used, false);
        //System.out.println("marking:" + used);
        nextStates.add(sp);
        statenum++;
        assert(statenum == nextStates.size());
      }
    }
    for (int[] t : newtransitionsI) {
      int s = t[0];
      int e = t[1];
      int ot = t[2];
      StateProxy source = nextStates.get(s);
      EventProxy event = mTransitionRelation.getEvent(e);
      TIntArrayList targets = newStates.get(ot);
      StateProxy target = nextStates.get(ot);
      newTransitions.add(factory.createTransitionProxy(source, event, target));
    }
    EventProxy tau = factory.createEventProxy("tau:" + mTransitionRelation.getName(), EventKind.UNCONTROLLABLE);
    for (int i = 0; i < newStates.size(); i++) {
      StateProxy source = nextStates.get(i);
      TIntArrayList taus = newStates.get(i);
      for (int j = 0; j < taus.size(); j++) {
        int state = taus.get(j);
        if (i == state) {continue;}
        StateProxy target = nextStates.get(state);
        newTransitions.add(factory.createTransitionProxy(source, tau, target));
      }
    }
    Collection<EventProxy> eventsset = mTransitionRelation.getEvents();
    eventsset.add(tau);
    return factory.createAutomatonProxy(mTransitionRelation.getName(),
                                        ComponentKind.PLANT,
                                        eventsset,
                                        nextStates, newTransitions);
  }
}
