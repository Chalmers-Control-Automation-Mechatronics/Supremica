//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   Minimizer
//###########################################################################
//# $Id: Minimizer.java,v 1.2 2007-07-21 06:28:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


public class Minimizer
{
  
  //#########################################################################
  //# Constructor
  @SuppressWarnings("unchecked")
  public Minimizer(final AutomatonProxy automaton,
                   final ProductDESProxyFactory factory)
  {
    mAutomaton = automaton;
    mFactory = factory;
    mStates = new StateProxy[automaton.getStates().size()];
    mStateToPart = new int[mStates.length];
    mEvents = new EventProxy[automaton.getEvents().size()];
    mEventMap = new HashMap<EventProxy, Integer>(mEvents.length);
    Map<StateProxy, Integer> stateMap =
      new HashMap<StateProxy, Integer>(mStates.length);
    mTransitionsSucc = new int[mStates.length][mEvents.length];
    mTransitionsPred = new HashSet[mStates.length][mEvents.length];
    automaton.getStates().toArray(mStates);
    automaton.getEvents().toArray(mEvents);
    for (int i = 0; i < mTransitionsPred.length; i++) {
      for (int j = 0; j < mTransitionsPred[i].length; j++) {
        mTransitionsPred[i][j] = new HashSet<Integer>(0);
        mTransitionsSucc[i][j] = -1;
      }
    }
    mPartitions = new ArrayList<Set<Integer>>(mStates.length);
    Set<Integer> firstpartition = new HashSet<Integer>(mStates.length);
    for (int i = 0; i < mEvents.length; i++) {
      mEventMap.put(mEvents[i], i);
    }
    for (int i = 0; i < mStates.length; i++) {
      firstpartition.add(i);
      stateMap.put(mStates[i], i);
    }
    mPartitions.add(firstpartition);
    for (TransitionProxy t : automaton.getTransitions()) {
      mTransitionsSucc[stateMap.get(t.getSource())][mEventMap.get(t.getEvent())]
        = stateMap.get(t.getTarget());
      mTransitionsPred[stateMap.get(t.getTarget())][mEventMap.get(t.getEvent())]
        .add(stateMap.get(t.getSource()));
    }
  }
  
