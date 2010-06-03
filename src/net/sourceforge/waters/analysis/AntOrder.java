//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id: ProjectingControllabilityChecker.java 4468 2008-11-01 21:54:58Z robi $
//###########################################################################

package net.sourceforge.waters.analysis;

import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.modular.BiSimulator;
import net.sourceforge.waters.analysis.modular.ConfRevBiSimulator;
import net.sourceforge.waters.analysis.modular.BlockedEvents;
import net.sourceforge.waters.analysis.modular.NonDeterministicComposer;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import gnu.trove.TObjectIntHashMap;
import java.util.Collections;
import net.sourceforge.waters.analysis.compnb.IncomingEquivalent;
import gnu.trove.TObjectDoubleHashMap;
import java.security.AccessControlException;
import net.sourceforge.waters.analysis.modular.TransBiSimulator;
import net.sourceforge.waters.analysis.annotation.SilentOutGoing;
import java.io.FileReader;
import java.io.BufferedReader;
import net.sourceforge.waters.analysis.annotation.MakeBisimiliar;
import net.sourceforge.waters.analysis.annotation.BiSimulatorRedundant;
import net.sourceforge.waters.analysis.annotation.RemoveEvents;
import net.sourceforge.waters.analysis.modular.Composer;
import net.sourceforge.waters.analysis.annotation.OptimisticBiSimulatorRedundant;
import org.omg.SendingContext.RunTime;
import java.lang.Runtime;
import java.util.Random;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TObjectDoubleProcedure;
import java.util.IdentityHashMap;
import gnu.trove.TObjectIdentityHashingStrategy;
import gnu.trove.TIntHashSet;


/**
 * The projectiong controllability check algorithm.
 *
 * @author Simon Ware
 */

public class AntOrder
{
  private static final long SEED = 564621;
  private static final int MAXTEMP = 100000;
  private static final int ANTS = 20;
  private static final double DISSIPATION = .8;
  
  private final ProductDESProxy mModel;
  private final Set<AutomatonProxy> mAutomata;
  private final TObjectDoubleHashMap<Set<AutomatonProxy>> mPheromone;
  private final TObjectDoubleHashMap<Set<AutomatonProxy>> mGuessed;
  private final THashMap<AutomatonProxy, Set<AutomatonProxy>> mSingletonMap;
  private final THashMap<Set<AutomatonProxy>, AutomatonProxy> mActual;
  private final THashMap<AutomatonProxy, Set<AutomatonProxy>> mActualToComp;
  private final THashMap<Set<AutomatonProxy>, Set<AutomatonProxy>> mCanonical;
  private final THashMap<Tuple, Set<AutomatonProxy>> mUnion;
  private final Map<EventProxy, Set<AutomatonProxy>> mLocal;
  private final Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> mTransitionMap;
  private final Random mRand;
  
  //#########################################################################
  //# Constructors
  public AntOrder(final ProductDESProxy model, Set<AutomatonProxy> automata)
  {
    mModel = model;
    mAutomata = automata;
    mPheromone = new TObjectDoubleHashMap<Set<AutomatonProxy>>(new TObjectIdentityHashingStrategy<Set<AutomatonProxy>>());
    mGuessed = new TObjectDoubleHashMap<Set<AutomatonProxy>>(new TObjectIdentityHashingStrategy<Set<AutomatonProxy>>());
    mActual = new THashMap<Set<AutomatonProxy>, AutomatonProxy>(new TObjectIdentityHashingStrategy<Set<AutomatonProxy>>());
    mActualToComp = new THashMap<AutomatonProxy, Set<AutomatonProxy>>();
    mRand = new Random(SEED);
    mCanonical = new THashMap<Set<AutomatonProxy>, Set<AutomatonProxy>>();
    mLocal = new THashMap<EventProxy, Set<AutomatonProxy>>();
    mTransitionMap = new THashMap<AutomatonProxy, TObjectIntHashMap<EventProxy>>();
    mSingletonMap = new THashMap<AutomatonProxy, Set<AutomatonProxy>>();
    mUnion = new THashMap<Tuple, Set<AutomatonProxy>>();
    Map<EventProxy, TIntHashSet> autwithevent = new THashMap<EventProxy, TIntHashSet>();
    for (AutomatonProxy a : mAutomata) {
      mSingletonMap.put(a, Collections.singleton(a));
      TObjectIntHashMap etrans = new TObjectIntHashMap<EventProxy>();
      mTransitionMap.put(a, etrans);
      for (EventProxy e : a.getEvents()) {
        Set<AutomatonProxy> auts = mLocal.get(e);
        if (auts == null) {
          auts = new THashSet<AutomatonProxy>();
          mLocal.put(e, auts);
        }
        auts.add(a);
      }
      for (TransitionProxy t : a.getTransitions()) {
        int tnum = etrans.get(t.getEvent());
        tnum++;
        etrans.put(t.getEvent(), tnum);
      }
    }
  }
  
