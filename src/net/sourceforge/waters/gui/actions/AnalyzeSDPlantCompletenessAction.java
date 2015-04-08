//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty5Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDPlantCompletenessChecker;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDPlantCompletenessAction extends WatersAnalyzeAction
{

  protected AnalyzeSDPlantCompletenessAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "Plant Completeness";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy Plant Completeness";
  }

  protected ModelVerifier getModelVerifier
    (final ModelAnalyzerFactory factory,
     final ProductDESProxyFactory desFactory) throws AnalysisConfigurationException
  {  final ControllabilityChecker checker=
  factory.createControllabilityChecker(desFactory);

     return new SDPlantCompletenessChecker(desFactory,checker);
  }

  protected String getSuccessDescription()
  {
    return "satisfies Plant Completeness";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
