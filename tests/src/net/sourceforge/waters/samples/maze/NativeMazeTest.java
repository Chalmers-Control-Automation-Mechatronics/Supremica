//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   NativeMazeTest
//###########################################################################
//# $Id: NativeMazeTest.java,v 1.2 2006-11-03 05:18:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NativeMazeTest extends AbstractMazeTest
{

  //#########################################################################
  //# Overrides for net.sourceforge.waters.samples.maze.AbstractMazeTest
  protected ControllabilityChecker getControllabilityChecker
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
  {
    return new NativeControllabilityChecker(des, factory);
  }

}
