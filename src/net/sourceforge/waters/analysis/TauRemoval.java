//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   TauRemoval
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TIntStack;
import gnu.trove.TObjectHashingStrategy;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.AnnotatedMemStateProxy;
import net.sourceforge.waters.analysis.modular.BiSimulator;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.base.NamedElement;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

public class TauRemoval
{  
  private static int INDEX = 1;
  private static int[] TARJAN = null;
  private static int[] LOWLINK = null;
  private static boolean[] ONSTACK = null;
  private static boolean[] VISITED = null;
  private static TIntStack STACK = new TIntStack();
  private static TIntIntHashMap STATEMAPSTO = new TIntIntHashMap();
  private static THashMap<TIntHashSet, TIntHashSet> SETS =
    new THashMap<TIntHashSet, TIntHashSet>();
  private static THashMap<Set<Set<EventProxy>>, AnnotationEvent> EVENTSETS =
    new THashMap<Set<Set<EventProxy>>, AnnotationEvent>();
  

  @SuppressWarnings("unchecked")
  public static AutomatonProxy tauRemoval(AutomatonProxy graph,
                                          Set<EventProxy> taus,
                                          EventProxy mark,
                                          ProductDESProxyFactory factory)
    throws Exception
  {
    TIntHashSet[] tautransitions = new TIntHashSet[graph.getStates().size()];
    THashSet<int[]>[] statetransitions =
      new THashSet[graph.getStates().size()];
    StateProxy[] states = new StateProxy[graph.getStates().size()];
    EventProxy[] events = new EventProxy[graph.getEvents().size()];
    states = graph.getStates().toArray(states);
    events = graph.getEvents().toArray(events);
    TObjectIntHashMap<StateProxy> sti =
      new TObjectIntHashMap<StateProxy>(states.length);
    TObjectIntHashMap<EventProxy> eti =
      new TObjectIntHashMap<EventProxy>(events.length);
    boolean[] marked = new boolean[graph.getStates().size()];
    boolean[] isInitial = new boolean[graph.getStates().size()];
    for (int i = 0; i < states.length; i++) {
      if (states[i].isInitial()) {
        StateProxy t = states[0]; states[0] = states[i]; states[i] = t; break;
      }
    }
    for (int i = 0; i < states.length; i++) {
      sti.put(states[i], i);
      if (states[i].getPropositions().contains(mark)) {
        marked[i] = true;
      }
      isInitial[i] = states[i].isInitial();
    }
    for (int i = 0; i < events.length; i++) {eti.put(events[i], i);}
    System.out.println("SETUP");
    setupTransitions(graph.getTransitions(), taus, eti, sti,
                     statetransitions, tautransitions);
    CertainConflict con = new CertainConflict(statetransitions, tautransitions,
                                              marked, isInitial);
    con.run();
    System.out.println("TARJAN");
    removeTauLoops(statetransitions, tautransitions, marked);
    System.out.println("FOLLOWON");
    removeFollowOnTau(statetransitions, tautransitions);
    //System.out.println("SUBSETS");
    //removeSubsets(statetransitions, tautransitions, marked);
    System.out.println("ANNOTATE");
    Set<TIntHashSet>[] annotations = annotatedGraph(statetransitions,
                                                    tautransitions,
                                                    marked);
    System.out.println("INCOMING");
    //equivalentIncoming(statetransitions, annotations, marked, isInitial);
    System.out.println("FINISH UP");
    EventProxy tau = factory.createEventProxy(":tau:" + graph.getName(),
                                              EventKind.UNCONTROLLABLE);
    for (int i = 0; i < states.length; i++) {
      AnnotationEvent annotation = getEventSet(annotations[i], events, mark);
      EventProxy m = marked[i] ? mark : null;
      Collection<EventProxy> props = new ArrayList<EventProxy>();
      if (annotation != null) {props.add(annotation);}
      if (m != null) {props.add(m);}
      states[i] = new AnnotatedMemStateProxy(i, props, isInitial[i]);
    }
    List<TransitionProxy> newtrans = newTransitions(statetransitions,
                                                    tautransitions, states,
                                                    events,
                                                    graph.getTransitions().size(),
                                                    tau, factory);
    THashSet<EventProxy> ev = new THashSet<EventProxy>(graph.getEvents());
    taus.remove(mark);
    ev.removeAll(taus); ev.add(tau);
    System.out.println("TauTransitions:" + newtrans.size());
    AutomatonProxy result = factory.createAutomatonProxy(graph.getName(), ComponentKind.PLANT,
                                                         ev, Arrays.asList(states), newtrans);
    /*if (graph.getStates().size() < 10) {
      System.out.println("thing");
      System.out.println(graph);
      System.out.println("result");
      System.out.println(result);
      System.exit(1);
    }*/
    BiSimulator sim = new BiSimulator(result, mark, factory);
    result = sim.run();
    result = unnanotateAutomaton(result, mark, factory);
    SETS.clear();
    EVENTSETS.clear();
    return result;
  }
  
