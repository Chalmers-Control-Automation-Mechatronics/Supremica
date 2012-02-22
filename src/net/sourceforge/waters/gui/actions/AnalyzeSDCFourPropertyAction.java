//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDCFourVerifier;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDCFourPropertyAction extends WatersAnalyzeAction
{

  protected AnalyzeSDCFourPropertyAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SD Controllability Four";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy SD Controllability Point iv ";
  }

  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final LanguageInclusionChecker Checker =
      factory.createLanguageInclusionChecker(desFactory);


        final SDCFourVerifier verifier =
        new SDCFourVerifier(Checker, null, desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies SD Controllability Point iv";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
