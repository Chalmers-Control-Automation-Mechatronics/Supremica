//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MonolithicMazeTest
//###########################################################################
//# $Id: MonolithicMazeTest.java,v 1.1 2006-11-03 05:18:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import net.sourceforge.waters.analysis.monolithic.
  MonolithicControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicMazeTest extends AbstractMazeTest
{

  //#########################################################################
  //# Overrides for net.sourceforge.waters.samples.maze.AbstractMazeTest
  protected ControllabilityChecker getControllabilityChecker
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
  {
    return new MonolithicControllabilityChecker(des, factory);
  }

}
