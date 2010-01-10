package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
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

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory factory,
                                           final ProductDESProxyFactory desFactory)
  {
    return factory.createConflictChecker(desFactory);
  }

  protected String getSuccessDescription()
  {
    return "is non-blocking";
  }
}