  public AutomatonProxy run()
  {
    int size = (int)(mPartitions.size() * 1.5);
    while (true) {
      partition();
      if (mPartitions.size() <= size) {
        break;
      }
      size = (int)(mPartitions.size() * 1.5);
    }
    LinkedList<int[]> L = new LinkedList<int[]>();
    for (int i = 0; i < mPartitions.size(); i++) {
      for (int j = 0; j < mEvents.length; j++) {
        L.offer(new int[] {i, j});
      }
    }
    while (!L.isEmpty()) {
      int[] tup = L.poll();
      int part = tup[0];
      int event = tup[1];
      Set<Integer> parts = new HashSet<Integer>(mPartitions.size());
      for (Integer state : mPartitions.get(part)) {
        Set<Integer> predecesorstates = mTransitionsPred[state][event];
        for (Object predstate : predecesorstates) {
          parts.add(mStateToPart[(Integer)predstate]);
        }
      }
      Map<Integer, Integer> split = new HashMap<Integer, Integer>(parts.size());
      for (Integer partition : parts) {
        Set<Integer> newPart = new HashSet<Integer>();
        Iterator<Integer> it = mPartitions.get(partition).iterator();
        while (it.hasNext()) {
          int state = it.next();
          int successorstate = mTransitionsSucc[state][event];
          int successorstatepart = successorstate == -1 ? -1 :
                                                mStateToPart[successorstate];
          if (successorstatepart != part) {
            newPart.add(state);
            it.remove();
          }
        }
        if (!newPart.isEmpty()) {
          split.put(partition, mPartitions.size());
          mPartitions.add(newPart);
        }
      }
      if (!split.isEmpty()) {
        Map<Integer, Set<Integer>> events = new HashMap<Integer, Set<Integer>>(split.size());
        for (Integer partition : split.keySet()) {
          events.put(partition, allTo(mEvents.length));
        }
        for (Integer partition : split.values()) {
          for (Integer state : mPartitions.get(partition)) {
            mStateToPart[state] = partition;
          }
        }
        ListIterator<int[]> it = L.listIterator();
        while (it.hasNext()) {
          tup = it.next();
          if (split.containsKey(tup[0])) {
            int newpart = split.get(tup[0]);
            int[] newtup = new int[] {newpart, tup[1]};
            events.get(tup[0]).remove(tup[1]);
            it.add(newtup);
          }
        }
        for (Map.Entry<Integer, Set<Integer>> entry : events.entrySet()) {
          int partitionnumber = entry.getKey();
          int oldsize = mPartitions.get(partitionnumber).size();
          int newsize = mPartitions.get(split.get(partitionnumber)).size();
          if (newsize < oldsize) {
            partitionnumber = split.get(partitionnumber);
          }
          for (Integer eventi : entry.getValue()) {
            L.offer(new int[] {partitionnumber, eventi});
          }
        }
      }
    }
    /*for (Set<Integer> p : mPartitions) {
      check(p);
    }*/
    AutomatonProxy minAut;
    if (mStates.length > mPartitions.size()) { 
      List<StateProxy> states = new ArrayList<StateProxy>(mPartitions.size());
      Collection<TransitionProxy> transitions = new ArrayList<TransitionProxy>();
      int initial = -1;
      for(int i = 0; i < mStates.length; i++) {
        if (mStates[i].isInitial()) {
          initial = i;
        }
      }
      assert(initial != -1);
      int j = 1;
      for (int i = 0; i < mPartitions.size(); i++) {
        if (mPartitions.get(i).contains(initial)) {
          states.add(new MemStateProxy(0));
        } else {
          states.add(new MemStateProxy(j));
          j++;
        }
      }
      for (int i = 0; i < mPartitions.size(); i++) {
        int sourcestate = mPartitions.get(i).iterator().next();
        for(int event = 0; event < mTransitionsSucc[sourcestate].length; event++) {
          int targetstate = mTransitionsSucc[sourcestate][event];
          if (targetstate != -1) {
            int sourcepartition = i;
            int targetpartition = mStateToPart[targetstate];
            TransitionProxy trans = mFactory.createTransitionProxy(
                                               states.get(sourcepartition),
                                               mEvents[event],
                                               states.get(targetpartition));
            transitions.add(trans);
          }
        }
      }
      minAut = mFactory.createAutomatonProxy(mAutomaton.getName(),
                                             mAutomaton.getKind(),
                                             mAutomaton.getEvents(), states,
                                             transitions);
    }
    else {
      minAut = mAutomaton;
    }
    System.out.println("Minimized:" + minAut.getStates().size());
    return minAut;
  }
  
  private void partition()
  {
    int start = mPartitions.size();
    int current = start;
    for (int i = 0; i < mPartitions.size(); i++) {
      if (mPartitions.get(i).size() > 1) {
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        Iterator<Integer> it = mPartitions.get(i).iterator();
        map.put(stateHashCode(it.next()), i);
        while (it.hasNext()) {
          int state = it.next();
          long hash = stateHashCode(state);
          int partition;
          if (map.containsKey(hash)) {
            partition = map.get(hash);
          } else {
            partition = current;
            current++;
            map.put(hash, partition);
            mPartitions.add(new HashSet<Integer>());
          }
          if (partition != i) {
            it.remove();
            mPartitions.get(partition).add(state);
          }
        }
      }
    }
    for (int i = start; i < mPartitions.size(); i++) {
      for (Integer j : mPartitions.get(i)) {
        mStateToPart[j] = i;
      }
    }
  }
  
  private long stateHashCode(int state)
  {
    long hashCode = 1;
    for(int i = 0; i < mTransitionsSucc[state].length; i++) {
      int successor = mTransitionsSucc[state][i];
      int successorPartition = successor == -1 ? -1 : mStateToPart[successor];
      successorPartition++;
      hashCode = 31 * hashCode + successorPartition * i;
    }
    return hashCode;
  }
  