  /*private double calculatedistance(Set<AutomatonProxy> comp)
  {
    if (!mGuessed.contains(comp)) {
      Set<EventProxy> events = new THashSet<EventProxy>();
      for (AutomatonProxy a : comp) {
        events.addAll(a.getEvents());
      }
      double numlocal = 1;
      for (EventProxy e : events) {
        if (comp.containsAll(mLocal.get(e))) {
          numlocal++;
        }
      }
      double distance = events.size() - numlocal;
      mGuessed.put(comp, distance);
    }
    return mGuessed.get(comp);
  }*/
  
  private double calculatedistance(Set<AutomatonProxy> comp)
  {
    if (!mGuessed.contains(comp)) {
      Set<EventProxy> events = new THashSet<EventProxy>();
      for (AutomatonProxy a : comp) {
        events.addAll(a.getEvents());
      }
      double num = 0;
      double local = 0;
      double numlocal = 1;
      for (EventProxy e : events) {
        double num2 = 1;
        for (AutomatonProxy a : comp) {
          if (!a.getEvents().contains(e)) {
            num2 *= (double)mTransitionMap.get(a).get(e);
          } else {
            num2 *= ((double)a.getStates().size() / (double)2);
          }
        }
        if (!comp.containsAll(mLocal.get(e))) {
          num += num2;
        } else {
          local += num2;
          numlocal++;
        }
      }
      double total = num + local;
      total = Math.log(total);
      numlocal = Math.log(numlocal);
      double distance = total / numlocal;
      mGuessed.put(comp, distance);
    }
    return mGuessed.get(comp);
  }
  
  public void setActual(Set<AutomatonProxy> comp, AutomatonProxy aut)
  {
    Iterator<AutomatonProxy> it = comp.iterator();
    AutomatonProxy a1 = it.next();
    AutomatonProxy a2 = it.next();
    Tuple t = new Tuple(getComp(a1), getComp(a2));
    Set<AutomatonProxy> acomp = mUnion.get(t);
    System.out.println(acomp.toString().substring(10));
    mActual.put(acomp, aut);
    if (aut != null) {
      mActualToComp.put(aut, acomp);
    } else {
      mPheromone.remove(comp);
    }
  }
  
  public Set<AutomatonProxy> getComp(AutomatonProxy aut)
  {
    if (mSingletonMap.contains(aut)) {return mSingletonMap.get(aut);}
    return mActualToComp.get(aut);
  }
  
  public Set<AutomatonProxy> selectComp(Set<AutomatonProxy> stateo)
  {
    List<Set<AutomatonProxy>> state = new ArrayList<Set<AutomatonProxy>>();
    for (AutomatonProxy a : stateo) {
      state.add(getComp(a));
    }
    Set<Set<AutomatonProxy>> best = new THashSet<Set<AutomatonProxy>>();
    double bestpheromone = -1;
    for (int i = 0; i < state.size(); i++) {
      for (int j = i + 1; j < state.size(); j++) {
        Tuple t = new Tuple(state.get(i), state.get(j));
        Set<AutomatonProxy> comp = mUnion.get(t);
        if (comp == null) {continue;}
        double pheromone = mPheromone.get(comp);
        if (pheromone > bestpheromone) {
          bestpheromone = pheromone;
          best.clear();
          best.add(state.get(i));
          best.add(state.get(j));
        }
      }
    }
    Set<AutomatonProxy> res = new THashSet<AutomatonProxy>();
    for (Set<AutomatonProxy> auts : best) {
      if (auts.size() == 1) {
        res.addAll(auts);
      } else {
        res.add(mActual.get(auts));
      }
    }
    return res;
  }
  
  private double calculateweight(Set<AutomatonProxy> comp)
  {
    double distance = 0;
    AutomatonProxy aut = mActual.get(comp);
    if (aut == null) {
      if (mActual.contains(comp)) {return 0;}
      distance = calculatedistance(comp);
    } else {
      distance = aut.getTransitions().size();
    }
    distance = 1 / distance;
    double pheromone = mPheromone.get(comp) + 1;
    return distance * pheromone;
  }
  
