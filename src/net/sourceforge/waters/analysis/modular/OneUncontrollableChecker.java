//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id: OneUncontrollableChecker.java,v 1.1 2006-12-01 02:16:42 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.AnalysisException;
import java.util.Comparator;
import java.util.Set;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import java.util.Collections;
import net.sourceforge.waters.xsd.base.ComponentKind;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;


public class OneUncontrollableChecker
  extends AbstractModelVerifier
  implements ControllabilityChecker
{
  private final ControllabilityChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  private int mStates;
  
  public OneUncontrollableChecker(ProductDESProxy model,
                                  ProductDESProxyFactory factory,
                                  ControllabilityChecker checker)
  {
    super(model, factory);
    mChecker = checker;
    mTranslator = IdenticalKindTranslator.getInstance();
    mStates = 0;
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
    List<EventProxy> uncontrollables = new ArrayList<EventProxy>();
    for (EventProxy event : getModel().getEvents()) {
      if (getKindTranslator().getEventKind(event)
          == EventKind.UNCONTROLLABLE) {
        uncontrollables.add(event);
      }
    }
    Collections.sort(uncontrollables, new EventComparator());
    for (final EventProxy event : uncontrollables) {
      mChecker.setModel(getModel());
      mChecker.setKindTranslator(new KindTranslator()
      {
        public EventKind getEventKind(EventProxy e)
        {
          return e.equals(event) ? EventKind.UNCONTROLLABLE
                                 : EventKind.CONTROLLABLE;
        }
        
        public ComponentKind getComponentKind(AutomatonProxy a)
        {
          return getKindTranslator().getComponentKind(a);
        }
      });
      mChecker.setStateLimit(getStateLimit() - mStates);
      if (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        setFailedResult(mChecker.getCounterExample());
        return false;
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
    }
    setSatisfiedResult();
    return true;
  }
  
  protected void addStatistics(VerificationResult result)
  {
    result.setNumberOfStates(mStates);
  }
  
  private final static class EventComparator
    implements Comparator<EventProxy>
  {
    public int compare(EventProxy a1, EventProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }
}
