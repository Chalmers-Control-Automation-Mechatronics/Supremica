//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularControllabilityChecker
//###########################################################################
//# $Id: CullingControllabilityChecker.java,v 1.3 2007-02-12 03:54:09 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import java.io.PrintStream;
import net.sourceforge.waters.model.des.TraceProxy;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import net.sourceforge.waters.model.analysis.VerificationResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class CullingControllabilityChecker
  extends AbstractModelVerifier
  implements ControllabilityChecker
{
  private final ControllabilityChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  private int mStates;
  private final boolean mLeast;
  static ModularHeuristic ALL = new AllHeuristic(HeuristicType.NOPREF);
  private final PrintStream mOut;
 
  public CullingControllabilityChecker(ProductDESProxy model,
                                       ProductDESProxyFactory factory,
                                       ControllabilityChecker checker,
                                       ModularHeuristic heuristic,
                                       boolean least, PrintStream out)
  {
    super(model, factory);
    mChecker = checker;
    mHeuristic = heuristic;
    mTranslator = IdenticalKindTranslator.getInstance();
    mStates = 0;
    mLeast = least;
    setStateLimit(2000000);
    mOut = out;
  }
  
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy)super.getCounterExample();
  }
  
  public KindTranslator getKindTranslator()
  {
    return mTranslator;
  }
  
  public void setKindTranslator(KindTranslator trans)
  {
    mTranslator = trans;
  }
  
  public boolean run()
    throws AnalysisException
  {
    mStates = 0;
    mChecker.setStateLimit(getStateLimit());
    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specplants = new HashSet<AutomatonProxy>();
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
    while (!specs.isEmpty()) {
      List<Op> oportunitys = new LinkedList<Op>();
      Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
      Set<EventProxy> events = new HashSet<EventProxy>();
      SortedSet<AutomatonProxy> uncomposedplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecs = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      uncomposedplants.addAll(plants);
      uncomposedspecplants.addAll(specplants);
      uncomposedspecs.addAll(specs);
      AutomatonProxy spec = mLeast ? specs.first() : specs.last();
      composition.add(spec);
      events.addAll(spec.getEvents());
      uncomposedspecs.remove(spec);
      ProductDESProxy comp = getFactory().createProductDESProxy("comp", events, composition);
      mChecker.setModel(comp);
      mChecker.setKindTranslator(new KindTranslator()
      {
        public EventKind getEventKind(EventProxy e)
        {
          return getKindTranslator().getEventKind(e);
        }
        
        public ComponentKind getComponentKind(AutomatonProxy a)
        {
          return specs.contains(a) ? ComponentKind.SPEC
                                   : ComponentKind.PLANT;
        }
      });
      mChecker.setStateLimit(getStateLimit());
      mainloop:
      while (!mChecker.run()) {
        TraceProxy newcounter = mChecker.getCounterExample();
        mOut.println("Size:" + newcounter.getEvents().size());
        mOut.println(newcounter);
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        mChecker.setStateLimit(getStateLimit());
        while (newcounter != null) {
          TraceProxy counter = newcounter;
          newcounter = null;
          Collection<AutomatonProxy> newComp =
            mHeuristic.heur(comp,
                            uncomposedplants,
                            uncomposedspecplants,
                            uncomposedspecs,
                            counter,
                            getKindTranslator());
          if (newComp == null) {
            setFailedResult(mChecker.getCounterExample());
            return false;
          }
          Op op = new Op(ALL.heur(comp,
                                  uncomposedplants,
                                  uncomposedspecplants,
                                  uncomposedspecs,
                                  counter,
                                  getKindTranslator()), newComp.iterator().next());
          Set<AutomatonProxy> possible = new HashSet<AutomatonProxy>(op.others);
          possible.add(op.added);
          CheckSuffix.checkSuffix(counter, possible, null);
          int prevstates = mChecker.getAnalysisResult().getTotalNumberOfStates();
          System.out.println(op.added.getName() + " added " + mStates + " " + composition.size() + " " + prevstates);
          uncomposedplants.remove(op.added);
          uncomposedspecplants.remove(op.added);
          uncomposedspecs.remove(op.added);
          events.addAll(op.added.getEvents());
          Iterator<Op> it = oportunitys.iterator();
          while (it.hasNext()) {
            Op oportunity = it.next();
            if (oportunity.others.contains(op.added)) {
              composition.remove(oportunity.added);
              comp = getFactory().createProductDESProxy("comp", events, composition);
              mChecker.setModel(comp);
              //mChecker.setStateLimit(getStateLimit() - mStates);
              if (mChecker.run()) {
                break mainloop;
              };
              mOut.println("Size:" + mChecker.getCounterExample().getEvents().size());
              mOut.println(mChecker.getCounterExample());
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
        mChecker.setStateLimit(getStateLimit());
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      /*specs.removeAll(composition);
      plants.addAll(composition);*/
      for (AutomatonProxy automaton : composition) {
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
  
  protected void addStatistics(VerificationResult result)
  {
    result.setNumberOfStates(mStates);
  }
  
  private static class Op
  {
    public final AutomatonProxy added;
    public final Set<AutomatonProxy> others;
    
    public Op(Collection<AutomatonProxy> possible, AutomatonProxy add)
    {
      added = add;
      others = new HashSet<AutomatonProxy>(possible);
      others.remove(added);
    }
  }
  
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }
}
