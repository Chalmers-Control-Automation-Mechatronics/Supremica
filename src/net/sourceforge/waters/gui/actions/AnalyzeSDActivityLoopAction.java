package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDActivityLoopChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDActivityLoopAction extends WatersAnalyzeAction
{
  protected AnalyzeSDActivityLoopAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "Activity Loop";
  }

  protected String getFailureDescription()
  {
    return "has an activity loop";
  }

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    return new SDActivityLoopChecker(null, desFactory);
  }

  protected String getSuccessDescription()
  {
    return "is Activity-loop free";
  }

  private static final long serialVersionUID = 2167516363996006935L;
}