  private static AnnotationEvent getEventSet(Set<TIntHashSet> ans,
                                             EventProxy[] events,
                                             EventProxy marked)
  {
    if (ans == null) {return null;}
    Set<Set<EventProxy>> result = new THashSet<Set<EventProxy>>(ans.size());
    for (TIntHashSet an : ans) {
      Set<EventProxy> newevents = new THashSet<EventProxy>(an.size());
      TIntIterator it = an.iterator();
      while (it.hasNext()) {
        int ev = it.next();
        EventProxy event = ev == -1 ? marked : events[ev];
        newevents.add(event);
      }
      result.add(newevents);
    }
    AnnotationEvent event = EVENTSETS.get(result);
    if (event == null) {
      event = new AnnotationEvent(result, ":an" + EVENTSETS.size());
      EVENTSETS.put(result, event);
    }
    return event;
  }
  
  private static List<TransitionProxy> 
    newTransitions(THashSet<int[]>[] statetransitions,
                   TIntHashSet[] tautransitions,
                   StateProxy[] states, EventProxy[] events,
                   int transitionnumber, EventProxy tau,
                   ProductDESProxyFactory factory)
  {
    List<TransitionProxy> newtransitions = 
      new ArrayList<TransitionProxy>(transitionnumber * 3);
    for (int i = 0; i < statetransitions.length; i++) {
      if (statetransitions[i] != null) {
        StateProxy s = states[i];
        for (int[] tran: statetransitions[i]) {
          EventProxy e = events[tran[0]];
          StateProxy t = states[tran[1]];
          newtransitions.add(factory.createTransitionProxy(s, e, t));
        }
      }
    }
    for (int i = 0; i < tautransitions.length; i++) {
      if (tautransitions[i] != null) {
        StateProxy s = states[i];
        TIntIterator it = tautransitions[i].iterator();
        while (it.hasNext()) {
          StateProxy t = states[it.next()];
          newtransitions.add(factory.createTransitionProxy(s, tau, t));
        }
      }
    }
    return newtransitions;
  }
  
  private static void setupTransitions(final Collection<TransitionProxy> transitions,
                                       final Set<EventProxy> taus,
                                       final TObjectIntHashMap<EventProxy> eti,
                                       final TObjectIntHashMap<StateProxy> sti,
                                       final THashSet<int[]>[] statetransitions,
                                       final TIntHashSet[] tautransitions)
  {
    for (TransitionProxy t : transitions) {
      int source = sti.get(t.getSource());
      int event = eti.get(t.getEvent());
      int target = sti.get(t.getTarget());
      if (taus.contains(t.getEvent())) {
        if (source == target) {
          continue;
        }
        TIntHashSet sourcetau = tautransitions[source];
        if (sourcetau == null) {
          sourcetau = new TIntHashSet();
          tautransitions[source] = sourcetau;
        }
        sourcetau.add(target);
      } else {
        THashSet<int[]> sourcetrans = statetransitions[source];
        if (sourcetrans == null) {
          sourcetrans = new THashSet<int[]>(1, ArrayHash.ARRAYHASH);
          statetransitions[source] = sourcetrans;
        }
        sourcetrans.add(new int[]{event, target});
      }
    }
  }
  
  private static void tarjan(TIntHashSet[] tautransitions, int state,
                             boolean[] marked)
  {
    TARJAN[state] = INDEX;
    LOWLINK[state] = INDEX;
    INDEX++;
    TIntHashSet successors = tautransitions[state];
    if (successors == null) {
      return;
    }
    ONSTACK[state] = true;
    STACK.push(state);
    TIntIterator targets = successors.iterator();
    while (targets.hasNext()) {
      int suc = targets.next();
      if(ONSTACK[suc]) {
        LOWLINK[state] = TARJAN[suc] < LOWLINK[state] ? TARJAN[suc]
                                                      : LOWLINK[state];
      } else if (TARJAN[suc] == 0) {
        tarjan(tautransitions, suc, marked);
        LOWLINK[state] = LOWLINK[suc] < LOWLINK[state] ? LOWLINK[suc]
                                                       : LOWLINK[state];
      }
      if (marked[suc]) {marked[state] = true;}
    }
    if (TARJAN[state] == LOWLINK[state]) {
      int mapto = state + 1;
      while (true) {
        int pop = STACK.pop();
        ONSTACK[pop] = false;
        if (pop == state) {
          break;
        }
        STATEMAPSTO.put(pop, mapto);
      }
    }
  }
  
