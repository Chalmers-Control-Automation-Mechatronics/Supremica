//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularControllabilityChecker
//###########################################################################
//# $Id: ModularControllabilityChecker.java,v 1.5 2006-11-17 03:38:22 robi Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

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
  
  public ModularControllabilityChecker(ProductDESProxy model,
                                       ProductDESProxyFactory factory,
                                       ControllabilityChecker checker,
                                       ModularHeuristic heuristic)
  {
    super(model, factory);
    mChecker = checker;
    mHeuristic = heuristic;
    mTranslator = IdenticalKindTranslator.getInstance();
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
    mChecker.setStateLimit(getStateLimit());

    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
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
    System.out.println(specs.size());
    while (!specs.isEmpty()) {
      Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
      Set<EventProxy> events = new HashSet<EventProxy>();
      Set<AutomatonProxy> uncomposedplants = new HashSet<AutomatonProxy>(plants);
      Set<AutomatonProxy> uncomposedspecs = new HashSet<AutomatonProxy>(specs);
      AutomatonProxy spec = specs.first();
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
      while (!mChecker.run()) {
        Collection<AutomatonProxy> newComp =
          mHeuristic.heur(comp,
                          uncomposedplants,
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
          uncomposedspecs.remove(automaton);
          events.addAll(automaton.getEvents());
        }
        comp = getFactory().createProductDESProxy("comp", events, composition);
        mChecker.setModel(comp);
      }
      specs.removeAll(composition);
      plants.addAll(composition);
      assert(specs.size() + plants.size() == getModel().getAutomata().size());
    }
    setSatisfiedResult();
    return true;
  }
  
  public static void main(String[] args) throws Exception
  {
    ProductDESProxyFactory mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    JAXBProductDESMarshaller mProductDESMarshaller =
      new JAXBProductDESMarshaller(mProductDESProxyFactory);
    ModuleProxyFactory mModuleProxyFactory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller modmarshaller =
      new JAXBModuleMarshaller(mModuleProxyFactory, optable);
    DocumentManager mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(modmarshaller);
    ProductDESProxy mod = (ProductDESProxy)mDocumentManager.load(new File(args[0]));
    ControllabilityChecker check = new ModularControllabilityChecker(mod, mProductDESProxyFactory, 
                                                                     new MonolithicControllabilityChecker(mProductDESProxyFactory.createProductDESProxy("empty"), mProductDESProxyFactory),
                                                                     new MaxCommonEventsHeuristic(false));
    check.run();
  }
}
