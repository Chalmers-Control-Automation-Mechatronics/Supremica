//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id: ModularLanguageInclusionChecker.java,v 1.13 2008-06-30 01:50:57 robi Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
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

import org.apache.log4j.Logger;


public class ModularLanguageInclusionChecker
  extends AbstractModelVerifier
  implements LanguageInclusionChecker
{
  
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
    setNodeLimit(10000000);
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
    for (AutomatonProxy p : properties) {
      automata.add(p);
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
      mChecker.setNodeLimit(getNodeLimit() - mStates);
      if (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        return setFailedResult(mChecker.getCounterExample(), p);
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      automata.remove(p);
    }
    setSatisfiedResult();
    return true;
  }
  
  protected void addStatistics(VerificationResult result)
  {
    result.setNumberOfStates(mStates);
  } 


  //#########################################################################
  //# Auxiliary Methods
  private boolean setFailedResult(final TraceProxy counterexample,
                                  final AutomatonProxy property)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String propname = property.getName();
    final String tracename = desname + ":" + propname;
    final Collection<AutomatonProxy> automata = counterexample.getAutomata();
    final List<TraceStepProxy> steps = counterexample.getTraceSteps();
    final SafetyTraceProxy wrapper =
      factory.createSafetyTraceProxy(tracename, null, null,
                                     des, automata, steps);
    return setFailedResult(wrapper);
  }


  //#########################################################################
  //# Inner Class AutomatonComparator
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }


  //#########################################################################
  //# Data Members
  private final ControllabilityChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  private int mStates;


  //#########################################################################
  //# Static Class Variables
  private static final Logger LOGGER =
    Logger.getLogger(ModularLanguageInclusionChecker.class);

}
