package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;

public class AnalyzeConflictCheckAction
extends WatersAnalyzeAction
{
  protected AnalyzeConflictCheckAction(final IDE ide)
  {
    super(ide);
  }

  private static final long serialVersionUID = -8684703946705836025L;

  protected String getCheckName()
  {
    return "Conflict";
  }

  protected String getFailureDescription()
  {
    return "is blocking";
  }

  protected ModelVerifier getModelVerifier(final ModelAnalyzerFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    return factory.createConflictChecker(desFactory);
  }

  protected String getSuccessDescription()
  {
    return "is nonblocking";
  }
}