  private static void removeTauLoops(THashSet<int[]>[] statetransitions,
                                     TIntHashSet[] tautransitions,
                                     boolean[] marked)
  {
    TARJAN = new int[statetransitions.length];
    LOWLINK = new int[statetransitions.length];
    ONSTACK = new boolean[statetransitions.length];
    STACK = new TIntStack();
    INDEX = 1;
    for (int i = 0; i < TARJAN.length; i++) {
      if (TARJAN[i] == 0) {
        tarjan(tautransitions, i, marked);
      }
    }
    TARJAN = null;
    LOWLINK = null;
    ONSTACK = null;
    STACK.clear();
    if (STATEMAPSTO.size() == 0) {
      return;
    }
    TIntHashSet set = new TIntHashSet();
    Collection<int[]> arset = new THashSet<int[]>(ArrayHash.ARRAYHASH);
    for (int i = 0; i < statetransitions.length; i++) {
      THashSet<int[]> trans = statetransitions[i];
      if (trans != null) {
        Iterator<int[]> it = trans.iterator();
        arset.clear();
        while (it.hasNext()) {
          int[] tran = it.next();
          int newtarget = STATEMAPSTO.get(tran[1]);
          if (newtarget == 0) {
            continue;
          }
          it.remove();
          tran[1] = newtarget - 1;
          arset.add(tran);
        }
        trans.addAll(arset);
      }
      TIntHashSet tau = tautransitions[i];
      set.clear();
      if (tau != null) {
        TIntIterator it = tau.iterator();
        while (it.hasNext()) {
          int targ = it.next();
          int newtarg = STATEMAPSTO.get(targ);
          if (newtarg == 0) {
            continue;
          }
          set.add(newtarg - 1);
          it.remove();
        }
        tau.addAll(set.toArray());
      }
    }
    for (int i = 0; i < statetransitions.length; i++) {
      int addto = STATEMAPSTO.get(i);
      if (addto == 0) {
        continue;
      }
      addto--;

      THashSet<int[]> trans = statetransitions[i];
      if (trans != null) {
        THashSet<int[]> addtotrans = statetransitions[addto];
        if (addtotrans == null) {
          statetransitions[addto] = trans;
        } else {
          addtotrans.addAll(trans);
        }
        statetransitions[i] = null;
      }
      TIntHashSet tau = tautransitions[i];
      if (tau != null) {
        TIntHashSet addtotrans = tautransitions[addto];
        if (addtotrans == null) {
          tautransitions[addto] = tau;
        } else {
          addtotrans.addAll(tau.toArray());
        }
        tautransitions[i] = null;
      }
    }
    STATEMAPSTO.clear();
  }
  
  private static void removeFollowons(THashSet<int[]>[] statetransitions,
                                      TIntHashSet[] tautransitions, int state)
  {
    VISITED[state] = true;
    TIntHashSet targets = tautransitions[state];
    TIntIterator targs = targets.iterator();
    TIntHashSet newtargets = new TIntHashSet();
    while (targs.hasNext()) {
      int target = targs.next();
      if (tautransitions[target] == null || tautransitions[target].isEmpty()) {
        newtargets.add(target);
        continue;
      } else if (target == state) {
        continue;
      }
      removeFollowons(statetransitions, tautransitions, target);
      if (statetransitions[target] != null) {
        if (statetransitions[state] == null) {
          statetransitions[state] = new THashSet<int[]>(statetransitions[target],
                                                        ArrayHash.ARRAYHASH);
        } else {
          statetransitions[state].addAll(statetransitions[target]);
        }
      }  
      newtargets.addAll(tautransitions[target].toArray());
    }
    tautransitions[state] = newtargets;
  }
  
  private static void removeFollowOnTau(THashSet<int[]>[] statetransitions,
                                        TIntHashSet[] tautransitions)
  {
    VISITED = new boolean[tautransitions.length];
    for (int i = 0; i < tautransitions.length; i++) {
      TIntHashSet targets = tautransitions[i];
      if (VISITED[i] || targets == null) {
        continue;
      }
      removeFollowons(statetransitions, tautransitions, i);
    }
    VISITED = null;
  }
  
  private static TIntHashSet getActiveEvents(THashSet<int[]> statetransitions,
                                             boolean[] marked, int state)
  {
    TIntHashSet active = new TIntHashSet();
    if (statetransitions == null) {
      return active;
    }
    if (marked[state]) {active.add(-1);}
    for (int[] tran : statetransitions) {
      active.add(tran[0]);
    }
    TIntHashSet result = SETS.get(active);
    if (result == null) {
      result = active;
      SETS.put(result, result);
    }
    return result;
  }
  
  private static void addTransitions(THashSet<int[]>[] statetransitions,
                                     int from, int to)
  {
    THashSet<int[]> fromtrans = statetransitions[from];
    THashSet<int[]> totrans = statetransitions[to];
    if (fromtrans == null) {
      return;
    }
    if (totrans == null) {
      statetransitions[to] = new THashSet<int[]>(fromtrans, ArrayHash.ARRAYHASH);
      return;
    }
    totrans.addAll(fromtrans);
  }
  
