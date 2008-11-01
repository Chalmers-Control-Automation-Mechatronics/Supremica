//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class OneUncontrollableChecker
  extends AbstractModelVerifier
  implements ControllabilityChecker
{
  private final ControllabilityChecker mChecker;
  private KindTranslator mTranslator;
  private int mStates;
  
  public OneUncontrollableChecker(ProductDESProxy model,
                                  ProductDESProxyFactory factory,
                                  ControllabilityChecker checker)
  {
    super(model, factory);
    mChecker = checker;
    mTranslator = ControllabilityKindTranslator.getInstance();
    mStates = 0;
    setNodeLimit(5000000);
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
    for (final EventProxy event : getModel().getEvents()) {
      if (getKindTranslator().getEventKind(event) ==
          EventKind.UNCONTROLLABLE) {
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
          if (getKindTranslator().getComponentKind(a) == ComponentKind.SPEC) {
            if (!a.getEvents().contains(event)) {
              return ComponentKind.PLANT;
            }
          }
          return getKindTranslator().getComponentKind(a);
        }
      });
      mChecker.setNodeLimit(getNodeLimit()/* - mStates*/);
      if (!mChecker.run()) {
        // System.out.println(event.getName() + " uncontrollable");
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        setFailedResult(mChecker.getCounterExample());
        return false;
      }
      // System.out.println(event.getName() + " succeeded in " +
      //                    mChecker.getAnalysisResult().
      //                    getTotalNumberOfStates());
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
