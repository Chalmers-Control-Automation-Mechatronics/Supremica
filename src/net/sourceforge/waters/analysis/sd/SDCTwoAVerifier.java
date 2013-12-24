//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters SD Analysis
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDCTwoAVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * <P>A model verifier to check SD Controllability Property II.a.</P>
 *
 * <P><STRONG>Reference.</STRONG> Mahvash Baloch. A compositional approach for
 * verifying sampled-data supervisory control. M.Sc. Thesis, Dept. of
 * Computing and Software, McMaster University, March 2012.</P>
 *
 * @see SD_Two_PropertyBuilder
 * @see LanguageInclusionChecker
 *
 * @author Mahvash Baloch , Robi Malik
 */


//#########################################################################
//# Constructors
public class SDCTwoAVerifier extends AbstractSDLanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SDCTwoAVerifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SDCTwoAVerifier(final LanguageInclusionChecker checker,
                         final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SDCTwoAVerifier(final LanguageInclusionChecker checker,
                         final ProductDESProxy model,
                         final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final ProductDESProxy model = getModel();
      final Collection<AutomatonProxy> oldAutomata = model.getAutomata();
      final int numaut = oldAutomata.size();
      if (numaut == 0) {
        return setSatisfiedResult();
      }
      final SD_Two_PropertyBuilder builder =
        new SD_Two_PropertyBuilder(model, getFactory());
      final ProductDESProxy convertedModel = builder.createSDTwoModel();
      final LanguageInclusionChecker checker = getLanguageInclusionChecker();
      checker.setModel(convertedModel);
      final VerificationResult result;
      try {
        checker.run();
      } finally {
        result = checker.getAnalysisResult();
        setAnalysisResult(result);
      }
      if (result.isSatisfied()) {
        return setSatisfiedResult();
      } else {
        final SafetyTraceProxy counterexample = checker.getCounterExample();
        return setFailedResult(counterexample);
      }
    } catch (final AnalysisException exception) {
      final VerificationResult result = getAnalysisResult();
      result.setException(exception);
      throw exception;
    } finally {
      tearDown();
    }
  }

}