
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.List;

import net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * A model verifier to check SD Controllability Property.
 *
 * This wrapper can be used to check whether a model satisfies
 * SD Controllability Property iii.1
 *
 * The check is done by creating a test automata and modifying Plant automata for
 * each prohibitable event in the model, and passing these models to a modular
 * language inclusion checker
 *
 * @see SDPropertyBuilder
 * @see ModularLanguageInclusionChecker
 *
 * @author Mahvash Baloch , Robi Malik
 */

public class SDCThreebVerifier extends AbstractSDLanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SDCThreebVerifier( final ProductDESProxyFactory factory)
  {
  super(factory);
  }
  public SDCThreebVerifier(   final LanguageInclusionChecker checker,
                            final ProductDESProxyFactory factory
                            )
  {
  super(checker,factory);
  }
    public SDCThreebVerifier( final LanguageInclusionChecker checker,
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
      final SD_three_PropertyBuilder builder =
          new SD_three_PropertyBuilder(model, getFactory());
      final List<EventProxy> Hibs =
          (List<EventProxy>) builder.getHibEvents();

      ProductDESProxy convertedModel = null;

      for (final EventProxy hib : Hibs)
       {
        convertedModel = builder.createSDThree_bModel(hib);
        final LanguageInclusionChecker checker= getLanguageInclusionChecker();
        checker.setModel(convertedModel);
        final VerificationResult result;
        try {
          checker.run();
        } finally {

          result = checker.getAnalysisResult();
          mergeAll(result);
          recordStatistics(result);
          setAnalysisResult(Result);
        }
        if (!result.isSatisfied()) {
          final SafetyTraceProxy counterexample =
              checker.getCounterExample();
          mFailedAnswer = hib;
          return setFailedResult(counterexample);
        }
      }
      return setSatisfiedResult();


    } finally {
      tearDown();

    }
  }

  public EventProxy getFailedAnswer()
  {
    return mFailedAnswer;
  }

//#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return false;
  }

//#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    mFirstResult = true;
    mFailedAnswer = null;
  }

   //#########################################################################
  //# Auxiliary Methods

  private void recordStatistics(final AnalysisResult result)
  {
     if (mFirstResult) {
       setAnalysisResult(result);
       mFirstResult = false;
     } else {
       final AnalysisResult present = getAnalysisResult();
       final int numaut1 = present.getTotalNumberOfAutomata();
       final int numaut2 = result.getTotalNumberOfAutomata();
       final int numaut = Math.max(numaut1, numaut2);
       present.merge(result);
       present.setNumberOfAutomata(numaut);
     }
  }
  private void mergeAll(final AnalysisResult result)
  {
     if (mFirstResult) {
       Result = result;
     } else {
       final int numaut1 = Result.getTotalNumberOfAutomata();
       final int numaut2 = result.getTotalNumberOfAutomata();
       final int numaut = Math.max(numaut1, numaut2);
       Result.merge(result);
       Result.setNumberOfAutomata(numaut);
     }
  }
  //#########################################################################
  //# Data Members
  private EventProxy mFailedAnswer;
  private boolean mFirstResult;
  private AnalysisResult Result;
}
