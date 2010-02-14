//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ParallelModularControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ParallelModularControllabilityChecker
  extends AbstractModelVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructor
  public ParallelModularControllabilityChecker(final ProductDESProxy model,
                                               final ProductDESProxyFactory factory,
                                               final ControllabilityChecker checker,
                                               final ModularHeuristic heuristic)
  {
    super(model, factory, ControllabilityKindTranslator.getInstance());
    mChecker = checker;
    mHeuristic = heuristic;
    mStates = 0;
    setNodeLimit(2000000);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    mStates = 0;
    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specplants = new HashSet<AutomatonProxy>();
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
    for (final AutomatonProxy automaton : getModel().getAutomata()) {
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);
                      break;
        default : break;
      }
    }
    if (specs.isEmpty()) {
      setSatisfiedResult();
      return true;
    }
    while (!specs.isEmpty()) {
      //System.out.println("start run");
      final Set<ParallelRun> lookedat = new HashSet<ParallelRun>();
      final Queue<ParallelRun> runs = new PriorityQueue<ParallelRun>(specs.size(),
                                                                     new Comparator<ParallelRun>()
      {
        public int compare(final ParallelRun p1, final ParallelRun p2)
        {
          if (p1.mStates < p2.mStates) {
            return -1;
          } else if (p1.mStates > p2.mStates) {
            return 1;
          }
          return 0;
        }
      });
      for (final AutomatonProxy spec : specs) {
        runs.add(new ParallelRun(Collections.singleton(spec),
                                 new HashSet<AutomatonProxy>(),
                                 spec.getStates().size(),
                                 Collections.singleton(spec), null,
                                 spec.getEvents()));
      }
      while (!runs.isEmpty()) {
        ParallelRun run = runs.poll();
        mChecker.setModel(run.mModel);
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
        mChecker.setNodeLimit(getNodeLimit() - mStates);
        if (!mChecker.run()) {
          final Set<AutomatonProxy> cplants =
            new HashSet<AutomatonProxy>(run.mPlants);
          final Set<AutomatonProxy> cspecs =
            new HashSet<AutomatonProxy>(run.mSpecs);
          final Set<EventProxy> events =
            new HashSet<EventProxy>(run.mModel.getEvents());
          final SortedSet<AutomatonProxy> uncomposedplants =
            new TreeSet<AutomatonProxy>();
          final SortedSet<AutomatonProxy> uncomposedspecplants =
            new TreeSet<AutomatonProxy>();
          final SortedSet<AutomatonProxy> uncomposedspecs =
            new TreeSet<AutomatonProxy>();
          uncomposedplants.addAll(plants);
          uncomposedplants.removeAll(cplants);
          uncomposedspecplants.addAll(specplants);
          uncomposedspecplants.removeAll(cplants);
          uncomposedspecs.addAll(specs);
          uncomposedplants.removeAll(cspecs);
          final Collection<AutomatonProxy> newComp =
            mHeuristic.heur(run.mModel,
                            uncomposedplants,
                            uncomposedspecplants,
                            uncomposedspecs,
                            mChecker.getCounterExample(),
                            getKindTranslator());
          if (newComp == null) {
            mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
            setFailedResult(mChecker.getCounterExample());
            return false;
          }
          final int eventsBefore = events.size();
          double newStates = mChecker.getAnalysisResult().getTotalNumberOfStates();
          for (final AutomatonProxy automaton : newComp) {
            if (specs.contains(automaton)) {
              cspecs.add(automaton);
            } else {
              cplants.add(automaton);
            }
            events.addAll(automaton.getEvents());
            newStates *= automaton.getStates().size();
            System.out.println(automaton.getName());
          }
          final double numevents = events.size();
          final double newEvents = events.size() - eventsBefore;
          newStates -= mChecker.getAnalysisResult().getTotalNumberOfStates();
          newStates *= (newEvents / numevents);
          newStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
          run.mCounter = mChecker.getCounterExample();
          run = new ParallelRun(cspecs, cplants, newStates,
                                newComp, run, events);
          if (lookedat.add(run)) {
            runs.offer(run);
          }
        } else {
          final Collection<AutomatonProxy> changed = new ArrayList<AutomatonProxy>();
          for (final AutomatonProxy automaton : run.mModel.getAutomata()) {
            if (specs.contains(automaton)) {
              System.out.println(mChecker.getAnalysisResult().getTotalNumberOfStates() + " " + automaton.getName() + " size " + automaton.getStates().size());
              specs.remove(automaton);
              specplants.add(automaton);
              changed.add(automaton);
            }
          }
          /*for (ParallelRun i = run; i != null; i = i.mParent) {
            for (AutomatonProxy automaton : i.mAdded) {
              //if (specplants.contains(automaton) || specs.contains(automaton)) {
                thing.insert(0, automaton.getName());
                thing.insert(0, ',');
              //}
            }
          }
          System.out.println(thing);*/
          final Iterator<ParallelRun> it = runs.iterator();
          final Collection<ParallelRun> tobeadded = new ArrayList<ParallelRun>();
          while (it.hasNext()) {
            ParallelRun check = it.next();
            final Collection<AutomatonProxy> changed2 = new ArrayList<AutomatonProxy>(changed);
            changed2.retainAll(check.mSpecs);
            if (!changed2.isEmpty()) {
              it.remove();
              final ParallelRun neo = check(check, changed2);
              if (neo != null) {
                changed2.retainAll(neo.mSpecs);
                check = neo;
              }
              final Set<AutomatonProxy> cspecs = new HashSet<AutomatonProxy>(check.mSpecs);
              final Set<AutomatonProxy> cplants = new HashSet<AutomatonProxy>(check.mPlants);
              cspecs.removeAll(changed2);
              cplants.addAll(changed2);
              tobeadded.add(new ParallelRun(cspecs, cplants, check.mStates,
                                            check.mAdded, check.mParent,
                                            check.mModel.getEvents()));
            }
          }
          runs.addAll(tobeadded);
          lookedat.addAll(tobeadded);
        }
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        mChecker.setNodeLimit(getNodeLimit() - mStates);
      }
    }
    setSatisfiedResult();
    return true;
  }

  private ParallelRun check(final ParallelRun run, final Collection<AutomatonProxy> changed)
  {
    final Collection<AutomatonProxy> changed2 = new ArrayList<AutomatonProxy>(changed);
    changed2.removeAll(run.mAdded);
    if (!changed2.isEmpty()) {
      if (run.mParent == null) {
        final Set<AutomatonProxy> empty = Collections.emptySet();
        final Set<EventProxy> emptyE = Collections.emptySet();
        return new ParallelRun(empty, empty, 0, empty, null, emptyE);
      }
      final ParallelRun ret = check(run.mParent, changed2);
      if (ret != null) {
        return ret;
      }
    }
    if (run.mCounter == null) {
      return run;
    }
    for (final AutomatonProxy automaton : changed) {
      if (!AbstractModularHeuristic.acc(automaton, run.mCounter)) {
        return run;
      }
    }
    return null;
  }

  protected void addStatistics(final VerificationResult result)
  {
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Inner Class ParallelRun
  private final class ParallelRun
  {
    private ParallelRun(final Set<AutomatonProxy> specs,
                       final Set<AutomatonProxy> plants,
                       final double states,
                       final Collection<AutomatonProxy> added,
                       final ParallelRun parent,
                       final Set<EventProxy> events)
    {
      mSpecs = specs;
      mPlants = plants;
      mStates = states;
      mAdded = added;
      mParent = parent;
      final Collection<AutomatonProxy> model =
        new ArrayList<AutomatonProxy>(specs.size() + plants.size());
      model.addAll(specs);
      model.addAll(plants);
      mModel = getFactory().createProductDESProxy("comp", events, model);
    }

    public int hashCode()
    {
      return 17 + mSpecs.hashCode() * 31 + mPlants.hashCode() * 31;
    }

    public boolean equals(final Object o)
    {
      if (o instanceof ParallelRun) {
        final ParallelRun run = (ParallelRun) o;
        return mSpecs.equals(run.mSpecs) && mPlants.equals(run.mPlants);
      }
      return false;
    }

    private final ProductDESProxy mModel;
    private final Set<AutomatonProxy> mSpecs;
    private final Set<AutomatonProxy> mPlants;
    private final double mStates;
    private final Collection<AutomatonProxy> mAdded;
    private final ParallelRun mParent;
    private TraceProxy mCounter;
  }


  //#########################################################################
  //# Data Members
  private final ControllabilityChecker mChecker;
  private final ModularHeuristic mHeuristic;
  private int mStates;

}
