//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularControllabilityChecker
//###########################################################################
//# $Id: ModularControllabilityChecker.java,v 1.6 2006-12-01 02:16:42 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.VerificationResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.monolithic.
       MonolithicControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ModularControllabilityChecker
  extends AbstractModelVerifier
  implements ControllabilityChecker
{
  private final ControllabilityChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  private int mStates;
  private final boolean mLeast;
 
  public ModularControllabilityChecker(ProductDESProxy model,
                                       ProductDESProxyFactory factory,
                                       ControllabilityChecker checker,
                                       ModularHeuristic heuristic,
                                       boolean least)
  {
    super(model, factory);
    mChecker = checker;
    mHeuristic = heuristic;
    mTranslator = IdenticalKindTranslator.getInstance();
    mStates = 0;
    mLeast = least;
    setStateLimit(2000000);
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
    System.out.println("automata: " + getModel().getAutomata().size());
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
      System.out.println("specs: " + specs.size());
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
      mChecker.setStateLimit(getStateLimit() - mStates);
      while (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        mChecker.setStateLimit(getStateLimit() - mStates);
        Collection<AutomatonProxy> newComp =
          mHeuristic.heur(comp,
                          uncomposedplants,
                          uncomposedspecplants,
                          uncomposedspecs,
                          mChecker.getCounterExample(),
                          getKindTranslator());
        if (newComp == null) {
          setFailedResult(mChecker.getCounterExample());
          return false;
        }
        for (AutomatonProxy automaton : newComp) {
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
      /*specs.removeAll(composition);
      plants.addAll(composition);*/
      StringBuffer thing = new StringBuffer();
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
  
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }
}
