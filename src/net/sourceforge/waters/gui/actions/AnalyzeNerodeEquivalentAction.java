//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty5Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.monolithic.MonolithicNerodeEChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeNerodeEquivalentAction extends WatersAnalyzeAction
{

  protected AnalyzeNerodeEquivalentAction(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SD Controllability iii.2 ";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy SD Controllability Point iii.2";
  }

  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    return new MonolithicNerodeEChecker(desFactory);
  }

  protected String getSuccessDescription()
  {
    return "satisfies SD Controllability Point iii.2";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
