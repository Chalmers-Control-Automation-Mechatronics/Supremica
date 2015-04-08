//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.SDSingularProhibitableBehaviorVerifier;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
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
    (final ModelAnalyzerFactory factory,
     final ProductDESProxyFactory desFactory) throws AnalysisConfigurationException
  {
    final LanguageInclusionChecker Checker =
        factory.createLanguageInclusionChecker(desFactory);


        final SDSingularProhibitableBehaviorVerifier verifier =
        new SDSingularProhibitableBehaviorVerifier(Checker,null, desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies S-Singular Prohibitable Behaviour";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
