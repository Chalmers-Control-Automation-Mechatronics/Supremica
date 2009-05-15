//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.composing
//# CLASS:   ComposingLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.composing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ComposingLanguageInclusionChecker
  extends ComposingSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public ComposingLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public ComposingLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {    
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);   
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    final ProductDESProxy model = getModel();
    System.out.println("Original Events: "+model.getEvents().size());
    final KindTranslator translator0 = getKindTranslator();
    final SingleSpecKindTranslator translator1 =
      new SingleSpecKindTranslator(translator0);
    final ProductDESProxyFactory factory = getFactory();
    final ConvertModelLang converter =
      new ConvertModelLang(model, translator1, factory);
    final ComposingSafetyVerifier verifier =
      new ComposingSafetyVerifier(translator0, factory);
    verifier.setNodeLimit(getNodeLimit());
    verifier.setProjectionNodeLimit(getProjectionNodeLimit());
    verifier.setHeuristic(getHeuristic());
    resetStatistics();
    for (final AutomatonProxy aut : model.getAutomata()) {
      if (translator0.getComponentKind(aut) == ComponentKind.SPEC) {
	translator1.setCurrentSpec(aut);
	final ProductDESProxy convmodel = converter.run();
	verifier.setModel(convmodel);
	verifier.run();
	recordStatistics(verifier);
	if (!verifier.isSatisfied()) {
	  final String tracename =
	    model.getName() + ":" + aut.getName() + ":false";
	  final SafetyTraceProxy counterexample = verifier.getCounterExample();
	  final List<EventProxy> oldlist = counterexample.getEvents();
	  final List<EventProxy> newlist = convertTrace(converter, oldlist);
	  final SafetyTraceProxy fixedCounterexample =
	    factory.createSafetyTraceProxy(tracename, model, newlist);
	  return setFailedResult(fixedCounterexample);
	}
      }
    }
    return setSatisfiedResult();
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifier
  protected void addStatistics(final VerificationResult result)
  {
    result.setNumberOfAutomata(mTotalNumberOfAutomata);
    result.setTotalNumberOfStates(mTotalNumberOfStates);
    result.setPeakNumberOfStates(mPeakNumberOfStates);
  }


  //#########################################################################
  //# Auxiliary Methods
  private List<EventProxy> convertTrace(final ConvertModelLang converter,
					final List<EventProxy> oldlist)
  {
    final int len = oldlist.size();
    final List<EventProxy> newlist = new ArrayList<EventProxy>(len);
    final Iterator<EventProxy> iter = oldlist.iterator();
    while (iter.hasNext()) {
      EventProxy event = iter.next();
      if (!iter.hasNext()) {
	event = converter.getOriginalEvent(event);
      }
      newlist.add(event);
    }
    return newlist;
  }

  private void resetStatistics()
  {
    mTotalNumberOfAutomata = 0;
    mTotalNumberOfStates = mPeakNumberOfStates = 0.0;
  }

  private void recordStatistics(final ModelVerifier subverifier)
  {
    final VerificationResult subresult = subverifier.getAnalysisResult();
    mTotalNumberOfAutomata += subresult.getTotalNumberOfAutomata();
    mTotalNumberOfStates += subresult.getTotalNumberOfStates();
    final double peak = subresult.getPeakNumberOfStates();
    if (peak > mPeakNumberOfStates) {
      mPeakNumberOfStates = peak;
    }
  }


  //#########################################################################
  //# Inner Class SingleSpecKindTranslator
  private static class SingleSpecKindTranslator implements KindTranslator
  {

    //#######################################################################
    //# Constructor
    private SingleSpecKindTranslator(final KindTranslator master)
    {
      mMaster = master;
    }

    //#######################################################################
    //# Configuration
    private void setCurrentSpec(final AutomatonProxy spec)
    {
      mCurrentSpec = spec;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      final ComponentKind kind = mMaster.getComponentKind(aut);
      if (kind == ComponentKind.SPEC && aut != mCurrentSpec) {
	return ComponentKind.PROPERTY;
      } else {
	return kind;
      }
    }

    public EventKind getEventKind(final EventProxy event)
    { 
      return mMaster.getEventKind(event);
    }

    //#######################################################################
    //# Data Members
    private final KindTranslator mMaster;
    private AutomatonProxy mCurrentSpec;
  }


  //#########################################################################
  //# Data Members
  private int mTotalNumberOfAutomata;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;

}
