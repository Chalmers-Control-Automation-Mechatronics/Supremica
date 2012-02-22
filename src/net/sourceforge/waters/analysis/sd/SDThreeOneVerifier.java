
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * A model verifier to check SD Controllability (iii.1) Property.
 *
 * This wrapper can be used to check whether a model satisfies
 * SD Property (iii.1)
 *
 * The check is done by creating a test automata and modifying Plant automata for
 * each prohibitable event in the model, and passing these models to a modular
 * language inclusion checker
 *
 * @see SDPropertyBuilder
 * @see Modular Language Inclusion Checker
 *
 * @author Mahvash Baloch , Robi Malik
 */

public class SDThreeOneVerifier extends AbstractSDLanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SDThreeOneVerifier( final ProductDESProxyFactory factory)
  {
  super(factory);
  }
  public SDThreeOneVerifier(   final LanguageInclusionChecker checker,
                            final ProductDESProxyFactory factory
                            )
  {
  super(checker,factory);
  }
    public SDThreeOneVerifier( final LanguageInclusionChecker checker,
                            final ProductDESProxy model,
                            final ProductDESProxyFactory factory)
    {
      super(checker, model, factory );
     }
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
      final LanguageInclusionChecker cChecker= getLanguageInclusionChecker();
      final SDCThreeaVerifier verifier1 =
          new SDCThreeaVerifier(cChecker, model, getFactory());
        final VerificationResult result1;
        try {
          verifier1.run();
        } finally {
          result1 = verifier1.getAnalysisResult();
          }
        if (!result1.isSatisfied()) {
          final SafetyTraceProxy counterexample =
              verifier1.getCounterExample();

          return setFailedResult(counterexample);
        }
      final SDCThreebVerifier verifier2 =
            new SDCThreebVerifier(cChecker, model, getFactory());
       final VerificationResult result2;
          try {
            verifier2.run();
          } finally {
            result2 = verifier2.getAnalysisResult();
             }
          if (!result2.isSatisfied()) {
            final SafetyTraceProxy counterexample =
                verifier2.getCounterExample();
            return setFailedResult(counterexample);
          }
          result2.merge(result1);
          return setSatisfiedResult();


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

