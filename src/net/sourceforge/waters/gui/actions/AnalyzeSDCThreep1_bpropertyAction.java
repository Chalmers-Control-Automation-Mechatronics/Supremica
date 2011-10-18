//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDCThreebVerifier;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDCThreep1_bpropertyAction extends WatersAnalyzeAction
{

  protected AnalyzeSDCThreep1_bpropertyAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SD Controllability iii.1_b";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy SD Controllability iii.1 Property ";
  }

  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final ControllabilityChecker Checker =
        factory.createControllabilityChecker(desFactory);


        final SDCThreebVerifier verifier =
        new SDCThreebVerifier(null, desFactory, Checker);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies SD Controllability Property iii.1";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
