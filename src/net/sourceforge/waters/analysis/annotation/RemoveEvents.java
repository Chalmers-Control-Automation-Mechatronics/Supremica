package net.sourceforge.waters.analysis.annotation;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TIntArrayList;
import java.util.Arrays;
import gnu.trove.TLongByteHashMap;
import java.util.Set;
import gnu.trove.TLongIntHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.THashSet;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.analysis.TransitionRelation;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.EventProxy;
import java.util.Iterator;
import net.sourceforge.waters.xsd.base.ComponentKind;
import gnu.trove.TObjectIntHashMap;
import net.sourceforge.waters.model.des.StateProxy;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

public class RemoveEvents
{
  final private AutomatonProxy mAut;
  final ProductDESProxyFactory mFactory;
  final TIntHashSet[][] mSuccs;
  TIntHashSet[] mActivePreds;
  final EventProxy[] mEvents;
  final EventProxy mMarked;
  final boolean[] mInitial;
  final boolean[][] mImPossible;
  final Set<EventProxy> mImpossibleEvent;
  final Set<EventProxy> mNotLocal;
  
  public RemoveEvents(AutomatonProxy aut, Map<EventProxy,
                                              Map<EventProxy,
                                                  Set<Set<EventProxy>>>> canthappen,
                      EventProxy marked,
                      ProductDESProxyFactory factory)
  {
    mMarked = marked;
    mAut = aut;
    mFactory = factory;
    mSuccs = new TIntHashSet[aut.getStates().size()][aut.getEvents().size()];
    mActivePreds = new TIntHashSet[aut.getStates().size()];
    mEvents = new EventProxy[aut.getEvents().size()];
    mImpossibleEvent = new THashSet<EventProxy>();
    mNotLocal = new THashSet<EventProxy>();
    mImPossible = new boolean[aut.getStates().size()][aut.getEvents().size()];
    mInitial = new boolean[aut.getStates().size()];
    TObjectIntHashMap<StateProxy> statetoint =
      new TObjectIntHashMap<StateProxy>();
    TObjectIntHashMap<EventProxy> eventtoint =
      new TObjectIntHashMap<EventProxy>();
    int i = 0;
    for (int s = 0; s < mSuccs.length; s++) {
      for (int e = 0; e < mSuccs[s].length; e++) {
        mSuccs[s][e] = new TIntHashSet();
      }
      mActivePreds[s] = new TIntHashSet();
    }
    for (StateProxy state : aut.getStates()) {
      if (state.isInitial()) {
        mInitial[i] = true;
      }
      statetoint.put(state, i); i++;
    }
    i = 0;
    for (EventProxy event : aut.getEvents()) {
      mEvents[i] = event;
      eventtoint.put(event, i);
      i++;
    }
    for (TransitionProxy tran : aut.getTransitions()) {
      int source = statetoint.get(tran.getSource());
      int target = statetoint.get(tran.getTarget());
      int event = eventtoint.get(tran.getEvent());
      mSuccs[source][event].add(target);
      if (source != target) {mActivePreds[target].add(event);}
    }
    //calculatePossible(canthappen);
  }
  
  public void calculatePossible(Map<EventProxy,
                                    Map<EventProxy,
                                        Set<Set<EventProxy>>>> eventmap)
  {
    boolean changed = true;
    changed = false;
    for (int s = 0; s < mInitial.length; s++) {
      if (mInitial[s]) {continue;}
      ACTIVE:
      for (int ei = 0; ei < mEvents.length; ei++) {
        EventProxy event = mEvents[ei];
        Map<EventProxy, Set<Set<EventProxy>>> tups = eventmap.get(event);
        if (tups == null || tups.isEmpty()) {continue;}
        Set<EventProxy> selflooped = new THashSet<EventProxy>();
        Set<EventProxy> incoming = new THashSet<EventProxy>();
        for (int e = 0; e < mEvents.length; e++) {
          EventProxy eventpred = mEvents[e];
          if (mSuccs[s][e].contains(s)) {
            selflooped.add(eventpred);
          }
          if (!mActivePreds[s].contains(e)) {continue;}
          if (!tups.containsKey(eventpred)) {continue ACTIVE;}
          incoming.add(eventpred);
        }
        INCOMING:
        for (EventProxy e : incoming) {
          Set<Set<EventProxy>> required = tups.get(e);
          REQUIRED:
          for (Set<EventProxy> req : required) {
            //if (!tr.getEvents().containsAll(required)) {continue;}
            for (EventProxy self : selflooped) {
              if (req.contains(self)) {continue REQUIRED;}
            }
            // has no selfloops for this requires, next event
            continue INCOMING;
          }
          // failed each requires, can't do
          continue ACTIVE;                                 
        }
        //System.out.println("removed transitions");
        //tr.removeOutgoing(s, arr[i]);
        mImPossible[s][ei] = true;
        changed = true;
      }
    }
  }
  
  public void run()
  {
    Events:
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e].equals(mMarked)) {continue;}
      boolean impossible = true;
      boolean notlocal = true;
      for (int s = 0; s < mSuccs.length; s++) {
        //if (mImPossible[s][e]) {continue;}
        if (!mSuccs[s][e].isEmpty()) {impossible = false;}
        if (!mSuccs[s][e].contains(s) || mSuccs[s][e].size() > 1) {
          notlocal = false;
        }
        if (!impossible && !notlocal) {continue Events;}
      }
      if (impossible) {mImpossibleEvent.add(mEvents[e]); System.out.println("impossible");}
      if (notlocal) {mNotLocal.add(mEvents[e]); System.out.println("notlocal");}
    }
  }
  
  public AutomatonProxy getAutomaton()
  {
    if (mNotLocal.isEmpty()) {return mAut;}
    String name = mAut.getName();
    Collection<EventProxy> mEvents = new THashSet<EventProxy>(mAut.getEvents());
    mEvents.removeAll(mNotLocal);
    Collection<TransitionProxy> trans = new ArrayList<TransitionProxy>(mAut.getTransitions());
    Iterator<TransitionProxy> it = trans.iterator();
    while (it.hasNext()) {
      TransitionProxy tran = it.next();
      if (mNotLocal.contains(tran.getEvent())) {it.remove();}
    }
    AutomatonProxy result = mFactory.createAutomatonProxy(name,
                                                          ComponentKind.PLANT,
                                                          mEvents,
                                                          mAut.getStates(), trans);
    return result;
  }
  
  public Set<EventProxy> getImpossible()
  {
    return mImpossibleEvent;
  }
}
