//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.sd.ProperTimeBehaviorVerifier;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeProperTimeBehaviorPropertyAction extends WatersAnalyzeAction
{

  protected AnalyzeProperTimeBehaviorPropertyAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "Proper Time Behavior";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy Proper Time Behavior";
  }

  protected ModelVerifier getModelVerifier
    (final ModelAnalyzerFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final ConflictChecker conflictChecker =
        factory.createConflictChecker(desFactory);
    final ProperTimeBehaviorVerifier verifier =
        new ProperTimeBehaviorVerifier(conflictChecker, null, desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies Proper Time Behavior";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
