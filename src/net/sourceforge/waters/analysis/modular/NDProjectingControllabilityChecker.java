//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

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

import net.sourceforge.waters.analysis.annotation.BiSimulatorLanguage;
import net.sourceforge.waters.analysis.annotation.CertainDeath;
import net.sourceforge.waters.analysis.annotation.RemoveAllTau;
import net.sourceforge.waters.analysis.annotation.TauLoopRemoval;
import net.sourceforge.waters.analysis.annotation.TransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * The projecting controllability check algorithm.
 *
 * @author Simon Ware
 */

public class NDProjectingControllabilityChecker
  extends AbstractModularSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public NDProjectingControllabilityChecker(final ProductDESProxy model,
                                            final ProductDESProxyFactory factory,
                                            final ControllabilityChecker checker,
                                            final boolean least)
  {
    this(model, factory, checker, least, 500);
  }

  public NDProjectingControllabilityChecker(final ProductDESProxy model,
                                            final ProductDESProxyFactory factory,
                                            final ControllabilityChecker checker,
                                            final boolean least, final int maxsize)
  {
    super(model, factory, checker);
    setKindTranslator(ControllabilityKindTranslator.getInstance());
    setHeuristicMethod(ModularHeuristicFactory.Method.MaxCommonEvents);
    setHeuristicPreference
      (ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
    mChecker = checker;
    mStates = 0;
    setNodeLimit(10000000);
    maxprojection = maxsize;
    mMaxProjStates = maxprojection;
  }


  //#########################################################################
  //# Invocation
  /*public boolean run()
    throws AnalysisException
  {
    //LOGGER.debug("ProjectingControllabilityChecker: STARTING on " +
    //             getModel().getName());
    mStates = 0;
    mChecker.setNodeLimit(getNodeLimit());
    final Map<AutomatonProxy, AutomatonProxy> getorig = new HashMap<AutomatonProxy, AutomatonProxy>();
    final Map<AutomatonProxy, AutomatonProxy> getplant = new HashMap<AutomatonProxy, AutomatonProxy>();
    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specplants = new HashSet<AutomatonProxy>();
    final Set<EventProxy> forbiddenEvents = new HashSet<EventProxy>();
    final SortedSet<AutomatonProxy> specs =
      new TreeSet<AutomatonProxy>(new Comparator<AutomatonProxy>() {
      public int compare(AutomatonProxy a1, AutomatonProxy a2)
      {
        if (a1.getStates().size() < a2.getStates().size()) {
          return -1;
        } else if (a1.getStates().size() > a2.getStates().size()) {
          return 1;
        }
        if (a1.getTransitions().size() < a2.getTransitions().size()) {
          return -1;
        } else if (a1.getTransitions().size() > a2.getTransitions().size()) {
          return 1;
        }
        if (a1.getEvents().size() < a2.getEvents().size()) {
          return -1;
        } else if (a1.getEvents().size() > a2.getEvents().size()) {
          return 1;
        }
        return a1.getName().compareTo(a2.getName());
      }
    });
    for (AutomatonProxy automaton : getModel().getAutomata()) {
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);
                      break;
        default : break;
      }
    }
    //System.out.println(specs);
    while (!specs.isEmpty()) {
      mMinAutMap.clear();
      Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
      Set<EventProxy> events = new HashSet<EventProxy>();
      SortedSet<AutomatonProxy> uncomposedplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecs = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      final AutomatonProxy origspec = mLeast ? specs.first() : specs.last();
      Object[] array = convertSpec(origspec);
      final AutomatonProxy spec = (AutomatonProxy) array[1];
      AutomatonProxy plant = (AutomatonProxy) array[0];
      final Map<EventProxy,EventProxy> uncont = Casting.toMap((Map) array[2]);
      forbiddenEvents.clear();
      forbiddenEvents.addAll(spec.getEvents());
      for (AutomatonProxy a : plants) {
        uncomposedplants.add(convertPlant(a, uncont));
      }
      for (AutomatonProxy a : specplants) {
        uncomposedspecplants.add(convertPlant(a, uncont));
      }
      //uncomposedspecplants.addAll(specplants);
      uncomposedspecs.addAll(specs);
      mSpec = spec;
      composition.add(spec);
      composition.add(plant);
      //forbiddenEvents = aut[1].getEvents();
      for (AutomatonProxy a : composition) {
        events.addAll(a.getEvents());
      }
      uncomposedspecs.remove(origspec);
      ProductDESProxy comp = getFactory().createProductDESProxy("comp", events, composition);
      ProjectionList proj = null;
      mChecker.setModel(comp);
      KindTranslator translator = new KindTranslator()
      {
        public EventKind getEventKind(EventProxy e)
        {
          return forbiddenEvents.contains(e) ? EventKind.UNCONTROLLABLE
                                             : EventKind.CONTROLLABLE;
        }

        public ComponentKind getComponentKind(AutomatonProxy a)
        {
          return a == spec ? ComponentKind.SPEC : ComponentKind.PLANT;
        }
      };
      mChecker.setKindTranslator(translator);
      final ModularHeuristic heuristic = getHeuristic();
      while (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        final TraceProxy counter =
          proj == null ?
          mChecker.getCounterExample() :
          proj.getTrace(mChecker.getCounterExample(), comp);
        Collection<AutomatonProxy> newComp =
          heuristic.heur(comp,
                         uncomposedplants,
                         uncomposedspecplants,
                         uncomposedspecs,
                         counter,
                         translator);
        if (newComp == null) {
          return setFailedResult(counter, uncont);
        }
        for (AutomatonProxy automaton : newComp) {
          uncomposedplants.remove(automaton);
          uncomposedspecplants.remove(automaton);
          uncomposedspecs.remove(automaton);
          composition.add(automaton);
          events.addAll(automaton.getEvents());
        }
        comp = getFactory().createProductDESProxy("comp", events, composition);
        System.out.println("forb1:" + forbiddenEvents);
        proj = project(comp, forbiddenEvents);
        mChecker.setModel(proj == null ? comp : proj.getModel());
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      specs.remove(origspec);
      specplants.add(origspec);
    }
    setSatisfiedResult();
    return true;
  }*/


  @Override
  public boolean run()
    throws AnalysisException
  {
    setUp();
    Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specs = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> origspecs = new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>();
    final Set<EventProxy> forbiddenEvents = new HashSet<EventProxy>();
    final Set<EventProxy> events = new HashSet<EventProxy>(getModel().getEvents());
    for (final AutomatonProxy automaton : getModel().getAutomata()) {
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  origspecs.add(automaton);
                      break;
        default : break;
      }
    }
    int l = 0;
    for (final AutomatonProxy s : origspecs) {
      final Object[] array = convertSpec(s, l);
      l++;
      final AutomatonProxy spec = (AutomatonProxy) array[1];
      specs.add(spec);
      forbiddenEvents.addAll(spec.getEvents());
      final AutomatonProxy plant = (AutomatonProxy) array[0];
      automata.add(plant);
      @SuppressWarnings("unchecked")
      final Map<EventProxy,EventProxy> uncont =
        (Map<EventProxy,EventProxy>) array[2];
      final Set<AutomatonProxy> newplants = new HashSet<AutomatonProxy>(plants.size());
      for (final AutomatonProxy p : plants) {
        newplants.add(convertPlant(p, uncont));
      }
      plants = newplants;
    }
    /*for (AutomatonProxy aut : specs) {
      TransitionRelation tr = new TransitionRelation(aut, null, forbiddenEvents);
      automata.add(tr.getAutomaton(getFactory()));
    }
    for (AutomatonProxy aut : plants) {
      TransitionRelation tr = new TransitionRelation(aut, null, forbiddenEvents);
      automata.add(tr.getAutomaton(getFactory()));
    }*/
    //System.out.println(automata);
    //System.out.println(forbiddenEvents);
    automata.addAll(plants);
    automata.addAll(specs);
    final Set<AutomatonProxy> tempautomata = new THashSet<AutomatonProxy>();
    for (final AutomatonProxy a : automata) {
      events.addAll(a.getEvents());
      tempautomata.add(a);//addSelfLoops(a, forbiddenEvents));
    }
    automata = tempautomata;
    CertainDeath.clearStats();
    final ProductDESProxy model = getFactory().createProductDESProxy("model",
                                                               events,
                                                               automata);
    final ProjectionList list = project(model, forbiddenEvents);
    //System.out.println(CertainDeath.stats());
    /*ControllabilityChecker checker = new ModularControllabilityChecker(
                                           list.getModel(), getFactory(),
                                           mChecker, mHeuristic, mLeast);*/
    final ControllabilityChecker checker = mChecker;
    //System.out.println(list.getModel());
    checker.setModel(list.getModel());
    checker.setNodeLimit(getNodeLimit());
    checker.setKindTranslator(new KindTranslator()
    {
      @Override
      public EventKind getEventKind(final EventProxy e)
      {
        return getKindTranslator().getEventKind(e);
      }

      @Override
      public ComponentKind getComponentKind(final AutomatonProxy a)
      {
        return specs.contains(a) ? ComponentKind.SPEC : ComponentKind.PLANT;
      }
    });
    mMinAutMap.clear();
    if (checker.run()) {
      //mStates += checker.getAnalysisResult().getTotalNumberOfStates();
      mStates = (int)checker.getAnalysisResult().getTotalNumberOfStates();
      setSatisfiedResult();
      return true;
    } else {
      //mStates += checker.getAnalysisResult().getTotalNumberOfStates();
      mStates = (int)checker.getAnalysisResult().getTotalNumberOfStates();
      //System.out.println(checker.getAnalysisResult().getTotalNumberOfStates());
      final SafetyCounterExampleProxy counter = checker.getCounterExample();
      final TraceProxy trace = counter.getTrace();
      final List<EventProxy> e = trace.getEvents();
      final SafetyCounterExampleProxy newCounter =
        getFactory().createSafetyCounterExampleProxy(getModel().getName(),
                                                     getModel(),
                                                     e.subList(0, e.size() - 1));
      setFailedResult(newCounter);
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  public void setNodeLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Object[] convertSpec(final AutomatonProxy a, final int num)
  {
    final Map<EventProxy, EventProxy> uncont = new HashMap<EventProxy, EventProxy>();
    for (final EventProxy e : a.getEvents()) {
      if (getKindTranslator().getEventKind(e) == EventKind.UNCONTROLLABLE) {
        uncont.put(e, getFactory().createEventProxy(e.getName() + ":" + num,
                                                    EventKind.UNCONTROLLABLE));
      }
    }
    final Map<StateProxy, Set<EventProxy>> stateevents =
      new HashMap<StateProxy, Set<EventProxy>>();
    for (final StateProxy s : a.getStates()) {
      stateevents.put(s, new HashSet<EventProxy>());
    }
    for (final TransitionProxy t : a.getTransitions()) {
      if (getKindTranslator().getEventKind(t.getEvent()) ==
          EventKind.UNCONTROLLABLE) {
        stateevents.get(t.getSource()).add(t.getEvent());
      }
    }
    final Set<StateProxy> states = new HashSet<StateProxy>(a.getStates());
    final Set<TransitionProxy> transitions =
      new HashSet<TransitionProxy>(a.getTransitions());
    final Set<EventProxy> events = new HashSet<EventProxy>(a.getEvents());
    events.addAll(uncont.values());
    for (final StateProxy s : states) {
      for (final EventProxy e: uncont.keySet()) {
        if (!stateevents.get(s).contains(e)) {
          transitions.add
            (getFactory().createTransitionProxy(s, uncont.get(e), s));
        }
      }
    }
    final StateProxy onestate =
      getFactory().createStateProxy("one", true, new HashSet<EventProxy>());
    final AutomatonProxy plant =
      getFactory().createAutomatonProxy(a.getName() + ":plant",
                                        ComponentKind.PLANT,
                                        events, states, transitions);
    final AutomatonProxy spec =
      getFactory().createAutomatonProxy(a.getName() + ":spec",
                                        ComponentKind.SPEC,
                                        uncont.values(),
                                        Collections.singleton(onestate),
                                        new HashSet<TransitionProxy>());
    return new Object[] {plant, spec, uncont};
  }

  @SuppressWarnings("unused")
  private AutomatonProxy addSelfLoops(final AutomatonProxy a,
                                      final Set<EventProxy> selfloop)
  {
    final Set<EventProxy> self = new HashSet<EventProxy>(selfloop);
    self.removeAll(a.getEvents());
    if (self.isEmpty()) {
      return a;
    }
    final Collection<TransitionProxy> trans =
      new ArrayList<TransitionProxy>(a.getTransitions());
    final Collection<EventProxy> events = new ArrayList<EventProxy>(a.getEvents());
    events.addAll(self);
    for (final StateProxy s : a.getStates()) {
      for (final EventProxy e : self) {
        trans.add(getFactory().createTransitionProxy(s, e, s));
      }
    }
    return getFactory().createAutomatonProxy
      (a.getName(), ComponentKind.PLANT,
       events, a.getStates(), trans);
  }

  private AutomatonProxy convertPlant(final AutomatonProxy a,
                                      final Map<EventProxy, EventProxy> uncont)
  {
    final Set<EventProxy> same = new HashSet<EventProxy>(a.getEvents());
    same.retainAll(uncont.keySet());
    if (same.isEmpty()) {
      return a;
    }
    final Collection<TransitionProxy> trans =
      new ArrayList<TransitionProxy>(a.getTransitions());
    final Collection<EventProxy> events = new ArrayList<EventProxy>(a.getEvents());
    for(final EventProxy e : same) {
      events.add(uncont.get(e));
    }
    for (final TransitionProxy t : a.getTransitions()) {
      if (uncont.containsKey(t.getEvent())) {
        trans.add(getFactory().createTransitionProxy(t.getSource(),
                                                     uncont.get(t.getEvent()),
                                                     t.getSource()));
      }
    }
    return getFactory().createAutomatonProxy
      (a.getName(), ComponentKind.PLANT,
       events, a.getStates(), trans);
  }

  private ProjectionList project(final ProductDESProxy model,
                                 final Set<EventProxy> forbiddenEvents)
    throws AnalysisException
  {
    mChecked.clear();
    Set<AutomatonProxy> automata =
      new HashSet<AutomatonProxy>(model.getAutomata());
    ProjectionList p = null;
    while (true) {
      final SortedMap<SortedSet<AutomatonProxy>, Integer> numoccuring =
        new TreeMap<SortedSet<AutomatonProxy>,Integer>
        (new AutomataComparator());
      for (final EventProxy e : model.getEvents()) {
        if (!forbiddenEvents.contains(e)) {
          final SortedSet<AutomatonProxy> possess = new TreeSet<AutomatonProxy>();
          for (final AutomatonProxy a : automata) {
            if (a.getEvents().contains(e)) {
              possess.add(a);
            }
          }
          if (!possess.isEmpty()) {
            if (numoccuring.get(possess) == null) {
              numoccuring.put(possess, 0);
            }
            numoccuring.put(possess, numoccuring.get(possess) + 1);
          }
        }
      }
      //System.out.println("numautomata:" + automata.size());
      //System.out.println(numoccuring.size());
      final Set<Tuple> possible = new TreeSet<Tuple>();
      boolean stop = true;
      for (final SortedSet<AutomatonProxy> s : numoccuring.keySet()) {
        /*if (s.size() > 4 && s.size() != automata.size()) {
          continue;
        }*/
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
        size *= uncom;
        possible.add(new Tuple(s, size));
      }
      int overflows = 0;
      ProjectionList minlist = null;
      minSize = Integer.MAX_VALUE / 4;
      for (final Tuple tup : possible) {
        try {
          //System.out.println("forb2:" + forbiddenEvents);
          final ProjectionList t =
            new ProjectionList(p, automata, tup.mSet, forbiddenEvents);
          if (minSize >= t.getNew().getStates().size()) {
          minlist = t;
          minSize = t.getNew().getStates().size();
          break;
          }
        } catch (final AnalysisException exception) {
          overflows++;
         //System.out.println(overflows);
          if (overflows >= 2) {
          //  break;
          }
        }
      }
      if (minlist != null) {
        p = minlist;
        automata = new HashSet<AutomatonProxy>(p.getModel().getAutomata());
        stop = false;
      }
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
    final Set<AutomatonProxy> mAutomata;
    final ProjectionList mParent;
    final Set<AutomatonProxy> mCompautomata;
    final Set<EventProxy> mOriginalAlphabet;
    final Set<EventProxy> mHidden;
    final ProductDESProxy mModel;
    final AutomatonProxy mNew;

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
          if (aut2.getKind().equals(ComponentKind.PLANT) && containsAny(aut1.getEvents(), aut2.getEvents())) {
            List<AutomatonProxy> tocomp =
              Arrays.asList(new AutomatonProxy[]{aut1, aut2});
            final BlockedEvents be = new BlockedEvents(tocomp, getFactory(), null);
            be.setNodeLimit(10000);
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
                          final Set<AutomatonProxy> compAutomata,
                          final Set<EventProxy> forbiddenEvents)
      throws AnalysisException
    {
      final Set<EventProxy> localforbiddenEvents =
        new HashSet<EventProxy>(forbiddenEvents);
      mParent = parent;
      mCompautomata = compAutomata;
      mAutomata = new HashSet<AutomatonProxy>(automata);
      mAutomata.removeAll(compAutomata);
      final Set<EventProxy> events = new HashSet<EventProxy>();
      for (final AutomatonProxy a : compAutomata) {
        events.addAll(a.getEvents());
      }
      mOriginalAlphabet = events;
      mHidden = new HashSet<EventProxy>(events);
      for (final AutomatonProxy a : mAutomata) {
        if (a != mSpec) {
          mHidden.removeAll(a.getEvents());
        }
      }
      localforbiddenEvents.retainAll(mHidden);
      final AutomataHidden ah =
        new AutomataHidden(compAutomata, new HashSet<EventProxy>(mHidden));
      mHidden.removeAll(forbiddenEvents);
      mChecked.add(ah);
      AutomatonProxy minAutomaton;
      if (mMinAutMap.containsKey(ah)) {
        minAutomaton = mMinAutMap.get(ah);
        if (minAutomaton == null) {
          throw new OverflowException();
        }
      } else {
        try {
          //blockedEvents();
          //System.out.println(mCompautomata);
          final NonDeterministicComposer composer =
            new NonDeterministicComposer(
              new ArrayList<AutomatonProxy>(mCompautomata), getFactory(), null);
          composer.setNodeLimit(maxprojection);
          minAutomaton = composer.run();
          //System.out.println(minAutomaton);
          final TransitionRelation tr = new TransitionRelation(minAutomaton, null);
          final int tau = tr.mergeEvents(mHidden, getFactory());
          //System.out.println("alph:" + mOriginalAlphabet);
          //System.out.println("forb:" + forbiddenEvents);
          final TauLoopRemoval tlr = new TauLoopRemoval(tr, tau); tlr.run();
          final RemoveAllTau rat = new RemoveAllTau(tr, tau); rat.run();
          if (mOriginalAlphabet.containsAll(forbiddenEvents)) {
            System.out.println("con");
            tr.setMarkingToStatesWithOutgoing(forbiddenEvents);
            final CertainDeath con = new CertainDeath(tr); con.run();
          }
          tr.removeAllUnreachable();
          final BiSimulatorLanguage tbs = new BiSimulatorLanguage(tr); tbs.run();
          final RevBiSimulator rbs = new RevBiSimulator(tr); rbs.run();
          minAutomaton = tr.getAutomaton(getFactory());
          //System.out.println(minAutomaton);
          //mBITIME -= System.currentTimeMillis();
          /*BiSimulator sim = new BiSimulator(minAutomaton, null, getFactory());
          //mBISIMulation += minAutomaton.getStates().size();
          minAutomaton = sim.run();*/
          //mBITIME += System.currentTimeMillis();
          /*if (minAutomaton.getStates().size() > maxprojection) {
            throw new AnalysisException("to big");
          }*/
          //mBISIMulation -= minAutomaton.getStates().size();
          mMinAutMap.put(ah, minAutomaton);
        } catch (final AnalysisException exception) {
          mStates += mMaxProjStates;
          mMinAutMap.put(ah, null);
          throw exception;
        }
      }
      mAutomata.add(minAutomaton);
      mNew = minAutomaton;
      final Set<EventProxy> targ = new HashSet<EventProxy>();
      for (final AutomatonProxy a : mAutomata) {
        targ.addAll(a.getEvents());
      }
      mModel = getFactory().createProductDESProxy("model", targ, mAutomata);
    }

    public ProductDESProxy getModel()
    {
      return mModel;
    }

    public AutomatonProxy getNew()
    {
      return mNew;
    }

    @SuppressWarnings("unused")
	public TraceProxy getTrace(TraceProxy trace, final ProductDESProxy model)
    {
      final List<Map<StateProxy, Set<EventProxy>>> events =
        new ArrayList<Map<StateProxy, Set<EventProxy>>>(mCompautomata.size());
      final List<Map<Key, Set<StateProxy>>> automata =
        new ArrayList<Map<Key, Set<StateProxy>>>(mCompautomata.size());
      List<StateProxy> currstate = new ArrayList<StateProxy>(mCompautomata.size());
      final AutomatonProxy[] aut = new AutomatonProxy[mCompautomata.size()];
      int i = 0;
      for (final AutomatonProxy proxy : mCompautomata) {
        events.add(new HashMap<StateProxy, Set<EventProxy>>(proxy.getStates().size()));
        automata.add(new HashMap<Key, Set<StateProxy>>(proxy.getTransitions().size()));
        final Set<EventProxy> autevents = new HashSet<EventProxy>(mOriginalAlphabet);
        //System.out.println(autevents);
        autevents.removeAll(proxy.getEvents());
        //System.out.println(autevents);
        int init = 0;
        for (final StateProxy s : proxy.getStates()) {
          if (s.isInitial()) {
            init++;
            currstate.add(s);
          }
          events.get(i).put(s, new HashSet<EventProxy>(autevents));
        }
        assert(init == 1);
        for (final TransitionProxy t : proxy.getTransitions()) {
          events.get(i).get(t.getSource()).add(t.getEvent());
          final Key key = new Key(t.getSource(), t.getEvent());
          Set<StateProxy> targets = automata.get(i).get(key);
          if (targets == null) {
            targets = new THashSet<StateProxy>();
            automata.get(i).put(key, targets);
          }
          targets.add(t.getTarget());
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
          final List<Set<StateProxy>> newstate = new ArrayList<Set<StateProxy>>(currstate.size());
          for (i = 0; i < currstate.size(); i++) {
            if (aut[i].getEvents().contains(pe)) {
              final Set<StateProxy> t = automata.get(i).get(new Key(currstate.get(i), pe));
              //System.out.println(t);
              if (t == null) {
                continue hidden;
              }
              newstate.add(t);
            } else {
              newstate.add(Collections.singleton(currstate.get(i)));
            }
          }
          //System.out.println(newstate);
          addNewPlace(newstate, pe, place.mIndex, place, visited, stateList);
          /*Place newPlace = new Place(newstate, pe, place.mIndex, place);
          if (visited.add(newPlace)) {
            stateList.offer(newPlace);
          }*/
        }
        final EventProxy currevent = oldevents.get(place.mIndex);
        final List<Set<StateProxy>> newstate = new ArrayList<Set<StateProxy>>(currstate.size());
        boolean contains = true;
        for (i = 0; i < currstate.size(); i++) {
          if (aut[i].getEvents().contains(currevent)) {
            final Set<StateProxy> t = automata.get(i).get(new Key(currstate.get(i), currevent));
            if (t == null) {
              contains = false;
            }
            newstate.add(t);
          } else {
            newstate.add(Collections.singleton(currstate.get(i)));
          }
        }
        if (contains) {
          addNewPlace(newstate, currevent, place.mIndex + 1, place, visited, stateList);
        }
        /*Place newPlace = new Place(newstate, currevent, place.mIndex + 1, place);
        if (contains && visited.add(newPlace)) {
          stateList.offer(newPlace);
        }*/
        assert(!stateList.isEmpty());
      }
      stateList = null;
      final ProductDESProxy mod = mParent == null ? model : mParent.getModel();
      trace = getFactory().createTraceProxyDeterministic(place.getTrace());
      return mParent == null ? trace : mParent.getTrace(trace, model);
    }

    private void addNewPlace(final List<Set<StateProxy>> newstate, final EventProxy pe,
                             final int index, final Place place,  final Set<Place> visited,
                             final Queue<Place> stateList)
    {
      final List<StateProxy> states = new ArrayList<StateProxy>(newstate.size());
      for (int i = 0; i < newstate.size(); i++) {
        states.add(null);
      }
      addNewPlace(newstate, pe, index, place, visited, stateList, states, 0);
    }

    private void addNewPlace(final List<Set<StateProxy>> newstate, final EventProxy pe,
                             final int index, final Place place,  final Set<Place> visited,
                             final Queue<Place> stateList,
                             final List<StateProxy> workingstate, final int depth)
    {
      if (depth == newstate.size()) {
        final Place newPlace = new Place(workingstate, pe, index, place);
        if (visited.add(newPlace)) {
          stateList.offer(newPlace);
        }
        return;
      }
      for (final StateProxy s : newstate.get(depth)) {
        workingstate.set(depth, s);
        addNewPlace(newstate, pe, index, place, visited, stateList, workingstate, depth + 1);
      }
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
  private int minSize = 1000;
  private int maxprojection = 1000;
  private final ControllabilityChecker mChecker;
  private final AutomatonProxy mSpec = null;
  private int mStates;
  private final int mMaxProjStates;
  private final Map<AutomataHidden, AutomatonProxy> mMinAutMap =
    new HashMap<AutomataHidden, AutomatonProxy>();
  private final Set<AutomataHidden> mChecked = new HashSet<AutomataHidden>();

}
