
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import java.util.Collection;


/**
 * A model verifier to check Proper Time Behavior.
 *
 * The check is done by converting the verification problem to an
 * equivalent nonblocking verification problem, and passing that to a
 * conflict checker.
 *
 * @see SDPropertyBuilder
 * @see ConflictChecker
 *
 * @author Robi Malik, Mahvash Baloch
 */

public class ProperTimeBehaviorVerifier extends AbstractSDConflictChecker
{

  //#########################################################################
  //# Constructors
  public ProperTimeBehaviorVerifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public ProperTimeBehaviorVerifier(final ConflictChecker checker,
                              final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public ProperTimeBehaviorVerifier(final ConflictChecker checker,
                              final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
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
      convertedModel = builder.createModelproperTimeB();
      final ConflictChecker checker = getConflictChecker();
      checker.setModel(convertedModel);

      final VerificationResult result;
      try {
        checker.run();
      } finally {
        result = checker.getAnalysisResult();
        setAnalysisResult(result);
              }
      if (result.isSatisfied()) {

        return true;
      } else {
        final ConflictTraceProxy counterexample = checker.getCounterExample();

        return setFailedResult(counterexample);
      }
    } finally {
      tearDown();
    }
  }


///#########################################################################
//# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
public boolean supportsNondeterminism()
{
  return false;
}
}
