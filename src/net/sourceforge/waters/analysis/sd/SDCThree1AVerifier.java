//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters SD Analysis
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDCThreeAVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * <P>A model verifier to check SD Controllability Property III.1a.</P>
 *
 * <P>The check is done by creating a test automaton and modifying plant
 * automata for each prohibitable event in the model, and passing these
 * models to a language inclusion checker</P>

 * <P><STRONG>Reference.</STRONG>
 * Mahvash Baloch. A compositional approach for verifying sampled-data
 * supervisory control. M.Sc. Thesis, Dept. of Computing and Software,
 * McMaster University, March 2012.</P>
 *
 * @see SDPropertyBuilder
 * @see LanguageInclusionChecker
 *
 * @author Mahvash Baloch , Robi Malik
 */

public class SDCThree1AVerifier extends AbstractSDLanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SDCThree1AVerifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SDCThree1AVerifier(final LanguageInclusionChecker checker,
                            final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SDCThree1AVerifier(final LanguageInclusionChecker checker,
                            final ProductDESProxy model,
                            final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mFailedProhibitable = null;
  }

  @Override
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final ProductDESProxy model = getModel();
      final SD_three_PropertyBuilder builder =
        new SD_three_PropertyBuilder(model, getFactory());
      final LanguageInclusionChecker checker = getLanguageInclusionChecker();
      final Collection<EventProxy> hibs = builder.getHibEvents();
      for (final EventProxy hib : hibs) {
        logHibEvent(hib, hibs);
        final ProductDESProxy convertedModel = builder.createSDThreeModel(hib);
        checker.setModel(convertedModel);
        try {
          checker.run();
        } finally {
          final VerificationResult result = checker.getAnalysisResult();
          recordStatistics(result);
        }
        final VerificationResult result = getAnalysisResult();
        if (!result.isSatisfied()) {
          final SafetyTraceProxy counterexample = checker.getCounterExample();
          mFailedProhibitable = hib;
          return setFailedResult(counterexample);
        }
      }
      return setSatisfiedResult();
    } finally {
      tearDown();
    }
  }

  public EventProxy getFailedProhibitable()
  {
    return mFailedProhibitable;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mFailedProhibitable;

}
