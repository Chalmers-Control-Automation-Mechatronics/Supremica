package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.bdd.BDDLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;

public class AnalyzeLanguageInclusionAction extends WatersAnalyzeAction
{
  protected AnalyzeLanguageInclusionAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "Language Inclusion";
  }

  protected String getFailureDescription()
  {
    return "is not Language Inclusive";
  }

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    final BDDLanguageInclusionChecker verifier =
        new BDDLanguageInclusionChecker(desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "is Language Inclusive";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
