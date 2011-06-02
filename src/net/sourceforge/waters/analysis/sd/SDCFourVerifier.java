//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;


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

public class SDCFourVerifier extends AbstractSafetyVerifier

{

  //#########################################################################
  //# Constructors
  public SDCFourVerifier( final ProductDESProxy model,
                              final ProductDESProxyFactory factory,
                              final ControllabilityChecker checker)

  {
    super(model,
          LanguageInclusionKindTranslator.getInstance(),
          LanguageInclusionDiagnostics.getInstance(),
          factory);
    mChecker = checker;
  }


  //#########################################################################
  //# Invocation
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

      final SDPropertyBuilder builder =
        new SDPropertyBuilder(model, getFactory());


      ProductDESProxy convertedModel = null;
      convertedModel = builder.createModelSDFour();

      final ModularLanguageInclusionChecker checker=
        new ModularLanguageInclusionChecker(convertedModel, getFactory(),
                                             mChecker );
      checker.setModel(convertedModel);

      final VerificationResult result;

      try {
        checker.run();
      } finally {
        result = checker.getAnalysisResult();
        setAnalysisResult(result);
      }
        if(result.isSatisfied())
        return(true);
        else
        { return (false);
        }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return mChecker.supportsNondeterminism();
  }


  //#########################################################################
  //# Data Members
  private final ControllabilityChecker mChecker;
}
