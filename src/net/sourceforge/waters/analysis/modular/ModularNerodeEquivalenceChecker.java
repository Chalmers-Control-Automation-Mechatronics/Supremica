//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.monolithic.MonolithicNerodeEChecker;
import net.sourceforge.waters.analysis.abstraction.TraceFinder;
import net.sourceforge.waters.analysis.sd.NerodeEquVerificationResult;
import net.sourceforge.waters.analysis.sd.NerodeKindTranslator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The modular Nerode equivalent check algorithm.
 *
 * @author Mahvash Baloch
 */

public class ModularNerodeEquivalenceChecker
  extends AbstractModelVerifier

{

  //#########################################################################
  //# Constructors
  public ModularNerodeEquivalenceChecker(final ProductDESProxyFactory factory,
                                       final MonolithicNerodeEChecker checker)
  {
    this(null, factory, checker);
  }

  public ModularNerodeEquivalenceChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final MonolithicNerodeEChecker checker
                                       )
  {
    super(model,factory, NerodeKindTranslator.getInstance());
    //setKindTranslator(NerodeKindTranslator.getInstance());
    mChecker = checker;
    mTraceFinders = new HashMap<AutomatonProxy,TraceFinder>();
    mStates = 0;
    setNodeLimit(2000000);
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    setUp();
    mStates = 0;
    mChecker.setNodeLimit(getNodeLimit());
    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    //final Set<AutomatonProxy> specplants = new HashSet<AutomatonProxy>();
    final SortedSet<AutomatonProxy> specs =
      new TreeSet<AutomatonProxy>(new Comparator<AutomatonProxy>() {
      public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
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

    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> input = des.getAutomata();
    final int numAutomata = input.size();
    final List<AutomatonProxy> automata = new ArrayList<AutomatonProxy>(numAutomata);
    for (final AutomatonProxy aut : input) {
      switch (getKindTranslator().getComponentKind(aut)) {
      case PLANT:
        automata.add(aut);
        plants.add(aut);
        break;
      case SPEC:
        automata.add(aut);
        specs.add(aut);
        break;
      default:
        break;
      }
    }

    final Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
    final Set<EventProxy> events = new HashSet<EventProxy>();
    final SortedSet<AutomatonProxy> unProcessedAut = new TreeSet<AutomatonProxy>(new AutomatonComparator());
    final SortedSet<AutomatonProxy> ProcessedAut = new TreeSet<AutomatonProxy>(new AutomatonComparator());
    unProcessedAut.addAll(plants);
    unProcessedAut.addAll(specs);

    while (!unProcessedAut.isEmpty()) {
      final AutomatonProxy Current = unProcessedAut.first();
      composition.clear();
      composition.add(Current);
      events.clear();
      events.addAll(Current.getEvents());
      ProductDESProxy comp =
        getFactory().createProductDESProxy("comp", events, composition);
      mChecker.setModel(comp);
      mChecker.setKindTranslator(new KindTranslator()
      {
        public EventKind getEventKind(final EventProxy e)
        {
          return getKindTranslator().getEventKind(e);
        }

        public ComponentKind getComponentKind(final AutomatonProxy a)
        {
          return specs.contains(a) ? ComponentKind.SPEC
                                   : ComponentKind.PLANT;
        }
      });
      //final ModularNerodeHeuristic heuristic = getHeuristic();
      while (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        final NerodeEquVerificationResult result = mChecker.getAnalysisResult();
        final SafetyTraceProxy counterExample1 = mChecker.getCounterExample();
        final SafetyTraceProxy counterExample2 = result.getCounterExample2();
        unProcessedAut.remove(Current);
        if(ProcessedAut.contains(Current))
              ProcessedAut.remove(Current);

        final Collection<AutomatonProxy> newComp =
                     heur(comp,
                         unProcessedAut,
                         ProcessedAut,
                         counterExample1,
                         counterExample2
                         );
        if (newComp == null) {
          final ProductDESProxyFactory factory = getFactory();
          final SafetyTraceProxy trace = mChecker.getCounterExample();
          final SafetyTraceProxy extended =
            extendTrace(factory, trace, automata);
          return setFailedResult(extended);
        }
        for (final AutomatonProxy automaton : newComp) {
          composition.add(automaton);
          events.addAll(automaton.getEvents());
        }
        comp = getFactory().createProductDESProxy("comp", events, composition);
        mChecker.setModel(comp);
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      for (final AutomatonProxy automaton : composition) {
         {
          unProcessedAut.remove(automaton);
          ProcessedAut.add(automaton);
        }
      }
    }
    setSatisfiedResult();
    return true;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return mChecker.supportsNondeterminism();
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  protected boolean setFailedResult(final TraceProxy counterexample)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + "-nerode";
    final Collection<AutomatonProxy> automata = counterexample.getAutomata();
    final List<TraceStepProxy> steps = counterexample.getTraceSteps();
    final SafetyTraceProxy wrapper =
      factory.createSafetyTraceProxy(tracename, null, null,
                                     des, automata, steps);
    return super.setFailedResult(wrapper);
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Inner Class AutomatonComparator
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }
  public Collection<AutomatonProxy> heur(final ProductDESProxy composition,
                                         final Set<AutomatonProxy> unProcessedAut,
                                         final Set<AutomatonProxy> ProcessedAut,
                                         final SafetyTraceProxy counterExample1,
                                         final SafetyTraceProxy counterExample2)
  {
    AutomatonProxy automaton = checkAutomata( unProcessedAut,
                                             new MaxEventComparator(composition),
                                             counterExample1, counterExample2);
    if (automaton == null ) {

      automaton = checkAutomata(automaton, ProcessedAut,
                                new MaxEventComparator(composition),
                                counterExample1,counterExample2);
    }

    return automaton == null ? null : Collections.singleton(automaton);
  }

  private static class MaxEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;

    public MaxEventComparator(final ProductDESProxy composition)
    {
      mEvents = composition.getEvents();
    }

    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (final EventProxy e : a1.getEvents()) {
        if (mEvents.contains(e)) {
          count1++;
        }
      }
      for (final EventProxy e : a2.getEvents()) {
        if (mEvents.contains(e)) {
          count2++;
        }
      }
      return count1 - count2;
    }
  }
  public String getName()
  {
    final String fullname = getClass().getName();
    final int dotpos = fullname.lastIndexOf('.');
    final int start = dotpos + 1;
    if (fullname.endsWith(HEURISTIC_SUFFIX)) {
      final int end = fullname.length() - HEURISTIC_SUFFIX.length();
      return fullname.substring(start, end);
    } else {
      return fullname.substring(start);
    }
  }

  public SafetyTraceProxy extendTrace(final ProductDESProxyFactory factory,
                                      final SafetyTraceProxy trace,
                                      final List<AutomatonProxy> automata)
  {
    final Set<AutomatonProxy> oldAutomata =
      new THashSet<AutomatonProxy>(trace.getAutomata());
    boolean done = false;
    boolean det = true;
    for (final AutomatonProxy aut : automata) {
      if (!oldAutomata.contains(aut)) {
        done = false;
        final TraceFinder finder = getTraceFinder(aut);
        det &= finder.isDeterministic();
      }
    }
    if (done) {
      return trace;
    }
    final String name = trace.getName();
    final String comment = trace.getComment();
    final URI location = trace.getLocation();
    final ProductDESProxy des = trace.getProductDES();
    final List<TraceStepProxy> oldSteps = trace.getTraceSteps();
    if (det) {
      return factory.createSafetyTraceProxy(name, comment, location,
                                            des, automata, oldSteps);
    }
    final int numSteps = oldSteps.size();
    final List<TraceStepProxy> newSteps = new ArrayList<TraceStepProxy>(numSteps);
    int depth = 0;
    for (final TraceStepProxy oldStep : oldSteps) {
      final EventProxy event = oldStep.getEvent();
      final Map<AutomatonProxy,StateProxy> oldMap = oldStep.getStateMap();
      Map<AutomatonProxy,StateProxy> newMap = null;
      for (final AutomatonProxy aut : automata) {
        if (!oldAutomata.contains(aut)) {
          final TraceFinder finder = getTraceFinder(aut);
          final StateProxy state = finder.getState(depth);
          if (state != null) {
            if (newMap == null) {
              newMap = new HashMap<AutomatonProxy,StateProxy>(oldMap);
            }
            newMap.put(aut, state);
          }
        }
      }
      if (newMap == null) {
        newSteps.add(oldStep);
      } else {
        final TraceStepProxy newStep =
          factory.createTraceStepProxy(event, newMap);
        newSteps.add(newStep);
      }
      depth++;
    }
    return factory.createSafetyTraceProxy(name, comment, location,
                                          des, automata, newSteps);
  }


  //#########################################################################
  //# Auxiliary Methods
  AutomatonProxy checkAutomata(final Set<AutomatonProxy> automata,
                               final Comparator<AutomatonProxy> comp,
                               final SafetyTraceProxy counterExample1,
                               final SafetyTraceProxy counterExample2)
  {
    return checkAutomata
      (null, automata, comp, counterExample1, counterExample2);
  }

  AutomatonProxy checkAutomata(AutomatonProxy bestautomaton,
                               final Set<AutomatonProxy> automata,
                               final Comparator<AutomatonProxy> comp,
                               final SafetyTraceProxy counterExample1,
                               final SafetyTraceProxy counterExample2)
  { boolean mark = false;
    final List<TraceStepProxy> oldSteps = counterExample1.getTraceSteps();
    final int last = oldSteps.size();
    final TraceStepProxy oldStep = oldSteps.get(last-1);
    final EventProxy event = oldStep.getEvent();
      if(event.getKind().equals(EventKind.PROPOSITION))
      {
      mark = true;
      }

    for (final AutomatonProxy automaton : automata) {
      final int i = getNumberOfAcceptedEvents(automaton, counterExample1);
      final int j = getNumberOfAcceptedEvents(automaton, counterExample2);
      if(mark) {
        if (i == (counterExample1.getEvents().size()) &&
            j == (counterExample2.getEvents().size())) {
             if (bestautomaton == null ||
                comp.compare(bestautomaton, automaton) < 0) {
              bestautomaton = automaton;
              }
            }
        else
          bestautomaton = null;
      }
      else
      if (i != counterExample1.getEvents().size() &&
          j != counterExample2.getEvents().size()) {
           if (bestautomaton == null ||
              comp.compare(bestautomaton, automaton) < 0) {
            bestautomaton = automaton;
                     }
        }
      else
        bestautomaton = null;
    }
    return bestautomaton;
  }

  //#########################################################################
  //# Trace Checking
  boolean accepts(final AutomatonProxy aut, final TraceProxy trace)
  {
    return trace.getEvents().size() == getNumberOfAcceptedEvents(aut, trace);
  }

  int getNumberOfAcceptedEvents(final AutomatonProxy aut,
                                final TraceProxy trace)
  {
    final TraceFinder finder = getTraceFinder(aut);
    return finder.computeNumberOfAcceptedSteps(trace);
  }

  private TraceFinder getTraceFinder(final AutomatonProxy aut)
  {
    TraceFinder finder = mTraceFinders.get(aut);
    if (finder == null) {
      finder = new TraceFinder(aut, getKindTranslator());
      mTraceFinders.put(aut, finder);
    }
    return finder;
  }

   //#########################################################################
  //# Data Members
  private final MonolithicNerodeEChecker mChecker;
  private int mStates;
  private final Map<AutomatonProxy,TraceFinder> mTraceFinders;

  //#########################################################################
  //# Class Constants
  static final String HEURISTIC_SUFFIX = "Heuristic";

}









