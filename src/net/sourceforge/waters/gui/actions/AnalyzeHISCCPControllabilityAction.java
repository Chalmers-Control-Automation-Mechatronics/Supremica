//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeHISCCPControllabilityAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import org.supremica.gui.ide.IDE;


/**
 * The action to invoke an HISC-CP controllability check.
 * This invokes just a standard controllability check.
 * The only difference to {@link AnalyzeControllabilityAction} is that
 * the module is compiled differently for HISC, with only the interfaces
 * of instantiated modules included.
 *
 * @author Robi Malik
 */

public class AnalyzeHISCCPControllabilityAction
  extends WatersAnalyzeHISCAction
{

  //#########################################################################
  //# Constructor
  protected AnalyzeHISCCPControllabilityAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Overrides for base class
  //# net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected String getCheckName()
  {
    return "HISC-CP Controllability";
  }

  @Override
  protected String getFailureDescription()
  {
    return "is not locally controllable";
  }

  @Override
  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    return factory.createControllabilityChecker(desFactory);
  }

  @Override
  protected String getSuccessDescription()
  {
    return "is locally controllable";
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
