//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The culling controllability check algorithm.
 *
 * @author Simon Ware
 */

public class CullingControllabilityChecker
  extends AbstractModularSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructor
  public CullingControllabilityChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final ControllabilityChecker checker,
                                       final ModularHeuristic heuristic,
                                       final boolean least)
  {
    super(model, factory);
    setKindTranslator(ControllabilityKindTranslator.getInstance());
    mChecker = checker;
    mStates = 0;
    mLeast = least;
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
    while (!specs.isEmpty()) {
      final List<Op> oportunitys = new LinkedList<Op>();
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
      ProductDESProxy comp = getFactory().createProductDESProxy("comp", events, composition);
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
      mChecker.setNodeLimit(getNodeLimit());
      mainloop:
      while (!mChecker.run()) {
        TraceProxy newcounter = mChecker.getCounterExample();
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        mChecker.setNodeLimit(getNodeLimit());
        final ModularHeuristic heuristic = getHeuristic();
        while (newcounter != null) {
          final TraceProxy counter = newcounter;
          newcounter = null;
          final Collection<AutomatonProxy> newComp =
            heuristic.heur(comp,
                           uncomposedplants,
                           uncomposedspecplants,
                           uncomposedspecs,
                           counter);
          if (newComp == null) {
            setFailedResult(mChecker.getCounterExample());
            return false;
          }
          final KindTranslator translator = getKindTranslator();
          final ModularHeuristic all =
            new AllHeuristic(translator,
                             ModularHeuristicFactory.Preference.NOPREF);
          final Op op = new Op(all.heur(comp,
                                        uncomposedplants,
                                        uncomposedspecplants,
                                        uncomposedspecs,
                                        counter),
                               newComp.iterator().next());
          final Set<AutomatonProxy> possible = new HashSet<AutomatonProxy>(op.others);
          possible.add(op.added);
          //CheckSuffix.checkSuffix(counter, possible, null);
          double prevstates = mChecker.getAnalysisResult().getTotalNumberOfStates();
          System.out.println(op.added.getName() + " added " + mStates + " " + composition.size() + " " + prevstates);
          uncomposedplants.remove(op.added);
          uncomposedspecplants.remove(op.added);
          uncomposedspecs.remove(op.added);
          events.addAll(op.added.getEvents());
          final Iterator<Op> it = oportunitys.iterator();
          while (it.hasNext()) {
            final Op oportunity = it.next();
            if (oportunity.others.contains(op.added)) {
              composition.remove(oportunity.added);
              comp = getFactory().createProductDESProxy("comp", events, composition);
              mChecker.setModel(comp);
              //mChecker.setNodeLimit(getNodeLimit() - mStates);
              if (mChecker.run()) {
                break mainloop;
              };
              mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
              if (mChecker.getAnalysisResult().getTotalNumberOfStates() < prevstates) {
                //newcounter = mChecker.getCounterExample();
                op.others.retainAll(oportunity.others);
                System.out.println(oportunity.added.getName() + " removed");
                it.remove();
                if (specs.contains(oportunity.added)) {
                  uncomposedspecs.add(oportunity.added);
                } else if (specplants.contains(oportunity.added)) {
                  uncomposedspecplants.add(oportunity.added);
                } else if (plants.contains(oportunity.added)) {
                  uncomposedplants.add(oportunity.added);
                } else {
                  System.out.println("error");
                }
                prevstates = mChecker.getAnalysisResult().getTotalNumberOfStates();
              } else {
                composition.add(oportunity.added);
              }
            }
          }
          //if (mPrevStates < mChecker.getAnalysisResult().getTotalNumberOfStates()) {
            oportunitys.add(op);
          //}
          //mPrevStates = mChecker.getAnalysisResult().getTotalNumberOfStates();
          composition.add(op.added);
        }
        comp = getFactory().createProductDESProxy("comp", events, composition);
        mChecker.setModel(comp);
        mChecker.setNodeLimit(getNodeLimit());
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      /*specs.removeAll(composition);
      plants.addAll(composition);*/
      for (final AutomatonProxy automaton : composition) {
        if (specs.contains(automaton)) {
          //System.out.println(mChecker.getAnalysisResult().getTotalNumberOfStates() + " " + automaton.getName() + " size " + automaton.getStates().size());
          specs.remove(automaton);
          specplants.add(automaton);
        }
        /*if (specplants.contains(automaton) || specs.contains(automaton)) {
          thing.append(automaton.getName());
          thing.append(',');
        }*/
      }
      //System.out.println(thing);
    }
    setSatisfiedResult();
    return true;
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Inner Class Op
  private static class Op
  {
    public final AutomatonProxy added;
    public final Set<AutomatonProxy> others;

    public Op(final Collection<AutomatonProxy> possible, final AutomatonProxy add)
    {
      added = add;
      others = new HashSet<AutomatonProxy>(possible);
      others.remove(added);
    }
  }

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
  private final ControllabilityChecker mChecker;
  private final boolean mLeast;
  private int mStates;

}
