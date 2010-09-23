//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   NativeSICProperty5VerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import net.sourceforge.waters.analysis.hisc.SICProperty5Verifier;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NativeSICProperty5VerifierTest extends
    AbstractSICProperty5VerifierTest
{

  protected ModelVerifier createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final ConflictChecker checker = new NativeConflictChecker(factory);
    return new SICProperty5Verifier(checker, factory);
  }

}
