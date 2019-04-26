//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.modular.BlockedEvents;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The canonicalising generalised conflict check algorithm.
 *
 * @author Simon Ware
 */

public class AlphaNonBlockingChecker
  extends AbstractConflictChecker
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

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  private SortedSet<AutomatonProxy> addAlpha(final Set<AutomatonProxy> model)
    throws AnalysisException
  {
    final SortedSet<AutomatonProxy> newmodel = new TreeSet<>();
    for (final AutomatonProxy aut : model) {
      final EventEncoding ee = new EventEncoding(aut, getKindTranslator());
      ee.addEvent(getUsedDefaultMarking(), getKindTranslator(),
                  EventStatus.STATUS_UNUSED);
      ee.addEvent(mAlpha, getKindTranslator(), EventStatus.STATUS_UNUSED);
      ee.addEvent(mCont, getKindTranslator(), EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation tr =
        new ListBufferTransitionRelation(aut, ee,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      tr.addRedundantPropositions();
      newmodel.add(tr.createAutomaton(getFactory(), ee));
    }
    return newmodel;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      new AllSame(getModel()); // TODO Is this used?
      mTime -= System.currentTimeMillis();
      boolean result = false;

      final ProjectionList list = project(getModel());
      mMinAutMap.clear();
      if (list == null) {
        return true;
      }
      final ConflictChecker checker =
        new NativeConflictChecker(list.getModel(),
                                  getUsedDefaultMarking(),
                                  getFactory());
      checker.setConfiguredPreconditionMarking(mAlpha);
      checker.setNodeLimit(getNodeLimit());
      result = checker.run();
      mFinalStates = checker.getAnalysisResult().getTotalNumberOfStates();
      mFinalTrans = checker.getAnalysisResult().getTotalNumberOfTransitions();

      if (!result) {
        final List<EventProxy> e = new ArrayList<>();
        final SafetyCounterExampleProxy counter =
          getFactory().createSafetyCounterExampleProxy(getModel().getName(), getModel(), e);
        setFailedResult(counter);
      } else {
        setSatisfiedResult();
      }
      mTime += System.currentTimeMillis();
      return result;
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    }
  }

  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    return null;
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

  @Override
  public void setUp()
    throws AnalysisException
  {
    super.setUp();
    final Set<EventProxy> mPropositions = new THashSet<>();
    final ProductDESProxyFactory factory = getFactory();
    mPropositions.add(mAlpha);
    mPropositions.add(getUsedDefaultMarking());
    final EventProxy alpha = mAlpha;
    final EventProxy omega = getUsedDefaultMarking();
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

  public void setuplocal(final ProductDESProxy model,
                         final Set<AutomatonProxy> automata)
    throws EventNotFoundException
  {
    final TObjectIntHashMap<Set<AutomatonProxy>> numlocal =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    for (final EventProxy e : model.getEvents()) {
      if (e == getUsedDefaultMarking()) {
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

  private List<Set<AutomatonProxy>> getMinTransitions
    (final ProductDESProxy model, final SortedSet<AutomatonProxy> automata)
    throws EventNotFoundException
  {
    final TObjectIntHashMap<Set<AutomatonProxy>> common =
      new TObjectIntHashMap<Set<AutomatonProxy>>();
    final List<Set<AutomatonProxy>> pairs = new ArrayList<Set<AutomatonProxy>>();
    AutomatonProxy minaut = null;
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
      if (e == getUsedDefaultMarking()) {
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
    mCommon = common;
    mNumlocal = numoccuring;
    return pairs;
  }

  @SuppressWarnings("unused")
  private Set<AutomatonProxy> getMinSync(final List<Set<AutomatonProxy>> auts)
    throws AnalysisException
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
      final MonolithicSynchronousProductBuilder composer =
        new MonolithicSynchronousProductBuilder(compmodel, getFactory());
      final List<EventProxy> propositions = new ArrayList<EventProxy>();
      propositions.add(getUsedDefaultMarking());
      propositions.add(mAlpha);
      composer.setPropositions(propositions);
      final int size = maxsize;
      //composer.setTransitionLimit(maxsize * maxsize);
      composer.setNodeLimit(6500);
      composer.setTransitionLimit(10000000);
      composer.run();
      final int compnum = composer.getComputedAutomaton().getStates().size();
      if (compnum < minautnum) {
        minauts = automata;
        minautnum = compnum;
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

  @SuppressWarnings("unused")
  private static class AutomataComparator implements
      Comparator<SortedSet<AutomatonProxy>>
  {
    @Override
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
      if (ComponentKind.PROPERTY != aut.getKind()) {
        automata.add(aut);
      }
    }
    automata = addAlpha(automata);
    ProjectionList p = null;
    final Collection<AutomatonProxy> tautomata = automata;
    for (final AutomatonProxy a : tautomata) {
      p = new ProjectionList(p, automata, Collections.singleton(a));
      automata = new TreeSet<>(p.getAutomata());
    }
    while (true) {
      final List<Set<AutomatonProxy>> possible = getMinTransitions(model, automata);
      boolean stop = true;
      ProjectionList minlist = null;
      minSize = Integer.MAX_VALUE / 4;
      while (!possible.isEmpty()) {
        final Set<AutomatonProxy> set = getMinSet(possible);
        try {
          final ProjectionList t = new ProjectionList(p, automata, set);
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
        automata = new TreeSet<>(p.getAutomata());
        stop = false;
      }
      stop = automata.size() == 2 ? true : stop;
      if (stop) {
        break;
      }
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
        if (contains.contains(e)) {
          return true;
        }
      }
      return false;
    }

    @SuppressWarnings("unused")
    private void blockedEvents()
      throws EventNotFoundException
    {
      final Set<AutomatonProxy> mTempComp = new TreeSet<AutomatonProxy>();
      final Set<AutomatonProxy> mTempAut = new TreeSet<AutomatonProxy>();
      for (final AutomatonProxy aut : mCompautomata) {
        AutomatonProxy aut1 = aut;
        mTempAut.clear();
        for (final AutomatonProxy aut2 : mAutomata) {
          if (containsAny(aut1.getEvents(), aut2.getEvents())) {
            List<AutomatonProxy> tocomp =
              Arrays.asList(new AutomatonProxy[] {aut1, aut2});
            final BlockedEvents be =
              new BlockedEvents(tocomp, getFactory(), getUsedDefaultMarking());
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
      mCompautomata.clear();
      mCompautomata.addAll(mTempComp);
    }

    public ProjectionList(final ProjectionList parent,
                          final Set<AutomatonProxy> automata,
                          final Set<AutomatonProxy> compAutomata)
    throws AnalysisException
    {
      mParent = null;// parent;
      mCompautomata = new TreeSet<AutomatonProxy>();
      mCompautomata.addAll(compAutomata);
      mAutomata = new TreeSet<>(automata);
      final Set<EventProxy> events = new TreeSet<>();
      for (final AutomatonProxy a : mCompautomata) {
        events.addAll(a.getEvents());
      }
      mAutomata.removeAll(compAutomata);
      mOriginalAlphabet = events;
      mHidden = new THashSet<>(events);
      for (final AutomatonProxy a : mAutomata) {
        if (!compAutomata.contains(a)) {
          mHidden.removeAll(a.getEvents());
        }
      }
      mHidden.remove(getUsedDefaultMarking());
      mHidden.remove(mAlpha);
      mHidden.remove(mCont);
      AutomatonProxy minAutomaton;
      try {
        final ProductDESProxy compmodel =
          getFactory().createProductDESProxy("temp",
                                             mOriginalAlphabet,
                                             mCompautomata);
        String compname = "";
        boolean first = true;
        for (final AutomatonProxy aut : mCompautomata) {
          if (!first) {compname += "||";}
          compname += aut.getName();
          first = false;
        }
        if (mCompautomata.size() > 1) {
          final MonolithicSynchronousProductBuilder composer =
            new MonolithicSynchronousProductBuilder(compmodel, getFactory());
          final List<EventProxy> propositions = new ArrayList<EventProxy>();
          propositions.add(getUsedDefaultMarking());
          propositions.add(mAlpha);
          composer.setPropositions(propositions);
          composer.setNodeLimit(6500);
          composer.setTransitionLimit(10000000);
          composer.run();
          minAutomaton = composer.getComputedAutomaton();
          final int compsize = minAutomaton.getStates().size();
          final int transitionsize = minAutomaton.getTransitions().size();
          mPeakstates = compsize >= mPeakstates ? compsize : mPeakstates;
          mTotalstates += compsize;
          mPeakTransitions = transitionsize >= mPeakTransitions ? transitionsize : mPeakTransitions;
          mTotalTransitions += transitionsize;
        } else {
          minAutomaton = mCompautomata.iterator().next();
        }
        if (true) {
          final EventProxy tauproxy =
            getFactory().createEventProxy("tau:" + minAutomaton.getName(),
                                          EventKind.UNCONTROLLABLE);
          final EventEncoding ee = new EventEncoding(minAutomaton, getKindTranslator(), tauproxy);
          final ListBufferTransitionRelation tr =
            new ListBufferTransitionRelation(minAutomaton, ee,
                                             ListBufferTransitionRelation.CONFIG_SUCCESSORS);
          final int tau = EventEncoding.TAU;
          for (final EventProxy event : mHidden) {
            if (getKindTranslator().getEventKind(event) == EventKind.PROPOSITION) {continue;}
            final int evcode = ee.getEventCode(event);
            if (evcode == -1) {continue;}
            if (evcode == EventEncoding.TAU) {continue;}
            tr.replaceEvent(evcode, tau);
            tr.removeEvent(evcode);
          }
          final int marking = ee.getEventCode(getUsedDefaultMarking());
          final int alpha = ee.getEventCode(mAlpha);
          final int cont = ee.getEventCode(mCont);
          tr.replaceEvent(cont, tau);
          final Canonize canonizer = new Canonize(tr, ee, marking, alpha, cont);
          final ListBufferTransitionRelation canon = canonizer.run(getFactory());
          canon.setName(compname);
          minAutomaton = canon.createAutomaton(getFactory(), ee);
        }
      } catch (final AnalysisException exception) {
        mStates += mMaxProjStates;
        throw exception;
      }
      mAutomata.add(minAutomaton);
      mDontOnOwn.add(minAutomaton);
      mNew = minAutomaton;
      mTarget = new THashSet<>();
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
        final Set<EventProxy> autevents = new THashSet<>(mOriginalAlphabet);
        autevents.removeAll(proxy.getEvents());
        int init = 0;
        final Set<StateProxy> states = proxy.getStates();
        for (final StateProxy s : states) {
          if (s.isInitial()) {
            init++;
            currstate.add(s);
          }
          events.get(i).put(s, new THashSet<>(autevents));
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
      Queue<Place> stateList = new PriorityQueue<>();
      Place place = new Place(currstate, null, 0, null);
      stateList.offer(place);
      final List<EventProxy> oldevents = trace.getEvents();

      final Set<Place> visited = new THashSet<>();
      visited.add(place);
      while (true) {
        place = stateList.poll();
        if (place.mIndex >= oldevents.size()) {
          break;
        }
        currstate = place.mCurrState;
        final Set<EventProxy> possevents = new THashSet<>(mHidden);
        hidden: for (final EventProxy pe : possevents) {
          final List<StateProxy> newstate = new ArrayList<>(currstate.size());
          for (i = 0; i < currstate.size(); i++) {
            if (aut[i].getEvents().contains(pe)) {
              final StateProxy t = automata.get(i).get(new Key(currstate.get(i), pe));
              if (t == null) {
                continue hidden;
              }
              newstate.add(t);
            } else {
              newstate.add(currstate.get(i));
            }
          }
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
      trace = getFactory().createTraceProxyDeterministic(place.getTrace());
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

    @SuppressWarnings("unused")
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
  private static class Tuple implements Comparable<Tuple>
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

  @SuppressWarnings("unused")
  private AutomatonProxy applyAbstractionRules(AutomatonProxy autToAbstract,
                                               final EventProxy tau)
      throws AnalysisException
  {
    AutomatonProxy abstractedAut = autToAbstract;
    for (final AbstractionRule rule : mAbstractionRules) {
      try {
        abstractedAut = rule.applyRule(autToAbstract, tau);
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
      new HashMap<>();
  private final Set<AutomataHidden> mChecked = new THashSet<>();
  private final Set<AutomatonProxy> mDontOnOwn = new THashSet<>();
  private TObjectIntHashMap<Set<AutomatonProxy>> mCommon = null;

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

}
