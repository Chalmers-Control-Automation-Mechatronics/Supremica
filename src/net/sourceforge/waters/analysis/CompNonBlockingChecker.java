//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id: ProjectingControllabilityChecker.java 4468 2008-11-01 21:54:58Z robi $
//###########################################################################

package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;

import java.lang.Comparable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.analysis.modular.ObserverProjection;
import net.sourceforge.waters.analysis.modular.NonDeterministicComposer;
import net.sourceforge.waters.analysis.modular.BiSimulator;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import net.sourceforge.waters.analysis.LightWeightGraph;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import gnu.trove.THashSet;
import gnu.trove.THashMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import gnu.trove.TObjectIntHashMap;
import net.sourceforge.waters.analysis.modular.BlockedEvents;
import net.sourceforge.waters.analysis.compnb.IncomingEquivalent;
import gnu.trove.TObjectDoubleHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import net.sourceforge.waters.analysis.modular.TransBiSimulator;


/**
 * The projectiong controllability check algorithm.
 *
 * @author Simon Ware
 */

public class CompNonBlockingChecker
  extends AbstractConflictChecker
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public CompNonBlockingChecker(final ProductDESProxy model,
                                final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mStates = 0;
    setNodeLimit(10000000);
  }

  private void eventscheck(ProductDESProxy model)
  {
    Collection<AutomatonProxy> automata = model.getAutomata();
    mAllSelfLoops.clear();
    mAllwaysEnabled.clear();
    for (EventProxy event : model.getEvents()) {
      mAllSelfLoops.put(event, new THashSet<AutomatonProxy>());
      mAllwaysEnabled.put(event, new THashSet<AutomatonProxy>());
      for (AutomatonProxy aut : automata) {
        if (aut.getEvents().contains(event)) {
          mAllSelfLoops.get(event).add(aut);
          mAllwaysEnabled.get(event).add(aut);
        }
      }
    }
    for (AutomatonProxy auto : automata) {
      TransitionRelation tr = new TransitionRelation(auto, getMarkingProposition());
      Collection<EventProxy> allselflooped = tr.getAllSelfLoops();
      for (EventProxy event : allselflooped) {
        mAllSelfLoops.get(event).remove(auto);
      }
      Collection<EventProxy> allwaysenabled = tr.getAllwaysEnabled();
      for (EventProxy event : allwaysenabled) {
        mAllwaysEnabled.get(event).remove(auto);
      }
    }
  }
  
  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    System.out.println("run comp conf");
    clearStats();
    mTime -= System.currentTimeMillis();
    if (getMarkingProposition() == null) {
      setMarkingProposition(getUsedMarkingProposition());
    }
    for (AutomatonProxy a : getModel().getAutomata()) {
      //System.out.println(a.getName() + " " + a.getStates().size());
    }
    boolean result = false;
    double checkerstates = 0;
    try {
      ProjectionList list = project(getModel());
      mMinAutMap.clear();
      //System.out.println(list);
      if (list == null) {
        return true;
      }
      //System.out.println(list.getModel());
      ConflictChecker checker = 
        new NativeConflictChecker(list.getModel(), getMarkingProposition(),
                                  getFactory());
      result = checker.run();
      checkerstates = checker.getAnalysisResult().getTotalNumberOfStates();
    } catch (CertainConflictException cce) {
      //System.out.println("caught:" + cce);
      result = false;
    }
    if (!result) {
      List<EventProxy> e = new ArrayList<EventProxy>();
      TraceProxy counter = getFactory().createSafetyTraceProxy(getModel().getName(),
                                                               getModel(), e);
      setFailedResult(counter);
    } else {
      setSatisfiedResult();
    }
    System.out.println("result: " + result);
    System.out.println("checkerstates: " + checkerstates);
    mTime += System.currentTimeMillis();
    try {
      BufferedWriter write = new BufferedWriter(new FileWriter("/home/darius/Projects/supr/supremica/hugo/" + getModel().getName()));
      write.append(getStats());
      write.append("states:" + checkerstates + "\n");
      write.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    //System.out.println("Total Switched: " + switched);
    clearStats();
    return result;
    /*if (checker.run()) {
      mStates += checker.getAnalysisResult().getTotalNumberOfStates();
      setSatisfiedResult();
      return true;
    } else {
      mStates += checker.getAnalysisResult().getTotalNumberOfStates();
      TraceProxy counter = checker.getCounterExample();
      counter = list.getTrace(counter, model);
      List<EventProxy> e = counter.getEvents();
      counter = getFactory().createSafetyTraceProxy(getModel().getName(),
                                                    getModel(),
                                                    e.subList(0, e.size() - 1));
      setFailedResult(counter);
      return false;
    }*/
  }
  
  public ConflictTraceProxy getCounterExample()
  {
    return null;
  }
  
  private void clearStats()
  {
    mLargestComposition = 0;
    AnnotateGraph.clearStats();
    CertainConflict.clearStats();
    EquivalentIncoming.clearStats();
    RemoveFollowOnTau.clearStats();
    TauLoopRemoval.clearStats();
    UnAnnotateGraph.clearStats();
    IncomingEquivalent.clearStats();
    RemoveImpossibleTransitions.clearStats();
    MergeEvents.clearStats();
    mAnnotatedBISIMulation = 0;
    mBISIMulation = 0;
    mCompTime = 0;
    mAnnBITIME = 0;
    mBITIME = 0;
    mSmallestDiff = Integer.MAX_VALUE;
    mLargestDiff = Integer.MIN_VALUE;
    mAggDiff = 0;
    mTime = 0;
    mLargestTransitions = 0;
    mAggComposition = 0;
    mAggTransitions = 0;
  }
  
  public String getStats()
  {
    String stats = getModel().getName() + "\n";
    stats = AnnotateGraph.stats() + "\n";
    stats += CertainConflict.stats() + "\n";
    stats += TauLoopRemoval.stats()  + "\n";
    stats += IncomingEquivalent.stats() + "\n";
    stats += "mAggDiff = " + mAggDiff + " mSmallestDiff = " + mSmallestDiff + " mLargestDiff = " + mLargestDiff + "\n";
    stats += "Largest Composition: " + mLargestComposition + " COMPTIME: " + mCompTime + "\n";
    stats += "Largest Transitions: " + mLargestTransitions + " aggcomp: " + mAggComposition + " aggtrans: " + mAggTransitions + "\n";
    stats += "Annotated Bisimulation: " + mAnnotatedBISIMulation + " mAnnBITIME: " + mAnnBITIME + "\n";
    stats += "Bisimulation: " + mBISIMulation + " mBITIME: " + mBITIME + "\n";
    stats += "Time: " + mTime + "\n";
    return stats;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifier
  public void setNodeLimit(final int limit)
  {
    super.setNodeLimit(limit);
    if (limit > 500000) {
      mMaxProjStates = 500000;
    } else {
      mMaxProjStates = limit;
    }
  }

  protected void addStatistics(final VerificationResult result)
  {
    result.setNumberOfStates(mStates);
  }
  
  /*private Set<Set<AutomatonProxy>> getMaxAutomaton(Set<AutomatonProxy> automata)
  {
    Set<Set<AutomatonProxy>> pairs = new THashSet<Set<AutomatonProxy>>();
    AutomatonProxy maxaut = null;
    for (AutomatonProxy aut : automata) {
      maxaut = maxaut == null ? aut : maxaut;
      maxaut = maxaut.getStates.size() < aut.getStates().size() ? aut : maxaut;
    }
    for (AutomatonProxy aut : automata) {
      if (maxaut == aut) {continue;}
      Set<AutomatonProxy> pair = new THashSet<AutomatonProxy>(2);
      pair.add(maxaut);
      pair.add(aut);
      pairs.add(pair);
    }
    return pairs;
  }*/
  
  private Collection<Set<AutomatonProxy>> getMinTransitions(final ProductDESProxy model, final Set<AutomatonProxy> automata)
  {
    List<Set<AutomatonProxy>> pairs = new ArrayList<Set<AutomatonProxy>>();
    AutomatonProxy minaut = null;
    for (AutomatonProxy aut : automata) {
      minaut = minaut == null ? aut : minaut;
      minaut = minaut.getTransitions().size() > aut.getTransitions().size() ? aut : minaut;
    }
    for (AutomatonProxy aut : automata) {
      if (minaut == aut) {continue;}
      Set<AutomatonProxy> pair = new THashSet<AutomatonProxy>(2);
      pair.add(minaut);
      pair.add(aut);
      pairs.add(pair);
    }
    final TObjectIntHashMap<Set<AutomatonProxy>> numoccuring =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    for (EventProxy e : model.getEvents()) {
      if (e == getMarkingProposition()) {
        continue;
      }
      Set<AutomatonProxy> possess = new THashSet<AutomatonProxy>();
      for (AutomatonProxy a : automata) {
        if (a.getEvents().contains(e)) {
          possess.add(a);
        }
      }
      if (!possess.isEmpty()) {
        /*if (numoccuring.get(possess) == null) {
          numoccuring.put(possess, 0);
        }*/
        numoccuring.put(possess, numoccuring.get(possess) + 1);
      }
    }
    Collections.sort(pairs, new Comparator<Set<AutomatonProxy>>() {
        public int compare(Set<AutomatonProxy> a1, Set<AutomatonProxy> a2)
        {
          int local1 = numoccuring.get(a1);
          int local2 = numoccuring.get(a2);
          if (local1 != local2) {return local2 - local1;}
          Collection<EventProxy> CommonEvents1 = new THashSet<EventProxy>(model.getEvents());
          Collection<EventProxy> CommonEvents2 = new THashSet<EventProxy>(model.getEvents());
          for (AutomatonProxy aut : a1) {
            CommonEvents1.retainAll(aut.getEvents());
          }
          for (AutomatonProxy aut : a2) {
            CommonEvents2.retainAll(aut.getEvents());
          }
          return CommonEvents2.size() - CommonEvents1.size();
        }
    });
    return pairs;
  }
  
  /*private Set<Tuple> getTuples(ProductDESProxy model, Set<AutomatonProxy> automata)
  {
    THashMap<AutomatonProxy, PriorityQueue<Tuple>> queues = new THashMap<AutomatonProxy, PriorityQueue<Tuple>>();
    Set<Tuple> possible = new TreeSet<Tuple>();
    for (AutomatonProxy a1 : automata) {
      queues.put(a1, new PriorityQueue<Tuple>());
      for (AutomatonProxy a2 : automata) {
        if (a1 == a2) {continue;}
        Set<AutomatonProxy> auts = new HashSet<AutomatonProxy>(2);
        auts.add(a1); auts.add(a2);
        double size = 0;
        Set<EventProxy> common = new HashSet<EventProxy>(model.getEvents());
        Set<EventProxy> total = new HashSet<EventProxy>();
        boolean contproj = false;
        int taus = 0;
        for (AutomatonProxy a : auts) {
          size += Math.log(a.getStates().size());
          total.addAll(a.getEvents());
          common.retainAll(a.getEvents());
          for (EventProxy e : a.getEvents()) {
            if (e.getName().startsWith("tau:")) {
              taus++;
            }
          }
        }
        double tot = 1;
        double uncom = 1;
        for (AutomatonProxy a : auts) {
          int uncom1 = 0;
          int tot1 = 0;
          for (TransitionProxy t : a.getTransitions()) {
            if (!common.contains(t.getEvent())) {
              uncom1++;
            }
            tot1++;
          }
          tot *= tot1;
          uncom *= uncom1;
        }
        size = uncom / tot;
        queues.get(a1).offer(new Tuple(auts, size));
      }
    }
    for (AutomatonProxy a : queues.keySet()) {
      PriorityQueue<Tuple> queue = queues.get(a);
      if (queue.isEmpty()) {continue;}
      if (queue.size() == 1) {possible.add(queue.poll()); continue;}
      Tuple tup1 = queue.poll();
      Tuple tup2 = queue.poll();
      possible.add(new Tuple(tup1.mSet, tup1.mSize - tup2.mSize));
    }
    return possible;
  }*/
  
  
  /*private Set<Tuple> getTuples(ProductDESProxy model, Set<AutomatonProxy> automata)
  {
    Set<Tuple> possible = new TreeSet<Tuple>(); 
    for (AutomatonProxy a1 : automata) {
      for (AutomatonProxy a2 : automata) {
        if (a1 == a2) {continue;}
        Set<AutomatonProxy> auts = new HashSet<AutomatonProxy>(2);
        auts.add(a1); auts.add(a2);
        double size = 0;
        Set<EventProxy> common = new HashSet<EventProxy>(model.getEvents());
        Set<EventProxy> total = new HashSet<EventProxy>();
        boolean contproj = false;
        int taus = 0;
        for (AutomatonProxy a : auts) {
          size += Math.log(a.getStates().size());
          total.addAll(a.getEvents());
          common.retainAll(a.getEvents());
          for (EventProxy e : a.getEvents()) {
            if (e.getName().startsWith("tau:")) {
              taus++;
            }
          }
        }
        double tot = total.size();
        double uncom = tot - common.size() - taus;
        size = uncom/tot;
        possible.add(new Tuple(auts, size));
      }
    }
    return possible;
  }*/
  
  private Collection<Set<AutomatonProxy>> getTuples(ProductDESProxy model, Set<AutomatonProxy> automata)
  {
    final SortedMap<SortedSet<AutomatonProxy>, Integer> numoccuring =
      new TreeMap<SortedSet<AutomatonProxy>,Integer>(new AutomataComparator());
    Events:
    for (EventProxy e : model.getEvents()) {
      if (e == getMarkingProposition()) {
        continue;
      }
      SortedSet<AutomatonProxy> possess = new TreeSet<AutomatonProxy>();
      for (AutomatonProxy a : automata) {
        if (a.getEvents().contains(e)) {
          possess.add(a);
        }
      }
      if (!possess.isEmpty()) {
        if (!numoccuring.containsKey(possess)) {
          numoccuring.put(possess, 0);
          /*Iterator<SortedSet<AutomatonProxy>> it = numoccuring.keySet().iterator();
          while (it.hasNext()) {
            SortedSet<AutomatonProxy> auts = it.next();
            if (auts.size() == possess.size()) {continue;}
            if (auts.size() < possess.size()) {
              if (possess.containsAll(auts)) {continue Events;}
            }
            if (auts.size() > possess.size()) {
              if (auts.containsAll(possess)) {it.remove();}
            }
          }*/
        } else if (numoccuring.get(possess) == null) {
          continue;
        }
        numoccuring.put(possess, numoccuring.get(possess) + 1);
      }
    }
    final TObjectDoubleHashMap<Set<AutomatonProxy>> heur =
      new TObjectDoubleHashMap<Set<AutomatonProxy>>();
    List<Set<AutomatonProxy>> possible =
      new ArrayList<Set<AutomatonProxy>>();
    //System.out.println("keyset:" + numoccuring.keySet().size());
    for (Set<AutomatonProxy> s : numoccuring.keySet()) {
      if (s.size() > 4 && s.size() != automata.size()) {
        continue;
      }
      double size = 0;
      Set<EventProxy> common = new HashSet<EventProxy>(model.getEvents());
      Set<EventProxy> total = new HashSet<EventProxy>();
      for (AutomatonProxy a : s) {
        size += Math.log(a.getStates().size());
        total.addAll(a.getEvents());
        common.retainAll(a.getEvents());
      }
      double tot = total.size();
      double uncom = tot - common.size();
      size = uncom;
      //possible.add(new Tuple(s, size));
      possible.add(s);
      heur.put(s, size);
    }
    Collections.sort(possible, new Comparator<Set<AutomatonProxy>>() {
        public int compare(Set<AutomatonProxy> a1, Set<AutomatonProxy> a2)
        {
          double heur1 = numoccuring.get(a1);
          double heur2 = numoccuring.get(a2);
          if (heur1 < heur2) {return -1;}
          else if (heur1 == heur2) {return 0;}
          else {return 1;}
        }
    });
    return possible;
  }
  
  private static class AutomataComparator
    implements Comparator<SortedSet<AutomatonProxy>>
  {
    public int compare(SortedSet<AutomatonProxy> s1, SortedSet<AutomatonProxy> s2)
    {
      if (s1.size() < s2.size()) {
        return -1;
      } else if (s1.size() > s2.size()) {
        return 1;
      }
      Iterator<AutomatonProxy> i1 = s1.iterator();
      Iterator<AutomatonProxy> i2 = s2.iterator();
      while (i1.hasNext()) {
        AutomatonProxy a1 = i1.next();
        AutomatonProxy a2 = i2.next();
        int res = a1.compareTo(a2);
        if (res != 0) {
          return res;
        }
      }
      return 0;
    }
  }
  
  /*private Set<Tuple> getTuples(ProductDESProxy model, Set<AutomatonProxy> automata)
  {
    Map<EventProxy, Set<AutomatonProxy>> haveEvent =
      new THashMap<EventProxy, Set<AutomatonProxy>>();
    for (EventProxy e : model.getEvents()) {
      if (e == getMarkingProposition()) {
        continue;
      }
      Set<AutomatonProxy> possess = new THashSet<AutomatonProxy>();
      for (AutomatonProxy a : automata) {
        if (a.getEvents().contains(e)) {
          possess.add(a);
        }
      }
      haveEvent.put(e, possess);
    }
    Set<Tuple> possible = new TreeSet<Tuple>(); 
    for (AutomatonProxy a1 : automata) {
      for (AutomatonProxy a2 : automata) {
        if (a1 == a2) {continue;}
        Set<AutomatonProxy> auts = new HashSet<AutomatonProxy>(2);
        auts.add(a1); auts.add(a2);
        double size = 0;
        Set<EventProxy> common = new HashSet<EventProxy>(model.getEvents());
        Set<EventProxy> total = new HashSet<EventProxy>();
        Set<AutomatonProxy> cover = new THashSet<AutomatonProxy>();
        int taus = 0;
        for (AutomatonProxy a : auts) {
          total.addAll(a.getEvents());
          common.retainAll(a.getEvents());
        }
        for (EventProxy e : total) {
          Set<AutomatonProxy> have = haveEvent.get(e);
          if (have == null) {continue;}
          cover.addAll(have);
        }
        for (AutomatonProxy a : cover) {
          size += Math.log(a.getStates().size());
        }
        possible.add(new Tuple(auts, size));
      }
    }
    return possible;
  }*/
  private Set<AutomatonProxy> getFromReader(Set<AutomatonProxy> automata,
                                            BufferedReader reader)
  {
    Set<AutomatonProxy> comp = new TreeSet<AutomatonProxy>();
    try {
      Reader:
      while (reader.ready()) {
        String name = reader.readLine();
        //System.out.println(name);
        if (name.equals("")) {return comp;}
        for (AutomatonProxy aut : automata) {
          if (aut.getName().equals(name)) {
            comp.add(aut); continue Reader;
          }
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return null;
  }

  private ProjectionList project(ProductDESProxy model)
    throws AnalysisException, CertainConflictException
  {
    eventscheck(model);
    //mRIT = new RemoveImpossibleTransitions(getMarkingProposition());
    //mME = new MergeEvents(getMarkingProposition(), model.getEvents());
    maxsize = 400000;
    mChecked.clear();
    Set<AutomatonProxy> automata = new TreeSet<AutomatonProxy>();
    Iterator<AutomatonProxy> autit = model.getAutomata().iterator();
    while (autit.hasNext()) {
      AutomatonProxy aut = autit.next();
      //System.out.println(aut.getName() + " " + aut.getKind());
      if (ComponentKind.PROPERTY != aut.getKind()) {
        automata.add(aut);
      }
    }
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/home/darius/supremicastuff/" + model.getName()));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    //mRIT.addAutomata(automata);
    ProjectionList p = null;
    while (true) {
      //automata = mME.run(automata, getFactory());
      //System.out.println("numautomata:" + automata.size());
      //Set<Tuple> possible = getTuples(model, automata);
      //Collection<Set<AutomatonProxy>> possible = getMinTransitions(model, automata);
      Set<AutomatonProxy> set = getFromReader(automata, reader);
      if (set == null) {break;}
      boolean stop = true;
      int overflows = 0;
      ProjectionList minlist = null;
      minSize = Integer.MAX_VALUE / 4;
      int setSize = -1;
      //System.out.println("possible: " + possible.size());
      int num = 0;
      tuples:
      //for (Tuple tup : possible) {
      //for (Set<AutomatonProxy> set : possible) {
        //if (num > 3) {break;}
        try {
          long imaxsize = 1;
          /*for (AutomatonProxy a : tup.mSet) {
            if (mDontOnOwn.contains(a) && tup.mSet.size() == 1) {
              continue tuples;
            }
            imaxsize *= a.getStates().size();
          }
          System.out.println(imaxsize);*/
          ProjectionList t =
            new ProjectionList(p, automata, set);
          num++;
          if (minSize >= t.getNew().getStates().size()) {
            minlist = t;
            minSize = t.getNew().getStates().size();
            //break;
          }
        } catch (final AnalysisException exception) {
          //exception.printStackTrace();
          //System.out.println("over");
          overflows++;
        }
      //}
      if (minlist != null) {
        p = minlist;
        automata = new TreeSet<AutomatonProxy>(p.getAutomata());
        stop = false;
        //System.out.println("numcomposed" + p.getComposed().size());
      }
      /*else {
        if (maxsize < 2000000) {
          maxsize *= 2;
          stop = false;
        }
      }*/
      if (stop) {
        break;
      }
    }
    try {
      reader.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    Iterator<AutomataHidden> it = mMinAutMap.keySet().iterator();
    while (it.hasNext()) {
      AutomataHidden ah = it.next();
      if (!mChecked.contains(ah)) {
        it.remove();
      }
    }
    return p;
  }

  private boolean setFailedResult(final TraceProxy counterexample,
                                  final Map<EventProxy,EventProxy> uncont)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + ":uncontrollable";
    final List<EventProxy> events = counterexample.getEvents();
    final int len = events.size();
    final List<EventProxy> modevents = new ArrayList<EventProxy>(len);
    final Iterator<EventProxy> iter = events.iterator();
    EventProxy event = iter.next();
    while (iter.hasNext()) {
      modevents.add(event);
      event = iter.next();
    }
    for (final Map.Entry<EventProxy,EventProxy> entry : uncont.entrySet()) {
      if (entry.getValue() == event) {
        final EventProxy key = entry.getKey();
        modevents.add(key);
        break;
      }
    }
    final SafetyTraceProxy wrapper =
      factory.createSafetyTraceProxy(tracename, des, modevents);
    return super.setFailedResult(wrapper);
  }


  //#########################################################################
  //# Inner Class AutomatonComparator
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }


  //#########################################################################
  //# Inner Class ProjectionList
  private class ProjectionList
  {
    final SortedSet<AutomatonProxy> mAutomata;
    final ProjectionList mParent;
    final SortedSet<AutomatonProxy> mCompautomata;
    final Set<EventProxy> mOriginalAlphabet;
    final Set<EventProxy> mHidden;
    final AutomatonProxy mNew;
    final Set<EventProxy> mTarget;
    
    private boolean containsAny(Set<EventProxy> contains, Set<EventProxy> of)
    {
      for (EventProxy e : of) {
        if (contains.contains(e)) {return true;}
      }
      return false;
    }
    
    private void blockedEvents()
    {
      Set<AutomatonProxy> mTempComp = new THashSet<AutomatonProxy>();
      Set<AutomatonProxy> mTempAut = new THashSet<AutomatonProxy>();
      //System.out.println("before");
      for (AutomatonProxy aut : mCompautomata) {
        //System.out.println(aut.getName() + " trans:" + aut.getTransitions().size());
      }
      for (AutomatonProxy aut : mCompautomata) {
        AutomatonProxy aut1 = aut;
        mTempAut.clear();
        for (AutomatonProxy aut2 : mAutomata) {
          if (containsAny(aut1.getEvents(), aut2.getEvents())) {
            List<AutomatonProxy> tocomp = 
              Arrays.asList(new AutomatonProxy[]{aut1, aut2});
            BlockedEvents be = new BlockedEvents(tocomp, getFactory(), getMarkingProposition());
            be.setNodeLimit(100000);
            try {
              tocomp = be.run();
            } catch (AnalysisException ae) {
              ae.printStackTrace();
            }
            aut1 = tocomp.get(0);
            mTempAut.add(tocomp.get(1));
          } else {
            mTempAut.add(aut2);
          }
        }
        mAutomata.clear();
        mAutomata.addAll(mTempAut);
        mTempComp.add(aut1);
      }
      //System.out.println("after");
      mCompautomata.clear();
      mCompautomata.addAll(mTempComp);
      for (AutomatonProxy aut : mCompautomata) {
        //System.out.println(aut.getName() + " trans:" + aut.getTransitions().size());
      }
    }

    public ProjectionList(ProjectionList parent,
                          Set<AutomatonProxy> automata,
                          Set<AutomatonProxy> compAutomata)
      throws AnalysisException, CertainConflictException
    {
      mParent = parent;
      mCompautomata = new TreeSet<AutomatonProxy>(compAutomata);
      mAutomata = new TreeSet<AutomatonProxy>(automata);
      Set<EventProxy> events = new TreeSet<EventProxy>();
      for (AutomatonProxy a : compAutomata) {
        events.addAll(a.getEvents());
      }
      mAutomata.removeAll(compAutomata);
      //System.out.println("events: " + events);
      mOriginalAlphabet = events;
      mHidden = new HashSet<EventProxy>(events);
      for (AutomatonProxy a : mAutomata) {
        //System.out.println("before comp:" + a);
        if (!compAutomata.contains(a)) {
          mHidden.removeAll(a.getEvents());
        }
      }
      if (mHidden.contains(getMarkingProposition())) {
        mHidden.remove(getMarkingProposition());
      }
      AutomataHidden ah =
        new AutomataHidden(compAutomata, new HashSet<EventProxy>(mHidden));
      mChecked.add(ah);
      AutomatonProxy minAutomaton;
      Collection<EventProxy> allwaysenabled = null;
      Collection<EventProxy> allselflooped = null;
      mMinAutMap.remove(ah);
      if (mMinAutMap.containsKey(ah)) {
        minAutomaton = mMinAutMap.get(ah);
        if (minAutomaton == null) {
          throw new OverflowException();
        }
      } else {
        //blockedEvents();
        Set<EventProxy> forb = Collections.emptySet();
        //System.out.println("marking: " + getMarkingProposition());
        try {
          //removeTransitions
          //end remove transitions
          NonDeterministicComposer composer =
            new NonDeterministicComposer(
              new ArrayList<AutomatonProxy>(mCompautomata), getFactory(),
                                            getMarkingProposition());
          int size = maxsize;
          //System.out.println(size);
          composer.setNodeLimit(size);
          mCompTime -= System.currentTimeMillis();
          minAutomaton = composer.run();
          //System.out.println("compaut:" + minAutomaton);
          mCompTime += System.currentTimeMillis();
          int compsize = minAutomaton.getStates().size();
          int comptransitions = minAutomaton.getTransitions().size();
          mAggComposition += compsize;
          mAggTransitions += comptransitions;
          mLargestTransitions = mLargestTransitions > comptransitions ? mLargestTransitions :
                                comptransitions;
          mLargestComposition = mLargestComposition > minAutomaton.getStates().size() ? mLargestComposition :
                                minAutomaton.getStates().size();
          AutomatonProxy minAutomaton2 = minAutomaton;
          int origstates = minAutomaton.getStates().size();
          int origtrans = minAutomaton.getTransitions().size();
          //System.out.println("compsize:" + compsize);
          if (!mHidden.isEmpty()) {
            //System.out.println("hiding:" + mHidden.size());
            TransitionRelation tr = new TransitionRelation(minAutomaton,
                                                           getMarkingProposition());
            int tau = tr.mergeEvents(mHidden, getFactory());
            EventProxy tauevent = tr.getEvent(tau);
            //System.out.println("TLR");
            TauLoopRemoval tlr = new TauLoopRemoval(tr, tau); tlr.run();
            //System.out.println("CC");
            //System.out.println(tr.getAutomaton(getFactory()));
            CertainConflict con = new CertainConflict(tr, tau);
            if (!con.run()) {throw new CertainConflictException();}
            tr.removeAllUnreachable();
            IncomingEquivalent ie = new IncomingEquivalent(tr, tau); ie.run();
            RemoveUnneededTransitions rut = new RemoveUnneededTransitions(tr, tau); rut.run();
            tr.removeAllUnreachable();
            ie = new IncomingEquivalent(tr, tau); ie.run();
            //System.out.println("after remove:" + tr.getAutomaton(getFactory()));
            tlr = new TauLoopRemoval(tr, tau); tlr.run();
            tr.removeAllUnreachable();
            TransBiSimulator tbs = new TransBiSimulator(tr, tau); tbs.run();
            //System.out.println("COMP");
            /*mAnnBITIME -= System.currentTimeMillis();
            //System.out.println("TLR");
            minAutomaton = tr.getAutomaton(getFactory());
            BiSimulator sim = new BiSimulator(minAutomaton,
                                              getMarkingProposition(),
                                              getFactory());
            mAnnotatedBISIMulation += minAutomaton.getStates().size();
            minAutomaton = sim.run();
            mAnnBITIME += System.currentTimeMillis();
            mAnnotatedBISIMulation -= minAutomaton.getStates().size();
            tr = new TransitionRelation(minAutomaton, getMarkingProposition());
            tau = tr.getEventInt(tauevent);*/
            //System.out.println("before");
            //System.out.println(tr.getAutomaton(getFactory()));
            //System.out.println("make equivalent");
            //System.out.println("after");
            //System.out.println(tr.getAutomaton(getFactory()));
            con = new CertainConflict(tr, tau);
            if (!con.run()) {/*throw new CertainConflictException();*/}
            minAutomaton = tr.getAutomaton(getFactory());
            mStates += minAutomaton.getStates().size();
            int diff = compsize - minAutomaton.getStates().size();
            mAggDiff += diff;
            mSmallestDiff = mSmallestDiff > diff ? diff : mSmallestDiff;
            mLargestDiff = mLargestDiff < diff ? diff : mLargestDiff;
            //if (diff < 0) {throw new AnalysisException("exception");}
            //System.out.println(minAutomaton.getName());
            //System.out.println("origstates:" + origstates);
            //System.out.println("origtrans:" + origtrans);
            //System.out.println("states:" + minAutomaton.getStates().size());
            //System.out.println("trans:" + minAutomaton.getTransitions().size());
            //System.out.println("events:" + minAutomaton.getEvents().size());
            //System.out.println("hidden: " + mHidden);
          } else {
            mBITIME -= System.currentTimeMillis();
            mBISIMulation += minAutomaton.getStates().size();
            BiSimulator sim = new BiSimulator(minAutomaton2,
                                            getMarkingProposition(),
                                            getFactory());
            //mBISIMulation += minAutomaton2.getStates().size();
            minAutomaton = sim.run();
            mBISIMulation -= minAutomaton.getStates().size();
            mBITIME += System.currentTimeMillis();
            //mBISIMulation -= minAutomaton2.getStates().size();
          }
          TransitionRelation tr = new TransitionRelation(minAutomaton, getMarkingProposition());
          //mMinAutMap.put(ah, minAutomaton);
        } catch (final AnalysisException exception) {
          mCompTime += System.currentTimeMillis();
          mStates += mMaxProjStates;
          mMinAutMap.put(ah, null);
          throw exception;
        }
      }
      // RemoveTransitions
      //mRIT.removeAutomata(mCompautomata);
      //mRIT.addAutomata(Collections.singleton(minAutomaton));
      // EndRemoveTransitions
      mAutomata.add(minAutomaton);
      mDontOnOwn.add(minAutomaton);
      mNew = minAutomaton;
      mTarget = new HashSet<EventProxy>();
      for (AutomatonProxy a : mAutomata) {
        mTarget.addAll(a.getEvents());
      }
    }
    
    private TIntHashSet getIntSet(Set<EventProxy> set, EventProxy[] array)
    {
      TIntHashSet res = new TIntHashSet(set.size());
      for (int i = 0; i < array.length; i++) {
        if (set.contains(array[i])) {
          res.add(i);
        }
      }
      return res;
    }

    public ProductDESProxy getModel()
    {
      return getFactory().createProductDESProxy("model", mTarget, mAutomata);
    }

    public Set<EventProxy> getHidden()
    {
      return mHidden;
    }

    public Set<AutomatonProxy> getComposed()
    {
      return mCompautomata;
    }

    public AutomatonProxy getNew()
    {
      return mNew;
    }
    
    public Set<AutomatonProxy> getAutomata()
    {
      return mAutomata;
    }

    public TraceProxy getTrace(TraceProxy trace, ProductDESProxy model)
    {
      List<Map<StateProxy, Set<EventProxy>>> events =
        new ArrayList<Map<StateProxy, Set<EventProxy>>>(mCompautomata.size());
      List<Map<Key, StateProxy>> automata =
        new ArrayList<Map<Key, StateProxy>>(mCompautomata.size());
      List<StateProxy> currstate = new ArrayList<StateProxy>(mCompautomata.size());
      AutomatonProxy[] aut = new AutomatonProxy[mCompautomata.size()];
      int i = 0;
      for (AutomatonProxy proxy : mCompautomata) {
        events.add(new HashMap<StateProxy, Set<EventProxy>>(proxy.getStates().size()));
        automata.add(new HashMap<Key, StateProxy>(proxy.getTransitions().size()));
        Set<EventProxy> autevents = new HashSet<EventProxy>(mOriginalAlphabet);
        //System.out.println(autevents);
        autevents.removeAll(proxy.getEvents());
        //System.out.println(autevents);
        int init = 0;
        Set<StateProxy> states = proxy.getStates();
        for (StateProxy s : states) {
          if (s.isInitial()) {
            init++;
            currstate.add(s);
          }
          events.get(i).put(s, new HashSet<EventProxy>(autevents));
        }
        assert(init == 1);
        Collection<TransitionProxy> trans = proxy.getTransitions();
        for (TransitionProxy t : trans) {
          events.get(i).get(t.getSource()).add(t.getEvent());
          automata.get(i).put(new Key(t.getSource(), t.getEvent()), t.getTarget());
        }
        aut[i] = proxy;
        i++;
      }
      Queue<Place> stateList = new PriorityQueue<Place>();
      Place place = new Place(currstate, null, 0, null);
      stateList.offer(place);
      List<EventProxy> oldevents = trace.getEvents();
      //System.out.println(oldevents);

      Set<Place> visited = new HashSet<Place>();
      visited.add(place);
      while (true) {
        place = stateList.poll();
        //System.out.println(place.getTrace());
        if (place.mIndex >= oldevents.size()) {
          break;
        }
        currstate = place.mCurrState;
        Set<EventProxy> possevents = new HashSet<EventProxy>(mHidden);
        //System.out.println(mHidden);
        hidden:
        for (EventProxy pe : possevents) {
          //System.out.println(pe);
          List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
          for (i = 0; i < currstate.size(); i++) {
            if (aut[i].getEvents().contains(pe)) {
              StateProxy t = automata.get(i).get(new Key(currstate.get(i), pe));
              //System.out.println(t);
              if (t == null) {
                continue hidden;
              }
              newstate.add(t);
            } else {
              newstate.add(currstate.get(i));
            }
          }
          //System.out.println(newstate);
          Place newPlace = new Place(newstate, pe, place.mIndex, place);
          if (visited.add(newPlace)) {
            stateList.offer(newPlace);
          }
        }
        EventProxy currevent = oldevents.get(place.mIndex);
        List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
        boolean contains = true;
        for (i = 0; i < currstate.size(); i++) {
          if (aut[i].getEvents().contains(currevent)) {
            StateProxy t = automata.get(i).get(new Key(currstate.get(i), currevent));
            if (t == null) {
              contains = false;
            }
            newstate.add(t);
          } else {
            newstate.add(currstate.get(i));
          }
        }
        Place newPlace = new Place(newstate, currevent, place.mIndex + 1, place);
        if (contains && visited.add(newPlace)) {
          stateList.offer(newPlace);
        }
        assert(!stateList.isEmpty());
      }
      stateList = null;
      ProductDESProxy mod = mParent == null ? model : mParent.getModel();
      trace = getFactory().createSafetyTraceProxy(mod, place.getTrace());
      return mParent == null ? trace : mParent.getTrace(trace, model);
    }

    private class Place
      implements Comparable<Place>
    {
      public final List<StateProxy> mCurrState;
      public final EventProxy mEvent;
      public final int mIndex;
      public final Place mParent;

      public Place(List<StateProxy> currState, EventProxy event,
                   int index, Place parent)
      {
        mCurrState = currState;
        mEvent = event;
        mIndex = index;
        mParent = parent;
      }

      public List<EventProxy> getTrace()
      {
        if (mParent == null) {
          return new LinkedList<EventProxy>();
        }
        List<EventProxy> events = mParent.getTrace();
        events.add(mEvent);
        return events;
      }

      public int compareTo(Place other)
      {
        return other.mIndex - mIndex;
      }

      public int hashCode()
      {
        int hash = 7;
        hash = hash + mIndex * 31;
        hash = hash + mCurrState.hashCode();
        return hash;
      }

      public boolean equals(Object o)
      {
        Place p = (Place) o;
        return p.mIndex == mIndex && p.mCurrState.equals(mCurrState);
      }
    }

    private class Key
    {
      private final StateProxy mState;
      private final EventProxy mEvent;
      private final int mHash;

      public Key(StateProxy state, EventProxy event)
      {
        int hash = 7;
        hash += state.hashCode() * 31;
        hash += event.hashCode() * 31;
        mState = state;
        mEvent = event;
        mHash = hash;
      }

      public int hashCode()
      {
        return mHash;
      }

      public boolean equals(final Object other)
      {
        if (other != null && other.getClass() == getClass()) {
          final Key key = (Key) other;
          return mState.equals(key.mState) && mEvent.equals(key.mEvent);
        } else {
          return false;
        }
      }
    }
  }

  private static class AutomataHidden
  {
    public final Set<AutomatonProxy> mAutomata;
    public final Set<EventProxy> mHidden;

    public AutomataHidden(Set<AutomatonProxy> automata, Set<EventProxy> hidden)
    {
      mAutomata = automata;
      mHidden = hidden;
    }

    public int hashCode()
    {
      int code = 31 + mAutomata.hashCode();
      code = code * 31 + mHidden.hashCode();
      return code;
    }

    public boolean equals(Object o)
    {
      if (o instanceof AutomataHidden) {
        AutomataHidden a = (AutomataHidden) o;
        return mAutomata.equals(a.mAutomata) && mHidden.equals(a.mHidden);
      }
      return false;
    }
  }

  private static class Tuple
    implements Comparable<Tuple>
  {
    public final Set<AutomatonProxy> mSet;
    public final double mSize;

    public Tuple(Set<AutomatonProxy> set, double size)
    {
      mSet = set;
      mSize = size;
    }

    public int compareTo(Tuple t)
    {
      if (mSize < t.mSize) {
        return -1;
      } else if (mSize == t.mSize) {
        return 0;
      } else {
        return 1;
      }
    }
  }

  //#########################################################################
  //# Data Members
  private int minSize = 10000;
  private AutomatonProxy mSpec = null;
  private int mStates;
  private int mMaxProjStates;
  private Map<AutomataHidden, AutomatonProxy> mMinAutMap =
    new HashMap<AutomataHidden, AutomatonProxy>();
  private Set<AutomataHidden> mChecked = new HashSet<AutomataHidden>();
  private Set<AutomatonProxy> mDontOnOwn = new HashSet<AutomatonProxy>();
  private Map<EventProxy, Set<AutomatonProxy>> mAllSelfLoops = 
    new THashMap<EventProxy, Set<AutomatonProxy>>();
  private Map<EventProxy, Set<AutomatonProxy>> mAllwaysEnabled = 
    new THashMap<EventProxy, Set<AutomatonProxy>>();
  private int mLargestComposition = 0;
  private int mLargestTransitions = 0;
  private int mAggComposition = 0;
  private int mAggTransitions = 0;
  
  private int mAnnotatedBISIMulation = 0;
  private int mBISIMulation = 0;
  private int mCompTime = 0;
  private int mAnnBITIME = 0;
  private int mBITIME = 0;
  private int mSmallestDiff = Integer.MAX_VALUE;
  private int mLargestDiff = Integer.MIN_VALUE;
  private int mAggDiff = 0;
  private int maxsize = 1000;
  private int switched = 0;
  private int mTime = 0;
  
  
  private RemoveImpossibleTransitions mRIT = null;
  private MergeEvents mME = null;
  
  //#########################################################################
  //# Class Constants
  private static final Logger LOGGER =
    LoggerFactory.createLogger(ProjectingNonBlockingChecker.class);

}
