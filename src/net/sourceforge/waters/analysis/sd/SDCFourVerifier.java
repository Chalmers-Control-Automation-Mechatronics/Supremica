//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * A model verifier to check SD Controllability Property Four.
 *
 *
 *
 * @see SDPropertyBuilder
 * @see LanguageInclusionChecker
 *
 * @author Mahvash Baloch, Robi Malik
 */

public class SDCFourVerifier extends AbstractSDLanguageInclusionChecker

{

  //#########################################################################
  //# Constructors
public SDCFourVerifier( final ProductDESProxyFactory factory)

{
super(factory);
}
public SDCFourVerifier(   final LanguageInclusionChecker checker,
                          final ProductDESProxyFactory factory
                          )

{
super(checker,factory);

}
  public SDCFourVerifier( final LanguageInclusionChecker checker,
                          final ProductDESProxy model,
                          final ProductDESProxyFactory factory
                              )

  {
    super(checker, model, factory );
   }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final ProductDESProxy model = getModel();

      final SDPropertyBuilder builder =
        new SDPropertyBuilder(model, getFactory());


      ProductDESProxy convertedModel = null;
      convertedModel = builder.createModelSDFour();

      final LanguageInclusionChecker checker= getLanguageInclusionChecker();
      checker.setModel(convertedModel);

      final VerificationResult result;

      try {
        checker.run();
      } finally {
        result = checker.getAnalysisResult();
        setAnalysisResult(result);

      }
        if(result.isSatisfied())
          {

          return(true);
          }
        else
        { final SafetyTraceProxy counterexample = checker.getCounterExample();
          return setFailedResult(counterexample);
        }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return false;
  }



}
