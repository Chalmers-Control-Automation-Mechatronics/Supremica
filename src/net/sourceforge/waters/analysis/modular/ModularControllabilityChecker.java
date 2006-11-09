package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import java.io.File;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.plain.des.SafetyTraceElement;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElement;
import net.sourceforge.waters.model.des.EventProxy;
import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.waters.xsd.base.ComponentKind;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;

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
  {
    int states = 0;
    Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> specs = new HashSet<AutomatonProxy>();
    for (AutomatonProxy automaton : getModel().getAutomata()) {
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);
                      break;
        case PROPERTY : specs.add(automaton);
                        break;
        default : break;
      }
    }
    System.out.println(specs.size());
    int j = 0;
    while (!specs.isEmpty()) {
      System.out.println(j++);
      Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
      Set<EventProxy> events = new HashSet<EventProxy>();
      Set<AutomatonProxy> uncomposedplants = new HashSet<AutomatonProxy>(plants);
      Set<AutomatonProxy> uncomposedspecs = new HashSet<AutomatonProxy>(specs);
      AutomatonProxy spec = specs.iterator().next();
      composition.add(spec);
      events.addAll(spec.getEvents());
      uncomposedspecs.remove(spec);
      ProductDESProxy comp = getFactory().createProductDESProxy("comp", events, composition);
      mChecker.setModel(comp);
      mChecker.setKindTranslator(getKindTranslator());
      int i = 0;
      while (!mChecker.run()) {
        System.out.println(i++);
        Collection<AutomatonProxy> newComp = mHeuristic.heur(comp,
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
    }
    setSatisfiedResult();
    return true;
  }
  
  public static void main(String[] args) throws Exception
  {
    ProductDESProxyFactory mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    JAXBTraceMarshaller mTraceMarshaller = new JAXBTraceMarshaller(mProductDESProxyFactory);
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
