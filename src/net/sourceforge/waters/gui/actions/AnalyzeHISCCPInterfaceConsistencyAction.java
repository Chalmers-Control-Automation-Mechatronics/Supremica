//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeHISCCPInterfaceConsistencyAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.analysis.compositional.CompositionalSimplifier;
import net.sourceforge.waters.analysis.hisc.HISCCPInterfaceConsistencyChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import org.supremica.gui.ide.IDE;


/**
 * The action to invoke the HISC-CP interface consistency check.
 *
 * @author Robi Malik
 */

public class AnalyzeHISCCPInterfaceConsistencyAction
  extends WatersAnalyzeHISCAction
{

  //#########################################################################
  //# Constructor
  protected AnalyzeHISCCPInterfaceConsistencyAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Overrides for base class
  //# net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected String getCheckName()
  {
    return "HISC-CP Interface Consistency";
  }

  @Override
  protected String getFailureDescription()
  {
    return "is not interface consistent";
  }

  @Override
  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final ConflictChecker checker = factory.createConflictChecker(desFactory);
    if (checker == null) {
      return null;
    } else {
      final CompositionalSimplifier simplifier =
        new CompositionalSimplifier(desFactory);
      return new HISCCPInterfaceConsistencyChecker
        (desFactory, checker, simplifier);
    }
  }

  @Override
  protected String getSuccessDescription()
  {
    return "is interface consistent";
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
