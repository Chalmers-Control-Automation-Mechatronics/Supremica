//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   MonolithicModelAnalyzerFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory interface for all types of model verifiers.
 *
 * @author Robi Malik
 */

public class MonolithicModelAnalyzerFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static MonolithicModelAnalyzerFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final MonolithicModelAnalyzerFactory INSTANCE =
      new MonolithicModelAnalyzerFactory();
  }


  //#########################################################################
  //# Constructors
  private MonolithicModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public MonolithicConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicConflictChecker(factory);
  }

  @Override
  public MonolithicControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicControllabilityChecker(factory);
  }

  @Override
  public MonolithicControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicControlLoopChecker(factory);
  }

  @Override
  public MonolithicLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicLanguageInclusionChecker(factory);
  }

  @Override
  public MonolithicSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicSynthesizer(factory);
  }

}