  private static void removeSubsets(THashSet<int[]>[] statetransitions,
                                    TIntHashSet[] tautransitions,
                                    boolean[] marked)
  {
    TIntHashSet[] activeEvents = new TIntHashSet[statetransitions.length];
    TIntArrayList stilltau = new TIntArrayList();
    for (int i = 0; i < tautransitions.length; i++) {
      TIntHashSet taus = tautransitions[i];
      if (taus == null) {
        continue;
      }
      TIntIterator it = taus.iterator();
      stilltau.clear();
      while (it.hasNext()) {
        int target = it.next();
        TIntHashSet ae = activeEvents[target];
        if (ae == null) {
          ae = getActiveEvents(statetransitions[target], marked, target);
          activeEvents[target] = ae;
        }
        boolean added = true;
        boolean subset = false;
        for (int j = 0; j < stilltau.size(); j++) {
          int othertau = stilltau.get(j);
          TIntHashSet ae2 = activeEvents[othertau];
          if (ae2.size() < ae.size()) {
            if (subset) {
              continue;
            }
            if (ae.containsAll(ae2.toArray())) {
              addTransitions(statetransitions, target, i);
              added = false;
              break;
            }
          } else {
            if (ae2.containsAll(ae.toArray())) {
              addTransitions(statetransitions, othertau, i);
              stilltau.remove(j);
              subset = true;
            }
          }
        }
        if (added) {
          stilltau.add(target);
        }
      }
      taus.retainAll(stilltau.toNativeArray());
      it = taus.iterator();
      while (it.hasNext()) {
        int target = it.next();
        if (statetransitions[i] != null) {
          statetransitions[i].removeAll(statetransitions[target]);
        }
      }
      stilltau.clear();
    }
    SETS.clear();
  }
  
  private static void generatepreds(THashSet<int[]>[] statetransitions,
                                    THashSet<int[]>[] predtransitions)
  {
    for (int source = 0; source < statetransitions.length; source++) {
      THashSet<int[]> trans = statetransitions[source];
      if (trans == null) {continue;}
      for (int[] tran : trans) {
        int target = tran[1]; int event = tran[0];
        THashSet<int[]> preds = predtransitions[target];
        if (preds == null) {
          preds = new THashSet<int[]>(1, ArrayHash.ARRAYHASH);
          predtransitions[target] = preds;
        }
        preds.add(new int[]{event, source});
      }
    }
  }
  
