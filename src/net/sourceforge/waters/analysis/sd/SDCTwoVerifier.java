
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;
/**
 * A model verifier to check SD Controllability Property.
 *
 * This wrapper can be used to check whether a model satisfies
 * SD Controllability Property ii.a
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

  //#########################################################################
  //# Constructors
  public class SDCTwoVerifier extends AbstractSDLanguageInclusionChecker
  {

    //#########################################################################
    //# Constructors
    public SDCTwoVerifier( final ProductDESProxyFactory factory)
    {
    super(factory);
    }
    public SDCTwoVerifier(   final LanguageInclusionChecker checker,
                              final ProductDESProxyFactory factory
                              )
    {
    super(checker,factory);
    }
      public SDCTwoVerifier( final LanguageInclusionChecker checker,
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

      final SD_Two_PropertyBuilder builder =
          new SD_Two_PropertyBuilder(model, getFactory());

      ProductDESProxy convertedModel = null;

      convertedModel = builder.createSDTwoModel();
      final LanguageInclusionChecker checker= getLanguageInclusionChecker();

        checker.setModel(convertedModel);
        final VerificationResult result;
        try {
          checker.run();
        } finally {

          result = checker.getAnalysisResult();
          setAnalysisResult(result);

          }
        if (!result.isSatisfied()) {
          final SafetyTraceProxy counterexample =
              checker.getCounterExample();

          return setFailedResult(counterexample);
        }

    return setSatisfiedResult();
    }
    catch (final AnalysisException exception) {
      final VerificationResult result = getAnalysisResult();
      result.setException(exception);
      throw exception;
    }
    finally {
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
    mFailedAnswer = null;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mFailedAnswer;


  }