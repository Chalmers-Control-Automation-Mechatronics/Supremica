//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id: AnalyzeSICProperty6Action.java 5926 2010-09-23 03:54:41Z robi $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDThreeOneVerifier;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSDCThree_one_propertyAction extends WatersAnalyzeAction
{

  protected AnalyzeSDCThree_one_propertyAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SD Controllability iii.1";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy SD Controllability Point iii.1  ";
  }

  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final LanguageInclusionChecker Checker =
        factory.createLanguageInclusionChecker(desFactory);


        final SDThreeOneVerifier verifier =
        new SDThreeOneVerifier(Checker,null, desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies SD Controllability Point iii.1";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