  private List<Set<AutomatonProxy>> antrun()
  {
    Set<Set<AutomatonProxy>> state = new THashSet<Set<AutomatonProxy>>();
    Map<Set<AutomatonProxy>, Set<Set<AutomatonProxy>>> possible = new IdentityHashMap<Set<AutomatonProxy>, Set<Set<AutomatonProxy>>>();
    for (AutomatonProxy aut : mAutomata) {
      state.add(mSingletonMap.get(aut));
      Set<Set<AutomatonProxy>> pos = new THashSet<Set<AutomatonProxy>>();
      for (EventProxy e : aut.getEvents()) {
        Collection<AutomatonProxy> sameevent = mLocal.get(e);
        for (AutomatonProxy aut2 : sameevent) {
          if (aut2 != aut) {pos.add(mSingletonMap.get(aut2));}
        }
      }
      possible.put(mSingletonMap.get(aut), pos);
    }
    List<Set<AutomatonProxy>> path = new ArrayList<Set<AutomatonProxy>>();
    while (state.size() != 1) {
      List<Set<AutomatonProxy>> comps = new ArrayList<Set<AutomatonProxy>>();
      TDoubleArrayList probs = new TDoubleArrayList();
      Set<Tuple> donetuples = new THashSet<Tuple>();
      List<Tuple> tuples = new ArrayList<Tuple>();
      double totalweight = 0;
      for (Set<AutomatonProxy> auts1 : state) {
        Set<Set<AutomatonProxy>> pos = possible.get(auts1);
        for (Set<AutomatonProxy> auts2 : pos) {
          Tuple t = new Tuple(auts1, auts2);
          if (!donetuples.add(t)) {continue;}
          Set<AutomatonProxy> comp = mUnion.get(t);
          if (comp == null) {
            comp = new THashSet<AutomatonProxy>(auts1);
            comp.addAll(auts2);
            Set<AutomatonProxy> canon = mCanonical.get(comp);
            if (canon == null) {
              canon = comp;
              mCanonical.put(canon, canon);
            }
            mUnion.put(t, comp);
          } else {
            //System.out.println("hashed");
          }
          double weight = calculateweight(comp);
          if (weight == 0) {continue;}
          totalweight += weight;
          comps.add(comp);
          probs.add(totalweight);
          tuples.add(t);
        }
      }
      if (tuples.isEmpty()) {
        return path;
      }
      double selection = mRand.nextDouble() * totalweight;
      int i = 0;
      for (; i < probs.size() - 1; i++) {
        double prob = probs.get(i);
        if (prob < selection) {
          break;
        }
      }
      Tuple tup = tuples.get(i);
      state.remove(tup.mSet1);
      state.remove(tup.mSet2);
      state.add(comps.get(i));
      path.add(comps.get(i));
      Set<Set<AutomatonProxy>> pos1 = possible.remove(tup.mSet1);
      Set<Set<AutomatonProxy>> pos2 = possible.remove(tup.mSet2);
      pos1.addAll(pos2);
      pos1.remove(tup.mSet1);
      pos1.remove(tup.mSet2);
      possible.put(comps.get(i), pos1);
      for (Set<AutomatonProxy> auts : pos1) {
        Set<Set<AutomatonProxy>> pos = possible.get(auts);
        pos.remove(tup.mSet1);
        pos.remove(tup.mSet2);
        pos.add(comps.get(i));
      }
    }
    return path;
  }
  
  private void pheromonedissipation()
  {
    mPheromone.forEachEntry(new TObjectDoubleProcedure<Set<AutomatonProxy>>() {
      public boolean execute(Set<AutomatonProxy> comp, double pheromone) {
        pheromone *= DISSIPATION;
        mPheromone.put(comp, pheromone);
        return true;
      }
    });
  }
  
  private void addpheromone(List<Set<AutomatonProxy>> path)
  {
    double totaldistance = 0;
    for (Set<AutomatonProxy> comp : path) {
      totaldistance += calculatedistance(comp);
    }
    double pheromoneupdate = 1 / totaldistance;
    for (Set<AutomatonProxy> comp : path) {
      double pheromone = mPheromone.get(comp);
      pheromone += pheromoneupdate;
      mPheromone.put(comp, pheromone);
    }
  }
  
  public void run(int times)
  {
    for (int i = 0; i < times; i++) {
      if (i % 1 == 0) {System.out.println("times: " + i);}
      List<List<Set<AutomatonProxy>>> paths = new ArrayList<List<Set<AutomatonProxy>>>();
      for (int j = 0; j < ANTS; j++) {
        paths.add(antrun());
      }
      pheromonedissipation();
      for (int j = 0; j < ANTS; j++) {
        addpheromone(paths.get(j));
      }
    }
  }
  
  private static class Tuple
  {
    private final Set<AutomatonProxy> mSet1;
    private final Set<AutomatonProxy> mSet2;
    
    public Tuple(Set<AutomatonProxy> set1, Set<AutomatonProxy> set2)
    {
      if (System.identityHashCode(set1) < System.identityHashCode(set2)) {
        mSet1 = set1;
        mSet2 = set2;
      } else {
        mSet1 = set2;
        mSet2 = set1;
      }
    }
    
    public int hashCode() {
      int hash = 13;
      hash += System.identityHashCode(mSet1);
      hash *= 7;
      hash += System.identityHashCode(mSet2);
      return hash;
    }
    
    public boolean equals(Object o) {
      Tuple t = (Tuple) o;
      return mSet1 == t.mSet1 && mSet2 == t.mSet2;
    }
  }
}
