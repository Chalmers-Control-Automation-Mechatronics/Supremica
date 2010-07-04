//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.despot.SICProperty6Verifier;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


public class AnalyzeSICProperty6Action extends WatersAnalyzeAction
{

  protected AnalyzeSICProperty6Action(final IDE ide)
  {
    super(ide);
  }

  protected String getCheckName()
  {
    return "SIC Property VI";
  }

  protected String getFailureDescription()
  {
    return "does not satisfy SIC Property VI";
  }

  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final ConflictChecker conflictChecker =
        factory.createConflictChecker(desFactory);
    final SICProperty6Verifier verifier =
        new SICProperty6Verifier(conflictChecker, null, desFactory);
    return verifier;
  }

  protected String getSuccessDescription()
  {
    return "satisfies SIC Property VI";
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
