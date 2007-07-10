//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id: ModularLanguageInclusionChecker.java,v 1.8 2007-07-10 01:52:06 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.AnalysisException;
import java.util.Comparator;
import java.util.Set;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import java.util.Collections;
import net.sourceforge.waters.xsd.base.ComponentKind;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;


public class ModularLanguageInclusionChecker
  extends AbstractModelVerifier
  implements LanguageInclusionChecker
{
  private final ControllabilityChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  private int mStates;
  
  public ModularLanguageInclusionChecker(ProductDESProxy model,
                                         ProductDESProxyFactory factory,
                                         ControllabilityChecker checker,
                                         ModularHeuristic heuristic)
  {
    super(model, factory);
    mChecker = checker;
    mHeuristic = heuristic;
    mTranslator = ControllabilityKindTranslator.getInstance();
    mStates = 0;
    setStateLimit(10000000);
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
    final List<AutomatonProxy> properties = new ArrayList<AutomatonProxy>();
    Set<AutomatonProxy> automata = 
      new HashSet<AutomatonProxy>(getModel().getAutomata().size());
    for (AutomatonProxy automaton : getModel().getAutomata()) {
      if (getKindTranslator().getComponentKind(automaton)
          == ComponentKind.PROPERTY) {
        properties.add(automaton);
      } else if (getKindTranslator().getComponentKind(automaton)
                 != ComponentKind.SUPERVISOR) {
        automata.add(automaton);
      }
    }
    Collections.sort(properties, new AutomatonComparator());
    /*for (final AutomatonProxy automaton : properties) {
      automata.add(automaton);
      ProductDESProxy model = 
        getFactory().createProductDESProxy("prop", getModel().getEvents(),
                                           automata);
      checker.setModel(model);
      checker.setKindTranslator(new KindTranslator()
      {
        public EventKind getEventKind(EventProxy e)
        {
          return EventKind.UNCONTROLLABLE;
        }
        
        public ComponentKind getComponentKind(AutomatonProxy a)
        {
          return a.equals(automaton) ? ComponentKind.SPEC : ComponentKind.PLANT;
        }
      });
      automata.remove(automaton);
      checker.setStateLimit(getStateLimit() - mStates);
      if (!checker.run()) {
        mStates += checker.getAnalysisResult().getTotalNumberOfStates();
        setFailedResult(mChecker.getCounterExample());
        return false;
      }
      mStates += checker.getAnalysisResult().getTotalNumberOfStates();
    }*/
    automata.addAll(properties);
    ProductDESProxy model = 
      getFactory().createProductDESProxy("prop", getModel().getEvents(),
                                         automata);
    mChecker.setModel(model);
    mChecker.setKindTranslator(new KindTranslator()
    {
      public EventKind getEventKind(EventProxy e)
      {
        return EventKind.UNCONTROLLABLE;
      }
      
      public ComponentKind getComponentKind(AutomatonProxy a)
      {
        return properties.contains(a) ? ComponentKind.SPEC : ComponentKind.PLANT;
      }
    });
    mChecker.setStateLimit(getStateLimit() - mStates);
    if (!mChecker.run()) {
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      setFailedResult(mChecker.getCounterExample());
      return false;
    }
    mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
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
