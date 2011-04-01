//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id: AnalyzeSICProperty6Action.java 5926 2010-09-23 03:54:41Z robi $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDSingularPropertyVerifier;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDSingularPropertyAction extends WatersAnalyzeAction
{

  protected AnalyzeSDSingularPropertyAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "S-Singular Prohibitable Behaviour";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy S-Singular Prohibitable Behaviour Property ";
  }

  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final ControllabilityChecker Checker =
        factory.createControllabilityChecker(desFactory);


        final SDSingularPropertyVerifier verifier =
        new SDSingularPropertyVerifier(null, desFactory, Checker);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies S-Singular Prohibitable Behaviour";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
