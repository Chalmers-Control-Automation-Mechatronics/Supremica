//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDCTwoVerifier;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDCTwoApropertyAction extends WatersAnalyzeAction
{

  protected AnalyzeSDCTwoApropertyAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SD Controllability ii_a";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy SD Controllability Point ii ";
  }

  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final LanguageInclusionChecker Checker =
        factory.createLanguageInclusionChecker(desFactory);


        final SDCTwoVerifier verifier =
        new SDCTwoVerifier(Checker, null, desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies SD Controllability Point ii";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