  private static Set<Integer> allTo(int to)
  {
    Set<Integer> all = new HashSet<Integer>(to);
    for(int i = 0; i < to; i++)
    {
      all.add(i);
    }
    return all;
  }
  
  private void check(Set<Integer> partition) {
    Integer st = partition.iterator().next();
    int[] successors = new int[mEvents.length];
    for(int i = 0; i < mEvents.length; i++) {
      int succ = mTransitionsSucc[st][i];
      int succpart = succ == -1 ? -1 : mStateToPart[succ];
      successors[i] = succpart;
    }
    boolean broken = false;
    for (Integer state : partition) {
      int[] successors2 = new int[mEvents.length];
      for(int i = 0; i < mEvents.length; i++) {
        int succ = mTransitionsSucc[state][i];
        int succpart = succ == -1 ? -1 : mStateToPart[succ];
        successors2[i] = succpart;
      }
      if (!Arrays.equals(successors, successors2)) {
        System.out.println(st + "," + state);
        System.out.println(Arrays.toString(successors));
        System.out.println(Arrays.toString(successors2));
        broken = true;
        break;
      }
    }
    if (broken) {
      for(int i = 0; i < mTransitionsSucc.length; i++) {
        System.out.print(Integer.toString(i, Character.MAX_RADIX) + " ");
        for(int j = 0; j < mTransitionsSucc[i].length; j++) {
          int succ = mTransitionsSucc[i][j];
          System.out.print(Integer.toString(succ, Character.MAX_RADIX) + " ");
        }
        System.out.println();
      }
      int i = 0;
      System.out.println();
      for (Set<Integer> part : mPartitions) {
        System.out.println("part " + i + ":" + part);
        i++;
      }
      for (int j = 0; j < mStateToPart.length; j++) {
        System.out.println("state: " + j + " part: " + mStateToPart[j]);
      }
      System.exit(4);
    }
  }
  
  private static class MemStateProxy
    implements StateProxy
  {
    private final int mName;
    
    public MemStateProxy(int name)
    {
      mName = name;
    }
    
    public Collection<EventProxy> getPropositions()
    {
      return Collections.emptySet();
    }
    
    public boolean isInitial()
    {
      return mName == 0;
    }
    
    public MemStateProxy clone()
    {
      return new MemStateProxy(mName);
    }
    
    public String getName()
    {
      return Integer.toString(mName);
    }
    
    public boolean refequals(Object o)
    {
      if (o instanceof NamedProxy) {
        return refequals((NamedProxy) o);
      }
      return false;
    }
    
    public boolean refequals(NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mName == mName;
      } else {
        return false;
      }
    }
    
    public int refHashCode()
    {
      return mName;
    }
    
    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor =
        (ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    public boolean equalsByContents(final Proxy partner)
    {
      if (partner != null &&
          partner.getProxyInterface() == getProxyInterface()) {
        final StateProxy state = (StateProxy) partner;
        return (getName().equals(state.getName())) &&
               (isInitial() == state.isInitial()) &&
               state.getPropositions().isEmpty();
      } else {
        return false;
      }
    }
    
    public boolean equalsWithGeometry(Proxy o)
    {
      return equalsByContents(o);
    }
    
    public int hashCodeByContents()
    {
      return refHashCode();
    }
    
    public int hashCodeWithGeometry()
    {
      return refHashCode();
    }
    
    public int compareTo(NamedProxy n)
    {
      return getName().compareTo(n.getName());
    }
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mFactory;
  private AutomatonProxy mAutomaton;
  private StateProxy[] mStates;
  private int[] mStateToPart;
  private List<Set<Integer>> mPartitions;
  private int[][] mTransitionsSucc;
  private Set<Integer>[][] mTransitionsPred;
  private EventProxy[] mEvents;
  private Map<EventProxy,Integer> mEventMap;

}
