//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICPropertyVVerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.despot;

import net.sourceforge.waters.analysis.gnonblocking.CompositionalGeneralisedConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class LargeCompositionalSICPropertyVVerifierTest extends
    AbstractLargeSICPropertyVVerifierTest
{

  protected ModelVerifier createModelVerifier(
                                              final ProductDESProxyFactory factory)
  {
    final CompositionalGeneralisedConflictChecker checker =
        new CompositionalGeneralisedConflictChecker(factory);
    checker.setInternalStepNodeLimit(10000);
    checker.setFinalStepNodeLimit(1000000);
    checker.setTransitionLimit(1000000);
    return new SICPropertyVVerifier(checker, factory);
  }
}
