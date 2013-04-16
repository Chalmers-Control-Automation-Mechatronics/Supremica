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
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityDiagnostics;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class OneUncontrollableChecker
  extends AbstractSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public OneUncontrollableChecker(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final ControllabilityChecker checker)
  {
    super(model,
          ControllabilityKindTranslator.getInstance(),
          ControllabilityDiagnostics.getInstance(),
          factory);
    mChecker = checker;
    mStates = 0;
    setNodeLimit(5000000);
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    setUp();
    mStates = 0;
    final List<EventProxy> uncontrollables = new ArrayList<EventProxy>();
    for (final EventProxy event : getModel().getEvents()) {
      if (getKindTranslator().getEventKind(event) ==
          EventKind.UNCONTROLLABLE) {
        uncontrollables.add(event);
      }
    }
    Collections.sort(uncontrollables);
    for (final EventProxy event : uncontrollables) {
      mChecker.setModel(getModel());
      mChecker.setKindTranslator(new KindTranslator()
      {
        public EventKind getEventKind(final EventProxy e)
        {
          return e.equals(event) ? EventKind.UNCONTROLLABLE
                                 : EventKind.CONTROLLABLE;
        }

        public ComponentKind getComponentKind(final AutomatonProxy a)
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


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Data Members
  private final ControllabilityChecker mChecker;
  private int mStates;

}
