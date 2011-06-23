//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The modular controllability check algorithm.
 *
 * @author Simon Ware
 */

public class ModularControllabilityChecker
  extends AbstractModularSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public ModularControllabilityChecker(final ProductDESProxyFactory factory,
                                       final SafetyVerifier checker)
  {
    this(null, factory, checker, false);
  }

  public ModularControllabilityChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final SafetyVerifier checker,
                                       final boolean least)
  {
    super(model, factory);
    setKindTranslator(ControllabilityKindTranslator.getInstance());
    mChecker = checker;
    mStates = 0;
    mLeast = least;
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

    while (!specs.isEmpty()) {
      final Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
      final Set<EventProxy> events = new HashSet<EventProxy>();
      final SortedSet<AutomatonProxy> uncomposedplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      final SortedSet<AutomatonProxy> uncomposedspecplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      final SortedSet<AutomatonProxy> uncomposedspecs = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      uncomposedplants.addAll(plants);
      uncomposedspecplants.addAll(specplants);
      uncomposedspecs.addAll(specs);
      final AutomatonProxy spec = mLeast ? specs.first() : specs.last();
      composition.add(spec);
      events.addAll(spec.getEvents());
      uncomposedspecs.remove(spec);
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
      final ModularHeuristic heuristic = getHeuristic();
      while (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        final Collection<AutomatonProxy> newComp =
          heuristic.heur(comp,
                         uncomposedplants,
                         uncomposedspecplants,
                         uncomposedspecs,
                         mChecker.getCounterExample());
        if (newComp == null) {
          final ProductDESProxyFactory factory = getFactory();
          final SafetyTraceProxy trace = mChecker.getCounterExample();
          final SafetyTraceProxy extended =
            heuristic.extendTrace(factory, trace, automata);
          return setFailedResult(extended);
        }
        for (final AutomatonProxy automaton : newComp) {
          composition.add(automaton);
          uncomposedplants.remove(automaton);
          uncomposedspecplants.remove(automaton);
          uncomposedspecs.remove(automaton);
          events.addAll(automaton.getEvents());
        }
        comp = getFactory().createProductDESProxy("comp", events, composition);
        mChecker.setModel(comp);
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      for (final AutomatonProxy automaton : composition) {
        if (specs.contains(automaton)) {
          specs.remove(automaton);
          specplants.add(automaton);
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
    final String tracename = desname + "-uncontrollable";
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


  //#########################################################################
  //# Data Members
  private final SafetyVerifier mChecker;
  private int mStates;
  private final boolean mLeast;

}
