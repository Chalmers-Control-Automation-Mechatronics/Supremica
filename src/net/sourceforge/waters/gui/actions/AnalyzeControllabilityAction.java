package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;

public class AnalyzeControllabilityAction extends WatersAnalyzeAction
{

  protected AnalyzeControllabilityAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "Controllability";
  }

  protected String getFailureDescription()
  {
    return "is not controllable";
  }

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    return factory.createControllabilityChecker(desFactory);
  }

  protected String getSuccessDescription()
  {
    return "is controllable";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
