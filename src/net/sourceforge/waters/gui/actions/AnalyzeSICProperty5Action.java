//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeSICProperty5Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.hisc.SICProperty5Verifier;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


/**
 * The action to check Serial Interface Consistency Property V (SIC&nbsp;V) of
 * a HISC module.
 *
 * @author Robi Malik
 */

public class AnalyzeSICProperty5Action
  extends WatersAnalyzeHISCAction
{

  //#########################################################################
  //# Constructor
  protected AnalyzeSICProperty5Action(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Overrides for base class
  //# net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected String getCheckName()
  {
    return "SIC Property V";
  }

  @Override
  protected String getFailureDescription()
  {
    return "does not satisfy SIC Property V";
  }

  @Override
  protected ModelVerifier getModelVerifier
    (final ModelAnalyzerFactory factory,
     final ProductDESProxyFactory desFactory) throws AnalysisConfigurationException
  {
    final ConflictChecker conflictChecker =
        factory.createConflictChecker(desFactory);
    final SICProperty5Verifier verifier =
        new SICProperty5Verifier(conflictChecker, null, desFactory);
    return verifier;
  }

  @Override
  protected String getSuccessDescription()
  {
    return "satisfies SIC Property V";
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1008097797553564719L;

}
