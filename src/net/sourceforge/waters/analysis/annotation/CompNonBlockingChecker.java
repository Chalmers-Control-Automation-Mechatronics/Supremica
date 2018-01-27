//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.modular.BiSimulator;
import net.sourceforge.waters.analysis.modular.BlockedEvents;
import net.sourceforge.waters.analysis.modular.NonDeterministicComposer;
import net.sourceforge.waters.analysis.modular.TransBiSimulator;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
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


/**
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
      final TransitionRelation tr = new TransitionRelation(auto, getConfiguredDefaultMarking());
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

  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    System.out.println("run comp conf");
    clearStats();
    mTime -= System.currentTimeMillis();
    if (getConfiguredDefaultMarking() == null) {
      setConfiguredDefaultMarking(getUsedDefaultMarking());
    }
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
        new NativeConflictChecker(list.getModel(), getConfiguredDefaultMarking(),
                                  getFactory());
      result = checker.run();
      checkerstates = checker.getAnalysisResult().getTotalNumberOfStates();
    } catch (final CertainConflictException cce) {
      //System.out.println("caught:" + cce);
      result = false;
    }
    if (!result) {
      final List<EventProxy> e = new ArrayList<EventProxy>();
      final TraceProxy counter = getFactory().createSafetyTraceProxy(getModel().getName(),
                                                               getModel(), e);
      setFailedResult(counter);
    } else {
      setSatisfiedResult();
    }
    System.out.println("result: " + result);
    System.out.println("checkerstates: " + checkerstates);
    mTime += System.currentTimeMillis();
    try {
      final BufferedWriter write = new BufferedWriter(new FileWriter("/home/darius/Projects/supr/supremica/hugo/" + getModel().getName()));
      write.append(getStats());
      write.append("states:" + checkerstates + "\n");
      write.close();
    } catch (final Throwable t) {
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

  @Override
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
    stats += EquivalentIncoming.stats() + "\n";
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
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
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

  @SuppressWarnings("unused")
  private Collection<Set<AutomatonProxy>> getMinTransitions(final ProductDESProxy model, final Set<AutomatonProxy> automata)
  {
    final List<Set<AutomatonProxy>> pairs = new ArrayList<Set<AutomatonProxy>>();
    AutomatonProxy minaut = null;
    for (final AutomatonProxy aut : automata) {
      minaut = minaut == null ? aut : minaut;
      minaut = minaut.getTransitions().size() > aut.getTransitions().size() ? aut : minaut;
    }
    for (final AutomatonProxy aut : automata) {
      if (minaut == aut) {continue;}
      final Set<AutomatonProxy> pair = new THashSet<AutomatonProxy>(2);
      pair.add(minaut);
      pair.add(aut);
      pairs.add(pair);
    }
    final TObjectIntHashMap<Set<AutomatonProxy>> numoccuring =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    for (final EventProxy e : model.getEvents()) {
      if (e == getConfiguredDefaultMarking()) {
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
    Collections.sort(pairs, new Comparator<Set<AutomatonProxy>>() {
        @Override
        public int compare(final Set<AutomatonProxy> a1, final Set<AutomatonProxy> a2)
        {
          final int local1 = numoccuring.get(a1);
          final int local2 = numoccuring.get(a2);
          if (local1 != local2) {return local2 - local1;}
          final Collection<EventProxy> CommonEvents1 = new THashSet<EventProxy>(model.getEvents());
          final Collection<EventProxy> CommonEvents2 = new THashSet<EventProxy>(model.getEvents());
          for (final AutomatonProxy aut : a1) {
            CommonEvents1.retainAll(aut.getEvents());
          }
          for (final AutomatonProxy aut : a2) {
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

  @SuppressWarnings("unused")
  private Collection<Set<AutomatonProxy>> getTuples(final ProductDESProxy model, final Set<AutomatonProxy> automata)
  {
    final SortedMap<SortedSet<AutomatonProxy>, Integer> numoccuring =
      new TreeMap<SortedSet<AutomatonProxy>,Integer>(new AutomataComparator());
    Events:
    for (final EventProxy e : model.getEvents()) {
      if (e == getConfiguredDefaultMarking()) {
        continue;
      }
      final SortedSet<AutomatonProxy> possess = new TreeSet<AutomatonProxy>();
      for (final AutomatonProxy a : automata) {
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
    final List<Set<AutomatonProxy>> possible =
      new ArrayList<Set<AutomatonProxy>>();
    //System.out.println("keyset:" + numoccuring.keySet().size());
    for (final Set<AutomatonProxy> s : numoccuring.keySet()) {
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
      heur.put(s, size);
    }
    Collections.sort(possible, new Comparator<Set<AutomatonProxy>>() {
        @Override
        public int compare(final Set<AutomatonProxy> a1, final Set<AutomatonProxy> a2)
        {
          final double heur1 = numoccuring.get(a1);
          final double heur2 = numoccuring.get(a2);
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
    @Override
    public int compare(final SortedSet<AutomatonProxy> s1, final SortedSet<AutomatonProxy> s2)
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

  private ProjectionList project(final ProductDESProxy model)
    throws AnalysisException, CertainConflictException
  {
    eventscheck(model);
    //mRIT = new RemoveImpossibleTransitions(getMarkingProposition());
    //mME = new MergeEvents(getMarkingProposition(), model.getEvents());
    maxsize = 400000;
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
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/home/darius/supremicastuff/" + model.getName()));
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    //mRIT.addAutomata(automata);
    ProjectionList p = null;
    while (true) {
      //automata = mME.run(automata, getFactory());
      //System.out.println("numautomata:" + automata.size());
      //Set<Tuple> possible = getTuples(model, automata);
      //Collection<Set<AutomatonProxy>> possible = getMinTransitions(model, automata);
      final Set<AutomatonProxy> set = getFromReader(automata, reader);
      if (set == null) {break;}
      boolean stop = true;
      ProjectionList minlist = null;
      minSize = Integer.MAX_VALUE / 4;
      //System.out.println("possible: " + possible.size());
      //for (Tuple tup : possible) {
      //for (Set<AutomatonProxy> set : possible) {
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
            //break;
          }
        } catch (final AnalysisException exception) {
          // TODO This can't be right ~~~Robi
          //exception.printStackTrace();
          //System.out.println("over");
          //overflows++;
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
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    final Iterator<AutomataHidden> it = mMinAutMap.keySet().iterator();
    while (it.hasNext()) {
      final AutomataHidden ah = it.next();
      if (!mChecked.contains(ah)) {
        it.remove();
      }
    }
    return p;
  }

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


  //#########################################################################
  //# Inner Class AutomatonComparator
  @SuppressWarnings("unused")
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    @Override
    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
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

    private boolean containsAny(final Set<EventProxy> contains, final Set<EventProxy> of)
    {
      for (final EventProxy e : of) {
        if (contains.contains(e)) {return true;}
      }
      return false;
    }

    @SuppressWarnings("unused")
    private void blockedEvents()
    {
      final Set<AutomatonProxy> mTempComp = new THashSet<AutomatonProxy>();
      final Set<AutomatonProxy> mTempAut = new THashSet<AutomatonProxy>();
      //System.out.println("before");
      for (final AutomatonProxy aut : mCompautomata) {
        AutomatonProxy aut1 = aut;
        mTempAut.clear();
        for (final AutomatonProxy aut2 : mAutomata) {
          if (containsAny(aut1.getEvents(), aut2.getEvents())) {
            List<AutomatonProxy> tocomp =
              Arrays.asList(new AutomatonProxy[]{aut1, aut2});
            final BlockedEvents be = new BlockedEvents(tocomp, getFactory(), getConfiguredDefaultMarking());
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
      mParent = parent;
      mCompautomata = new TreeSet<AutomatonProxy>(compAutomata);
      mAutomata = new TreeSet<AutomatonProxy>(automata);
      final Set<EventProxy> events = new TreeSet<EventProxy>();
      for (final AutomatonProxy a : compAutomata) {
        events.addAll(a.getEvents());
      }
      mAutomata.removeAll(compAutomata);
      //System.out.println("events: " + events);
      mOriginalAlphabet = events;
      mHidden = new HashSet<EventProxy>(events);
      for (final AutomatonProxy a : mAutomata) {
        //System.out.println("before comp:" + a);
        if (!compAutomata.contains(a)) {
          mHidden.removeAll(a.getEvents());
        }
      }
      if (mHidden.contains(getConfiguredDefaultMarking())) {
        mHidden.remove(getConfiguredDefaultMarking());
      }
      final AutomataHidden ah =
        new AutomataHidden(compAutomata, new HashSet<EventProxy>(mHidden));
      mChecked.add(ah);
      AutomatonProxy minAutomaton;
      mMinAutMap.remove(ah);
      if (mMinAutMap.containsKey(ah)) {
        minAutomaton = mMinAutMap.get(ah);
        if (minAutomaton == null) {
          throw new OverflowException();
        }
      } else {
        try {
          //removeTransitions
          //end remove transitions
          final NonDeterministicComposer composer =
            new NonDeterministicComposer(
              new ArrayList<AutomatonProxy>(mCompautomata), getFactory(),
                                            getConfiguredDefaultMarking());
          final int size = maxsize;
          //System.out.println(size);
          composer.setNodeLimit(size);
          mCompTime -= System.currentTimeMillis();
          minAutomaton = composer.run();
          //System.out.println("compaut:" + minAutomaton);
          mCompTime += System.currentTimeMillis();
          final int compsize = minAutomaton.getStates().size();
          final int comptransitions = minAutomaton.getTransitions().size();
          mAggComposition += compsize;
          mAggTransitions += comptransitions;
          mLargestTransitions = mLargestTransitions > comptransitions ? mLargestTransitions :
                                comptransitions;
          mLargestComposition = mLargestComposition > minAutomaton.getStates().size() ? mLargestComposition :
                                minAutomaton.getStates().size();
          final AutomatonProxy minAutomaton2 = minAutomaton;
          //System.out.println("compsize:" + compsize);
          if (!mHidden.isEmpty()) {
            //System.out.println("hiding:" + mHidden.size());
            final TransitionRelation tr = new TransitionRelation(minAutomaton,
                                                           getConfiguredDefaultMarking());
            final int tau = tr.mergeEvents(mHidden, getFactory());
            // EventProxy tauevent = tr.getEvent(tau);
            //System.out.println("TLR");
            TauLoopRemoval tlr = new TauLoopRemoval(tr, tau); tlr.run();
            //System.out.println("CC");
            //System.out.println(tr.getAutomaton(getFactory()));
            CertainConflict con = new CertainConflict(tr, tau);
            if (!con.run()) {throw new CertainConflictException();}
            tr.removeAllUnreachable();
            // IncomingEquivalent ie = new IncomingEquivalent(tr, tau); ie.run();
            EquivalentIncoming ie = new EquivalentIncoming(tr); ie.run();
            final RemoveUnneededTransitions rut = new RemoveUnneededTransitions(tr, tau); rut.run();
            tr.removeAllUnreachable();
            // ie = new IncomingEquivalent(tr, tau); ie.run();
            ie = new EquivalentIncoming(tr); ie.run();
            //System.out.println("after remove:" + tr.getAutomaton(getFactory()));
            tlr = new TauLoopRemoval(tr, tau); tlr.run();
            tr.removeAllUnreachable();
            final TransBiSimulator tbs = new TransBiSimulator(tr, tau); tbs.run();
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
            final int diff = compsize - minAutomaton.getStates().size();
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
            final BiSimulator sim = new BiSimulator(minAutomaton2,
                                            getConfiguredDefaultMarking(),
                                            getFactory());
            //mBISIMulation += minAutomaton2.getStates().size();
            minAutomaton = sim.run();
            mBISIMulation -= minAutomaton.getStates().size();
            mBITIME += System.currentTimeMillis();
            //mBISIMulation -= minAutomaton2.getStates().size();
          }
          // TransitionRelation tr = new TransitionRelation(minAutomaton, getMarkingProposition());
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
      for (final AutomatonProxy a : mAutomata) {
        mTarget.addAll(a.getEvents());
      }
    }

    @SuppressWarnings("unused")
    private TIntHashSet getIntSet(final Set<EventProxy> set, final EventProxy[] array)
    {
      final TIntHashSet res = new TIntHashSet(set.size());
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

    @SuppressWarnings("unused")
    public Set<EventProxy> getHidden()
    {
      return mHidden;
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
      final List<Map<StateProxy, Set<EventProxy>>> events =
        new ArrayList<Map<StateProxy, Set<EventProxy>>>(mCompautomata.size());
      final List<Map<Key, StateProxy>> automata =
        new ArrayList<Map<Key, StateProxy>>(mCompautomata.size());
      List<StateProxy> currstate = new ArrayList<StateProxy>(mCompautomata.size());
      final AutomatonProxy[] aut = new AutomatonProxy[mCompautomata.size()];
      int i = 0;
      for (final AutomatonProxy proxy : mCompautomata) {
        events.add(new HashMap<StateProxy, Set<EventProxy>>(proxy.getStates().size()));
        automata.add(new HashMap<Key, StateProxy>(proxy.getTransitions().size()));
        final Set<EventProxy> autevents = new HashSet<EventProxy>(mOriginalAlphabet);
        //System.out.println(autevents);
        autevents.removeAll(proxy.getEvents());
        //System.out.println(autevents);
        int init = 0;
        final Set<StateProxy> states = proxy.getStates();
        for (final StateProxy s : states) {
          if (s.isInitial()) {
            init++;
            currstate.add(s);
          }
          events.get(i).put(s, new HashSet<EventProxy>(autevents));
        }
        assert(init == 1);
        final Collection<TransitionProxy> trans = proxy.getTransitions();
        for (final TransitionProxy t : trans) {
          events.get(i).get(t.getSource()).add(t.getEvent());
          automata.get(i).put(new Key(t.getSource(), t.getEvent()), t.getTarget());
        }
        aut[i] = proxy;
        i++;
      }
      Queue<Place> stateList = new PriorityQueue<Place>();
      Place place = new Place(currstate, null, 0, null);
      stateList.offer(place);
      final List<EventProxy> oldevents = trace.getEvents();
      //System.out.println(oldevents);

      final Set<Place> visited = new HashSet<Place>();
      visited.add(place);
      while (true) {
        place = stateList.poll();
        //System.out.println(place.getTrace());
        if (place.mIndex >= oldevents.size()) {
          break;
        }
        currstate = place.mCurrState;
        final Set<EventProxy> possevents = new HashSet<EventProxy>(mHidden);
        //System.out.println(mHidden);
        hidden:
        for (final EventProxy pe : possevents) {
          //System.out.println(pe);
          final List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
          for (i = 0; i < currstate.size(); i++) {
            if (aut[i].getEvents().contains(pe)) {
              final StateProxy t = automata.get(i).get(new Key(currstate.get(i), pe));
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
            final StateProxy t = automata.get(i).get(new Key(currstate.get(i), currevent));
            if (t == null) {
              contains = false;
            }
            newstate.add(t);
          } else {
            newstate.add(currstate.get(i));
          }
        }
        final Place newPlace = new Place(newstate, currevent, place.mIndex + 1, place);
        if (contains && visited.add(newPlace)) {
          stateList.offer(newPlace);
        }
        assert(!stateList.isEmpty());
      }
      stateList = null;
      final ProductDESProxy mod = mParent == null ? model : mParent.getModel();
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

      public Place(final List<StateProxy> currState, final EventProxy event,
                   final int index, final Place parent)
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

      @Override
      public int compareTo(final Place other)
      {
        return other.mIndex - mIndex;
      }

      @Override
      public int hashCode()
      {
        int hash = 7;
        hash = hash + mIndex * 31;
        hash = hash + mCurrState.hashCode();
        return hash;
      }

      @Override
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

      @Override
      public int hashCode()
      {
        return mHash;
      }

      @Override
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

    public AutomataHidden(final Set<AutomatonProxy> automata, final Set<EventProxy> hidden)
    {
      mAutomata = automata;
      mHidden = hidden;
    }

    @Override
    public int hashCode()
    {
      int code = 31 + mAutomata.hashCode();
      code = code * 31 + mHidden.hashCode();
      return code;
    }

    @Override
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
  private static class Tuple
    implements Comparable<Tuple>
  {
    public final Set<AutomatonProxy> mSet;
    public final double mSize;

    public Tuple(final Set<AutomatonProxy> set, final double size)
    {
      mSet = set;
      mSize = size;
    }

    @Override
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
  private final Map<AutomataHidden, AutomatonProxy> mMinAutMap =
    new HashMap<AutomataHidden, AutomatonProxy>();
  private final Set<AutomataHidden> mChecked = new HashSet<AutomataHidden>();
  private final Set<AutomatonProxy> mDontOnOwn = new HashSet<AutomatonProxy>();
  private final Map<EventProxy, Set<AutomatonProxy>> mAllSelfLoops =
    new THashMap<EventProxy, Set<AutomatonProxy>>();
  private final Map<EventProxy, Set<AutomatonProxy>> mAllwaysEnabled =
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
  private int mTime = 0;

}
