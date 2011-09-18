
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
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

public class SDCTwoVerifier extends AbstractSafetyVerifier
{

  //#########################################################################
  //# Constructors
  public SDCTwoVerifier( final ProductDESProxy model,
                                     final ProductDESProxyFactory factory,
                                     final ControllabilityChecker checker)

         {
           super(model,
                 LanguageInclusionKindTranslator.getInstance(),
                 LanguageInclusionDiagnostics.getInstance(),
                 factory);
           cChecker = checker;
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
      final ModularLanguageInclusionChecker checker=
         new ModularLanguageInclusionChecker(convertedModel, getFactory(),
                                              cChecker );
        checker.setModel(convertedModel);
        final VerificationResult result;
        try {
          checker.run();
        } finally {

          result = checker.getAnalysisResult();

          recordStatistics(result);
        }
        if (!result.isSatisfied()) {
          final SafetyTraceProxy counterexample =
              checker.getCounterExample();

          return setFailedResult(counterexample);
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
    return cChecker.supportsNondeterminism();
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.ModelAnalyser
  @Override
  public SDPropertyVerifierVerificationResult getAnalysisResult()
  {
    return (SDPropertyVerifierVerificationResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mPeakNumberOfNodes = -1;
    mTotalNumberOfStates = mTotalNumberOfTransitions = 0.0;
    mPeakNumberOfStates = mPeakNumberOfTransitions = -1.0;
  }

  @Override
  protected SDPropertyVerifierVerificationResult createAnalysisResult()
  {
    return new SDPropertyVerifierVerificationResult();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final SDPropertyVerifierVerificationResult stats = getAnalysisResult();
    stats.setPeakNumberOfNodes(mPeakNumberOfNodes);
    stats.setTotalNumberOfStates(mTotalNumberOfStates);
    stats.setPeakNumberOfStates(mPeakNumberOfStates);
    stats.setTotalNumberOfTransitions(mTotalNumberOfTransitions);
    stats.setPeakNumberOfTransitions(mPeakNumberOfTransitions);
    }


  //#########################################################################
  //# Auxiliary Methods

  private void recordStatistics(final VerificationResult result)
  {
    mPeakNumberOfNodes =
        Math.max(mPeakNumberOfNodes, result.getPeakNumberOfNodes());
    mTotalNumberOfStates += result.getPeakNumberOfStates();
    mPeakNumberOfStates =
        Math.max(mPeakNumberOfStates, result.getPeakNumberOfStates());
    mTotalNumberOfTransitions += result.getPeakNumberOfTransitions();
    mPeakNumberOfTransitions =
        Math.max(mPeakNumberOfTransitions, result.getPeakNumberOfTransitions());

  }


  //#########################################################################
  //# Data Members
  private EventProxy mFailedAnswer;

  private int mPeakNumberOfNodes;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;


  private final ControllabilityChecker cChecker;
}
