//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AlphaNonBlockingChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.THashSet;
import gnu.trove.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.composing.AllSame;
import net.sourceforge.waters.analysis.modular.BlockedEvents;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
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
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * The canonicalising generalised conflict check algorithm.
 *
 * @author Simon Ware
 */

public class AlphaNonBlockingChecker extends AbstractConflictChecker
    implements ConflictChecker
{

  // #########################################################################
  // # Constructors
  public AlphaNonBlockingChecker(final ProductDESProxy model,
                                 final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mStates = 0;
    setNodeLimit(10000000);
    mAlpha = factory.createEventProxy(":nonblockingalpha",
                                      EventKind.PROPOSITION);
    mCont = factory.createEventProxy(":cont",
                                      EventKind.CONTROLLABLE);
    mAbstractionRules = new LinkedList<AbstractionRule>();
  }

  public boolean supportsNondeterminism()
  {
    return true;
  }

  public SortedSet<AutomatonProxy> addAlpha(final Set<AutomatonProxy> model)
  {
    final SortedSet<AutomatonProxy> newmodel = new TreeSet<AutomatonProxy>();
    for (final AutomatonProxy aut : model) {
      final EventEncoding ee = new EventEncoding(aut, getKindTranslator());
      if (!ee.getEvents().contains(getConfiguredDefaultMarking())) {
        ee.addEvent(getConfiguredDefaultMarking(), getKindTranslator(), true);
      }
      ee.addEvent(mAlpha, getKindTranslator(), true);
      ee.addEvent(mCont, getKindTranslator(), false);
      //ee.addEvent(getMarkingProposition(), true);
      final int alpha = ee.getEventCode(mAlpha);
      //int cont = ee.getEventCode(mCont);
      System.out.println(alpha);
      try {
        final ListBufferTransitionRelation tr =
          new ListBufferTransitionRelation(aut, ee,
                                           ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        newmodel.add(tr.createAutomaton(getFactory(), ee));
      } catch (final Throwable t) {
        System.out.println(t);
      }
    }
    return newmodel;
  }

  // #########################################################################
  // # Invocation
  public boolean run() throws AnalysisException
  {
    clearStats();
    setUp();
    new AllSame(getModel()); // TODO Is this used?
    mTime -= System.currentTimeMillis();
    if (getConfiguredDefaultMarking() == null) {
      setConfiguredDefaultMarking(getUsedDefaultMarking());
    }
    /*
    try {
      mWriter = new BufferedWriter(new FileWriter("/home/darius/supremicastuff/reductions" + getModel().getName()));
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    Runtime.getRuntime().traceMethodCalls(true);
    */
    boolean result = false;
    final double checkerstates = 0;

      final ProjectionList list = project(getModel());
      System.out.println("done proj");
      mMinAutMap.clear();
      //System.out.println(list);
      if (list == null) {
        return true;
      }
      System.out.println("1");
      //System.out.println(list.getModel());
      //System.out.println(list.getModel());
      final ConflictChecker checker =
        new NativeConflictChecker(list.getModel(), getConfiguredDefaultMarking(),
                                      getFactory());
      checker
          .setConfiguredPreconditionMarking(mAlpha);
      checker.setNodeLimit(50000000);
      System.out.println("2");
      result = checker.run();
     System.out.println(result);
     System.out.println("3");
      mFinalStates = checker.getAnalysisResult().getTotalNumberOfStates();
      mFinalTrans = checker.getAnalysisResult().getTotalNumberOfTransitions();

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
      System.out.println("writing");
      final BufferedWriter write = new BufferedWriter(new FileWriter("/home/darius/Projects/supr/supremica/alpha/" + getModel().getName()));
      write.append(getStats());
      //write.append("states:" + checkerstates + "\n");
      write.append("result:" + result);
      write.close();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    //System.out.println("Total Switched: " + switched);
    clearStats();
    /*
    try {
      mWriter.flush();
      mWriter.close();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
    */
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
    /*mLargestComposition = 0;
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
    mAggTransitions = 0;*/
  }

  public String getStats()
  {
    String stats = getModel().getName() + "\n";
    stats += "peak states: " + mPeakstates + "\n";
    stats += "total states: " + mTotalstates + "\n";
    stats += "peak transitions: " + mPeakTransitions + "\n";
    stats += "total transitions: " + mTotalTransitions + "\n";
    stats += "final checkerstates: " + mFinalStates + "\n";
    stats += "final checkertrans: " + mFinalTrans + "\n";
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

  public void setUp()
    throws AnalysisException
  {
    super.setUp();
    final Set<EventProxy> mPropositions = new HashSet<EventProxy>();
    final ProductDESProxyFactory factory = getFactory();
    mPropositions.add(mAlpha);
    mPropositions.add(getUsedDefaultMarking());
    final EventProxy alpha = mAlpha;
    final EventProxy omega = getUsedDefaultMarking();
    System.out.println(mPropositions);
    final TauLoopRemovalRule tlrRule =
        new TauLoopRemovalRule(factory, getKindTranslator(), mPropositions);
    mAbstractionRules.add(tlrRule);

    final ObservationEquivalenceRule oeRule =
        new ObservationEquivalenceRule(factory, getKindTranslator(), mPropositions);
    oeRule.setTransitionLimit(6500);
    mAbstractionRules.add(oeRule);

    final RemovalOfAlphaMarkingsRule ramRule =
        new RemovalOfAlphaMarkingsRule(factory, getKindTranslator(), mPropositions);
    ramRule.setAlphaMarking(alpha);
    mAbstractionRules.add(ramRule);

    final RemovalOfDefaultMarkingsRule rdmRule =
        new RemovalOfDefaultMarkingsRule(factory, getKindTranslator(), mPropositions);
    rdmRule.setAlphaMarking(alpha);
    rdmRule.setDefaultMarking(omega);
    mAbstractionRules.add(rdmRule);

    final RemovalOfNoncoreachableStatesRule rnsRule =
        new RemovalOfNoncoreachableStatesRule(factory, getKindTranslator(),
                                              mPropositions);
    rnsRule.setAlphaMarking(alpha);
    rnsRule.setDefaultMarking(omega);
    mAbstractionRules.add(rnsRule);

    final DeterminisationOfNonAlphaStatesRule dnasRule =
        new DeterminisationOfNonAlphaStatesRule(factory, getKindTranslator(),
                                                mPropositions);
    dnasRule.setAlphaMarking(alpha);
    dnasRule.setTransitionLimit(6500);
    mAbstractionRules.add(dnasRule);

    final RemovalOfTauTransitionsLeadingToNonAlphaStatesRule rttlnsRule =
        new RemovalOfTauTransitionsLeadingToNonAlphaStatesRule
              (factory, getKindTranslator(), mPropositions);
    rttlnsRule.setAlphaMarking(alpha);
    rttlnsRule.setRestrictsToUnreachableStates(true);
    mAbstractionRules.add(rttlnsRule);

    final RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule rttonsRule =
        new RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(
            factory, getKindTranslator(), mPropositions);
    rttonsRule.setAlphaMarking(alpha);
    rttonsRule.setDefaultMarking(omega);
    mAbstractionRules.add(rttonsRule);
  }

  public void setuplocal(final ProductDESProxy model, final Set<AutomatonProxy> automata)
  {
    final TObjectIntHashMap<Set<AutomatonProxy>> numlocal =
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
      numlocal.put(possess, numlocal.get(possess) + 1);
    }
    mNumlocal = numlocal;
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
    System.out.println("fixed mintrans");
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

  private List<Set<AutomatonProxy>> getMinTransitions(final ProductDESProxy model, final SortedSet<AutomatonProxy> automata)
  {
    final TObjectIntHashMap<Set<AutomatonProxy>> common =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    final List<Set<AutomatonProxy>> pairs = new ArrayList<Set<AutomatonProxy>>();
    AutomatonProxy minaut = null;
    System.out.println("realy fixed min trans");
    for (final AutomatonProxy aut : automata) {
      minaut = minaut == null ? aut : minaut;
      minaut = minaut.getTransitions().size() > aut.getTransitions().size() ? aut : minaut;
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
        numoccuring.put(possess, numoccuring.get(possess) + 1);
      }
    }
    //System.out.println(pairs);
    /*Collections.sort(pairs, new Comparator<Set<AutomatonProxy>>() {
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
    });*/
    mCommon = common;
    mNumlocal = numoccuring;
    return pairs;
  }

  /*private Set<AutomatonProxy> pollList(List<AutomatonProxy> possible)
  {
    Set<AutomatonProxy> min = Collections.min(possible, new Comparator<Set<AutomatonProxy>>() {
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
    possible.remove(min);
    return min;
  }*/

  @SuppressWarnings("unused")
  private Set<AutomatonProxy> getMinSync(final List<Set<AutomatonProxy>> auts)
  {
    int minautnum = 65000;
    Set<AutomatonProxy> minauts = null;
    for (final Set<AutomatonProxy> automata : auts) {
      final Set<EventProxy> alpha = new THashSet<EventProxy>();
      for (final AutomatonProxy aut : automata) {
        alpha.addAll(aut.getEvents());
      }
      final ProductDESProxy compmodel = getFactory().createProductDESProxy("temp",
                                                                     alpha,
                                                                     automata);
      final String compname = "";
      final boolean first = true;
      try {
        final MonolithicSynchronousProductBuilder composer =
          new MonolithicSynchronousProductBuilder(compmodel, getFactory());
        final List<EventProxy> propositions = new ArrayList<EventProxy>();
        propositions.add(getConfiguredDefaultMarking());
        propositions.add(mAlpha);
        composer.setPropositions(propositions);
        final int size = maxsize;
        //System.out.println(size);
        //composer.setTransitionLimit(maxsize * maxsize);
        composer.setNodeLimit(6500);
        composer.setTransitionLimit(10000000);
        composer.run();
        final int compnum = composer.getComputedAutomaton().getStates().size();
        System.out.println("compsize: " + compnum);
        if (compnum < minautnum) {
          minauts = automata;
          minautnum = compnum;
        }
      } catch (final Throwable t) {
      }
    }
    return minauts;
  }

  private Set<AutomatonProxy> getMinSet(final List<Set<AutomatonProxy>> auts)
  {
    int maxlocal = -1;
    int maxcommon = -1;
    int i = -1;
    for (int index = 0; index < auts.size(); index++) {
      final Set<AutomatonProxy> set = auts.get(index);
      final int local = mNumlocal.get(set);
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

  /*
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
    Collections.sort(possible, new Comparator<Set<AutomatonProxy>>() {
        public int compare(Set<AutomatonProxy> a1, Set<AutomatonProxy> a2)
        {
          double heur1 = mNumOccurinng.get(a1);
          double heur2 = mNumOccurinng.get(a2);
          if (heur1 < heur2) {return -1;}
          else if (heur1 == heur2) {return 0;}
          else {return 1;}
        }
    });
    return possible;
  }
  */


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

  @SuppressWarnings("unused")
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
    throws AnalysisException
  {
    //mRIT = new RemoveImpossibleTransitions(getMarkingProposition());
    //mME = new MergeEvents(getMarkingProposition(), model.getEvents());
    maxsize = 10000;
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
    automata = addAlpha(automata);
    /*BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/home/darius/supremicastuff/" + model.getName()));
    } catch (final Throwable t) {
      t.printStackTrace();
    }*/
    //mRIT.addAutomata(automata);
    ProjectionList p = null;
    final Collection<AutomatonProxy> tautomata = automata;
    for (final AutomatonProxy a : tautomata) {
      p = new ProjectionList(p, automata, Collections.singleton(a));
      automata = new TreeSet<AutomatonProxy>(p.getAutomata());
      /*final ConflictChecker checker =
        new NativeConflictChecker(p.getModel(), getMarkingProposition(),
                                      getFactory());
      checker
          .setPreconditionMarking(mAlpha);
      boolean result = checker.run();
      if (!result) {
        System.out.println(checker.getCounterExample());
        MonolithicSynchronousProductBuilder composer =
          new MonolithicSynchronousProductBuilder(p.getModel(), getFactory());
        List<EventProxy> propositions = new ArrayList<EventProxy>();
        propositions.add(getMarkingProposition());
        propositions.add(mAlpha);
        composer.setPropositions(propositions);
        final int size = maxsize;
        //System.out.println(size);
        //composer.setTransitionLimit(maxsize * maxsize);
        composer.setNodeLimit(6500);
        composer.setTransitionLimit(10000000);
        composer.run();
        System.out.println(composer.getComputedAutomaton());
        System.exit(1);
      }*/
    }
    while (true) {
      //setuplocal(model, automata);
      //automata = mME.run(automata, getFactory());
      //System.out.println("numautomata:" + automata.size());
      //Set<Tuple> possible = getTuples(model, automata);
      final List<Set<AutomatonProxy>> possible = getMinTransitions(model, automata);
      //final Set<AutomatonProxy> set = getFromReader(automata, reader);
      //if (set == null) {break;}
      boolean stop = true;
      ProjectionList minlist = null;
      minSize = Integer.MAX_VALUE / 4;
      System.out.println("Automata: " +automata.size());
      //for (Tuple tup : possible) {
      //for (Set<AutomatonProxy> set : possible) {
      while (!possible.isEmpty()) {
        final Set<AutomatonProxy> set = getMinSet(possible);
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
        }
      }
      if (minlist != null) {
        p = minlist;
        automata = new TreeSet<AutomatonProxy>(p.getAutomata());
        stop = false;
        //System.out.println("numcomposed" + p.getComposed().size());
      }
      stop = automata.size() == 2 ? true : stop;
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
    System.out.println("finish project");
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
        System.out.println(aut.getName() +
                           " states:" + aut.getStates().size() +
                           " trans:" + aut.getTransitions().size());
      }
      for (final AutomatonProxy aut : mCompautomata) {
        AutomatonProxy aut1 = aut;
        mTempAut.clear();
        for (final AutomatonProxy aut2 : mAutomata) {
          if (containsAny(aut1.getEvents(), aut2.getEvents())) {
            List<AutomatonProxy> tocomp =
                Arrays.asList(new AutomatonProxy[] {aut1, aut2});
            final BlockedEvents be =
                new BlockedEvents(tocomp, getFactory(), getConfiguredDefaultMarking());
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
      throws AnalysisException
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
      //if (mHidden.contains(getMarkingProposition())) {
      mHidden.remove(getConfiguredDefaultMarking());
      mHidden.remove(mAlpha);
      System.out.println("hide: " + mHidden);
      //}
      //AutomataHidden ah =
      //  new AutomataHidden(compAutomata, new HashSet<EventProxy>(mHidden));
      //mChecked.add(ah);
      AutomatonProxy minAutomaton;
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
          final ProductDESProxy compmodel = getFactory().createProductDESProxy("temp",
                                                                         mOriginalAlphabet,
                                                                         mCompautomata);
          String compname = "";
          boolean first = true;
          //System.out.println("comp");
          for (final AutomatonProxy aut : mCompautomata) {
            if (!first) {compname += "||";}
            compname += aut.getName();
            first = false;
          }
          if (mCompautomata.size() > 1) {
            //GeneralisedCanonicalSynchronousProductBuilder composer =
            //  new GeneralisedCanonicalSynchronousProductBuilder(compmodel, getFactory(), mAlpha);
            final MonolithicSynchronousProductBuilder composer =
              new MonolithicSynchronousProductBuilder(compmodel, getFactory());
            final List<EventProxy> propositions = new ArrayList<EventProxy>();
            propositions.add(getConfiguredDefaultMarking());
            propositions.add(mAlpha);
            composer.setPropositions(propositions);
            //System.out.println(size);
            //composer.setTransitionLimit(maxsize * maxsize);
            composer.setNodeLimit(6500);
            composer.setTransitionLimit(10000000);
            System.out.println("attempt");
            mCompTime -= System.currentTimeMillis();
            composer.run();
            System.out.println("finish comp");
            minAutomaton = composer.getComputedAutomaton();
            //System.out.println(mCompautomata);
            //System.out.println(minAutomaton);
            mCompTime += System.currentTimeMillis();
            //String compname = minAutomaton.getName();
            final int compsize = minAutomaton.getStates().size();
            final int transitionsize = minAutomaton.getTransitions().size();
            mPeakstates = compsize >= mPeakstates ? compsize : mPeakstates;
            mTotalstates += compsize;
            mPeakTransitions = transitionsize >= mPeakTransitions ? transitionsize : mPeakTransitions;
            mTotalTransitions += transitionsize;
            System.out.println("compsize:" + minAutomaton.getStates().size());
          } else {
            minAutomaton = mCompautomata.iterator().next();
          }
          //mAllSame.update(compAutomata, mAutomata, minAutomaton, getFactory(), mHidden);
          //minAutomaton = mAllSame.getResult() != null ? mAllSame.getResult() :
                                                        //minAutomaton;
          /*if (mAllSame.getNotComposed() != null) {
            mAutomata.clear();
            mAutomata.addAll(mAllSame.getNotComposed());
          }*/
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
          if (true) {
            //System.out.println("hiding:" + mHidden.size());
            final EventProxy tauproxy =
              getFactory().createEventProxy("tau:" + minAutomaton.getName(),
                                            EventKind.UNCONTROLLABLE);
            final EventEncoding ee = new EventEncoding(minAutomaton, getKindTranslator(), tauproxy);
            final ListBufferTransitionRelation tr =
              new ListBufferTransitionRelation(minAutomaton, ee,
                                               ListBufferTransitionRelation.CONFIG_SUCCESSORS);
            /*for (EventProxy event : mOriginalAlphabet) {
              if (mAllSelfLoops.containsKey(event) && mAllSelfLoops.get(event).isEmpty()) {
                System.out.println("self looped");
                tr.removeAllTransitionsWithEvent(tr.eventToInt(event));
              }
            }*/
            final int tau = EventEncoding.TAU;
            //System.out.println(tr.createAutomaton(getFactory(), ee));
            for (final EventProxy event : mHidden) {
              if (getKindTranslator().getEventKind(event) == EventKind.PROPOSITION) {continue;}
              final int evcode = ee.getEventCode(event);
              if (evcode == -1) {System.out.println(event);continue;}
              if (evcode == EventEncoding.TAU) {continue;}
              tr.replaceEvent(evcode, tau);
              tr.removeEvent(evcode);
            }
            //System.out.println(tr.createAutomaton(getFactory(), ee));
            // System.out.println(minAutomaton);
            // System.out.println(mCompautomata);
            final int marking = ee.getEventCode(getConfiguredDefaultMarking());
            final int alpha = ee.getEventCode(mAlpha);
            final int cont = ee.getEventCode(mCont);
            tr.replaceEvent(cont, tau);
            //minAutomaton = applyAbstractionRules(tr.createAutomaton(getFactory(), ee), ee.getTauEvent());
            //System.out.println("heuristic: " + applyAbstractionRules(tr.createAutomaton(getFactory(), ee), ee.getTauEvent()).getStates().size());
            /*CertainConflictListBuffer cc = new CertainConflictListBuffer(tr,
                                                                         tau, marking); cc.run();
            if (cc.getDumpState() != -1) {
              tr.setMarked(cc.getDumpState(), alpha, true);
            }*/
            //System.out.println(tr.createAutomaton(getFactory(), ee));
            final Canonize canonizer = new Canonize(tr, ee, marking, alpha, cont);
            final ListBufferTransitionRelation canon = canonizer.run(getFactory());
            /*ObservationEquivalenceTRSimplifier oetrs = new ObservationEquivalenceTRSimplifier(tr);
            oetrs.run();
            ListBufferTransitionRelation canon = tr;*/
            /*cc = new CertainConflictListBuffer(canon, tau, marking); cc.run();
            if (cc.getDumpState() != -1) {
              canon.setMarked(cc.getDumpState(), alpha, true);
            }*/
            canon.setName(compname);
            //canon.replaceEvent(cont, tau);
            //minAutomaton = canon.createAutomaton(getFactory(), ee);
            //System.out.println("CANON: " + canon.getName());
            //System.out.println(minAutomaton);
            System.out.println("canminsize: " + canon.createAutomaton(getFactory(), ee).getStates().size());
            /*ee = new EventEncoding(minAutomaton, getKindTranslator());
            tr =
              new ListBufferTransitionRelation(minAutomaton, ee,
                                               ListBufferTransitionRelation.CONFIG_SUCCESSORS);
            tr.setName(compname);
            minAutomaton = tr.createAutomaton(getFactory(), ee);*/
            minAutomaton = canon.createAutomaton(getFactory(), ee);
            //System.out.println(minAutomaton);
            //minAutomaton = applyAbstractionRules(minAutomaton, ee.getTauEvent());
            //System.out.println(minAutomaton.getEvents());
            //System.out.println("minsize: " + minAutomaton.getStates().size());
            //System.out.println(minAutomaton);
            //mAggDiff += diff;
            //mSmallestDiff = mSmallestDiff > diff ? diff : mSmallestDiff;
            //mLargestDiff = mLargestDiff < diff ? diff : mLargestDiff;
            /*System.out.println("write");
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
            }*/
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
          }
          /*if (minAutomaton.getStates().size() > minAutomaton2.getStates().size()) {
            System.out.println("SWITCH"); System.out.println("SWITCH");
            switched++;
            minAutomaton = minAutomaton2;
          }*/
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
      //mRIT.addAutomata(Collections.singleton(minAutomaton));
      mAutomata.add(minAutomaton);
      mDontOnOwn.add(minAutomaton);
      mNew = minAutomaton;
      mTarget = new HashSet<EventProxy>();
      for (final AutomatonProxy a : mAutomata) {
        mTarget.addAll(a.getEvents());
      }
      System.out.println("end projection");
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

  @SuppressWarnings("unused")
  private AutomatonProxy applyAbstractionRules(AutomatonProxy autToAbstract,
                                               final EventProxy tau)
      throws AnalysisException
  {
    AutomatonProxy abstractedAut = autToAbstract;
    for (final AbstractionRule rule : mAbstractionRules) {
      try {
        abstractedAut = rule.applyRule(autToAbstract, tau);
 //       System.out.println(rule.getClass().toString());
//        System.out.println(abstractedAut);
        autToAbstract = abstractedAut;
      } catch (final OutOfMemoryError error) {
        System.gc();
        throw new OverflowException(error);
      } finally {
        rule.cleanup();
      }
    }
    return abstractedAut;
  }

  //#########################################################################
  //# Data Members
  private int minSize = 10000;
  private int mStates;
  private int mMaxProjStates;
  private final Map<AutomataHidden,AutomatonProxy> mMinAutMap =
      new HashMap<AutomataHidden,AutomatonProxy>();
  private final Set<AutomataHidden> mChecked =
    new THashSet<AutomataHidden>();
  private final Set<AutomatonProxy> mDontOnOwn =
    new THashSet<AutomatonProxy>();
  private TObjectIntHashMap<Set<AutomatonProxy>> mCommon = null;

  /*
  private final TObjectIntHashMap<Set<AutomatonProxy>> mNumOccurinng = null;
  private final int mLargestComposition = 0;
  private final int mLargestTransitions = 0;
  private final int mAggComposition = 0;
  private final int mAggTransitions = 0;
  private final int mAnnotatedBISIMulation = 0;
  private final int mBISIMulation = 0;
  private final int mAnnBITIME = 0;
  private final int mBITIME = 0;
  private final int mSmallestDiff = Integer.MAX_VALUE;
  private final int mLargestDiff = Integer.MIN_VALUE;
  private final int mAggDiff = 0;
  */
  @SuppressWarnings("unused")
  private int mCompTime = 0;

  private int maxsize = 1000;
  private int mTime = 0;
  private int mPeakstates = 0;
  private int mTotalstates = 0;
  private int mPeakTransitions = 0;
  private int mTotalTransitions = 0;
  private double mFinalStates = 0;
  private double mFinalTrans = 0;
  private TObjectIntHashMap<Set<AutomatonProxy>> mNumlocal;
  private final List<AbstractionRule> mAbstractionRules;

  private final EventProxy mAlpha;
  private final EventProxy mCont;

  // private BufferedWriter mWriter = null;


  // #########################################################################
  // # Class Constants
  @SuppressWarnings("unused")
  private static final Logger LOGGER =
      LoggerFactory.createLogger(AlphaNonBlockingChecker.class);

}
