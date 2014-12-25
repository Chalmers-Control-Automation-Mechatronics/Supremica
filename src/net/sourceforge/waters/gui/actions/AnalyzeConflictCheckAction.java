package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
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

  @Override
  protected String getCheckName()
  {
    return "Conflict";
  }

  @Override
  protected String getFailureDescription()
  {
    return "is blocking";
  }

  @Override
  protected ModelVerifier getModelVerifier(final ModelAnalyzerFactory factory,
                                           final ProductDESProxyFactory desFactory)
    throws AnalysisConfigurationException
  {
    return factory.createConflictChecker(desFactory);
  }

  @Override
  protected String getSuccessDescription()
  {
    return "is nonblocking";
  }
}
