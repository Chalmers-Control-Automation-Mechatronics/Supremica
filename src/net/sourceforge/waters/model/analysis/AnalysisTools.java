//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AutomatonTools
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.analysis.monolithic.MonolithicModelAnalyzerFactory;
import net.sourceforge.waters.cpp.analysis.NativeModelVerifierFactory;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * A collection of static methods to perform analysis operations.
 *
 * @author Robi Malik
 */

public final class AnalysisTools
{

  //########################################################################
  //# Invocation
  /**
   * Checks whether the given automaton is nonblocking.
   */
  public static boolean isNonBlocking(final AutomatonProxy aut)
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(aut, factory);
    try {
      final ConflictChecker checker = getDefaultConflictChecker(des);
      return checker.run();
    } catch (final EventNotFoundException exception) {
      return true;
    } catch (final AnalysisException exception) {
      throw exception.getRuntimeException();
    }
  }


  //########################################################################
  //# Auxiliary Methods
  private static ConflictChecker getDefaultConflictChecker
    (final ProductDESProxy des)
    throws AnalysisConfigurationException
  {
    final ProductDESProxyFactory desFactory =
      ProductDESElementFactory.getInstance();
    ConflictChecker checker;
    try {
      final ModelAnalyzerFactory vFactory =
        NativeModelVerifierFactory.getInstance();
      checker = vFactory.createConflictChecker(desFactory);
    } catch (final UnsatisfiedLinkError | AnalysisConfigurationException error) {
      final ModelAnalyzerFactory vFactory =
        MonolithicModelAnalyzerFactory.getInstance();
      checker = vFactory.createConflictChecker(desFactory);
    }
    checker.setModel(des);
    return checker;
  }
}
