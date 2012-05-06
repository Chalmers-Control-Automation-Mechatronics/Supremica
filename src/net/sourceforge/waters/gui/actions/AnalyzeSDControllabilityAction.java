package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;

public class AnalyzeSDControllabilityAction extends WatersAnalyzeAction
{
  protected AnalyzeSDControllabilityAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SD Controllability i";
  }

  protected String getFailureDescription()
  {
    return "is not controllable";
  }

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    final ControllabilityChecker checker=
      factory.createControllabilityChecker(desFactory);
    return new SDControllabilityChecker(desFactory,checker);
  }

  protected String getSuccessDescription()
  {
    return "is Controllable";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