  private static void generatepreds(TIntHashSet[] tautransitions,
                                    TIntHashSet[] predtaus)
  {
    for (int source = 0; source < tautransitions.length; source++) {
      TIntHashSet trans = tautransitions[source];
      if (trans == null) {continue;}
      TIntIterator it = trans.iterator();
      while (it.hasNext()) {
        int target = it.next();
        TIntHashSet preds = predtaus[target];
        if (preds == null) {
          preds = new TIntHashSet(1);
          predtaus[target] = preds;
        }
        preds.add(source);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private static Set<TIntHashSet>[] annotatedGraph
    (THashSet<int[]>[] statetransitions,
     TIntHashSet[] tautransitions,
     boolean[] marked)
  {
    TIntHashSet[] activeEvents = new TIntHashSet[statetransitions.length];
    Set<TIntHashSet>[] annotations = new Set[statetransitions.length];
    TIntArrayList stilltau = new TIntArrayList();
    for (int i = 0; i < tautransitions.length; i++) {
      TIntHashSet taus = tautransitions[i];
      if (taus == null) {
        continue;
      }
      tautransitions[i] = null;
      TIntIterator it = taus.iterator();
      stilltau.clear();
      while (it.hasNext()) {
        int target = it.next();
        TIntHashSet ae = activeEvents[target];
        if (ae == null) {
          ae = getActiveEvents(statetransitions[target], marked, target);
          activeEvents[target] = ae;
        }
        addTransitions(statetransitions, target, i);
        boolean added = true;
        boolean subset = false;
        for (int j = 0; j < stilltau.size(); j++) {
          int othertau = stilltau.get(j);
          TIntHashSet ae2 = activeEvents[othertau];
          if (ae2.size() < ae.size()) {
            if (subset) {
              continue;
            }
            if (ae.containsAll(ae2.toArray())) {
              added = false;
              break;
            }
          } else {
            if (ae2.containsAll(ae.toArray())) {
              stilltau.remove(j);
              subset = true;
            }
          }
        }
        if (added) {
          stilltau.add(target);
        }
      }
      annotations[i] = new THashSet<TIntHashSet>(stilltau.size());
      for (int j = 0; j < stilltau.size(); j++) {
        int target = stilltau.get(j); annotations[i].add(activeEvents[target]);
      }
      stilltau.clear();
    }
    SETS.clear();
    return annotations;
  }
  
  private static Set<TIntHashSet> merge(Set<TIntHashSet> an1, 
                                        Set<TIntHashSet> an2)
  {
    Iterator<TIntHashSet> it1 = an1.iterator();
    OUTERLOOP:
    while (it1.hasNext()) {
      TIntHashSet annotation1 = it1.next();
      Iterator<TIntHashSet> it2 = an2.iterator();
      boolean subset = false;
      while (it2.hasNext()) {
        TIntHashSet annotation2 = it2.next();
        if (!subset) {
          if (annotation1.size() == annotation2.size()) {
            if (annotation1.equals(annotation2)) {
              it1.remove(); continue OUTERLOOP;
            }
          } else if (annotation1.size() > annotation2.size()) {
            if (annotation1.containsAll(annotation2.toArray())) {
              it1.remove(); continue OUTERLOOP;
            }
          }
        }
        if (annotation1.size() < annotation2.size()) {
          if (annotation2.containsAll(annotation1.toArray())) {
            it2.remove(); subset = true;
          }
        }
      }
    }
    an2.addAll(an1);
    return an2;
  }
  
  @SuppressWarnings("unchecked")
  private static void equivalentIncoming(THashSet<int[]>[] statetransitions,
                                         Set<TIntHashSet>[] annotations,
                                         boolean[] marked, boolean[] initial)
  {
    THashSet<int[]>[] preds = new THashSet[statetransitions.length];
    generatepreds(statetransitions, preds);
    boolean[] onstack = new boolean[marked.length];
    TIntStack stack = new TIntStack(statetransitions.length);
    for (int i = 0; i < statetransitions.length; i++) {
      stack.push(i); onstack[i] = true;
    }
    while(stack.size() != 0) {
      int state = stack.pop(); onstack[state] = false;
      if (preds[state] == null) {continue;}
      for (int other = 0; other < preds.length; other++) {
        if (other == state) {continue;}
        if (preds[other] == null && !initial[other]) {continue;}
        if (initial[state] == initial[other] &&
            preds[state].equals(preds[other])) {
          Set<TIntHashSet> an1, an2;
          if (annotations[state] == null) {
            an1 = new THashSet<TIntHashSet>();
            an1.add(getActiveEvents(statetransitions[state], marked, state));
          } else {
            an1 = annotations[state];
          }
          if (annotations[other] == null) {
            an2 = new THashSet<TIntHashSet>();
            an2.add(getActiveEvents(statetransitions[other], marked, other));
          } else {
            an2 = annotations[other];
          }
          annotations[state] = merge(an1, an2);
          annotations[other] = null;
          marked[state] = marked[state] || marked[other];
          movetransitions(statetransitions, preds, state, other, stack, onstack);
          removePreds(preds, statetransitions, other, initial);
        }
      }
    }
  }
  
  private static void movetransitions(THashSet<int[]>[] statetransitions,
                                      THashSet<int[]>[] preds, int state,
                                      int other, TIntStack stack,
                                      boolean[] onstack)
  {
    THashSet<int[]> trans1 = statetransitions[state];
    THashSet<int[]> trans2 = statetransitions[other];
    if (trans2 == null) {return;}
    for (int[] tran : trans2) {
      trans1.add(tran);
      int target = tran[1]; int event = tran[0];
      THashSet<int[]> pred = preds[target];
      pred.remove(new int[] {event, other});
      pred.add(new int[] {event, state});
      if (!onstack[target]) {onstack[target] = true; stack.push(target);}
    }
  }
  
  private static void removePreds(THashSet<int[]>[] preds,
                                  THashSet<int[]>[] statetransitions, int other,
                                  boolean[] initial)
  {
    THashSet<int[]> pre = preds[other];
    for (int[] tran : pre) {
      int p = tran[1]; int e = tran[0];
      THashSet<int[]> succ = statetransitions[p];
      succ.remove(new int[] {e, other});
    }
    preds[other] = null;
    initial[other] = false;
  }
  
  private static class CertainConflict
  {
    private final THashSet<int[]>[] mStatetransitions;
    private final TIntHashSet[] mTautransitions;
    private final TIntHashSet[] mPreds;
    private boolean[] mMarked;
    private boolean[] mReachable;
    private boolean[] mIsInitial;
    
    public CertainConflict(THashSet<int[]>[] statetransitions,
                           TIntHashSet[] tautransitions,
                           boolean[] marked, boolean isInitial[])
    {
      mStatetransitions = statetransitions;
      mTautransitions = tautransitions;
      mPreds = new TIntHashSet[mTautransitions.length];
      mMarked = marked;
      mReachable = new boolean[mMarked.length];
      mIsInitial = isInitial;
      generatepreds(mStatetransitions, mTautransitions, mPreds);
    }
    
    private static void generatepreds(THashSet<int[]>[] statetransitions,
                                      TIntHashSet[] tautransitions,
                                      TIntHashSet[] predtrans)
    {
      for (int source = 0; source < statetransitions.length; source++) {
        THashSet<int[]> trans = statetransitions[source];
        if (trans == null) {continue;}
        for (int[] tran : trans) {
          int target = tran[1]; int event = tran[0];
          TIntHashSet preds = predtrans[target];
          if (preds == null) {
            preds = new TIntHashSet();
            predtrans[target] = preds;
          }
          preds.add(source);
        }
      }
      for (int source = 0; source < tautransitions.length; source++) {
        TIntHashSet trans = tautransitions[source];
        if (trans == null) {continue;}
        TIntIterator it = trans.iterator();
        while (it.hasNext()) {
          int target = it.next();
          TIntHashSet preds = predtrans[target];
          if (preds == null) {
            preds = new TIntHashSet(1);
            predtrans[target] = preds;
          }
          preds.add(source);
        }
      }
    }
    
    private void backtrack(int state)
    {
      if(mReachable[state]) {return;} //already done this state
      mReachable[state] = true;
      TIntHashSet preds = mPreds[state];
      if (preds == null) {return;}
      TIntIterator it = preds.iterator();
      while (it.hasNext()) {//mark all state which can reach this state as reachable
        int pred = it.next(); backtrack(pred);
      }
    }
    
    public void run()
    {
      for (int state = 0; state < mMarked.length; state++) {
        if (mMarked[state]) {
          backtrack(state);
        }
      }
      int dumpstate = -1;
      TIntHashSet redirect = new TIntHashSet();
      TIntStack stack = new TIntStack();
      for (int state = 0; state < mReachable.length; state++) {
        if (!mReachable[state]) {
          dumpstate = dumpstate == -1 ? state : dumpstate;
          redirect.add(state); // all transitions leading to this state will be redirected here
          stack.push(state);
        }
      }
      while (stack.size() != 0) {
        int state = stack.pop();
        mStatetransitions[state] = null;
        mTautransitions[state] = null;
        if (mIsInitial[state]) {
          for (int i = 0; i < mIsInitial.length; i++) {
            if (mIsInitial[i]) {
              if (redirect.add(i)) {
                stack.push(i);
              }
            }
            mIsInitial[i] = false;
          }
          mIsInitial[dumpstate] = true;
        }
        TIntHashSet preds = mPreds[state];
        if (preds == null) {continue;}
        TIntIterator it = preds.iterator();
        while (it.hasNext()) {
          int pred = it.next();
          if (redirect.contains(pred)) {
            continue; //has or will be nulled;
          }
          TIntHashSet taus = mTautransitions[pred];
          if (taus != null) {
            if (taus.contains(state)) {
              redirect.add(pred); stack.push(pred); continue; // is another dumpstate
            }
          }
          THashSet<int[]> succs = mStatetransitions[pred];
          if (succs == null) {continue;}
          Iterator<int[]> trans = succs.iterator();
          TIntHashSet todumpstate = new TIntHashSet(); //TODO don't make these objects often;
          List<int[]> tobeadded = new ArrayList<int[]>();
          while (trans.hasNext()) {
            int[] tran = trans.next();
            if (tran[1] == state) {
              tran = new int[]{tran[0], dumpstate}; it.remove(); tobeadded.add(tran);
              todumpstate.add(tran[0]);
            }
          }
          trans = succs.iterator();
          boolean alldump = true;
          while (trans.hasNext()) {
            int[] tran = trans.next();
            if (todumpstate.contains(tran[0])) {
              it.remove(); continue;
            }
            if (!redirect.contains(tran[1])) {
              alldump = false;
            }
          }
          if (alldump && (taus == null || taus.isEmpty())) {
            redirect.add(pred); stack.push(pred); continue; // is another dumpstate
          }
          succs.addAll(tobeadded);
        }
      }
    }
  }
  
  private static Set<Set<EventProxy>> getAnnotations(Collection<EventProxy> props)
  {
    Iterator<EventProxy> it = props.iterator();
    while (it.hasNext()) {
      EventProxy e = it.next();
      if (e instanceof AnnotationEvent) {
        AnnotationEvent a = (AnnotationEvent)e;
        return a.getAnnotations();
      }
    }
    return null;
  }
  /*
  public static AutomatonProxy unnanotateAutomaton(AutomatonProxy a, EventProxy marked,
                                                    ProductDESProxyFactory factory)
  {
    TObjectIntHashMap<StateProxy> stateToNumber =
      new TObjectIntHashMap<StateProxy>();
    TObjectIntHashMap<EventProxy> eventToNumber =
      new TObjectIntHashMap<EventProxy>();
    EventProxy[] events = new EventProxy[a.getEvents().size()];
    StateProxy[] origstates = new StateProxy[a.getStates().size()];
    int snumber = 0;
    for (StateProxy s : a.getStates()) {
      stateToNumber.put(s, snumber); origstates[snumber] = s; snumber++;
    }
    int enumber = 0;
    for (EventProxy e : a.getEvents()) {
      eventToNumber.put(e, enumber); events[enumber] = e; enumber++;
    }
    TIntArrayList[][] transitions = new TIntArrayList[snumber][enumber];
    TIntHashSet[] activeEvents = new TIntHashSet[snumber];
    Map<TIntHashSet, TIntArrayList> statesWithAnnotation = 
      new THashMap<TIntHashSet, TIntArrayList>();
    Collection<TransitionProxy> newTransitions = new ArrayList<TransitionProxy>();
    List<StateProxy> nextStates = new ArrayList<StateProxy>();
    for (TransitionProxy t : a.getTransitions()) {
      int s = stateToNumber.get(t.getSource());
      int e = eventToNumber.get(t.getEvent());
      int ta = stateToNumber.get(t.getTarget());
      TIntArrayList succs = transitions[s][e];
      if (succs == null) {
        succs = new TIntArrayList();
        transitions[s][e] = succs;
      }
      succs.add(ta);
      TIntHashSet ae = activeEvents[s];
      if (ae == null) {
        ae = new TIntHashSet();
        activeEvents[s] = ae;
        if (t.getSource().getPropositions().contains(marked)) {
          eventToNumber.get(marked);
        }
      }
      ae.add(e);
    }
    for (StateProxy state : a.getStates()) {
      int s = stateToNumber.get(state);
      Set<Set<EventProxy>> annotations = getAnnotations(state.getPropositions());
      if (annotations == null) {continue;}
      for (Set<EventProxy> ann : annotations) {
        TIntHashSet annint = new TIntHashSet(ann.size());
        for (EventProxy e : ann) {
          annint.add(eventToNumber.get(e));
        }
        TIntArrayList states = statesWithAnnotation.get(annint);
        if (states == null) {
          states = new TIntArrayList();
          statesWithAnnotation.put(annint, states);
        }
        states.add(s);
      }
    }
    TIntObjectHashMap<TIntArrayList> newStates =
      new TIntObjectHashMap<TIntArrayList>();
    // slightly confusing the source refers to state in new automaton
    // the successor refers to original
    Collection<EventProxy> markedcol = Collections.singleton(marked);
    Collection<EventProxy> notmarked = Collections.emptySet();
    List<int[]> newtransitionsI = new ArrayList<int[]>();
    int statenum = 0;
    for (int s = 0; s < snumber; s++) {
      TIntArrayList states = new TIntArrayList();
      states.add(statenum);
      newStates.put(s, states);
      for (int e = 0; e < transitions[s].length; e++) {
        TIntArrayList succs = transitions[s][e];
        if (succs == null) {continue;}
        for (int ti = 0; ti < succs.size(); ti++) {
          int t = succs.get(ti);
          newtransitionsI.add(new int[]{s, e, t});
        }
      }
      Collection<EventProxy> used = origstates[s].getPropositions().contains(marked) ?
                                    markedcol : notmarked;
      boolean isInitial = origstates[s].isInitial();
      StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
      nextStates.add(sp);
      statenum++;
      assert(statenum == nextStates.size());
    }
    for (TIntHashSet ann : statesWithAnnotation.keySet()) {
      TIntHashSet states = new TIntHashSet(statesWithAnnotation.get(ann).toNativeArray());
      while (!states.isEmpty()) {
        TIntObjectHashMap<TIntArrayList> indexToSuccessor =
          new TIntObjectHashMap<TIntArrayList>();
        TIntArrayList tocheck = new TIntArrayList(states.toArray());
        TIntIterator it = ann.iterator();
        Collection<EventProxy> used = notmarked;
        while (it.hasNext()) {
          int event = it.next();
          if (events[event] == marked) {
            used = markedcol;
            continue;
          }
          for (int its = 0; its < tocheck.size(); its++) {
            int s = tocheck.get(its);
            TIntArrayList succs = transitions[s][event];
            for (int suci = 0; suci < succs.size(); suci++) {
              int suc = succs.get(suci);
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
          TIntObjectIterator<TIntArrayList> it2 = indexToSuccessor.iterator();
          while (it2.hasNext()) {
            it2.advance();
            TIntArrayList list = it2.value();
            if (biggestlist == null || biggestlist.size() < list.size()) {
              biggestlist = list;
              bestsuc = it2.key();
            }
          }
          if (biggestlist == null) {
            System.out.println("number of things:" + indexToSuccessor.size());
          }
          newtransitionsI.add(new int[]{statenum, event, bestsuc});
          tocheck = biggestlist;
          indexToSuccessor.clear();
        }
        boolean isInitial = false;
        for (int its = tocheck.size() - 1; its >= 0; its--) {
          int state = tocheck.get(its);
          TIntArrayList sharedstates = newStates.get(state);
          if (sharedstates == null) {
            sharedstates = new TIntArrayList();
            newStates.put(state, sharedstates);
          }
          isInitial = origstates[state].isInitial() ? true : isInitial;
          sharedstates.add(statenum);
          states.remove(state);
        }
        StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
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
      EventProxy event = events[e];
      TIntArrayList targets = newStates.get(ot);
      for (int ti = 0; ti < targets.size(); ti++) {
        int ta = targets.get(ti);
        StateProxy target = nextStates.get(ta);
        newTransitions.add(factory.createTransitionProxy(source, event, target));
      }
    }
    return factory.createAutomatonProxy(a.getName(),
                                        ComponentKind.PLANT,
                                        a.getEvents(),
                                        nextStates, newTransitions);
  }
  */
  public static AutomatonProxy unnanotateAutomaton(AutomatonProxy a, EventProxy marked,
                                                    ProductDESProxyFactory factory)
  {
    Map<StateProxy, Collection<TransitionProxy>> transitions =
      new THashMap<StateProxy, Collection<TransitionProxy>>();
    Map<StateProxy, Set<EventProxy>> activeEvents = 
      new THashMap<StateProxy, Set<EventProxy>>();
    Collection<TransitionProxy> newTransitions = new ArrayList<TransitionProxy>();
    Collection<StateProxy> nextStates = new ArrayList<StateProxy>();
    for (TransitionProxy t : a.getTransitions()) {
      Collection<TransitionProxy> trans = transitions.get(t.getSource());
      if (trans == null) {
        trans = new ArrayList<TransitionProxy>();
        transitions.put(t.getSource(), trans);
      }
      trans.add(t);
      Set<EventProxy> ae = activeEvents.get(t.getSource());
      if (ae == null) {
        ae = new THashSet<EventProxy>();
        activeEvents.put(t.getSource(), ae);
        if (t.getSource().getPropositions().contains(marked)) {
          ae.add(marked);
        }
      }
      ae.add(t.getEvent());
    }
    Map<StateProxy, Collection<StateProxy>> newStates =
      new THashMap<StateProxy, Collection<StateProxy>>();
    int statenum = 0;
    Collection<EventProxy> markedcol = Collections.singleton(marked);
    Collection<EventProxy> notmarked = Collections.emptySet();
    for (StateProxy s : a.getStates()) {
      Set<Set<EventProxy>> annotations = getAnnotations(s.getPropositions());
      Collection<StateProxy> ns = new ArrayList<StateProxy>();
      boolean isInitial = s.isInitial();
      if (annotations == null) {
        StateProxy state = new AnnotatedMemStateProxy(statenum,
                                                      s.getPropositions(),
                                                      isInitial);
        statenum++;
        ns.add(state);
        activeEvents.put(state, activeEvents.get(s));
      } else {
        Set<EventProxy> active = activeEvents.get(s);
        active = active == null ? new THashSet<EventProxy>() :
                                  new THashSet<EventProxy>(activeEvents.get(s));
        for (Set<EventProxy> act : annotations) {
          Collection<EventProxy> props = act.contains(marked) ? markedcol
                                                              : notmarked;
                                         
          StateProxy state = new AnnotatedMemStateProxy(statenum, props,
                                                        isInitial);
          statenum++;
          ns.add(state);
          activeEvents.put(state, act);
          active.removeAll(act);
        }
        if (!active.isEmpty()) {
          Collection<EventProxy> props = s.getPropositions().contains(marked) ?
                                         markedcol : notmarked;
          StateProxy state = new AnnotatedMemStateProxy(statenum, props,
                                                        isInitial);
          statenum++;
          ns.add(state);
          activeEvents.put(state, activeEvents.get(s));
        }
      }
      newStates.put(s, ns);
      nextStates.addAll(ns);
    }
    for (TransitionProxy t : a.getTransitions()) {
      Collection<StateProxy> possibleSources = newStates.get(t.getSource());
      Collection<StateProxy> targets = newStates.get(t.getTarget());
      EventProxy event = t.getEvent();
      for (StateProxy source : possibleSources) {
        if (!activeEvents.get(source).contains(event)) {continue;}
        for (StateProxy target : targets) {
          newTransitions.add(factory.createTransitionProxy(source, event,
                                                           target));
        }
      }
    }
    return factory.createAutomatonProxy(a.getName(),
                                        ComponentKind.PLANT,
                                        a.getEvents(),
                                        nextStates, newTransitions);
  }
  
  private static class ArrayHash
    implements TObjectHashingStrategy<int[]>
  {
    static ArrayHash ARRAYHASH = new ArrayHash();

    public int computeHashCode(int[] arr)
    {
      int hashcode = 7;
      for (int i = 0 ; i < arr.length; i++) {
        hashcode = hashcode * 31 + arr[i];
      }
      return hashcode;
    }
    
    public boolean equals(int[] arr1, int[] arr2)
    {
      if (arr1.length != arr2.length) {
        return false;
      }
      for (int i = 0; i < arr1.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    }
  }
}
