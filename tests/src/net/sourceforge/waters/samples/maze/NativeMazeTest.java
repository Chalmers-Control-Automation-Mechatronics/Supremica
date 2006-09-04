//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MazeTest
//###########################################################################
//# $Id: NativeMazeTest.java,v 1.1 2006-09-04 15:42:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NativeMazeTest extends AbstractMazeTest
{

  //#########################################################################
  //# Overrides for net.sourceforge.waters.samples.maze.AbstractMazeTest
  protected VerificationResult checkControllability
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
  {
    final NativeControllabilityChecker checker =
      new NativeControllabilityChecker(des, factory);
    checker.run();
    return checker.getAnalysisResult();
  }

}
