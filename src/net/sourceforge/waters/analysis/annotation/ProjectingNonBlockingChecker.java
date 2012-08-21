//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
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
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.modular.BiSimulator;
import net.sourceforge.waters.analysis.modular.BlockedEvents;
import net.sourceforge.waters.analysis.modular.ConfRevBiSimulator;
import net.sourceforge.waters.analysis.modular.NonDeterministicComposer;
import net.sourceforge.waters.analysis.modular.TransBiSimulator;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
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
import net.sourceforge.waters.analysis.annotation.CompareLessConflicting;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.analysis.tr.Determinizer;


/**
 * The projecting controllability check algorithm.
 *
 * @author Simon Ware
 */

public class ProjectingNonBlockingChecker extends AbstractConflictChecker
    implements ConflictChecker
{

  // #########################################################################
  // # Constructors
  public ProjectingNonBlockingChecker(final ProductDESProxy model,
                                      final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mStates = 0;
    setNodeLimit(10000000);
  }

  private void eventscheck(final ProductDESProxy model)
  {
    final Collection<AutomatonProxy> automata = model.getAutomata();
    mAllSelfLoops.clear();
    mAllwaysEnabled.clear();
    for (final EventProxy event : model.getEvents()) {
      mAllSelfLoops.put(event, new THashSet<AutomatonProxy>());
      mAllwaysEnabled.put(event, new THashSet<AutomatonProxy>());
      for (final AutomatonProxy aut : automata) {
        if (aut.getEvents().contains(event)) {
          mAllSelfLoops.get(event).add(aut);
          mAllwaysEnabled.get(event).add(aut);
        }
      }
    }
    for (final AutomatonProxy auto : automata) {
      final TransitionRelation tr =
          new TransitionRelation(auto, getMarkingProposition());
      final Collection<EventProxy> allselflooped = tr.getAllSelfLoops();
      for (final EventProxy event : allselflooped) {
        mAllSelfLoops.get(event).remove(auto);
      }
      final Collection<EventProxy> allwaysenabled = tr.getAllwaysEnabled();
      for (final EventProxy event : allwaysenabled) {
        mAllwaysEnabled.get(event).remove(auto);
      }
    }
  }

  // #########################################################################
  // # Invocation
  public boolean run() throws AnalysisException
  {
    clearStats();
    mTime -= System.currentTimeMillis();
    if (getMarkingProposition() == null) {
      setMarkingProposition(getUsedMarkingProposition());
    }
    try {
      mWriter = new BufferedWriter(new FileWriter("/home/darius/supremicastuff/reductions" + getModel().getName()));
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    Runtime.getRuntime().traceMethodCalls(true);
    boolean result = false;
    double checkerstates = 0;

    try {
      final ProjectionList list = project(getModel());
      mMinAutMap.clear();
      //System.out.println(list);
      if (list == null) {
        return true;
      }
      //System.out.println(list.getModel());
      final ConflictChecker checker =
        new NativeConflictChecker(list.getModel(), getMarkingProposition(),
                                  getFactory());
      result = checker.run();
      checkerstates = checker.getAnalysisResult().getTotalNumberOfStates();
    } catch (final CertainConflictException cce) {
      //System.out.println("caught:" + cce);
      result = false;
    }
    System.out.println("result: " + result);
    System.out.println("checkerstates: " + checkerstates);
    mTime += System.currentTimeMillis();
    try {
      final BufferedWriter write = new BufferedWriter(new FileWriter("/home/darius/Projects/supr/supremica/mine/" + getModel().getName()));
      write.append(getStats());
      write.append("states:" + checkerstates + "\n");
      write.append("result:" + result);
      write.close();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    //System.out.println("Total Switched: " + switched);
    clearStats();
    try {
      mWriter.flush();
      mWriter.close();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    if (!result) {
      final List<EventProxy> e = new ArrayList<EventProxy>();
      final TraceProxy counter = getFactory().createSafetyTraceProxy(getModel().getName(),
                                                               getModel(), e);
      setFailedResult(counter);
    } else {
      setSatisfiedResult();
    }
    return result;
    /*
     * if (checker.run()) { mStates +=
     * checker.getAnalysisResult().getTotalNumberOfStates();
     * setSatisfiedResult(); return true; } else { mStates +=
     * checker.getAnalysisResult().getTotalNumberOfStates(); TraceProxy counter
     * = checker.getCounterExample(); counter = list.getTrace(counter, model);
     * List<EventProxy> e = counter.getEvents(); counter =
     * getFactory().createSafetyTraceProxy(getModel().getName(), getModel(),
     * e.subList(0, e.size() - 1)); setFailedResult(counter); return false; }
     */
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
    RedundantTransitions.clearStats();
    AddRedundantTransitions.clearStats();
    UnAnnotateGraph.clearStats();
    BiSimulatorRedundant.clearStats();
    RemoveImpossibleTransitions.clearStats();
    RemoveAnnotations.clearStats();
    MergeEvents.clearStats();
    ConfRevBiSimulator.clearStats();
    // IncomingEquivalent.clearStats();
    TransBiSimulator.clearStats();
    SilentOutGoing.clearStats();
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
    stats += EquivalentIncoming.stats() + "\n";
    stats += RemoveFollowOnTau.stats() + "\n";
    stats += TauLoopRemoval.stats() + "\n";
    stats += UnAnnotateGraph.stats() + "\n";
    stats += RemoveImpossibleTransitions.stats() + "\n";
    stats += RemoveSubsetTau.stats() + "\n";
    stats += RedundantTransitions.stats() + "\n";
    stats += AddRedundantTransitions.stats() + "\n";
    stats += MergeEvents.stats() + "\n";
    stats += ConfRevBiSimulator.stats() + "\n";
    // stats += IncomingEquivalent.stats() + "\n";
    stats += TransBiSimulator.stats() + "\n";
    stats += BiSimulatorRedundant.stats() + "\n";
    stats += SilentOutGoing.stats() + "\n";
    stats += RemoveAnnotations.stats() + "\n";
    stats += "mAggDiff = " + mAggDiff + " mSmallestDiff = " + mSmallestDiff + " mLargestDiff = " + mLargestDiff + "\n";
    stats += "Largest Composition: " + mLargestComposition + " COMPTIME: " + mCompTime + "\n";
    stats += "Largest Transitions: " + mLargestTransitions + " aggcomp: " + mAggComposition + " aggtrans: " + mAggTransitions + "\n";
    stats += "Annotated Bisimulation: " + mAnnotatedBISIMulation + " mAnnBITIME: " + mAnnBITIME + "\n";
    stats += "Bisimulation: " + mBISIMulation + " mBITIME: " + mBITIME + "\n";
    stats += "Time: " + mTime + "\n";
    return stats;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return true;
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

  public void setuplocal(final ProductDESProxy model, final Set<AutomatonProxy> automata)
  {
    final TObjectIntHashMap<Set<AutomatonProxy>> numoccuring =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    for (final EventProxy e : model.getEvents()) {
      if (e == getMarkingProposition()) {
        continue;
      }
      final Set<AutomatonProxy> possess = new THashSet<AutomatonProxy>();
      for (final AutomatonProxy a : automata) {
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
    mNumOccurinng = numoccuring;
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }

  /*
   * private Set<Set<AutomatonProxy>> getMaxAutomaton(Set<AutomatonProxy>
   * automata) { Set<Set<AutomatonProxy>> pairs = new
   * THashSet<Set<AutomatonProxy>>(); AutomatonProxy maxaut = null; for
   * (AutomatonProxy aut : automata) { maxaut = maxaut == null ? aut : maxaut;
   * maxaut = maxaut.getStates.size() < aut.getStates().size() ? aut : maxaut; }
   * for (AutomatonProxy aut : automata) { if (maxaut == aut) {continue;}
   * Set<AutomatonProxy> pair = new THashSet<AutomatonProxy>(2);
   * pair.add(maxaut); pair.add(aut); pairs.add(pair); } return pairs; }
   *
   * private Set<Set<AutomatonProxy>> getMinTransitions(Set<AutomatonProxy>
   * automata) { Set<Set<AutomatonProxy>> pairs = new
   * THashSet<Set<AutomatonProxy>>(); AutomatonProxy minaut = null; for
   * (AutomatonProxy aut : automata) { minaut = minaut == null ? aut : minaut;
   * minaut = minaut.getTransitions().size() > aut.getStates().size() ? aut :
   * minaut; } for (AutomatonProxy aut : automata) { if (minaut == aut)
   * {continue;} Set<AutomatonProxy> pair = new THashSet<AutomatonProxy>(2);
   * pair.add(minaut); pair.add(aut); pairs.add(pair); } return pairs; }
   */

  /*
   * private Set<Tuple> getTuples(ProductDESProxy model, Set<AutomatonProxy>
   * automata) { THashMap<AutomatonProxy, PriorityQueue<Tuple>> queues = new
   * THashMap<AutomatonProxy, PriorityQueue<Tuple>>(); Set<Tuple> possible = new
   * TreeSet<Tuple>(); for (AutomatonProxy a1 : automata) { queues.put(a1, new
   * PriorityQueue<Tuple>()); for (AutomatonProxy a2 : automata) { if (a1 == a2)
   * {continue;} Set<AutomatonProxy> auts = new HashSet<AutomatonProxy>(2);
   * auts.add(a1); auts.add(a2); double size = 0; Set<EventProxy> common = new
   * HashSet<EventProxy>(model.getEvents()); Set<EventProxy> total = new
   * HashSet<EventProxy>(); boolean contproj = false; int taus = 0; for
   * (AutomatonProxy a : auts) { size += Math.log(a.getStates().size());
   * total.addAll(a.getEvents()); common.retainAll(a.getEvents()); for
   * (EventProxy e : a.getEvents()) { if (e.getName().startsWith("tau:")) {
   * taus++; } } } double tot = 1; double uncom = 1; for (AutomatonProxy a :
   * auts) { int uncom1 = 0; int tot1 = 0; for (TransitionProxy t :
   * a.getTransitions()) { if (!common.contains(t.getEvent())) { uncom1++; }
   * tot1++; } tot *= tot1; uncom *= uncom1; } size = uncom / tot;
   * queues.get(a1).offer(new Tuple(auts, size)); } } for (AutomatonProxy a :
   * queues.keySet()) { PriorityQueue<Tuple> queue = queues.get(a); if
   * (queue.isEmpty()) {continue;} if (queue.size() == 1)
   * {possible.add(queue.poll()); continue;} Tuple tup1 = queue.poll(); Tuple
   * tup2 = queue.poll(); possible.add(new Tuple(tup1.mSet, tup1.mSize -
   * tup2.mSize)); } return possible; }
   */

  /*
   * private Set<Tuple> getTuples(ProductDESProxy model, Set<AutomatonProxy>
   * automata) { Set<Tuple> possible = new TreeSet<Tuple>(); for (AutomatonProxy
   * a1 : automata) { for (AutomatonProxy a2 : automata) { if (a1 == a2)
   * {continue;} Set<AutomatonProxy> auts = new HashSet<AutomatonProxy>(2);
   * auts.add(a1); auts.add(a2); double size = 0; Set<EventProxy> common = new
   * HashSet<EventProxy>(model.getEvents()); Set<EventProxy> total = new
   * HashSet<EventProxy>(); boolean contproj = false; int taus = 0; for
   * (AutomatonProxy a : auts) { size += Math.log(a.getStates().size());
   * total.addAll(a.getEvents()); common.retainAll(a.getEvents()); for
   * (EventProxy e : a.getEvents()) { if (e.getName().startsWith("tau:")) {
   * taus++; } } } double tot = total.size(); double uncom = tot - common.size()
   * - taus; size = uncom/tot; possible.add(new Tuple(auts, size)); } } return
   * possible; }
   */

  @SuppressWarnings("unused")
  private Set<Set<AutomatonProxy>> getMinTransitions(final Set<AutomatonProxy> automata)
  {
    final Set<Set<AutomatonProxy>> pairs = new THashSet<Set<AutomatonProxy>>();
    AutomatonProxy minaut = null;
    for (final AutomatonProxy aut : automata) {
      minaut = minaut == null ? aut : minaut;
      minaut = minaut.getTransitions().size() > aut.getStates().size() ? aut : minaut;
    }
    for (final AutomatonProxy aut : automata) {
      if (minaut == aut) {continue;}
      final Set<AutomatonProxy> pair = new THashSet<AutomatonProxy>(2);
      pair.add(minaut);
      pair.add(aut);
      pairs.add(pair);
    }
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

  @SuppressWarnings("unused")
  private List<Set<AutomatonProxy>> getMinTransitions(final ProductDESProxy model, final SortedSet<AutomatonProxy> automata)
  {
    final TObjectIntHashMap<Set<AutomatonProxy>> common =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    final List<Set<AutomatonProxy>> pairs = new ArrayList<Set<AutomatonProxy>>();
    AutomatonProxy minaut = null;
    for (final AutomatonProxy aut : automata) {
      minaut = minaut == null ? aut : minaut;
      minaut = minaut.getTransitions().size() > aut.getStates().size() ? aut : minaut;
    }
    for (final AutomatonProxy aut : automata) {
      if (minaut == aut) {continue;}
      final Set<AutomatonProxy> pair = new THashSet<AutomatonProxy>(2);
      final Set<EventProxy> events = new THashSet<EventProxy>(aut.getEvents());
      events.retainAll(minaut.getEvents());
      pair.add(minaut);
      pair.add(aut);
      pairs.add(pair);
      common.put(pair, events.size());
    }
    /*final TObjectIntHashMap<Set<AutomatonProxy>> numoccuring =
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
        numoccuring.put(possess, numoccuring.get(possess) + 1);
      }
    }*/
    /*Collections.sort(pairs, new Comparator<Set<AutomatonProxy>>() {
        public int compare(Set<AutomatonProxy> a1, Set<AutomatonProxy> a2)
        {
          int local1 = mNumOccurinng.get(a1);
          int local2 = mNumOccurinng.get(a2);
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
    });*/
    mCommon = common;
    //mNumOccurinng = numoccuring;
    return pairs;
  }

  @SuppressWarnings("unused")
  private Set<AutomatonProxy> getMinSet(final List<Set<AutomatonProxy>> auts)
  {
    int maxlocal = -1;
    int maxcommon = -1;
    int i = -1;
    for (int index = 0; index < auts.size(); index++) {
      final Set<AutomatonProxy> set = auts.get(index);
      final int local = mNumOccurinng.get(set);
      final int common = mCommon.get(set);
      if (local > maxlocal) {
        maxlocal = local;
        maxcommon = common;
        i = index;
      } else if (local == maxlocal) {
        if (common > maxcommon) {
          maxlocal = local;
          maxcommon = common;
          i = index;
        }
      }
    }
    return auts.remove(i);
  }

  @SuppressWarnings({"unused", "unchecked"})
  private List<Set<AutomatonProxy>> getTuples(final ProductDESProxy model, final Set<AutomatonProxy> automata)
  {
    final TObjectIntHashMap<Set<AutomatonProxy>> heur =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    final List<Set<AutomatonProxy>> possible =
      new ArrayList<Set<AutomatonProxy>>();
    //System.out.println("keyset:" + numoccuring.keySet().size());
    Set<AutomatonProxy>[] keys = new Set[mNumOccurinng.size()];
    keys = mNumOccurinng.keys(keys);
    for (final Set<AutomatonProxy> s : Arrays.asList(keys)) {
      if (s.size() > 4 && s.size() != automata.size()) {
        continue;
      }
      double size = 0;
      final Set<EventProxy> common = new HashSet<EventProxy>(model.getEvents());
      final Set<EventProxy> total = new HashSet<EventProxy>();
      for (final AutomatonProxy a : s) {
        size += Math.log(a.getStates().size());
        total.addAll(a.getEvents());
        common.retainAll(a.getEvents());
      }
      final double tot = total.size();
      final double uncom = tot - common.size();
      size = uncom;
      //possible.add(new Tuple(s, size));
      possible.add(s);
      heur.put(s, common.size());
    }
    mCommon = heur;
    /*Collections.sort(possible, new Comparator<Set<AutomatonProxy>>() {
        public int compare(Set<AutomatonProxy> a1, Set<AutomatonProxy> a2)
        {
          double heur1 = mNumOccurinng.get(a1);
          double heur2 = mNumOccurinng.get(a2);
          if (heur1 < heur2) {return -1;}
          else if (heur1 == heur2) {return 0;}
          else {return 1;}
        }
    });*/
    return possible;
  }


  @SuppressWarnings("unused")
  private static class AutomataComparator implements
      Comparator<SortedSet<AutomatonProxy>>
  {
    public int compare(final SortedSet<AutomatonProxy> s1,
                       final SortedSet<AutomatonProxy> s2)
    {
      if (s1.size() < s2.size()) {
        return -1;
      } else if (s1.size() > s2.size()) {
        return 1;
      }
      final Iterator<AutomatonProxy> i1 = s1.iterator();
      final Iterator<AutomatonProxy> i2 = s2.iterator();
      while (i1.hasNext()) {
        final AutomatonProxy a1 = i1.next();
        final AutomatonProxy a2 = i2.next();
        final int res = a1.compareTo(a2);
        if (res != 0) {
          return res;
        }
      }
      return 0;
    }
  }

  private Set<AutomatonProxy> getFromReader(final Set<AutomatonProxy> automata,
                                            final BufferedReader reader)
  {
    final Set<AutomatonProxy> comp = new TreeSet<AutomatonProxy>();
    try {
      Reader:
      while (reader.ready()) {
        final String name = reader.readLine();
        //System.out.println(name);
        if (name.equals("")) {return comp;}
        for (final AutomatonProxy aut : automata) {
          if (aut.getName().equals(name)) {
            comp.add(aut); continue Reader;
          }
        }
      }
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    return null;
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

  private ProjectionList project(final ProductDESProxy model)
    throws AnalysisException, CertainConflictException
  {
    eventscheck(model);
    //mRIT = new RemoveImpossibleTransitions(getMarkingProposition());
    //mME = new MergeEvents(getMarkingProposition(), model.getEvents());
    maxsize = 400000;
    mChecked.clear();
    SortedSet<AutomatonProxy> automata = new TreeSet<AutomatonProxy>();
    final Iterator<AutomatonProxy> autit = model.getAutomata().iterator();
    while (autit.hasNext()) {
      final AutomatonProxy aut = autit.next();
      //System.out.println(aut.getName() + " " + aut.getKind());
      if (ComponentKind.PROPERTY != aut.getKind()) {
        automata.add(aut);
      }
    }
    BufferedReader reader = null;
    /*try {
      reader = new BufferedReader(new FileReader("/home/darius/supremicastuff/" + model.getName()));
    } catch (final Throwable t) {
      t.printStackTrace();
    }*/
    //mRIT.addAutomata(automata);
    ProjectionList p = null;
    while (true) {
      //automata = mME.run(automata, getFactory());
      //System.out.println("numautomata:" + automata.size());
      //Set<Tuple> possible = getTuples(model, automata);
      Collection<Set<AutomatonProxy>> possible = getMinTransitions(model, automata);
      //final Set<AutomatonProxy> set = getFromReader(automata, reader);
      //if (set == null) {break;}
      boolean stop = true;
      ProjectionList minlist = null;
      minSize = Integer.MAX_VALUE / 4;
      //System.out.println("possible: " + possible.size());
      //for (Tuple tup : possible) {
      for (Set<AutomatonProxy> set : possible) {
        //if (num > 3) {break;}
        try {
          /*
          long imaxsize = 1;
          for (AutomatonProxy a : tup.mSet) {
            if (mDontOnOwn.contains(a) && tup.mSet.size() == 1) {
              continue tuples;
            }
            imaxsize *= a.getStates().size();
          }
          System.out.println(imaxsize);*/
          final ProjectionList t =
            new ProjectionList(p, automata, set);
          if (minSize >= t.getNew().getStates().size()) {
            minlist = t;
            minSize = t.getNew().getStates().size();
            break;
          }
        } catch (final AnalysisException exception) {
          // TODO This can't be right ~~~Robi
          //exception.printStackTrace();
          //System.out.println("over");
          //overflows++;
        }
      }
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
    /*try {
      reader.close();
    } catch (final Throwable t) {
      t.printStackTrace();
    }*/
    final Iterator<AutomataHidden> it = mMinAutMap.keySet().iterator();
    while (it.hasNext()) {
      final AutomataHidden ah = it.next();
      if (!mChecked.contains(ah)) {
        it.remove();
      }
    }
    return p;
  }

  /*private ProjectionList project(ProductDESProxy model)
    throws AnalysisException, CertainConflictException
  {
    eventscheck(model);
    // mRIT = new RemoveImpossibleTransitions(getMarkingProposition());
    // mME = new MergeEvents(getMarkingProposition(), model.getEvents());
    maxsize = 4000000;
    mChecked.clear();
    Set<AutomatonProxy> automata = new TreeSet<AutomatonProxy>();
    final Iterator<AutomatonProxy> autit = model.getAutomata().iterator();
    while (autit.hasNext()) {
      final AutomatonProxy aut = autit.next();
      //System.out.println(aut.getName() + " " + aut.getKind());
      if (ComponentKind.PROPERTY != aut.getKind()) {
        automata.add(aut);
      }
    }
    ComposeOrder comporder = new ComposeOrder(model, automata);
    comporder.run();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/home/darius/supremicastuff/" + model.getName()));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    //mRIT.addAutomata(automata);
    ProjectionList p = null;
    while (true) {
      // automata = mME.run(automata, getFactory());
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
  }*/

  /*private ProjectionList project(ProductDESProxy model)
    throws AnalysisException, CertainConflictException
  {
    eventscheck(model);
    //mRIT = new RemoveImpossibleTransitions(getMarkingProposition());
    //mME = new MergeEvents(getMarkingProposition(), model.getEvents());
    maxsize = 4000000;
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
    AntOrder antorder = new AntOrder(model, automata);
    antorder.run(100);
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
      //Set<AutomatonProxy> set = getFromReader(automata, reader);
      Set<AutomatonProxy> set = antorder.selectComp(automata);
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
          ProjectionList t =
            new ProjectionList(p, automata, set);
          num++;
          if (minSize >= t.getNew().getStates().size()) {
            minlist = t;
            minSize = t.getNew().getStates().size();
            //break;
          }
        } catch (final AnalysisException exception) {
          // exception.printStackTrace();
          //System.out.println("over");
          overflows++;
        }
      //}
      if (minlist != null) {
        p = minlist;
        automata = new TreeSet<AutomatonProxy>(p.getAutomata());
        stop = false;
        antorder.setActual(set, p.getNew());
        //System.out.println("numcomposed" + p.getComposed().size());
      } else {
        antorder.setActual(set, null);
      }
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
      final AutomataHidden ah = it.next();
      if (!mChecked.contains(ah)) {
        it.remove();
      }
    }
    return p;
  }*/

  @SuppressWarnings("unused")
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


  // #########################################################################
  // # Inner Class ProjectionList
  private class ProjectionList
  {
    final SortedSet<AutomatonProxy> mAutomata;
    final ProjectionList mParent;
    final SortedSet<AutomatonProxy> mCompautomata;
    final Set<EventProxy> mOriginalAlphabet;
    final Set<EventProxy> mHidden;
    final AutomatonProxy mNew;
    final Set<EventProxy> mTarget;

    private boolean containsAny(final Set<EventProxy> contains, final Set<EventProxy> of)
    {
      for (final EventProxy e : of) {
        if (contains.contains(e)) {
          return true;
        }
      }
      return false;
    }

    @SuppressWarnings("unused")
    private void blockedEvents()
    {
      final Set<AutomatonProxy> mTempComp = new TreeSet<AutomatonProxy>();
      final Set<AutomatonProxy> mTempAut = new TreeSet<AutomatonProxy>();
      //System.out.println("before");
      for (final AutomatonProxy aut : mCompautomata) {
        //System.out.println(aut.getName() + " trans:" + aut.getTransitions().size());
      }
      for (final AutomatonProxy aut : mCompautomata) {
        AutomatonProxy aut1 = aut;
        mTempAut.clear();
        for (final AutomatonProxy aut2 : mAutomata) {
          if (containsAny(aut1.getEvents(), aut2.getEvents())) {
            List<AutomatonProxy> tocomp =
                Arrays.asList(new AutomatonProxy[] {aut1, aut2});
            final BlockedEvents be =
                new BlockedEvents(tocomp, getFactory(), getMarkingProposition());
            be.setNodeLimit(100000);
            try {
              tocomp = be.run();
            } catch (final AnalysisException ae) {
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
      for (final AutomatonProxy aut : mCompautomata) {
        //System.out.println(aut.getName() + " trans:" + aut.getTransitions().size());
      }
    }

    public ProjectionList(final ProjectionList parent,
                          final Set<AutomatonProxy> automata,
                          final Set<AutomatonProxy> compAutomata)
      throws AnalysisException, CertainConflictException
    {
      mParent = null;// parent;
      mCompautomata = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      mCompautomata.addAll(compAutomata);
      mAutomata = new TreeSet<AutomatonProxy>(automata);
      final Set<EventProxy> events = new TreeSet<EventProxy>();
      for (final AutomatonProxy a : mCompautomata) {
        System.out.println(a.getName() + ": " + a.getStates().size());
        events.addAll(a.getEvents());
      }
      mAutomata.removeAll(compAutomata);
      mOriginalAlphabet = events;
      mHidden = new HashSet<EventProxy>(events);
      for (final AutomatonProxy a : mAutomata) {
        if (!compAutomata.contains(a)) {
          mHidden.removeAll(a.getEvents());
        }
      }
      if (mHidden.contains(getMarkingProposition())) {
        mHidden.remove(getMarkingProposition());
      }
      //AutomataHidden ah =
      //  new AutomataHidden(compAutomata, new HashSet<EventProxy>(mHidden));
      //mChecked.add(ah);
      AutomatonProxy minAutomaton;
      Collection<EventProxy> allwaysenabled = null;
      Collection<EventProxy> allselflooped = null;
      //mMinAutMap.remove(ah);
      /*if (mMinAutMap.containsKey(ah)) {
        minAutomaton = mMinAutMap.get(ah);
        if (minAutomaton == null) {
          throw new OverflowException();
        }
      } else {*/
        //blockedEvents();
        //System.out.println("marking: " + getMarkingProposition());
        try {
          // removeTransitions
          /*
           * compAutomata.clear(); for (AutomatonProxy aut : mCompautomata) {
           * //System.out.println("before"); //System.out.println(aut);
           * TransitionRelation tr = new TransitionRelation(aut,
           * getMarkingProposition(), mOriginalAlphabet); mRIT.run(tr); aut =
           * tr.getAutomaton(getFactory()); compAutomata.add(aut);
           * //System.out.println("after"); //System.out.println(aut); }
           */
          // end remove transitions
          final NonDeterministicComposer composer =
              new NonDeterministicComposer(new ArrayList<AutomatonProxy>(
                  mCompautomata), getFactory(), getMarkingProposition());
          final int size = maxsize;
          //System.out.println(size);
          composer.setNodeLimit(size);
          mCompTime -= System.currentTimeMillis();
          minAutomaton = composer.run();
          mCompTime += System.currentTimeMillis();
          final int compsize = minAutomaton.getStates().size();
          final int comptransitions = minAutomaton.getTransitions().size();
          mAggComposition += compsize;
          mAggTransitions += comptransitions;
          mLargestTransitions = mLargestTransitions > comptransitions ? mLargestTransitions :
                                comptransitions;
          mLargestComposition = mLargestComposition > minAutomaton.getStates().size() ? mLargestComposition :
                                minAutomaton.getStates().size();
          System.out.println("compsize:" + minAutomaton.getStates().size());
          EventProxy tauproxy =
              getFactory().createEventProxy("tau:" + minAutomaton.getName(),
                                            EventKind.UNCONTROLLABLE);
          EventEncoding ee = new EventEncoding(minAutomaton, getKindTranslator(), tauproxy);
          if (!minAutomaton.getEvents().contains(getMarkingProposition())) {
            ee.addEvent(getMarkingProposition(), getKindTranslator(), true);
          }
          ListBufferTransitionRelation orig =
            new ListBufferTransitionRelation(minAutomaton, ee,
                                             ListBufferTransitionRelation.CONFIG_SUCCESSORS);
          for (EventProxy event : mHidden) {
            if (getKindTranslator().getEventKind(event) == EventKind.PROPOSITION) {continue;}
            int evcode = ee.getEventCode(event);
            if (evcode == -1) {System.out.println(event);continue;}
            if (evcode == EventEncoding.TAU) {continue;}
            orig.replaceEvent(evcode, EventEncoding.TAU);
            orig.removeEvent(evcode);
          }
          //mRIT.removeAutomata(mCompautomata);
          /*RemoveEvents rev = new RemoveEvents(minAutomaton, mRIT.findEventsWhichAreImpossibleAfter(minAutomaton.getEvents()),
                                              getMarkingProposition(), getFactory());
          rev.run();
          Set<EventProxy> blocked = rev.getImpossible();
          if (!blocked.isEmpty()) {
            minAutomaton = Composer.removeBlocked(blocked, minAutomaton, getFactory());
            List<AutomatonProxy> temp = new ArrayList<AutomatonProxy>(mAutomata);
            mAutomata.clear();
            //mRIT.removeAutomata(mAutomata);
            for (AutomatonProxy aut : temp) {
              aut = Composer.removeBlocked(blocked, aut, getFactory());
              mAutomata.add(aut);
              //mRIT.addAutomata(Collections.singleton(aut));
            }
            mHidden.removeAll(blocked);
          }*/
          if (!mHidden.isEmpty()) {
            //System.out.println("hiding:" + mHidden.size());
            TransitionRelation tr = new TransitionRelation(minAutomaton,
                                                           getMarkingProposition());
            /*for (EventProxy event : mOriginalAlphabet) {
              if (mAllSelfLoops.containsKey(event) && mAllSelfLoops.get(event).isEmpty()) {
                System.out.println("self looped");
                tr.removeAllTransitionsWithEvent(tr.eventToInt(event));
              }
            }*/
            int tau = tr.mergeEvents(mHidden, getFactory());
            //AutomatonProxy minAutomaton3 = tr.getAutomaton(getFactory());
            EventProxy tauevent = tr.getEvent(tau);
            ee.addSilentEvent(tauevent);
            //System.out.println("TLR");
            //tr.makeObservationEquivalent(tau);
            final TauLoopRemoval tlr = new TauLoopRemoval(tr, tau);
            tlr.run();
            //System.out.println("CC");
            //System.out.println(tr.getAutomaton(getFactory()));
            //System.out.println("before remove:" + tr.getAutomaton(getFactory()));
            final RemoveUnneededTransitions ru = new RemoveUnneededTransitions(tr, tau); ru.run();
            CertainConflict con = new CertainConflict(tr, tau);
            if (!con.run()) {throw new CertainConflictException();}
            //tr.makeObservationEquivalent(tau);
            //System.out.println("COMP");
            tr.removeAllUnreachable();
            //System.out.println("after remove:" + tr.getAutomaton(getFactory()));
            final TransBiSimulator tbs = new TransBiSimulator(tr, tau); tbs.run();
            //tbs = null;
            //System.out.println("RFT");
            final RemoveFollowOnTau rft = new RemoveFollowOnTau(tr, tau); rft.run();
            tr.removeAllSelfLoops(tau);
            AutomatonProxy minAutomaton3 = tr.getAutomaton(getFactory());
            //System.out.println("ANN");
            final AnnotateGraph an = new AnnotateGraph(tr, tau); an.run();
            tr.removeAllUnreachable();
            /*for (EventProxy event : mOriginalAlphabet) {
              if (mAllwaysEnabled.containsKey(event)) {
                Set<AutomatonProxy> auts = new HashSet<AutomatonProxy>(mAllwaysEnabled.get(event));
                auts.removeAll(mCompautomata);
                if (auts.isEmpty()) {
                  System.out.println("allways enabled");
                  tr.removeAllAnnotations(tr.eventToInt(event));
                }
              }
            }*/
            //RedundantTransitions rt = new RedundantTransitions(tr); rt.run();
            EquivalentIncoming eq = new EquivalentIncoming(tr); eq.run();
            //RemoveAnnotations ra = new RemoveAnnotations(tr); ra.run();
            //ConfRevBiSimulator rbs = new ConfRevBiSimulator(tr); rbs.run();
            BiSimulatorRedundant bsr = new BiSimulatorRedundant(tr, false); bsr.run();
            //AddRedundantTransitions ad = new AddRedundantTransitions(tr); ad.run();
            //bsr = new BiSimulatorRedundant(tr, true); bsr.run();
            //OptimisticBiSimulatorRedundant bsr = new OptimisticBiSimulatorRedundant(tr); bsr.run();
            //int thing = 4;
            /*mAnnBITIME -= System.currentTimeMillis();
            //System.out.println("TLR");
            minAutomaton = tr.getAutomaton(getFactory());
            System.out.println("BISIM");
            final BiSimulator sim =
                new BiSimulator(minAutomaton, getMarkingProposition(),
                    getFactory());
            mAnnotatedBISIMulation += minAutomaton.getStates().size();
            minAutomaton = sim.run();
            mAnnBITIME += System.currentTimeMillis();
            mAnnotatedBISIMulation -= minAutomaton.getStates().size();
            tr = new TransitionRelation(minAutomaton, getMarkingProposition());*/
            //System.out.println("EI");
            //System.out.println(tr.getAutomaton(getFactory()));
            //MakeBisimiliar mb = new MakeBisimiliar(tr); mb.run();
            //System.out.println(tr.getAutomaton(getFactory()));
            //eq = new EquivalentIncoming(tr); eq.run();
            //System.out.println("UA");
            final UnAnnotateGraph ua =
                new UnAnnotateGraph(tr, getMarkingProposition(), tauproxy);
            minAutomaton = ua.run(getFactory());
            //System.out.println(minAutomaton);
            tauevent = ua.getTau();
            tr = new TransitionRelation(minAutomaton, getMarkingProposition());
            tau = tr.getEventInt(tauevent);
            //System.out.println("IE");
            //System.out.println("before");
            //System.out.println(tr.getAutomaton(getFactory()));
            //System.out.println("make equivalent");
            //System.out.println("after");
            //System.out.println(tr.getAutomaton(getFactory()));
            //SilentOutGoing sog = new SilentOutGoing(tr, tau); sog.run(getFactory());
            //rt = new RedundantTransitions(tr); rt.run();
            //final RemoveUnneededTransitions rut = new RemoveUnneededTransitions(tr, tau); rut.run();
            // IncomingEquivalent ie = new IncomingEquivalent(tr, tau); ie.run();
            tr.removeAllUnreachable();
            con = new CertainConflict(tr, tau); con.run();
            //tr.removeAllUnreachable();
            //tbs = new TransBiSimulator(tr, tau); tbs.run();
            tr.removeAllUnreachable();
            minAutomaton = tr.getAutomaton(getFactory());
            ee.addSilentEvent(tauevent);
            ListBufferTransitionRelation abstracted =
              new ListBufferTransitionRelation(minAutomaton, ee, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
            /*Determinizer det = new Determinizer(orig, ee, ee.getEventCode(getMarkingProposition()));
            det.run();
            abstracted = det.getAutomaton();*/
            boolean stop = false;
            /*CompareLessConflicting clc = new CompareLessConflicting(orig, abstracted, ee.getEventCode(getMarkingProposition()));
            if (!clc.isLessConflicting()) {
              System.out.println("original more conf" );
              /*System.out.println(orig.toString());
              System.out.println(abstracted.toString());
              System.out.println(orig.createAutomaton(getFactory(), ee));
              System.out.println(minAutomaton);*/
              /*clc = new CompareLessConflicting(orig,
                new ListBufferTransitionRelation(minAutomaton3, ee, ListBufferTransitionRelation.CONFIG_SUCCESSORS),
                ee.getEventCode(getMarkingProposition()));
              System.out.println(clc.isLessConflicting());
              clc = new CompareLessConflicting(orig, orig, ee.getEventCode(getMarkingProposition()));
              System.out.println(clc.isLessConflicting());*/
              //System.exit(1);
              //stop = true;
            //}
            //clc = new CompareLessConflicting(abstracted, orig, ee.getEventCode(getMarkingProposition()));
            //if (!clc.isLessConflicting()) {
              //System.out.println("abstracted more conf" );
              /*System.out.println(orig.toString());
              System.out.println(abstracted.toString());
              System.out.println(orig.createAutomaton(getFactory(), ee));
              System.out.println(minAutomaton);*/
              /*clc = new CompareLessConflicting(orig,
                new ListBufferTransitionRelation(minAutomaton3, ee, ListBufferTransitionRelation.CONFIG_SUCCESSORS),
                ee.getEventCode(getMarkingProposition()));
              System.out.println(clc.isLessConflicting());
              clc = new CompareLessConflicting(orig, orig, ee.getEventCode(getMarkingProposition()));
              System.out.println(clc.isLessConflicting());*/
              //System.exit(1);
              //stop = true;
            //}
            if (stop) {System.exit(1);}
            /*abstracted = CompareLessConflicting.mergeConflictEquivalent(abstracted,
                                          ee.getEventCode(getMarkingProposition()));
            minAutomaton = abstracted.createAutomaton(getFactory(), ee);*/
            //System.out.println("less conflicting: " + clc.isLessConflicting());
            mStates += minAutomaton.getStates().size();
            System.out.println("minautomaton: " + minAutomaton.getStates().size());
            final int diff = compsize - minAutomaton.getStates().size();
            mAggDiff += diff;
            mSmallestDiff = mSmallestDiff > diff ? diff : mSmallestDiff;
            mLargestDiff = mLargestDiff < diff ? diff : mLargestDiff;
            try {
              for (final AutomatonProxy a : mCompautomata) {
                mWriter.write(a.getName());
                mWriter.newLine();
              }
              mWriter.write("compsize: " + compsize);
              mWriter.newLine();
              mWriter.write("minsize: " + minAutomaton.getStates().size());
              mWriter.newLine();
              mWriter.newLine();
              mWriter.flush();
            } catch (final Throwable t) {
              t.printStackTrace();
            }
            /*AutomatonProxy minAutomaton2 = minAutomaton;
            minAutomaton = temp;
            //System.out.println("half");
            mBITIME -= System.currentTimeMillis();
            sim = new BiSimulator(minAutomaton,
                                              getMarkingProposition(),
                                              getFactory());
            mBISIMulation += minAutomaton.getStates().size();
            minAutomaton = sim.run();
            mBITIME += System.currentTimeMillis();
            mBISIMulation -= minAutomaton.getStates().size();
            tr = new TransitionRelation(minAutomaton, getMarkingProposition());
            tau = tr.getEventInt(tauevent);
            ie = new IncomingEquivalent(tr, tau); ie.run();
            //System.out.println("before");
            //System.out.println(tr.getAutomaton(getFactory()));
            //System.out.println("make equivalent");
            //System.out.println("after");
            //System.out.println(tr.getAutomaton(getFactory()));
            rut = new RemoveUnneededTransitions(tr, tau); rut.run();
            ie = new IncomingEquivalent(tr, tau); ie.run();
            con = new CertainConflict(tr, tau);
            con.run();
            minAutomaton = tr.getAutomaton(getFactory());
            if (minAutomaton.getStates().size() > minAutomaton2.getStates().size()) {
              System.out.println("without ann: " + minAutomaton.getStates().size());
              System.out.println("with ann: " + minAutomaton2.getStates().size());
              minAutomaton = minAutomaton2;
            }*/
            // if (diff < 0) {throw new AnalysisException("exception");}
            //System.out.println(minAutomaton.getName());
            //System.out.println("origstates:" + origstates);
            //System.out.println("origtrans:" + origtrans);
            //System.out.println("states:" + minAutomaton.getStates().size());
            //System.out.println("trans:" + minAutomaton.getTransitions().size());
            //System.out.println("events:" + minAutomaton.getEvents().size());
            //System.out.println("hidden: " + mHidden);
          } else {
            final TransitionRelation tr = new TransitionRelation(minAutomaton, getMarkingProposition());
            //OptimisticBiSimulatorRedundant obsr = new OptimisticBiSimulatorRedundant(tr); obsr.run();
            //BiSimulatorRedundant bsr = new BiSimulatorRedundant(tr); bsr.run();
            minAutomaton = tr.getAutomaton(getFactory());
            //System.out.println(minAutomaton.getName());
            //System.out.println("no hide before: " + minAutomaton.getStates().size());
            mBITIME -= System.currentTimeMillis();
            mBISIMulation += minAutomaton.getStates().size();
            final BiSimulator sim = new BiSimulator(minAutomaton,
                                            getMarkingProposition(),
                                            getFactory());
            //mBISIMulation += minAutomaton2.getStates().size();
            minAutomaton = sim.run();
            mBISIMulation -= minAutomaton.getStates().size();
            mBITIME += System.currentTimeMillis();
            //System.out.println("no hide after: " + minAutomaton.getStates().size());
            //System.out.println("orig: " + mOriginalAlphabet);
            //System.out.println("hidden: " + mHidden);
            //mBISIMulation -= minAutomaton2.getStates().size();*/
          }
          /*if (minAutomaton.getStates().size() > minAutomaton2.getStates().size()) {
            System.out.println("SWITCH"); System.out.println("SWITCH");
            switched++;
            minAutomaton = minAutomaton2;
          }*/
          final TransitionRelation tr =
              new TransitionRelation(minAutomaton, getMarkingProposition());
          allwaysenabled = tr.getAllwaysEnabled();
          allselflooped = tr.getAllSelfLoops();
          // mMinAutMap.put(ah, minAutomaton);
        } catch (final AnalysisException exception) {
          mCompTime += System.currentTimeMillis();
          mStates += mMaxProjStates;
          //mMinAutMap.put(ah, null);
          throw exception;
        }
      //}
      // RemoveTransitions
      // mRIT.removeAutomata(mCompautomata);
      // mRIT.addAutomata(Collections.singleton(minAutomaton));
      // EndRemoveTransitions
      for (final EventProxy e : minAutomaton.getEvents()) {
        if (mAllSelfLoops.containsKey(e)) {
          mAllSelfLoops.get(e).removeAll(mCompautomata);
          mAllwaysEnabled.get(e).removeAll(mCompautomata);
          mAllSelfLoops.get(e).add(minAutomaton);
          mAllwaysEnabled.get(e).add(minAutomaton);
        }
      }
      for (final EventProxy e : allselflooped) {
        if (mAllSelfLoops.containsKey(e)) {
          mAllSelfLoops.get(e).remove(minAutomaton);
          /*
           * System.out.print("Auts: "); for (AutomatonProxy a :
           * mAllSelfLoops.get(e)) { System.out.print(a.getName() + " "); }
           * System.out.println();
           */
        }
      }
      for (final EventProxy e : allwaysenabled) {
        if (mAllSelfLoops.containsKey(e)) {
          mAllwaysEnabled.get(e).remove(minAutomaton);
          /*
           * System.out.print("Auts: "); for (AutomatonProxy a :
           * mAllwaysEnabled.get(e)) { System.out.print(a.getName() + " "); }
           * System.out.println();
           */
        }
      }
      //mRIT.addAutomata(Collections.singleton(minAutomaton));
      mAutomata.add(minAutomaton);
      mDontOnOwn.add(minAutomaton);
      mNew = minAutomaton;
      mTarget = new HashSet<EventProxy>();
      for (final AutomatonProxy a : mAutomata) {
        mTarget.addAll(a.getEvents());
      }
    }

    public ProductDESProxy getModel()
    {
      return getFactory().createProductDESProxy("model", mTarget, mAutomata);
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public TraceProxy getTrace(TraceProxy trace, final ProductDESProxy model)
    {
      final List<Map<StateProxy,Set<EventProxy>>> events =
          new ArrayList<Map<StateProxy,Set<EventProxy>>>(mCompautomata.size());
      final List<Map<Key,StateProxy>> automata =
          new ArrayList<Map<Key,StateProxy>>(mCompautomata.size());
      List<StateProxy> currstate =
          new ArrayList<StateProxy>(mCompautomata.size());
      final AutomatonProxy[] aut = new AutomatonProxy[mCompautomata.size()];
      int i = 0;
      for (final AutomatonProxy proxy : mCompautomata) {
        events.add(new HashMap<StateProxy,Set<EventProxy>>(proxy.getStates()
            .size()));
        automata
            .add(new HashMap<Key,StateProxy>(proxy.getTransitions().size()));
        final Set<EventProxy> autevents = new HashSet<EventProxy>(mOriginalAlphabet);
        // System.out.println(autevents);
        autevents.removeAll(proxy.getEvents());
        // System.out.println(autevents);
        int init = 0;
        final Set<StateProxy> states = proxy.getStates();
        for (final StateProxy s : states) {
          if (s.isInitial()) {
            init++;
            currstate.add(s);
          }
          events.get(i).put(s, new HashSet<EventProxy>(autevents));
        }
        assert (init == 1);
        final Collection<TransitionProxy> trans = proxy.getTransitions();
        for (final TransitionProxy t : trans) {
          events.get(i).get(t.getSource()).add(t.getEvent());
          automata.get(i).put(new Key(t.getSource(), t.getEvent()),
                              t.getTarget());
        }
        aut[i] = proxy;
        i++;
      }
      Queue<Place> stateList = new PriorityQueue<Place>();
      Place place = new Place(currstate, null, 0, null);
      stateList.offer(place);
      final List<EventProxy> oldevents = trace.getEvents();
      // System.out.println(oldevents);

      final Set<Place> visited = new HashSet<Place>();
      visited.add(place);
      while (true) {
        place = stateList.poll();
        // System.out.println(place.getTrace());
        if (place.mIndex >= oldevents.size()) {
          break;
        }
        currstate = place.mCurrState;
        final Set<EventProxy> possevents = new HashSet<EventProxy>(mHidden);
        // System.out.println(mHidden);
        hidden: for (final EventProxy pe : possevents) {
          // System.out.println(pe);
          final List<StateProxy> newstate =
              new ArrayList<StateProxy>(currstate.size());
          for (i = 0; i < currstate.size(); i++) {
            if (aut[i].getEvents().contains(pe)) {
              final StateProxy t = automata.get(i).get(new Key(currstate.get(i), pe));
              // System.out.println(t);
              if (t == null) {
                continue hidden;
              }
              newstate.add(t);
            } else {
              newstate.add(currstate.get(i));
            }
          }
          // System.out.println(newstate);
          final Place newPlace = new Place(newstate, pe, place.mIndex, place);
          if (visited.add(newPlace)) {
            stateList.offer(newPlace);
          }
        }
        final EventProxy currevent = oldevents.get(place.mIndex);
        final List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
        boolean contains = true;
        for (i = 0; i < currstate.size(); i++) {
          if (aut[i].getEvents().contains(currevent)) {
            final StateProxy t =
                automata.get(i).get(new Key(currstate.get(i), currevent));
            if (t == null) {
              contains = false;
            }
            newstate.add(t);
          } else {
            newstate.add(currstate.get(i));
          }
        }
        final Place newPlace =
            new Place(newstate, currevent, place.mIndex + 1, place);
        if (contains && visited.add(newPlace)) {
          stateList.offer(newPlace);
        }
        assert (!stateList.isEmpty());
      }
      stateList = null;
      final ProductDESProxy mod = mParent == null ? model : mParent.getModel();
      trace = getFactory().createSafetyTraceProxy(mod, place.getTrace());
      return mParent == null ? trace : mParent.getTrace(trace, model);
    }


    private class Place implements Comparable<Place>
    {
      public final List<StateProxy> mCurrState;
      public final EventProxy mEvent;
      public final int mIndex;
      public final Place mParent;

      public Place(final List<StateProxy> currState, final EventProxy event, final int index,
                   final Place parent)
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
        final List<EventProxy> events = mParent.getTrace();
        events.add(mEvent);
        return events;
      }

      public int compareTo(final Place other)
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

      public boolean equals(final Object o)
      {
        final Place p = (Place) o;
        return p.mIndex == mIndex && p.mCurrState.equals(mCurrState);
      }
    }


    private class Key
    {
      private final StateProxy mState;
      private final EventProxy mEvent;
      private final int mHash;

      public Key(final StateProxy state, final EventProxy event)
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

  private static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }


  private static class AutomataHidden
  {
    public final Set<AutomatonProxy> mAutomata;
    public final Set<EventProxy> mHidden;

    @SuppressWarnings("unused")
    public AutomataHidden(final Set<AutomatonProxy> automata, final Set<EventProxy> hidden)
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

    public boolean equals(final Object o)
    {
      if (o instanceof AutomataHidden) {
        final AutomataHidden a = (AutomataHidden) o;
        return mAutomata.equals(a.mAutomata) && mHidden.equals(a.mHidden);
      }
      return false;
    }
  }


  @SuppressWarnings("unused")
  private static class Tuple implements Comparable<Tuple>
  {
    public final Set<AutomatonProxy> mSet;
    public final double mSize;

    public Tuple(final Set<AutomatonProxy> set, final double size)
    {
      mSet = set;
      mSize = size;
    }

    public int compareTo(final Tuple t)
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
  private int mStates;
  private int mMaxProjStates;
  private final Map<AutomataHidden,AutomatonProxy> mMinAutMap =
      new HashMap<AutomataHidden,AutomatonProxy>();
  private final Set<AutomataHidden> mChecked = new HashSet<AutomataHidden>();
  private final Set<AutomatonProxy> mDontOnOwn = new HashSet<AutomatonProxy>();
  private final Map<EventProxy,Set<AutomatonProxy>> mAllSelfLoops =
      new THashMap<EventProxy,Set<AutomatonProxy>>();
  private final Map<EventProxy,Set<AutomatonProxy>> mAllwaysEnabled =
      new THashMap<EventProxy,Set<AutomatonProxy>>();
  private int mLargestComposition = 0;
  private int mLargestTransitions = 0;
  private int mAggComposition = 0;
  private int mAggTransitions = 0;
  private TObjectIntHashMap<Set<AutomatonProxy>> mNumOccurinng = null;
  private TObjectIntHashMap<Set<AutomatonProxy>> mCommon = null;

  private int mAnnotatedBISIMulation = 0;
  private int mBISIMulation = 0;
  private int mCompTime = 0;
  private int mAnnBITIME = 0;
  private int mBITIME = 0;
  private int mSmallestDiff = Integer.MAX_VALUE;
  private int mLargestDiff = Integer.MIN_VALUE;
  private int mAggDiff = 0;
  private int maxsize = 1000;
  private int mTime = 0;

  private BufferedWriter mWriter = null;


  @SuppressWarnings("unused")
  private final RemoveImpossibleTransitions mRIT = null;
  @SuppressWarnings("unused")
  private final MergeEvents mME = null;

  // #########################################################################
  // # Class Constants
  @SuppressWarnings("unused")
  private static final Logger LOGGER =
      LoggerFactory.createLogger(ProjectingNonBlockingChecker.class);

}
