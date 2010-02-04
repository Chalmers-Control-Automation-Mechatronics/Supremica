package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.despot.SICPropertyVVerifier;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSICPropertyVAction extends WatersAnalyzeAction
{

  protected AnalyzeSICPropertyVAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SIC Property V";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy SIC Property V";
  }

  protected boolean getAllowLastStep()
  {
    return true;
  }

  protected ModelVerifier getModelVerifier(
                                           final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    final ConflictChecker conflictChecker =
        factory.createConflictChecker(desFactory);
    final SICPropertyVVerifier verifier =
        new SICPropertyVVerifier(conflictChecker, null, desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies SIC Property V";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
