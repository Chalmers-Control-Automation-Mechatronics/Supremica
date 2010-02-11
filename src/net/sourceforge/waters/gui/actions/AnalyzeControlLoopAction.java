package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.actions.WatersAnalyzeAction;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeControlLoopAction extends WatersAnalyzeAction
{
  protected AnalyzeControlLoopAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "Control Loop";
  }

  protected String getFailureDescription()
  {
    return "has a control loop";
  }

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    return factory.createControlLoopChecker(desFactory);
  }

  protected String getSuccessDescription()
  {
    return "is control-loop free";
  }

  private static final long serialVersionUID = 2167516363996006935L;
}
