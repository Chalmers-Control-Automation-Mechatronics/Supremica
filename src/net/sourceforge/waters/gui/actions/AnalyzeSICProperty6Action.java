//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty6Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.hisc.SICProperty6Verifier;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


/**
 * The action to check Serial Interface Consistency Property VI (SIC&nbsp;VI)
 * of a HISC module.
 *
 * @author Robi Malik
 */

public class AnalyzeSICProperty6Action
  extends WatersAnalyzeHISCAction
{

  //#########################################################################
  //# Constructor
  protected AnalyzeSICProperty6Action(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Overrides for base class
  //# net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected String getCheckName()
  {
    return "SIC Property VI";
  }

  @Override
  protected String getFailureDescription()
  {
    return "does not satisfy SIC Property VI";
  }

  @Override
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

  @Override
  protected String getSuccessDescription()
  {
    return "satisfies SIC Property VI";
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1008097797553564719L;

}
