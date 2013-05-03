package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDNonSLALFBuilder;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDNSLActivityLoopAction extends WatersAnalyzeAction
{
  protected AnalyzeSDNSLActivityLoopAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "Non-Selfloop ALF";
  }

  protected String getFailureDescription()
  {
    return "has a non-self loop activity loop";
  }

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    return new SDNonSLALFBuilder(null, desFactory);
  }

  protected String getSuccessDescription()
  {
    return "is Non-Selfloop Activity-loop free";
  }

  private static final long serialVersionUID = 2167516363996006935L;
}
