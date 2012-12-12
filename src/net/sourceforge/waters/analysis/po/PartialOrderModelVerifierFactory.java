//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   MonolithicModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.po;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory interface for all types of model verifiers.
 *
 * @author Robi Malik
 */

public class PartialOrderModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static PartialOrderModelVerifierFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final PartialOrderModelVerifierFactory INSTANCE =
      new PartialOrderModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private PartialOrderModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public ConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return null;
  }

  @Override
  public PartialOrderControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new PartialOrderControllabilityChecker(factory);
  }

  @Override
  public ControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return null;
  }

  @Override
  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new PartialOrderLanguageInclusionChecker(factory);
  }

}
