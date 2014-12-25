//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSDActivityLoopAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDActivityLoopChecker;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDActivityLoopAction extends WatersAnalyzeAction
{

  //#########################################################################
  //# Constructors
  public AnalyzeSDActivityLoopAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected String getCheckName()
  {
    return "Activity Loop";
  }

  @Override
  protected String getFailureDescription()
  {
    return "has an activity loop";
  }

  @Override
  protected ModelVerifier getModelVerifier(final ModelAnalyzerFactory factory,
                                           final ProductDESProxyFactory desFactory) throws AnalysisConfigurationException
  {
    final ControlLoopChecker checker =
      factory.createControlLoopChecker(desFactory);
    if (checker == null) {
      return null;
    } else {
      return new SDActivityLoopChecker(checker, desFactory);
    }
  }

  @Override
  protected String getSuccessDescription()
  {
    return "is activity-loop free";
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 2167516363996006935L;

}
