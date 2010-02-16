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
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class LargeCompositionalSICPropertyVVerifierTest extends
    AbstractLargeSICPropertyVVerifierTest
{

  protected ModelVerifier createModelVerifier(
                                              final ProductDESProxyFactory factory)
  {
    final ConflictChecker checker =
        new CompositionalGeneralisedConflictChecker(factory);
    return new SICPropertyVVerifier(checker, factory);
  }
}
